<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item prop="dbType" label="数据源类型">
				<el-select v-model="dataForm.groupName" clearable placeholder="数据源类型" style="width: 100%">
					<el-option value="MySQL" label="MySQL"></el-option>
					<el-option value="Oracle" label="Oracle"></el-option>
					<el-option value="PostgreSQL" label="PostgreSQL"></el-option>
					<el-option value="SQLServer" label="SQLServer"></el-option>
					<el-option value="DM" label="达梦8"></el-option>
					<el-option value="Mongo" label="Mongo"></el-option>
					<el-option value="Kafka" label="Kafka"></el-option>
					<el-option value="ElasticSearch" label="ElasticSearch"></el-option>
					<el-option value="Api" label="Api"></el-option>
				</el-select>
			</el-form-item>
			<el-form-item label="字段类型" prop="columnType">
				<el-input v-model="dataForm.columnType" placeholder="字段类型"></el-input>
			</el-form-item>
			<el-form-item label="属性类型" prop="attrType">
				<el-input v-model="dataForm.attrType" placeholder="属性类型"></el-input>
			</el-form-item>
			<el-form-item label="属性包名" prop="packageName">
				<el-input v-model="dataForm.packageName" placeholder="属性包名"></el-input>
			</el-form-item>
		</el-form>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { useFieldTypeApi, useFieldTypeSubmitApi } from '@/api/fieldType'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	id: '',
	columnType: '',
	attrType: '',
	packageName: '',
	groupName:''
})

const init = (id?: number) => {
	visible.value = true
	dataForm.id = ''
	dataForm.groupName=''
	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	// id 存在则为修改
	if (id) {
		getFieldType(id)
	}
}

const getFieldType = (id: number) => {
	useFieldTypeApi(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const dataRules = ref({
	columnType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	attrType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	groupName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	packageName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		useFieldTypeSubmitApi(dataForm).then(() => {
			ElMessage.success({
				message: '操作成功',
				duration: 500,
				onClose: () => {
					visible.value = false
					emit('refreshDataList')
				}
			})
		})
	})
}

defineExpose({
	init
})
</script>

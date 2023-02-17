<template>
	<el-dialog v-model="visible" :title="!dataForm.id ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item label="规则名" prop="name">
				<el-input v-model="dataForm.name" placeholder="规则名"></el-input>
			</el-form-item>
			<el-form-item label="类型" prop="type">
				<el-select v-model="dataForm.type" style="width: 100%" placeholder="类型">
					<el-option v-for="ds in dataType" :key="ds.name" :label="ds.name" :value="ds.value"> </el-option>
				</el-select>
			</el-form-item>

			<el-form-item label="规则描述" prop="description">
				<el-input v-model="dataForm.description" placeholder="规则"></el-input>
			</el-form-item>
			<el-form-item label="关联字段" prop="description">
				<el-input v-model="dataForm.relativeFieldName" placeholder="关联字段 如: 字段1,字段2"></el-input>
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
import { useSaveOrUpdateMockApi } from '@/api/mock'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()
const dataType = reactive([
	{
		value: '1',
		name: '默认'
	},
	{
		value: '2',
		name: '自定义'
	}
])

const dataForm = ref({
	id: '',
	name: '',
	type: '',
	description: '',
	relativeFieldName: ''
})

const init = (data: any) => {
	visible.value = true
	// 重置表单数据
	dataForm.value = { id: '', name: '', type: '', description: '', relativeFieldName: '' }
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}
	// id 存在则为修改
	if (data.id) {
		Object.assign(dataForm.value, data)
	}
}

const dataRules = ref({
	columnType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	attrType: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		useSaveOrUpdateMockApi(dataForm.value).then(() => {
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

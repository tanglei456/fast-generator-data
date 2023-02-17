<template>
	<el-dialog v-model="visible" title="表信息" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item label="表名"  prop="tableName">
				<el-input v-model="dataForm.tableName" :disabled="true" placeholder="表名"></el-input>
			</el-form-item>
			<el-form-item label="说明" prop="tableComment">
				<el-input v-model="dataForm.tableComment" placeholder="说明"></el-input>
			</el-form-item>
			<el-form-item label="数据量" prop="dataNumber">
				<el-input v-model="dataForm.dataNumber" placeholder="数据量"></el-input>
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
import { useTableApi,useTableSubmitApi } from '@/api/table'
const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()
const tableId = ref()

const dataForm = reactive({
	id: '',
	tableName: '',
	tableComment: '',
	dataNumber: ''
})

const init = (id?: number) => {
	visible.value = true
	tableId.value = id

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}
	getTable(id)
}



const dataRules = ref({
	tableName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	dataNumber: [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
})

const getTable = (id: number) => {
	useTableApi(id).then(res => {
		dataForm.id = res.data.id;
		dataForm.tableName = res.data.tableName;
		dataForm.tableComment = res.data.tableComment;
		dataForm.dataNumber = res.data.dataNumber;
	})
}

// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		useTableSubmitApi(dataForm).then(() => {
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

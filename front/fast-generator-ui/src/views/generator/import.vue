<template>
	<el-dialog v-model="visible" title="导入表" :close-on-click-modal="false" draggable>
		<el-tabs v-model="activeName">
			<el-tab-pane label="数据源导入" direction="btt" name="field">
				<el-form ref="dataFormRef" :model="dataForm">
					<el-form-item label="数据源" prop="datasourceId">
						<el-select filterable v-model="dataForm.datasourceId" style="width: 100%" placeholder="请选择数据源" @change="getTableList">
							<el-option v-for="ds in dataForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option>
						</el-select>
					</el-form-item>
					<el-table :data="dataForm.tableList" border style="width: 100%" :max-height="400" @selection-change="selectionChangeHandle">
						<el-table-column type="selection" header-align="center" align="center" width="60"></el-table-column>
						<el-table-column prop="tableName" label="表名" header-align="center" align="center"></el-table-column>
						<el-table-column prop="tableComment" label="表说明" header-align="center" align="center"></el-table-column>
					</el-table>
				</el-form>
				<div style="margin-top:20px;margin-right:20px;float:right">
					<el-button @click="visible = false">取消</el-button>
					<el-button type="primary" @click="submitHandle()">确定</el-button>
				</div>
			</el-tab-pane>
			<el-tab-pane label="json导入" direction="btt" name="json">
				<el-form ref="dataFormRef" :model="dataForm">
					<el-form-item label="数据源" prop="datasourceId">
						<el-select filterable v-model="dataForm.datasourceId" style="width: 100%" placeholder="请选择数据源" @change="getTableList">
							<el-option v-for="ds in dataForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option>
						</el-select>
					</el-form-item>
					<el-form-item label="JSON" prop="file">
						<el-input type="textarea" :rows="6" v-model="dataForm.file"> </el-input>
						<!-- <el-upload  name="file"  v-model="dataForm.file" action="http://localhost:8088/fast-generator-data/gen/table/import/template" class="upload-demo" drag  multiple>
							<el-icon class="el-icon--upload"><upload-filled /></el-icon>
							<div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
							<template #tip>
								<div class="el-upload__tip">仅支持上传.json,.txt文件</div>
							</template>
						</el-upload> -->
					</el-form-item>
				</el-form>
				<div style="margin-top:10px;margin-right:10px;float:right">
					<el-button @click="visible = false">取消</el-button>
					<el-button type="primary" @click="submitImortHandle()">确定</el-button>
				</div>
			</el-tab-pane>
		</el-tabs>
	</el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus/es'
import { useDataSourceListApi } from '@/api/datasource'
import { useTableImportSubmitApi, useTableImportTemplateSubmitApi } from '@/api/table'
import { useDataSourceTableListApi } from '@/api/datasource'

const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()
const activeName = ref()
const dataForm = reactive({
	id: '',
	tableNameListSelections: [] as any,
	datasourceId: '',
	datasourceList: [] as any,
	tableList: [] as any,
	file: '',
	table: {
		tableName: ''
	}
})

// 多选
const selectionChangeHandle = (selections: any[]) => {
	dataForm.tableNameListSelections = selections.map((item: any) => item['tableName'])
}

const init = () => {
	visible.value = true
	dataForm.id = ''
	activeName.value = 'field'
	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	dataForm.tableList = []

	getDataSourceList()
}

const getDataSourceList = () => {
	useDataSourceListApi().then(res => {
		dataForm.datasourceList = res.data
	})
}

const getTableList = () => {
	dataForm.table.tableName = ''
	useDataSourceTableListApi(dataForm.datasourceId).then(res => {
		dataForm.tableList = res.data
	})
}

// 表单提交
const submitHandle = () => {
	const tableNameList = dataForm.tableNameListSelections ? dataForm.tableNameListSelections : []
	if (tableNameList.length === 0) {
		ElMessage.warning('请选择记录')
		return
	}

	useTableImportSubmitApi(dataForm.datasourceId, tableNameList).then(() => {
		ElMessage.success({
			message: '操作成功',
			duration: 500,
			onClose: () => {
				visible.value = false
				emit('refreshDataList')
			}
		})
	})
}

// 表单提交
const submitImortHandle = () => {
	if (dataForm.datasourceId === null) {
		ElMessage.warning('请选择数据源')
		return
	}
	useTableImportTemplateSubmitApi(dataForm.datasourceId, dataForm.file).then(() => {
		ElMessage.success({
			message: '操作成功',
			duration: 500,
			onClose: () => {
				visible.value = false
				emit('refreshDataList')
			}
		})
	})
}

defineExpose({
	init
})
</script>

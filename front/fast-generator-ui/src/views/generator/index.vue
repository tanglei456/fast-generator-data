<template>
	<el-card>
		<el-form :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item  label="模板名:" prop="datasourceId">
				<el-select filterable   v-model="state.queryForm.datasourceId" @click="getDataSourceList(true)" style="width: 100%">
					<el-option   @click="getDataList()" label="全部" value=""></el-option>
					<el-option  @click="getDataList()" v-for="ds in dataForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option> 
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-input v-model="state.queryForm.tableName" placeholder="表名"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="importHandle()">表导入</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="success" @click="batchGenerator('1')">生成数据</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="success" @click="batchGenerator('2')">生成dbf</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="success" @click="batchGenerator('3')">生成excel</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="progressHandle(1)">数据生成进度</el-button>
			</el-form-item>
		</el-form>
		
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border style="width: 100%" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column prop="datasourceName" label="数据源" header-align="center" align="center"></el-table-column> 
			<el-table-column prop="tableName" label="表名" header-align="center" align="center">
				<template v-slot="scope">
					<a  style="color:cornflowerblue" @click="tableEditHandle(scope.row.id)">{{ scope.row.tableName }}</a>
				</template>
			</el-table-column>
			<el-table-column prop="tableComment" label="表说明"  header-align="center" align="center"></el-table-column>
			<el-table-column prop="dataNumber" label="数据量" header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="250">
				<template #default="scope">
					<el-button type="primary" link @click="editHandle(scope.row.id,scope.row.tableName)">编辑</el-button>
					<el-button type="primary" link @click="syncHandle(scope.row)">同步</el-button>
					<el-button type="primary" link @click="generatorData(scope.row.id)">生成数据</el-button>
					<el-button type="primary" link @click="generatorExcel(scope.row.id)">生成excel</el-button>
					<el-button type="primary" link @click="generatorDbf(scope.row.id)">生成dbf</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.id)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
		<el-dialog draggable v-model="dialogVisible" title="提示" width="30%" :before-close="handleClose">
			<template  #footer>
				<span class="dialog-footer">
					<el-button type="primary" @click="sync('1')"> 智能合并 </el-button>
					<el-button type="primary" @click="sync('2')"> 强制覆盖 </el-button>
					<el-button @click="dialogVisible = false"> 取消 </el-button>
				</span>
			</template>
			<span>{{ tips }}</span>
		
		</el-dialog>
		<el-pagination
			:current-page="state.page"
			:page-sizes="state.pageSizes"
			:page-size="state.limit"
			:total="state.total"
			layout="total, sizes, prev, pager, next, jumper"
			@size-change="sizeChangeHandle"
			@current-change="currentChangeHandle"
		>
		</el-pagination>

		<import ref="importRef" @refreshDataList="getDataList"></import>
		<edit ref="editRef" @refreshDataList="getDataList"></edit>
		<myProgress ref="progreesRef" ></myProgress>
		<tableEdit ref="tableEditRef" @refreshDataList="getDataList"></tableEdit>
		
	</el-card>
	
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import { useCrud } from '@/hooks'
import Import from './import.vue'
import Edit from './edit.vue'
import TableEdit from './tableEdit.vue'
import MyProgress from './myProgress.vue'
import fileDownload from "js-file-download";
import { useGeneratorApi,useGeneratorExcel ,useGeneratorDbf} from '@/api/generator'
import { useTableSyncApi } from '@/api/table'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useDownloadApi } from '@/api/generator'
import { constant } from 'lodash'
import { useDataSourceListApi } from '@/api/datasource'


const state: IHooksOptions = reactive({
	dataListUrl: '/gen/table/page',
	deleteUrl: '/gen/table',
	queryForm: {
		tableName: '',
		datasourceId:''
	}
})
const dialogVisible = ref(false)
const tips = ref()
const id = ref()
const importRef = ref()
const editRef = ref()
const tableEditRef = ref()
const progreesRef = ref()




const dataForm = reactive({
	id: '',
	tableNameListSelections: [] as any,
	datasourceId: '',
	datasourceList: [] as any,
	tableList: [] as any,
	table: {
		tableName: ''
	}
})


const importHandle = (id?: number) => {
	console.log(importRef.value)
	importRef.value.init(id)
}

const editHandle = (id: number,tableName:string) => {
	editRef.value.init(id,tableName)
}

const progressHandle = (id:any) => {
	progreesRef.value.init(id)
}

const tableEditHandle = (id?: number) => {
	console.log(tableEditRef.value)
	tableEditRef.value.init(id)
}

const getDataSourceList = (flag:boolean) => {
	useDataSourceListApi(flag).then(res => {
		dataForm.datasourceList = res.data
	})
}

// const generatorHandle = (id?: number) => {
// 	generatorRef.value.init(id)
// }

const generatorData = (id?: any) => {
	ElMessageBox.confirm(`确定要生成测试数据吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			//生成进度条
			progressHandle(id);
			if (!(id instanceof Array)) {
				id = [id]
			}
			useGeneratorApi(id).then(() => {
				ElMessage.success('执行成功')
			})
		})
		.catch(() => {})
}

const batchGenerator = (type:string) => {
	const tableIds = state.dataListSelections ? state.dataListSelections : []

	if (tableIds.length === 0) {
		ElMessage.warning('请选择生成数据的表')
		return
	}
	if('1'===type){
		generatorData(tableIds)
	}
	if('2'===type){
		generatorDbf(tableIds)
	}
	if('3'===type){
		generatorExcel(tableIds)
	}
}

const generatorDbf = (id?: any) => {

	ElMessageBox.confirm(`确定要生成dbf吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			//生成进度条
			progressHandle(id);
			if (!(id instanceof Array)) {
				id = [id]
			}
			useGeneratorDbf(id).then((response) => {
				fileDownload(response,'test.dbf'); 
				ElMessage.success('执行成功')
			})
		})
		.catch(() => {})
}

const generatorExcel = (id?: any) => {
	ElMessageBox.confirm(`确定要生成excel吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	})
		.then(() => {
			//生成进度条
			progressHandle(id);
			if (!(id instanceof Array)) {
				id = [id]
			}
			useGeneratorExcel(id).then((response) => {
				// fileDownload(response,'test.xlsx'); 
				ElMessage.success('执行成功')
			})
		})
		.catch(() => {})

}

const syncHandle = (row: any) => {
	dialogVisible.value=true
	tips.value='确定同步数据表'+row.tableName+'吗?'
	id.value=row.id;
	
}

const sync= (type:String) => {
	dialogVisible.value=false;
	useTableSyncApi(id.value,type).then(() => {
				ElMessage.success('执行成功')
			})
}

const handleClose = (done: () => void) => {
	dialogVisible.value = false
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)

</script>

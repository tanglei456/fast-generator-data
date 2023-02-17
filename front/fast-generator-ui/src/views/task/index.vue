<template>
	<el-card>
		<el-form :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item  label="任务组:" prop="group">
				<el-select filterable   v-model="state.queryForm.jobGroup" @click="getDataSourceList(true)" style="width: 100%">
					<el-option  @click="getDataList()" label="全部" value=""></el-option>
					<el-option   @click="getDataList()" v-for="ds in dataForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option> 
				</el-select>
			</el-form-item>
			<el-form-item  label="状态:" prop="status">
				<el-select filterable   v-model="state.queryForm.status"  style="width: 100%">
					<el-option @click="getDataList()" label="全部" value=""></el-option>
					<el-option @click="getDataList()"  label="正常" value="0"></el-option>
					<el-option @click="getDataList()" label="暂停" value="1"></el-option>
					<!-- <el-option v-for="ds in dataForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option>  -->
				</el-select>
			</el-form-item>
			<el-form-item>
				<el-input v-model="state.queryForm.jobName" placeholder="任务名称"></el-input>
			</el-form-item>
			<el-form-item>
				<el-button @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button @click="addOrUpdateHandle()" type="primary">新增</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
			</el-form-item>
		</el-form>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border style="width: 100%" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column prop="jobName" label="任务名称" header-align="center" align="center"></el-table-column>
			<el-table-column prop="jobGroup" label="任务组" header-align="center" align="center"></el-table-column>
			<el-table-column prop="cronExpression" label="cron执行表达式" show-overflow-tooltip header-align="center" align="center"></el-table-column>
			<el-table-column prop="status" label="状态" header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="240">
				<template #default="scope">
					<el-button type="primary" link @click="runTask(scope.row.jobId)">立即执行</el-button>
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row.jobId)">编辑</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.jobId)">删除</el-button>
				</template>
			</el-table-column>
		</el-table>
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

		<!-- 弹窗, 新增 / 修改 -->
		<add-or-update ref="addOrUpdateRef" @refreshDataList="getDataList"></add-or-update>
	</el-card>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { IHooksOptions } from '@/hooks/interface'
import { listJob,runJob } from '@/api/task'
import { useDataSourceListApi } from '@/api/datasource'
import { useCrud } from '@/hooks'
import AddOrUpdate from './add-or-update.vue'
import { ElMessage, ElMessageBox } from 'element-plus'

const dataForm = reactive({
	id: '',
	tableNameListSelections: [] as any,
	datasourceId: '',
	datasourceList: [] as any,
	tableList: [] as any,
	table: {
		group: '',

	}
})


const state: IHooksOptions = reactive({
	dataListUrl: '/gen/job/list',
	deleteUrl: '/gen/job',
	queryForm: {
		jobGroup: '',
		status: '',
		jobName:'',
		limit:10,
		page:1
	}
})

const datasourceHandle = (id: number) => {
	// useDataSourceTestApi(id).then((res: any) => {
	// 	ElMessage.success(res.data)
	// })
}

const getDataSourceList = (flag:boolean) => {
	useDataSourceListApi(flag).then(res => {
		dataForm.datasourceList = res.data
	})
}

const runTask =(id:number)=>{
	runJob(id).then(res => {
		ElMessageBox.confirm(`确定要执行任务吗?`, '提示', {
		confirmButtonText: '确定',
		cancelButtonText: '取消',
		type: 'warning'
	   })
		.then(() => {
			ElMessage.success('执行成功')
		})
		.catch(() => {})
	
	})
}


const addOrUpdateRef = ref()
const addOrUpdateHandle = (id?: number) => {
	console.log(id)
	addOrUpdateRef.value.init(id)
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>

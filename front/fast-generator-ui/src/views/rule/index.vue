<template>
	<el-card>
		<el-form :inline="true" :model="state.queryForm" @keyup.enter="getDataList()">
			<el-form-item>
				<el-form-item>
					<el-input v-model="state.queryForm.name" placeholder="规则名字"></el-input>
				</el-form-item>
				<el-form-item>
					<el-input v-model="state.queryForm.description" placeholder="规则描述"></el-input>
				</el-form-item>
			</el-form-item>
			<el-form-item>
				<el-button @click="getDataList()">查询</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="primary" @click="addOrUpdateHandle()">新增</el-button>
			</el-form-item>
			<el-form-item>
				<el-button type="danger" @click="deleteBatchHandle()">删除</el-button>
			</el-form-item>
		</el-form>
		<el-table v-loading="state.dataListLoading" :data="state.dataList" border style="width: 100%" @selection-change="selectionChangeHandle">
			<el-table-column type="selection" header-align="center" align="center" width="50"></el-table-column>
			<el-table-column prop="name" label="规则名字" header-align="center" align="center"></el-table-column>
			<el-table-column prop="description" label="规则描述" header-align="center" align="center"></el-table-column>
			<el-table-column prop="type" label="类型" header-align="center" align="center">
				<template #default="scope">
					{{ handelType(scope.row.type) }}
				</template>
			</el-table-column>
			<el-table-column prop="relativeFieldName" label="关联字段" header-align="center" align="center"></el-table-column>
			<el-table-column prop="createTime" label="创建时间" header-align="center" align="center"></el-table-column>
			<el-table-column label="操作" fixed="right" header-align="center" align="center" width="150">
				<template #default="scope">
					<el-button type="primary" link @click="addOrUpdateHandle(scope.row)">编辑</el-button>
					<el-button type="primary" link @click="deleteBatchHandle(scope.row.id)">删除</el-button>
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
import { useCrud } from '@/hooks'
import AddOrUpdate from './add-or-update.vue'

const state: IHooksOptions = reactive({
	dataListUrl: '/mock/rule/list',
	deleteUrl: '/mock/rule',
	queryForm: {
		name: '',
		description: ''
	}
})

const addOrUpdateRef = ref()
const addOrUpdateHandle = (data?:any) => {
	addOrUpdateRef.value.init(data)
}

// 处理类型
const handelType = (type: String) => {
	switch (type) {
		case '1': {
			return '默认'
		}
		case '2': {
			return '自定义'
		}
	}
}

const { getDataList, selectionChangeHandle, sizeChangeHandle, currentChangeHandle, deleteBatchHandle } = useCrud(state)
</script>

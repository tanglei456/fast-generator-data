<template>
	<el-drawer v-model="visible" title="编辑" size="100%" :with-header="false">
		<el-tabs v-model="activeName" @tab-click="handleClick">
			<el-tab-pane :label="tableName" direction="btt" name="field">
				<vxe-toolbar>
					<template #buttons>
						<vxe-button @click="$refs.xTable.setAllTreeExpand(true)">展开所有</vxe-button>
						<vxe-button @click="$refs.xTable.clearTreeExpand()">关闭所有</vxe-button>
						<vxe-button @click="insertEvent">新增</vxe-button>
					</template>
				</vxe-toolbar>
				<vxe-table
					row-key
					border
					show-overflow
					show-header-overflow
					class="sortable-row-gen"
					ref="xTable"
					:edit-config="{ trigger: 'click', mode: 'cell' }"
					:column-config="{ resizable: true }"
					:tree-config="{ transform: true, rowField: 'id', parentField: 'parentId' }"
					:checkbox-config="{ checkStrictly: true }"
					:data="fieldList"
				>
					<vxe-column type="seq" tree-node width="120"></vxe-column>

					<!-- <vxe-column width="120" field="id" title="#">
						<template #default="{ row }">
							<vxe-checkbox v-model="row.id"></vxe-checkbox>
						</template>
					</vxe-column> -->
					<!-- <vxe-column width="60" title="拖动">
						<template #default>
							<span class="drag-btn">
								<i class="vxe-icon-sort"></i>
							</span>
						</template>
						<template #header>
							<el-tooltip class="item" effect="dark" content="按住后可以上下拖动排序" placement="top-start">
								<i class="vxe-icon-question-circle-fill"></i>
							</el-tooltip>
						</template>
					</vxe-column> -->
					<vxe-column field="fieldName" :edit-render="{ name: 'input' }" title="字段名"></vxe-column>
					<vxe-column field="fieldComment" title="字段说明" :edit-render="{ name: 'input' }"></vxe-column>
					<vxe-column field="fieldType" title="字段类型"></vxe-column>
					<vxe-column field="attrType" title="数据类型">
						<template #default="{ row }">
							<vxe-select :disabled="!row.leaf" v-model="row.attrType">
								<vxe-option v-for="item in typeList" :key="item.value" :value="item.value" :label="item.label"></vxe-option>
							</vxe-select>
						</template>
					</vxe-column>
					<!-- :show-all-levels="false" -->
					<vxe-column width="180" field="foreignKeys" title="外键字段">
						<template #default="{ row }">
							<div class="m-4">
								<el-cascader v-if="row.leaf" filterable v-model="row.foreignKeys" :props="cascaderProps" @change="handleChange" clearable>
								</el-cascader>
							</div>
						</template>
					</vxe-column>

					<vxe-column width="250" field="mockName" title="mock">
						<template #default="{ row }">
							<el-dialog v-model="row.popup" title="mock规则" width="30%" >
								<el-autocomplete
									style="width: 100% "
									v-if="row.leaf"
									v-model="row.mockName"
									value-key="name"
									popper-class="my-autocomplete"
									:fetch-suggestions="querySearch"
									placeholder="规则"
									clearable
									show-word-limit
									:rows="6"
									type="textarea"
								>
								<template #default="{ item }">
										<div class="value">{{ item.name }}</div>
										<span class="link">{{ item.description }}</span>
									</template>
								</el-autocomplete>
								<template #footer>
									<span class="dialog-footer">
										<el-button @click="row.popup = false">取消</el-button>
										<el-button type="primary" @click="row.popup = false"> 确定 </el-button>
									</span>
								</template>
								
							</el-dialog>
							<div class="m-4">
								<el-autocomplete
									v-if="row.leaf"
									v-model="row.mockName"
									value-key="name"
									popper-class="my-autocomplete"
									:fetch-suggestions="querySearch"
									placeholder="规则"
									clearable
									show-word-limit
									select-when-unmatchedyy
								>
									<template #suffix>
										<el-icon class="el-input__icon" @click="row.popup = true">
											<edit />
										</el-icon>
									</template>
									<template #default="{ item }">
										<div class="value">{{ item.name }}</div>
										<span class="link">{{ item.description }}</span>
									</template>
								</el-autocomplete>

								<!-- <el-select v-if="row.leaf" clearable filterable allow-create default-first-option v-model="row.mockName">
									<el-option v-for="item in mockTypeList" :key="item.id" :value="item.name" :label="item.name">
										<span style="float: left">{{ item.name }}</span>
										<span style="float: right; color: var(--el-text-color-secondary); font-size: 13px">{{ item.description }}</span>
									</el-option>
								</el-select> -->
							</div>
						</template>
					</vxe-column>
					<vxe-column width="80" field="uniqueIndex" title="唯一索引">
						<template #default="{ row }">
							<vxe-checkbox v-model="row.uniqueIndex"></vxe-checkbox>
						</template>
					</vxe-column>
					<vxe-column width="80" field="autoIncrement" title="自增">
						<template #default="{ row }">
							<vxe-checkbox disabled v-model="row.autoIncrement"></vxe-checkbox>
						</template>
					</vxe-column>
					<vxe-column title="操作" width="300">
						<template #default="{ row }">
							<vxe-button type="text" status="primary" @click="insertRow(row, 'current')">新增字段</vxe-button>
							<vxe-button type="text" status="primary" @click="insertRow(row, 'top')">新增子字段</vxe-button>
							<vxe-button type="text" status="primary" @click="removeRow(row)">删除</vxe-button>
						</template>
					</vxe-column>
				</vxe-table>
			</el-tab-pane>
		</el-tabs>
		<template #footer>
			<el-button @click="visible = false">取消</el-button>
			<el-button type="primary" @click="submitHandle()">确定</el-button>
		</template>
	</el-drawer>
</template>

<script setup lang="ts">
import { nextTick, reactive, ref } from 'vue'
import { ElMessage, TabsPaneContext } from 'element-plus/es'
import Sortable from 'sortablejs'
import { useTableFieldSubmitApi } from '@/api/table'
import { useTableApi, useTableTree } from '@/api/table'
import { useMockListApi } from '@/api/mock'
import { useFieldTypeListApi } from '@/api/fieldType'
import { da } from 'element-plus/es/locale'
import type { CascaderProps } from 'element-plus'
import { VXETable, VxeTableInstance, VxeToolbarInstance } from 'vxe-table'
import { Edit } from '@element-plus/icons-vue'
import QueryString from 'qs'
const state = ref('')

const activeName = ref()
const fieldTable = ref<VxeTableInstance>()
const formTable = ref<VxeTableInstance>()
const gridTable = ref<VxeTableInstance>()
const value = ref([])
const datasourceId = ref()
const props = {
	expandTrigger: 'hover' as const
}

const handleChange = (value: any) => {
	console.log(value)
}

const handleClick = (tab: TabsPaneContext) => {
	if (tab.paneName !== 'field') {
		formTable.value?.loadData(fieldList.value)
		gridTable.value?.loadData(fieldList.value)
	}
}

const emit = defineEmits(['refreshDataList'])
const visible = ref(false)
const dataFormRef = ref()
let tableTree = reactive([{ label: '', value: '', parentId: '', children: [], leaf: false }])
const sortable = ref() as any
const xTable = ref<VxeTableInstance>()
const typeList = ref([]) as any
let mockTypeList = reactive([]) as any
const tableId = ref()
const tableName = ref()
const fieldList = ref([])

const init = (id: number, name: string) => {
	visible.value = true
	tableId.value = id
	tableName.value = '数据规则设置(' + name + ')'
	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	activeName.value = 'field'

	rowDrop()
	getTable(id)
	getFieldTypeList()
	getMockList()
}

const cascaderProps: CascaderProps = {
	lazy: true,
	lazyLoad(node, resolve) {
		setTimeout(() => {
			resolve(tableTree)
		}, 1000)
	}
}
const querySearch = (queryString: any, cb: any) => {
	var results = queryString.length == 0 || queryString == 'null' ? mockTypeList : mockTypeList.filter(createFilter(queryString))
	// 调用 callback 返回建议列表的数据
	cb(results)
}
const createFilter = (queryString: any) => {
	return (restaurant: any) => {
		//模糊匹配
		return restaurant.name.toLowerCase().match(queryString.toLowerCase()) ? true : false
	}
}

const handleSelect = (item: any) => {
	console.log(item.name)
	return item.name
}

/**
 * 节点增加
 * @param currRow
 * @param locat
 */
const insertRow = async (currRow: any, locat: string) => {
	const $table = xTable.value
	const date = new Date()
	// 如果 null 则插入到目标节点顶部
	// 如果 -1 则插入到目标节点底部
	// 如果 row 则有插入到效的目标节点该行的位置

	if (locat === 'current') {
		let record = {
			fieldName: '新数据',
			id: Date.now(),
			attrType: null,
			mockName: null,
			uniqueIndex: null,
			autoIncrement: null,
			leaf: true,
			tableId: currRow.tableId,
			parentId: currRow.parentId, // 需要指定父节点，自动插入该节点中
			date: `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
		}
		const { row: newRow } = await $table.insertAt(record, currRow)
		fieldList.value.push(newRow)
		await $table.setEditRow(newRow) // 插入子节点
	} else if (locat === 'top') {
		let record = {
			fieldName: '新数据',
			id: Date.now(),
			attrType: null,
			mockName: null,
			tableId: currRow.tableId,
			uniqueIndex: null,
			leaf: true,
			autoIncrement: null,
			parentId: currRow.id, // 需要指定父节点，自动插入该节点中
			date: `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
		}
		const { row: newRow } = await $table.insert(record)
		fieldList.value.push(newRow)

		await $table.setTreeExpand(currRow, true) // 将父节点展开
		await $table.setEditRow(newRow) // 插入子节点
	}
}

const insertEvent = async () => {
	const $table = xTable.value
	const date = new Date()
	let record = {
		fieldName: '新数据',
		id: Date.now(),
		attrType: null,
		mockName: null,
		uniqueIndex: null,
		autoIncrement: null,
		tableId: tableId.value,
		leaf: true,
		parentId: null, // 父节点必须与当前行一致
		date: `${date.getFullYear()}-${date.getMonth() + 1}-${date.getDate()}`
	}

	const { row: newRow } = await $table.insert(record)
	fieldList.value.push(newRow)
	await $table.setEditRow(newRow)
}

/**
 * 节点删除
 * @param row
 */
const removeRow = async (row: any) => {
	const $table = xTable.value
	fieldList.value = fieldList.value.filter(item => {
		return item.id != row.id
	})
	await $table.remove(row)
}

const rowDrop = () => {
	nextTick(() => {
		const el: any = window.document.querySelector('.body--wrapper>.vxe-table--body tbody')
		sortable.value = Sortable.create(el, {
			handle: '.drag-btn',
			onEnd: (e: any) => {
				const { newIndex, oldIndex } = e
				const currRow = fieldList.value.splice(oldIndex, 1)[0]
				fieldList.value.splice(newIndex, 0, currRow)
			}
		})
	})
}

/**
 *
 * @param id 获取表列表
 */
const getTable = (id: number) => {
	useTableApi(id).then(res => {
		datasourceId.value = res.data.datasourceId
		fieldList.value = res.data.fieldList
		getTreeTable(id)
	})
}

/**
 *
 * @param id 获取级联数据
 */
const getTreeTable = (id: number) => {
	const ids = [datasourceId.value, id]
	useTableTree(ids).then(res => {
		tableTree = res.data
	})
}

const getMockList = async () => {
	mockTypeList = []
	// 获取数据
	const { data } = await useMockListApi()
	mockTypeList = data.list
}

const getFieldTypeList = async () => {
	typeList.value = []
	// 获取数据
	const { data } = await useFieldTypeListApi()
	// 设置属性类型值
	data.forEach((item: any) => typeList.value.push({ label: item, value: item }))
	// 增加Object类型
	typeList.value.push({ label: 'Object', value: 'Object' })
}
// 表单提交
const submitHandle = () => {
	useTableFieldSubmitApi(tableId.value, fieldList.value).then(() => {
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

<style lang="scss">
.sortable-row-gen .drag-btn {
	cursor: move;
	font-size: 12px;
}
.vxe-select--panel {
	position: fixed !important;
	min-width: 10% !important;
	left: auto !important;
}
.my-autocomplete li {
	line-height: normal;
	padding: 7px;
}
.my-autocomplete li .name {
	text-overflow: ellipsis;
	overflow: hidden;
}
.my-autocomplete li .addr {
	font-size: 12px;
	color: #b4b4b4;
}
.my-autocomplete li .highlighted .addr {
	color: #ddd;
}
.sortable-row-gen .vxe-body--row.sortable-ghost,
.sortable-row-gen .vxe-body--row.sortable-chosen {
	background-color: #dfecfb;
}
</style>

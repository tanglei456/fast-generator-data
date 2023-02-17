<template>
	<el-dialog v-model="visible" :title="!dataForm.jobId ? '新增' : '修改'" :close-on-click-modal="false">
		<el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="120px" @keyup.enter="submitHandle()">
			<el-form-item label="名称:" prop="jobName">
				<el-input v-model="dataForm.jobName" placeholder="名称">
				</el-input>
			</el-form-item>
			<el-form-item  label="分组:" prop="jobGroup">
				<el-select filterable   v-model="dataForm.jobGroup" @click="getDataSourceList" style="width: 100%">
					<el-option @click="getTableList(ds.id)" v-for="ds in queryForm.datasourceList" :key="ds.id" :label="ds.connName" :value="ds.id"> </el-option> 
				</el-select>
			</el-form-item>
			<el-form-item prop="tableIds" label="任务:">
				<el-select multiple collapse-tags v-model="dataForm.tableIds"   filterable clearable placeholder="任务"  style="width: 100%" >
					<el-option  v-for="ds in queryForm.tableList" :key="ds.id" :label="ds.tableName" :value="ds.id"> </el-option> 
				</el-select>
			</el-form-item>
			<el-form-item label="corn表达式:" prop="cronExpression">
				<el-input v-model="dataForm.cronExpression" placeholder="请输入表达式">
					<template slot="append" >
                      <el-button type="primary" >
                         生成表达式
                       <i class="el-icon-time el-icon--right"></i>
                      </el-button>
                    </template>
				</el-input>
			</el-form-item>
			<el-form-item label="执行策略:" prop="misfirePolicy">
				<el-radio-group v-model="dataForm.misfirePolicy" size="small">
                <el-radio-button label="1">立即执行</el-radio-button>
                <el-radio-button label="2">执行一次</el-radio-button>
                <el-radio-button label="3">放弃执行</el-radio-button>
				</el-radio-group>
			</el-form-item>
			<el-form-item label="是否并发:" prop="concurrent">
              <el-radio-group v-model="dataForm.concurrent" size="small">
                <el-radio-button label="0">允许</el-radio-button>
                <el-radio-button label="1">禁止</el-radio-button>
              </el-radio-group>
            </el-form-item>
			<el-form-item label="状态:">
              <el-radio-group v-model="dataForm.status">
                <el-radio label="0">正常</el-radio>
				<el-radio label="1">暂停</el-radio>
              </el-radio-group>
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
import { useDataSourceListApi} from '@/api/datasource'
import {useTableListApi} from '@/api/table'
import { getJob,addOrUpdateJob} from '@/api/task'
const emit = defineEmits(['refreshDataList'])

const visible = ref(false)
const dataFormRef = ref()

const dataForm = reactive({
	jobId: '',
	status: '',
	misfirePolicy: '',
	concurrent: '',
	jobName: '',
	jobGroup: '',
	cronExpression:'',
	tableIds:[] 
	
})

const queryForm=reactive({
	datasourceList:[] as any,
	tableList:[] as any
})



const init = (id?: number) => {
	visible.value = true
	dataForm.jobId = ''

	// 重置表单数据
	if (dataFormRef.value) {
		dataFormRef.value.resetFields()
	}

	// id 存在则为修改
	if (id) {
		getJobById(id)
	}
}

const getJobById = (id: number) => {
	getJob(id).then(res => {
		Object.assign(dataForm, res.data)
	})
}

const getDataSourceList = () => {
	useDataSourceListApi(true).then(res => {
		queryForm.datasourceList=res.data;
	})
}

const getTableList = (datasourceId:number) => {
	useTableListApi(datasourceId).then(res => {
		queryForm.tableList=res.data.list;
	})
}

const dataRules = ref({
	jobGroup: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	jobName: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	cronExpression: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	misfirePolicy: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
	concurrent: [{ required: true, message: '必填项不能为空', trigger: 'blur' }],
})


// 表单提交
const submitHandle = () => {
	dataFormRef.value.validate((valid: boolean) => {
		if (!valid) {
			return false
		}

		addJob(dataForm).then(() => {
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

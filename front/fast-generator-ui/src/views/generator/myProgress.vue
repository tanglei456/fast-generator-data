<template>
	<el-drawer v-model="visible" title="数据生成进度" size="20%" :with-header="true">
		<div v-for="(item, index) in progressArrays" :key="index">
			<div>{{ item.tableName }}</div>
			<el-progress :percentage="item.percentage" :status="item.status" :stroke-width="8" :color="customColors" />
			<div>{{ item.generatorNumber }} / {{ item.totalNumber }}(耗时:{{ item.useTime }}分钟)</div>
			<el-divider />
		</div>
		<el-empty v-if="progressArrays==null||progressArrays.length==0"  :image-size="300" description="暂无任务" />
	</el-drawer>
</template>

<script setup lang="ts">
import { reactive, ref, defineExpose } from 'vue'
import { webSocketStore } from '@/api/webSocket'
import { createWebSocket, sendSock, closeSock } from '@/api/socket'
const webSocket = webSocketStore()
const percentage = ref(20)
const customColor = ref('#409eff')
const visible = ref(false)

const progressArrays = ref([])
const customColors = [
	{ color: '#f56c6c', percentage: 20 },
	{ color: '#e6a23c', percentage: 40 },
	{ color: '#5cb87a', percentage: 60 },
	{ color: '#1989fa', percentage: 80 },
	{ color: '#6f7ad3', percentage: 100 }
]

const init = (data: any) => {
	visible.value = true
	//进度上传完成关闭连接
	createWebSocket(global_callback)
	//发送数据
	sendSock('ping')
}

const showProgress = () => {
	visible.value = true
}

const global_callback = (msg: any) => {
	console.log('websocket的回调函数收到服务器信息：' + JSON.stringify(msg))
	console.log('收到服务器信息：' + msg)
	progressArrays.value = JSON.parse(JSON.stringify(msg))
}

defineExpose({ init, showProgress })
</script>
<style scoped>
.demo-progress .el-progress--line {
	margin-bottom: 15px;
	width: 350px;
}
</style>

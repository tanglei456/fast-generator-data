import service from '@/utils/request'

// 生成代码（zip压缩包）
export const useDownloadApi = (tableIds: any[]) => {
	location.href = import.meta.env.VITE_API_URL + '/gen/generator/download?tableIds=' + tableIds.join(',')
}

// 生成数据
export const useGeneratorApi = (tableIds: any[]) => {
	return service.post('/gen/generator/data', tableIds)
}

// 生成数据
export const useGeneratorExcel = (tableIds: any[]) => {
	return service.post('/gen/generator/excel', tableIds)
}

// 生成数据
export const useGeneratorDbf = (tableIds: any[]) => {
	return service.post('/gen/generator/dbf', tableIds)
}

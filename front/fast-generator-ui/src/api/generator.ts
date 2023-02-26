import service from '@/utils/request'

// 生成代码（zip压缩包）
export const useDownloadApi = (tableIds: any[]) => {
	location.href = import.meta.env.VITE_API_URL + '/gen/generator/download?tableIds=' + tableIds.join(',')
}

// 生成数据
export const useGeneratorApi = (tableIds: any[]) => {
	return service.post('/gen/generator/data', tableIds)
}

// 生成excel
export const useGeneratorExcel = (tableIds: any[]) => {
	const elemIF = document.createElement(`iframe`);
	elemIF.src = '/gen/generator/excel?tableIds='+tableIds;
	elemIF.style.display = `none`;
	document.body.appendChild(elemIF);
	// return service.get('/gen/generator/excel?tableIds='+tableIds)
}

// 生成dbf
export const useGeneratorDbf = (tableIds: any[]) => {
	return service.get('/gen/generator/dbf?tableIds='+tableIds)
}

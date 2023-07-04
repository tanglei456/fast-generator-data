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
	return service.get('/gen/generator/excel?tableIds=' + tableIds)
}	

// 生成dbf
export const useGeneratorDbf = (tableIds: any[]) => {
	return service.get('/gen/generator/dbf?tableIds=' + tableIds);
}	


// 生成excel
export const useGeneratorDownloadDbfOrExcel = (batchNumber: string) => {
	let link = document.createElement('a');
	link.style.display = 'none';
	let url = "http://192.168.31.109:8088/fast-generator-data/gen/generator/download/dbfOrExcel?batchNumber="+batchNumber; //绝对地址
	link.href = url;
	document.head.appendChild(link);
	link.click();
	document.head.removeChild(link);
	window.location.href();
}	

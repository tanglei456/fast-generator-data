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
	let link = document.createElement('a');
	link.style.display = 'none';
	let url = "http://localhost:8088/fast-generator-data/gen/generator/excel?tableIds="+tableIds; //绝对地址
	link.href = url;
	document.head.appendChild(link);
	link.click();
	document.head.removeChild(link);
	window.location.href();
}	

// 生成dbf
export const useGeneratorDbf = (tableIds: any[]) => {
	let link = document.createElement('a');
	link.style.display = 'none';
	let url = "http://localhost:8088/fast-generator-data/gen/generator/dbf?tableIds="+tableIds; //绝对地址
	link.href = url;
	document.head.appendChild(link);
	link.click();
	document.head.removeChild(link);
	window.location.href();
}	

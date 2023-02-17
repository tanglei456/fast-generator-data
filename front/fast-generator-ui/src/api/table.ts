import service from '@/utils/request'

export const useTableApi = (id: number) => {
	return service.get('/gen/table/' + id)
}

export const useTableSubmitApi = (dataForm: any) => {
	return service.put('/gen/table', dataForm)
}

export const useTableImportSubmitApi = (datasourceId: string, tableNameList: string) => {
	return service.post('/gen/table/import/' + datasourceId, tableNameList)
}

export const useTableImportTemplateSubmitApi = (datasourceId: string, file: string) => {
	return service.post('/gen/table/save/template/' + datasourceId+'/'+file)
}

export const useTableFieldSubmitApi = (tableId: number, fieldList: any) => {
	return service.put('/gen/table/field/' + tableId, fieldList)
}

export const useTableSyncApi = (id: number,type:String) => {
	return service.post('/gen/table/sync/' + id+"/"+type)
}

export const useTableTree = (id: number[] )=> {
	return service.get('/gen/table/tree/datasourceId/' + id[0]+"/tableId/"+id[1])
}

export const useTableListApi = (id: number )=> {
	return service.get('/gen/table/page?datasourceId='+id)
}


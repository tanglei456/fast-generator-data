import service from '@/utils/request'
import { da } from 'element-plus/es/locale'


export const useMockListApi = (dataForm: any) => {
	return service.get('/mock/rule/list',dataForm)
}

export const useSaveOrUpdateMockApi = (dataForm: any) => {
	if (dataForm.id) {
		return service.put('/mock/rule', dataForm)
	} else {
		return service.post('/mock/rule', dataForm)
	}
}

export const useDelMockApi = (id: number) => {
	return service.delete('/mock/rule/'+id)
}

export const useUpdateMockApi = (dataForm: any) => {
	return service.put('/mock/rule',dataForm)
}




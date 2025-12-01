import api from './api'

const departmentService = {
	async getList(params = {}) {
		return await api.get('/departments', { params })
	},

	async getById(id) {
		return await api.get(`/departments/${id}`)
	},

	async create(data) {
		return await api.post('/departments', data)
	},

	async update(id, data) {
		return await api.put(`/departments/${id}`, data)
	},

	async delete(id) {
		return await api.delete(`/departments/${id}`)
	},

	async getActiveList() {
		return await api.get('/departments', { params: { status: 'ACTIVE' } })
	}
}

export default departmentService


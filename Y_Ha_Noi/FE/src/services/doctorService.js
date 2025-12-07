import api from './api'

const doctorService = {
	async getList(params = {}, forceRefresh = false) {
		const config = { params }
		if (forceRefresh) {
			config.__noCache = true
		}
		return await api.get('/doctors', config)
	},

	async getById(id) {
		return await api.get(`/doctors/${id}`)
	},

	async getByDepartment(departmentId) {
		return await api.get('/doctors', { params: { departmentId, listOnly: true } })
	},

	async getAll() {
		return await api.get('/doctors', { params: { page: 1, size: 1000 } })
	},

	async create(data) {
		return await api.post('/doctors', data)
	},

	async update(id, data) {
		return await api.put(`/doctors/${id}`, data)
	},

	async delete(id) {
		return await api.delete(`/doctors/${id}`)
	}
}

export default doctorService


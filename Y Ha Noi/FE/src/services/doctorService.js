import api from './api'

const doctorService = {
	async getList(params = {}) {
		return await api.get('/doctors', { params })
	},

	async getById(id) {
		return await api.get(`/doctors/${id}`)
	},

	async getByDepartment(departmentId) {
		return await api.get('/doctors', { params: { departmentId } })
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


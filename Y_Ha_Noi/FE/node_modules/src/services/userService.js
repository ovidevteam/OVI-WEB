import api from './api'

const userService = {
	async getList(params = {}) {
		return await api.get('/users', { params })
	},

	async getById(id) {
		return await api.get(`/users/${id}`)
	},

	async create(data) {
		return await api.post('/users', data)
	},

	async update(id, data) {
		return await api.put(`/users/${id}`, data)
	},

	async delete(id) {
		return await api.delete(`/users/${id}`)
	},

	async resetPassword(id) {
		return await api.put(`/users/${id}/reset-password`)
	},

	async toggleStatus(id) {
		return await api.put(`/users/${id}/toggle-status`)
	},

	async getHandlers(departmentId = null) {
		return await api.get('/users/handlers', { params: { departmentId } })
	}
}

export default userService


import api from './api'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const feedbackService = {
	async getList(params = {}) {
		return await api.get('/feedbacks', { params })
	},

	async getById(id) {
		return await api.get(`/feedbacks/${id}`)
	},

	async getMyFeedbacks() {
		return await api.get('/feedbacks/my')
	},

	async create(data) {
		return await api.post('/feedbacks', data)
	},

	async update(id, data) {
		return await api.put(`/feedbacks/${id}`, data)
	},

	async delete(id) {
		return await api.delete(`/feedbacks/${id}`)
	},

	async process(id, data) {
		return await api.put(`/feedbacks/${id}/process`, data)
	},

	async assignHandler(id, handlerId) {
		return await api.put(`/feedbacks/${id}/assign`, { handlerId })
	},

	// Cập nhật xử lý phản ánh
	async updateProcessing(feedbackId, data) {
		if (DEMO_MODE) {
			// Demo mode - return mock response
			return Promise.resolve({
				success: true,
				message: 'Cập nhật xử lý thành công (DEMO)'
			})
		}
		return await api.put(`/feedbacks/${feedbackId}/processing`, data)
	},

	// Lấy lịch sử xử lý
	async getProcessHistory(feedbackId) {
		if (DEMO_MODE) {
			// Demo mode - return empty (will use mock data in component)
			return Promise.reject(new Error('Demo mode'))
		}
		const response = await api.get(`/feedbacks/${feedbackId}/history`)
		return response.data
	}
}

export default feedbackService


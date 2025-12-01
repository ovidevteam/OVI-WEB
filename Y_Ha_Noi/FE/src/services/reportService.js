import api from './api'

const reportService = {
	async getDashboard() {
		return await api.get('/reports/dashboard')
	},

	async getByDepartment(params = {}) {
		return await api.get('/reports/by-department', { params })
	},

	async getByDoctor(params = {}) {
		return await api.get('/reports/by-doctor', { params })
	},

	async getWithImages(params = {}) {
		return await api.get('/reports/with-images', { params })
	},

	async getMonthlyStats(year) {
		return await api.get('/reports/monthly-stats', { params: { year } })
	},

	async exportExcel(params = {}) {
		return await api.get('/reports/export-excel', {
			params,
			responseType: 'blob'
		})
	},

	async exportPdf(params = {}) {
		return await api.get('/reports/export-pdf', {
			params,
			responseType: 'blob'
		})
	}
}

export default reportService


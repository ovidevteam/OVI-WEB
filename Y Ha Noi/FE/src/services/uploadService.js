import api from './api'

const uploadService = {
	async uploadFeedbackImages(feedbackId, files) {
		const formData = new FormData()
		files.forEach(file => {
			formData.append('files', file)
		})
		formData.append('feedbackId', feedbackId)

		return await api.post('/upload/feedback-images', formData, {
			headers: {
				'Content-Type': 'multipart/form-data'
			}
		})
	},

	async uploadProcessImages(feedbackId, files) {
		const formData = new FormData()
		files.forEach(file => {
			formData.append('files', file)
		})
		formData.append('feedbackId', feedbackId)

		return await api.post('/upload/process-images', formData, {
			headers: {
				'Content-Type': 'multipart/form-data'
			}
		})
	},

	async deleteImage(imageId) {
		return await api.delete(`/upload/images/${imageId}`)
	},

	getImageUrl(filename) {
		const baseUrl = import.meta.env.VITE_API_BASE_URL || '/api'
		return `${baseUrl}/upload/images/${filename}`
	}
}

export default uploadService


/**
 * Rating Service
 * Handles API calls for feedback ratings (doctor evaluation after feedback completion)
 */

import api from './api'

// Demo mode - if true, use mock data instead of real API
const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

// Mock data for demo
const mockRatings = [
	{
		id: 1,
		feedbackId: 1,
		feedbackCode: 'PA-2024-001',
		doctorId: 101,
		doctorName: 'BS. Nguyễn Văn A',
		rating: 4,
		comment: 'Bác sĩ xử lý tốt, giải quyết nhanh chóng',
		ratedBy: 'admin',
		ratedDate: '2024-11-20'
	},
	{
		id: 2,
		feedbackId: 3,
		feedbackCode: 'PA-2024-003',
		doctorId: 103,
		doctorName: 'BS. Lê Văn C',
		rating: 5,
		comment: 'Xuất sắc!',
		ratedBy: 'leader01',
		ratedDate: '2024-11-22'
	},
	{
		id: 3,
		feedbackId: 5,
		feedbackCode: 'PA-2024-005',
		doctorId: 105,
		doctorName: 'BS. Hoàng Văn E',
		rating: 3,
		comment: 'Xử lý ổn',
		ratedBy: 'handler01',
		ratedDate: '2024-11-24'
	}
]

// Simulate delay
const delay = (ms) => new Promise(resolve => setTimeout(resolve, ms))

const ratingService = {
	/**
	 * Get completed feedbacks with rating status
	 * @param {Object} params - Query parameters
	 * @returns {Promise} List of completed feedbacks
	 */
	async getCompletedFeedbacks(params = {}) {
		if (DEMO_MODE) {
			await delay(500)
			return {
				data: [
					{
						id: 1,
						code: 'PA-2024-001',
						content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng',
						doctorId: 101,
						doctorName: 'BS. Nguyễn Văn A',
						departmentId: 1,
						departmentName: 'Nội khoa',
						completedDate: '20/11/2024',
						rating: 4,
						comment: 'Bác sĩ xử lý tốt, giải quyết nhanh chóng'
					},
					{
						id: 2,
						code: 'PA-2024-002',
						content: 'Nhân viên lễ tân thiếu thân thiện với bệnh nhân ca sáng',
						doctorId: 102,
						doctorName: 'BS. Trần Thị B',
						departmentId: 2,
						departmentName: 'Ngoại khoa',
						completedDate: '21/11/2024',
						rating: null,
						comment: null
					},
					{
						id: 3,
						code: 'PA-2024-003',
						content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp',
						doctorId: 103,
						doctorName: 'BS. Lê Văn C',
						departmentId: 3,
						departmentName: 'Da liễu',
						completedDate: '22/11/2024',
						rating: 5,
						comment: 'Xuất sắc!'
					}
				],
				total: 3,
				stats: {
					total: 8,
					pending: 4,
					rated: 4,
					avgRating: 4.0
				}
			}
		}
		return api.get('/ratings/completed-feedbacks', { params })
	},

	/**
	 * Submit rating for a completed feedback
	 * @param {Object} ratingData - Rating data { feedbackId, doctorId, rating, comment }
	 * @returns {Promise} Created rating
	 */
	async submitRating(ratingData) {
		if (DEMO_MODE) {
			await delay(800)
			const newRating = {
				id: mockRatings.length + 1,
				...ratingData,
				ratedBy: 'current_user',
				ratedDate: new Date().toISOString().split('T')[0]
			}
			mockRatings.push(newRating)
			return { success: true, data: newRating }
		}
		return api.post('/ratings', ratingData)
	},

	/**
	 * Update existing rating
	 * @param {number} ratingId - Rating ID
	 * @param {Object} ratingData - Updated rating data
	 * @returns {Promise} Updated rating
	 */
	async updateRating(ratingId, ratingData) {
		if (DEMO_MODE) {
			await delay(600)
			const index = mockRatings.findIndex(r => r.id === ratingId)
			if (index !== -1) {
				mockRatings[index] = { ...mockRatings[index], ...ratingData }
				return { success: true, data: mockRatings[index] }
			}
			throw new Error('Rating not found')
		}
		return api.put(`/ratings/${ratingId}`, ratingData)
	},

	/**
	 * Get rating by feedback ID
	 * @param {number} feedbackId - Feedback ID
	 * @returns {Promise} Rating data or null
	 */
	async getRatingByFeedback(feedbackId) {
		if (DEMO_MODE) {
			await delay(300)
			const rating = mockRatings.find(r => r.feedbackId === feedbackId)
			return rating || null
		}
		return api.get(`/ratings/by-feedback/${feedbackId}`)
	},

	/**
	 * Get average rating for a doctor
	 * @param {number} doctorId - Doctor ID
	 * @returns {Promise} Average rating and count
	 */
	async getDoctorRating(doctorId) {
		if (DEMO_MODE) {
			await delay(300)
			const doctorRatings = mockRatings.filter(r => r.doctorId === doctorId)
			if (doctorRatings.length === 0) {
				return { avgRating: 0, totalRatings: 0 }
			}
			const avgRating = doctorRatings.reduce((sum, r) => sum + r.rating, 0) / doctorRatings.length
			return {
				avgRating: Math.round(avgRating * 10) / 10,
				totalRatings: doctorRatings.length
			}
		}
		return api.get(`/ratings/doctor/${doctorId}/average`)
	},

	/**
	 * Get all ratings for a doctor
	 * @param {number} doctorId - Doctor ID
	 * @param {Object} params - Query parameters (page, size, etc.)
	 * @returns {Promise} List of ratings
	 */
	async getDoctorRatings(doctorId, params = {}) {
		if (DEMO_MODE) {
			await delay(400)
			const doctorRatings = mockRatings.filter(r => r.doctorId === doctorId)
			return {
				data: doctorRatings,
				total: doctorRatings.length
			}
		}
		return api.get(`/ratings/doctor/${doctorId}`, { params })
	},

	/**
	 * Get rating statistics
	 * @param {Object} params - Query parameters (dateFrom, dateTo, departmentId, etc.)
	 * @returns {Promise} Rating statistics
	 */
	async getStatistics(params = {}) {
		if (DEMO_MODE) {
			await delay(400)
			return {
				totalRatings: mockRatings.length,
				avgRating: 4.0,
				ratingDistribution: {
					1: 0,
					2: 0,
					3: 1,
					4: 1,
					5: 1
				},
				topDoctors: [
					{ doctorId: 103, doctorName: 'BS. Lê Văn C', avgRating: 5.0, totalRatings: 1 },
					{ doctorId: 101, doctorName: 'BS. Nguyễn Văn A', avgRating: 4.0, totalRatings: 1 },
					{ doctorId: 105, doctorName: 'BS. Hoàng Văn E', avgRating: 3.0, totalRatings: 1 }
				]
			}
		}
		return api.get('/ratings/statistics', { params })
	}
}

export default ratingService


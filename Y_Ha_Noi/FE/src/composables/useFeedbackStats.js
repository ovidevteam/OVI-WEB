import { ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import feedbackService from '@/services/feedbackService'
import ratingService from '@/services/ratingService'
import { mockFeedbackStats } from '@/mock/db'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

// Global stats state
const feedbackStats = ref({
	total: 0,       // Tổng = pending + needRating + myPending - hiển thị trên menu Phản ánh
	pending: 0,     // Danh sách chưa xử lý (NEW + PROCESSING) - hiển thị trên menu Danh sách
	myPending: 0,    // Của tôi đang chờ xử lý (NEW + PROCESSING assigned to me)
	needRating: 0    // PA đã hoàn thành cần đánh giá - hiển thị trên menu Đánh giá
})

/**
 * Composable for managing feedback stats
 * Can be used across components to fetch and update stats
 */
export function useFeedbackStats() {
	const authStore = useAuthStore()

	const fetchFeedbackStats = async () => {
		if (DEMO_MODE) {
			// Use mock data from db.js
			feedbackStats.value = { ...mockFeedbackStats }
			// Calculate total = pending + needRating + myPending
			feedbackStats.value.total = feedbackStats.value.pending + feedbackStats.value.needRating + feedbackStats.value.myPending
			return
		}

		try {
			// Fetch NEW feedbacks count
			const newResponse = await feedbackService.getList({ status: 'NEW', size: 1 })
			const newCount = newResponse.total || 0

			// Fetch PROCESSING feedbacks count
			const processingResponse = await feedbackService.getList({ status: 'PROCESSING', size: 1 })
			const processingCount = processingResponse.total || 0
			
			// Total unprocessed = NEW + PROCESSING (for "Danh sách" menu)
			const totalUnprocessed = newCount + processingCount
			feedbackStats.value.pending = totalUnprocessed

			// Fetch my pending feedbacks (if handler)
			if (authStore.isHandler) {
				try {
					const response = await feedbackService.getMyFeedbacks()
					// Handle both array and object response formats
					const myFeedbacks = Array.isArray(response) ? response : (response?.data || [])
					// Count both NEW and PROCESSING feedbacks (feedbacks that need action)
					const myNewCount = myFeedbacks.filter(f => f.status === 'NEW').length
					const myProcessingCount = myFeedbacks.filter(f => f.status === 'PROCESSING').length
					feedbackStats.value.myPending = myNewCount + myProcessingCount
				} catch (error) {
					// Error fetching my feedbacks - non-critical
					feedbackStats.value.myPending = 0
				}
			} else {
				feedbackStats.value.myPending = 0
			}

			// Fetch completed feedbacks needing rating
			if (authStore.hasRole(['ADMIN', 'LEADER', 'HANDLER'])) {
				try {
					const completedResponse = await ratingService.getCompletedFeedbacks({ 
						hasRating: false, 
						size: 1 
					})
					feedbackStats.value.needRating = completedResponse.total || 0
				} catch (error) {
					// Error fetching ratings - non-critical
					feedbackStats.value.needRating = 0
				}
			} else {
				feedbackStats.value.needRating = 0
			}
			
			// Total = tổng của Danh sách + Đánh giá + Của tôi
			feedbackStats.value.total = feedbackStats.value.pending + feedbackStats.value.needRating + feedbackStats.value.myPending
		} catch (error) {
			// Error fetching feedback stats - set all to 0
			feedbackStats.value = {
				total: 0,
				pending: 0,
				myPending: 0,
				needRating: 0
			}
		}
	}

	return {
		feedbackStats,
		fetchFeedbackStats
	}
}


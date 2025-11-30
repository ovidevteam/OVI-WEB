import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import feedbackService from '@/services/feedbackService'

export const useFeedbackStore = defineStore('feedback', () => {
	const feedbacks = ref([])
	const currentFeedback = ref(null)
	const myFeedbacks = ref([])
	const loading = ref(false)
	const totalItems = ref(0)
	const filters = ref({
		dateFrom: null,
		dateTo: null,
		departmentId: null,
		doctorId: null,
		status: null,
		level: null,
		keyword: ''
	})

	const pendingCount = computed(() =>
		feedbacks.value.filter(f => f.status === 'NEW').length
	)
	const processingCount = computed(() =>
		feedbacks.value.filter(f => f.status === 'PROCESSING').length
	)
	const completedCount = computed(() =>
		feedbacks.value.filter(f => f.status === 'COMPLETED').length
	)
	const overdueCount = computed(() =>
		feedbacks.value.filter(f => f.isOverdue).length
	)

	async function fetchFeedbacks(params = {}) {
		loading.value = true
		try {
			const response = await feedbackService.getList({ ...filters.value, ...params })
			feedbacks.value = response.data
			totalItems.value = response.total
		} catch (error) {
			console.error('Fetch feedbacks error:', error)
		} finally {
			loading.value = false
		}
	}

	async function fetchFeedbackById(id) {
		loading.value = true
		try {
			currentFeedback.value = await feedbackService.getById(id)
		} catch (error) {
			console.error('Fetch feedback error:', error)
		} finally {
			loading.value = false
		}
	}

	async function fetchMyFeedbacks() {
		loading.value = true
		try {
			myFeedbacks.value = await feedbackService.getMyFeedbacks()
		} catch (error) {
			console.error('Fetch my feedbacks error:', error)
		} finally {
			loading.value = false
		}
	}

	async function createFeedback(data) {
		loading.value = true
		try {
			const result = await feedbackService.create(data)
			return { success: true, data: result }
		} catch (error) {
			return { success: false, message: error.message }
		} finally {
			loading.value = false
		}
	}

	async function updateFeedback(id, data) {
		loading.value = true
		try {
			const result = await feedbackService.update(id, data)
			return { success: true, data: result }
		} catch (error) {
			return { success: false, message: error.message }
		} finally {
			loading.value = false
		}
	}

	async function processFeedback(id, data) {
		loading.value = true
		try {
			const result = await feedbackService.process(id, data)
			return { success: true, data: result }
		} catch (error) {
			return { success: false, message: error.message }
		} finally {
			loading.value = false
		}
	}

	function setFilters(newFilters) {
		filters.value = { ...filters.value, ...newFilters }
	}

	function resetFilters() {
		filters.value = {
			dateFrom: null,
			dateTo: null,
			departmentId: null,
			doctorId: null,
			status: null,
			level: null,
			keyword: ''
		}
	}

	return {
		feedbacks,
		currentFeedback,
		myFeedbacks,
		loading,
		totalItems,
		filters,
		pendingCount,
		processingCount,
		completedCount,
		overdueCount,
		fetchFeedbacks,
		fetchFeedbackById,
		fetchMyFeedbacks,
		createFeedback,
		updateFeedback,
		processFeedback,
		setFilters,
		resetFilters
	}
})


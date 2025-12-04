import { ref, onUnmounted } from 'vue'
import axios from 'axios'
import api from '@/services/api'
import { handleApiError } from '@/utils/errorHandler'

/**
 * Composable for managing API requests with cancellation support
 * Automatically cancels requests when component unmounts to prevent memory leaks
 *
 * @param {Object} options - Configuration options
 * @param {boolean} options.autoCancel - Whether to auto-cancel on unmount (default: true)
 * @returns {Object} API request utilities
 *
 * @example
 * const { request, cancel, isPending } = useApiRequest()
 *
 * onMounted(async () => {
 *   const data = await request(() => api.get('/feedbacks'))
 * })
 */
export function useApiRequest(options = {}) {
	const { autoCancel = true } = options

	const cancelTokenSource = ref(null)
	const isPending = ref(false)
	const error = ref(null)

	/**
	 * Create a new cancel token source
	 */
	function createCancelToken() {
		if (cancelTokenSource.value) {
			cancelTokenSource.value.cancel('New request initiated')
		}
		cancelTokenSource.value = axios.CancelToken.source()
		return cancelTokenSource.value.token
	}

	/**
	 * Execute an API request with cancellation support
	 * @param {Function} requestFn - Function that returns a promise (e.g., () => api.get('/endpoint'))
	 * @param {Object} config - Additional axios config
	 * @returns {Promise} The request promise
	 */
	async function request(requestFn, config = {}) {
		// Cancel previous request if exists
		if (cancelTokenSource.value) {
			cancelTokenSource.value.cancel('New request initiated')
		}

		// Create new cancel token
		const cancelToken = createCancelToken()

		// Set pending state
		isPending.value = true
		error.value = null

		try {
			// Execute request with cancel token
			const result = await requestFn({
				...config,
				cancelToken
			})

			return result
		} catch (err) {
			// Don't set error if request was cancelled
			if (axios.isCancel(err)) {
				console.log('Request cancelled:', err.message)
				return null
			}

			// Handle error
			error.value = err
			handleApiError(err, 'API Request')
			throw err
		} finally {
			isPending.value = false
		}
	}

	/**
	 * Cancel the current request
	 * @param {string} message - Cancellation message
	 */
	function cancel(message = 'Request cancelled by user') {
		if (cancelTokenSource.value) {
			cancelTokenSource.value.cancel(message)
			cancelTokenSource.value = null
			isPending.value = false
		}
	}

	/**
	 * Reset error state
	 */
	function clearError() {
		error.value = null
	}

	// Auto-cancel on unmount
	if (autoCancel) {
		onUnmounted(() => {
			if (cancelTokenSource.value) {
				cancelTokenSource.value.cancel('Component unmounted')
				cancelTokenSource.value = null
			}
		})
	}

	return {
		request,
		cancel,
		clearError,
		isPending,
		error
	}
}

/**
 * Simplified composable for single request
 * @param {Function} requestFn - Function that returns a promise
 * @returns {Object} Request state and methods
 */
export function useSingleRequest(requestFn) {
	const { request, isPending, error } = useApiRequest()

	const execute = async (config) => {
		return await request(() => requestFn(config))
	}

	return {
		execute,
		isPending,
		error
	}
}


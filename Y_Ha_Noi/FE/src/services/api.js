import axios from 'axios'
import router from '@/router'
import { handleApiError } from '@/utils/errorHandler'
import { decryptToken } from '@/utils/encryption'
import { useAuthStore } from '@/stores/auth'

const api = axios.create({
	baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
	timeout: 30000,
	headers: {
		'Content-Type': 'application/json'
	}
})

// Retry configuration
const MAX_RETRIES = 3
const RETRY_DELAY = 1000 // 1 second
const RETRYABLE_STATUS_CODES = [408, 429, 500, 502, 503, 504]
const RETRYABLE_NETWORK_ERRORS = ['ECONNABORTED', 'ETIMEDOUT', 'ENOTFOUND']

/**
 * Check if error is retryable
 * @param {Error} error - The error object
 * @returns {boolean} True if error is retryable
 */
function isRetryableError(error) {
	if (!error.config) return false
	
	const { status } = error.response || {}
	const { code } = error
	
	// Retry on specific status codes
	if (status && RETRYABLE_STATUS_CODES.includes(status)) {
		return true
	}
	
	// Retry on network errors
	if (code && RETRYABLE_NETWORK_ERRORS.includes(code)) {
		return true
	}
	
	// Retry on timeout
	if (error.code === 'ECONNABORTED' && error.message.includes('timeout')) {
		return true
	}
	
	return false
}

/**
 * Retry failed request with exponential backoff
 * @param {Error} error - The error object
 * @returns {Promise} Retry promise
 */
async function retryRequest(error) {
	const config = error.config
	
	// Don't retry if config doesn't exist or retry is disabled
	if (!config || config.__retryCount === undefined) {
		config.__retryCount = 0
	}
	
	// Don't retry if max retries reached
	if (config.__retryCount >= MAX_RETRIES) {
		return Promise.reject(error)
	}
	
	// Don't retry if error is not retryable
	if (!isRetryableError(error)) {
		return Promise.reject(error)
	}
	
	// Increment retry count
	config.__retryCount += 1
	
	// Calculate delay with exponential backoff
	const delay = RETRY_DELAY * Math.pow(2, config.__retryCount - 1)
	
	// Wait before retrying
	await new Promise(resolve => setTimeout(resolve, delay))
	
	// Retry the request
	return api(config)
}

// Request interceptor
api.interceptors.request.use(
	(config) => {
		const encryptedToken = localStorage.getItem('token')
		if (encryptedToken) {
			// Decrypt token before using
			const token = decryptToken(encryptedToken)
			config.headers.Authorization = `Bearer ${token}`
		}
		
		// Update activity timestamp for session management
		const authStore = useAuthStore()
		if (authStore.isAuthenticated) {
			authStore.updateActivity()
		}
		
		// Initialize retry count
		config.__retryCount = config.__retryCount || 0
		
		return config
	},
	(error) => {
		return Promise.reject(error)
	}
)

// Response interceptor
api.interceptors.response.use(
	(response) => {
		return response.data
	},
	async (error) => {
		const { response } = error
		const config = error.config

		// Handle 401 - Unauthorized
		if (response?.status === 401) {
			// Try to refresh token if available
			const authStore = useAuthStore()
			if (authStore.refreshTokenValue && !config.__isRefreshing) {
				try {
					config.__isRefreshing = true
					const refreshed = await authStore.refreshAccessToken()
					if (refreshed) {
						// Retry original request with new token
						const encryptedToken = localStorage.getItem('token')
						if (encryptedToken) {
							const token = decryptToken(encryptedToken)
							config.headers.Authorization = `Bearer ${token}`
						}
						config.__isRefreshing = false
						config.__retryCount = 0 // Reset retry count for new attempt
						return api(config)
					}
				} catch (refreshError) {
					config.__isRefreshing = false
					// Refresh failed, logout user
					await authStore.logout()
					router.push('/login')
					return Promise.reject(refreshError)
				}
			}
			
			// If refresh failed or not available, logout
			await authStore.logout()
			router.push('/login')
			return Promise.reject(error)
		}

		// Retry logic for retryable errors
		if (isRetryableError(error) && config.__retryCount < MAX_RETRIES) {
			return retryRequest(error)
		}

		// Use centralized error handler
		handleApiError(error, 'API Request')

		return Promise.reject(error)
	}
)

export default api


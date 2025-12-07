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

// Cache configuration
const CACHE_TTL = 5 * 60 * 1000 // 5 minutes
const cache = new Map()
const pendingRequests = new Map()

/**
 * Generate cache key from request config
 * @param {Object} config - Axios request config
 * @returns {string} Cache key
 */
function getCacheKey(config) {
	const { method, url, params } = config
	const paramsStr = params ? JSON.stringify(params) : ''
	return `${method}:${url}:${paramsStr}`
}

/**
 * Check if request should be cached
 * @param {Object} config - Axios request config
 * @returns {boolean} True if request should be cached
 */
function shouldCache(config) {
	// Only cache GET requests
	if (config.method?.toUpperCase() !== 'GET') return false
	
	// Check if caching is explicitly disabled
	if (config.__noCache === true) return false
	
	// Cache department/doctor lists and other reference data
	const cacheablePaths = ['/departments', '/doctors', '/feedbacks/my']
	return cacheablePaths.some(path => config.url?.includes(path))
}

/**
 * Get cached response if available and not expired
 * @param {string} key - Cache key
 * @returns {Object|null} Cached response or null
 */
function getCachedResponse(key) {
	const cached = cache.get(key)
	if (!cached) return null
	
	const now = Date.now()
	if (now - cached.timestamp > CACHE_TTL) {
		cache.delete(key)
		return null
	}
	
	return cached.data
}

/**
 * Store response in cache
 * @param {string} key - Cache key
 * @param {*} data - Response data
 */
function setCachedResponse(key, data) {
	cache.set(key, {
		data,
		timestamp: Date.now()
	})
}

/**
 * Clear cache for specific URL pattern
 * @param {string} pattern - URL pattern to clear
 */
export function clearCache(pattern) {
	if (!pattern) {
		cache.clear()
		return
	}
	
	for (const key of cache.keys()) {
		if (key.includes(pattern)) {
			cache.delete(key)
		}
	}
}

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
		
		// Mark cache key for GET requests
		if (shouldCache(config)) {
			config.__cacheKey = getCacheKey(config)
		}
		
		return config
	},
	(error) => {
		return Promise.reject(error)
	}
)

// Response interceptor
api.interceptors.response.use(
	(response) => {
		const config = response.config || {}
		
		// Cache successful GET responses
		if (config.__cacheKey && response.status === 200 && response.data) {
			setCachedResponse(config.__cacheKey, response.data)
			pendingRequests.delete(config.__cacheKey)
		}
		
		return response.data
	},
	async (error) => {
		const { response } = error
		const config = error.config || {}
		
		// Clean up pending request on error
		if (config.__cacheKey) {
			pendingRequests.delete(config.__cacheKey)
		}

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

// Wrap axios methods to add caching
const originalGet = api.get.bind(api)
api.get = async function(url, config = {}) {
	// Check cache first
	if (shouldCache({ method: 'GET', url, ...config })) {
		const cacheKey = getCacheKey({ method: 'GET', url, ...config })
		const cached = getCachedResponse(cacheKey)
		
		if (cached) {
			return Promise.resolve({ data: cached })
		}
		
		// Check for pending duplicate requests
		if (pendingRequests.has(cacheKey)) {
			return pendingRequests.get(cacheKey)
		}
		
		// Make request and cache it
		const requestPromise = originalGet(url, { ...config, __cacheKey: cacheKey })
			.then(response => {
				if (response?.data) {
					setCachedResponse(cacheKey, response.data)
				}
				pendingRequests.delete(cacheKey)
				return response
			})
			.catch(error => {
				pendingRequests.delete(cacheKey)
				throw error
			})
		
		pendingRequests.set(cacheKey, requestPromise)
		return requestPromise
	}
	
	return originalGet(url, config)
}

// Export cache utilities
api.clearCache = clearCache

export default api


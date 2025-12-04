import axios from 'axios'
import router from '@/router'
import { handleApiError } from '@/utils/errorHandler'
import { decryptToken } from '@/utils/encryption'

const api = axios.create({
	baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
	timeout: 30000,
	headers: {
		'Content-Type': 'application/json'
	}
})

// Request interceptor
api.interceptors.request.use(
	(config) => {
		const encryptedToken = localStorage.getItem('token')
		if (encryptedToken) {
			// Decrypt token before using
			const token = decryptToken(encryptedToken)
			config.headers.Authorization = `Bearer ${token}`
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
		return response.data
	},
	(error) => {
		const { response } = error

		// Handle 401 - Unauthorized (special case: clear auth and redirect)
		if (response?.status === 401) {
			localStorage.removeItem('token')
			localStorage.removeItem('user')
			router.push('/login')
		}

		// Use centralized error handler
		handleApiError(error, 'API Request')

		return Promise.reject(error)
	}
)

export default api


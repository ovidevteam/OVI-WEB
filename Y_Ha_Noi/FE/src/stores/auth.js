import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService'
import { encryptToken, decryptToken } from '@/utils/encryption'

// Session timeout constants (in milliseconds)
const SESSION_TIMEOUT_WARNING = 5 * 60 * 1000 // 5 minutes before timeout
const SESSION_TIMEOUT = 30 * 60 * 1000 // 30 minutes total session time
const TOKEN_REFRESH_THRESHOLD = 5 * 60 * 1000 // Refresh token 5 minutes before expiry

export const useAuthStore = defineStore('auth', () => {
	const user = ref(null)
	const token = ref(null)
	const refreshTokenValue = ref(null)
	const loading = ref(false)
	const lastActivity = ref(null)
	const sessionTimeoutWarning = ref(false)
	let sessionTimeoutTimer = null
	let sessionWarningTimer = null

	const isAuthenticated = computed(() => !!token.value)
	const userRole = computed(() => user.value?.role || '')
	const userName = computed(() => user.value?.fullName || '')

	const isAdmin = computed(() => userRole.value === 'ADMIN')
	const isLeader = computed(() => userRole.value === 'LEADER')
	const isReceiver = computed(() => userRole.value === 'RECEIVER')
	const isHandler = computed(() => userRole.value === 'HANDLER')
	const isViewer = computed(() => userRole.value === 'VIEWER')

	/**
	 * Initialize auth state from localStorage
	 */
	function initAuth() {
		const savedToken = localStorage.getItem('token')
		const savedUser = localStorage.getItem('user')
		const savedRefreshToken = localStorage.getItem('refreshToken')
		const savedLastActivity = localStorage.getItem('lastActivity')

		if (savedToken && savedUser) {
			try {
				// Decrypt token if it's encrypted
				token.value = decryptToken(savedToken)
				
				// Parse user data with validation
				if (savedUser && savedUser !== 'undefined' && savedUser !== 'null') {
					user.value = JSON.parse(savedUser)
				} else {
					// Clear invalid user data
					localStorage.removeItem('user')
					user.value = null
				}
				
				refreshTokenValue.value = savedRefreshToken ? decryptToken(savedRefreshToken) : null
				lastActivity.value = savedLastActivity ? parseInt(savedLastActivity) : Date.now()

				// Start session timeout monitoring
				startSessionTimeout()
			} catch (error) {
				console.error('Error initializing auth:', error)
				// Clear invalid data
				localStorage.removeItem('token')
				localStorage.removeItem('user')
				localStorage.removeItem('refreshToken')
				token.value = null
				user.value = null
				refreshTokenValue.value = null
			}
		}
	}

	/**
	 * Update last activity timestamp
	 */
	function updateActivity() {
		lastActivity.value = Date.now()
		localStorage.setItem('lastActivity', lastActivity.value.toString())
		// Reset session timeout timers
		startSessionTimeout()
	}

	/**
	 * Start session timeout monitoring
	 */
	function startSessionTimeout() {
		// Clear existing timers
		if (sessionTimeoutTimer) {
			clearTimeout(sessionTimeoutTimer)
		}
		if (sessionWarningTimer) {
			clearTimeout(sessionWarningTimer)
		}

		// Clear warning when restarting timeout (user is active)
		sessionTimeoutWarning.value = false

		if (!isAuthenticated.value) return

		const timeSinceActivity = Date.now() - (lastActivity.value || Date.now())
		const remainingTime = SESSION_TIMEOUT - timeSinceActivity

		// Set warning timer
		if (remainingTime > SESSION_TIMEOUT_WARNING) {
			sessionWarningTimer = setTimeout(() => {
				sessionTimeoutWarning.value = true
			}, remainingTime - SESSION_TIMEOUT_WARNING)
		} else {
			sessionTimeoutWarning.value = true
		}

		// Set timeout timer
		if (remainingTime > 0) {
			sessionTimeoutTimer = setTimeout(() => {
				handleSessionTimeout()
			}, remainingTime)
		} else {
			handleSessionTimeout()
		}
	}

	/**
	 * Handle session timeout
	 */
	async function handleSessionTimeout() {
		sessionTimeoutWarning.value = false
		await logout()
		// Optionally show message
		if (window.location.pathname !== '/login') {
			// Router will redirect to login
		}
	}

	/**
	 * Verify if current token is still valid
	 * @returns {Promise<boolean>} True if token is valid
	 */
	async function verifyToken() {
		if (!token.value) return false
		try {
			const isValid = await authService.verifyToken()
			if (!isValid) {
				// Try to refresh token if available
				if (refreshTokenValue.value) {
					return await refreshAccessToken()
				}
				return false
			}
			updateActivity()
			return true
		} catch {
			return false
		}
	}

	/**
	 * Refresh access token using refresh token
	 * @returns {Promise<boolean>} True if refresh successful
	 */
	async function refreshAccessToken() {
		if (!refreshTokenValue.value) return false
		try {
			const response = await authService.refreshToken(refreshTokenValue.value)
			if (response.token) {
				token.value = response.token
				if (response.refreshToken) {
					refreshTokenValue.value = response.refreshToken
					localStorage.setItem('refreshToken', encryptToken(response.refreshToken))
				}
				if (response.user) {
					user.value = response.user
					localStorage.setItem('user', JSON.stringify(response.user))
				}
				localStorage.setItem('token', encryptToken(response.token))
				updateActivity()
				return true
			}
			return false
		} catch {
			// Refresh failed, logout user
			await logout()
			return false
		}
	}

	async function login(credentials) {
		loading.value = true
		try {
			const response = await authService.login(credentials)
			token.value = response.token
			user.value = response.user
			refreshTokenValue.value = response.refreshToken || null
			
			// Encrypt tokens before storing
			localStorage.setItem('token', encryptToken(response.token))
			localStorage.setItem('user', JSON.stringify(response.user))
			if (response.refreshToken) {
				localStorage.setItem('refreshToken', encryptToken(response.refreshToken))
			}
			
			// Initialize session timeout
			updateActivity()
			startSessionTimeout()
			
			return { success: true }
		} catch (error) {
			return { success: false, message: error.message || 'Đăng nhập thất bại' }
		} finally {
			loading.value = false
		}
	}

	async function logout() {
		try {
			await authService.logout()
		} catch (error) {
			console.error('Logout error:', error)
		} finally {
			// Clear timers
			if (sessionTimeoutTimer) {
				clearTimeout(sessionTimeoutTimer)
				sessionTimeoutTimer = null
			}
			if (sessionWarningTimer) {
				clearTimeout(sessionWarningTimer)
				sessionWarningTimer = null
			}
			
			// Clear state
			user.value = null
			token.value = null
			refreshTokenValue.value = null
			lastActivity.value = null
			sessionTimeoutWarning.value = false
			
			// Clear storage
			localStorage.removeItem('token')
			localStorage.removeItem('user')
			localStorage.removeItem('refreshToken')
			localStorage.removeItem('lastActivity')
		}
	}

	function hasRole(roles) {
		if (!Array.isArray(roles)) roles = [roles]
		return roles.includes(userRole.value)
	}

	return {
		user,
		token,
		loading,
		sessionTimeoutWarning,
		isAuthenticated,
		userRole,
		userName,
		isAdmin,
		isLeader,
		isReceiver,
		isHandler,
		isViewer,
		initAuth,
		login,
		logout,
		hasRole,
		verifyToken,
		refreshAccessToken,
		updateActivity,
		startSessionTimeout
	}
})


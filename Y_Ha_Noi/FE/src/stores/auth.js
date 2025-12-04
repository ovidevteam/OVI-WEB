import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authService from '@/services/authService'
import { encryptToken, decryptToken } from '@/utils/encryption'

export const useAuthStore = defineStore('auth', () => {
	const user = ref(null)
	const token = ref(null)
	const loading = ref(false)

	const isAuthenticated = computed(() => !!token.value)
	const userRole = computed(() => user.value?.role || '')
	const userName = computed(() => user.value?.fullName || '')

	const isAdmin = computed(() => userRole.value === 'ADMIN')
	const isLeader = computed(() => userRole.value === 'LEADER')
	const isReceiver = computed(() => userRole.value === 'RECEIVER')
	const isHandler = computed(() => userRole.value === 'HANDLER')
	const isViewer = computed(() => userRole.value === 'VIEWER')

	function initAuth() {
		const savedToken = localStorage.getItem('token')
		const savedUser = localStorage.getItem('user')
		if (savedToken && savedUser) {
			// Decrypt token if it's encrypted
			token.value = decryptToken(savedToken)
			user.value = JSON.parse(savedUser)
		}
	}

	async function login(credentials) {
		loading.value = true
		try {
			const response = await authService.login(credentials)
			token.value = response.token
			user.value = response.user
			// Encrypt token before storing
			localStorage.setItem('token', encryptToken(response.token))
			localStorage.setItem('user', JSON.stringify(response.user))
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
			user.value = null
			token.value = null
			localStorage.removeItem('token')
			localStorage.removeItem('user')
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
		hasRole
	}
})


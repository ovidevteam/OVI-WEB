import api from './api'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

// Demo users for testing without backend
const DEMO_USERS = {
	admin: {
		id: 1,
		username: 'admin',
		fullName: 'Quản trị viên',
		email: 'admin@bvyhanoi.vn',
		role: 'ADMIN',
		departmentId: null,
		departmentName: ''
	},
	leader: {
		id: 2,
		username: 'leader',
		fullName: 'Nguyễn Văn Lãnh đạo',
		email: 'leader@bvyhanoi.vn',
		role: 'LEADER',
		departmentId: null,
		departmentName: ''
	},
	receiver: {
		id: 3,
		username: 'receiver',
		fullName: 'Trần Thị Tiếp nhận',
		email: 'receiver@bvyhanoi.vn',
		role: 'RECEIVER',
		departmentId: 1,
		departmentName: 'Tiếp nhận'
	},
	handler: {
		id: 4,
		username: 'handler',
		fullName: 'BS. Nguyễn Văn A',
		email: 'handler@bvyhanoi.vn',
		role: 'HANDLER',
		departmentId: 2,
		departmentName: 'Nội khoa'
	}
}

const DEMO_PASSWORDS = {
	admin: 'admin123',
	leader: 'leader123',
	receiver: 'receiver123',
	handler: 'handler123'
}

const authService = {
	async login(credentials) {
		// If demo mode is enabled, skip API call
		if (DEMO_MODE) {
			return this.demoLogin(credentials)
		}

		// Try real API - no fallback if DEMO_MODE is false
		return await api.post('/auth/login', credentials)
	},

	demoLogin(credentials) {
		const { username, password } = credentials
		const user = DEMO_USERS[username]
		const validPassword = DEMO_PASSWORDS[username]

		if (user && password === validPassword) {
			return {
				token: 'demo-token-' + Date.now(),
				user: user
			}
		}

		throw new Error('Tên đăng nhập hoặc mật khẩu không đúng')
	},

	async logout() {
		if (DEMO_MODE) {
			return { success: true }
		}
		return await api.post('/auth/logout')
	},

	async getCurrentUser() {
		if (DEMO_MODE) {
			// Return from localStorage in demo mode
			const savedUser = localStorage.getItem('user')
			if (savedUser) {
				return JSON.parse(savedUser)
			}
			throw new Error('No user found in demo mode')
		}
		return await api.get('/auth/me')
	},

	async changePassword(data) {
		return await api.put('/auth/change-password', data)
	},

	async forgotPassword(email) {
		if (DEMO_MODE) {
			// Simulate delay for demo
			await new Promise(resolve => setTimeout(resolve, 1500))
			return { success: true, message: 'Email đã được gửi thành công (DEMO)' }
		}
		return await api.post('/auth/forgot-password', { email })
	},

	/**
	 * Verify if current token is still valid
	 * @returns {Promise<boolean>} True if token is valid
	 */
	async verifyToken() {
		if (DEMO_MODE) {
			// In demo mode, check if token exists in localStorage
			const savedToken = localStorage.getItem('token')
			return !!savedToken
		}
		try {
			await api.get('/auth/verify')
			return true
		} catch {
			return false
		}
	},

	/**
	 * Refresh access token using refresh token
	 * @param {string} refreshToken - The refresh token
	 * @returns {Promise<Object>} New token and user data
	 */
	async refreshToken(refreshToken) {
		if (DEMO_MODE) {
			// In demo mode, return current user data
			const savedUser = localStorage.getItem('user')
			if (savedUser) {
				return {
					token: 'demo-token-' + Date.now(),
					user: JSON.parse(savedUser)
				}
			}
			throw new Error('No user found in demo mode')
		}
		return await api.post('/auth/refresh', { refreshToken })
	}
}

export default authService

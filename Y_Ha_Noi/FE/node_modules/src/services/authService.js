import api from './api'

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
		// Try real API first
		try {
			return await api.post('/auth/login', credentials)
		} catch (error) {
			// Fallback to demo mode if backend not available
			console.log('Backend not available, using demo mode')
			return this.demoLogin(credentials)
		}
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
		try {
			return await api.post('/auth/logout')
		} catch (error) {
			// Demo mode - just return success
			return { success: true }
		}
	},

	async getCurrentUser() {
		try {
			return await api.get('/auth/me')
		} catch (error) {
			// Return from localStorage in demo mode
			const savedUser = localStorage.getItem('user')
			if (savedUser) {
				return JSON.parse(savedUser)
			}
			throw error
		}
	},

	async changePassword(data) {
		return await api.put('/auth/change-password', data)
	}
}

export default authService

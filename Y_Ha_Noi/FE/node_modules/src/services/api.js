import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

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
		const token = localStorage.getItem('token')
		if (token) {
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

		if (response) {
			switch (response.status) {
				case 401:
					localStorage.removeItem('token')
					localStorage.removeItem('user')
					router.push('/login')
					ElMessage.error('Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.')
					break
				case 403:
					ElMessage.error('Bạn không có quyền thực hiện thao tác này.')
					break
				case 404:
					ElMessage.error('Không tìm thấy dữ liệu.')
					break
				case 500:
					ElMessage.error('Lỗi máy chủ. Vui lòng thử lại sau.')
					break
				default:
					ElMessage.error(response.data?.message || 'Đã có lỗi xảy ra.')
			}
		} else {
			ElMessage.error('Không thể kết nối đến máy chủ.')
		}

		return Promise.reject(error)
	}
)

export default api


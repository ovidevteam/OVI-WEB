// Feedback Channels
export const CHANNELS = [
	{ value: 'HOTLINE', label: 'Hotline', color: '#409EFF' },
	{ value: 'EMAIL', label: 'Email', color: '#67C23A' },
	{ value: 'DIRECT', label: 'Trực tiếp', color: '#E6A23C' },
	{ value: 'ZALO', label: 'Zalo', color: '#0068FF' },
	{ value: 'FACEBOOK', label: 'Facebook', color: '#1877F2' },
	{ value: 'OTHER', label: 'Khác', color: '#909399' }
]

// Feedback Levels
export const LEVELS = [
	{ value: 'CRITICAL', label: 'Khẩn cấp', color: '#F56C6C', type: 'danger' },
	{ value: 'HIGH', label: 'Cao', color: '#E6A23C', type: 'warning' },
	{ value: 'MEDIUM', label: 'Trung bình', color: '#409EFF', type: 'primary' },
	{ value: 'LOW', label: 'Thấp', color: '#909399', type: 'info' }
]

// Feedback Status
export const STATUS = [
	{ value: 'NEW', label: 'Chưa xử lý', color: '#F56C6C', type: 'danger' },
	{ value: 'PROCESSING', label: 'Đang xử lý', color: '#E6A23C', type: 'warning' },
	{ value: 'COMPLETED', label: 'Hoàn thành', color: '#67C23A', type: 'success' }
]

// User Roles
export const ROLES = [
	{ value: 'ADMIN', label: 'Admin', color: '#F56C6C' },
	{ value: 'LEADER', label: 'Lãnh đạo', color: '#409EFF' },
	{ value: 'RECEIVER', label: 'Người tiếp nhận', color: '#67C23A' },
	{ value: 'HANDLER', label: 'Người xử lý', color: '#E6A23C' },
	{ value: 'VIEWER', label: 'Người theo dõi', color: '#909399' }
]

// User Status
export const USER_STATUS = [
	{ value: 'ACTIVE', label: 'Hoạt động', color: '#67C23A', type: 'success' },
	{ value: 'INACTIVE', label: 'Ngừng hoạt động', color: '#909399', type: 'info' }
]

// Doctor Specialties
export const SPECIALTIES = [
	'Nội khoa',
	'Ngoại khoa',
	'Sản khoa',
	'Nhi khoa',
	'Tim mạch',
	'Thần kinh',
	'Da liễu',
	'Mắt',
	'Tai Mũi Họng',
	'Răng Hàm Mặt',
	'Chẩn đoán hình ảnh',
	'Xét nghiệm',
	'Cấp cứu',
	'Hồi sức tích cực',
	'Khác'
]

// Pagination
export const PAGE_SIZES = [10, 20, 50, 100]
export const DEFAULT_PAGE_SIZE = 20

// Upload
export const UPLOAD_CONFIG = {
	maxSize: parseInt(import.meta.env.VITE_UPLOAD_MAX_SIZE) || 5242880, // 5MB
	maxFiles: parseInt(import.meta.env.VITE_UPLOAD_MAX_FILES) || 10,
	acceptTypes: ['image/jpeg', 'image/png', 'image/webp'],
	acceptExtensions: ['.jpg', '.jpeg', '.png', '.webp']
}

// Date formats
export const DATE_FORMAT = 'DD/MM/YYYY'
export const DATETIME_FORMAT = 'DD/MM/YYYY HH:mm'
export const TIME_FORMAT = 'HH:mm'


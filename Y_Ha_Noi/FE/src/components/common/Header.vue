<template>
	<header class="app-header">
		<div class="header-left">
			<el-button
				class="toggle-btn"
				:icon="uiStore.sidebarCollapsed ? 'Expand' : 'Fold'"
				@click="uiStore.toggleSidebar"
				text
			/>

			<el-breadcrumb separator="/" class="breadcrumb">
				<el-breadcrumb-item :to="{ path: '/dashboard' }">
					<el-icon><HomeFilled /></el-icon>
				</el-breadcrumb-item>
				<el-breadcrumb-item v-if="currentRoute.meta?.title">
					{{ currentRoute.meta.title }}
				</el-breadcrumb-item>
			</el-breadcrumb>
		</div>

		<div class="header-right">
			<!-- Notifications Dropdown -->
			<el-popover
				placement="bottom-end"
				:width="360"
				trigger="click"
				popper-class="notification-popover"
			>
				<template #reference>
					<div class="notification-btn-wrapper" :class="{ 'has-unread': unreadCount > 0 }">
						<el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="notification-badge">
							<div class="notification-btn">
								<el-icon :size="22"><Bell /></el-icon>
							</div>
						</el-badge>
					</div>
				</template>

				<div class="notification-header">
					<span class="notification-title">Thông báo</span>
					<el-button v-if="unreadCount > 0" type="primary" link size="small" @click="markAllRead">
						Đánh dấu tất cả đã đọc
					</el-button>
				</div>

				<el-scrollbar max-height="320px">
					<div v-if="notifications.length === 0" class="notification-empty">
						<el-icon :size="40"><BellFilled /></el-icon>
						<p>Không có thông báo mới</p>
					</div>

					<div
						v-for="item in notifications"
						:key="item.id"
						class="notification-item"
						:class="{ unread: !item.read }"
						@click="handleNotificationClick(item)"
					>
						<div class="notification-icon" :class="item.type">
							<el-icon>
								<ChatDotRound v-if="item.type === 'feedback'" />
								<UserFilled v-else-if="item.type === 'assigned'" />
								<CircleCheckFilled v-else-if="item.type === 'completed'" />
								<Star v-else-if="item.type === 'rating'" />
								<CircleCheckFilled v-else />
							</el-icon>
						</div>
						<div class="notification-content">
							<div class="notification-text">{{ item.title }}</div>
							<div class="notification-desc">{{ item.message }}</div>
							<div class="notification-time">{{ item.time }}</div>
						</div>
					</div>
				</el-scrollbar>
			</el-popover>

			<!-- User Dropdown -->
			<el-dropdown trigger="click" @command="handleCommand">
				<div class="user-info">
					<el-avatar :size="36" class="user-avatar">
						{{ userInitials }}
					</el-avatar>
					<div class="user-details">
						<span class="user-name">{{ authStore.userName }}</span>
						<span class="user-role">{{ roleLabel }}</span>
					</div>
					<el-icon class="dropdown-icon"><ArrowDown /></el-icon>
				</div>

				<template #dropdown>
					<el-dropdown-menu>
						<el-dropdown-item :icon="User" command="profile">
							Thông tin cá nhân
						</el-dropdown-item>
						<el-dropdown-item :icon="Lock" command="password">
							Đổi mật khẩu
						</el-dropdown-item>
						<el-dropdown-item divided :icon="SwitchButton" command="logout">
							Đăng xuất
						</el-dropdown-item>
					</el-dropdown-menu>
				</template>
			</el-dropdown>
		</div>

		<!-- Profile Dialog -->
		<el-dialog v-model="profileDialogVisible" title="Thông tin cá nhân" width="500px">
			<el-form :model="profileForm" label-width="120px" label-position="left">
				<el-form-item label="Họ tên">
					<el-input v-model="profileForm.fullName" />
				</el-form-item>
				<el-form-item label="Email">
					<el-input v-model="profileForm.email" />
				</el-form-item>
				<el-form-item label="Điện thoại">
					<el-input v-model="profileForm.phone" />
				</el-form-item>
				<el-form-item label="Phòng ban">
					<el-input v-model="profileForm.department" disabled />
				</el-form-item>
				<el-form-item label="Vai trò">
					<el-input v-model="profileForm.role" disabled />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="profileDialogVisible = false">Đóng</el-button>
				<el-button type="primary" @click="saveProfile">Lưu thay đổi</el-button>
			</template>
		</el-dialog>

		<!-- Change Password Dialog -->
		<el-dialog v-model="passwordDialogVisible" title="Đổi mật khẩu" width="450px">
			<el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="140px" label-position="left">
				<el-form-item label="Mật khẩu hiện tại" prop="currentPassword">
					<el-input v-model="passwordForm.currentPassword" type="password" show-password />
				</el-form-item>
				<el-form-item label="Mật khẩu mới" prop="newPassword">
					<el-input v-model="passwordForm.newPassword" type="password" show-password />
				</el-form-item>
				<el-form-item label="Xác nhận mật khẩu" prop="confirmPassword">
					<el-input v-model="passwordForm.confirmPassword" type="password" show-password />
				</el-form-item>
			</el-form>
			<template #footer>
				<el-button @click="passwordDialogVisible = false">Hủy</el-button>
				<el-button type="primary" @click="changePassword">Đổi mật khẩu</el-button>
			</template>
		</el-dialog>
	</header>
</template>

<script setup>
import { computed, ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import { getRoleLabel, getRelativeTime } from '@/utils/helpers'
import api from '@/services/api'
import authService from '@/services/authService'
import feedbackService from '@/services/feedbackService'
import { mockNotifications } from '@/mock/db'
import {
	HomeFilled, Bell, BellFilled, User, Lock, SwitchButton, ArrowDown,
	ChatDotRound, UserFilled, CircleCheckFilled, Star
} from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { handleApiError } from '@/utils/errorHandler'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const uiStore = useUIStore()

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

// Notifications
const notifications = ref([])

const fetchNotifications = async () => {
	if (DEMO_MODE) {
		// Use mock data from db.js
		notifications.value = [...mockNotifications]
		return
	}

	try {
		// Call real API - api interceptor already unwraps response.data
		const rawNotifications = await api.get('/notifications')
		// Ensure it's an array
		const notificationsArray = Array.isArray(rawNotifications) ? rawNotifications : (rawNotifications?.data || [])
		// Map backend data to frontend format
		notifications.value = notificationsArray.map(notif => ({
			...notif,
			// Convert enum type to lowercase for frontend (FEEDBACK -> feedback, ASSIGNED -> assigned)
			type: notif.type?.toLowerCase() || notif.type,
			// Add time property from createdAt
			time: getRelativeTime(notif.createdAt)
		}))
	} catch (error) {
		// Don't set to mock data if DEMO_MODE is false
		notifications.value = []
		// Only show error if it's not 403 (might be permission issue)
		if (error?.response?.status !== 403) {
			handleApiError(error, 'Fetch Notifications')
		}
	}
}

const unreadCount = computed(() => notifications.value.filter(n => !n.read).length)

const markAllRead = async () => {
	if (DEMO_MODE) {
		notifications.value.forEach(n => n.read = true)
		ElMessage.success('Đã đánh dấu tất cả là đã đọc')
	} else {
		try {
			await api.post('/notifications/mark-all-read')
			notifications.value.forEach(n => n.read = true)
			ElMessage.success('Đã đánh dấu tất cả là đã đọc')
		} catch (error) {
			handleApiError(error, 'Mark notifications as read')
		}
	}
}

const handleNotificationClick = async (item) => {
	if (!DEMO_MODE) {
		try {
			// Mark as read via API
			await api.put(`/notifications/${item.id}/read`)
		} catch (error) {
			// Error marking notification as read - non-critical
		}
	}
	item.read = true
	
	// Get feedback ID from notification
	let feedbackId = item.feedbackId
	
	// If no feedbackId, try to extract from message or find by code
	if (!feedbackId) {
		const codeMatch = item.message?.match(/PA-\d{8}-\d{3}/)
		if (codeMatch) {
			try {
				const feedback = await feedbackService.getByCode(codeMatch[0])
				if (feedback && feedback.id) {
					feedbackId = feedback.id
				}
			} catch (error) {
				ElMessage.warning(`Không tìm thấy phản ánh với mã: ${codeMatch[0]}`)
				return
			}
		}
	}
	
	if (!feedbackId) {
		ElMessage.warning('Không thể xác định phản ánh từ thông báo.')
		return
	}
	
	// Navigate with query param to auto-open popup
	if (item.type === 'rating') {
		// For rating notifications, navigate to ratings page with feedbackId query param
		router.push({
			path: '/feedback/ratings',
			query: { feedbackId: feedbackId.toString() }
		})
	} else {
		// For feedback/assigned/completed notifications, navigate to feedback list page with feedbackId query param
		router.push({
			path: '/feedback',
			query: { feedbackId: feedbackId.toString() }
		})
	}
}

let notificationInterval = null

onMounted(() => {
	fetchNotifications()
	// Refresh notifications periodically (every 30 seconds)
	notificationInterval = setInterval(fetchNotifications, 30000)
})

onBeforeUnmount(() => {
	if (notificationInterval) {
		clearInterval(notificationInterval)
	}
})

// Profile Dialog
const profileDialogVisible = ref(false)
const profileForm = reactive({
	fullName: '',
	email: '',
	phone: '',
	department: '',
	role: ''
})

const openProfile = async () => {
	// Refresh user data from API before opening dialog to get latest info
	try {
		const currentUser = await authService.getCurrentUser()
		authStore.user = currentUser
		localStorage.setItem('user', JSON.stringify(currentUser))
	} catch (error) {
		// Error fetching current user - non-critical
	}
	
	// Populate form with current user data
	profileForm.fullName = authStore.user?.fullName || 'Quản trị viên'
	profileForm.email = authStore.user?.email || 'admin@bvyhanoi.vn'
	profileForm.phone = authStore.user?.phone || ''
	profileForm.department = authStore.user?.departmentName || 'Quản trị hệ thống'
	profileForm.role = getRoleLabel(authStore.userRole)
	profileDialogVisible.value = true
}

const saveProfile = async () => {
	try {
		// Call API to update user profile
		// Note: api interceptor already unwraps response.data, so response is the User object directly
		const updatedUser = await api.put('/auth/me', {
			fullName: profileForm.fullName,
			email: profileForm.email,
			phone: profileForm.phone
		})

		// User updated successfully

		// Check if updatedUser is valid
		if (!updatedUser || !updatedUser.fullName) {
			ElMessage.error('Lỗi: Dữ liệu trả về không hợp lệ')
			return
		}

		// Update auth store with new user info
		authStore.user = updatedUser
		localStorage.setItem('user', JSON.stringify(updatedUser))

		// Refresh profile form with updated data
		profileForm.fullName = updatedUser.fullName || profileForm.fullName
		profileForm.email = updatedUser.email || profileForm.email
		profileForm.phone = updatedUser.phone || profileForm.phone

		ElMessage.success('Đã cập nhật thông tin cá nhân')
		profileDialogVisible.value = false
	} catch (error) {
		handleApiError(error, 'Update profile')
	}
}

// Password Dialog
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
	currentPassword: '',
	newPassword: '',
	confirmPassword: ''
})

const passwordRules = {
	currentPassword: [
		{ required: true, message: 'Vui lòng nhập mật khẩu hiện tại', trigger: 'blur' }
	],
	newPassword: [
		{ required: true, message: 'Vui lòng nhập mật khẩu mới', trigger: 'blur' },
		{ min: 6, message: 'Mật khẩu tối thiểu 6 ký tự', trigger: 'blur' }
	],
	confirmPassword: [
		{ required: true, message: 'Vui lòng xác nhận mật khẩu', trigger: 'blur' },
		{
			validator: (rule, value, callback) => {
				if (value !== passwordForm.newPassword) {
					callback(new Error('Mật khẩu xác nhận không khớp'))
				} else {
					callback()
				}
			},
			trigger: 'blur'
		}
	]
}

const changePassword = async () => {
	if (!passwordFormRef.value) return
	try {
		await passwordFormRef.value.validate()
		
		// Call API to change password
		await authService.changePassword({
			oldPassword: passwordForm.currentPassword,
			newPassword: passwordForm.newPassword
		})

		ElMessage.success('Đã đổi mật khẩu thành công')
		passwordDialogVisible.value = false
		passwordForm.currentPassword = ''
		passwordForm.newPassword = ''
		passwordForm.confirmPassword = ''
	} catch (error) {
		if (error.response?.data?.message) {
			ElMessage.error(error.response.data.message)
		} else if (error.message) {
			ElMessage.error(error.message)
		} else {
			ElMessage.error('Lỗi khi đổi mật khẩu')
		}
	}
}

const currentRoute = computed(() => route)

const userInitials = computed(() => {
	const name = authStore.userName || ''
	return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
})

const roleLabel = computed(() => getRoleLabel(authStore.userRole))

const handleCommand = async (command) => {
	switch (command) {
		case 'profile':
			openProfile()
			break
		case 'password':
			passwordDialogVisible.value = true
			break
		case 'logout':
			try {
				await ElMessageBox.confirm(
					'Bạn có chắc chắn muốn đăng xuất?',
					'Xác nhận',
					{
						confirmButtonText: 'Đăng xuất',
						cancelButtonText: 'Hủy',
						type: 'warning'
					}
				)
				await authStore.logout()
				router.push('/login')
			} catch {
				// Cancelled
			}
			break
	}
}
</script>

<style scoped>
.app-header {
	height: var(--header-height);
	background: var(--card-background);
	border-bottom: 1px solid var(--border-color);
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 0 20px;
	position: sticky;
	top: 0;
	z-index: 100;
}

.header-left {
	display: flex;
	align-items: center;
	gap: 16px;
}

.toggle-btn {
	font-size: 18px;
}

.breadcrumb {
	font-size: 14px;
}

.header-right {
	display: flex;
	align-items: center;
	gap: 16px;
}

/* Notification Button Styles */
.notification-btn-wrapper {
	position: relative;
	cursor: pointer;
}

.notification-btn {
	width: 42px;
	height: 42px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	background: var(--background-color);
	color: var(--text-secondary);
	transition: all 0.3s ease;
	border: 1px solid transparent;
}

.notification-btn:hover {
	background: var(--border-color);
	color: var(--text-primary);
}

/* Khi có thông báo chưa đọc */
.notification-btn-wrapper.has-unread .notification-btn {
	background: linear-gradient(135deg, rgba(239, 68, 68, 0.12) 0%, rgba(220, 38, 38, 0.08) 100%);
	color: #dc2626;
	border-color: rgba(239, 68, 68, 0.2);
	animation: pulse-notify 2s infinite;
}

.notification-btn-wrapper.has-unread .notification-btn:hover {
	background: linear-gradient(135deg, rgba(239, 68, 68, 0.2) 0%, rgba(220, 38, 38, 0.15) 100%);
}

@keyframes pulse-notify {
	0%, 100% {
		box-shadow: 0 0 0 0 rgba(239, 68, 68, 0.4);
	}
	50% {
		box-shadow: 0 0 0 8px rgba(239, 68, 68, 0);
	}
}

/* Badge styling */
.notification-badge :deep(.el-badge__content) {
	background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
	border: 2px solid white;
	font-weight: 600;
	font-size: 11px;
	min-width: 20px;
	height: 20px;
	line-height: 16px;
	padding: 0 6px;
	box-shadow: 0 2px 6px rgba(220, 38, 38, 0.4);
}

/* Notification Dropdown Styles */
.notification-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding-bottom: 12px;
	margin-bottom: 8px;
	border-bottom: 1px solid var(--border-color);
}

.notification-title {
	font-weight: 600;
	font-size: 15px;
	color: var(--text-primary);
}

.notification-empty {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 40px 20px;
	color: var(--text-muted);
}

.notification-empty p {
	margin-top: 12px;
	font-size: 14px;
}

.notification-item {
	position: relative;
	display: flex;
	gap: 12px;
	padding: 12px;
	margin: 4px 0;
	border-radius: var(--radius-md);
	cursor: pointer;
	transition: background-color var(--transition-fast);
	background-color: transparent;
}

.notification-item:hover {
	background-color: var(--background-color);
}

.notification-item.unread {
	background-color: rgba(13, 110, 253, 0.05);
}

.notification-item.unread::before {
	content: '';
	position: absolute;
	left: 4px;
	top: 50%;
	transform: translateY(-50%);
	width: 6px;
	height: 6px;
	border-radius: 50%;
	background: var(--primary-color);
}

.notification-icon {
	width: 40px;
	height: 40px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.notification-icon.feedback {
	background: rgba(220, 53, 69, 0.1);
	color: var(--danger-color);
}

.notification-icon.assigned {
	background: rgba(255, 193, 7, 0.15);
	color: #997404;
}

.notification-icon.completed {
	background: rgba(25, 135, 84, 0.1);
	color: var(--success-color);
}

.notification-content {
	flex: 1;
	min-width: 0;
}

.notification-text {
	font-weight: 500;
	font-size: 14px;
	color: var(--text-primary);
}

.notification-desc {
	font-size: 13px;
	color: var(--text-secondary);
	margin-top: 2px;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
}

.notification-time {
	font-size: 11px;
	color: var(--text-muted);
	margin-top: 4px;
}

/* User Info Styles */
.user-info {
	display: flex;
	align-items: center;
	gap: 10px;
	cursor: pointer;
	padding: 6px 12px;
	border-radius: var(--radius-md);
	transition: background-color var(--transition-fast);
}

.user-info:hover {
	background-color: var(--background-color);
}

.user-avatar {
	background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%);
	color: white;
	font-weight: 600;
	font-size: 13px;
}

.user-details {
	display: flex;
	flex-direction: column;
	line-height: 1.3;
}

.user-name {
	font-weight: 500;
	color: var(--text-primary);
	font-size: 14px;
}

.user-role {
	font-size: 12px;
	color: var(--text-secondary);
}

.dropdown-icon {
	color: var(--text-secondary);
	margin-left: 4px;
}

@media (max-width: 768px) {
	.breadcrumb,
	.user-details {
		display: none;
	}

	.app-header {
		padding: 0 12px;
	}

	.header-right {
		gap: 8px;
	}
}
</style>



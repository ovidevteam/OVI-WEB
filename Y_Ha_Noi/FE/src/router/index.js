import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Layouts
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import AuthLayout from '@/layouts/AuthLayout.vue'

// Views
const Login = () => import('@/views/Login.vue')
const Dashboard = () => import('@/views/Dashboard.vue')
const FeedbackList = () => import('@/views/feedback/FeedbackList.vue')
const FeedbackCreate = () => import('@/views/feedback/FeedbackCreate.vue')
const FeedbackDetail = () => import('@/views/feedback/FeedbackDetail.vue')
const FeedbackRatings = () => import('@/views/feedback/FeedbackRatings.vue')
const MyFeedbacks = () => import('@/views/feedback/MyFeedbacks.vue')
const UserManagement = () => import('@/views/admin/UserManagement.vue')
const DepartmentManagement = () => import('@/views/admin/DepartmentManagement.vue')
const DoctorManagement = () => import('@/views/admin/DoctorManagement.vue')
const ReportByDepartment = () => import('@/views/reports/ReportByDepartment.vue')
const ReportByDoctor = () => import('@/views/reports/ReportByDoctor.vue')
const ReportWithImages = () => import('@/views/reports/ReportWithImages.vue')

const routes = [
	{
		path: '/login',
		component: AuthLayout,
		children: [
			{
				path: '',
				name: 'Login',
				component: Login,
				meta: { requiresAuth: false }
			}
		]
	},
	{
		path: '/',
		component: DefaultLayout,
		meta: { requiresAuth: true },
		children: [
			{
				path: '',
				redirect: '/dashboard'
			},
			{
				path: 'dashboard',
				name: 'Dashboard',
				component: Dashboard,
				meta: { title: 'Dashboard', icon: 'Odometer' }
			},
			// Feedback routes
			{
				path: 'feedback',
				name: 'FeedbackList',
				component: FeedbackList,
				meta: { title: 'Danh sách Phản ánh', icon: 'ChatDotRound' }
			},
			{
				path: 'feedback/create',
				name: 'FeedbackCreate',
				component: FeedbackCreate,
				meta: { title: 'Nhập Phản ánh mới', icon: 'Plus', roles: ['ADMIN', 'RECEIVER'] }
			},
			{
				path: 'feedback/:id',
				name: 'FeedbackDetail',
				component: FeedbackDetail,
				meta: { title: 'Chi tiết Phản ánh', icon: 'Document' }
			},
			{
				path: 'feedback/ratings',
				name: 'FeedbackRatings',
				component: FeedbackRatings,
				meta: { title: 'Đánh giá Bác sĩ', icon: 'Star', roles: ['ADMIN', 'LEADER', 'HANDLER'] }
			},
			{
				path: 'my-feedbacks',
				name: 'MyFeedbacks',
				component: MyFeedbacks,
				meta: { title: 'Phản ánh của tôi', icon: 'User', roles: ['HANDLER'] }
			},
			// Admin routes
			{
				path: 'admin/users',
				name: 'UserManagement',
				component: UserManagement,
				meta: { title: 'Quản lý User', icon: 'UserFilled', roles: ['ADMIN'] }
			},
			{
				path: 'admin/departments',
				name: 'DepartmentManagement',
				component: DepartmentManagement,
				meta: { title: 'Quản lý Phòng ban', icon: 'OfficeBuilding', roles: ['ADMIN'] }
			},
			{
				path: 'admin/doctors',
				name: 'DoctorManagement',
				component: DoctorManagement,
				meta: { title: 'Quản lý Bác sĩ', icon: 'FirstAidKit', roles: ['ADMIN'] }
			},
			// Report routes
			{
				path: 'reports/by-department',
				name: 'ReportByDepartment',
				component: ReportByDepartment,
				meta: { title: 'Báo cáo theo Phòng', icon: 'DataAnalysis', roles: ['ADMIN', 'LEADER'] }
			},
			{
				path: 'reports/by-doctor',
				name: 'ReportByDoctor',
				component: ReportByDoctor,
				meta: { title: 'Báo cáo theo Bác sĩ', icon: 'TrendCharts', roles: ['ADMIN', 'LEADER'] }
			},
			{
				path: 'reports/with-images',
				name: 'ReportWithImages',
				component: ReportWithImages,
				meta: { title: 'Báo cáo có Hình ảnh', icon: 'Picture', roles: ['ADMIN', 'LEADER'] }
			}
		]
	},
	{
		path: '/:pathMatch(.*)*',
		redirect: '/dashboard'
	}
]

const router = createRouter({
	history: createWebHistory(),
	routes
})

// Navigation guards
router.beforeEach(async (to, from, next) => {
	const authStore = useAuthStore()
	const requiresAuth = to.matched.some(record => record.meta.requiresAuth)
	const allowedRoles = to.meta.roles

	// If route requires auth, verify token validity
	if (requiresAuth && authStore.isAuthenticated) {
		const isValid = await authStore.verifyToken()
		if (!isValid) {
			// Token invalid, redirect to login
			next('/login')
			return
		}
	}

	// Check authentication
	if (requiresAuth && !authStore.isAuthenticated) {
		next('/login')
		return
	}

	// Redirect authenticated users away from login
	if (to.path === '/login' && authStore.isAuthenticated) {
		next('/dashboard')
		return
	}

	// Check role permissions
	if (allowedRoles && !allowedRoles.includes(authStore.user?.role)) {
		next('/dashboard')
		return
	}

	// Update activity on navigation
	if (authStore.isAuthenticated) {
		authStore.updateActivity()
	}

	next()
})

export default router


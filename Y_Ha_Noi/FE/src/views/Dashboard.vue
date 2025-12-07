<template>
	<div class="dashboard" v-loading="loading">
		<!-- Stats Cards -->
		<div class="stats-grid">
			<div class="stat-card primary">
				<div class="stat-icon">
					<el-icon><ChatDotRound /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.total }}</div>
					<div class="stat-label">Tổng phản ánh tháng</div>
				</div>
			</div>

			<div class="stat-card warning">
				<div class="stat-icon">
					<el-icon><Loading /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.processing }}</div>
					<div class="stat-label">Đang xử lý</div>
				</div>
			</div>

			<div class="stat-card success">
				<div class="stat-icon">
					<el-icon><CircleCheck /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.completed }}</div>
					<div class="stat-label">Hoàn thành</div>
				</div>
			</div>

			<div class="stat-card danger">
				<div class="stat-icon">
					<el-icon><Warning /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.overdue }}</div>
					<div class="stat-label">Quá hạn</div>
				</div>
			</div>
		</div>

		<!-- Charts Row -->
		<div class="charts-row">
			<div class="content-card chart-card">
				<div class="content-card-header">
					<h3 class="content-card-title">Phản ánh theo tháng</h3>
					<el-select v-model="selectedYear" size="small" style="width: 100px">
						<el-option
							v-for="year in years"
							:key="year"
							:label="year"
							:value="year"
						/>
					</el-select>
				</div>
				<div class="chart-container">
					<LineChart :data="monthlyData" />
				</div>
			</div>

			<div class="content-card chart-card">
				<div class="content-card-header">
					<h3 class="content-card-title">Top 5 Phòng ban</h3>
				</div>
				<div class="chart-container">
					<BarChart :data="departmentData" />
				</div>
			</div>
		</div>

		<!-- Recent Feedbacks -->
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Phản ánh mới nhất</h3>
				<el-button type="primary" text @click="goToList">
					Xem tất cả <el-icon class="el-icon--right"><ArrowRight /></el-icon>
				</el-button>
			</div>

			<el-table 
				:data="recentFeedbacks" 
				stripe 
				style="width: 100%" 
				row-key="id"
				@row-click="handleRowClick"
				row-class-name="clickable-row"
				aria-label="Danh sách phản ánh mới nhất"
				:default-sort="{ prop: 'receivedDate', order: 'descending' }"
			>
				<el-table-column prop="code" label="Số PA" width="160" />
				<el-table-column prop="receivedDate" label="Ngày nhận" width="120">
					<template #default="{ row }">
						{{ formatDate(row.receivedDate) }}
					</template>
				</el-table-column>
				<el-table-column prop="channel" label="Kênh" width="100">
					<template #default="{ row }">
						<el-tag size="small" effect="plain">
							{{ getChannelLabel(row.channel) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="content" label="Nội dung" min-width="250">
					<template #default="{ row }">
						<el-tooltip :content="row.content" placement="top" :show-after="500">
							<span class="content-cell">{{ row.content }}</span>
						</el-tooltip>
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng ban" min-width="120" />
				<el-table-column prop="doctorName" label="Bác sĩ" min-width="150" />
				<el-table-column prop="level" label="Mức độ" width="100" align="center">
					<template #default="{ row }">
						<el-tag :type="getLevelType(row.level)" size="small">
							{{ getLevelLabel(row.level) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="Trạng thái" width="120" align="center">
					<template #default="{ row }">
						<el-tag :type="getStatusType(row.status)" size="small">
							{{ getStatusLabel(row.status) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="handlerName" label="Người xử lý" min-width="140" />
			</el-table>
		</div>

		<!-- Feedback Detail Dialog -->
		<el-dialog
			v-model="detailDialogVisible"
			title="Chi tiết Phản ánh"
			width="1200px"
			:close-on-click-modal="false"
			destroy-on-close
			align-center
			class="feedback-detail-dialog"
		>
			<FeedbackDetail v-if="selectedFeedbackId" :feedback-id="selectedFeedbackId" :is-dialog="true" />
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import {
	ChatDotRound, Loading, CircleCheck, Warning,
	ArrowRight
} from '@element-plus/icons-vue'
import FeedbackDetail from '@/views/feedback/FeedbackDetail.vue'
import { formatDate, truncate, getChannelLabel, getLevelLabel, getLevelType, getStatusLabel, getStatusType } from '@/utils/helpers'
import { handleApiError } from '@/utils/errorHandler'
import reportService from '@/services/reportService'
import feedbackService from '@/services/feedbackService'
import departmentService from '@/services/departmentService'
import doctorService from '@/services/doctorService'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import {
	mockDashboardStats,
	mockMonthlyStats,
	mockDepartmentStats,
	mockFeedbacks
} from '@/mock/db'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const stats = reactive({
	total: 0,
	processing: 0,
	completed: 0,
	overdue: 0
})

const selectedYear = ref(new Date().getFullYear())
const years = computed(() => {
	const currentYear = new Date().getFullYear()
	return [currentYear - 2, currentYear - 1, currentYear]
})

const monthlyData = ref({
	labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
	datasets: [{
		label: 'Phản ánh',
		data: [],
		borderColor: '#0d6efd',
		backgroundColor: 'rgba(13, 110, 253, 0.1)',
		fill: true,
		tension: 0.4
	}]
})

const departmentData = ref({
	labels: [],
	datasets: [{
		label: 'Số phản ánh',
		data: [],
		backgroundColor: [
			'rgba(13, 110, 253, 0.8)',
			'rgba(25, 135, 84, 0.8)',
			'rgba(255, 193, 7, 0.8)',
			'rgba(220, 53, 69, 0.8)',
			'rgba(108, 117, 125, 0.8)'
		]
	}]
})

const recentFeedbacks = ref([])
const loading = ref(false)
const departments = ref([]) // Store departments for mapping
const doctors = ref([]) // Store doctors for mapping
const detailDialogVisible = ref(false)
const selectedFeedbackId = ref(null)

const goToList = () => {
	router.push('/feedback')
}

const handleRowClick = (row) => {
	selectedFeedbackId.value = row.id
	detailDialogVisible.value = true
}

const viewDetail = (id) => {
	selectedFeedbackId.value = id
	detailDialogVisible.value = true
}

/**
 * Load departments list for mapping departmentId to departmentName
 */
const loadDepartments = async () => {
	try {
		departments.value = await departmentService.getActiveList()
	} catch (error) {
		departments.value = []
	}
}

/**
 * Load doctors list for mapping doctorId to doctorName
 */
const loadDoctors = async () => {
	try {
		const response = await doctorService.getAll()
		// Handle both array and paginated response
		if (Array.isArray(response)) {
			doctors.value = response
		} else if (response && response.data) {
			doctors.value = response.data
		} else {
			doctors.value = []
		}
	} catch (error) {
		doctors.value = []
	}
}

/**
 * Get doctor name by ID
 */
const getDoctorName = (doctorId) => {
	if (!doctorId) return null
	const doctor = doctors.value.find(d => d.id === doctorId)
	return doctor ? doctor.fullName || doctor.name : null
}

/**
 * Get department name by ID
 */
const getDepartmentName = (departmentId) => {
	if (!departmentId) return ''
	const dept = departments.value.find(d => d.id === departmentId)
	return dept ? dept.name : ''
}

/**
 * Map departmentId to departmentName and doctorId to doctorName for feedbacks
 */
const mapFeedbackDepartments = (feedbacks) => {
	if (!feedbacks || !Array.isArray(feedbacks)) return []
	return feedbacks.map(feedback => {
		// Create a new object to avoid mutation
		const mapped = { ...feedback }
		
		// Map department - check if departmentName is missing or empty
		if (mapped.departmentId && (!mapped.departmentName || mapped.departmentName.trim() === '') && departments.value.length > 0) {
			mapped.departmentName = getDepartmentName(mapped.departmentId)
		}
		// If still no departmentName, try to get from department object if exists
		if (!mapped.departmentName && mapped.department) {
			mapped.departmentName = mapped.department.name || mapped.department.departmentName
		}
		
		// Map doctor - check if doctorName is missing or empty
		if (mapped.doctorId && (!mapped.doctorName || mapped.doctorName.trim() === '') && doctors.value.length > 0) {
			mapped.doctorName = getDoctorName(mapped.doctorId)
		}
		// If still no doctorName, try to get from doctor object if exists
		if (!mapped.doctorName && mapped.doctor) {
			mapped.doctorName = mapped.doctor.fullName || mapped.doctor.name
		}
		
		return mapped
	})
}

/**
 * Map departmentId to departmentName for department stats
 */
const mapDepartmentStats = (stats) => {
	if (!stats || !Array.isArray(stats)) return stats
	return stats.map(stat => {
		// If has departmentId but no departmentName, map it
		if (stat.departmentId && !stat.departmentName && departments.value.length > 0) {
			stat.departmentName = getDepartmentName(stat.departmentId)
		}
		// If has name but no departmentName, use name
		if (!stat.departmentName && stat.name) {
			stat.departmentName = stat.name
		}
		return stat
	})
}

const loadDashboardData = async () => {
	loading.value = true
	try {
		// Load departments and doctors first for mapping
		await Promise.all([loadDepartments(), loadDoctors()])
		
		if (DEMO_MODE) {
			// Demo data - use mock data from db.js
			Object.assign(stats, mockDashboardStats)
			// Update monthly chart data
			monthlyData.value = {
				labels: monthlyData.value.labels,
				datasets: [{
					...monthlyData.value.datasets[0],
					data: mockMonthlyStats
				}]
			}
			// Update department chart data
			departmentData.value = {
				labels: mockDepartmentStats.map(d => d.departmentName || d.name || ''),
				datasets: [{
					...departmentData.value.datasets[0],
					data: mockDepartmentStats.map(d => d.count || 0)
				}]
			}
			recentFeedbacks.value = mockFeedbacks.slice(0, 5)
			return
		}
		// Load dashboard stats - API returns { stats, monthlyStats, departmentStats, recentFeedbacks }
		try {
			const dashboardData = await reportService.getDashboard()
			if (dashboardData) {
				// Extract stats from response
				if (dashboardData.stats) {
					stats.total = dashboardData.stats.total || 0
					stats.processing = dashboardData.stats.processing || 0
					stats.completed = dashboardData.stats.completed || 0
					stats.overdue = dashboardData.stats.overdue || 0
				} else {
					// Fallback: if stats is at root level
					stats.total = dashboardData.total || 0
					stats.processing = dashboardData.processing || 0
					stats.completed = dashboardData.completed || 0
					stats.overdue = dashboardData.overdue || 0
				}

				// Extract monthly stats from dashboard response
				if (dashboardData.monthlyStats && Array.isArray(dashboardData.monthlyStats) && dashboardData.monthlyStats.length > 0) {
					// Convert array of {month, count} to array of counts
					const monthlyCounts = new Array(12).fill(0)
					dashboardData.monthlyStats.forEach(item => {
						if (item.month >= 1 && item.month <= 12) {
							monthlyCounts[item.month - 1] = item.count || 0
						}
					})
					// Update chart data properly - create new object to trigger reactivity
					monthlyData.value = {
						labels: monthlyData.value.labels,
						datasets: [{
							...monthlyData.value.datasets[0],
							data: monthlyCounts
						}]
					}
				}

				// Extract department stats from dashboard response
				if (dashboardData.departmentStats && Array.isArray(dashboardData.departmentStats) && dashboardData.departmentStats.length > 0) {
					// Map departmentId to departmentName if needed
					const mappedStats = mapDepartmentStats(dashboardData.departmentStats)
					// Update chart data properly - create new object to trigger reactivity
					departmentData.value = {
						labels: mappedStats.map(d => d.departmentName || d.name || ''),
						datasets: [{
							...departmentData.value.datasets[0],
							data: mappedStats.map(d => d.count || d.total || 0)
						}]
					}
				}

				// Extract recent feedbacks from dashboard response
				if (dashboardData.recentFeedbacks && Array.isArray(dashboardData.recentFeedbacks)) {
					// Map feedbacks with department and doctor names
					const mappedFeedbacks = mapFeedbackDepartments(dashboardData.recentFeedbacks)
					recentFeedbacks.value = mappedFeedbacks
				}
			}
		} catch (error) {
			// If 403 Forbidden, user doesn't have permission - redirect or show message
			if (error?.response?.status === 403) {
				handleApiError(error, 'Dashboard')
				// Redirect to appropriate page based on user role
				if (authStore.isHandler) {
					router.push('/my-feedbacks')
				} else {
					router.push('/feedback')
				}
				return
			}
			// Log error but continue with fallback APIs
			console.error('Error loading dashboard data:', error)
		}

		// If monthly stats not loaded from dashboard, try separate API
		if (!monthlyData.value.datasets[0].data || monthlyData.value.datasets[0].data.length === 0 || monthlyData.value.datasets[0].data.every(v => v === 0)) {
			try {
				const monthlyStats = await reportService.getMonthlyStats(selectedYear.value)
				if (monthlyStats && Array.isArray(monthlyStats) && monthlyStats.length > 0) {
					// Convert array of {month, count} to array of counts
					const monthlyCounts = new Array(12).fill(0)
					monthlyStats.forEach(item => {
						if (item.month >= 1 && item.month <= 12) {
							monthlyCounts[item.month - 1] = item.count || 0
						}
					})
					// Update chart data properly - create new object to trigger reactivity
					monthlyData.value = {
						labels: monthlyData.value.labels,
						datasets: [{
							...monthlyData.value.datasets[0],
							data: monthlyCounts
						}]
					}
				}
			} catch (error) {
				console.error('Error loading monthly stats:', error)
			}
		}

		// If department stats not loaded from dashboard, try separate API
		if (!departmentData.value.labels || departmentData.value.labels.length === 0 || !departmentData.value.datasets[0].data || departmentData.value.datasets[0].data.length === 0 || departmentData.value.datasets[0].data.every(v => v === 0)) {
			try {
				const deptStats = await reportService.getByDepartment({ limit: 5 })
				if (deptStats && Array.isArray(deptStats) && deptStats.length > 0) {
					// Map departmentId to departmentName if needed
					const mappedStats = mapDepartmentStats(deptStats)
					// Update chart data properly - create new object to trigger reactivity
					departmentData.value = {
						labels: mappedStats.map(d => d.departmentName || d.name || ''),
						datasets: [{
							...departmentData.value.datasets[0],
							data: mappedStats.map(d => d.count || d.total || 0)
						}]
					}
				}
			} catch (error) {
				console.error('Error loading department stats:', error)
			}
		}

		// If recent feedbacks not loaded from dashboard, try separate API
		if (recentFeedbacks.value.length === 0) {
			try {
				const feedbacks = await feedbackService.getList({ page: 1, size: 5, sort: 'receivedDate,desc' })
				let feedbackList = []
				if (feedbacks && feedbacks.data) {
					feedbackList = feedbacks.data
				} else if (Array.isArray(feedbacks)) {
					feedbackList = feedbacks
				}
				// Map departmentId to departmentName and doctorId to doctorName
				// Ensure departments and doctors are loaded before mapping
				if (departments.value.length === 0) {
					await loadDepartments()
				}
				if (doctors.value.length === 0) {
					await loadDoctors()
				}
				const mappedFeedbacks = mapFeedbackDepartments(feedbackList)
				recentFeedbacks.value = mappedFeedbacks.slice(0, 5)
			} catch (error) {
				// Silently fail for fallback API calls
			}
		}
	} catch (error) {
		handleApiError(error, 'Dashboard')
	} finally {
		loading.value = false
	}
}

// Listen for notification events to open feedback dialog
const handleOpenFeedbackDialog = (event) => {
	const { feedbackId } = event.detail
	if (feedbackId) {
		// Always update selectedFeedbackId and open dialog
		// This ensures that clicking different notifications will open different feedbacks
		selectedFeedbackId.value = feedbackId
		// Force dialog to open even if it's already open
		detailDialogVisible.value = true
	}
}

// Check query param and auto-open dialog
const checkQueryParam = () => {
	const feedbackId = route.query.feedbackId
	if (feedbackId) {
		// Convert to number if it's a string
		const id = typeof feedbackId === 'string' ? parseInt(feedbackId, 10) : feedbackId
		if (id && !isNaN(id)) {
			selectedFeedbackId.value = id
			detailDialogVisible.value = true
			// Clear query param after opening dialog
			router.replace({ query: {} })
		}
	}
}

onMounted(() => {
	loadDashboardData()
	
	// Check query param for auto-opening dialog (from notification click)
	checkQueryParam()
	
	// Listen for notification events (for same-page notifications)
	window.addEventListener('open-feedback-dialog', handleOpenFeedbackDialog)
})

onBeforeUnmount(() => {
	// Clean up event listener
	window.removeEventListener('open-feedback-dialog', handleOpenFeedbackDialog)
})

// Watch route query changes
watch(() => route.query.feedbackId, (newId) => {
	if (newId) {
		checkQueryParam()
	}
})

// Watch year change
watch(selectedYear, () => {
	loadDashboardData()
})
</script>

<style scoped>
.dashboard {
	animation: fadeIn 0.3s ease-out;
}

.charts-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 24px;
	margin-bottom: 24px;
}

.chart-card {
	min-height: 400px;
}

.chart-container {
	height: 320px;
	position: relative;
}

.content-cell {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
	line-height: 1.4;
	word-break: break-word;
}

.clickable-row {
	cursor: pointer;
}

.clickable-row:hover {
	background-color: var(--el-table-row-hover-bg-color);
}

@media (max-width: 1200px) {
	.charts-row {
		grid-template-columns: 1fr;
	}
}

/* Feedback Detail Dialog */
:deep(.feedback-detail-dialog) {
	max-width: 95vw;
}

:deep(.feedback-detail-dialog .el-dialog) {
	margin: 5vh auto;
	max-height: 90vh;
	display: flex;
	flex-direction: column;
}

:deep(.feedback-detail-dialog .el-dialog__body) {
	flex: 1;
	overflow-y: auto;
	padding: 20px;
}
</style>


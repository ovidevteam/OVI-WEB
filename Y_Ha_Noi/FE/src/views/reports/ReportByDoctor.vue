<template>
	<div class="report-by-doctor">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">
					<el-icon><User /></el-icon>
					Báo cáo theo Bác sĩ
				</h3>
				<div class="header-actions">
					<el-select v-model="filterDepartment" placeholder="Phòng ban" clearable @change="fetchData" style="width: 180px">
						<el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
					</el-select>
					<el-date-picker
						v-model="dateRange"
						type="monthrange"
						start-placeholder="Từ tháng"
						end-placeholder="Đến tháng"
						format="MM/YYYY"
						value-format="YYYY-MM"
						@change="fetchData"
					/>
					<el-button type="success" :icon="Download" @click="exportExcel">
						Xuất Excel
					</el-button>
				</div>
			</div>

			<!-- Summary Stats với icon và gradient -->
			<div class="stats-grid">
				<div class="stat-card stat-total">
					<div class="stat-icon">
						<el-icon><Document /></el-icon>
					</div>
					<div class="stat-info">
						<span class="stat-value">{{ summary.total }}</span>
						<span class="stat-label">Tổng phản ánh</span>
					</div>
				</div>
				<div class="stat-card stat-completed">
					<div class="stat-icon">
						<el-icon><CircleCheck /></el-icon>
					</div>
					<div class="stat-info">
						<span class="stat-value">{{ summary.completed }}</span>
						<span class="stat-label">Hoàn thành</span>
					</div>
				</div>
				<div class="stat-card stat-time">
					<div class="stat-icon">
						<el-icon><Timer /></el-icon>
					</div>
					<div class="stat-info">
						<span class="stat-value">{{ summary.avgTime }}<small> ngày</small></span>
						<span class="stat-label">Thời gian TB</span>
					</div>
				</div>
				<div class="stat-card stat-rating">
					<div class="stat-icon">
						<el-icon><Star /></el-icon>
					</div>
					<div class="stat-info">
						<span class="stat-value">{{ summary.avgRating }}</span>
						<span class="stat-label">Điểm đánh giá TB</span>
					</div>
				</div>
			</div>

			<!-- Table -->
			<div class="table-wrapper">
				<el-table
					:data="reportData"
					v-loading="loading"
					stripe
					row-key="id"
					show-summary
					:summary-method="getSummaries"
					row-class-name="clickable-row"
					@row-click="handleRowClick"
				>
				<el-table-column type="index" label="#" width="50" />
				<el-table-column prop="doctorName" label="Bác sĩ" min-width="200">
					<template #default="{ row }">
						<div class="doctor-info">
							<el-avatar :size="40" class="doctor-avatar">
								{{ row.doctorName?.charAt(0) }}
							</el-avatar>
							<div class="doctor-details">
								<div class="doctor-name">{{ row.doctorName }}</div>
								<div class="doctor-dept">{{ row.departmentName }}</div>
							</div>
						</div>
					</template>
				</el-table-column>
				<el-table-column prop="specialty" label="Chuyên khoa" width="140">
					<template #default="{ row }">
						<el-tag size="small" effect="plain">{{ row.specialty }}</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="total" label="Tổng PA" width="100" align="center" sortable />
				<el-table-column prop="completed" label="Hoàn thành" width="110" align="center">
					<template #default="{ row }">
						<span class="text-success font-bold">{{ row.completed }}</span>
					</template>
				</el-table-column>
				<el-table-column prop="avgDays" label="Thời gian TB" width="130" align="center" sortable>
					<template #default="{ row }">
						<el-tag
							:type="row.avgDays <= 1.5 ? 'success' : row.avgDays <= 2.5 ? 'warning' : 'danger'"
							size="small"
							effect="dark"
						>
							<el-icon><Timer /></el-icon>
							{{ row.avgDays }} ngày
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="rating" label="Đánh giá" width="180" align="center">
					<template #default="{ row }">
						<div class="rating-cell">
							<el-rate v-model="row.rating" disabled show-score text-color="#ff9900" />
						</div>
					</template>
				</el-table-column>
				<el-table-column label="" width="60" align="center">
					<template #default>
						<el-icon class="view-icon"><ArrowRight /></el-icon>
					</template>
				</el-table-column>
			</el-table>
			</div>

			<!-- Pagination -->
			<div class="pagination-wrapper">
				<el-pagination
					v-model:current-page="currentPage"
					v-model:page-size="pageSize"
					:total="total"
					layout="total, prev, pager, next"
				/>
			</div>
		</div>

		<!-- Charts -->
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">
					<el-icon><TrendCharts /></el-icon>
					Biểu đồ so sánh
				</h3>
			</div>
			<!-- Chart -->
			<div class="chart-container">
				<BarChart :data="chartData" />
			</div>
		</div>

		<!-- Detail Dialog -->
		<el-dialog
			v-model="detailDialogVisible"
			:title="'Chi tiết: ' + selectedDoctor?.doctorName"
			width="1400px"
			class="detail-dialog"
			:close-on-click-modal="false"
		>
			<div class="detail-content" v-if="selectedDoctor">
				<!-- Doctor Summary -->
				<div class="detail-summary">
					<div class="summary-card summary-total">
						<div class="summary-icon">
							<el-icon><Document /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ detailStats.total }}</span>
							<span class="summary-label">Tổng phản ánh</span>
						</div>
					</div>
					<div class="summary-card summary-completed">
						<div class="summary-icon">
							<el-icon><CircleCheck /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ detailStats.completed }}</span>
							<span class="summary-label">Hoàn thành</span>
						</div>
					</div>
					<div class="summary-card summary-processing">
						<div class="summary-icon">
							<el-icon><Loading /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ detailStats.processing }}</span>
							<span class="summary-label">Đang xử lý</span>
						</div>
					</div>
					<div class="summary-card summary-pending">
						<div class="summary-icon">
							<el-icon><Warning /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ detailStats.pending }}</span>
							<span class="summary-label">Chưa xử lý</span>
						</div>
					</div>
					<div class="summary-card summary-rate">
						<div class="summary-icon">
							<el-icon><TrendCharts /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ detailStats.completionRate }}%</span>
							<span class="summary-label">Tỷ lệ HT</span>
						</div>
					</div>
				</div>

				<!-- Feedback List -->
				<div class="detail-section">
					<h4 class="section-title">Danh sách phản ánh đã xử lý</h4>
					<el-table 
						:data="doctorFeedbacks" 
						max-height="500" 
						v-loading="detailLoading" 
						row-key="id"
						:default-sort="{ prop: 'code', order: 'descending' }"
						stripe
						@row-click="handleFeedbackRowClick"
					>
						<el-table-column prop="code" label="Mã PA" width="160" sortable />
						<el-table-column prop="receivedDate" label="Ngày nhận" width="120" align="center">
							<template #default="{ row }">
								{{ row.receivedDate || row.createdDate }}
							</template>
						</el-table-column>
						<el-table-column prop="content" label="Nội dung" min-width="250">
							<template #default="{ row }">
								<el-tooltip :content="row.content" placement="top" :disabled="!row.content || row.content.length < 60">
									<span class="content-preview">{{ row.content?.substring(0, 60) }}{{ row.content?.length > 60 ? '...' : '' }}</span>
								</el-tooltip>
							</template>
						</el-table-column>
						<el-table-column prop="status" label="Trạng thái" width="120" align="center">
							<template #default="{ row }">
								<el-tag :type="getStatusType(row.status)" size="small">
									{{ getStatusLabel(row.status) }}
								</el-tag>
							</template>
						</el-table-column>
						<el-table-column prop="processingTime" label="Thời gian XL" width="120" align="center">
							<template #default="{ row }">
								<el-tag :type="row.processingTime <= 2 ? 'success' : 'warning'" size="small" v-if="row.processingTime">
									{{ row.processingTime }} ngày
								</el-tag>
								<span v-else class="text-muted">-</span>
							</template>
						</el-table-column>
						<el-table-column prop="completedDate" label="Ngày HT" width="110" align="center" />
					</el-table>
				</div>
			</div>

			<template #footer>
				<el-button @click="detailDialogVisible = false">Đóng</el-button>
				<el-button type="primary" @click="goToFeedbackList">
					Xem tất cả phản ánh
				</el-button>
			</template>
		</el-dialog>

		<!-- Feedback Detail Dialog -->
		<el-dialog
			v-model="feedbackDetailVisible"
			:title="'Chi tiết: ' + (selectedFeedback?.code || '')"
			width="1000px"
			class="feedback-detail-dialog"
		>
			<div v-if="selectedFeedback" class="feedback-detail-content">
				<!-- Process History Section -->
				<div class="process-history-section" v-loading="historyLoading">
					<h4 class="section-title">Lịch sử xử lý</h4>
					<FeedbackTimeline :logs="processHistory" />
				</div>
			</div>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, onMounted, computed, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, User, Timer, TrendCharts, ArrowRight, Document, CircleCheck, Loading, Warning, List, Star } from '@element-plus/icons-vue'
import reportService from '@/services/reportService'
import departmentService from '@/services/departmentService'
import { downloadBlob } from '@/utils/helpers'
import { handleApiError } from '@/utils/errorHandler'
import BarChart from '@/components/charts/BarChart.vue'
import feedbackService from '@/services/feedbackService'
import FeedbackTimeline from '@/components/feedback/FeedbackTimeline.vue'
import { formatDateTime, formatDate } from '@/utils/helpers'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const reportData = ref([])
const departments = ref([])
const filterDepartment = ref('')
const dateRange = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const detailDialogVisible = ref(false)
const selectedDoctor = ref(null)
const doctorFeedbacks = ref([])
const selectedFeedback = ref(null)
const feedbackDetailVisible = ref(false)
const processHistory = ref([])
const historyLoading = ref(false)

const summary = reactive({
	total: 0,
	completed: 0,
	avgTime: 0,
	avgRating: 0
})

const chartData = computed(() => {
	const top10 = [...reportData.value]
		.sort((a, b) => a.avgDays - b.avgDays)
		.slice(0, 10)

	return {
		labels: top10.map(r => r.doctorName),
		datasets: [{
			label: 'Thời gian xử lý TB (ngày)',
			data: top10.map(r => r.avgDays),
			backgroundColor: top10.map(r =>
				r.avgDays <= 1.5 ? 'rgba(103, 194, 58, 0.8)' :
				r.avgDays <= 2.5 ? 'rgba(230, 162, 60, 0.8)' :
				'rgba(245, 108, 108, 0.8)'
			)
		}]
	}
})

// Computed stats from doctorFeedbacks to ensure accuracy
const detailStats = computed(() => {
	const feedbacks = doctorFeedbacks.value
	const total = feedbacks.length
	const completed = feedbacks.filter(f => f.status === 'COMPLETED').length
	const processing = feedbacks.filter(f => f.status === 'PROCESSING' || f.status === 'ASSIGNED').length
	const pending = feedbacks.filter(f => f.status === 'NEW' || f.status === 'PENDING').length
	const completionRate = total > 0 ? Math.round((completed / total) * 100) : 0
	
	return {
		total,
		completed,
		processing,
		pending,
		completionRate
	}
})


const fetchData = async () => {
	loading.value = true
	try {
		// Convert month range to date range for API
		let dateFrom = null
		let dateTo = null
		if (dateRange.value && dateRange.value.length === 2) {
			const startMonth = dateRange.value[0] // YYYY-MM
			const endMonth = dateRange.value[1] // YYYY-MM
			
			if (startMonth) {
				// First day of start month
				dateFrom = startMonth + '-01'
			}
			if (endMonth) {
				// Last day of end month
				const [year, month] = endMonth.split('-')
				const lastDay = new Date(parseInt(year), parseInt(month), 0).getDate()
				dateTo = endMonth + '-' + String(lastDay).padStart(2, '0')
			}
		}
		
		const response = await reportService.getByDoctor({
			departmentId: filterDepartment.value,
			dateFrom,
			dateTo
		})
		// API returns array directly
		const data = Array.isArray(response) ? response : (response.data || [])
		reportData.value = data
		total.value = data.length
		
		// Calculate summary from data
		if (data.length > 0) {
			summary.total = data.reduce((sum, item) => sum + (item.total || 0), 0)
			summary.completed = data.reduce((sum, item) => sum + (item.completed || 0), 0)
			
			// Calculate average time (if avgDays exists in data)
			const totalDays = data.reduce((sum, item) => sum + ((item.avgDays || 0) * (item.total || 0)), 0)
			summary.avgTime = summary.total > 0 ? (totalDays / summary.total).toFixed(1) : 0
			
			// Calculate average rating (weighted by total feedbacks)
			const doctorsWithRating = data.filter(item => item.rating && item.rating > 0)
			if (doctorsWithRating.length > 0) {
				const totalRatingWeighted = doctorsWithRating.reduce((sum, item) => sum + ((item.rating || 0) * (item.total || 0)), 0)
				const totalForRating = doctorsWithRating.reduce((sum, item) => sum + (item.total || 0), 0)
				summary.avgRating = totalForRating > 0 ? (totalRatingWeighted / totalForRating).toFixed(1) : 0
			} else {
				summary.avgRating = 0
			}
		} else {
			// Reset summary if no data
			summary.total = 0
			summary.completed = 0
			summary.avgTime = 0
			summary.avgRating = 0
		}
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			reportData.value = [
				{ id: 1, doctorName: 'BS. Nguyễn Văn A', departmentName: 'Nội khoa', specialty: 'Nội khoa', total: 25, completed: 23, avgDays: 1.2, rating: 4.8 },
				{ id: 2, doctorName: 'BS. Trần Thị B', departmentName: 'Ngoại khoa', specialty: 'Ngoại khoa', total: 20, completed: 18, avgDays: 1.5, rating: 4.5 },
				{ id: 3, doctorName: 'BS. Lê Văn C', departmentName: 'Da liễu', specialty: 'Da liễu', total: 18, completed: 17, avgDays: 1.8, rating: 4.6 },
				{ id: 4, doctorName: 'BS. Phạm Thị D', departmentName: 'Sản khoa', specialty: 'Sản khoa', total: 15, completed: 12, avgDays: 2.5, rating: 4.2 },
				{ id: 5, doctorName: 'BS. Hoàng Văn E', departmentName: 'Nhi khoa', specialty: 'Nhi khoa', total: 22, completed: 21, avgDays: 1.0, rating: 4.9 }
			]
			total.value = 5
		} else {
			handleApiError(error, 'Report By Doctor')
			// Reset summary on error
			summary.total = 0
			summary.completed = 0
			summary.avgTime = 0
			summary.avgRating = 0
		}
	} finally {
		loading.value = false
	}
}

const fetchDepartments = async () => {
	try {
		departments.value = await departmentService.getActiveList()
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			departments.value = [
				{ id: 1, name: 'Nội khoa' },
				{ id: 2, name: 'Ngoại khoa' },
				{ id: 3, name: 'Da liễu' },
				{ id: 4, name: 'Sản khoa' },
				{ id: 5, name: 'Nhi khoa' }
			]
		} else {
			console.error('Failed to load departments:', error)
		}
	}
}

const handleRowClick = async (row) => {
	selectedDoctor.value = row
	detailDialogVisible.value = true
	await fetchDoctorDetail(row.id)
}

const fetchDoctorDetail = async (doctorId) => {
	detailLoading.value = true
	try {
		// Convert month range to date range for API
		let dateFrom = null
		let dateTo = null
		if (dateRange.value && dateRange.value.length === 2 && dateRange.value[0] && dateRange.value[1]) {
			const startMonth = dateRange.value[0] // YYYY-MM
			const endMonth = dateRange.value[1] // YYYY-MM
			
			if (startMonth) {
				dateFrom = startMonth + '-01'
			}
			if (endMonth) {
				const [year, month] = endMonth.split('-')
				const lastDay = new Date(parseInt(year), parseInt(month), 0).getDate()
				dateTo = endMonth + '-' + String(lastDay).padStart(2, '0')
			}
		}
		
		// Use feedbackService to get feedbacks by doctor with date range
		// Always include dateFrom and dateTo if dateRange is set, otherwise don't filter by date
		const params = { doctorId, size: 100 }
		if (dateFrom) {
			params.dateFrom = dateFrom
		}
		if (dateTo) {
			params.dateTo = dateTo
		}
		
		const response = await feedbackService.getList(params)
		
		// Handle both array and object with data property
		const data = Array.isArray(response) ? response : (response.data || [])
		doctorFeedbacks.value = data.map(f => ({
			id: f.id,
			code: f.code,
			content: f.content,
			status: f.status,
			receivedDate: f.receivedDate ? formatDate(f.receivedDate) : (f.createdDate ? formatDate(f.createdDate) : ''),
			createdDate: f.receivedDate ? formatDate(f.receivedDate) : (f.createdDate ? formatDate(f.createdDate) : ''),
			processingTime: f.completedDate && f.receivedDate ? 
				Math.round((new Date(f.completedDate) - new Date(f.receivedDate)) / (1000 * 60 * 60 * 24) * 10) / 10 : null,
			completedDate: f.completedDate ? formatDate(f.completedDate) : null
		}))
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			doctorFeedbacks.value = [
				{ id: 1, code: 'PA-2024-001', content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng', status: 'COMPLETED', processingTime: 1.5, completedDate: '20/11/2024' },
				{ id: 2, code: 'PA-2024-004', content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp', status: 'COMPLETED', processingTime: 0.5, completedDate: '22/11/2024' },
				{ id: 3, code: 'PA-2024-007', content: 'Bác sĩ giải thích rõ ràng về tình trạng bệnh và phác đồ điều trị', status: 'COMPLETED', processingTime: 1.0, completedDate: '24/11/2024' },
				{ id: 4, code: 'PA-2024-010', content: 'Phản hồi nhanh chóng khi bệnh nhân có thắc mắc về thuốc', status: 'COMPLETED', processingTime: 2.0, completedDate: '25/11/2024' },
				{ id: 5, code: 'PA-2024-015', content: 'Cần cải thiện thái độ phục vụ trong giờ cao điểm', status: 'PROCESSING', processingTime: 1.5, completedDate: null }
			]
		} else {
			handleApiError(error, 'Doctor Detail')
			doctorFeedbacks.value = []
		}
	} finally {
		detailLoading.value = false
	}
}

const getSummaries = (param) => {
	const { columns, data } = param
	const sums = []
	
	columns.forEach((column, index) => {
		if (index === 0) {
			// Index column - show "Tổng"
			sums[index] = 'Tổng'
			return
		}
		
		if (column.property === 'avgDays' || column.property === 'rating') {
			// These columns have custom summary templates
			sums[index] = ''
			return
		}
		
		if (column.property === 'doctorName' || column.property === 'specialty') {
			// Text columns - empty
			sums[index] = ''
			return
		}
		
		const values = data.map(item => Number(item[column.property]))
		
		if (!values.every(value => isNaN(value))) {
			// Các cột khác - tính tổng
			sums[index] = values.reduce((prev, curr) => {
				const value = Number(curr)
				if (!isNaN(value)) {
					return prev + curr
				} else {
					return prev
				}
			}, 0)
		} else {
			sums[index] = ''
		}
	})
	
	return sums
}

const getAvgDaysSummary = () => {
	if (reportData.value.length === 0) return '0 ngày'
	
	const values = reportData.value.map(item => Number(item.avgDays)).filter(v => !isNaN(v))
	if (values.length === 0) return '0 ngày'
	
	const avg = values.reduce((prev, curr) => prev + curr, 0) / values.length
	return avg > 0 ? avg.toFixed(1) + ' ngày' : '0 ngày'
}

const getRatingSummary = () => {
	if (reportData.value.length === 0) return '0.0'
	
	const values = reportData.value.map(item => Number(item.rating)).filter(v => !isNaN(v) && v > 0)
	if (values.length === 0) return '0.0'
	
	const avg = values.reduce((prev, curr) => prev + curr, 0) / values.length
	return avg > 0 ? avg.toFixed(1) : '0.0'
}

const getStatusType = (status) => {
	const types = {
		'PENDING': 'danger',
		'PROCESSING': 'warning',
		'COMPLETED': 'success'
	}
	return types[status] || 'info'
}

const getStatusLabel = (status) => {
	const labels = {
		'PENDING': 'Chưa xử lý',
		'PROCESSING': 'Đang xử lý',
		'COMPLETED': 'Hoàn thành'
	}
	return labels[status] || status
}

const mapStatusToAction = (status) => {
	const statusMap = {
		'NEW': 'CREATE',
		'ASSIGNED': 'ASSIGN',
		'PROCESSING': 'PROCESS',
		'COMPLETED': 'COMPLETE',
		'PENDING': 'PENDING'
	}
	return statusMap[status] || 'UPDATE'
}

const handleFeedbackRowClick = async (row) => {
	selectedFeedback.value = row
	feedbackDetailVisible.value = true
	await fetchProcessHistory(row.id)
}

const fetchProcessHistory = async (feedbackId) => {
	historyLoading.value = true
	try {
		const response = await feedbackService.getProcessHistory(feedbackId)
		const data = Array.isArray(response) ? response : (response.data || [])
		
		processHistory.value = data.map(item => ({
			action: mapStatusToAction(item.status),
			actor: item.createdByName || 'Hệ thống',
			time: item.createdAt ? formatDateTime(item.createdAt) : '',
			note: item.content || item.note || '',
			attachments: item.images || []
		}))
	} catch (error) {
		handleApiError(error, 'Process History')
		processHistory.value = []
	} finally {
		historyLoading.value = false
	}
}

const goToFeedbackList = () => {
	detailDialogVisible.value = false
	const query = {}
	if (selectedDoctor.value?.id) {
		query.doctorId = selectedDoctor.value.id
	}
	// Preserve date range if exists
	if (dateRange.value && dateRange.value.length === 2) {
		const startMonth = dateRange.value[0]
		const endMonth = dateRange.value[1]
		if (startMonth) {
			query.dateFrom = startMonth + '-01'
		}
		if (endMonth) {
			const [year, month] = endMonth.split('-')
			const lastDay = new Date(parseInt(year), parseInt(month), 0).getDate()
			query.dateTo = endMonth + '-' + String(lastDay).padStart(2, '0')
		}
	}
	router.push({
		path: '/feedback',
		query
	})
}

const exportExcel = async () => {
	try {
		// Convert month range to date range for API
		let dateFrom = null
		let dateTo = null
		if (dateRange.value && dateRange.value.length === 2) {
			const startMonth = dateRange.value[0] // YYYY-MM
			const endMonth = dateRange.value[1] // YYYY-MM
			
			if (startMonth) {
				dateFrom = startMonth + '-01'
			}
			if (endMonth) {
				const [year, month] = endMonth.split('-')
				const lastDay = new Date(parseInt(year), parseInt(month), 0).getDate()
				dateTo = endMonth + '-' + String(lastDay).padStart(2, '0')
			}
		}
		
		const blob = await reportService.exportExcel({
			type: 'by-doctor',
			departmentId: filterDepartment.value,
			dateFrom,
			dateTo
		})
		downloadBlob(blob, `bao-cao-theo-bac-si-${Date.now()}.xlsx`)
		ElMessage.success('Xuất Excel thành công!')
	} catch (error) {
		handleApiError(error, 'Export Excel')
	}
}

onMounted(() => {
	fetchData()
	fetchDepartments()
})

// Watch dateRange to reload doctor detail if dialog is open
watch(dateRange, () => {
	if (detailDialogVisible.value && selectedDoctor.value?.id) {
		fetchDoctorDetail(selectedDoctor.value.id)
	}
})
</script>

<style scoped>
.content-card-title {
	display: flex;
	align-items: center;
	gap: 8px;
}

.content-card-title .el-icon {
	color: var(--primary-color);
}

.header-actions {
	display: flex;
	gap: 12px;
	flex-wrap: wrap;
}

/* Doctor Info in Table */
.doctor-info {
	display: flex;
	align-items: center;
	gap: 12px;
}

.doctor-avatar {
	background: linear-gradient(135deg, var(--primary-color) 0%, var(--accent-color) 100%);
	color: white;
	font-weight: 600;
	font-size: 16px;
}

.doctor-details {
	display: flex;
	flex-direction: column;
}

.doctor-name {
	font-weight: 600;
	color: var(--text-primary);
}

.doctor-dept {
	font-size: 12px;
	color: var(--text-secondary);
}

.rating-cell {
	display: flex;
	justify-content: center;
}

.text-success {
	color: var(--success-color);
}

.font-bold {
	font-weight: 600;
}

/* Table Summary Row - Bold */
:deep(.el-table__footer-wrapper) {
	background-color: #f5f7fa;
}

:deep(.el-table__footer-wrapper .el-table__cell) {
	font-weight: 700;
	color: var(--secondary-color);
	background-color: #f5f7fa !important;
}

:deep(.el-table__footer-wrapper .el-table__cell .cell) {
	font-weight: 700;
	color: var(--secondary-color);
	white-space: nowrap;
}

.summary-text {
	font-weight: 700;
	color: var(--secondary-color);
	white-space: nowrap;
}

/* Clickable Row */
:deep(.clickable-row) {
	cursor: pointer;
	transition: background-color 0.2s;
}

:deep(.clickable-row:hover) {
	background-color: rgba(13, 110, 253, 0.05) !important;
}

.view-icon {
	color: var(--text-secondary);
	transition: color 0.2s, transform 0.2s;
}

:deep(.clickable-row:hover) .view-icon {
	color: var(--primary-color);
	transform: translateX(4px);
}

.chart-container {
	height: 350px;
}

.pagination-wrapper {
	margin-top: 20px;
	display: flex;
	justify-content: flex-end;
}

/* Detail Dialog */
.detail-content {
	padding: 0 20px;
}

/* Doctor Profile */
.doctor-profile {
	display: flex;
	align-items: center;
	gap: 24px;
	padding: 24px;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	border-radius: var(--radius-lg);
	margin-bottom: 24px;
}

.profile-avatar {
	background: rgba(255, 255, 255, 0.2);
	color: white;
	font-size: 28px;
	font-weight: 600;
	border: 3px solid rgba(255, 255, 255, 0.3);
}

.profile-info {
	color: white;
}

.profile-name {
	margin: 0 0 8px;
	font-size: 22px;
	font-weight: 700;
}

.profile-specialty {
	display: flex;
	align-items: center;
	gap: 12px;
	margin: 0 0 12px;
}

.profile-specialty .el-tag {
	background: rgba(255, 255, 255, 0.2);
	border: none;
	color: white;
}

.profile-dept {
	opacity: 0.9;
}

.profile-rating :deep(.el-rate__icon) {
	color: rgba(255, 255, 255, 0.3);
}

.profile-rating :deep(.el-rate__icon.is-active) {
	color: #ffd700 !important;
}

/* Stats Grid */
.stats-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16px;
	margin-bottom: 24px;
}

.stat-card {
	display: flex;
	align-items: center;
	gap: 12px;
	padding: 16px;
	border-radius: var(--radius-lg);
	background: white;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
	border-left: 4px solid;
	transition: transform 0.2s, box-shadow 0.2s;
	min-width: 0;
	flex: 1;
}

.stat-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.stat-total {
	border-left-color: #667eea;
}

.stat-completed {
	border-left-color: #67C23A;
}

.stat-time {
	border-left-color: #409EFF;
}

.stat-rating {
	border-left-color: #ff9900;
}

.stat-icon {
	width: 48px;
	height: 48px;
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 20px;
	flex-shrink: 0;
}

.stat-total .stat-icon {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
}

.stat-completed .stat-icon {
	background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
	color: white;
}

.stat-time .stat-icon {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
	color: white;
}

.stat-rating .stat-icon {
	background: linear-gradient(135deg, #ff9900 0%, #ffb84d 100%);
	color: white;
}

.stat-info {
	display: flex;
	flex-direction: column;
	flex: 1;
	min-width: 0;
}

.stat-value {
	font-size: 24px;
	font-weight: 700;
	color: var(--secondary-color);
	line-height: 1.2;
}

.stat-value small {
	font-size: 14px;
	font-weight: 500;
}

.stat-label {
	font-size: 12px;
	white-space: nowrap;
	overflow: hidden;
	text-overflow: ellipsis;
	color: var(--text-secondary);
	margin-top: 4px;
	font-weight: 500;
}

.table-wrapper {
	margin-top: 0;
}

/* Detail Summary */
.detail-summary {
	display: grid;
	grid-template-columns: repeat(5, 1fr);
	gap: 16px;
	margin-bottom: 24px;
}

.summary-card {
	display: flex;
	align-items: center;
	gap: 12px;
	padding: 16px;
	border-radius: var(--radius-lg);
	background: white;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
	border-left: 4px solid;
	transition: transform 0.2s, box-shadow 0.2s;
}

.summary-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.summary-total {
	border-left-color: #667eea;
}

.summary-completed {
	border-left-color: #67C23A;
}

.summary-processing {
	border-left-color: #E6A23C;
}

.summary-pending {
	border-left-color: #F56C6C;
}

.summary-rate {
	border-left-color: #4facfe;
}

.summary-icon {
	width: 48px;
	height: 48px;
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 20px;
	flex-shrink: 0;
}

.summary-total .summary-icon {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
}

.summary-completed .summary-icon {
	background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
	color: white;
}

.summary-processing .summary-icon {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	color: white;
}

.summary-pending .summary-icon {
	background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
	color: white;
}

.summary-rate .summary-icon {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
	color: white;
}

.summary-info {
	display: flex;
	flex-direction: column;
	flex: 1;
	min-width: 0;
}

.summary-label {
	font-size: 12px;
	color: var(--text-secondary);
	margin-top: 4px;
	font-weight: 500;
}

.summary-value {
	font-size: 24px;
	font-weight: 700;
	color: var(--secondary-color);
	line-height: 1.2;
}

/* Detail Section */
.detail-section {
	margin-top: 24px;
}

.section-title {
	font-size: 16px;
	font-weight: 600;
	color: var(--secondary-color);
	margin-bottom: 16px;
	padding-bottom: 8px;
	border-bottom: 2px solid var(--primary-color);
}

.content-preview {
	color: var(--text-primary);
}

/* Responsive */
@media (max-width: 1200px) {
	.stats-grid {
		grid-template-columns: repeat(2, 1fr);
		gap: 12px;
	}

	.detail-summary {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 768px) {
	.header-actions {
		flex-direction: column;
		width: 100%;
	}

	.doctor-profile {
		flex-direction: column;
		text-align: center;
	}

	.profile-specialty {
		justify-content: center;
	}

	.stats-grid {
		grid-template-columns: 1fr;
	}

	.stat-card {
		padding: 12px;
	}

	.stat-icon {
		width: 40px;
		height: 40px;
		font-size: 18px;
	}

	.stat-value {
		font-size: 20px;
	}

	.charts-row {
		grid-template-columns: 1fr;
	}

	.chart-container {
		height: 350px;
	}

	.detail-summary {
		grid-template-columns: 1fr 1fr;
	}

	.summary-card {
		padding: 12px;
	}

	.summary-icon {
		width: 40px;
		height: 40px;
		font-size: 18px;
	}

	.summary-value {
		font-size: 20px;
	}
}

@media (max-width: 480px) {
	.detail-stats {
		grid-template-columns: 1fr;
	}
}
</style>

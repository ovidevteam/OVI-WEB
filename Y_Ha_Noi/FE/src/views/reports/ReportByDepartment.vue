<template>
	<div class="report-by-department">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">
					<el-icon><OfficeBuilding /></el-icon>
					Báo cáo theo Phòng ban
				</h3>
				<div class="header-actions">
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
				<div class="stat-card stat-processing">
					<div class="stat-icon">
						<el-icon><Loading /></el-icon>
					</div>
					<div class="stat-info">
						<span class="stat-value">{{ summary.processing }}</span>
						<span class="stat-label">Đang xử lý</span>
					</div>
				</div>
				<div class="stat-card stat-pending">
					<div class="stat-icon">
						<el-icon><Warning /></el-icon>
					</div>
					<div class="stat-info">
						<span class="stat-value">{{ summary.pending }}</span>
						<span class="stat-label">Chưa xử lý</span>
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
			</div>

			<!-- Table với hover effect -->
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
					<el-table-column prop="departmentName" label="Phòng ban" min-width="180">
						<template #default="{ row }">
							<div class="dept-name">
								<el-icon class="dept-icon"><OfficeBuilding /></el-icon>
								{{ row.departmentName }}
							</div>
						</template>
					</el-table-column>
					<el-table-column prop="total" label="Tổng PA" width="100" align="center" sortable />
					<el-table-column prop="pending" label="Chưa xử lý" width="110" align="center">
						<template #default="{ row }">
							<el-tag type="danger" size="small" v-if="row.pending > 0">{{ row.pending }}</el-tag>
							<span v-else>0</span>
						</template>
					</el-table-column>
					<el-table-column prop="processing" label="Đang xử lý" width="110" align="center">
						<template #default="{ row }">
							<el-tag type="warning" size="small" v-if="row.processing > 0">{{ row.processing }}</el-tag>
							<span v-else>0</span>
						</template>
					</el-table-column>
					<el-table-column prop="completed" label="Hoàn thành" width="110" align="center">
						<template #default="{ row }">
							<el-tag type="success" size="small">{{ row.completed }}</el-tag>
						</template>
					</el-table-column>
					<el-table-column prop="overdue" label="Quá hạn" width="100" align="center">
						<template #default="{ row }">
							<el-tag type="danger" effect="dark" size="small" v-if="row.overdue > 0">{{ row.overdue }}</el-tag>
							<span v-else class="text-muted">0</span>
						</template>
					</el-table-column>
					<el-table-column prop="avgDays" label="Thời gian TB" width="120" align="center">
						<template #default="{ row }">
							<el-tag
								:type="row.avgDays <= 2 ? 'success' : row.avgDays <= 3 ? 'warning' : 'danger'"
								size="small"
							>
								{{ row.avgDays }} ngày
							</el-tag>
						</template>
					</el-table-column>
					<el-table-column prop="completionRate" label="Tỷ lệ HT" width="140" align="center" sortable>
						<template #default="{ row }">
							<el-progress
								:percentage="row.completionRate"
								:stroke-width="10"
								:color="getProgressColor(row.completionRate)"
							>
								<span class="progress-text">{{ row.completionRate }}%</span>
							</el-progress>
						</template>
					</el-table-column>
					<el-table-column label="" width="60" align="center">
						<template #default>
							<el-icon class="view-icon"><ArrowRight /></el-icon>
						</template>
					</el-table-column>
				</el-table>
			</div>
		</div>

		<!-- Chart -->
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">
					<el-icon><TrendCharts /></el-icon>
					Biểu đồ so sánh
				</h3>
			</div>
			<!-- Charts -->
			<div class="charts-row">
				<div class="chart-container">
					<h4 class="chart-title">Phân bổ theo Phòng ban</h4>
					<BarChart :data="chartData" :stacked="true" :show-legend="true" index-axis="x" />
				</div>
				<div class="chart-container">
					<h4 class="chart-title">Phân bổ theo Phòng ban (Tổng phản ánh)</h4>
					<PieChart :data="pieChartData" />
				</div>
			</div>
		</div>

		<!-- Detail Dialog -->
		<el-dialog
			v-model="detailDialogVisible"
			:title="'Chi tiết: ' + selectedDepartment?.departmentName"
			width="1400px"
			class="detail-dialog"
			:close-on-click-modal="false"
		>
			<div class="detail-content" v-if="selectedDepartment">
				<!-- Department Summary -->
				<div class="detail-summary">
					<div class="summary-card summary-total">
						<div class="summary-icon">
							<el-icon><Document /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ selectedDepartment.total }}</span>
							<span class="summary-label">Tổng phản ánh</span>
						</div>
					</div>
					<div class="summary-card summary-completed">
						<div class="summary-icon">
							<el-icon><CircleCheck /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ selectedDepartment.completed }}</span>
							<span class="summary-label">Hoàn thành</span>
						</div>
					</div>
					<div class="summary-card summary-processing">
						<div class="summary-icon">
							<el-icon><Loading /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ selectedDepartment.processing }}</span>
							<span class="summary-label">Đang xử lý</span>
						</div>
					</div>
					<div class="summary-card summary-pending">
						<div class="summary-icon">
							<el-icon><Warning /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ selectedDepartment.pending }}</span>
							<span class="summary-label">Chưa xử lý</span>
						</div>
					</div>
					<div class="summary-card summary-rate">
						<div class="summary-icon">
							<el-icon><TrendCharts /></el-icon>
						</div>
						<div class="summary-info">
							<span class="summary-value">{{ selectedDepartment.completionRate }}%</span>
							<span class="summary-label">Tỷ lệ HT</span>
						</div>
					</div>
				</div>

				<!-- Status Tabs -->
				<el-tabs v-model="activeTab" class="detail-tabs">
					<el-tab-pane name="all">
						<template #label>
							<span>Tất cả</span>
							<el-badge v-if="detailFeedbacks.length > 0" :value="detailFeedbacks.length" class="tab-badge" />
						</template>
					</el-tab-pane>
					<el-tab-pane name="pending">
						<template #label>
							<span>Chưa xử lý</span>
							<el-badge v-if="pendingFeedbacks.length > 0" :value="pendingFeedbacks.length" class="tab-badge" type="danger" />
						</template>
					</el-tab-pane>
					<el-tab-pane name="processing">
						<template #label>
							<span>Đang xử lý</span>
							<el-badge v-if="processingFeedbacks.length > 0" :value="processingFeedbacks.length" class="tab-badge" type="warning" />
						</template>
					</el-tab-pane>
					<el-tab-pane name="completed">
						<template #label>
							<span>Hoàn thành</span>
							<el-badge v-if="completedFeedbacks.length > 0" :value="completedFeedbacks.length" class="tab-badge" type="success" />
						</template>
					</el-tab-pane>
				</el-tabs>

				<!-- Feedback List -->
				<el-table 
					:data="filteredFeedbacks" 
					max-height="500" 
					v-loading="detailLoading" 
					row-key="id"
					:default-sort="{ prop: 'code', order: 'descending' }"
					stripe
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
					<el-table-column prop="departmentName" label="Phòng ban" width="150" />
					<el-table-column prop="doctorName" label="Bác sĩ" width="150">
						<template #default="{ row }">
							{{ row.doctorName || 'Không có' }}
						</template>
					</el-table-column>
					<el-table-column prop="level" label="Mức độ" width="110" align="center">
						<template #default="{ row }">
							<el-tag :type="getLevelType(row.level)" size="small" effect="plain">
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
					<el-table-column prop="handlerName" label="Người xử lý" width="150">
						<template #default="{ row }">
							{{ row.handlerName || 'Chưa phân công' }}
						</template>
					</el-table-column>
				</el-table>
			</div>

			<template #footer>
				<el-button @click="detailDialogVisible = false">Đóng</el-button>
				<el-button type="primary" @click="goToFeedbackList">Xem tất cả phản ánh</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
	Download, OfficeBuilding, Document, CircleCheck, Loading, Timer,
	TrendCharts, ArrowRight, Warning
} from '@element-plus/icons-vue'
import reportService from '@/services/reportService'
import feedbackService from '@/services/feedbackService'
import { downloadBlob } from '@/utils/helpers'
import { handleApiError } from '@/utils/errorHandler'
import BarChart from '@/components/charts/BarChart.vue'
import PieChart from '@/components/charts/PieChart.vue'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const loading = ref(false)
const detailLoading = ref(false)
const reportData = ref([])
const dateRange = ref([])
const detailDialogVisible = ref(false)
const selectedDepartment = ref(null)
const detailFeedbacks = ref([])
const activeTab = ref('all')

const summary = reactive({
	total: 0,
	completed: 0,
	processing: 0,
	avgTime: 0
})

const chartData = computed(() => ({
	labels: reportData.value.map(r => r.departmentName),
	datasets: [
		{
			label: 'Hoàn thành',
			data: reportData.value.map(r => r.completed || 0),
			backgroundColor: 'rgba(103, 194, 58, 0.8)',  // Green
			borderColor: 'rgba(103, 194, 58, 1)',
			borderWidth: 1
		},
		{
			label: 'Đang xử lý',
			data: reportData.value.map(r => r.processing || 0),
			backgroundColor: 'rgba(230, 162, 60, 0.8)',  // Orange
			borderColor: 'rgba(230, 162, 60, 1)',
			borderWidth: 1
		},
		{
			label: 'Chưa xử lý',
			data: reportData.value.map(r => r.pending || 0),
			backgroundColor: 'rgba(245, 108, 108, 0.8)',  // Red
			borderColor: 'rgba(245, 108, 108, 1)',
			borderWidth: 1
		}
	]
}))

const pieChartData = computed(() => ({
	labels: reportData.value.map(r => r.departmentName),
	datasets: [{
		label: 'Tổng phản ánh',
		data: reportData.value.map(r => r.total || 0),
		backgroundColor: [
			'rgba(102, 126, 234, 0.8)',  // Purple
			'rgba(103, 194, 58, 0.8)',   // Green
			'rgba(230, 162, 60, 0.8)',   // Orange
			'rgba(245, 108, 108, 0.8)',  // Red
			'rgba(64, 158, 255, 0.8)',    // Blue
			'rgba(144, 147, 153, 0.8)',   // Gray
			'rgba(255, 193, 7, 0.8)',     // Yellow
			'rgba(156, 39, 176, 0.8)',    // Deep Purple
			'rgba(0, 188, 212, 0.8)',     // Cyan
			'rgba(255, 87, 34, 0.8)'      // Deep Orange
		],
		borderColor: [
			'rgba(102, 126, 234, 1)',
			'rgba(103, 194, 58, 1)',
			'rgba(230, 162, 60, 1)',
			'rgba(245, 108, 108, 1)',
			'rgba(64, 158, 255, 1)',
			'rgba(144, 147, 153, 1)',
			'rgba(255, 193, 7, 1)',
			'rgba(156, 39, 176, 1)',
			'rgba(0, 188, 212, 1)',
			'rgba(255, 87, 34, 1)'
		],
		borderWidth: 2
	}]
}))

// Filtered feedbacks based on active tab
const pendingFeedbacks = computed(() =>
	detailFeedbacks.value.filter(f => f.status === 'NEW' || f.status === 'PENDING')
)
const processingFeedbacks = computed(() =>
	detailFeedbacks.value.filter(f => f.status === 'PROCESSING' || f.status === 'ASSIGNED')
)
const completedFeedbacks = computed(() =>
	detailFeedbacks.value.filter(f => f.status === 'COMPLETED')
)
const filteredFeedbacks = computed(() => {
	switch (activeTab.value) {
		case 'pending': return pendingFeedbacks.value
		case 'processing': return processingFeedbacks.value
		case 'completed': return completedFeedbacks.value
		default: return detailFeedbacks.value
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
		
		// API returns array directly (not wrapped in data property)
		const response = await reportService.getByDepartment({
			dateFrom,
			dateTo
		})
		
		// Handle both array response and object with data property
		const data = Array.isArray(response) ? response : (response.data || [])
		reportData.value = data
		
		// Calculate summary from data
		if (data.length > 0) {
			summary.total = data.reduce((sum, item) => sum + (item.total || 0), 0)
			summary.completed = data.reduce((sum, item) => sum + (item.completed || 0), 0)
			summary.processing = data.reduce((sum, item) => sum + (item.processing || 0), 0)
			summary.pending = data.reduce((sum, item) => sum + (item.pending || 0), 0)
			
			// Calculate average time (if avgDays exists in data)
			const totalDays = data.reduce((sum, item) => sum + ((item.avgDays || 0) * (item.total || 0)), 0)
			summary.avgTime = summary.total > 0 ? (totalDays / summary.total).toFixed(1) : 0
		} else {
			// Reset summary if no data
			summary.total = 0
			summary.completed = 0
			summary.processing = 0
			summary.pending = 0
			summary.avgTime = 0
		}
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			reportData.value = [
				{ id: 1, departmentName: 'Nội khoa', total: 45, pending: 5, processing: 8, completed: 32, overdue: 2, avgDays: 2.3, completionRate: 71 },
				{ id: 2, departmentName: 'Ngoại khoa', total: 38, pending: 3, processing: 5, completed: 30, overdue: 1, avgDays: 1.8, completionRate: 79 },
				{ id: 3, departmentName: 'Da liễu', total: 25, pending: 2, processing: 3, completed: 20, overdue: 0, avgDays: 1.5, completionRate: 80 },
				{ id: 4, departmentName: 'Sản khoa', total: 30, pending: 4, processing: 6, completed: 20, overdue: 3, avgDays: 2.8, completionRate: 67 },
				{ id: 5, departmentName: 'Nhi khoa', total: 22, pending: 1, processing: 2, completed: 19, overdue: 0, avgDays: 1.2, completionRate: 86 }
			]
			summary.total = 160
			summary.completed = 121
			summary.processing = 24
			summary.pending = 15
			summary.avgTime = 2.1
		} else {
			handleApiError(error, 'Report By Department')
		}
	} finally {
		loading.value = false
	}
}

const handleRowClick = async (row) => {
	selectedDepartment.value = row
	activeTab.value = 'all'
	detailDialogVisible.value = true
	await fetchDepartmentDetail(row.departmentId || row.id)
}

const fetchDepartmentDetail = async (departmentId) => {
	detailLoading.value = true
	try {
		// Convert month range to date range for API
		let dateFrom = null
		let dateTo = null
		if (dateRange.value && dateRange.value.length === 2) {
			// dateRange is in format YYYY-MM, convert to first and last day of month
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
		
		// Use feedbackService to get feedbacks by department with date range
		const response = await feedbackService.getList({ 
			departmentId, 
			dateFrom,
			dateTo,
			size: 100 
		})
		
		// Handle both array and object with data property
		const data = Array.isArray(response) ? response : (response.data || [])
		detailFeedbacks.value = data.map(f => ({
			id: f.id,
			code: f.code,
			content: f.content,
			status: f.status,
			level: f.level,
			receivedDate: f.receivedDate ? new Date(f.receivedDate).toLocaleDateString('vi-VN') : '',
			createdDate: f.receivedDate ? new Date(f.receivedDate).toLocaleDateString('vi-VN') : '',
			departmentName: f.departmentName || selectedDepartment.value?.departmentName || '',
			doctorName: f.doctorName || null,
			handlerName: f.handlerName || null
		}))
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			detailFeedbacks.value = [
				{ id: 1, code: 'PA-2024-001', content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng', status: 'COMPLETED', createdDate: '15/11/2024', handlerName: 'BS. Nguyễn Văn A' },
				{ id: 2, code: 'PA-2024-002', content: 'Nhân viên lễ tân thiếu thân thiện với bệnh nhân cao tuổi', status: 'PROCESSING', createdDate: '16/11/2024', handlerName: 'Trần Thị B' },
				{ id: 3, code: 'PA-2024-003', content: 'Phòng khám thiếu sạch sẽ, cần cải thiện vệ sinh', status: 'NEW', createdDate: '17/11/2024', handlerName: null },
				{ id: 4, code: 'PA-2024-004', content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp', status: 'COMPLETED', createdDate: '18/11/2024', handlerName: 'BS. Lê Văn C' },
				{ id: 5, code: 'PA-2024-005', content: 'Cơ sở vật chất xuống cấp, máy lạnh không hoạt động', status: 'PROCESSING', createdDate: '19/11/2024', handlerName: 'Phạm Văn D' }
			]
		} else {
			handleApiError(error, 'Department Detail')
			detailFeedbacks.value = []
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
			// Phòng ban column - show "Tổng"
			sums[index] = 'Tổng'
			return
		}
		
		if (column.property === 'avgDays' || column.property === 'completionRate') {
			// These columns have custom summary templates
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

const getCompletionRateSummary = () => {
	if (reportData.value.length === 0) return 0
	
	const values = reportData.value.map(item => Number(item.completionRate)).filter(v => !isNaN(v))
	if (values.length === 0) return 0
	
	const avg = values.reduce((prev, curr) => prev + curr, 0) / values.length
	return Math.round(avg)
}

const getProgressColor = (percentage) => {
	if (percentage >= 80) return '#67C23A'
	if (percentage >= 60) return '#E6A23C'
	return '#F56C6C'
}

const getStatusType = (status) => {
	const types = {
		'NEW': 'primary',
		'PENDING': 'danger',
		'ASSIGNED': 'info',
		'PROCESSING': 'warning',
		'COMPLETED': 'success'
	}
	return types[status] || 'info'
}

const getStatusLabel = (status) => {
	const labels = {
		'NEW': 'Tiếp nhận',
		'PENDING': 'Chưa xử lý',
		'ASSIGNED': 'Đã phân công',
		'PROCESSING': 'Đang xử lý',
		'COMPLETED': 'Hoàn thành'
	}
	return labels[status] || status
}

const getLevelLabel = (level) => {
	const labels = {
		'LOW': 'Thấp',
		'MEDIUM': 'Trung bình',
		'HIGH': 'Cao',
		'URGENT': 'Khẩn cấp'
	}
	return labels[level] || level
}

const getLevelType = (level) => {
	const types = {
		'LOW': 'info',
		'MEDIUM': 'warning',
		'HIGH': 'danger',
		'URGENT': 'danger'
	}
	return types[level] || 'info'
}

const goToFeedbackList = () => {
	detailDialogVisible.value = false
	const query = {}
	
	// Set departmentId
	if (selectedDepartment.value?.id || selectedDepartment.value?.departmentId) {
		query.departmentId = selectedDepartment.value?.id || selectedDepartment.value?.departmentId
	}
	
	// Map activeTab to status query param
	if (activeTab.value !== 'all') {
		const statusMap = {
			'pending': 'NEW',
			'processing': 'PROCESSING',
			'completed': 'COMPLETED'
		}
		if (statusMap[activeTab.value]) {
			query.status = statusMap[activeTab.value]
		}
	}
	
	// Convert month range to date range for query params
	if (dateRange.value && dateRange.value.length === 2) {
		const startMonth = dateRange.value[0] // YYYY-MM
		const endMonth = dateRange.value[1] // YYYY-MM
		
		if (startMonth) {
			// First day of start month
			query.dateFrom = startMonth + '-01'
		}
		if (endMonth) {
			// Last day of end month
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
			type: 'by-department',
			dateFrom,
			dateTo
		})
		downloadBlob(blob, `bao-cao-theo-phong-${Date.now()}.xlsx`)
		ElMessage.success('Xuất Excel thành công!')
	} catch (error) {
		handleApiError(error, 'Export Excel')
	}
}

onMounted(() => {
	fetchData()
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

/* Stats Grid */
.stats-grid {
	display: grid;
	grid-template-columns: repeat(5, 1fr);
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

.stat-processing {
	border-left-color: #E6A23C;
}

.stat-pending {
	border-left-color: #F56C6C;
}

.stat-time {
	border-left-color: #409EFF;
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

.stat-processing .stat-icon {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	color: white;
}

.stat-pending .stat-icon {
	background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
	color: white;
}

.stat-time .stat-icon {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
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
	color: var(--text-secondary);
	margin-top: 4px;
	font-weight: 500;
}

/* Table */
.table-wrapper {
	margin-top: 16px;
}

.dept-name {
	display: flex;
	align-items: center;
	gap: 8px;
	font-weight: 500;
}

.dept-icon {
	color: var(--primary-color);
}

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

.progress-text {
	font-size: 12px;
	font-weight: 600;
}

.text-muted {
	color: var(--text-secondary);
}

.text-success { color: var(--success-color); }
.text-warning { color: #e6a23c; }
.text-danger { color: var(--danger-color); }

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
}

.summary-text {
	font-weight: 700;
	color: var(--secondary-color);
}

/* Charts */
.charts-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 20px;
}

.chart-container {
	background: white;
	border-radius: var(--radius-lg);
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
	height: 400px;
	display: flex;
	flex-direction: column;
}

.chart-title {
	font-size: 16px;
	font-weight: 600;
	color: var(--secondary-color);
	margin-bottom: 16px;
	text-align: center;
}

.chart-container :deep(canvas) {
	flex: 1;
}

/* Detail Dialog */
.detail-content {
	padding: 0 20px;
}

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
	border-left-color: #409EFF;
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

.detail-tabs {
	margin-bottom: 16px;
}

.detail-tabs :deep(.el-tabs__item) {
	position: relative;
}

.tab-badge {
	margin-left: 8px;
}

.content-preview {
	color: var(--text-primary);
}

/* Responsive */
@media (max-width: 1200px) {
	.stats-grid {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 768px) {
	.header-actions {
		flex-direction: column;
		width: 100%;
	}

	.stats-grid {
		grid-template-columns: 1fr;
	}

	.stat-card {
		padding: 16px;
	}

	.stat-icon {
		width: 48px;
		height: 48px;
		font-size: 20px;
	}

	.stat-value {
		font-size: 24px;
	}

	.charts-row {
		grid-template-columns: 1fr;
	}

	.chart-container {
		height: 350px;
	}

	.detail-summary {
		grid-template-columns: repeat(2, 1fr);
		gap: 12px;
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
</style>

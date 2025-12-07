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
						<span class="stat-label">Đã hoàn thành</span>
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
			<div class="chart-container">
				<BarChart :data="chartData" />
			</div>
		</div>

		<!-- Detail Dialog -->
		<el-dialog
			v-model="detailDialogVisible"
			:title="'Chi tiết: ' + selectedDepartment?.departmentName"
			width="900px"
			class="detail-dialog"
		>
			<div class="detail-content" v-if="selectedDepartment">
				<!-- Department Summary -->
				<div class="detail-summary">
					<div class="summary-item">
						<span class="summary-label">Tổng phản ánh</span>
						<span class="summary-value">{{ selectedDepartment.total }}</span>
					</div>
					<div class="summary-item">
						<span class="summary-label">Hoàn thành</span>
						<span class="summary-value text-success">{{ selectedDepartment.completed }}</span>
					</div>
					<div class="summary-item">
						<span class="summary-label">Đang xử lý</span>
						<span class="summary-value text-warning">{{ selectedDepartment.processing }}</span>
					</div>
					<div class="summary-item">
						<span class="summary-label">Chưa xử lý</span>
						<span class="summary-value text-danger">{{ selectedDepartment.pending }}</span>
					</div>
					<div class="summary-item">
						<span class="summary-label">Tỷ lệ HT</span>
						<span class="summary-value">{{ selectedDepartment.completionRate }}%</span>
					</div>
				</div>

				<!-- Status Tabs -->
				<el-tabs v-model="activeTab" class="detail-tabs">
					<el-tab-pane label="Tất cả" name="all">
						<el-badge :value="detailFeedbacks.length" :max="99" class="tab-badge" />
					</el-tab-pane>
					<el-tab-pane label="Chưa xử lý" name="pending">
						<el-badge :value="pendingFeedbacks.length" :max="99" type="danger" class="tab-badge" />
					</el-tab-pane>
					<el-tab-pane label="Đang xử lý" name="processing">
						<el-badge :value="processingFeedbacks.length" :max="99" type="warning" class="tab-badge" />
					</el-tab-pane>
					<el-tab-pane label="Hoàn thành" name="completed">
						<el-badge :value="completedFeedbacks.length" :max="99" type="success" class="tab-badge" />
					</el-tab-pane>
				</el-tabs>

				<!-- Feedback List -->
				<el-table :data="filteredFeedbacks" max-height="400" v-loading="detailLoading" row-key="id">
					<el-table-column prop="code" label="Mã PA" width="120" />
					<el-table-column prop="content" label="Nội dung" min-width="200">
						<template #default="{ row }">
							<el-tooltip :content="row.content" placement="top" :disabled="row.content?.length < 50">
								<span class="content-preview">{{ row.content?.substring(0, 50) }}{{ row.content?.length > 50 ? '...' : '' }}</span>
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
					<el-table-column prop="createdDate" label="Ngày tạo" width="120" align="center" />
					<el-table-column prop="handlerName" label="Người xử lý" width="150" />
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
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
	Download, OfficeBuilding, Document, CircleCheck, Loading, Timer,
	TrendCharts, ArrowRight
} from '@element-plus/icons-vue'
import reportService from '@/services/reportService'
import feedbackService from '@/services/feedbackService'
import { downloadBlob } from '@/utils/helpers'
import { handleApiError } from '@/utils/errorHandler'
import BarChart from '@/components/charts/BarChart.vue'

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
	datasets: [{
		label: 'Số phản ánh',
		data: reportData.value.map(r => r.total),
		backgroundColor: 'rgba(13, 110, 253, 0.8)'
	}]
}))

// Filtered feedbacks based on active tab
const pendingFeedbacks = computed(() =>
	detailFeedbacks.value.filter(f => f.status === 'PENDING')
)
const processingFeedbacks = computed(() =>
	detailFeedbacks.value.filter(f => f.status === 'PROCESSING')
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
		// API returns array directly (not wrapped in data property)
		const response = await reportService.getByDepartment({
			startMonth: dateRange.value?.[0],
			endMonth: dateRange.value?.[1]
		})
		
		// Handle both array response and object with data property
		const data = Array.isArray(response) ? response : (response.data || [])
		reportData.value = data
		
		// Calculate summary from data
		if (data.length > 0) {
			summary.total = data.reduce((sum, item) => sum + (item.total || 0), 0)
			summary.completed = data.reduce((sum, item) => sum + (item.completed || 0), 0)
			summary.processing = data.reduce((sum, item) => sum + (item.processing || 0), 0)
			
			// Calculate average time (if avgDays exists in data)
			const totalDays = data.reduce((sum, item) => sum + ((item.avgDays || 0) * (item.total || 0)), 0)
			summary.avgTime = summary.total > 0 ? (totalDays / summary.total).toFixed(1) : 0
		} else {
			// Reset summary if no data
			summary.total = 0
			summary.completed = 0
			summary.processing = 0
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
		// Use feedbackService to get feedbacks by department
		const response = await feedbackService.getList({ departmentId, size: 100 })
		
		// Handle both array and object with data property
		const data = Array.isArray(response) ? response : (response.data || [])
		detailFeedbacks.value = data.map(f => ({
			id: f.id,
			code: f.code,
			content: f.content,
			status: f.status,
			createdDate: f.receivedDate ? new Date(f.receivedDate).toLocaleDateString('vi-VN') : '',
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

const getProgressColor = (percentage) => {
	if (percentage >= 80) return '#67C23A'
	if (percentage >= 60) return '#E6A23C'
	return '#F56C6C'
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

const goToFeedbackList = () => {
	detailDialogVisible.value = false
	router.push({
		path: '/feedback',
		query: { departmentId: selectedDepartment.value?.id }
	})
}

const exportExcel = async () => {
	try {
		const blob = await reportService.exportExcel({
			type: 'by-department',
			startMonth: dateRange.value?.[0],
			endMonth: dateRange.value?.[1]
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
	grid-template-columns: repeat(4, 1fr);
	gap: 16px;
	margin-bottom: 24px;
}

.stat-card {
	display: flex;
	align-items: center;
	gap: 16px;
	padding: 20px;
	border-radius: var(--radius-lg);
	background: white;
	box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
	transition: transform 0.2s, box-shadow 0.2s;
}

.stat-card:hover {
	transform: translateY(-2px);
	box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.stat-icon {
	width: 56px;
	height: 56px;
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24px;
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

.stat-time .stat-icon {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
	color: white;
}

.stat-info {
	display: flex;
	flex-direction: column;
}

.stat-value {
	font-size: 28px;
	font-weight: 700;
	color: var(--secondary-color);
	line-height: 1.2;
}

.stat-value small {
	font-size: 14px;
	font-weight: 500;
}

.stat-label {
	font-size: 13px;
	color: var(--text-secondary);
	margin-top: 4px;
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

/* Chart */
.chart-container {
	height: 350px;
}

/* Detail Dialog */
.detail-content {
	padding: 0 20px;
}

.detail-summary {
	display: flex;
	gap: 24px;
	padding: 20px;
	background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
	border-radius: var(--radius-lg);
	margin-bottom: 24px;
}

.summary-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	flex: 1;
}

.summary-label {
	font-size: 12px;
	color: var(--text-secondary);
	margin-bottom: 4px;
}

.summary-value {
	font-size: 24px;
	font-weight: 700;
	color: var(--secondary-color);
}

.detail-tabs {
	margin-bottom: 16px;
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

	.detail-summary {
		flex-wrap: wrap;
		gap: 16px;
	}

	.summary-item {
		min-width: 45%;
	}
}
</style>

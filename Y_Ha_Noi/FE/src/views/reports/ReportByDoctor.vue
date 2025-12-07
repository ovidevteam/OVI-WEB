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

			<!-- Table -->
			<el-table
				:data="reportData"
				v-loading="loading"
				stripe
				row-key="id"
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

		<!-- Top 10 Chart -->
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">
					<el-icon><TrendCharts /></el-icon>
					Top 10 Bác sĩ xử lý nhanh nhất
				</h3>
			</div>
			<div class="chart-container">
				<BarChart :data="chartData" />
			</div>
		</div>

		<!-- Detail Dialog -->
		<el-dialog
			v-model="detailDialogVisible"
			:title="'Chi tiết: ' + selectedDoctor?.doctorName"
			width="900px"
			class="detail-dialog"
		>
			<div class="detail-content" v-if="selectedDoctor">
				<!-- Doctor Profile -->
				<div class="doctor-profile">
					<el-avatar :size="80" class="profile-avatar">
						{{ selectedDoctor.doctorName?.charAt(0) }}
					</el-avatar>
					<div class="profile-info">
						<h3 class="profile-name">{{ selectedDoctor.doctorName }}</h3>
						<p class="profile-specialty">
							<el-tag>{{ selectedDoctor.specialty }}</el-tag>
							<span class="profile-dept">{{ selectedDoctor.departmentName }}</span>
						</p>
						<div class="profile-rating">
							<el-rate v-model="selectedDoctor.rating" disabled show-score text-color="#ff9900" />
						</div>
					</div>
				</div>

				<!-- Stats Cards -->
				<div class="detail-stats">
					<div class="detail-stat-card">
						<span class="stat-number">{{ selectedDoctor.total }}</span>
						<span class="stat-text">Tổng phản ánh</span>
					</div>
					<div class="detail-stat-card success">
						<span class="stat-number">{{ selectedDoctor.completed }}</span>
						<span class="stat-text">Đã hoàn thành</span>
					</div>
					<div class="detail-stat-card warning">
						<span class="stat-number">{{ selectedDoctor.avgDays }}</span>
						<span class="stat-text">Thời gian TB (ngày)</span>
					</div>
					<div class="detail-stat-card info">
						<span class="stat-number">{{ Math.round(selectedDoctor.completed / selectedDoctor.total * 100) }}%</span>
						<span class="stat-text">Tỷ lệ hoàn thành</span>
					</div>
				</div>

				<!-- Feedback List -->
				<div class="detail-section">
					<h4 class="section-title">Danh sách phản ánh đã xử lý</h4>
					<el-table :data="doctorFeedbacks" max-height="350" v-loading="detailLoading" row-key="id">
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
						<el-table-column prop="processingTime" label="Thời gian XL" width="120" align="center">
							<template #default="{ row }">
								<el-tag :type="row.processingTime <= 2 ? 'success' : 'warning'" size="small">
									{{ row.processingTime }} ngày
								</el-tag>
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
	</div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download, User, Timer, TrendCharts, ArrowRight } from '@element-plus/icons-vue'
import reportService from '@/services/reportService'
import departmentService from '@/services/departmentService'
import { downloadBlob } from '@/utils/helpers'
import { handleApiError } from '@/utils/errorHandler'
import BarChart from '@/components/charts/BarChart.vue'

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

const fetchData = async () => {
	loading.value = true
	try {
		const response = await reportService.getByDoctor({
			departmentId: filterDepartment.value,
			startMonth: dateRange.value?.[0],
			endMonth: dateRange.value?.[1]
		})
		// API returns array directly
		const data = Array.isArray(response) ? response : (response.data || [])
		reportData.value = data
		total.value = data.length
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
		// Use feedbackService to get feedbacks by doctor
		const response = await feedbackService.getList({ doctorId, size: 100 })
		
		// Handle both array and object with data property
		const data = Array.isArray(response) ? response : (response.data || [])
		doctorFeedbacks.value = data.map(f => ({
			id: f.id,
			code: f.code,
			content: f.content,
			status: f.status,
			processingTime: f.completedDate && f.receivedDate ? 
				Math.round((new Date(f.completedDate) - new Date(f.receivedDate)) / (1000 * 60 * 60 * 24) * 10) / 10 : null,
			completedDate: f.completedDate ? new Date(f.completedDate).toLocaleDateString('vi-VN') : null
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
		query: { doctorId: selectedDoctor.value?.id }
	})
}

const exportExcel = async () => {
	try {
		const blob = await reportService.exportExcel({
			type: 'by-doctor',
			departmentId: filterDepartment.value,
			startMonth: dateRange.value?.[0],
			endMonth: dateRange.value?.[1]
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

/* Detail Stats */
.detail-stats {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16px;
	margin-bottom: 24px;
}

.detail-stat-card {
	padding: 20px;
	background: #f5f7fa;
	border-radius: var(--radius-md);
	text-align: center;
	border-left: 4px solid var(--primary-color);
}

.detail-stat-card.success {
	border-left-color: var(--success-color);
}

.detail-stat-card.warning {
	border-left-color: #e6a23c;
}

.detail-stat-card.info {
	border-left-color: #409eff;
}

.stat-number {
	display: block;
	font-size: 28px;
	font-weight: 700;
	color: var(--secondary-color);
	line-height: 1.2;
}

.stat-text {
	font-size: 12px;
	color: var(--text-secondary);
	margin-top: 4px;
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
	.detail-stats {
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

	.detail-stats {
		grid-template-columns: 1fr 1fr;
	}

	.detail-stat-card {
		padding: 16px;
	}

	.stat-number {
		font-size: 22px;
	}
}

@media (max-width: 480px) {
	.detail-stats {
		grid-template-columns: 1fr;
	}
}
</style>

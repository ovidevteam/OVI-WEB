<template>
	<div class="feedback-ratings">
		<!-- Page Header -->
		<div class="page-header">
			<h3 class="page-title">Đánh giá Bác sĩ sau xử lý</h3>
			<p class="page-desc">Đánh giá chất lượng xử lý của bác sĩ sau khi phản ánh được hoàn thành</p>
		</div>

		<!-- Filter Section -->
		<div class="content-card filter-card">
			<el-row :gutter="16">
				<el-col :xs="24" :sm="8" :md="6">
					<el-select v-model="filterStatus" placeholder="Trạng thái đánh giá" clearable style="width: 100%">
						<el-option label="Chưa đánh giá" value="pending" />
						<el-option label="Đã đánh giá" value="rated" />
					</el-select>
				</el-col>
				<el-col :xs="24" :sm="8" :md="6">
					<el-select v-model="filterDepartment" placeholder="Phòng ban" clearable style="width: 100%">
						<el-option
							v-for="dept in departments"
							:key="dept.id"
							:label="dept.name"
							:value="dept.id"
						/>
					</el-select>
				</el-col>
				<el-col :xs="24" :sm="8" :md="6">
					<el-date-picker
						v-model="filterDateRange"
						type="daterange"
						range-separator="-"
						start-placeholder="Từ ngày"
						end-placeholder="Đến ngày"
						style="width: 100%"
					/>
				</el-col>
				<el-col :xs="24" :sm="24" :md="6">
					<el-button type="primary" @click="fetchData" :icon="Search">Tìm kiếm</el-button>
					<el-button @click="resetFilter" :icon="RefreshRight">Đặt lại</el-button>
				</el-col>
			</el-row>
		</div>

		<!-- Summary Stats -->
		<div class="stats-grid">
			<div class="stat-card">
				<div class="stat-icon total">
					<el-icon><Star /></el-icon>
				</div>
				<div class="stat-info">
					<span class="stat-value">{{ stats.total }}</span>
					<span class="stat-label">Tổng PA hoàn thành</span>
				</div>
			</div>
			<div class="stat-card">
				<div class="stat-icon pending">
					<el-icon><Clock /></el-icon>
				</div>
				<div class="stat-info">
					<span class="stat-value">{{ stats.pending }}</span>
					<span class="stat-label">Chờ đánh giá</span>
				</div>
			</div>
			<div class="stat-card">
				<div class="stat-icon rated">
					<el-icon><CircleCheck /></el-icon>
				</div>
				<div class="stat-info">
					<span class="stat-value">{{ stats.rated }}</span>
					<span class="stat-label">Đã đánh giá</span>
				</div>
			</div>
			<div class="stat-card">
				<div class="stat-icon avg">
					<el-icon><TrendCharts /></el-icon>
				</div>
				<div class="stat-info">
					<span class="stat-value">{{ stats.avgRating }}</span>
					<span class="stat-label">Điểm TB</span>
				</div>
			</div>
		</div>

		<!-- Feedback List -->
		<div class="content-card">
			<div class="content-card-header">
				<h4 class="content-card-title">Danh sách phản ánh đã hoàn thành</h4>
			</div>

			<el-table
				:data="feedbackList"
				v-loading="loading"
				stripe
				@row-click="openRatingDialog"
				class="rating-table"
				style="width: 100%"
			>
				<el-table-column prop="code" label="Mã PA" width="120" fixed />
				<el-table-column prop="content" label="Nội dung" min-width="200">
					<template #default="{ row }">
						<el-tooltip :content="row.content" placement="top" :show-after="500">
							<span class="text-ellipsis">{{ row.content }}</span>
						</el-tooltip>
					</template>
				</el-table-column>
				<el-table-column prop="doctorName" label="Bác sĩ liên quan" width="160">
					<template #default="{ row }">
						<div class="doctor-cell">
							<el-avatar :size="28" class="doctor-avatar">
								{{ row.doctorName?.charAt(0) }}
							</el-avatar>
							<span>{{ row.doctorName || '-' }}</span>
						</div>
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng ban" width="120" />
				<el-table-column prop="completedDate" label="Ngày HT" width="110" />
				<el-table-column prop="ratingStatus" label="Trạng thái" width="130">
					<template #default="{ row }">
						<el-tag :type="row.rating ? 'success' : 'warning'" size="small">
							{{ row.rating ? 'Đã đánh giá' : 'Chờ đánh giá' }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="rating" label="Đánh giá" width="150">
					<template #default="{ row }">
						<el-rate
							v-if="row.rating"
							v-model="row.rating"
							disabled
							show-score
							text-color="#ff9900"
							:size="'small'"
						/>
						<span v-else class="no-rating">Chưa có</span>
					</template>
				</el-table-column>
				<el-table-column label="Thao tác" width="120" fixed="right">
					<template #default="{ row }">
						<el-button
							type="primary"
							:icon="row.rating ? Edit : Star"
							size="small"
							@click.stop="openRatingDialog(row)"
						>
							{{ row.rating ? 'Sửa' : 'Đánh giá' }}
						</el-button>
					</template>
				</el-table-column>
			</el-table>

			<!-- Pagination -->
			<div class="pagination-wrapper">
				<el-pagination
					v-model:current-page="currentPage"
					v-model:page-size="pageSize"
					:page-sizes="[10, 20, 50]"
					layout="total, sizes, prev, pager, next, jumper"
					:total="total"
					@size-change="handleSizeChange"
					@current-change="handlePageChange"
				/>
			</div>
		</div>

		<!-- Rating Dialog -->
		<el-dialog
			v-model="ratingDialogVisible"
			:title="selectedFeedback?.rating ? 'Chỉnh sửa đánh giá' : 'Đánh giá Bác sĩ'"
			width="600px"
			class="rating-dialog"
			:close-on-click-modal="false"
		>
			<div class="rating-dialog-content" v-if="selectedFeedback">
				<!-- Feedback Info -->
				<div class="feedback-info-section">
					<h4 class="section-title">Thông tin phản ánh</h4>
					<div class="feedback-info-grid">
						<div class="info-item">
							<span class="info-label">Mã PA:</span>
							<span class="info-value">{{ selectedFeedback.code }}</span>
						</div>
						<div class="info-item">
							<span class="info-label">Phòng ban:</span>
							<span class="info-value">{{ selectedFeedback.departmentName }}</span>
						</div>
						<div class="info-item full-width">
							<span class="info-label">Nội dung:</span>
							<span class="info-value">{{ selectedFeedback.content }}</span>
						</div>
					</div>
				</div>

				<!-- Doctor Info -->
				<div class="doctor-info-section">
					<h4 class="section-title">Bác sĩ được đánh giá</h4>
					<div class="doctor-profile">
						<el-avatar :size="56" class="doctor-profile-avatar">
							{{ selectedFeedback.doctorName?.charAt(0) }}
						</el-avatar>
						<div class="doctor-profile-info">
							<span class="doctor-name">{{ selectedFeedback.doctorName }}</span>
							<span class="doctor-dept">{{ selectedFeedback.departmentName }}</span>
						</div>
					</div>
				</div>

				<!-- Rating Form -->
				<div class="rating-form-section">
					<h4 class="section-title">Đánh giá chất lượng xử lý</h4>

					<el-form :model="ratingForm" ref="ratingFormRef" :rules="ratingRules" label-position="top">
						<el-form-item label="Đánh giá (1-5 sao)" prop="rating">
							<div class="rating-stars">
								<el-rate
									v-model="ratingForm.rating"
									show-score
									show-text
									:texts="['Rất tệ', 'Tệ', 'Bình thường', 'Tốt', 'Rất tốt']"
									text-color="#ff9900"
									:size="'large'"
								/>
							</div>
						</el-form-item>

						<el-form-item label="Nhận xét" prop="comment">
							<el-input
								v-model="ratingForm.comment"
								type="textarea"
								:rows="3"
								placeholder="Nhập nhận xét về chất lượng xử lý của bác sĩ..."
								maxlength="500"
								show-word-limit
							/>
						</el-form-item>
					</el-form>
				</div>
			</div>

			<template #footer>
				<el-button @click="ratingDialogVisible = false">Hủy</el-button>
				<el-button type="primary" @click="submitRating" :loading="submitting">
					<el-icon><Check /></el-icon>
					{{ selectedFeedback?.rating ? 'Cập nhật' : 'Lưu đánh giá' }}
				</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'
import {
	Star, Clock, CircleCheck, TrendCharts, Search, RefreshRight, Edit, Check
} from '@element-plus/icons-vue'

const authStore = useAuthStore()

const loading = ref(false)
const submitting = ref(false)
const feedbackList = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)

// Filters
const filterStatus = ref(null)
const filterDepartment = ref(null)
const filterDateRange = ref([])

const departments = ref([
	{ id: 1, name: 'Nội khoa' },
	{ id: 2, name: 'Ngoại khoa' },
	{ id: 3, name: 'Da liễu' },
	{ id: 4, name: 'Sản khoa' },
	{ id: 5, name: 'Nhi khoa' }
])

// Stats
const stats = reactive({
	total: 45,
	pending: 8,
	rated: 37,
	avgRating: 4.2
})

// Rating dialog
const ratingDialogVisible = ref(false)
const selectedFeedback = ref(null)
const ratingFormRef = ref(null)

const ratingForm = reactive({
	rating: 0,
	comment: ''
})

const ratingRules = {
	rating: [
		{ required: true, message: 'Vui lòng chọn số sao đánh giá', trigger: 'change' },
		{ type: 'number', min: 1, max: 5, message: 'Đánh giá từ 1-5 sao', trigger: 'change' }
	]
}

// Mock data
const mockFeedbackList = [
	{
		id: 1,
		code: 'PA-2024-001',
		content: 'Thời gian chờ khám quá lâu, bệnh nhân phải đợi hơn 2 tiếng',
		doctorId: 101,
		doctorName: 'BS. Nguyễn Văn A',
		departmentId: 1,
		departmentName: 'Nội khoa',
		completedDate: '20/11/2024',
		rating: 4,
		comment: 'Bác sĩ xử lý tốt, giải quyết nhanh chóng'
	},
	{
		id: 2,
		code: 'PA-2024-002',
		content: 'Nhân viên lễ tân thiếu thân thiện với bệnh nhân ca sáng',
		doctorId: 102,
		doctorName: 'BS. Trần Thị B',
		departmentId: 2,
		departmentName: 'Ngoại khoa',
		completedDate: '21/11/2024',
		rating: null,
		comment: null
	},
	{
		id: 3,
		code: 'PA-2024-003',
		content: 'Khen ngợi bác sĩ điều trị nhiệt tình, chuyên nghiệp',
		doctorId: 103,
		doctorName: 'BS. Lê Văn C',
		departmentId: 3,
		departmentName: 'Da liễu',
		completedDate: '22/11/2024',
		rating: 5,
		comment: 'Xuất sắc!'
	},
	{
		id: 4,
		code: 'PA-2024-004',
		content: 'Phòng khám thiếu sạch sẽ, cần cải thiện vệ sinh',
		doctorId: 104,
		doctorName: 'BS. Phạm Thị D',
		departmentId: 4,
		departmentName: 'Sản khoa',
		completedDate: '23/11/2024',
		rating: null,
		comment: null
	},
	{
		id: 5,
		code: 'PA-2024-005',
		content: 'Cơ sở vật chất xuống cấp, máy lạnh không hoạt động',
		doctorId: 105,
		doctorName: 'BS. Hoàng Văn E',
		departmentId: 5,
		departmentName: 'Nhi khoa',
		completedDate: '24/11/2024',
		rating: 3,
		comment: 'Xử lý ổn'
	},
	{
		id: 6,
		code: 'PA-2024-006',
		content: 'Thái độ phục vụ của y tá chưa tốt trong giờ trưa',
		doctorId: 101,
		doctorName: 'BS. Nguyễn Văn A',
		departmentId: 1,
		departmentName: 'Nội khoa',
		completedDate: '25/11/2024',
		rating: null,
		comment: null
	},
	{
		id: 7,
		code: 'PA-2024-007',
		content: 'Bác sĩ giải thích rõ ràng về tình trạng bệnh và phác đồ điều trị',
		doctorId: 102,
		doctorName: 'BS. Trần Thị B',
		departmentId: 2,
		departmentName: 'Ngoại khoa',
		completedDate: '26/11/2024',
		rating: 5,
		comment: 'Rất hài lòng với cách giải thích của bác sĩ'
	},
	{
		id: 8,
		code: 'PA-2024-008',
		content: 'Phòng chờ quá đông, không đủ ghế ngồi cho bệnh nhân',
		doctorId: 103,
		doctorName: 'BS. Lê Văn C',
		departmentId: 3,
		departmentName: 'Da liễu',
		completedDate: '27/11/2024',
		rating: null,
		comment: null
	}
]

const fetchData = async () => {
	loading.value = true
	try {
		// Simulate API call
		await new Promise(resolve => setTimeout(resolve, 500))

		let filtered = [...mockFeedbackList]

		// Apply filters
		if (filterStatus.value === 'pending') {
			filtered = filtered.filter(f => !f.rating)
		} else if (filterStatus.value === 'rated') {
			filtered = filtered.filter(f => f.rating)
		}

		if (filterDepartment.value) {
			filtered = filtered.filter(f => f.departmentId === filterDepartment.value)
		}

		feedbackList.value = filtered
		total.value = filtered.length

		// Update stats
		stats.total = mockFeedbackList.length
		stats.pending = mockFeedbackList.filter(f => !f.rating).length
		stats.rated = mockFeedbackList.filter(f => f.rating).length
		const ratedItems = mockFeedbackList.filter(f => f.rating)
		stats.avgRating = ratedItems.length > 0
			? (ratedItems.reduce((sum, f) => sum + f.rating, 0) / ratedItems.length).toFixed(1)
			: 0
	} catch (error) {
		ElMessage.error('Lỗi khi tải dữ liệu')
		console.error(error)
	} finally {
		loading.value = false
	}
}

const resetFilter = () => {
	filterStatus.value = null
	filterDepartment.value = null
	filterDateRange.value = []
	fetchData()
}

const handlePageChange = (page) => {
	currentPage.value = page
	fetchData()
}

const handleSizeChange = (size) => {
	pageSize.value = size
	fetchData()
}

const openRatingDialog = (row) => {
	selectedFeedback.value = { ...row }
	ratingForm.rating = row.rating || 0
	ratingForm.comment = row.comment || ''
	ratingDialogVisible.value = true
}

const submitRating = async () => {
	if (!ratingFormRef.value) return

	try {
		await ratingFormRef.value.validate()
		submitting.value = true

		// Simulate API call
		await new Promise(resolve => setTimeout(resolve, 800))

		// Update local data
		const index = feedbackList.value.findIndex(f => f.id === selectedFeedback.value.id)
		if (index !== -1) {
			feedbackList.value[index].rating = ratingForm.rating
			feedbackList.value[index].comment = ratingForm.comment
		}

		// Also update mock data
		const mockIndex = mockFeedbackList.findIndex(f => f.id === selectedFeedback.value.id)
		if (mockIndex !== -1) {
			mockFeedbackList[mockIndex].rating = ratingForm.rating
			mockFeedbackList[mockIndex].comment = ratingForm.comment
		}

		ElMessage.success(selectedFeedback.value.rating ? 'Cập nhật đánh giá thành công!' : 'Đánh giá thành công!')
		ratingDialogVisible.value = false

		// Refresh stats
		stats.pending = mockFeedbackList.filter(f => !f.rating).length
		stats.rated = mockFeedbackList.filter(f => f.rating).length
		const ratedItems = mockFeedbackList.filter(f => f.rating)
		stats.avgRating = ratedItems.length > 0
			? (ratedItems.reduce((sum, f) => sum + f.rating, 0) / ratedItems.length).toFixed(1)
			: 0
	} catch (error) {
		if (error !== false) {
			ElMessage.error('Có lỗi xảy ra. Vui lòng thử lại.')
		}
	} finally {
		submitting.value = false
	}
}

onMounted(() => {
	fetchData()
})
</script>

<style scoped>
.feedback-ratings {
	width: 100%;
}

.page-header {
	margin-bottom: 24px;
}

.page-title {
	font-size: 1.8rem;
	color: var(--text-primary);
	margin: 0 0 8px 0;
}

.page-desc {
	color: var(--text-secondary);
	font-size: 0.95rem;
	margin: 0;
}

.filter-card {
	margin-bottom: 24px;
}

.filter-card .el-col {
	margin-bottom: 12px;
}

/* Stats Grid */
.stats-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 20px;
	margin-bottom: 24px;
}

.stat-card {
	background: var(--bg-card);
	border-radius: var(--radius-lg);
	padding: 20px;
	display: flex;
	align-items: center;
	gap: 16px;
	box-shadow: var(--shadow-sm);
	transition: all 0.3s ease;
}

.stat-card:hover {
	transform: translateY(-2px);
	box-shadow: var(--shadow-md);
}

.stat-icon {
	width: 48px;
	height: 48px;
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 22px;
	color: white;
	flex-shrink: 0;
}

.stat-icon.total {
	background: linear-gradient(135deg, #409eff, #66b1ff);
}

.stat-icon.pending {
	background: linear-gradient(135deg, #e6a23c, #f5c77e);
}

.stat-icon.rated {
	background: linear-gradient(135deg, #67c23a, #95d475);
}

.stat-icon.avg {
	background: linear-gradient(135deg, #909399, #c0c4cc);
}

.stat-info {
	display: flex;
	flex-direction: column;
}

.stat-value {
	font-size: 1.6rem;
	font-weight: 700;
	color: var(--text-primary);
	line-height: 1.2;
}

.stat-label {
	font-size: 0.85rem;
	color: var(--text-secondary);
}

/* Table */
.rating-table {
	border-radius: var(--radius-md);
}

.rating-table :deep(.el-table__row) {
	cursor: pointer;
}

.text-ellipsis {
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;
	max-width: 250px;
	display: inline-block;
}

.doctor-cell {
	display: flex;
	align-items: center;
	gap: 8px;
}

.doctor-avatar {
	background: linear-gradient(135deg, #409eff, #66b1ff);
	color: white;
	font-size: 12px;
}

.no-rating {
	color: var(--text-placeholder);
	font-style: italic;
}

.pagination-wrapper {
	margin-top: 20px;
	display: flex;
	justify-content: flex-end;
}

/* Rating Dialog */
.rating-dialog-content {
	padding: 0 10px;
}

.section-title {
	font-size: 1rem;
	font-weight: 600;
	color: var(--primary-color);
	margin: 0 0 16px 0;
	padding-bottom: 8px;
	border-bottom: 2px solid var(--primary-color);
}

.feedback-info-section {
	margin-bottom: 24px;
}

.feedback-info-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 12px;
}

.feedback-info-grid .info-item.full-width {
	grid-column: 1 / -1;
}

.info-item {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.info-label {
	font-size: 0.85rem;
	color: var(--text-secondary);
}

.info-value {
	font-size: 0.95rem;
	color: var(--text-primary);
}

.doctor-info-section {
	margin-bottom: 24px;
}

.doctor-profile {
	display: flex;
	align-items: center;
	gap: 16px;
	padding: 16px;
	background: var(--bg-hover);
	border-radius: var(--radius-md);
}

.doctor-profile-avatar {
	background: linear-gradient(135deg, #00b4d8, #48cae4);
	color: white;
	font-size: 20px;
}

.doctor-profile-info {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.doctor-name {
	font-size: 1.1rem;
	font-weight: 600;
	color: var(--text-primary);
}

.doctor-dept {
	font-size: 0.9rem;
	color: var(--text-secondary);
}

.rating-form-section {
	margin-bottom: 16px;
}

.rating-stars {
	padding: 12px 0;
}

.rating-stars :deep(.el-rate) {
	height: auto;
}

.rating-stars :deep(.el-rate__item) {
	font-size: 28px;
}

/* Responsive */
@media (max-width: 1024px) {
	.stats-grid {
		grid-template-columns: repeat(2, 1fr);
	}
}

@media (max-width: 768px) {
	.stats-grid {
		grid-template-columns: 1fr;
	}

	.feedback-info-grid {
		grid-template-columns: 1fr;
	}
}

@media (max-width: 480px) {
	.stat-card {
		padding: 16px;
	}

	.stat-value {
		font-size: 1.3rem;
	}
}
</style>


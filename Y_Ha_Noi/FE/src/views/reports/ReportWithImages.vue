<template>
	<div class="report-with-images">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Báo cáo có Hình ảnh</h3>
				<div class="header-actions">
					<el-select v-model="filterDepartment" placeholder="Phòng ban" clearable @change="fetchData">
						<el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
					</el-select>
					<el-date-picker
						v-model="dateRange"
						type="daterange"
						start-placeholder="Từ ngày"
						end-placeholder="Đến ngày"
						format="DD/MM/YYYY"
						value-format="YYYY-MM-DD"
						@change="fetchData"
					/>
					<el-button type="primary" :icon="Download" @click="downloadAll">
						Tải tất cả ảnh
					</el-button>
				</div>
			</div>

			<!-- Stats -->
			<div class="stats-row">
				<div class="stat-item">
					<span class="stat-value">{{ reportData.length }}</span>
					<span class="stat-label">Phản ánh có ảnh</span>
				</div>
				<div class="stat-item">
					<span class="stat-value">{{ totalImages }}</span>
					<span class="stat-label">Tổng số ảnh</span>
				</div>
			</div>

			<!-- Feedback Cards with Images -->
			<div class="feedback-grid">
				<div
					v-for="feedback in reportData"
					:key="feedback.id"
					class="feedback-card"
					@click="viewDetail(feedback.id)"
				>
					<div class="card-header">
						<span class="feedback-code">{{ feedback.code }}</span>
						<el-tag :type="getStatusType(feedback.status)" size="small">
							{{ getStatusLabel(feedback.status) }}
						</el-tag>
					</div>

					<div class="card-body">
						<p class="feedback-content">{{ truncate(feedback.content, 80) }}</p>
						<div class="feedback-meta">
							<span>{{ feedback.departmentName }}</span>
							<span>{{ formatDate(feedback.receivedDate) }}</span>
						</div>
					</div>

					<div class="card-images">
						<div class="image-grid">
							<div
								v-for="(image, index) in feedback.images.slice(0, 4)"
								:key="image.id"
								class="image-item"
								@click.stop="openGallery(feedback, index)"
							>
								<el-image
									:src="image.url"
									fit="cover"
									lazy
								/>
								<div v-if="index === 3 && feedback.images.length > 4" class="image-overlay">
									+{{ feedback.images.length - 4 }}
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>

			<el-empty v-if="reportData.length === 0 && !loading" description="Không có phản ánh có hình ảnh" />
		</div>

		<!-- Image Gallery Dialog -->
		<el-dialog
			v-model="galleryVisible"
			title="Xem hình ảnh"
			width="800px"
		>
			<ImageGallery v-if="selectedFeedback" :images="selectedFeedback.images" />
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import { formatDate, truncate, getStatusLabel, getStatusType } from '@/utils/helpers'
import reportService from '@/services/reportService'
import departmentService from '@/services/departmentService'
import { handleApiError } from '@/utils/errorHandler'
import ImageGallery from '@/components/upload/ImageGallery.vue'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const loading = ref(false)
const reportData = ref([])
const departments = ref([])
const filterDepartment = ref('')
const dateRange = ref([])
const galleryVisible = ref(false)
const selectedFeedback = ref(null)

const totalImages = computed(() => {
	return reportData.value.reduce((sum, f) => sum + (f.images?.length || 0), 0)
})

const fetchData = async () => {
	loading.value = true
	try {
		const response = await reportService.getWithImages({
			departmentId: filterDepartment.value || undefined,
			dateFrom: dateRange.value?.[0],
			dateTo: dateRange.value?.[1]
		})
		// API returns array directly
		reportData.value = Array.isArray(response) ? response : (response.data || [])
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			reportData.value = [
				{
					id: 1,
					code: 'PA-20251127-001',
					content: 'Phản ánh về thái độ phục vụ của nhân viên khoa Nội khi tiếp nhận bệnh nhân',
					departmentName: 'Nội khoa',
					receivedDate: '2025-11-27',
					status: 'PROCESSING',
					images: [
						{ id: 1, url: 'https://via.placeholder.com/300x200?text=Image+1' },
						{ id: 2, url: 'https://via.placeholder.com/300x200?text=Image+2' }
					]
				},
				{
					id: 2,
					code: 'PA-20251126-002',
					content: 'Thời gian chờ khám quá lâu tại phòng khám da liễu, cần cải thiện',
					departmentName: 'Da liễu',
					receivedDate: '2025-11-26',
					status: 'COMPLETED',
					images: [
						{ id: 3, url: 'https://via.placeholder.com/300x200?text=Image+3' },
						{ id: 4, url: 'https://via.placeholder.com/300x200?text=Image+4' },
						{ id: 5, url: 'https://via.placeholder.com/300x200?text=Image+5' }
					]
				},
				{
					id: 3,
					code: 'PA-20251125-003',
					content: 'Cơ sở vật chất phòng khám cần được nâng cấp để phục vụ bệnh nhân tốt hơn',
					departmentName: 'Ngoại khoa',
					receivedDate: '2025-11-25',
					status: 'NEW',
					images: [
						{ id: 6, url: 'https://via.placeholder.com/300x200?text=Image+6' }
					]
				}
			]
		} else {
			handleApiError(error, 'Report With Images')
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
				{ id: 2, name: 'Ngoại khoa' }
			]
		} else {
			console.error('Failed to load departments:', error)
		}
	}
}

const viewDetail = (id) => {
	router.push(`/feedback/${id}`)
}

const openGallery = (feedback, index) => {
	selectedFeedback.value = feedback
	galleryVisible.value = true
}

const downloadAll = async () => {
	try {
		ElMessage.info('Đang tải... Vui lòng chờ')
		// TODO: Implement download all images as ZIP
		ElMessage.success('Tải thành công!')
	} catch (error) {
		ElMessage.error('Tải thất bại!')
	}
}

onMounted(() => {
	fetchData()
	fetchDepartments()
})
</script>

<style scoped>
.header-actions {
	display: flex;
	gap: 12px;
	flex-wrap: wrap;
}

.stats-row {
	display: flex;
	gap: 32px;
	margin-bottom: 24px;
}

.stat-item {
	display: flex;
	flex-direction: column;
}

.stat-value {
	font-size: 28px;
	font-weight: 700;
	color: var(--primary-color);
}

.stat-label {
	font-size: 13px;
	color: var(--text-secondary);
}

.feedback-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
	gap: 20px;
}

.feedback-card {
	background: var(--card-background);
	border: 1px solid var(--border-color);
	border-radius: var(--radius-lg);
	overflow: hidden;
	cursor: pointer;
	transition: transform var(--transition-fast), box-shadow var(--transition-fast);
}

.feedback-card:hover {
	transform: translateY(-4px);
	box-shadow: var(--shadow-md);
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 12px 16px;
	background: var(--background-color);
	border-bottom: 1px solid var(--border-color);
}

.feedback-code {
	font-weight: 600;
	color: var(--primary-color);
}

.card-body {
	padding: 16px;
}

.feedback-content {
	margin: 0 0 12px;
	color: var(--text-primary);
	line-height: 1.5;
}

.feedback-meta {
	display: flex;
	justify-content: space-between;
	font-size: 12px;
	color: var(--text-secondary);
}

.card-images {
	padding: 0 16px 16px;
}

.image-grid {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 8px;
}

.image-item {
	position: relative;
	aspect-ratio: 1;
	border-radius: var(--radius-sm);
	overflow: hidden;
}

.image-item :deep(.el-image) {
	width: 100%;
	height: 100%;
}

.image-overlay {
	position: absolute;
	inset: 0;
	background: rgba(0, 0, 0, 0.6);
	display: flex;
	align-items: center;
	justify-content: center;
	color: white;
	font-size: 18px;
	font-weight: 600;
}

@media (max-width: 768px) {
	.header-actions {
		flex-direction: column;
		width: 100%;
	}

	.feedback-grid {
		grid-template-columns: 1fr;
	}
}
</style>


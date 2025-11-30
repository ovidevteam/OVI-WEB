<template>
	<div class="feedback-detail" v-loading="loading">
		<template v-if="feedback">
			<!-- Header -->
			<div class="detail-header">
				<div class="header-left">
					<el-button :icon="ArrowLeft" @click="goBack">Quay lại</el-button>
					<div class="header-info">
						<h2 class="feedback-code">{{ feedback.code }}</h2>
						<div class="header-meta">
							<el-tag :type="getStatusType(feedback.status)" size="large">
								{{ getStatusLabel(feedback.status) }}
							</el-tag>
							<el-tag :type="getLevelType(feedback.level)" size="large" effect="plain">
								{{ getLevelLabel(feedback.level) }}
							</el-tag>
						</div>
					</div>
				</div>
				<!-- Nút xử lý đã chuyển sang trang "Của tôi" (/my-feedbacks) -->
			</div>

			<!-- Content -->
			<div class="detail-content">
				<!-- Info Card -->
				<div class="content-card info-card">
					<div class="content-card-header">
						<h3 class="content-card-title">Thông tin phản ánh</h3>
					</div>

					<el-descriptions :column="2" border>
						<el-descriptions-item label="Ngày tiếp nhận">
							{{ formatDateTime(feedback.receivedDate) }}
						</el-descriptions-item>
						<el-descriptions-item label="Kênh tiếp nhận">
							<el-tag size="small" effect="plain">
								{{ getChannelLabel(feedback.channel) }}
							</el-tag>
						</el-descriptions-item>
						<el-descriptions-item label="Phòng ban">
							{{ feedback.departmentName }}
						</el-descriptions-item>
						<el-descriptions-item label="Bác sĩ liên quan">
							{{ feedback.doctorName || 'Không có' }}
						</el-descriptions-item>
						<el-descriptions-item label="Người tiếp nhận">
							{{ feedback.receiverName }}
						</el-descriptions-item>
						<el-descriptions-item label="Người xử lý">
							{{ feedback.handlerName || 'Chưa phân công' }}
						</el-descriptions-item>
						<el-descriptions-item label="Nội dung" :span="2">
							<div class="content-text">{{ feedback.content }}</div>
						</el-descriptions-item>
					</el-descriptions>
				</div>

				<!-- Images -->
				<div class="content-card" v-if="feedback.images?.length > 0">
					<div class="content-card-header">
						<h3 class="content-card-title">Hình ảnh đính kèm</h3>
					</div>
					<ImageGallery :images="feedback.images" />
				</div>

				<!-- Timeline -->
				<div class="content-card">
					<div class="content-card-header">
						<h3 class="content-card-title">Lịch sử xử lý</h3>
					</div>
					<FeedbackTimeline :logs="feedback.logs" />
				</div>

				<!-- Process Result -->
				<div class="content-card" v-if="feedback.status === 'COMPLETED'">
					<div class="content-card-header">
						<h3 class="content-card-title">Kết quả xử lý</h3>
					</div>
					<div class="process-result">
						<el-descriptions :column="1" border>
							<el-descriptions-item label="Ngày hoàn thành">
								{{ formatDateTime(feedback.completedDate) }}
							</el-descriptions-item>
							<el-descriptions-item label="Ghi nhận xử lý">
								{{ feedback.processNote || 'Không có ghi chú' }}
							</el-descriptions-item>
						</el-descriptions>

						<div v-if="feedback.processImages?.length > 0" class="process-images">
							<h4>Hình ảnh minh chứng</h4>
							<ImageGallery :images="feedback.processImages" />
						</div>
					</div>
				</div>
			</div>
		</template>

		<!-- Dialog xử lý đã chuyển sang trang "Của tôi" (/my-feedbacks) -->
	</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import {
	formatDateTime, getChannelLabel, getLevelLabel, getLevelType,
	getStatusLabel, getStatusType
} from '@/utils/helpers'
import feedbackService from '@/services/feedbackService'
import ImageGallery from '@/components/upload/ImageGallery.vue'
import FeedbackTimeline from '@/components/feedback/FeedbackTimeline.vue'

const route = useRoute()
const router = useRouter()

const loading = ref(false)
const feedback = ref(null)

const fetchFeedback = async () => {
	loading.value = true
	try {
		feedback.value = await feedbackService.getById(route.params.id)
	} catch (error) {
		// Demo data
		feedback.value = {
			id: 1,
			code: 'PA-20251127-001',
			receivedDate: '2025-11-27T09:30:00',
			channel: 'HOTLINE',
			content: 'Phản ánh về thái độ phục vụ của nhân viên khoa Nội khi tiếp nhận bệnh nhân. Nhân viên không nhiệt tình hướng dẫn, khiến bệnh nhân phải chờ đợi lâu.',
			departmentName: 'Nội khoa',
			doctorName: 'BS. Nguyễn Văn A',
			level: 'HIGH',
			status: 'PROCESSING',
			receiverName: 'Nguyễn Thị Tiếp nhận',
			handlerName: 'BS. Nguyễn Văn A',
			handlerId: 1,
			images: [
				{ id: 1, url: 'https://via.placeholder.com/300x200' },
				{ id: 2, url: 'https://via.placeholder.com/300x200' }
			],
			logs: [
				{
					id: 1,
					action: 'CREATE',
					createdDate: '2025-11-27T09:30:00',
					userName: 'Nguyễn Thị Tiếp nhận',
					note: 'Tạo phản ánh mới'
				},
				{
					id: 2,
					action: 'ASSIGN',
					createdDate: '2025-11-27T09:35:00',
					userName: 'Hệ thống',
					note: 'Phân công cho BS. Nguyễn Văn A'
				},
				{
					id: 3,
					action: 'STATUS_CHANGE',
					createdDate: '2025-11-27T10:00:00',
					userName: 'BS. Nguyễn Văn A',
					oldStatus: 'NEW',
					newStatus: 'PROCESSING',
					note: 'Bắt đầu xử lý'
				}
			]
		}
	} finally {
		loading.value = false
	}
}

const goBack = () => {
	router.back()
}

onMounted(() => {
	fetchFeedback()
})
</script>

<style scoped>
.detail-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 24px;
	flex-wrap: wrap;
	gap: 16px;
}

.header-left {
	display: flex;
	align-items: center;
	gap: 20px;
}

.header-info {
	display: flex;
	flex-direction: column;
	gap: 8px;
}

.feedback-code {
	margin: 0;
	font-size: 1.5rem;
	font-weight: 700;
	color: var(--secondary-color);
}

.header-meta {
	display: flex;
	gap: 8px;
}

.content-text {
	white-space: pre-wrap;
	line-height: 1.6;
}

.process-images {
	margin-top: 20px;
}

.process-images h4 {
	margin: 0 0 12px;
	font-size: 0.875rem;
	color: var(--text-secondary);
}

@media (max-width: 768px) {
	.detail-header {
		flex-direction: column;
		align-items: flex-start;
	}

	.header-left {
		flex-direction: column;
		align-items: flex-start;
	}
}
</style>


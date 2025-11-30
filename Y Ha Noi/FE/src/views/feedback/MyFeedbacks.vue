<template>
	<div class="my-feedbacks">
		<!-- Stats -->
		<div class="stats-grid">
			<div class="stat-card danger">
				<div class="stat-icon">
					<el-icon><Warning /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.pending }}</div>
					<div class="stat-label">Chờ xử lý</div>
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
			<div class="stat-card" style="border-left-color: #F56C6C">
				<div class="stat-icon" style="background: rgba(245, 108, 108, 0.1); color: #F56C6C">
					<el-icon><Clock /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.overdue }}</div>
					<div class="stat-label">Quá hạn</div>
				</div>
			</div>
		</div>

		<!-- Tabs -->
		<div class="content-card">
			<el-tabs v-model="activeTab" @tab-change="handleTabChange">
				<el-tab-pane name="pending">
					<template #label>
						<span>Chờ xử lý</span>
						<el-badge v-if="stats.pending > 0" :value="stats.pending" class="tab-badge" />
					</template>
				</el-tab-pane>
				<el-tab-pane label="Đang xử lý" name="processing" />
				<el-tab-pane label="Hoàn thành" name="completed" />
				<el-tab-pane name="overdue">
					<template #label>
						<span class="text-danger">Quá hạn</span>
						<el-badge v-if="stats.overdue > 0" :value="stats.overdue" class="tab-badge" type="danger" />
					</template>
				</el-tab-pane>
			</el-tabs>

			<el-table
				:data="filteredFeedbacks"
				v-loading="loading"
				stripe
				style="width: 100%"
				@row-click="handleRowClick"
			>
				<el-table-column prop="code" label="Số PA" width="160" />
				<el-table-column prop="receivedDate" label="Ngày nhận" width="120">
					<template #default="{ row }">
						<div class="date-cell">
							{{ formatDate(row.receivedDate) }}
							<el-tag
								v-if="row.isOverdue"
								type="danger"
								size="small"
								effect="dark"
								class="overdue-tag"
							>
								Quá hạn
							</el-tag>
						</div>
					</template>
				</el-table-column>
				<el-table-column prop="content" label="Nội dung" min-width="250">
					<template #default="{ row }">
						{{ truncate(row.content, 60) }}
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng ban" width="140" />
				<el-table-column prop="level" label="Mức độ" width="110" align="center">
					<template #default="{ row }">
						<el-tag :type="getLevelType(row.level)" size="small">
							{{ getLevelLabel(row.level) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="" width="120" align="center">
					<template #default="{ row }">
						<el-button type="primary" @click.stop="openProcessDialog(row)">
							{{ row.status === 'COMPLETED' ? 'Xem' : 'Xử lý' }}
						</el-button>
					</template>
				</el-table-column>
			</el-table>

			<el-empty v-if="filteredFeedbacks.length === 0 && !loading" description="Không có phản ánh nào" />
		</div>

		<!-- Dialog Xử lý Phản ánh -->
		<el-dialog
			v-model="processDialogVisible"
			:title="currentFeedback?.status === 'COMPLETED' ? 'Chi tiết xử lý' : 'Xử lý Phản ánh'"
			width="1000px"
			top="3vh"
			draggable
			class="process-dialog"
			@close="resetProcessForm"
		>
			<div class="process-content" v-if="currentFeedback">
				<!-- Thông tin PA -->
				<div class="feedback-info-section">
					<h4 class="section-title">
						<el-icon><Document /></el-icon>
						Thông tin Phản ánh
					</h4>
					<el-descriptions :column="2" border>
						<el-descriptions-item label="Mã PA">
							<strong>{{ currentFeedback.code }}</strong>
						</el-descriptions-item>
						<el-descriptions-item label="Ngày nhận">
							{{ formatDate(currentFeedback.receivedDate) }}
						</el-descriptions-item>
						<el-descriptions-item label="Phòng ban">
							{{ currentFeedback.departmentName }}
						</el-descriptions-item>
						<el-descriptions-item label="Mức độ">
							<el-tag :type="getLevelType(currentFeedback.level)" size="small">
								{{ getLevelLabel(currentFeedback.level) }}
							</el-tag>
						</el-descriptions-item>
						<el-descriptions-item label="Nội dung" :span="2">
							{{ currentFeedback.content }}
						</el-descriptions-item>
					</el-descriptions>
				</div>

				<!-- Form xử lý (chỉ hiện khi chưa hoàn thành) -->
				<div v-if="currentFeedback.status !== 'COMPLETED'" class="process-form-section">
					<h4 class="section-title">
						<el-icon><Edit /></el-icon>
						Ghi nhận xử lý
					</h4>
					<el-form :model="processForm" ref="processFormRef" :rules="processRules" label-width="140px">
						<el-form-item label="Nội dung xử lý" prop="processContent">
							<el-input
								type="textarea"
								:rows="4"
								v-model="processForm.processContent"
								placeholder="Nhập nội dung xử lý chi tiết..."
								maxlength="1000"
								show-word-limit
							/>
						</el-form-item>
						<el-form-item label="Trạng thái" prop="newStatus">
							<el-radio-group v-model="processForm.newStatus">
								<el-radio value="PROCESSING">
									<el-tag type="warning" size="small">Đang xử lý</el-tag>
								</el-radio>
								<el-radio value="COMPLETED">
									<el-tag type="success" size="small">Hoàn thành</el-tag>
								</el-radio>
							</el-radio-group>
						</el-form-item>
						<el-form-item label="Ghi chú">
							<el-input
								type="textarea"
								:rows="2"
								v-model="processForm.note"
								placeholder="Ghi chú thêm (nếu có)..."
							/>
						</el-form-item>
						<el-form-item label="File đính kèm">
							<el-upload
								v-model:file-list="processForm.attachments"
								class="upload-area"
								action="#"
								:auto-upload="false"
								:limit="5"
								:on-exceed="handleExceed"
								:on-remove="handleRemove"
								:before-upload="beforeUpload"
								accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
								multiple
								drag
							>
								<div class="upload-dragger">
									<el-icon class="upload-icon"><Upload /></el-icon>
									<div class="upload-text">
										Kéo thả file vào đây hoặc <em>click để chọn</em>
									</div>
								</div>
								<template #tip>
									<div class="el-upload__tip">
										Hỗ trợ: JPG, PNG, PDF, DOC (tối đa 5 file, mỗi file ≤ 5MB)
									</div>
								</template>
							</el-upload>
						</el-form-item>
					</el-form>
				</div>

				<!-- Lịch sử xử lý -->
				<div class="process-history-section">
					<h4 class="section-title">
						<el-icon><List /></el-icon>
						Lịch sử xử lý
					</h4>
					<el-timeline v-if="processHistory.length > 0">
						<el-timeline-item
							v-for="(item, index) in processHistory"
							:key="index"
							:timestamp="item.timestamp"
							:type="getTimelineType(item.status)"
							:hollow="index !== 0"
							placement="top"
						>
							<div class="timeline-content">
								<div class="timeline-header">
									<span class="handler-name">{{ item.handlerName }}</span>
									<el-tag :type="getTimelineType(item.status)" size="small">
										{{ getStatusLabel(item.status) }}
									</el-tag>
								</div>
								<p class="timeline-text">{{ item.content }}</p>
								<div v-if="item.attachments?.length > 0" class="timeline-attachments">
									<el-link
										v-for="file in item.attachments"
										:key="file.name"
										type="primary"
										:icon="Paperclip"
									>
										{{ file.name }}
									</el-link>
								</div>
							</div>
						</el-timeline-item>
					</el-timeline>
					<el-empty v-else description="Chưa có lịch sử xử lý" :image-size="80" />
				</div>
			</div>

			<template #footer>
				<el-button @click="processDialogVisible = false">Đóng</el-button>
				<el-button
					v-if="currentFeedback?.status !== 'COMPLETED'"
					type="primary"
					:loading="submitting"
					@click="submitProcess"
				>
					Lưu xử lý
				</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
	Warning, Loading, CircleCheck, Clock, Document, Edit,
	List, Upload, Paperclip
} from '@element-plus/icons-vue'
import { formatDate, truncate, getLevelLabel, getLevelType } from '@/utils/helpers'
import feedbackService from '@/services/feedbackService'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('pending')
const feedbacks = ref([])

// Dialog state
const processDialogVisible = ref(false)
const currentFeedback = ref(null)
const processFormRef = ref(null)
const processHistory = ref([])

const processForm = reactive({
	processContent: '',
	newStatus: 'PROCESSING',
	note: '',
	attachments: []
})

const processRules = {
	processContent: [
		{ required: true, message: 'Vui lòng nhập nội dung xử lý', trigger: 'blur' }
	],
	newStatus: [
		{ required: true, message: 'Vui lòng chọn trạng thái', trigger: 'change' }
	]
}

const stats = reactive({
	pending: 0,
	processing: 0,
	completed: 0,
	overdue: 0
})

const filteredFeedbacks = computed(() => {
	switch (activeTab.value) {
		case 'pending':
			return feedbacks.value.filter(f => f.status === 'NEW')
		case 'processing':
			return feedbacks.value.filter(f => f.status === 'PROCESSING')
		case 'completed':
			return feedbacks.value.filter(f => f.status === 'COMPLETED')
		case 'overdue':
			return feedbacks.value.filter(f => f.isOverdue && f.status !== 'COMPLETED')
		default:
			return feedbacks.value
	}
})

const fetchData = async () => {
	loading.value = true
	try {
		feedbacks.value = await feedbackService.getMyFeedbacks()
	} catch (error) {
		// Demo data
		feedbacks.value = [
			{
				id: 1,
				code: 'PA-20251127-001',
				receivedDate: '2025-11-27',
				content: 'Phản ánh về thái độ phục vụ của nhân viên khoa Nội',
				departmentName: 'Nội khoa',
				level: 'HIGH',
				status: 'NEW',
				isOverdue: false
			},
			{
				id: 2,
				code: 'PA-20251124-005',
				receivedDate: '2025-11-24',
				content: 'Thời gian chờ khám quá lâu tại phòng khám da liễu',
				departmentName: 'Da liễu',
				level: 'CRITICAL',
				status: 'NEW',
				isOverdue: true
			},
			{
				id: 3,
				code: 'PA-20251126-003',
				receivedDate: '2025-11-26',
				content: 'Cơ sở vật chất phòng khám cần được nâng cấp',
				departmentName: 'Ngoại khoa',
				level: 'MEDIUM',
				status: 'PROCESSING',
				isOverdue: false
			},
			{
				id: 4,
				code: 'PA-20251120-002',
				receivedDate: '2025-11-20',
				content: 'Khen ngợi bác sĩ khoa Ngoại điều trị nhiệt tình',
				departmentName: 'Ngoại khoa',
				level: 'LOW',
				status: 'COMPLETED',
				isOverdue: false
			}
		]
	} finally {
		loading.value = false
		updateStats()
	}
}

const updateStats = () => {
	stats.pending = feedbacks.value.filter(f => f.status === 'NEW').length
	stats.processing = feedbacks.value.filter(f => f.status === 'PROCESSING').length
	stats.completed = feedbacks.value.filter(f => f.status === 'COMPLETED').length
	stats.overdue = feedbacks.value.filter(f => f.isOverdue && f.status !== 'COMPLETED').length
}

const handleTabChange = () => {
	// Tab changed
}

const handleRowClick = (row) => {
	openProcessDialog(row)
}

// Dialog functions
const openProcessDialog = async (feedback) => {
	currentFeedback.value = { ...feedback }
	processDialogVisible.value = true

	// Fetch process history
	try {
		processHistory.value = await feedbackService.getProcessHistory(feedback.id)
	} catch (error) {
		// Mock history data
		if (feedback.status === 'PROCESSING') {
			processHistory.value = [
				{
					timestamp: '27/11/2025 10:30',
					handlerName: 'Nguyễn Văn A',
					status: 'PROCESSING',
					content: 'Đã tiếp nhận phản ánh và đang liên hệ phòng ban liên quan để xác minh.',
					attachments: []
				}
			]
		} else if (feedback.status === 'COMPLETED') {
			processHistory.value = [
				{
					timestamp: '20/11/2025 09:00',
					handlerName: 'Admin',
					status: 'NEW',
					content: 'Tiếp nhận phản ánh từ khách hàng.',
					attachments: []
				},
				{
					timestamp: '21/11/2025 14:30',
					handlerName: 'Nguyễn Văn A',
					status: 'PROCESSING',
					content: 'Đã liên hệ phòng ban và xác minh thông tin phản ánh.',
					attachments: []
				},
				{
					timestamp: '22/11/2025 16:00',
					handlerName: 'Nguyễn Văn A',
					status: 'COMPLETED',
					content: 'Đã xử lý xong. Phản ánh được ghi nhận và chuyển đến lãnh đạo phòng ban.',
					attachments: [{ name: 'bien_ban_xu_ly.pdf' }]
				}
			]
		} else {
			processHistory.value = []
		}
	}
}

const resetProcessForm = () => {
	processForm.processContent = ''
	processForm.newStatus = 'PROCESSING'
	processForm.note = ''
	processForm.attachments = []
	currentFeedback.value = null
	processHistory.value = []
}

const submitProcess = async () => {
	if (!processFormRef.value) return

	await processFormRef.value.validate(async (valid) => {
		if (valid) {
			submitting.value = true
			try {
				await feedbackService.updateProcessing(currentFeedback.value.id, {
					content: processForm.processContent,
					status: processForm.newStatus,
					note: processForm.note,
					attachments: processForm.attachments
				})

				// Update local data
				const index = feedbacks.value.findIndex(f => f.id === currentFeedback.value.id)
				if (index !== -1) {
					feedbacks.value[index].status = processForm.newStatus
				}
				updateStats()

				ElMessage.success('Cập nhật xử lý thành công!')
				processDialogVisible.value = false
			} catch (error) {
				// Demo mode - update locally
				const index = feedbacks.value.findIndex(f => f.id === currentFeedback.value.id)
				if (index !== -1) {
					feedbacks.value[index].status = processForm.newStatus
				}
				updateStats()

				ElMessage.success('Cập nhật xử lý thành công! (Demo)')
				processDialogVisible.value = false
			} finally {
				submitting.value = false
			}
		}
	})
}

const getTimelineType = (status) => {
	switch (status) {
		case 'COMPLETED': return 'success'
		case 'PROCESSING': return 'warning'
		case 'NEW': return 'primary'
		default: return 'info'
	}
}

const getStatusLabel = (status) => {
	switch (status) {
		case 'COMPLETED': return 'Hoàn thành'
		case 'PROCESSING': return 'Đang xử lý'
		case 'NEW': return 'Tiếp nhận'
		default: return status
	}
}

// Upload handlers
const handleExceed = (files) => {
	ElMessage.warning(`Chỉ được upload tối đa 5 file. Bạn đã chọn ${files.length} file.`)
}

const handleRemove = (file, fileList) => {
	console.log('File removed:', file.name)
}

const beforeUpload = (file) => {
	const isLt5M = file.size / 1024 / 1024 < 5
	if (!isLt5M) {
		ElMessage.error('File không được vượt quá 5MB!')
		return false
	}
	return true
}

onMounted(() => {
	fetchData()
})
</script>

<style scoped>
.tab-badge {
	margin-left: 8px;
}

.date-cell {
	display: flex;
	flex-direction: column;
	gap: 4px;
}

.overdue-tag {
	width: fit-content;
}

.text-danger {
	color: var(--danger-color);
}

:deep(.el-tabs__item) {
	display: flex;
	align-items: center;
}

/* Process Dialog Styles */
:deep(.process-dialog) {
	max-width: 95vw;
}

:deep(.process-dialog .el-dialog__body) {
	max-height: calc(94vh - 140px);
	overflow-y: auto;
	padding: 20px 24px;
}

:deep(.process-dialog .el-dialog__header) {
	cursor: move;
	padding: 16px 24px;
	border-bottom: 1px solid var(--border-color);
	margin-right: 0;
}

:deep(.process-dialog .el-dialog__footer) {
	padding: 16px 24px;
	border-top: 1px solid var(--border-color);
}

.process-content {
	display: flex;
	flex-direction: column;
	gap: 20px;
}

.section-title {
	display: flex;
	align-items: center;
	gap: 8px;
	font-size: 15px;
	font-weight: 600;
	color: var(--secondary-color);
	margin-bottom: 16px;
	padding-bottom: 10px;
	border-bottom: 2px solid var(--primary-color);
}

.section-title .el-icon {
	color: var(--primary-color);
}

.feedback-info-section :deep(.el-descriptions__label) {
	width: 100px;
	font-weight: 600;
}

.process-form-section {
	background: #f8fafc;
	padding: 20px;
	border-radius: var(--radius-md);
	border: 1px solid var(--border-color);
}

/* Upload Area - Drag & Drop */
.upload-area {
	width: 100%;
}

.upload-area :deep(.el-upload) {
	width: 100%;
}

.upload-area :deep(.el-upload-dragger) {
	width: 100%;
	padding: 30px 20px;
	border: 2px dashed var(--border-color);
	border-radius: var(--radius-md);
	background: #fafbfc;
	transition: all 0.3s ease;
}

.upload-area :deep(.el-upload-dragger:hover) {
	border-color: var(--primary-color);
	background: rgba(0, 180, 216, 0.05);
}

.upload-area :deep(.el-upload-dragger.is-dragover) {
	border-color: var(--primary-color);
	background: rgba(0, 180, 216, 0.1);
}

.upload-dragger {
	text-align: center;
}

.upload-icon {
	font-size: 40px;
	color: #c0c4cc;
	margin-bottom: 12px;
}

.upload-text {
	color: #606266;
	font-size: 14px;
}

.upload-text em {
	color: var(--primary-color);
	font-style: normal;
}

.upload-area :deep(.el-upload-list) {
	margin-top: 12px;
}

.upload-area :deep(.el-upload-list__item) {
	border: 1px solid var(--border-color);
	border-radius: var(--radius-sm);
	padding: 8px 12px;
	margin-top: 8px;
}

.process-history-section {
	/* Không giới hạn chiều cao - sử dụng chung scroll với dialog body */
}

.timeline-content {
	background: white;
	padding: 12px 16px;
	border-radius: var(--radius-sm);
	border: 1px solid var(--border-color);
	box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.timeline-header {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 8px;
}

.handler-name {
	font-weight: 600;
	color: var(--secondary-color);
}

.timeline-text {
	color: var(--text-secondary);
	margin: 0;
	line-height: 1.5;
}

.timeline-attachments {
	margin-top: 8px;
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
}

/* Dialog responsive */
@media (max-width: 1024px) {
	:deep(.process-dialog) {
		width: 95% !important;
	}
}

@media (max-width: 768px) {
	:deep(.process-dialog) {
		width: 98% !important;
		margin: 0 auto;
	}

	:deep(.process-dialog .el-dialog__body) {
		padding: 16px;
		max-height: calc(94vh - 120px);
	}

	.feedback-info-section :deep(.el-descriptions) {
		--el-descriptions-table-border: none;
	}

	.feedback-info-section :deep(.el-descriptions__cell) {
		display: block;
		padding: 8px 0;
	}

	.upload-area :deep(.el-upload-dragger) {
		padding: 20px 15px;
	}

	.upload-icon {
		font-size: 32px;
	}
}
</style>


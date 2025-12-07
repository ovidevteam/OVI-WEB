<template>
	<div class="feedback-create">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Nhập Phản ánh mới</h3>
			</div>

			<el-form
				ref="formRef"
				:model="form"
				:rules="rules"
				:label-width="isMobile ? 'auto' : '140px'"
				:label-position="isMobile ? 'top' : 'left'"
				class="feedback-form"
			>
				<!-- Basic Info -->
				<div class="form-section">
					<h4 class="form-section-title">
						<el-icon><Document /></el-icon>
						Thông tin phản ánh
					</h4>

					<el-row :gutter="20">
						<el-col :xs="24" :sm="12">
							<el-form-item label="Kênh tiếp nhận" prop="channel">
								<el-select v-model="form.channel" placeholder="Chọn kênh" style="width: 100%">
									<el-option
										v-for="c in CHANNELS"
										:key="c.value"
										:label="c.label"
										:value="c.value"
									/>
								</el-select>
							</el-form-item>
						</el-col>
						<el-col :xs="24" :sm="12">
							<el-form-item label="Mức độ" prop="level">
								<el-radio-group v-model="form.level" class="level-group">
									<el-radio-button
										v-for="l in LEVELS"
										:key="l.value"
										:value="l.value"
									>
										{{ l.label }}
									</el-radio-button>
								</el-radio-group>
							</el-form-item>
						</el-col>
					</el-row>

					<el-row :gutter="20">
						<el-col :xs="24" :sm="12">
							<el-form-item label="Phòng liên quan" prop="departmentId">
								<el-select
									v-model="form.departmentId"
									placeholder="Chọn phòng ban"
									style="width: 100%"
									@change="handleDepartmentChange"
								>
									<el-option
										v-for="d in departments"
										:key="d.id"
										:label="d.name"
										:value="d.id"
									/>
								</el-select>
							</el-form-item>
						</el-col>
						<el-col :xs="24" :sm="12">
							<el-form-item label="Bác sĩ liên quan">
								<el-select
									v-model="form.doctorId"
									placeholder="Chọn bác sĩ (nếu có)"
									style="width: 100%"
									clearable
								>
									<el-option
										v-for="d in doctors"
										:key="d.id"
										:label="d.fullName"
										:value="d.id"
									/>
								</el-select>
							</el-form-item>
						</el-col>
					</el-row>

					<el-form-item label="Nội dung" prop="content">
						<el-input
							v-model="form.content"
							type="textarea"
							:rows="5"
							placeholder="Nhập nội dung phản ánh chi tiết..."
							maxlength="1000"
							show-word-limit
						/>
					</el-form-item>

					<el-form-item label="Ghi chú">
						<el-input
							v-model="form.note"
							type="textarea"
							:rows="2"
							placeholder="Ghi chú thêm (nếu có)..."
						/>
					</el-form-item>
				</div>

				<!-- Images -->
				<div class="form-section">
					<h4 class="form-section-title">
						<el-icon><Picture /></el-icon>
						Hình ảnh đính kèm
					</h4>

					<ImageUpload
						v-model="form.images"
						:max="10"
						tip="Tối đa 10 ảnh, mỗi ảnh không quá 5MB"
					/>
				</div>

				<!-- Actions -->
				<div class="form-actions">
					<el-button @click="goBack" size="large">Hủy</el-button>
					<el-button type="primary" :loading="loading" @click="handleSubmit" size="large">
						Lưu phản ánh
					</el-button>
				</div>
			</el-form>
		</div>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Picture } from '@element-plus/icons-vue'
import { CHANNELS, LEVELS } from '@/utils/constants'
import feedbackService from '@/services/feedbackService'
import departmentService from '@/services/departmentService'
import doctorService from '@/services/doctorService'
import uploadService from '@/services/uploadService'
import ImageUpload from '@/components/upload/ImageUpload.vue'
import { handleApiError } from '@/utils/errorHandler'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const departments = ref([])
const doctors = ref([])
const windowWidth = ref(window.innerWidth)

const isMobile = computed(() => windowWidth.value < 768)

const form = reactive({
	channel: 'HOTLINE',
	level: 'MEDIUM',
	departmentId: null,
	doctorId: null,
	content: '',
	note: '',
	images: []
})

const rules = {
	channel: [
		{ required: true, message: 'Vui lòng chọn kênh tiếp nhận', trigger: 'change' }
	],
	level: [
		{ required: true, message: 'Vui lòng chọn mức độ', trigger: 'change' }
	],
	departmentId: [
		{ required: true, message: 'Vui lòng chọn phòng ban', trigger: 'change' }
	],
	content: [
		{ required: true, message: 'Vui lòng nhập nội dung phản ánh', trigger: 'blur' },
		{ max: 1000, message: 'Nội dung không được vượt quá 1000 ký tự', trigger: 'blur' }
	]
}

const handleResize = () => {
	windowWidth.value = window.innerWidth
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
				{ id: 4, name: 'Sản khoa' }
			]
		} else {
			// Silently fail for department fetch - not critical
		}
	}
}

const handleDepartmentChange = async (departmentId) => {
	form.doctorId = null
	if (departmentId) {
		try {
			const response = await doctorService.getByDepartment(departmentId)
			// Handle both array and paginated response
			if (Array.isArray(response)) {
				doctors.value = response
			} else if (response && response.data) {
				doctors.value = response.data
			} else {
				doctors.value = []
			}
		} catch (error) {
			if (DEMO_MODE) {
				// Demo data - only in demo mode
				doctors.value = [
					{ id: 1, fullName: 'BS. Nguyễn Văn A' },
					{ id: 2, fullName: 'BS. Trần Thị B' }
				]
			} else {
				// Silently fail for doctor fetch - not critical
				doctors.value = []
			}
		}
	} else {
		doctors.value = []
	}
}

const handleSubmit = async () => {
	if (!formRef.value) return

	try {
		await formRef.value.validate()
		loading.value = true

		// Prepare feedback data (without images)
		// Note: Backend CreateFeedbackRequest doesn't have 'note' field
		// Map frontend enum values to backend enum values
		const mapChannelToBackend = (channel) => {
			const mapping = {
				'HOTLINE': 'PHONE',
				'EMAIL': 'EMAIL',
				'DIRECT': 'DIRECT',
				'ZALO': 'PHONE', // Map to PHONE
				'FACEBOOK': 'PHONE', // Map to PHONE
				'OTHER': 'PHONE' // Map to PHONE
			}
			return mapping[channel] || 'PHONE'
		}

		const mapLevelToBackend = (level) => {
			const mapping = {
				'CRITICAL': 'HIGH', // Backend doesn't have CRITICAL, map to HIGH
				'HIGH': 'HIGH',
				'MEDIUM': 'MEDIUM',
				'LOW': 'LOW'
			}
			return mapping[level] || 'MEDIUM'
		}

		const feedbackData = {
			channel: mapChannelToBackend(form.channel),
			level: mapLevelToBackend(form.level),
			departmentId: form.departmentId,
			doctorId: form.doctorId || null,
			content: form.content.trim()
		}

		// Create feedback first
		const response = await feedbackService.create(feedbackData)
		const feedback = response?.data || response
		const feedbackId = feedback?.id

		if (!feedbackId) {
			throw new Error('Không thể lấy ID phản ánh sau khi tạo')
		}

		// Upload images if any
		if (form.images && form.images.length > 0) {
			// Filter out already uploaded images (those with id or url)
			const filesToUpload = form.images.filter(img => {
				// If it's a File object (raw), it needs to be uploaded
				return img instanceof File || (img.raw && img.raw instanceof File)
			})

			if (filesToUpload.length > 0) {
				// Extract File objects
				const files = filesToUpload.map(img => img.raw || img).filter(f => f instanceof File)
				
				if (files.length > 0) {
					try {
						await uploadService.uploadFeedbackImages(feedbackId, files)
					} catch (uploadError) {
						// Log error but don't fail the whole operation
						console.error('Error uploading images:', uploadError)
						ElMessage.warning('Phản ánh đã được tạo nhưng có lỗi khi upload ảnh')
					}
				}
			}
		}

		ElMessage.success('Tạo phản ánh thành công!')
		
		// Refresh sidebar stats to update badge counts
		window.dispatchEvent(new CustomEvent('refresh-feedback-stats'))
		
		router.push('/feedback')
	} catch (error) {
		if (error !== false) {
			handleApiError(error, 'Create Feedback')
		}
	} finally {
		loading.value = false
	}
}

const goBack = () => {
	router.back()
}

onMounted(() => {
	fetchDepartments()
	window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
	window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.feedback-create {
	width: 100%;
}

.feedback-form {
	width: 100%;
}

.form-section {
	margin-bottom: 32px;
}

.form-section-title {
	display: flex;
	align-items: center;
	gap: 8px;
	font-size: 16px;
	font-weight: 600;
	color: var(--secondary-color);
	margin-bottom: 20px;
	padding-bottom: 12px;
	border-bottom: 2px solid var(--primary-color);
}

.form-section-title .el-icon {
	color: var(--primary-color);
}

.level-group {
	width: 100%;
}

.level-group :deep(.el-radio-button) {
	flex: 1;
}

.level-group :deep(.el-radio-button__inner) {
	width: 100%;
}

.form-actions {
	margin-top: 32px;
	padding-top: 24px;
	border-top: 1px solid var(--border-color);
	display: flex;
	justify-content: flex-end;
	gap: 12px;
}

/* Responsive */
@media (max-width: 768px) {
	.form-section-title {
		font-size: 15px;
	}

	.form-actions {
		flex-direction: column-reverse;
	}

	.form-actions .el-button {
		width: 100%;
	}

	.level-group {
		display: flex;
		flex-wrap: wrap;
	}

	.level-group :deep(.el-radio-button) {
		flex: 1 1 auto;
		min-width: 80px;
	}
}

@media (max-width: 480px) {
	.form-section {
		margin-bottom: 24px;
	}

	.form-section-title {
		font-size: 14px;
		margin-bottom: 16px;
		padding-bottom: 10px;
	}
}
</style>

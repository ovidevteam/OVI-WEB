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
import ImageUpload from '@/components/upload/ImageUpload.vue'

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
		departments.value = [
			{ id: 1, name: 'Nội khoa' },
			{ id: 2, name: 'Ngoại khoa' },
			{ id: 3, name: 'Da liễu' },
			{ id: 4, name: 'Sản khoa' }
		]
	}
}

const handleDepartmentChange = async (departmentId) => {
	form.doctorId = null
	if (departmentId) {
		try {
			doctors.value = await doctorService.getByDepartment(departmentId)
		} catch (error) {
			doctors.value = [
				{ id: 1, fullName: 'BS. Nguyễn Văn A' },
				{ id: 2, fullName: 'BS. Trần Thị B' }
			]
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

		await feedbackService.create(form)
		ElMessage.success('Tạo phản ánh thành công!')
		router.push('/feedback')
	} catch (error) {
		if (error !== false) {
			ElMessage.error('Có lỗi xảy ra. Vui lòng thử lại.')
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

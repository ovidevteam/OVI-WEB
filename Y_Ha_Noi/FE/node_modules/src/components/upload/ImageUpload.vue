<template>
	<div class="image-upload">
		<el-upload
			v-model:file-list="fileList"
			:action="uploadUrl"
			list-type="picture-card"
			:multiple="true"
			:limit="max"
			:accept="acceptTypes"
			:before-upload="beforeUpload"
			:on-preview="handlePreview"
			:on-remove="handleRemove"
			:on-exceed="handleExceed"
			:auto-upload="false"
		>
			<el-icon><Plus /></el-icon>
			<template #tip>
				<div class="upload-tip">{{ tip }}</div>
			</template>
		</el-upload>

		<el-dialog v-model="previewVisible" title="Xem ảnh" width="800px">
			<img :src="previewUrl" alt="Preview" class="preview-image" />
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { UPLOAD_CONFIG } from '@/utils/constants'
import { formatFileSize } from '@/utils/helpers'

const props = defineProps({
	modelValue: {
		type: Array,
		default: () => []
	},
	max: {
		type: Number,
		default: 10
	},
	tip: {
		type: String,
		default: 'Tối đa 10 ảnh, mỗi ảnh không quá 5MB'
	}
})

const emit = defineEmits(['update:modelValue'])

const fileList = ref([])
const previewVisible = ref(false)
const previewUrl = ref('')

const uploadUrl = computed(() => {
	return import.meta.env.VITE_API_BASE_URL + '/upload/images'
})

const acceptTypes = computed(() => {
	return UPLOAD_CONFIG.acceptExtensions.join(',')
})

watch(() => props.modelValue, (newVal) => {
	if (newVal.length !== fileList.value.length) {
		fileList.value = newVal.map(img => ({
			name: img.name || 'image',
			url: img.url || img
		}))
	}
}, { immediate: true })

watch(fileList, (newVal) => {
	emit('update:modelValue', newVal)
}, { deep: true })

const beforeUpload = (file) => {
	const isValidType = UPLOAD_CONFIG.acceptTypes.includes(file.type)
	const isValidSize = file.size <= UPLOAD_CONFIG.maxSize

	if (!isValidType) {
		ElMessage.error('Chỉ chấp nhận file ảnh JPG, PNG, WEBP!')
		return false
	}

	if (!isValidSize) {
		ElMessage.error(`Kích thước file không được vượt quá ${formatFileSize(UPLOAD_CONFIG.maxSize)}!`)
		return false
	}

	return true
}

const handlePreview = (file) => {
	previewUrl.value = file.url
	previewVisible.value = true
}

const handleRemove = (file, files) => {
	// File removed
}

const handleExceed = () => {
	ElMessage.warning(`Chỉ được upload tối đa ${props.max} ảnh!`)
}
</script>

<style scoped>
.image-upload {
	width: 100%;
}

.upload-tip {
	font-size: 12px;
	color: var(--text-muted);
	margin-top: 8px;
}

.preview-image {
	width: 100%;
	max-height: 600px;
	object-fit: contain;
}

:deep(.el-upload-list__item) {
	transition: all 0.3s ease;
}

:deep(.el-upload--picture-card) {
	--el-upload-picture-card-size: 100px;
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
	width: 100px;
	height: 100px;
}
</style>


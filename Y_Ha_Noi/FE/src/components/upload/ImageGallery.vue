<template>
	<div class="image-gallery">
		<div class="gallery-grid">
			<div
				v-for="(image, index) in images"
				:key="image.id || index"
				class="gallery-item"
				@click="openLightbox(index)"
			>
				<el-image
					:src="image.url || image"
					:alt="image.name || `Image ${index + 1}`"
					fit="cover"
					lazy
				>
					<template #placeholder>
						<div class="image-placeholder">
							<el-icon class="is-loading"><Loading /></el-icon>
						</div>
					</template>
					<template #error>
						<div class="image-error">
							<el-icon><Picture /></el-icon>
						</div>
					</template>
				</el-image>
				<div class="gallery-overlay">
					<el-icon><ZoomIn /></el-icon>
				</div>
			</div>
		</div>

		<!-- Lightbox -->
		<el-dialog
			v-model="lightboxVisible"
			:show-close="false"
			width="90%"
			class="lightbox-dialog"
			@close="closeLightbox"
		>
			<div class="lightbox-content" @click.stop>
				<img
					:src="currentImage?.url || currentImage"
					:alt="currentImage?.name || 'Image'"
					class="lightbox-image"
				/>

				<!-- Navigation -->
				<div class="lightbox-nav">
					<el-button
						class="nav-btn prev"
						circle
						:icon="ArrowLeft"
						:disabled="currentIndex === 0"
						@click="prevImage"
					/>
					<el-button
						class="nav-btn next"
						circle
						:icon="ArrowRight"
						:disabled="currentIndex === images.length - 1"
						@click="nextImage"
					/>
				</div>

				<!-- Info -->
				<div class="lightbox-info">
					<span>{{ currentIndex + 1 }} / {{ images.length }}</span>
				</div>

				<!-- Close -->
				<el-button
					class="close-btn"
					circle
					:icon="Close"
					@click="closeLightbox"
				/>
			</div>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Loading, Picture, ZoomIn, ArrowLeft, ArrowRight, Close } from '@element-plus/icons-vue'

const props = defineProps({
	images: {
		type: Array,
		default: () => []
	}
})

const lightboxVisible = ref(false)
const currentIndex = ref(0)

const currentImage = computed(() => {
	return props.images[currentIndex.value]
})

const openLightbox = (index) => {
	currentIndex.value = index
	lightboxVisible.value = true
}

const closeLightbox = () => {
	lightboxVisible.value = false
}

const prevImage = () => {
	if (currentIndex.value > 0) {
		currentIndex.value--
	}
}

const nextImage = () => {
	if (currentIndex.value < props.images.length - 1) {
		currentIndex.value++
	}
}
</script>

<style scoped>
.gallery-grid {
	display: grid;
	grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
	gap: 12px;
}

.gallery-item {
	position: relative;
	aspect-ratio: 1;
	border-radius: var(--radius-md);
	overflow: hidden;
	cursor: pointer;
	background: var(--background-color);
}

.gallery-item :deep(.el-image) {
	width: 100%;
	height: 100%;
}

.gallery-overlay {
	position: absolute;
	inset: 0;
	background: rgba(0, 0, 0, 0.4);
	display: flex;
	align-items: center;
	justify-content: center;
	opacity: 0;
	transition: opacity 0.2s ease;
	color: white;
	font-size: 24px;
}

.gallery-item:hover .gallery-overlay {
	opacity: 1;
}

.image-placeholder,
.image-error {
	width: 100%;
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
	color: var(--text-muted);
	font-size: 24px;
	background: var(--background-color);
}

/* Lightbox */
:deep(.lightbox-dialog) {
	--el-dialog-bg-color: transparent;
	--el-dialog-box-shadow: none;
}

:deep(.lightbox-dialog .el-dialog__header) {
	display: none;
}

:deep(.lightbox-dialog .el-dialog__body) {
	padding: 0;
}

.lightbox-content {
	position: relative;
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 80vh;
}

.lightbox-image {
	max-width: 100%;
	max-height: 80vh;
	object-fit: contain;
	border-radius: var(--radius-md);
}

.lightbox-nav {
	position: absolute;
	width: 100%;
	display: flex;
	justify-content: space-between;
	padding: 0 20px;
	pointer-events: none;
}

.nav-btn {
	pointer-events: auto;
	background: rgba(255, 255, 255, 0.9) !important;
	width: 48px;
	height: 48px;
}

.lightbox-info {
	position: absolute;
	bottom: 20px;
	left: 50%;
	transform: translateX(-50%);
	background: rgba(0, 0, 0, 0.6);
	color: white;
	padding: 8px 16px;
	border-radius: 20px;
	font-size: 14px;
}

.close-btn {
	position: absolute;
	top: 20px;
	right: 20px;
	background: rgba(255, 255, 255, 0.9) !important;
	width: 40px;
	height: 40px;
}
</style>


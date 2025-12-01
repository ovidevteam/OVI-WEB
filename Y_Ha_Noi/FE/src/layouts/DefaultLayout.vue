<template>
	<div class="layout-container">
		<!-- Sidebar -->
		<Sidebar />

		<!-- Main Content -->
		<div class="main-content" :class="{
			collapsed: uiStore.sidebarCollapsed,
			'is-mobile': uiStore.isMobile
		}">
			<!-- Header -->
			<Header />

			<!-- Page Content -->
			<main class="page-container">
				<router-view v-slot="{ Component }">
					<transition name="fade" mode="out-in">
						<component :is="Component" />
					</transition>
				</router-view>
			</main>

			<!-- Footer -->
			<Footer />
		</div>
	</div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useUIStore } from '@/stores/ui'
import Header from '@/components/common/Header.vue'
import Sidebar from '@/components/common/Sidebar.vue'
import Footer from '@/components/common/Footer.vue'

const uiStore = useUIStore()

const handleResize = () => {
	// Set mobile at 768px for sidebar hidden
	uiStore.setMobile(window.innerWidth < 768)
}

onMounted(() => {
	handleResize()
	window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
	window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.fade-enter-active,
.fade-leave-active {
	transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
	opacity: 0;
}
</style>


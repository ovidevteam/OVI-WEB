<template>
	<!-- Mobile Overlay -->
	<transition name="fade">
		<div
			v-if="uiStore.isMobile && !uiStore.sidebarCollapsed"
			class="sidebar-overlay"
			@click="uiStore.collapseSidebar"
		></div>
	</transition>

	<aside class="sidebar" :class="{
		collapsed: uiStore.sidebarCollapsed,
		'mobile-open': uiStore.isMobile && !uiStore.sidebarCollapsed
	}">
		<!-- Logo -->
		<div class="sidebar-logo">
			<div class="logo-icon">
				<!-- Logo vuông tùy chỉnh (khi có file logo-square.png) -->
				<img
					v-if="hasSquareLogo"
					src="/images/logo-square.png"
					alt="Logo"
					class="logo-img"
				/>
				<!-- Fallback icon nếu chưa có logo -->
				<el-icon v-else :size="28"><FirstAidKit /></el-icon>
			</div>
			<transition name="fade">
				<div v-show="!uiStore.sidebarCollapsed" class="logo-text">
					<span class="logo-title">Bệnh viện ĐH Y Hà Nội</span>
					<span class="logo-subtitle">Quản lý Phản ánh</span>
				</div>
			</transition>
		</div>

		<!-- Menu -->
		<el-scrollbar class="sidebar-menu-wrapper">
			<el-menu
				:default-active="activeMenu"
				:collapse="uiStore.sidebarCollapsed"
				:collapse-transition="false"
				router
				class="sidebar-menu"
				role="navigation"
				aria-label="Menu điều hướng chính"
			>
			<!-- Dashboard -->
			<el-menu-item index="/dashboard" v-if="authStore.isAdmin || authStore.isLeader" aria-label="Trang tổng quan">
				<el-icon><Odometer /></el-icon>
				<template #title>Dashboard</template>
			</el-menu-item>

				<!-- Feedback -->
				<el-sub-menu index="feedback" v-if="canViewFeedback" class="menu-with-badge">
					<template #title>
						<el-icon><ChatDotRound /></el-icon>
						<span>Phản ánh</span>
						<el-badge
							v-if="feedbackStats.total > 0"
							:value="feedbackStats.total"
							:max="99"
							class="menu-badge-parent"
						/>
					</template>
				<el-menu-item index="/feedback">
					<el-icon><List /></el-icon>
					<span>Danh sách</span>
					<el-badge
						v-if="feedbackStats.pending > 0"
						:value="feedbackStats.pending"
						:max="99"
						class="menu-badge"
					/>
				</el-menu-item>
				<el-menu-item index="/feedback/ratings" v-if="canRateFeedback">
					<el-icon><Star /></el-icon>
					<span>Đánh giá</span>
					<el-badge
						v-if="feedbackStats.needRating > 0"
						:value="feedbackStats.needRating"
						:max="99"
						class="menu-badge"
					/>
				</el-menu-item>
				<el-menu-item index="/feedback/create" v-if="canCreateFeedback">
						<el-icon><Plus /></el-icon>
						Nhập mới
					</el-menu-item>
					<el-menu-item index="/my-feedbacks" v-if="authStore.isHandler">
						<el-icon><User /></el-icon>
						<span>Của tôi</span>
						<el-badge
							v-if="feedbackStats.myPending > 0"
							:value="feedbackStats.myPending"
							:max="99"
							class="menu-badge"
						/>
					</el-menu-item>
				</el-sub-menu>

				<!-- Admin -->
				<el-sub-menu index="admin" v-if="authStore.isAdmin">
					<template #title>
						<el-icon><Setting /></el-icon>
						<span>Quản trị</span>
					</template>
					<el-menu-item index="/admin/users">
						<el-icon><UserFilled /></el-icon>
						Quản lý User
					</el-menu-item>
					<el-menu-item index="/admin/departments">
						<el-icon><OfficeBuilding /></el-icon>
						Phòng ban
					</el-menu-item>
					<el-menu-item index="/admin/doctors">
						<el-icon><FirstAidKit /></el-icon>
						Bác sĩ
					</el-menu-item>
				</el-sub-menu>

				<!-- Reports -->
				<el-sub-menu index="reports" v-if="canViewReports">
					<template #title>
						<el-icon><DataAnalysis /></el-icon>
						<span>Báo cáo</span>
					</template>
					<el-menu-item index="/reports/by-department">
						<el-icon><OfficeBuilding /></el-icon>
						Theo Phòng
					</el-menu-item>
					<el-menu-item index="/reports/by-doctor">
						<el-icon><TrendCharts /></el-icon>
						Theo Bác sĩ
					</el-menu-item>
					<el-menu-item index="/reports/with-images">
						<el-icon><Picture /></el-icon>
						Có Hình ảnh
					</el-menu-item>
				</el-sub-menu>
			</el-menu>
		</el-scrollbar>

		<!-- Sidebar Footer -->
		<div class="sidebar-footer">
			<transition name="fade">
				<div v-show="!uiStore.sidebarCollapsed" class="version-info">
					<span>Version 1.4.0</span>
				</div>
			</transition>
		</div>
	</aside>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useUIStore } from '@/stores/ui'
import { useFeedbackStats } from '@/composables/useFeedbackStats'
import {
	Odometer, ChatDotRound, List, Plus, User, Setting, UserFilled,
	OfficeBuilding, FirstAidKit, DataAnalysis, TrendCharts, Picture, Star
} from '@element-plus/icons-vue'

const route = useRoute()
const authStore = useAuthStore()
const uiStore = useUIStore()

// Use composable for feedback stats
const { feedbackStats, fetchFeedbackStats } = useFeedbackStats()

let statsInterval = null

// Listen for refresh events from other components
const handleRefreshStats = () => {
	fetchFeedbackStats()
}

onMounted(() => {
	fetchFeedbackStats()
	// Refresh stats periodically (every 30 seconds)
	statsInterval = setInterval(fetchFeedbackStats, 30000)
	// Listen for custom events to refresh stats immediately
	window.addEventListener('refresh-feedback-stats', handleRefreshStats)
})

onBeforeUnmount(() => {
	if (statsInterval) {
		clearInterval(statsInterval)
	}
	window.removeEventListener('refresh-feedback-stats', handleRefreshStats)
})

const hasSquareLogo = ref(true) // Logo vuông đã được cung cấp

const activeMenu = computed(() => route.path)

const canViewFeedback = computed(() => true)
const canCreateFeedback = computed(() => authStore.hasRole(['ADMIN', 'RECEIVER']))
const canRateFeedback = computed(() => authStore.hasRole(['ADMIN', 'LEADER', 'HANDLER']))
const canViewReports = computed(() => authStore.hasRole(['ADMIN', 'LEADER']))
</script>

<style scoped>
/* Sidebar màu trắng/xám nhạt - khác biệt với logo */
.sidebar {
	position: fixed;
	left: 0;
	top: 0;
	height: 100vh;
	width: var(--sidebar-width);
	background: linear-gradient(180deg, #ffffff 0%, #f1f5f9 100%);
	backdrop-filter: blur(12px);
	-webkit-backdrop-filter: blur(12px);
	display: flex;
	flex-direction: column;
	transition: width var(--transition-normal);
	z-index: 200;
	overflow: hidden;
	border-right: 1px solid #e2e8f0;
	box-shadow: 4px 0 24px rgba(0, 0, 0, 0.08);
}

.sidebar::before {
	content: '';
	position: absolute;
	inset: 0;
	background:
		radial-gradient(ellipse at 0% 100%, rgba(59, 130, 246, 0.05) 0%, transparent 50%),
		radial-gradient(ellipse at 100% 0%, rgba(6, 182, 212, 0.05) 0%, transparent 50%);
	pointer-events: none;
}

.sidebar.collapsed {
	width: var(--sidebar-collapsed-width);
}

/* Overlay cho mobile */
.sidebar-overlay {
	position: fixed;
	inset: 0;
	background: rgba(0, 0, 0, 0.5);
	z-index: 199;
	backdrop-filter: blur(2px);
}

/* Logo - cùng màu với sidebar */
.sidebar-logo {
	height: var(--header-height);
	display: flex;
	align-items: center;
	padding: 0 12px;
	gap: 12px;
	border-bottom: 1px solid #e2e8f0;
	flex-shrink: 0;
	overflow: hidden;
	background: transparent;
}

/* Khi collapsed - căn giữa logo */
.sidebar.collapsed .sidebar-logo {
	padding: 0 12px;
}

.logo-icon {
	width: 40px;
	height: 40px;
	min-width: 40px;
	/* Trùng màu với login-logo */
	background: linear-gradient(135deg, #1a365d 0%, #2c5282 100%);
	border-radius: var(--radius-md);
	display: flex;
	align-items: center;
	justify-content: center;
	color: white;
	flex-shrink: 0;
	overflow: hidden;
}

.logo-img {
	width: 100%;
	height: 100%;
	object-fit: cover;
}

.logo-text {
	display: flex;
	flex-direction: column;
	overflow: hidden;
	white-space: nowrap;
}

.logo-title {
	font-size: 15px;
	font-weight: 700;
	color: #1a365d;
	line-height: 1.2;
}

.logo-subtitle {
	font-size: 11px;
	color: #64748b;
}

/* Menu - Giảm padding để hiển thị nhiều chữ hơn */
.sidebar-menu-wrapper {
	flex: 1;
	overflow: hidden;
}

.sidebar-menu {
	border: none;
	background: transparent;
	padding: 8px 4px;
}

.sidebar-menu:not(.el-menu--collapse) {
	width: calc(var(--sidebar-width) - 8px);
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
	height: 40px;
	line-height: 40px;
	color: #475569;
	border-radius: var(--radius-sm);
	margin-bottom: 2px;
	padding: 0 12px !important;
	font-size: 13px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
	background-color: rgba(26, 54, 93, 0.08);
	color: #1a365d;
}

:deep(.el-menu-item.is-active) {
	background: linear-gradient(90deg, rgba(26, 54, 93, 0.15) 0%, rgba(26, 54, 93, 0.05) 100%);
	color: #1a365d;
	font-weight: 600;
}

/* Inline submenu - Giảm indent */
:deep(.el-sub-menu .el-menu) {
	background: rgba(26, 54, 93, 0.04) !important;
	border-radius: var(--radius-sm);
	padding: 2px 0;
	margin: 4px 0;
}

:deep(.el-sub-menu .el-menu-item) {
	padding-left: 36px !important;
	height: 36px;
	line-height: 36px;
	font-size: 13px;
	color: #64748b !important;
	background: transparent !important;
}

:deep(.el-sub-menu .el-menu-item:hover) {
	background: rgba(26, 54, 93, 0.08) !important;
	color: #1a365d !important;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
	background: rgba(26, 54, 93, 0.12) !important;
	color: #1a365d !important;
	font-weight: 600;
}

:deep(.el-menu--collapse .el-sub-menu.is-active .el-sub-menu__title) {
	color: #1a365d;
}

/* Khi collapsed - căn giữa icon menu */
:deep(.el-menu--collapse) {
	width: var(--sidebar-collapsed-width) !important;
	padding: 8px 0 !important;
}

:deep(.el-menu--collapse .el-menu-item),
:deep(.el-menu--collapse .el-sub-menu__title) {
	width: calc(var(--sidebar-collapsed-width) - 8px) !important;
	min-width: calc(var(--sidebar-collapsed-width) - 8px) !important;
	height: 40px !important;
	padding: 0 !important;
	margin: 0 auto 2px !important;
	display: flex !important;
	justify-content: center !important;
	align-items: center !important;
}

/* Fix tooltip trigger padding - đây là nguyên nhân icon Dashboard bị lệch */
:deep(.el-menu--collapse .el-menu-item .el-menu-tooltip__trigger) {
	padding: 0 !important;
	display: flex !important;
	justify-content: center !important;
	align-items: center !important;
	width: 100% !important;
	height: 100% !important;
}

:deep(.el-menu--collapse .el-menu-item .el-icon),
:deep(.el-menu--collapse .el-sub-menu__title .el-icon) {
	margin: 0 !important;
	font-size: 18px;
}

:deep(.el-menu--collapse .el-sub-menu__icon-arrow) {
	display: none !important;
}

/* Ẩn text khi collapsed */
:deep(.el-menu--collapse .el-menu-item span),
:deep(.el-menu--collapse .el-sub-menu__title span) {
	display: none;
}

/* Badge cho submenu items */
.menu-badge {
	margin-left: auto;
}

:deep(.menu-badge .el-badge__content) {
	background: #f56c6c;
	border: none;
	font-size: 10px;
	height: 16px;
	line-height: 16px;
	padding: 0 5px;
}

/* Badge cho parent menu */
.menu-badge-parent {
	margin-left: auto;
}

:deep(.menu-badge-parent .el-badge__content) {
	background: #f56c6c;
	border: none;
	font-size: 10px;
	height: 16px;
	line-height: 16px;
	padding: 0 5px;
}

/* Khi collapsed - hiển thị badge nhỏ góc trên */
:deep(.el-menu--collapse .menu-badge-parent) {
	position: absolute;
	top: 2px;
	right: 2px;
}

:deep(.el-menu--collapse .menu-badge-parent .el-badge__content) {
	font-size: 9px;
	height: 14px;
	line-height: 14px;
	padding: 0 4px;
}

/* Fix position cho sub-menu title khi có badge */
:deep(.el-sub-menu__title) {
	position: relative;
}

/* Footer */
.sidebar-footer {
	padding: 16px;
	border-top: 1px solid #e2e8f0;
	flex-shrink: 0;
}

.version-info {
	font-size: 11px;
	color: #94a3b8;
	text-align: center;
}

/* Transitions */
.fade-enter-active,
.fade-leave-active {
	transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
	opacity: 0;
}

/* Tablet - sidebar collapsed */
@media (max-width: 1024px) {
	.sidebar:not(.mobile-open) {
		width: var(--sidebar-collapsed-width);
	}

	.sidebar:not(.mobile-open) .logo-text {
		display: none;
	}
}

/* Mobile - sidebar ẩn hoàn toàn */
@media (max-width: 768px) {
	.sidebar {
		transform: translateX(-100%);
		width: var(--sidebar-width);
		transition: transform var(--transition-normal), width var(--transition-normal);
	}

	.sidebar.mobile-open {
		transform: translateX(0);
	}

	.sidebar.mobile-open .logo-text {
		display: flex;
	}
}
</style>


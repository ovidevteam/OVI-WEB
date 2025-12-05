<template>
	<el-dialog
		v-model="visible"
		title="Cảnh báo hết phiên"
		width="400px"
		:close-on-click-modal="false"
		:close-on-press-escape="false"
		:show-close="false"
		center
	>
		<div class="session-warning-content">
			<el-icon class="warning-icon" :size="48" color="#E6A23C">
				<Warning />
			</el-icon>
			<p class="warning-message">
				Phiên đăng nhập của bạn sắp hết hạn. Vui lòng tiếp tục sử dụng để duy trì phiên làm việc.
			</p>
			<p class="warning-time">
				Phiên sẽ tự động đăng xuất sau {{ remainingMinutes }} phút nếu không có hoạt động.
			</p>
		</div>
		<template #footer>
			<el-button @click="extendSession">Tiếp tục</el-button>
			<el-button type="primary" @click="logout">Đăng xuất</el-button>
		</template>
	</el-dialog>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { Warning } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()

const visible = computed(() => authStore.sessionTimeoutWarning)

const remainingMinutes = computed(() => {
	if (!authStore.lastActivity) return 5
	const elapsed = Date.now() - authStore.lastActivity
	const remaining = 30 * 60 * 1000 - elapsed // 30 minutes total
	return Math.max(1, Math.ceil(remaining / 60000))
})

/**
 * Extend session by updating activity
 */
function extendSession() {
	authStore.updateActivity()
	// Warning will be cleared automatically when activity is updated
}

/**
 * Logout user
 */
async function logout() {
	await authStore.logout()
	router.push('/login')
}
</script>

<style scoped>
.session-warning-content {
	text-align: center;
	padding: 20px 0;
}

.warning-icon {
	margin-bottom: 16px;
}

.warning-message {
	font-size: 16px;
	color: #303133;
	margin-bottom: 12px;
	line-height: 1.6;
}

.warning-time {
	font-size: 14px;
	color: #909399;
	margin-bottom: 0;
}
</style>


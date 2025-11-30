<template>
	<div class="login-card">
		<div class="login-header">
			<div class="login-logo">
				<img src="/images/logo-footer.png" alt="BV Y Hà Nội" class="login-logo-img" />
			</div>
			<p class="login-subtitle">Hệ thống Quản lý Phản ánh</p>
		</div>

		<el-form
			ref="formRef"
			:model="form"
			:rules="rules"
			class="login-form"
			@submit.prevent="handleLogin"
		>
			<el-form-item prop="username">
				<el-input
					v-model="form.username"
					placeholder="Tên đăng nhập"
					size="large"
					:prefix-icon="User"
					autocomplete="username"
				/>
			</el-form-item>

			<el-form-item prop="password">
				<el-input
					v-model="form.password"
					type="password"
					placeholder="Mật khẩu"
					size="large"
					:prefix-icon="Lock"
					show-password
					autocomplete="current-password"
					@keyup.enter="handleLogin"
				/>
			</el-form-item>

			<el-form-item>
				<el-checkbox v-model="form.remember">Ghi nhớ đăng nhập</el-checkbox>
			</el-form-item>

			<el-form-item>
				<el-button
					type="primary"
					size="large"
					class="login-button"
					:loading="authStore.loading"
					@click="handleLogin"
				>
					Đăng nhập
				</el-button>
			</el-form-item>
		</el-form>

		<div class="login-footer">
			<a href="#" class="forgot-password" @click.prevent="openForgotDialog">Quên mật khẩu?</a>
		</div>

		<!-- Forgot Password Dialog -->
		<el-dialog
			v-model="forgotDialogVisible"
			title="Quên mật khẩu"
			width="420px"
			class="forgot-dialog"
			:close-on-click-modal="false"
			append-to-body
			:modal="true"
		>
			<div v-if="!forgotSuccess">
				<p class="forgot-desc">
					<el-icon :size="20" class="forgot-icon"><Message /></el-icon>
					Nhập email đăng ký, chúng tôi sẽ gửi link khôi phục mật khẩu.
				</p>
				<el-form
					ref="forgotFormRef"
					:model="forgotForm"
					:rules="forgotRules"
					label-position="top"
				>
					<el-form-item prop="email" label="Email">
						<el-input
							v-model="forgotForm.email"
							placeholder="example@bvyhanoi.vn"
							size="large"
							:prefix-icon="Message"
						/>
					</el-form-item>
				</el-form>
			</div>
			<div v-else class="forgot-success">
				<div class="success-icon">
					<el-icon :size="48" color="#10b981"><CircleCheckFilled /></el-icon>
				</div>
				<h3>Đã gửi email!</h3>
				<p>Vui lòng kiểm tra hộp thư <strong>{{ forgotForm.email }}</strong> và làm theo hướng dẫn để khôi phục mật khẩu.</p>
				<p class="note">Nếu không thấy email, hãy kiểm tra thư mục Spam.</p>
			</div>
			<template #footer>
				<div v-if="!forgotSuccess">
					<el-button @click="forgotDialogVisible = false">Hủy</el-button>
					<el-button type="primary" @click="handleForgotPassword" :loading="forgotLoading">
						Gửi yêu cầu
					</el-button>
				</div>
				<div v-else>
					<el-button type="primary" @click="closeForgotDialog">
						Quay lại đăng nhập
					</el-button>
				</div>
			</template>
		</el-dialog>

		<!-- Demo Accounts -->
		<div class="demo-accounts">
			<p class="demo-title">Demo Accounts:</p>
			<div class="demo-list">
				<span @click="fillDemo('admin', 'admin123')">Admin</span>
				<span @click="fillDemo('leader', 'leader123')">Lãnh đạo</span>
				<span @click="fillDemo('receiver', 'receiver123')">Tiếp nhận</span>
				<span @click="fillDemo('handler', 'handler123')">Xử lý</span>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { User, Lock, Message, CircleCheckFilled } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const formRef = ref(null)

const form = reactive({
	username: '',
	password: '',
	remember: false
})

const rules = {
	username: [
		{ required: true, message: 'Vui lòng nhập tên đăng nhập', trigger: 'blur' }
	],
	password: [
		{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' }
	]
}

const handleLogin = async () => {
	if (!formRef.value) return

	try {
		await formRef.value.validate()
		const result = await authStore.login({
			username: form.username,
			password: form.password
		})

		if (result.success) {
			ElMessage.success('Đăng nhập thành công!')
			router.push('/dashboard')
		} else {
			ElMessage.error(result.message)
		}
	} catch (error) {
		// Validation failed
	}
}

const fillDemo = (username, password) => {
	form.username = username
	form.password = password
}

// Forgot Password Dialog
const forgotDialogVisible = ref(false)
const forgotFormRef = ref(null)
const forgotLoading = ref(false)
const forgotSuccess = ref(false)

const forgotForm = reactive({
	email: ''
})

const forgotRules = {
	email: [
		{ required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
		{ type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
	]
}

const openForgotDialog = () => {
	forgotForm.email = ''
	forgotSuccess.value = false
	forgotDialogVisible.value = true
}

const handleForgotPassword = async () => {
	if (!forgotFormRef.value) return

	try {
		await forgotFormRef.value.validate()
		forgotLoading.value = true

		// Mock API call - simulate delay
		await new Promise(resolve => setTimeout(resolve, 1500))

		// Show success state
		forgotSuccess.value = true
		forgotLoading.value = false
	} catch {
		forgotLoading.value = false
	}
}

const closeForgotDialog = () => {
	forgotDialogVisible.value = false
	forgotForm.email = ''
	forgotSuccess.value = false
}
</script>

<style scoped>
/* Frosted glass effect - gương mờ nhẹ */
.login-card {
	background: rgba(255, 255, 255, 0.85);
	backdrop-filter: blur(12px);
	-webkit-backdrop-filter: blur(12px);
	border-radius: var(--radius-xl);
	padding: 48px 40px;
	box-shadow:
		0 25px 50px -12px rgba(0, 0, 0, 0.25),
		0 0 0 1px rgba(255, 255, 255, 0.2) inset;
	border: 1px solid rgba(255, 255, 255, 0.3);
}

.login-header {
	text-align: center;
	margin-bottom: 32px;
}

.login-logo {
	background: linear-gradient(135deg, #1a365d 0%, #2c5282 100%);
	border-radius: 16px;
	padding: 20px 32px;
	margin: 0 auto 20px;
	display: inline-block;
}

.login-logo-img {
	height: 48px;
	width: auto;
	display: block;
}

.login-subtitle {
	color: var(--text-secondary);
	margin: 0;
	font-size: 0.95rem;
}

.login-form {
	margin-top: 24px;
}

.login-form :deep(.el-input__wrapper) {
	border-radius: var(--radius-md);
	padding: 4px 12px;
}

.login-button {
	width: 100%;
	height: 48px;
	font-size: 1rem;
	font-weight: 600;
	border-radius: var(--radius-md);
}

.login-footer {
	text-align: center;
	margin-top: 16px;
}

.forgot-password {
	color: var(--primary-color);
	font-size: 0.875rem;
}

.forgot-password:hover {
	text-decoration: underline;
}

/* Demo Accounts */
.demo-accounts {
	margin-top: 32px;
	padding-top: 24px;
	border-top: 1px dashed var(--border-color);
	text-align: center;
}

.demo-title {
	font-size: 0.75rem;
	color: var(--text-muted);
	margin: 0 0 12px;
	text-transform: uppercase;
	letter-spacing: 0.5px;
}

.demo-list {
	display: flex;
	justify-content: center;
	gap: 12px;
	flex-wrap: wrap;
}

.demo-list span {
	padding: 6px 12px;
	background: var(--background-color);
	border-radius: var(--radius-sm);
	font-size: 0.8rem;
	color: var(--text-secondary);
	cursor: pointer;
	transition: all var(--transition-fast);
}

.demo-list span:hover {
	background: var(--primary-color);
	color: white;
}

@media (max-width: 480px) {
	.login-card {
		padding: 32px 24px;
	}
}

/* Forgot Password Dialog - use :global since append-to-body */
:global(.forgot-dialog) {
	border-radius: 12px !important;
}

:global(.forgot-dialog .el-dialog__header) {
	padding: 20px 24px 16px;
	border-bottom: 1px solid var(--border-color);
}

:global(.forgot-dialog .el-dialog__title) {
	font-weight: 600;
	font-size: 1.1rem;
}

:global(.forgot-dialog .el-dialog__body) {
	padding: 24px;
}

:global(.forgot-dialog .el-dialog__footer) {
	padding: 16px 24px 20px;
	border-top: 1px solid var(--border-color);
}

.forgot-desc {
	display: flex;
	align-items: flex-start;
	gap: 10px;
	color: var(--text-secondary);
	font-size: 0.9rem;
	line-height: 1.6;
	margin-bottom: 20px;
	padding: 12px 16px;
	background: rgba(13, 110, 253, 0.05);
	border-radius: 8px;
	border-left: 3px solid var(--primary-color);
}

.forgot-icon {
	flex-shrink: 0;
	color: var(--primary-color);
	margin-top: 2px;
}

.forgot-success {
	text-align: center;
	padding: 20px 0;
}

.forgot-success .success-icon {
	margin-bottom: 16px;
}

.forgot-success h3 {
	color: #10b981;
	margin: 0 0 12px;
	font-size: 1.25rem;
}

.forgot-success p {
	color: var(--text-secondary);
	margin: 0 0 8px;
	font-size: 0.9rem;
	line-height: 1.6;
}

.forgot-success p strong {
	color: var(--text-primary);
}

.forgot-success .note {
	font-size: 0.8rem;
	color: var(--text-muted);
	font-style: italic;
}
</style>


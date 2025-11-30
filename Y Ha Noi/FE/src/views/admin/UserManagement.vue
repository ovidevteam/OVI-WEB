<template>
	<div class="user-management">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Quản lý User</h3>
				<el-button type="primary" :icon="Plus" @click="openDialog()">
					Thêm mới
				</el-button>
			</div>

			<!-- Filters -->
			<el-form :inline="true" class="filter-form">
				<el-form-item>
					<el-input
						v-model="searchKeyword"
						placeholder="Tìm theo tên, username..."
						:prefix-icon="Search"
						clearable
						style="width: 250px"
						@input="handleSearch"
					/>
				</el-form-item>
				<el-form-item>
					<el-select v-model="filterRole" placeholder="Vai trò" clearable @change="handleSearch">
						<el-option v-for="r in ROLES" :key="r.value" :label="r.label" :value="r.value" />
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-select v-model="filterStatus" placeholder="Trạng thái" clearable @change="handleSearch">
						<el-option v-for="s in USER_STATUS" :key="s.value" :label="s.label" :value="s.value" />
					</el-select>
				</el-form-item>
			</el-form>

			<!-- Table -->
			<el-table :data="users" v-loading="loading" stripe>
				<el-table-column prop="username" label="Username" width="140" />
				<el-table-column prop="fullName" label="Họ tên" min-width="180" />
				<el-table-column prop="email" label="Email" min-width="200" />
				<el-table-column prop="role" label="Vai trò" width="130" align="center">
					<template #default="{ row }">
						<el-tag size="small">{{ getRoleLabel(row.role) }}</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng ban" width="150" />
				<el-table-column prop="status" label="Trạng thái" width="120" align="center">
					<template #default="{ row }">
						<el-tag :type="getUserStatusType(row.status)" size="small">
							{{ getUserStatusLabel(row.status) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="Thao tác" width="180" align="center" fixed="right">
					<template #default="{ row }">
						<el-button type="primary" link @click="openDialog(row)">Sửa</el-button>
						<el-button type="warning" link @click="resetPassword(row)">Reset MK</el-button>
						<el-button
							:type="row.status === 'ACTIVE' ? 'danger' : 'success'"
							link
							@click="toggleStatus(row)"
						>
							{{ row.status === 'ACTIVE' ? 'Khóa' : 'Mở' }}
						</el-button>
					</template>
				</el-table-column>
			</el-table>

			<!-- Pagination -->
			<div class="pagination-wrapper">
				<el-pagination
					v-model:current-page="currentPage"
					v-model:page-size="pageSize"
					:total="total"
					layout="total, prev, pager, next"
				/>
			</div>
		</div>

		<!-- Dialog -->
		<el-dialog
			v-model="dialogVisible"
			:title="isEdit ? 'Sửa User' : 'Thêm User'"
			width="500px"
			:close-on-click-modal="false"
		>
			<el-form
				ref="formRef"
				:model="form"
				:rules="rules"
				label-width="100px"
			>
				<el-form-item label="Username" prop="username">
					<el-input v-model="form.username" :disabled="isEdit" />
				</el-form-item>
				<el-form-item label="Họ tên" prop="fullName">
					<el-input v-model="form.fullName" />
				</el-form-item>
				<el-form-item label="Email" prop="email">
					<el-input v-model="form.email" type="email" />
				</el-form-item>
				<el-form-item v-if="!isEdit" label="Mật khẩu" prop="password">
					<el-input v-model="form.password" type="password" show-password />
				</el-form-item>
				<el-form-item label="Vai trò" prop="role">
					<el-select v-model="form.role" style="width: 100%">
						<el-option v-for="r in ROLES" :key="r.value" :label="r.label" :value="r.value" />
					</el-select>
				</el-form-item>
				<el-form-item label="Phòng ban">
					<el-select v-model="form.departmentId" placeholder="Chọn phòng ban" clearable style="width: 100%">
						<el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
					</el-select>
				</el-form-item>
			</el-form>

			<template #footer>
				<el-button @click="dialogVisible = false">Hủy</el-button>
				<el-button type="primary" :loading="saveLoading" @click="handleSave">Lưu</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { ROLES, USER_STATUS } from '@/utils/constants'
import { getRoleLabel, getUserStatusLabel, getUserStatusType } from '@/utils/helpers'
import userService from '@/services/userService'
import departmentService from '@/services/departmentService'

const loading = ref(false)
const saveLoading = ref(false)
const users = ref([])
const departments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const filterRole = ref('')
const filterStatus = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
	id: null,
	username: '',
	fullName: '',
	email: '',
	password: '',
	role: 'HANDLER',
	departmentId: null
})

const isEdit = computed(() => !!form.id)

const rules = {
	username: [
		{ required: true, message: 'Vui lòng nhập username', trigger: 'blur' },
		{ min: 6, max: 20, message: 'Username phải từ 6-20 ký tự', trigger: 'blur' }
	],
	fullName: [
		{ required: true, message: 'Vui lòng nhập họ tên', trigger: 'blur' }
	],
	email: [
		{ required: true, message: 'Vui lòng nhập email', trigger: 'blur' },
		{ type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
	],
	password: [
		{ required: true, message: 'Vui lòng nhập mật khẩu', trigger: 'blur' },
		{ min: 8, message: 'Mật khẩu phải có ít nhất 8 ký tự', trigger: 'blur' }
	],
	role: [
		{ required: true, message: 'Vui lòng chọn vai trò', trigger: 'change' }
	]
}

const fetchData = async () => {
	loading.value = true
	try {
		const response = await userService.getList({
			keyword: searchKeyword.value,
			role: filterRole.value,
			status: filterStatus.value,
			page: currentPage.value,
			size: pageSize.value
		})
		users.value = response.data || []
		total.value = response.total || 0
	} catch (error) {
		// Demo data
		users.value = [
			{ id: 1, username: 'admin', fullName: 'Quản trị viên', email: 'admin@bvyhanoi.vn', role: 'ADMIN', departmentName: '', status: 'ACTIVE' },
			{ id: 2, username: 'leader', fullName: 'Nguyễn Văn Lãnh đạo', email: 'leader@bvyhanoi.vn', role: 'LEADER', departmentName: '', status: 'ACTIVE' },
			{ id: 3, username: 'receiver', fullName: 'Trần Thị Tiếp nhận', email: 'receiver@bvyhanoi.vn', role: 'RECEIVER', departmentName: 'Tiếp nhận', status: 'ACTIVE' },
			{ id: 4, username: 'handler1', fullName: 'BS. Nguyễn Văn A', email: 'handler1@bvyhanoi.vn', role: 'HANDLER', departmentName: 'Nội khoa', status: 'ACTIVE' }
		]
		total.value = 4
	} finally {
		loading.value = false
	}
}

const fetchDepartments = async () => {
	try {
		departments.value = await departmentService.getActiveList()
	} catch (error) {
		departments.value = [
			{ id: 1, name: 'Nội khoa' },
			{ id: 2, name: 'Ngoại khoa' }
		]
	}
}

const handleSearch = () => {
	currentPage.value = 1
	fetchData()
}

const openDialog = (row = null) => {
	if (row) {
		Object.assign(form, { ...row, password: '' })
	} else {
		Object.assign(form, {
			id: null,
			username: '',
			fullName: '',
			email: '',
			password: '',
			role: 'HANDLER',
			departmentId: null
		})
	}
	dialogVisible.value = true
}

const handleSave = async () => {
	if (!formRef.value) return
	try {
		await formRef.value.validate()
		saveLoading.value = true

		if (isEdit.value) {
			await userService.update(form.id, form)
		} else {
			await userService.create(form)
		}

		ElMessage.success(isEdit.value ? 'Cập nhật thành công!' : 'Thêm mới thành công!')
		dialogVisible.value = false
		fetchData()
	} catch (error) {
		if (error !== false) {
			ElMessage.error('Có lỗi xảy ra')
		}
	} finally {
		saveLoading.value = false
	}
}

const resetPassword = async (row) => {
	try {
		await ElMessageBox.confirm(
			`Reset mật khẩu cho user "${row.username}"?`,
			'Xác nhận',
			{ type: 'warning' }
		)
		await userService.resetPassword(row.id)
		ElMessage.success('Đã reset mật khẩu!')
	} catch {}
}

const toggleStatus = async (row) => {
	const action = row.status === 'ACTIVE' ? 'khóa' : 'mở khóa'
	try {
		await ElMessageBox.confirm(
			`Bạn có chắc chắn muốn ${action} user "${row.username}"?`,
			'Xác nhận',
			{ type: 'warning' }
		)
		await userService.toggleStatus(row.id)
		ElMessage.success(`Đã ${action} user!`)
		fetchData()
	} catch {}
}

onMounted(() => {
	fetchData()
	fetchDepartments()
})
</script>

<style scoped>
.filter-form {
	margin-bottom: 16px;
}

.pagination-wrapper {
	margin-top: 20px;
	display: flex;
	justify-content: flex-end;
}
</style>


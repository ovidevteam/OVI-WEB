<template>
	<div class="department-management">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Quản lý Phòng ban</h3>
				<el-button type="primary" :icon="Plus" @click="openDialog()">
					Thêm mới
				</el-button>
			</div>

			<!-- Table -->
			<el-table :data="departments" v-loading="loading" stripe row-key="id">
				<el-table-column prop="code" label="Mã phòng" width="120" />
				<el-table-column prop="name" label="Tên phòng" min-width="200" />
				<el-table-column prop="managerName" label="Trưởng phòng" width="180" />
				<el-table-column prop="defaultHandlerName" label="Người xử lý mặc định" width="180" />
				<el-table-column prop="notificationEmail" label="Email thông báo" min-width="200" />
				<el-table-column prop="status" label="Trạng thái" width="120" align="center">
					<template #default="{ row }">
						<el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
							{{ row.status === 'ACTIVE' ? 'Hoạt động' : 'Ngừng' }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="Thao tác" width="150" align="center" fixed="right">
					<template #default="{ row }">
						<el-button type="primary" link @click="openDialog(row)" @mousedown.prevent @selectstart.prevent>Sửa</el-button>
						<el-button type="danger" link @click="handleDelete(row)" @mousedown.prevent @selectstart.prevent>Xóa</el-button>
					</template>
				</el-table-column>
			</el-table>
		</div>

		<!-- Dialog -->
		<el-dialog
			v-model="dialogVisible"
			:title="isEdit ? 'Sửa Phòng ban' : 'Thêm Phòng ban'"
			width="500px"
			:close-on-click-modal="false"
		>
			<el-form
				ref="formRef"
				:model="form"
				:rules="rules"
				label-width="150px"
			>
				<el-form-item label="Mã phòng" prop="code">
					<el-input v-model="form.code" placeholder="Tự động nếu để trống" />
				</el-form-item>
				<el-form-item label="Tên phòng" prop="name">
					<el-input v-model="form.name" />
				</el-form-item>
				<el-form-item label="Mô tả">
					<el-input
						v-model="form.description"
						type="textarea"
						:rows="3"
						placeholder="Nhập mô tả về phòng ban..."
					/>
				</el-form-item>
				<el-form-item label="Trưởng phòng">
					<el-select v-model="form.managerId" placeholder="Chọn trưởng phòng" clearable style="width: 100%">
						<el-option v-for="u in users" :key="u.id" :label="u.fullName" :value="u.id" />
					</el-select>
				</el-form-item>
				<el-form-item label="Người xử lý mặc định">
					<el-select v-model="form.defaultHandlerId" placeholder="Chọn người xử lý" clearable style="width: 100%">
						<el-option v-for="u in handlers" :key="u.id" :label="u.fullName" :value="u.id" />
					</el-select>
				</el-form-item>
				<el-form-item label="Email thông báo" prop="notificationEmail">
					<el-input v-model="form.notificationEmail" type="email" />
				</el-form-item>
				<el-form-item label="Trạng thái">
					<el-radio-group v-model="form.status">
						<el-radio value="ACTIVE">Hoạt động</el-radio>
						<el-radio value="INACTIVE">Ngừng</el-radio>
					</el-radio-group>
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
import { Plus } from '@element-plus/icons-vue'
import departmentService from '@/services/departmentService'
import userService from '@/services/userService'
import { handleApiError } from '@/utils/errorHandler'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const loading = ref(false)
const saveLoading = ref(false)
const departments = ref([])
const users = ref([])
const handlers = ref([])
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
	id: null,
	code: '',
	name: '',
	description: '',
	managerId: null,
	defaultHandlerId: null,
	notificationEmail: '',
	status: 'ACTIVE'
})

const isEdit = computed(() => !!form.id)

const rules = {
	name: [
		{ required: true, message: 'Vui lòng nhập tên phòng', trigger: 'blur' }
	],
	notificationEmail: [
		{ type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
	]
}

const fetchData = async () => {
	loading.value = true
	try {
		departments.value = await departmentService.getList()
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			departments.value = [
				{ id: 1, code: 'PB-001', name: 'Nội khoa', managerName: 'BS. Nguyễn Văn A', defaultHandlerName: 'BS. Nguyễn Văn A', notificationEmail: 'noikhoa@bvyhanoi.vn', status: 'ACTIVE' },
				{ id: 2, code: 'PB-002', name: 'Ngoại khoa', managerName: 'BS. Trần Văn B', defaultHandlerName: 'BS. Trần Văn B', notificationEmail: 'ngoaikhoa@bvyhanoi.vn', status: 'ACTIVE' },
				{ id: 3, code: 'PB-003', name: 'Sản khoa', managerName: 'BS. Lê Thị C', defaultHandlerName: 'BS. Lê Thị C', notificationEmail: 'sankhoa@bvyhanoi.vn', status: 'ACTIVE' }
			]
		} else {
			handleApiError(error, 'Department Management')
		}
	} finally {
		loading.value = false
	}
}

const fetchUsers = async () => {
	try {
		const response = await userService.getList()
		users.value = response.data || []
		handlers.value = users.value.filter(u => u.role === 'HANDLER')
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			users.value = [
				{ id: 1, fullName: 'BS. Nguyễn Văn A' },
				{ id: 2, fullName: 'BS. Trần Văn B' }
			]
			handlers.value = users.value
		} else {
			// Silently fail for user fetch - not critical
		}
	}
}

const openDialog = (row = null) => {
	if (row) {
		// Map handlerId to defaultHandlerId for form
		Object.assign(form, {
			...row,
			defaultHandlerId: row.handlerId || row.defaultHandlerId
		})
	} else {
		Object.assign(form, {
			id: null,
			code: '',
			name: '',
			description: '',
			managerId: null,
			defaultHandlerId: null,
			notificationEmail: '',
			status: 'ACTIVE'
		})
	}
	dialogVisible.value = true
}

const handleSave = async () => {
	if (!formRef.value) return
	try {
		await formRef.value.validate()
		saveLoading.value = true

		// Normalize form data: convert empty strings to null for optional fields
		const formData = {
			...form,
			code: form.code && form.code.trim() ? form.code.trim() : null,
			description: form.description && form.description.trim() ? form.description.trim() : null,
			notificationEmail: form.notificationEmail && form.notificationEmail.trim() ? form.notificationEmail.trim() : null
		}

		if (isEdit.value) {
			await departmentService.update(form.id, formData)
		} else {
			await departmentService.create(formData)
		}

		ElMessage.success(isEdit.value ? 'Cập nhật thành công!' : 'Thêm mới thành công!')
		dialogVisible.value = false
		fetchData()
	} catch (error) {
		if (error !== false) {
			handleApiError(error, 'Save Department')
		}
	} finally {
		saveLoading.value = false
	}
}

const handleDelete = async (row) => {
	try {
		await ElMessageBox.confirm(
			`Bạn có chắc chắn muốn xóa phòng "${row.name}"?`,
			'Xác nhận xóa',
			{ type: 'warning' }
		)
		await departmentService.delete(row.id)
		ElMessage.success('Đã xóa phòng ban!')
		fetchData()
	} catch {}
}

onMounted(() => {
	fetchData()
	fetchUsers()
})
</script>


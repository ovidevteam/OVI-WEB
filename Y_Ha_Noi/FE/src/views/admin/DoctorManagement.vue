<template>
	<div class="doctor-management">
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Quản lý Bác sĩ</h3>
				<el-button type="primary" :icon="Plus" @click="openDialog()">
					Thêm mới
				</el-button>
			</div>

			<!-- Filters -->
			<el-form :inline="true" class="filter-form">
				<el-form-item>
					<el-input
						v-model="searchKeyword"
						placeholder="Tìm theo tên, mã BS..."
						:prefix-icon="Search"
						clearable
						style="width: 250px"
						@input="handleSearch"
					/>
				</el-form-item>
				<el-form-item>
					<el-select v-model="filterDepartment" placeholder="Phòng ban" clearable @change="handleSearch">
						<el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
					</el-select>
				</el-form-item>
			</el-form>

			<!-- Table -->
			<el-table :data="doctors" v-loading="loading" stripe>
				<el-table-column prop="code" label="Mã BS" width="100" />
				<el-table-column prop="fullName" label="Họ tên" min-width="180" />
				<el-table-column prop="specialty" label="Chuyên khoa" width="150" />
				<el-table-column prop="departmentName" label="Phòng ban" width="150" />
				<el-table-column prop="email" label="Email" min-width="180" />
				<el-table-column prop="phone" label="Số điện thoại" width="130" />
				<el-table-column prop="status" label="Trạng thái" width="110" align="center">
					<template #default="{ row }">
						<el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'" size="small">
							{{ row.status === 'ACTIVE' ? 'Hoạt động' : 'Ngừng' }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="Thao tác" width="150" align="center" fixed="right">
					<template #default="{ row }">
						<el-button type="primary" link @click="openDialog(row)">Sửa</el-button>
						<el-button type="danger" link @click="handleDelete(row)">Xóa</el-button>
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
			:title="isEdit ? 'Sửa Bác sĩ' : 'Thêm Bác sĩ'"
			width="500px"
			:close-on-click-modal="false"
		>
			<el-form
				ref="formRef"
				:model="form"
				:rules="rules"
				label-width="120px"
			>
				<el-form-item label="Mã bác sĩ">
					<el-input v-model="form.code" placeholder="Tự động nếu để trống" />
				</el-form-item>
				<el-form-item label="Họ tên" prop="fullName">
					<el-input v-model="form.fullName" />
				</el-form-item>
				<el-form-item label="Chuyên khoa" prop="specialty">
					<el-select v-model="form.specialty" placeholder="Chọn chuyên khoa" style="width: 100%">
						<el-option v-for="s in SPECIALTIES" :key="s" :label="s" :value="s" />
					</el-select>
				</el-form-item>
				<el-form-item label="Phòng ban" prop="departmentId">
					<el-select v-model="form.departmentId" placeholder="Chọn phòng ban" style="width: 100%">
						<el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
					</el-select>
				</el-form-item>
				<el-form-item label="Email" prop="email">
					<el-input v-model="form.email" type="email" />
				</el-form-item>
				<el-form-item label="Số điện thoại">
					<el-input v-model="form.phone" />
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
import { Plus, Search } from '@element-plus/icons-vue'
import { SPECIALTIES } from '@/utils/constants'
import doctorService from '@/services/doctorService'
import departmentService from '@/services/departmentService'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const loading = ref(false)
const saveLoading = ref(false)
const doctors = ref([])
const departments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const searchKeyword = ref('')
const filterDepartment = ref('')
const dialogVisible = ref(false)
const formRef = ref(null)

const form = reactive({
	id: null,
	code: '',
	fullName: '',
	specialty: '',
	departmentId: null,
	email: '',
	phone: '',
	status: 'ACTIVE'
})

const isEdit = computed(() => !!form.id)

const rules = {
	fullName: [
		{ required: true, message: 'Vui lòng nhập họ tên', trigger: 'blur' }
	],
	specialty: [
		{ required: true, message: 'Vui lòng chọn chuyên khoa', trigger: 'change' }
	],
	departmentId: [
		{ required: true, message: 'Vui lòng chọn phòng ban', trigger: 'change' }
	],
	email: [
		{ type: 'email', message: 'Email không hợp lệ', trigger: 'blur' }
	]
}

const fetchData = async () => {
	loading.value = true
	try {
		const response = await doctorService.getList({
			keyword: searchKeyword.value,
			departmentId: filterDepartment.value,
			page: currentPage.value,
			size: pageSize.value
		})
		doctors.value = response.data || []
		total.value = response.total || 0
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			doctors.value = [
				{ id: 1, code: 'BS-001', fullName: 'BS. Nguyễn Văn A', specialty: 'Nội khoa', departmentName: 'Nội khoa', email: 'bsa@bvyhanoi.vn', phone: '0912345678', status: 'ACTIVE' },
				{ id: 2, code: 'BS-002', fullName: 'BS. Trần Thị B', specialty: 'Ngoại khoa', departmentName: 'Ngoại khoa', email: 'bsb@bvyhanoi.vn', phone: '0987654321', status: 'ACTIVE' },
				{ id: 3, code: 'BS-003', fullName: 'BS. Lê Văn C', specialty: 'Da liễu', departmentName: 'Da liễu', email: 'bsc@bvyhanoi.vn', phone: '0909090909', status: 'ACTIVE' }
			]
			total.value = 3
		} else {
			console.error('Error fetching doctors:', error)
			ElMessage.error('Lỗi khi tải danh sách bác sĩ')
		}
	} finally {
		loading.value = false
	}
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
				{ id: 3, name: 'Da liễu' }
			]
		} else {
			console.error('Error fetching departments:', error)
		}
	}
}

const handleSearch = () => {
	currentPage.value = 1
	fetchData()
}

const openDialog = (row = null) => {
	if (row) {
		Object.assign(form, row)
	} else {
		Object.assign(form, {
			id: null,
			code: '',
			fullName: '',
			specialty: '',
			departmentId: null,
			email: '',
			phone: '',
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

		if (isEdit.value) {
			await doctorService.update(form.id, form)
		} else {
			await doctorService.create(form)
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

const handleDelete = async (row) => {
	try {
		await ElMessageBox.confirm(
			`Bạn có chắc chắn muốn xóa bác sĩ "${row.fullName}"?`,
			'Xác nhận xóa',
			{ type: 'warning' }
		)
		await doctorService.delete(row.id)
		ElMessage.success('Đã xóa bác sĩ!')
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


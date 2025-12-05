<template>
	<div class="feedback-list">
		<!-- Filters -->
		<div class="content-card">
			<el-form :inline="true" :model="filters" class="filter-form">
				<el-form-item label="Từ ngày">
					<el-date-picker
						v-model="filters.dateFrom"
						type="date"
						placeholder="Chọn ngày"
						format="DD/MM/YYYY"
						value-format="YYYY-MM-DD"
						style="width: 150px"
					/>
				</el-form-item>
				<el-form-item label="Đến ngày">
					<el-date-picker
						v-model="filters.dateTo"
						type="date"
						placeholder="Chọn ngày"
						format="DD/MM/YYYY"
						value-format="YYYY-MM-DD"
						style="width: 150px"
					/>
				</el-form-item>
				<el-form-item label="Phòng ban">
					<el-select v-model="filters.departmentId" placeholder="Tất cả" clearable style="width: 160px">
						<el-option
							v-for="dept in departments"
							:key="dept.id"
							:label="dept.name"
							:value="dept.id"
						/>
					</el-select>
				</el-form-item>
				<el-form-item label="Trạng thái">
					<el-select v-model="filters.status" placeholder="Tất cả" clearable style="width: 140px">
						<el-option
							v-for="s in STATUS"
							:key="s.value"
							:label="s.label"
							:value="s.value"
						/>
					</el-select>
				</el-form-item>
				<el-form-item label="Mức độ">
					<el-select v-model="filters.level" placeholder="Tất cả" clearable style="width: 140px">
						<el-option
							v-for="l in LEVELS"
							:key="l.value"
							:label="l.label"
							:value="l.value"
						/>
					</el-select>
				</el-form-item>
				<el-form-item>
					<el-button type="primary" :icon="Search" @click="handleSearch">
						Tìm kiếm
					</el-button>
					<el-button :icon="Refresh" @click="handleReset">
						Đặt lại
					</el-button>
				</el-form-item>
			</el-form>
		</div>

		<!-- Table -->
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Danh sách Phản ánh</h3>
				<el-button
					v-if="authStore.hasRole(['ADMIN', 'RECEIVER'])"
					type="primary"
					:icon="Plus"
					@click="goToCreate"
				>
					Nhập mới
				</el-button>
			</div>

			<el-table
				:data="feedbacks"
				v-loading="loading"
				stripe
				style="width: 100%"
				@row-click="handleRowClick"
				row-class-name="clickable-row"
			>
				<el-table-column prop="code" label="Số PA" width="145" />
				<el-table-column prop="receivedDate" label="Ngày nhận" width="105">
					<template #default="{ row }">
						{{ formatDate(row.receivedDate) }}
					</template>
				</el-table-column>
				<el-table-column prop="channel" label="Kênh" width="90">
					<template #default="{ row }">
						<el-tag size="small" effect="plain">
							{{ getChannelLabel(row.channel) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="content" label="Nội dung" min-width="200">
					<template #default="{ row }">
						<el-tooltip :content="row.content" placement="top" :show-after="500">
							<span class="content-cell">{{ row.content }}</span>
						</el-tooltip>
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng ban" min-width="110" />
				<el-table-column prop="level" label="Mức độ" width="95" align="center">
					<template #default="{ row }">
						<el-tag :type="getLevelType(row.level)" size="small">
							{{ getLevelLabel(row.level) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="Trạng thái" width="110" align="center">
					<template #default="{ row }">
						<el-tag :type="getStatusType(row.status)" size="small">
							{{ getStatusLabel(row.status) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="handlerName" label="Người xử lý" min-width="130" />
				<el-table-column label="" width="80" fixed="right" align="center">
					<template #default="{ row }">
						<el-button type="primary" link @click.stop="viewDetail(row.id)">
							Chi tiết
						</el-button>
					</template>
				</el-table-column>
			</el-table>

			<!-- Pagination -->
			<div class="pagination-wrapper">
				<el-pagination
					v-model:current-page="currentPage"
					v-model:page-size="pageSize"
					:page-sizes="PAGE_SIZES"
					:total="total"
					layout="total, sizes, prev, pager, next, jumper"
					@size-change="handleSizeChange"
					@current-change="handlePageChange"
				/>
			</div>
		</div>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { STATUS, LEVELS, PAGE_SIZES } from '@/utils/constants'
import {
	formatDate, truncate, getChannelLabel, getLevelLabel,
	getLevelType, getStatusLabel, getStatusType
} from '@/utils/helpers'
import feedbackService from '@/services/feedbackService'
import departmentService from '@/services/departmentService'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const feedbacks = ref([])
const departments = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)

const filters = reactive({
	dateFrom: null,
	dateTo: null,
	departmentId: null,
	status: null,
	level: null
})

const fetchData = async () => {
	loading.value = true
	try {
		const response = await feedbackService.getList({
			...filters,
			page: currentPage.value,
			size: pageSize.value
		})
		feedbacks.value = response.data || []
		total.value = response.total || 0
	} catch (error) {
		console.error('Error fetching feedbacks:', error)
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			feedbacks.value = [
				{
					id: 1,
					code: 'PA-20251127-001',
					receivedDate: '2025-11-27',
					channel: 'HOTLINE',
					content: 'Phản ánh về thái độ phục vụ của nhân viên khoa Nội khi tiếp nhận bệnh nhân',
					departmentName: 'Nội khoa',
					level: 'HIGH',
					status: 'NEW',
					handlerName: 'BS. Nguyễn Văn A'
				},
				{
					id: 2,
					code: 'PA-20251127-002',
					receivedDate: '2025-11-27',
					channel: 'EMAIL',
					content: 'Thời gian chờ khám quá lâu tại phòng khám da liễu',
					departmentName: 'Da liễu',
					level: 'MEDIUM',
					status: 'PROCESSING',
					handlerName: 'BS. Trần Thị B'
				},
				{
					id: 3,
					code: 'PA-20251126-003',
					receivedDate: '2025-11-26',
					channel: 'DIRECT',
					content: 'Cảm ơn bác sĩ đã tận tình điều trị cho bệnh nhân',
					departmentName: 'Ngoại khoa',
					level: 'LOW',
					status: 'COMPLETED',
					handlerName: 'BS. Lê Văn C'
				}
			]
			total.value = 3
		} else {
			ElMessage.error('Lỗi khi tải danh sách phản ánh')
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
				{ id: 3, name: 'Da liễu' },
				{ id: 4, name: 'Sản khoa' }
			]
		} else {
			console.error('Failed to load departments:', error)
		}
	}
}

const handleSearch = () => {
	currentPage.value = 1
	fetchData()
}

const handleReset = () => {
	Object.assign(filters, {
		dateFrom: null,
		dateTo: null,
		departmentId: null,
		status: null,
		level: null
	})
	handleSearch()
}

const handleRowClick = (row) => {
	router.push(`/feedback/${row.id}`)
}

const handleSizeChange = () => {
	currentPage.value = 1
	fetchData()
}

const handlePageChange = () => {
	fetchData()
}

const goToCreate = () => {
	router.push('/feedback/create')
}

const viewDetail = (id) => {
	router.push(`/feedback/${id}`)
}

onMounted(() => {
	fetchData()
	fetchDepartments()
})
</script>

<style scoped>
.filter-form {
	display: flex;
	flex-wrap: wrap;
	gap: 8px;
}

.content-cell {
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
	overflow: hidden;
	text-overflow: ellipsis;
	line-height: 1.4;
	word-break: break-word;
	color: var(--text-primary);
}

.clickable-row {
	cursor: pointer;
}

.pagination-wrapper {
	margin-top: 20px;
	display: flex;
	justify-content: flex-end;
}

:deep(.el-table__row:hover) {
	cursor: pointer;
}
</style>


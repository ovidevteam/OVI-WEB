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
				<el-form-item label="Thao tác">
					<el-select v-model="filters.hasHandler" placeholder="Tất cả" clearable style="width: 140px">
						<el-option label="Đã phân công" value="assigned" />
						<el-option label="Chưa phân công" value="unassigned" />
					</el-select>
				</el-form-item>
				<el-form-item label="Bác sĩ">
					<el-select v-model="filters.doctorId" placeholder="Tất cả" clearable filterable style="width: 180px">
						<el-option
							v-for="doctor in doctors"
							:key="doctor.id"
							:label="doctor.fullName || doctor.name"
							:value="doctor.id"
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
				:data="optimizedFeedbacks"
				v-loading="loading"
				stripe
				row-key="id"
				style="width: 100%"
				@row-click="handleRowClick"
				row-class-name="clickable-row"
				aria-label="Danh sách phản ánh"
				:default-sort="{ prop: 'receivedDate', order: 'descending' }"
			>
				<el-table-column prop="code" label="Số PA" width="160" />
				<el-table-column prop="receivedDate" label="Ngày nhận" width="120">
					<template #default="{ row }">
						{{ formatDate(row.receivedDate) }}
					</template>
				</el-table-column>
				<el-table-column prop="channel" label="Kênh" width="100">
					<template #default="{ row }">
						<el-tag size="small" effect="plain">
							{{ getChannelLabel(row.channel) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="content" label="Nội dung" min-width="250">
					<template #default="{ row }">
						<el-tooltip :content="row.content" placement="top" :show-after="500">
							<span class="content-cell">{{ row.content }}</span>
						</el-tooltip>
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng ban" min-width="120" />
				<el-table-column prop="doctorName" label="Bác sĩ" min-width="150" />
				<el-table-column prop="level" label="Mức độ" width="100" align="center">
					<template #default="{ row }">
						<el-tag :type="getLevelType(row.level)" size="small">
							{{ getLevelLabel(row.level) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="Trạng thái" width="120" align="center">
					<template #default="{ row }">
						<el-tag :type="getStatusType(row.status)" size="small">
							{{ getStatusLabel(row.status) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="handlerName" label="Người xử lý" min-width="140">
					<template #default="{ row }">
						<span v-if="row.handlerName">{{ row.handlerName }}</span>
						<el-tag v-else type="warning" size="small">Chưa phân công</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="Thao tác" width="120" align="center" fixed="right" v-if="authStore.hasRole(['ADMIN', 'LEADER'])">
					<template #default="{ row }">
						<el-button
							v-if="!row.handlerId"
							type="primary"
							size="small"
							link
							@click.stop="openAssignDialog(row)"
						>
							Phân công
						</el-button>
						<span v-else class="text-muted">Đã phân công</span>
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

		<!-- Feedback Detail Dialog -->
		<el-dialog
			v-model="detailDialogVisible"
			title="Chi tiết Phản ánh"
			width="1200px"
			:close-on-click-modal="false"
			destroy-on-close
			align-center
			class="feedback-detail-dialog"
		>
			<FeedbackDetail :feedback-id="selectedFeedbackId" :is-dialog="true" />
		</el-dialog>

		<!-- Assign Handler Dialog -->
		<el-dialog
			v-model="assignDialogVisible"
			title="Phân công người xử lý"
			width="600px"
			:close-on-click-modal="false"
			destroy-on-close
		>
			<div v-if="selectedFeedbackForAssign">
				<div class="assign-feedback-info">
					<p><strong>Mã PA:</strong> {{ selectedFeedbackForAssign.code }}</p>
					<p><strong>Nội dung:</strong> {{ selectedFeedbackForAssign.content }}</p>
					<p><strong>Phòng ban:</strong> {{ selectedFeedbackForAssign.departmentName || 'Chưa xác định' }}</p>
				</div>

				<el-form :model="assignForm" label-width="140px" style="margin-top: 20px">
					<el-form-item label="Người xử lý" required>
						<el-select
							v-model="assignForm.handlerId"
							placeholder="Chọn người xử lý"
							style="width: 100%"
							filterable
							:loading="loadingHandlers"
						>
							<el-option
								v-for="handler in handlers"
								:key="handler.id"
								:label="handler.fullName"
								:value="handler.id"
							>
								<span>{{ handler.fullName }}</span>
								<span v-if="handler.departmentName" style="color: #8492a6; font-size: 12px; margin-left: 8px">
									({{ handler.departmentName }})
								</span>
							</el-option>
						</el-select>
					</el-form-item>
				</el-form>
			</div>

			<template #footer>
				<el-button @click="assignDialogVisible = false">Hủy</el-button>
				<el-button type="primary" :loading="assigning" @click="handleAssignHandler">
					Phân công
				</el-button>
			</template>
		</el-dialog>
	</div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount, computed, shallowRef, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
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
import doctorService from '@/services/doctorService'
import userService from '@/services/userService'
import FeedbackDetail from './FeedbackDetail.vue'
import { handleApiError } from '@/utils/errorHandler'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const loading = ref(false)
// Use shallowRef for better performance with large arrays
const feedbacks = shallowRef([])
const departments = ref([])
const doctors = ref([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const detailDialogVisible = ref(false)
const selectedFeedbackId = ref(null)

// Assign handler dialog
const assignDialogVisible = ref(false)
const selectedFeedbackForAssign = ref(null)
const handlers = ref([])
const loadingHandlers = ref(false)
const assigning = ref(false)
const assignForm = reactive({
	handlerId: null
})

// Computed property for optimized feedback list
const optimizedFeedbacks = computed(() => feedbacks.value)

const filters = reactive({
	dateFrom: null,
	dateTo: null,
	departmentId: null,
	status: null,
	level: null,
	hasHandler: null,
	doctorId: null
})

const fetchData = async () => {
	loading.value = true
	try {
		// Prepare params, exclude hasHandler from API call (will filter on frontend)
		const { hasHandler, ...apiFilters } = filters
		const response = await feedbackService.getList({
			...apiFilters,
			page: currentPage.value,
			size: pageSize.value
		})
		let feedbackList = response.data || []
		
		// Filter by hasHandler on frontend
		if (hasHandler === 'assigned') {
			feedbackList = feedbackList.filter(f => f.handlerId != null)
		} else if (hasHandler === 'unassigned') {
			feedbackList = feedbackList.filter(f => f.handlerId == null)
		}
		
		// Map department and doctor names
		feedbackList = mapFeedbackData(feedbackList)
		feedbacks.value = feedbackList
		total.value = response.total || 0
	} catch (error) {
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
					doctorName: 'BS. Nguyễn Văn A',
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
					doctorName: 'BS. Trần Thị B',
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
					doctorName: 'BS. Lê Văn C',
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
			// Failed to load departments - not critical
		}
	}
}

const fetchDoctors = async () => {
	try {
		// Load all doctors
		const response = await doctorService.getAll()
		// Handle both array and paginated response
		if (Array.isArray(response)) {
			doctors.value = response
		} else if (response && response.data) {
			doctors.value = response.data
		} else {
			doctors.value = []
		}
	} catch (error) {
		if (DEMO_MODE) {
			// Demo data - only in demo mode
			doctors.value = [
				{ id: 101, fullName: 'BS. Nguyễn Văn A', departmentId: 1 },
				{ id: 102, fullName: 'BS. Trần Thị B', departmentId: 2 },
				{ id: 103, fullName: 'BS. Lê Văn C', departmentId: 3 }
			]
		} else {
			// Failed to load doctors - not critical
			doctors.value = []
		}
	}
}

/**
 * Map departmentId to departmentName and doctorId to doctorName
 */
const mapFeedbackData = (feedbackList) => {
	if (!feedbackList || !Array.isArray(feedbackList)) return []
	return feedbackList.map(feedback => {
		const mapped = { ...feedback }
		
		// Map department
		if (mapped.departmentId && (!mapped.departmentName || mapped.departmentName.trim() === '') && departments.value.length > 0) {
			const dept = departments.value.find(d => d.id === mapped.departmentId)
			if (dept) {
				mapped.departmentName = dept.name
			}
		}
		// If still no departmentName, try to get from department object if exists
		if (!mapped.departmentName && mapped.department) {
			mapped.departmentName = mapped.department.name || mapped.department.departmentName
		}
		
		// Map doctor
		if (mapped.doctorId && (!mapped.doctorName || mapped.doctorName.trim() === '') && doctors.value.length > 0) {
			const doctor = doctors.value.find(d => d.id === mapped.doctorId)
			if (doctor) {
				mapped.doctorName = doctor.fullName || doctor.name
			}
		}
		// If still no doctorName, try to get from doctor object if exists
		if (!mapped.doctorName && mapped.doctor) {
			mapped.doctorName = mapped.doctor.fullName || mapped.doctor.name
		}
		
		return mapped
	})
}

const handleSearch = () => {
	currentPage.value = 1
	
	// Update URL with current filters for bookmarking/sharing
	const query = {}
	if (filters.departmentId) query.departmentId = filters.departmentId
	if (filters.status) query.status = filters.status
	if (filters.level) query.level = filters.level
	if (filters.dateFrom) query.dateFrom = filters.dateFrom
	if (filters.dateTo) query.dateTo = filters.dateTo
	if (filters.doctorId) query.doctorId = filters.doctorId
	
	router.replace({ query })
	
	fetchData()
}

const handleReset = () => {
	Object.assign(filters, {
		dateFrom: null,
		dateTo: null,
		departmentId: null,
		status: null,
		level: null,
		hasHandler: null,
		doctorId: null
	})
	handleSearch()
}

const handleRowClick = (row) => {
	selectedFeedbackId.value = row.id
	detailDialogVisible.value = true
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

const openAssignDialog = async (row) => {
	selectedFeedbackForAssign.value = row
	assignForm.handlerId = null
	assignDialogVisible.value = true
	
	// Load handlers for the feedback's department
	await loadHandlers(row.departmentId)
}

const loadHandlers = async (departmentId) => {
	loadingHandlers.value = true
	try {
		if (DEMO_MODE) {
			// Demo data
			handlers.value = [
				{ id: 1, fullName: 'BS. Nguyễn Văn A', departmentName: 'Nội khoa' },
				{ id: 2, fullName: 'BS. Trần Thị B', departmentName: 'Ngoại khoa' },
				{ id: 3, fullName: 'BS. Lê Văn C', departmentName: 'Sản khoa' }
			]
		} else {
			const response = await userService.getHandlers(departmentId)
			// Handle both array and object response
			if (Array.isArray(response)) {
				handlers.value = response
			} else if (response && Array.isArray(response.data)) {
				handlers.value = response.data
			} else if (response && response.data) {
				handlers.value = [response.data]
			} else {
				handlers.value = []
			}
			
			// If no handlers found for department, try loading all handlers
			if (handlers.value.length === 0 && departmentId) {
				const allHandlersResponse = await userService.getHandlers(null)
				if (Array.isArray(allHandlersResponse)) {
					handlers.value = allHandlersResponse
				} else if (allHandlersResponse && Array.isArray(allHandlersResponse.data)) {
					handlers.value = allHandlersResponse.data
				}
			}
		}
	} catch (error) {
		handleApiError(error, 'Load Handlers')
		handlers.value = []
	} finally {
		loadingHandlers.value = false
	}
}

const handleAssignHandler = async () => {
	if (!assignForm.handlerId) {
		ElMessage.warning('Vui lòng chọn người xử lý')
		return
	}

	if (!selectedFeedbackForAssign.value) {
		return
	}

	assigning.value = true
	try {
		await feedbackService.assignHandler(selectedFeedbackForAssign.value.id, assignForm.handlerId)
		ElMessage.success('Phân công người xử lý thành công!')
		assignDialogVisible.value = false
		// Refresh data
		await fetchData()
		
		// Refresh sidebar stats to update badge counts
		window.dispatchEvent(new CustomEvent('refresh-feedback-stats'))
	} catch (error) {
		if (error !== false) {
			handleApiError(error, 'Assign Handler')
		}
	} finally {
		assigning.value = false
	}
}

// Listen for notification events to open feedback dialog
const handleOpenFeedbackDialog = (event) => {
	const { feedbackId } = event.detail
	if (feedbackId) {
		// Always update selectedFeedbackId and open dialog
		// This ensures that clicking different notifications will open different feedbacks
		selectedFeedbackId.value = feedbackId
		// Force dialog to open even if it's already open
		detailDialogVisible.value = true
	}
}

// Check query param and auto-open dialog
const checkQueryParam = () => {
	const feedbackId = route.query.feedbackId
	if (feedbackId) {
		// Convert to number if it's a string
		const id = typeof feedbackId === 'string' ? parseInt(feedbackId, 10) : feedbackId
		if (id && !isNaN(id)) {
			selectedFeedbackId.value = id
			detailDialogVisible.value = true
			// Clear query param after opening dialog
			router.replace({ query: {} })
		}
	}
}

onMounted(async () => {
	// Load departments and doctors first, then fetch feedbacks
	await Promise.all([fetchDepartments(), fetchDoctors()])
	
	// Read query params and apply filters
	if (route.query.departmentId) {
		filters.departmentId = Number(route.query.departmentId)
	}
	if (route.query.status) {
		filters.status = route.query.status
	}
	if (route.query.level) {
		filters.level = route.query.level
	}
	if (route.query.dateFrom) {
		filters.dateFrom = route.query.dateFrom
	}
	if (route.query.dateTo) {
		filters.dateTo = route.query.dateTo
	}
	if (route.query.doctorId) {
		filters.doctorId = Number(route.query.doctorId)
	}
	
	// Fetch data with filters applied
	fetchData()
	
	// Check query param for auto-opening dialog (from notification click)
	checkQueryParam()
	
	// Listen for notification events (for same-page notifications)
	window.addEventListener('open-feedback-dialog', handleOpenFeedbackDialog)
})

onBeforeUnmount(() => {
	// Clean up event listener
	window.removeEventListener('open-feedback-dialog', handleOpenFeedbackDialog)
})

// Watch route query changes
watch(() => route.query.feedbackId, (newId) => {
	if (newId) {
		checkQueryParam()
	}
})

// Watch for filter query params changes (from external navigation)
watch(() => [route.query.departmentId, route.query.status, route.query.level, route.query.dateFrom, route.query.dateTo, route.query.doctorId], 
	([deptId, status, level, dateFrom, dateTo, doctorId]) => {
		// Only update if values actually changed to avoid infinite loops
		if (deptId !== undefined) {
			filters.departmentId = deptId ? Number(deptId) : null
		}
		if (status !== undefined) {
			filters.status = status || null
		}
		if (level !== undefined) {
			filters.level = level || null
		}
		if (dateFrom !== undefined) {
			filters.dateFrom = dateFrom || null
		}
		if (dateTo !== undefined) {
			filters.dateTo = dateTo || null
		}
		if (doctorId !== undefined) {
			filters.doctorId = doctorId ? Number(doctorId) : null
		}
		// Trigger search when filters change from query params
		if (deptId !== undefined || status !== undefined || level !== undefined || dateFrom !== undefined || dateTo !== undefined) {
			currentPage.value = 1
			fetchData()
		}
	},
	{ deep: true }
)
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
	line-clamp: 2;
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

/* Mobile responsive */
@media (max-width: 768px) {
	.filter-form {
		flex-direction: column;
	}
	
	.filter-form .el-form-item {
		width: 100%;
		margin-right: 0;
	}
	
	:deep(.el-table) {
		font-size: 12px;
	}
	
	:deep(.el-table th),
	:deep(.el-table td) {
		padding: 8px 4px;
	}
	
	.pagination-wrapper {
		justify-content: center;
		flex-wrap: wrap;
	}
	
	:deep(.el-pagination) {
		justify-content: center;
	}
}

/* Feedback Detail Dialog */
:deep(.feedback-detail-dialog) {
	max-width: 95vw;
}

:deep(.feedback-detail-dialog .el-dialog) {
	margin: 5vh auto;
	max-height: 90vh;
	display: flex;
	flex-direction: column;
}

:deep(.feedback-detail-dialog .el-dialog__body) {
	flex: 1;
	overflow-y: auto;
	padding: 20px;
}

.assign-feedback-info {
	background: var(--bg-hover);
	padding: 16px;
	border-radius: var(--radius-md);
	margin-bottom: 20px;
}

.assign-feedback-info p {
	margin: 8px 0;
	color: var(--text-primary);
}

.assign-feedback-info p:first-child {
	margin-top: 0;
}

.assign-feedback-info p:last-child {
	margin-bottom: 0;
}

.text-muted {
	color: var(--text-secondary);
	font-size: 13px;
}
</style>


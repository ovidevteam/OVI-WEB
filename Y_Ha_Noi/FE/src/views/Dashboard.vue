<template>
	<div class="dashboard">
		<!-- Stats Cards -->
		<div class="stats-grid">
			<div class="stat-card primary">
				<div class="stat-icon">
					<el-icon><ChatDotRound /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.total }}</div>
					<div class="stat-label">Tổng phản ánh tháng</div>
				</div>
			</div>

			<div class="stat-card warning">
				<div class="stat-icon">
					<el-icon><Loading /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.processing }}</div>
					<div class="stat-label">Đang xử lý</div>
				</div>
			</div>

			<div class="stat-card success">
				<div class="stat-icon">
					<el-icon><CircleCheck /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.completed }}</div>
					<div class="stat-label">Hoàn thành</div>
				</div>
			</div>

			<div class="stat-card danger">
				<div class="stat-icon">
					<el-icon><Warning /></el-icon>
				</div>
				<div class="stat-content">
					<div class="stat-value">{{ stats.overdue }}</div>
					<div class="stat-label">Quá hạn</div>
				</div>
			</div>
		</div>

		<!-- Charts Row -->
		<div class="charts-row">
			<div class="content-card chart-card">
				<div class="content-card-header">
					<h3 class="content-card-title">Phản ánh theo tháng</h3>
					<el-select v-model="selectedYear" size="small" style="width: 100px">
						<el-option
							v-for="year in years"
							:key="year"
							:label="year"
							:value="year"
						/>
					</el-select>
				</div>
				<div class="chart-container">
					<LineChart :data="monthlyData" />
				</div>
			</div>

			<div class="content-card chart-card">
				<div class="content-card-header">
					<h3 class="content-card-title">Top 5 Phòng ban</h3>
				</div>
				<div class="chart-container">
					<BarChart :data="departmentData" />
				</div>
			</div>
		</div>

		<!-- Recent Feedbacks -->
		<div class="content-card">
			<div class="content-card-header">
				<h3 class="content-card-title">Phản ánh mới nhất</h3>
				<el-button type="primary" text @click="goToList">
					Xem tất cả <el-icon class="el-icon--right"><ArrowRight /></el-icon>
				</el-button>
			</div>

			<el-table :data="recentFeedbacks" stripe style="width: 100%">
				<el-table-column prop="code" label="Số PA" width="145" />
				<el-table-column prop="receivedDate" label="Ngày" width="105">
					<template #default="{ row }">
						{{ formatDate(row.receivedDate) }}
					</template>
				</el-table-column>
				<el-table-column prop="content" label="Nội dung" min-width="220">
					<template #default="{ row }">
						<el-tooltip :content="row.content" placement="top" :show-after="500">
							<span class="content-cell">{{ row.content }}</span>
						</el-tooltip>
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng" min-width="100" />
				<el-table-column prop="level" label="Mức độ" width="95" align="center">
					<template #default="{ row }">
						<el-tag :type="getLevelType(row.level)" size="small">
							{{ getLevelLabel(row.level) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="Trạng thái" width="105" align="center">
					<template #default="{ row }">
						<el-tag :type="getStatusType(row.status)" size="small">
							{{ getStatusLabel(row.status) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="" width="50" align="center">
					<template #default="{ row }">
						<el-button
							type="primary"
							text
							size="small"
							@click="viewDetail(row.id)"
						>
							<el-icon><View /></el-icon>
						</el-button>
					</template>
				</el-table-column>
			</el-table>
		</div>
	</div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
	ChatDotRound, Loading, CircleCheck, Warning,
	ArrowRight, View
} from '@element-plus/icons-vue'
import { formatDate, truncate, getLevelLabel, getLevelType, getStatusLabel, getStatusType } from '@/utils/helpers'
import reportService from '@/services/reportService'
import feedbackService from '@/services/feedbackService'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'
import {
	mockDashboardStats,
	mockMonthlyStats,
	mockDepartmentStats,
	mockFeedbacks
} from '@/mock/db'

const DEMO_MODE = import.meta.env.VITE_DEMO_MODE === 'true'

const router = useRouter()

const stats = reactive({
	total: 0,
	processing: 0,
	completed: 0,
	overdue: 0
})

const selectedYear = ref(new Date().getFullYear())
const years = computed(() => {
	const currentYear = new Date().getFullYear()
	return [currentYear - 2, currentYear - 1, currentYear]
})

const monthlyData = ref({
	labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
	datasets: [{
		label: 'Phản ánh',
		data: [],
		borderColor: '#0d6efd',
		backgroundColor: 'rgba(13, 110, 253, 0.1)',
		fill: true,
		tension: 0.4
	}]
})

const departmentData = ref({
	labels: [],
	datasets: [{
		label: 'Số phản ánh',
		data: [],
		backgroundColor: [
			'rgba(13, 110, 253, 0.8)',
			'rgba(25, 135, 84, 0.8)',
			'rgba(255, 193, 7, 0.8)',
			'rgba(220, 53, 69, 0.8)',
			'rgba(108, 117, 125, 0.8)'
		]
	}]
})

const recentFeedbacks = ref([])

const goToList = () => {
	router.push('/feedback')
}

const viewDetail = (id) => {
	router.push(`/feedback/${id}`)
}

const loadDashboardData = async () => {
	if (DEMO_MODE) {
		// Demo data - use mock data from db.js
		Object.assign(stats, mockDashboardStats)
		monthlyData.value.datasets[0].data = mockMonthlyStats
		departmentData.value.labels = mockDepartmentStats.map(d => d.departmentName)
		departmentData.value.datasets[0].data = mockDepartmentStats.map(d => d.count)
		recentFeedbacks.value = mockFeedbacks.slice(0, 5)
		return
	}

	try {
		// Load dashboard stats
		const dashboardData = await reportService.getDashboard()
		if (dashboardData) {
			stats.total = dashboardData.total || 0
			stats.processing = dashboardData.processing || 0
			stats.completed = dashboardData.completed || 0
			stats.overdue = dashboardData.overdue || 0
		}
	} catch (error) {
		console.error('Failed to load dashboard stats:', error)
	}

	try {
		// Load monthly stats
		const monthlyStats = await reportService.getMonthlyStats(selectedYear.value)
		if (monthlyStats && monthlyStats.data) {
			monthlyData.value.datasets[0].data = monthlyStats.data
		}
	} catch (error) {
		console.error('Failed to load monthly stats:', error)
	}

	try {
		// Load department stats (top 5)
		const deptStats = await reportService.getByDepartment({ limit: 5 })
		if (deptStats && deptStats.data) {
			departmentData.value.labels = deptStats.data.map(d => d.departmentName)
			departmentData.value.datasets[0].data = deptStats.data.map(d => d.count)
		}
	} catch (error) {
		console.error('Failed to load department stats:', error)
	}

	try {
		// Load recent feedbacks
		const feedbacks = await feedbackService.getList({ page: 1, size: 5, sort: 'receivedDate,desc' })
		if (feedbacks && feedbacks.data) {
			recentFeedbacks.value = feedbacks.data
		}
	} catch (error) {
		console.error('Failed to load recent feedbacks:', error)
	}
}

onMounted(() => {
	loadDashboardData()
})

// Watch year change
watch(selectedYear, () => {
	loadDashboardData()
})
</script>

<style scoped>
.dashboard {
	animation: fadeIn 0.3s ease-out;
}

.charts-row {
	display: grid;
	grid-template-columns: 1fr 1fr;
	gap: 24px;
	margin-bottom: 24px;
}

.chart-card {
	min-height: 400px;
}

.chart-container {
	height: 320px;
	position: relative;
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
}

@media (max-width: 1200px) {
	.charts-row {
		grid-template-columns: 1fr;
	}
}
</style>


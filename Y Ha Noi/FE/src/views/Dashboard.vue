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
				<el-table-column prop="code" label="Số PA" width="150" />
				<el-table-column prop="receivedDate" label="Ngày" width="120">
					<template #default="{ row }">
						{{ formatDate(row.receivedDate) }}
					</template>
				</el-table-column>
				<el-table-column prop="content" label="Nội dung" min-width="200">
					<template #default="{ row }">
						{{ truncate(row.content, 50) }}
					</template>
				</el-table-column>
				<el-table-column prop="departmentName" label="Phòng" width="150" />
				<el-table-column prop="level" label="Mức độ" width="120" align="center">
					<template #default="{ row }">
						<el-tag :type="getLevelType(row.level)" size="small">
							{{ getLevelLabel(row.level) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column prop="status" label="Trạng thái" width="130" align="center">
					<template #default="{ row }">
						<el-tag :type="getStatusType(row.status)" size="small">
							{{ getStatusLabel(row.status) }}
						</el-tag>
					</template>
				</el-table-column>
				<el-table-column label="" width="80" align="center">
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
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
	ChatDotRound, Loading, CircleCheck, Warning,
	ArrowRight, View
} from '@element-plus/icons-vue'
import { formatDate, truncate, getLevelLabel, getLevelType, getStatusLabel, getStatusType } from '@/utils/helpers'
import reportService from '@/services/reportService'
import LineChart from '@/components/charts/LineChart.vue'
import BarChart from '@/components/charts/BarChart.vue'

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
		data: [12, 19, 15, 25, 22, 30, 28, 35, 40, 33, 45, 38],
		borderColor: '#0d6efd',
		backgroundColor: 'rgba(13, 110, 253, 0.1)',
		fill: true,
		tension: 0.4
	}]
})

const departmentData = ref({
	labels: ['Nội khoa', 'Ngoại khoa', 'Cấp cứu', 'Sản khoa', 'Nhi khoa'],
	datasets: [{
		label: 'Số phản ánh',
		data: [65, 45, 38, 30, 25],
		backgroundColor: [
			'rgba(13, 110, 253, 0.8)',
			'rgba(25, 135, 84, 0.8)',
			'rgba(255, 193, 7, 0.8)',
			'rgba(220, 53, 69, 0.8)',
			'rgba(108, 117, 125, 0.8)'
		]
	}]
})

const recentFeedbacks = ref([
	{
		id: 1,
		code: 'PA-20251127-001',
		receivedDate: '2025-11-27',
		content: 'Phản ánh về thái độ phục vụ của nhân viên khoa Nội',
		departmentName: 'Nội khoa',
		level: 'HIGH',
		status: 'NEW'
	},
	{
		id: 2,
		code: 'PA-20251127-002',
		receivedDate: '2025-11-27',
		content: 'Thời gian chờ khám quá lâu tại phòng khám da liễu',
		departmentName: 'Da liễu',
		level: 'MEDIUM',
		status: 'PROCESSING'
	},
	{
		id: 3,
		code: 'PA-20251126-003',
		receivedDate: '2025-11-26',
		content: 'Cảm ơn bác sĩ đã tận tình điều trị cho bệnh nhân',
		departmentName: 'Ngoại khoa',
		level: 'LOW',
		status: 'COMPLETED'
	}
])

const goToList = () => {
	router.push('/feedback')
}

const viewDetail = (id) => {
	router.push(`/feedback/${id}`)
}

onMounted(async () => {
	// TODO: Fetch real data from API
	stats.total = 156
	stats.processing = 23
	stats.completed = 128
	stats.overdue = 5
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

@media (max-width: 1200px) {
	.charts-row {
		grid-template-columns: 1fr;
	}
}
</style>


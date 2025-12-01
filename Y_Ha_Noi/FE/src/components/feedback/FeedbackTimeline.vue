<template>
	<div class="feedback-timeline">
		<el-timeline>
			<el-timeline-item
				v-for="log in logs"
				:key="log.id"
				:timestamp="formatDateTime(log.createdDate)"
				:type="getTimelineType(log.action)"
				:hollow="log.action === 'CREATE'"
				placement="top"
			>
				<div class="timeline-content">
					<div class="timeline-header">
						<span class="action-label">{{ getActionLabel(log.action) }}</span>
						<span class="user-name">{{ log.userName }}</span>
					</div>
					<div class="timeline-body" v-if="log.note || log.oldStatus">
						<p v-if="log.note">{{ log.note }}</p>
						<p v-if="log.oldStatus && log.newStatus" class="status-change">
							<el-tag :type="getStatusType(log.oldStatus)" size="small">
								{{ getStatusLabel(log.oldStatus) }}
							</el-tag>
							<el-icon class="arrow-icon"><ArrowRight /></el-icon>
							<el-tag :type="getStatusType(log.newStatus)" size="small">
								{{ getStatusLabel(log.newStatus) }}
							</el-tag>
						</p>
					</div>
				</div>
			</el-timeline-item>
		</el-timeline>

		<el-empty v-if="!logs?.length" description="Chưa có lịch sử xử lý" />
	</div>
</template>

<script setup>
import { ArrowRight } from '@element-plus/icons-vue'
import { formatDateTime, getStatusLabel, getStatusType } from '@/utils/helpers'

defineProps({
	logs: {
		type: Array,
		default: () => []
	}
})

const getTimelineType = (action) => {
	const types = {
		'CREATE': 'primary',
		'ASSIGN': 'info',
		'STATUS_CHANGE': 'warning',
		'COMPLETE': 'success',
		'COMMENT': ''
	}
	return types[action] || ''
}

const getActionLabel = (action) => {
	const labels = {
		'CREATE': 'Tạo mới',
		'ASSIGN': 'Phân công',
		'STATUS_CHANGE': 'Thay đổi trạng thái',
		'COMPLETE': 'Hoàn thành',
		'COMMENT': 'Ghi chú'
	}
	return labels[action] || action
}
</script>

<style scoped>
.feedback-timeline {
	padding: 10px 0;
}

.timeline-content {
	padding: 12px 16px;
	background: var(--background-color);
	border-radius: var(--radius-md);
}

.timeline-header {
	display: flex;
	align-items: center;
	gap: 12px;
	margin-bottom: 8px;
}

.action-label {
	font-weight: 600;
	color: var(--secondary-color);
}

.user-name {
	color: var(--text-secondary);
	font-size: 13px;
}

.timeline-body {
	color: var(--text-primary);
	font-size: 14px;
}

.timeline-body p {
	margin: 0;
}

.status-change {
	display: flex;
	align-items: center;
	gap: 8px;
	margin-top: 8px !important;
}

.arrow-icon {
	color: var(--text-muted);
}

:deep(.el-timeline-item__timestamp) {
	font-size: 12px;
}
</style>


<template>
	<Pie :key="chartKey" :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import { Pie } from 'vue-chartjs'
import {
	Chart as ChartJS,
	ArcElement,
	Tooltip,
	Legend
} from 'chart.js'

ChartJS.register(
	ArcElement,
	Tooltip,
	Legend
)

const props = defineProps({
	data: {
		type: Object,
		required: true
	}
})

// Use computed to reactively update chart data
const chartData = computed(() => {
	// Ensure data structure is correct
	if (!props.data || !props.data.labels || !Array.isArray(props.data.labels) || !props.data.datasets || !Array.isArray(props.data.datasets) || props.data.datasets.length === 0) {
		return {
			labels: [],
			datasets: [{
				label: 'Số phản ánh',
				data: [],
				backgroundColor: [
					'rgba(102, 126, 234, 0.8)',
					'rgba(103, 194, 58, 0.8)',
					'rgba(230, 162, 60, 0.8)',
					'rgba(245, 108, 108, 0.8)',
					'rgba(64, 158, 255, 0.8)'
				],
				borderColor: [
					'rgba(102, 126, 234, 1)',
					'rgba(103, 194, 58, 1)',
					'rgba(230, 162, 60, 1)',
					'rgba(245, 108, 108, 1)',
					'rgba(64, 158, 255, 1)'
				],
				borderWidth: 2
			}]
		}
	}
	// Ensure datasets[0].data exists and is an array
	if (!props.data.datasets[0].data || !Array.isArray(props.data.datasets[0].data)) {
		const defaultDataset = props.data.datasets[0] || {
			label: 'Số phản ánh',
			backgroundColor: [
				'rgba(102, 126, 234, 0.8)',
				'rgba(103, 194, 58, 0.8)',
				'rgba(230, 162, 60, 0.8)',
				'rgba(245, 108, 108, 0.8)',
				'rgba(64, 158, 255, 0.8)'
			],
			borderColor: [
				'rgba(102, 126, 234, 1)',
				'rgba(103, 194, 58, 1)',
				'rgba(230, 162, 60, 1)',
				'rgba(245, 108, 108, 1)',
				'rgba(64, 158, 255, 1)'
			],
			borderWidth: 2
		}
		return {
			labels: props.data.labels || [],
			datasets: [{
				...defaultDataset,
				data: []
			}]
		}
	}
	return props.data
})

// Force chart update when data changes
const chartKey = ref(0)
watch(() => props.data, () => {
	chartKey.value++
}, { deep: true })

const chartOptions = {
	responsive: true,
	maintainAspectRatio: false,
	plugins: {
		legend: {
			position: 'bottom',
			labels: {
				padding: 15,
				font: {
					size: 12
				},
				usePointStyle: true,
				pointStyle: 'circle'
			}
		},
		tooltip: {
			backgroundColor: 'rgba(0, 0, 0, 0.8)',
			padding: 12,
			titleFont: {
				size: 14,
				weight: 'bold'
			},
			bodyFont: {
				size: 13
			},
			cornerRadius: 8,
			callbacks: {
				label: function(context) {
					const label = context.label || ''
					const value = context.parsed || 0
					const total = context.dataset.data.reduce((a, b) => a + b, 0)
					const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : 0
					return `${label}: ${value} (${percentage}%)`
				}
			}
		}
	}
}
</script>


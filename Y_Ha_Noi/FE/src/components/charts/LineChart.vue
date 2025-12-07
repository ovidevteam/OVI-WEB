<template>
	<Line :key="chartKey" :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import { Line } from 'vue-chartjs'
import {
	Chart as ChartJS,
	CategoryScale,
	LinearScale,
	PointElement,
	LineElement,
	Title,
	Tooltip,
	Legend,
	Filler
} from 'chart.js'

ChartJS.register(
	CategoryScale,
	LinearScale,
	PointElement,
	LineElement,
	Title,
	Tooltip,
	Legend,
	Filler
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
			labels: ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
			datasets: [{
				label: 'Phản ánh',
				data: [],
				borderColor: '#0d6efd',
				backgroundColor: 'rgba(13, 110, 253, 0.1)',
				fill: true,
				tension: 0.4
			}]
		}
	}
	// Ensure datasets[0].data exists and is an array
	if (!props.data.datasets[0].data || !Array.isArray(props.data.datasets[0].data)) {
		const defaultDataset = props.data.datasets[0] || {
			label: 'Phản ánh',
			borderColor: '#0d6efd',
			backgroundColor: 'rgba(13, 110, 253, 0.1)',
			fill: true,
			tension: 0.4
		}
		return {
			labels: props.data.labels || ['T1', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7', 'T8', 'T9', 'T10', 'T11', 'T12'],
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
			display: false
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
			cornerRadius: 8
		}
	},
	scales: {
		x: {
			grid: {
				display: false
			},
			ticks: {
				font: {
					size: 12
				}
			}
		},
		y: {
			beginAtZero: true,
			grid: {
				color: 'rgba(0, 0, 0, 0.05)'
			},
			ticks: {
				font: {
					size: 12
				}
			}
		}
	},
	elements: {
		point: {
			radius: 4,
			hoverRadius: 6
		}
	}
}
</script>


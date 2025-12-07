<template>
	<Bar :key="chartKey" :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { computed, watch, ref } from 'vue'
import { Bar } from 'vue-chartjs'
import {
	Chart as ChartJS,
	CategoryScale,
	LinearScale,
	BarElement,
	Title,
	Tooltip,
	Legend
} from 'chart.js'

ChartJS.register(
	CategoryScale,
	LinearScale,
	BarElement,
	Title,
	Tooltip,
	Legend
)

// Keep old props for backward compatibility
const props = defineProps({
	data: {
		type: Object,
		required: true
	},
	stacked: {
		type: Boolean,
		default: false
	},
	indexAxis: {
		type: String,
		default: 'y'  // 'y' for horizontal, 'x' for vertical
	},
	showLegend: {
		type: Boolean,
		default: false
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
					'rgba(13, 110, 253, 0.8)',
					'rgba(25, 135, 84, 0.8)',
					'rgba(255, 193, 7, 0.8)',
					'rgba(220, 53, 69, 0.8)',
					'rgba(108, 117, 125, 0.8)'
				]
			}]
		}
	}
	// Ensure datasets[0].data exists and is an array
	if (!props.data.datasets[0].data || !Array.isArray(props.data.datasets[0].data)) {
		const defaultDataset = props.data.datasets[0] || {
			label: 'Số phản ánh',
			backgroundColor: [
				'rgba(13, 110, 253, 0.8)',
				'rgba(25, 135, 84, 0.8)',
				'rgba(255, 193, 7, 0.8)',
				'rgba(220, 53, 69, 0.8)',
				'rgba(108, 117, 125, 0.8)'
			]
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

const chartOptions = computed(() => {
	const isStacked = props.stacked || false
	const axis = props.indexAxis || 'y'
	
	return {
		responsive: true,
		maintainAspectRatio: false,
		indexAxis: axis,
		plugins: {
			legend: {
				display: props.showLegend !== undefined ? props.showLegend : false,
				position: 'top',
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
				mode: isStacked ? 'index' : 'nearest',
				intersect: false,
				callbacks: isStacked ? {
					footer: (tooltipItems) => {
						let total = 0
						tooltipItems.forEach(item => {
							total += item.parsed[axis === 'x' ? 'y' : 'x']
						})
						return `Tổng: ${total}`
					}
				} : undefined
			}
		},
		scales: {
			x: {
				beginAtZero: true,
				stacked: isStacked,
				grid: {
					color: 'rgba(0, 0, 0, 0.05)'
				},
				ticks: {
					font: {
						size: 12
					}
				}
			},
			y: {
				stacked: isStacked,
				beginAtZero: true,
				grid: {
					display: axis === 'x',
					color: axis === 'x' ? 'rgba(0, 0, 0, 0.05)' : 'transparent'
				},
				ticks: {
					font: {
						size: 12
					}
				}
			}
		}
	}
})
</script>


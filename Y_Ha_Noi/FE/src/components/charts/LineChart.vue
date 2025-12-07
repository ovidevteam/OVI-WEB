<template>
	<Line :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { computed, watch, ref, shallowRef } from 'vue'
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

// Use shallowRef for better performance with large datasets
const chartData = shallowRef(props.data)
let updateTimeout = null

// Memoize chart data and debounce updates
watch(() => props.data, (newData) => {
	if (updateTimeout) {
		clearTimeout(updateTimeout)
	}
	
	// Debounce updates to prevent excessive re-renders
	updateTimeout = setTimeout(() => {
		// Only update if data actually changed
		if (JSON.stringify(chartData.value) !== JSON.stringify(newData)) {
			chartData.value = { ...newData }
		}
	}, 100)
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


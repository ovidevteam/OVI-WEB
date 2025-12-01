<template>
	<Line :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { computed } from 'vue'
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

const chartData = computed(() => props.data)

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


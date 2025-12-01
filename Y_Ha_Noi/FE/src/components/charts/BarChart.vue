<template>
	<Bar :data="chartData" :options="chartOptions" />
</template>

<script setup>
import { computed } from 'vue'
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
	indexAxis: 'y',
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
			beginAtZero: true,
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
			grid: {
				display: false
			},
			ticks: {
				font: {
					size: 12
				}
			}
		}
	}
}
</script>


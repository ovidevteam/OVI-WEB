import dayjs from 'dayjs'
import { CHANNELS, LEVELS, STATUS, ROLES, USER_STATUS } from './constants'

// Format date
export function formatDate(date, format = 'DD/MM/YYYY') {
	if (!date) return ''
	return dayjs(date).format(format)
}

// Format datetime
export function formatDateTime(date) {
	return formatDate(date, 'DD/MM/YYYY HH:mm')
}

// Get relative time
export function getRelativeTime(date) {
	if (!date) return ''
	const now = dayjs()
	const target = dayjs(date)
	const diffDays = now.diff(target, 'day')

	if (diffDays === 0) return 'Hôm nay'
	if (diffDays === 1) return 'Hôm qua'
	if (diffDays <= 7) return `${diffDays} ngày trước`
	return formatDate(date)
}

// Check if overdue (more than 3 days)
export function isOverdue(date, status) {
	if (!date || status === 'COMPLETED') return false
	const diffDays = dayjs().diff(dayjs(date), 'day')
	return diffDays > 3
}

// Get label from value in array
export function getLabelByValue(array, value, defaultLabel = '') {
	const item = array.find(i => i.value === value)
	return item?.label || defaultLabel
}

// Get color from value in array
export function getColorByValue(array, value, defaultColor = '#909399') {
	const item = array.find(i => i.value === value)
	return item?.color || defaultColor
}

// Get type from value in array
export function getTypeByValue(array, value, defaultType = 'info') {
	const item = array.find(i => i.value === value)
	return item?.type || defaultType
}

// Shorthand helpers
export const getChannelLabel = (value) => getLabelByValue(CHANNELS, value)
export const getChannelColor = (value) => getColorByValue(CHANNELS, value)
export const getLevelLabel = (value) => getLabelByValue(LEVELS, value)
export const getLevelColor = (value) => getColorByValue(LEVELS, value)
export const getLevelType = (value) => getTypeByValue(LEVELS, value)
export const getStatusLabel = (value) => getLabelByValue(STATUS, value)
export const getStatusColor = (value) => getColorByValue(STATUS, value)
export const getStatusType = (value) => getTypeByValue(STATUS, value)
export const getRoleLabel = (value) => getLabelByValue(ROLES, value)
export const getRoleColor = (value) => getColorByValue(ROLES, value)
export const getUserStatusLabel = (value) => getLabelByValue(USER_STATUS, value)
export const getUserStatusType = (value) => getTypeByValue(USER_STATUS, value)

// Truncate text
export function truncate(text, length = 50) {
	if (!text) return ''
	if (text.length <= length) return text
	return text.substring(0, length) + '...'
}

// Generate feedback code
export function generateFeedbackCode() {
	const date = dayjs().format('YYYYMMDD')
	const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
	return `PA-${date}-${random}`
}

// Download blob file
export function downloadBlob(blob, filename) {
	const url = window.URL.createObjectURL(blob)
	const link = document.createElement('a')
	link.href = url
	link.setAttribute('download', filename)
	document.body.appendChild(link)
	link.click()
	link.remove()
	window.URL.revokeObjectURL(url)
}

// Format file size
export function formatFileSize(bytes) {
	if (bytes === 0) return '0 Bytes'
	const k = 1024
	const sizes = ['Bytes', 'KB', 'MB', 'GB']
	const i = Math.floor(Math.log(bytes) / Math.log(k))
	return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// Debounce function
export function debounce(func, wait = 300) {
	let timeout
	return function executedFunction(...args) {
		const later = () => {
			clearTimeout(timeout)
			func(...args)
		}
		clearTimeout(timeout)
		timeout = setTimeout(later, wait)
	}
}

// Deep clone object
export function deepClone(obj) {
	return JSON.parse(JSON.stringify(obj))
}

// Check if object is empty
export function isEmpty(obj) {
	if (!obj) return true
	if (Array.isArray(obj)) return obj.length === 0
	if (typeof obj === 'object') return Object.keys(obj).length === 0
	return false
}


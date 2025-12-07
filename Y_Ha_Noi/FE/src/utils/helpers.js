import dayjs from 'dayjs'
import { CHANNELS, LEVELS, STATUS, ROLES, USER_STATUS } from './constants'

/**
 * Format date according to Vietnamese format
 * @param {string|Date} date - Date to format
 * @param {string} format - Date format pattern (default: 'DD/MM/YYYY')
 * @returns {string} Formatted date string
 * @example
 * formatDate('2025-01-27') // '27/01/2025'
 * formatDate(new Date(), 'DD/MM/YYYY HH:mm') // '27/01/2025 14:30'
 */
export function formatDate(date, format = 'DD/MM/YYYY') {
	if (!date) return ''
	return dayjs(date).format(format)
}

/**
 * Format date and time
 * @param {string|Date} date - Date to format
 * @returns {string} Formatted datetime string (DD/MM/YYYY HH:mm:ss)
 * @example
 * formatDateTime('2025-01-27T14:30:00') // '27/01/2025 14:30:00'
 */
export function formatDateTime(date) {
	return formatDate(date, 'DD/MM/YYYY HH:mm:ss')
}

/**
 * Get relative time description (e.g., "Hôm nay", "2 ngày trước")
 * @param {string|Date} date - Date to compare
 * @returns {string} Relative time description
 * @example
 * getRelativeTime('2025-01-27') // 'Hôm nay' (if today)
 * getRelativeTime('2025-01-25') // '2 ngày trước'
 */
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

/**
 * Check if a task/feedback is overdue (more than 3 days)
 * @param {string|Date} date - Date to check
 * @param {string} status - Current status (e.g., 'COMPLETED')
 * @returns {boolean} True if overdue, false otherwise
 * @example
 * isOverdue('2025-01-20', 'NEW') // true (if today is 2025-01-27)
 * isOverdue('2025-01-20', 'COMPLETED') // false
 */
export function isOverdue(date, status) {
	if (!date || status === 'COMPLETED') return false
	const diffDays = dayjs().diff(dayjs(date), 'day')
	return diffDays > 3
}

/**
 * Get label from value in a constant array
 * @param {Array} array - Array of objects with value and label properties
 * @param {string} value - Value to search for
 * @param {string} defaultLabel - Default label if not found
 * @returns {string} Label corresponding to the value
 * @example
 * getLabelByValue(STATUS, 'NEW') // 'Chưa xử lý'
 */
export function getLabelByValue(array, value, defaultLabel = '') {
	const item = array.find(i => i.value === value)
	return item?.label || defaultLabel
}

/**
 * Get color from value in a constant array
 * @param {Array} array - Array of objects with value and color properties
 * @param {string} value - Value to search for
 * @param {string} defaultColor - Default color if not found
 * @returns {string} Color corresponding to the value
 * @example
 * getColorByValue(LEVELS, 'CRITICAL') // '#F56C6C'
 */
export function getColorByValue(array, value, defaultColor = '#909399') {
	const item = array.find(i => i.value === value)
	return item?.color || defaultColor
}

/**
 * Get type from value in a constant array (for Element Plus tag types)
 * @param {Array} array - Array of objects with value and type properties
 * @param {string} value - Value to search for
 * @param {string} defaultType - Default type if not found
 * @returns {string} Type corresponding to the value (e.g., 'danger', 'warning', 'success')
 * @example
 * getTypeByValue(LEVELS, 'CRITICAL') // 'danger'
 */
export function getTypeByValue(array, value, defaultType = 'info') {
	const item = array.find(i => i.value === value)
	return item?.type || defaultType
}

// Shorthand helpers
export const getChannelLabel = (value) => {
	// Map backend enum values to frontend values
	const backendToFrontend = {
		'PHONE': 'HOTLINE',
		'EMAIL': 'EMAIL',
		'DIRECT': 'DIRECT',
		'WEBSITE': 'OTHER'
	}
	// If value is backend enum, map it first
	const frontendValue = backendToFrontend[value] || value
	return getLabelByValue(CHANNELS, frontendValue) || value
}
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

/**
 * Truncate text to specified length with ellipsis
 * @param {string} text - Text to truncate
 * @param {number} length - Maximum length (default: 50)
 * @returns {string} Truncated text with '...' if needed
 * @example
 * truncate('Very long text here', 10) // 'Very long ...'
 */
export function truncate(text, length = 50) {
	if (!text) return ''
	if (text.length <= length) return text
	return text.substring(0, length) + '...'
}

/**
 * Generate a unique feedback code
 * Format: PA-YYYYMMDD-XXX (e.g., PA-20250127-001)
 * @returns {string} Generated feedback code
 * @example
 * generateFeedbackCode() // 'PA-20250127-042'
 */
export function generateFeedbackCode() {
	const date = dayjs().format('YYYYMMDD')
	const random = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
	return `PA-${date}-${random}`
}

/**
 * Download a blob file
 * @param {Blob} blob - Blob object to download
 * @param {string} filename - Filename for the download
 * @example
 * downloadBlob(blob, 'report.pdf')
 */
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

/**
 * Format file size in human-readable format
 * @param {number} bytes - File size in bytes
 * @returns {string} Formatted file size (e.g., '1.5 MB')
 * @example
 * formatFileSize(1024) // '1 KB'
 * formatFileSize(1048576) // '1 MB'
 */
export function formatFileSize(bytes) {
	if (bytes === 0) return '0 Bytes'
	const k = 1024
	const sizes = ['Bytes', 'KB', 'MB', 'GB']
	const i = Math.floor(Math.log(bytes) / Math.log(k))
	return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

/**
 * Debounce function - delays execution until after wait time
 * @param {Function} func - Function to debounce
 * @param {number} wait - Wait time in milliseconds (default: 300)
 * @returns {Function} Debounced function
 * @example
 * const debouncedSearch = debounce(searchFunction, 500)
 * debouncedSearch('query') // Will only execute after 500ms of no calls
 */
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

/**
 * Deep clone an object using JSON serialization
 * Note: Does not preserve functions, undefined, or circular references
 * @param {*} obj - Object to clone
 * @returns {*} Cloned object
 * @example
 * const cloned = deepClone({ a: 1, b: { c: 2 } })
 */
export function deepClone(obj) {
	return JSON.parse(JSON.stringify(obj))
}

/**
 * Check if an object/array is empty
 * @param {*} obj - Object, array, or value to check
 * @returns {boolean} True if empty, false otherwise
 * @example
 * isEmpty({}) // true
 * isEmpty([]) // true
 * isEmpty({ a: 1 }) // false
 */
export function isEmpty(obj) {
	if (!obj) return true
	if (Array.isArray(obj)) return obj.length === 0
	if (typeof obj === 'object') return Object.keys(obj).length === 0
	return false
}


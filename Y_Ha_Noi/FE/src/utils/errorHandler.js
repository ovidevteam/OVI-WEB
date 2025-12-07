import { ElMessage } from 'element-plus'

/**
 * Error reporting interface
 * Can be extended to integrate with Sentry, LogRocket, etc.
 */
const errorReporter = {
	/**
	 * Report error to external service
	 * @param {Error} error - The error object
	 * @param {Object} context - Additional context
	 */
	report(error, context = {}) {
		// In production, integrate with error reporting service
		// Example: Sentry.captureException(error, { extra: context })
		
		// For now, log to console in development
		if (import.meta.env.DEV) {
			console.error('[Error Reporter]', {
				error,
				context,
				timestamp: new Date().toISOString(),
				url: window.location.href,
				userAgent: navigator.userAgent
			})
		}
	}
}

/**
 * Centralized error handling utility
 * Handles errors consistently across the application
 *
 * @param {Error} error - The error object
 * @param {string} context - Context where the error occurred (e.g., 'Login', 'FetchData')
 * @param {Object} options - Additional options
 * @param {boolean} options.showMessage - Whether to show user-friendly message (default: true)
 * @param {boolean} options.logError - Whether to log error to console (default: true)
 * @param {boolean} options.reportError - Whether to report error to external service (default: true)
 * @returns {Object} Error information object
 */
export function handleError(error, context = '', options = {}) {
	const {
		showMessage = true,
		logError = true,
		reportError = true
	} = options

	// Log error in development mode
	if (logError && import.meta.env.DEV) {
		console.error(`[${context || 'Error'}]`, error)
	}

	// Extract error message
	let message = 'Đã có lỗi xảy ra'
	let errorLevel = 'error'

	if (error?.response) {
		// Axios error with response
		const { status, data } = error.response
		
		// Debug: log full error response in development
		if (import.meta.env.DEV) {
			console.error('Error response:', {
				status,
				data: JSON.stringify(data, null, 2),
				message: data?.message,
				errors: data?.errors,
				headers: error.response.headers
			})
		}

		switch (status) {
			case 400:
				// Try to extract validation error messages
				if (data?.errors) {
					// Handle both array and object format
					let errorMessages = []
					if (Array.isArray(data.errors)) {
						errorMessages = data.errors
					} else if (typeof data.errors === 'object') {
						errorMessages = Object.values(data.errors).flat()
					}
					message = errorMessages.length > 0 
						? errorMessages.join(', ') 
						: data?.message || 'Dữ liệu không hợp lệ'
				} else {
					message = data?.message || 'Dữ liệu không hợp lệ'
				}
				errorLevel = 'warning'
				break
			case 401:
				message = 'Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.'
				errorLevel = 'error'
				break
			case 403:
				message = 'Bạn không có quyền thực hiện thao tác này.'
				errorLevel = 'warning'
				break
			case 404:
				message = 'Không tìm thấy dữ liệu.'
				errorLevel = 'info'
				break
			case 422:
				message = data?.message || 'Dữ liệu không hợp lệ'
				errorLevel = 'warning'
				break
			case 500:
				message = 'Lỗi máy chủ. Vui lòng thử lại sau.'
				errorLevel = 'error'
				break
			case 503:
				message = 'Dịch vụ tạm thời không khả dụng. Vui lòng thử lại sau.'
				errorLevel = 'error'
				break
			default:
				message = data?.message || error.message || message
		}
	} else if (error?.message) {
		// Standard error with message
		message = error.message
	}

	// Report error to external service (only for errors, not warnings/info)
	if (reportError && errorLevel === 'error') {
		errorReporter.report(error, {
			context,
			message,
			status: error?.response?.status,
			code: error?.code,
			url: window.location.href
		})
	}

	// Show user-friendly message
	if (showMessage) {
		const messageType = errorLevel === 'error' ? 'error' : errorLevel === 'warning' ? 'warning' : 'info'
		ElMessage[messageType](message)
	}

	// Return error information for further processing
	return {
		message,
		status: error?.response?.status,
		code: error?.code,
		level: errorLevel,
		originalError: error
	}
}

/**
 * Handle API errors specifically
 * @param {Error} error - The error object
 * @param {string} context - Context where the error occurred
 * @returns {Object} Error information object
 */
export function handleApiError(error, context = '') {
	return handleError(error, context, {
		showMessage: true,
		logError: true
	})
}

/**
 * Handle validation errors
 * @param {Error} error - The validation error object
 * @param {string} field - The field name that failed validation
 * @returns {Object} Error information object
 */
export function handleValidationError(error, field = '') {
	const message = error?.message || `Trường ${field} không hợp lệ`
	ElMessage.warning(message)

	return {
		message,
		field,
		originalError: error
	}
}


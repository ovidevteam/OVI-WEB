// Email validation
export function isValidEmail(email) {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
	return emailRegex.test(email)
}

// Phone validation (Vietnam)
export function isValidPhone(phone) {
	const phoneRegex = /^(0|\+84)[0-9]{9,10}$/
	return phoneRegex.test(phone?.replace(/\s/g, ''))
}

// Password validation (min 8 chars, has letter and number)
export function isValidPassword(password) {
	const hasLetter = /[a-zA-Z]/.test(password)
	const hasNumber = /[0-9]/.test(password)
	return password.length >= 8 && hasLetter && hasNumber
}

// Username validation (6-20 chars, no special chars)
export function isValidUsername(username) {
	const usernameRegex = /^[a-zA-Z0-9_]{6,20}$/
	return usernameRegex.test(username)
}

// Required field
export function isRequired(value) {
	if (Array.isArray(value)) return value.length > 0
	if (typeof value === 'string') return value.trim().length > 0
	return value !== null && value !== undefined
}

// Max length
export function maxLength(value, max) {
	return !value || value.length <= max
}

// Min length
export function minLength(value, min) {
	return value && value.length >= min
}

// Element Plus form rules generator
export const rules = {
	required: (message = 'Trường này là bắt buộc') => ({
		required: true,
		message,
		trigger: 'blur'
	}),

	email: (message = 'Email không hợp lệ') => ({
		type: 'email',
		message,
		trigger: 'blur'
	}),

	phone: (message = 'Số điện thoại không hợp lệ') => ({
		validator: (rule, value, callback) => {
			if (!value || isValidPhone(value)) {
				callback()
			} else {
				callback(new Error(message))
			}
		},
		trigger: 'blur'
	}),

	password: (message = 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ và số') => ({
		validator: (rule, value, callback) => {
			if (!value || isValidPassword(value)) {
				callback()
			} else {
				callback(new Error(message))
			}
		},
		trigger: 'blur'
	}),

	username: (message = 'Username phải có 6-20 ký tự, không dấu') => ({
		validator: (rule, value, callback) => {
			if (!value || isValidUsername(value)) {
				callback()
			} else {
				callback(new Error(message))
			}
		},
		trigger: 'blur'
	}),

	maxLength: (max, message) => ({
		max,
		message: message || `Không được vượt quá ${max} ký tự`,
		trigger: 'blur'
	}),

	minLength: (min, message) => ({
		min,
		message: message || `Phải có ít nhất ${min} ký tự`,
		trigger: 'blur'
	})
}


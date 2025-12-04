/**
 * Validate email address
 * @param {string} email - Email to validate
 * @returns {boolean} True if valid email, false otherwise
 * @example
 * isValidEmail('user@example.com') // true
 * isValidEmail('invalid-email') // false
 */
export function isValidEmail(email) {
	const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
	return emailRegex.test(email)
}

/**
 * Validate Vietnamese phone number
 * Accepts formats: 0xxxxxxxxx or +84xxxxxxxxx
 * @param {string} phone - Phone number to validate
 * @returns {boolean} True if valid phone, false otherwise
 * @example
 * isValidPhone('0912345678') // true
 * isValidPhone('+84912345678') // true
 */
export function isValidPhone(phone) {
	const phoneRegex = /^(0|\+84)[0-9]{9,10}$/
	return phoneRegex.test(phone?.replace(/\s/g, ''))
}

/**
 * Validate password strength
 * Requirements: minimum 8 characters, at least one letter and one number
 * @param {string} password - Password to validate
 * @returns {boolean} True if valid password, false otherwise
 * @example
 * isValidPassword('password123') // true
 * isValidPassword('weak') // false
 */
export function isValidPassword(password) {
	const hasLetter = /[a-zA-Z]/.test(password)
	const hasNumber = /[0-9]/.test(password)
	return password.length >= 8 && hasLetter && hasNumber
}

/**
 * Validate username
 * Requirements: 6-20 characters, alphanumeric and underscore only
 * @param {string} username - Username to validate
 * @returns {boolean} True if valid username, false otherwise
 * @example
 * isValidUsername('user123') // true
 * isValidUsername('user@name') // false
 */
export function isValidUsername(username) {
	const usernameRegex = /^[a-zA-Z0-9_]{6,20}$/
	return usernameRegex.test(username)
}

/**
 * Check if a field is required (has value)
 * @param {*} value - Value to check
 * @returns {boolean} True if has value, false otherwise
 * @example
 * isRequired('text') // true
 * isRequired('') // false
 * isRequired([]) // false
 */
export function isRequired(value) {
	if (Array.isArray(value)) return value.length > 0
	if (typeof value === 'string') return value.trim().length > 0
	return value !== null && value !== undefined
}

/**
 * Check if value length is within maximum limit
 * @param {string} value - Value to check
 * @param {number} max - Maximum length
 * @returns {boolean} True if within limit or empty, false otherwise
 * @example
 * maxLength('hello', 10) // true
 * maxLength('very long text', 5) // false
 */
export function maxLength(value, max) {
	return !value || value.length <= max
}

/**
 * Check if value length meets minimum requirement
 * @param {string} value - Value to check
 * @param {number} min - Minimum length
 * @returns {boolean} True if meets minimum, false otherwise
 * @example
 * minLength('hello', 3) // true
 * minLength('hi', 5) // false
 */
export function minLength(value, min) {
	return value && value.length >= min
}

/**
 * Element Plus form validation rules generator
 * Provides pre-configured validation rules for common form fields
 *
 * @example
 * const formRules = {
 *   email: [rules.required(), rules.email()],
 *   phone: [rules.required(), rules.phone()],
 *   password: [rules.required(), rules.password()]
 * }
 */
export const rules = {
	/**
	 * Required field rule
	 * @param {string} message - Error message (default: 'Trường này là bắt buộc')
	 * @returns {Object} Element Plus validation rule
	 */
	required: (message = 'Trường này là bắt buộc') => ({
		required: true,
		message,
		trigger: 'blur'
	}),

	/**
	 * Email validation rule
	 * @param {string} message - Error message (default: 'Email không hợp lệ')
	 * @returns {Object} Element Plus validation rule
	 */
	email: (message = 'Email không hợp lệ') => ({
		type: 'email',
		message,
		trigger: 'blur'
	}),

	/**
	 * Vietnamese phone number validation rule
	 * @param {string} message - Error message (default: 'Số điện thoại không hợp lệ')
	 * @returns {Object} Element Plus validation rule
	 */
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

	/**
	 * Password strength validation rule
	 * @param {string} message - Error message (default: 'Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ và số')
	 * @returns {Object} Element Plus validation rule
	 */
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

	/**
	 * Username validation rule
	 * @param {string} message - Error message (default: 'Username phải có 6-20 ký tự, không dấu')
	 * @returns {Object} Element Plus validation rule
	 */
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

	/**
	 * Maximum length validation rule
	 * @param {number} max - Maximum length
	 * @param {string} message - Error message (optional)
	 * @returns {Object} Element Plus validation rule
	 */
	maxLength: (max, message) => ({
		max,
		message: message || `Không được vượt quá ${max} ký tự`,
		trigger: 'blur'
	}),

	/**
	 * Minimum length validation rule
	 * @param {number} min - Minimum length
	 * @param {string} message - Error message (optional)
	 * @returns {Object} Element Plus validation rule
	 */
	minLength: (min, message) => ({
		min,
		message: message || `Phải có ít nhất ${min} ký tự`,
		trigger: 'blur'
	})
}


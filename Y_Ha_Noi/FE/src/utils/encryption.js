import CryptoJS from 'crypto-js'

const SECRET = import.meta.env.VITE_ENCRYPT_SECRET || 'default-secret-key-change-in-production'

/**
 * Encrypt a token before storing in localStorage
 * @param {string} token - The token to encrypt
 * @returns {string} Encrypted token
 */
export function encryptToken(token) {
	if (!token) return ''
	try {
		return CryptoJS.AES.encrypt(token, SECRET).toString()
	} catch (error) {
		console.error('Encryption error:', error)
		return token // Return original if encryption fails
	}
}

/**
 * Decrypt a token from localStorage
 * @param {string} encryptedToken - The encrypted token
 * @returns {string} Decrypted token
 */
export function decryptToken(encryptedToken) {
	if (!encryptedToken) return ''
	try {
		const bytes = CryptoJS.AES.decrypt(encryptedToken, SECRET)
		const decrypted = bytes.toString(CryptoJS.enc.Utf8)
		// If decryption fails, return original (might be unencrypted from old version)
		return decrypted || encryptedToken
	} catch (error) {
		console.error('Decryption error:', error)
		// Return original if decryption fails (might be unencrypted)
		return encryptedToken
	}
}

/**
 * Encrypt any string value
 * @param {string} value - The value to encrypt
 * @returns {string} Encrypted value
 */
export function encrypt(value) {
	if (!value) return ''
	try {
		return CryptoJS.AES.encrypt(value, SECRET).toString()
	} catch (error) {
		console.error('Encryption error:', error)
		return value
	}
}

/**
 * Decrypt any string value
 * @param {string} encryptedValue - The encrypted value
 * @returns {string} Decrypted value
 */
export function decrypt(encryptedValue) {
	if (!encryptedValue) return ''
	try {
		const bytes = CryptoJS.AES.decrypt(encryptedValue, SECRET)
		const decrypted = bytes.toString(CryptoJS.enc.Utf8)
		return decrypted || encryptedValue
	} catch (error) {
		console.error('Decryption error:', error)
		return encryptedValue
	}
}


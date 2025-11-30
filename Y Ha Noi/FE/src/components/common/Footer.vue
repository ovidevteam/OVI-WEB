<template>
	<footer class="app-footer">
		<div class="footer-content">
			<span class="copyright">
				© {{ currentYear }} Bệnh viện ĐH Y Hà Nội. All rights reserved.
			</span>
			<span class="developed-by">
				Developed by <a href="#" target="_blank">OVI Team</a>
			</span>
		</div>
	</footer>
</template>

<script setup>
import { computed } from 'vue'

const currentYear = computed(() => new Date().getFullYear())
</script>

<style scoped>
.app-footer {
	padding: 16px 24px;
	background: var(--card-background);
	border-top: 1px solid var(--border-color);
}

.footer-content {
	display: flex;
	justify-content: space-between;
	align-items: center;
	font-size: 13px;
	color: var(--text-secondary);
}

.copyright {
	display: flex;
	align-items: center;
	gap: 4px;
}

.developed-by a {
	color: var(--primary-color);
	font-weight: 500;
}

.developed-by a:hover {
	text-decoration: underline;
}

@media (max-width: 768px) {
	.footer-content {
		flex-direction: column;
		gap: 8px;
		text-align: center;
	}
}
</style>


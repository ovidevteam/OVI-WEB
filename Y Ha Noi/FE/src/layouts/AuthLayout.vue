<template>
	<div class="auth-layout">
		<div class="auth-background">
			<div class="auth-overlay"></div>
			<div class="auth-shapes">
				<div class="shape shape-1"></div>
				<div class="shape shape-2"></div>
				<div class="shape shape-3"></div>
				<div class="shape shape-4"></div>
			</div>
		</div>
		<div class="auth-container">
			<router-view />
		</div>
	</div>
</template>

<script setup>
</script>

<style scoped>
.auth-layout {
	min-height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
	overflow: hidden;
}

/* Background theo mẫu benhviendaihocyhanoi.isofhcare.vn */
.auth-background {
	position: absolute;
	inset: 0;
	background: linear-gradient(135deg, #1a365d 0%, #2563eb 50%, #0891b2 100%);
	z-index: 0;
}

.auth-overlay {
	position: absolute;
	inset: 0;
	background:
		radial-gradient(ellipse at 20% 80%, rgba(6, 182, 212, 0.3) 0%, transparent 50%),
		radial-gradient(ellipse at 80% 20%, rgba(37, 99, 235, 0.3) 0%, transparent 50%),
		radial-gradient(ellipse at 50% 50%, rgba(255, 255, 255, 0.05) 0%, transparent 70%);
}

.auth-shapes {
	position: absolute;
	inset: 0;
	overflow: hidden;
}

.shape {
	position: absolute;
	border-radius: 50%;
	background: rgba(255, 255, 255, 0.05);
	animation: float 20s ease-in-out infinite;
}

.shape-1 {
	width: 400px;
	height: 400px;
	top: -100px;
	left: -100px;
	animation-delay: 0s;
}

.shape-2 {
	width: 300px;
	height: 300px;
	bottom: -50px;
	right: -50px;
	animation-delay: -5s;
}

.shape-3 {
	width: 200px;
	height: 200px;
	top: 50%;
	left: 10%;
	animation-delay: -10s;
}

.shape-4 {
	width: 150px;
	height: 150px;
	top: 20%;
	right: 15%;
	animation-delay: -15s;
}

@keyframes float {
	0%, 100% { transform: translate(0, 0) scale(1); }
	25% { transform: translate(20px, -20px) scale(1.05); }
	50% { transform: translate(-10px, 10px) scale(0.95); }
	75% { transform: translate(15px, 15px) scale(1.02); }
}

.auth-container {
	position: relative;
	z-index: 1;
	width: 100%;
	max-width: 440px;
	padding: 20px;
}

@media (max-width: 480px) {
	.auth-container {
		padding: 16px;
	}
}
</style>


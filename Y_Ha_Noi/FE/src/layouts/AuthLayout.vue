<template>
	<div class="auth-layout">
		<!-- Layer 1: Background Image -->
		<div class="auth-bg-image"></div>
		<!-- Layer 2: Bubble shapes (phía trên ảnh) -->
		<div class="auth-shapes">
			<div class="shape shape-1"></div>
			<div class="shape shape-2"></div>
			<div class="shape shape-3"></div>
			<div class="shape shape-4"></div>
			<div class="shape shape-5"></div>
			<div class="shape shape-6"></div>
		</div>
		<!-- Layer 3: Content -->
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

/* Layer 1: Background Image - Ảnh bệnh viện */
.auth-bg-image {
	position: absolute;
	inset: 0;
	z-index: 0;
	background-image: url('/images/bg-dhy.png');
	background-size: cover;
	background-position: center;
	background-repeat: no-repeat;
}

/* Layer 2: Bubble shapes - Phía trên ảnh */
.auth-shapes {
	position: absolute;
	inset: 0;
	z-index: 1;
	overflow: hidden;
	pointer-events: none;
}

.shape {
	position: absolute;
	border-radius: 50%;
	backdrop-filter: blur(2px);
	animation: float 20s ease-in-out infinite;
}

/* Bong bóng xanh dương đậm */
.shape-1 {
	width: 400px;
	height: 400px;
	top: -100px;
	left: -100px;
	background: rgba(37, 99, 235, 0.15);
	border: 1px solid rgba(37, 99, 235, 0.2);
	animation-delay: 0s;
}

/* Bong bóng xanh cyan */
.shape-2 {
	width: 300px;
	height: 300px;
	bottom: -50px;
	right: -50px;
	background: rgba(6, 182, 212, 0.12);
	border: 1px solid rgba(6, 182, 212, 0.2);
	animation-delay: -5s;
}

/* Bong bóng xanh navy */
.shape-3 {
	width: 200px;
	height: 200px;
	top: 50%;
	left: 10%;
	background: rgba(26, 54, 93, 0.1);
	border: 1px solid rgba(26, 54, 93, 0.15);
	animation-delay: -10s;
}

/* Bong bóng xanh lá nhạt */
.shape-4 {
	width: 150px;
	height: 150px;
	top: 20%;
	right: 15%;
	background: rgba(16, 185, 129, 0.1);
	border: 1px solid rgba(16, 185, 129, 0.15);
	animation-delay: -15s;
}

/* Bong bóng xanh dương nhạt */
.shape-5 {
	width: 100px;
	height: 100px;
	bottom: 30%;
	left: 20%;
	background: rgba(59, 130, 246, 0.12);
	border: 1px solid rgba(59, 130, 246, 0.18);
	animation-delay: -7s;
}

/* Bong bóng tím nhạt */
.shape-6 {
	width: 80px;
	height: 80px;
	top: 60%;
	right: 25%;
	background: rgba(139, 92, 246, 0.1);
	border: 1px solid rgba(139, 92, 246, 0.15);
	animation-delay: -12s;
}

@keyframes float {
	0%, 100% { transform: translate(0, 0) scale(1); }
	25% { transform: translate(20px, -20px) scale(1.05); }
	50% { transform: translate(-10px, 10px) scale(0.95); }
	75% { transform: translate(15px, 15px) scale(1.02); }
}

/* Layer 3: Content container */
.auth-container {
	position: relative;
	z-index: 2;
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


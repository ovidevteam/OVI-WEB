<template>
	<router-view />
	<SessionTimeoutWarning />
</template>

<script setup>
import SessionTimeoutWarning from '@/components/common/SessionTimeoutWarning.vue'
// Auth initialization is handled in main.js
</script>

<style>
html, body, #app {
	height: 100%;
	margin: 0;
	padding: 0;
}
</style>


<template>
	<router-view />
</template>

<script setup>
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

// Initialize auth state from localStorage
authStore.initAuth()
</script>

<style>
html, body, #app {
	height: 100%;
	margin: 0;
	padding: 0;
}
</style>


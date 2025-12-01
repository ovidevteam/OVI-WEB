import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useUIStore = defineStore('ui', () => {
	const sidebarCollapsed = ref(false)
	const isMobile = ref(false)
	const breadcrumbs = ref([])
	const pageTitle = ref('')
	const globalLoading = ref(false)

	const sidebarWidth = computed(() => sidebarCollapsed.value ? '64px' : '240px')

	function toggleSidebar() {
		sidebarCollapsed.value = !sidebarCollapsed.value
	}

	function collapseSidebar() {
		sidebarCollapsed.value = true
	}

	function expandSidebar() {
		sidebarCollapsed.value = false
	}

	function setMobile(value) {
		isMobile.value = value
		if (value) {
			sidebarCollapsed.value = true
		}
	}

	function setBreadcrumbs(items) {
		breadcrumbs.value = items
	}

	function setPageTitle(title) {
		pageTitle.value = title
		document.title = title + ' - Quản lý Phản ánh'
	}

	function setGlobalLoading(loading) {
		globalLoading.value = loading
	}

	return {
		sidebarCollapsed,
		isMobile,
		breadcrumbs,
		pageTitle,
		globalLoading,
		sidebarWidth,
		toggleSidebar,
		collapseSidebar,
		expandSidebar,
		setMobile,
		setBreadcrumbs,
		setPageTitle,
		setGlobalLoading
	}
})


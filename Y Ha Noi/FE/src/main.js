import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import vi from 'element-plus/es/locale/lang/vi'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import App from './App.vue'
import router from './router'
import './assets/styles/main.css'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)
const pinia = createPinia()

// Register Element Plus Icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
	app.component(key, component)
}

app.use(pinia)
app.use(router)
app.use(ElementPlus, { locale: vi })

// Initialize auth from localStorage
const authStore = useAuthStore()
authStore.initAuth()

app.mount('#app')


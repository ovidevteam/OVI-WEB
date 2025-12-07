import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'

export default defineConfig({
	plugins: [
		vue(),
		AutoImport({
			resolvers: [ElementPlusResolver()],
			imports: ['vue', 'vue-router', 'pinia'],
			dts: 'src/auto-imports.d.ts'
		}),
		Components({
			resolvers: [ElementPlusResolver()],
			dts: 'src/components.d.ts'
		})
	],
	resolve: {
		alias: {
			'@': resolve(__dirname, 'src')
		}
	},
	server: {
		port: 5173,
		proxy: {
			'/api': {
				target: 'http://localhost:8080',
				changeOrigin: true
			}
		}
	},
	build: {
		target: 'es2015',
		cssCodeSplit: true,
		sourcemap: false,
		minify: 'terser',
		terserOptions: {
			compress: {
				drop_console: true,
				drop_debugger: true
			}
		},
		rollupOptions: {
			output: {
				manualChunks: {
					'vue-vendor': ['vue', 'vue-router', 'pinia'],
					'element-plus': ['element-plus', '@element-plus/icons-vue'],
					'chart-vendor': ['chart.js', 'vue-chartjs'],
					'utils': ['axios', 'dayjs', 'crypto-js']
				},
				chunkFileNames: 'js/[name]-[hash].js',
				entryFileNames: 'js/[name]-[hash].js',
				assetFileNames: 'assets/[name]-[hash].[ext]'
			}
		},
		chunkSizeWarningLimit: 1000
	},
	optimizeDeps: {
		include: ['vue', 'vue-router', 'pinia', 'element-plus', 'axios', 'dayjs']
	}
})


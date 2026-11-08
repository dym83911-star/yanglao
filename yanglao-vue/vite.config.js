import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src'
    }
  },
  server: {
    port: 3000,
    open: true,
    proxy: {
      '/api': {
        target: 'http://localhost:8088',
        changeOrigin: true
      },
      // 后端本地上传目录映射出的静态图片
      '/uploads': {
        target: 'http://localhost:8088',
        changeOrigin: true
      }
    }
  }
})

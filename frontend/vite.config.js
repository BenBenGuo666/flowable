import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

const BACKEND_TARGET = 'http://localhost:8080';
// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],

  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },

  server: {
    port: 5173,
    host: '0.0.0.0',
    open: true,
    proxy: {
      // 代理所有 /api 开头的请求到后端
      '/api/': {
        target: BACKEND_TARGET,
        changeOrigin: true
      },
      // 代理初始化接口（不带 /api 前缀）
      '/init': {
        target: BACKEND_TARGET,
        changeOrigin: true
      },
      // 代理流程相关接口（不带 /api 前缀）
      '/process-definition': {
        target: BACKEND_TARGET,
        changeOrigin: true
      },
      '/process-instance': {
        target: BACKEND_TARGET,
        changeOrigin: true
      },
      '/process-template': {
        target: BACKEND_TARGET,
        changeOrigin: true
      },
      '/task': {
        target: BACKEND_TARGET,
        changeOrigin: true
      }
    }
  },

  build: {
    outDir: 'dist',
    sourcemap: false,
    rollupOptions: {
      output: {
        manualChunks: {
          'naive-ui': ['naive-ui'],
          'chart': ['chart.js'],
          'bpmn': ['bpmn-js']
        }
      }
    }
  }
})

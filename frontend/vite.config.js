import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

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
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 代理初始化接口
      '/init': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理认证接口
      '/auth': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理用户接口
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理角色接口
      '/role': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理权限接口
      '/permission': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      // 代理流程相关接口
      '/process-definition': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/process-instance': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/process-template': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/task': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/leave': {
        target: 'http://localhost:8080',
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

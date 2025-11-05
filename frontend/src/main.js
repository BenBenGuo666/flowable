import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { appleTheme } from './config/theme'
import './styles/global.css'

// Naive UI
import naive from 'naive-ui'

const app = createApp(App)

// 使用插件
app.use(router)
app.use(naive)

// 挂载应用
app.mount('#app')

// 设置 Naive UI 全局配置
app.provide('theme', appleTheme)

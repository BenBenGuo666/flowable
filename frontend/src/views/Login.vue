<template>
  <div class="login-container">
    <!-- 毛玻璃背景卡片 -->
    <n-card class="login-card glass-effect" :bordered="false">
      <!-- Logo 和标题 -->
      <div class="login-header">
        <div class="logo">
          <svg width="60" height="60" viewBox="0 0 60 60" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="60" height="60" rx="12" fill="url(#gradient)"/>
            <path d="M30 15L45 25V40L30 50L15 40V25L30 15Z" stroke="white" stroke-width="3" stroke-linecap="round" stroke-linejoin="round"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="60" y2="60">
                <stop offset="0%" stop-color="#007AFF"/>
                <stop offset="100%" stop-color="#5856D6"/>
              </linearGradient>
            </defs>
          </svg>
        </div>
        <h1 class="login-title">Flowable 工作流</h1>
        <p class="login-subtitle">欢迎回来，请登录您的账户</p>
      </div>

      <!-- 登录表单 -->
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        size="large"
        :show-label="false"
      >
        <n-form-item path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :input-props="{ autocomplete: 'username' }"
          >
            <template #prefix>
              <n-icon :component="PersonOutline" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item path="password">
          <n-input
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
            :input-props="{ autocomplete: 'current-password' }"
            @keyup.enter="handleLogin"
          >
            <template #prefix>
              <n-icon :component="LockClosedOutline" />
            </template>
          </n-input>
        </n-form-item>

        <!-- 记住我 -->
        <div class="login-options">
          <n-checkbox v-model:checked="formData.rememberMe">
            记住我
          </n-checkbox>
          <a href="#" class="forgot-password">忘记密码?</a>
        </div>

        <!-- 登录按钮 -->
        <n-button
          type="primary"
          size="large"
          block
          round
          :loading="loading"
          @click="handleLogin"
          class="login-button"
        >
          {{ loading ? '登录中...' : '登录' }}
        </n-button>
      </n-form>

      <!-- 底部链接 -->
      <div class="login-footer">
        <span>还没有账户？</span>
        <a href="#" class="register-link">立即注册</a>
      </div>
    </n-card>

    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { PersonOutline, LockClosedOutline } from '@vicons/ionicons5'
import { login } from '../api/auth'

// 路由和消息提示
const router = useRouter()
const message = useMessage()

// 表单引用
const formRef = ref(null)

// 加载状态
const loading = ref(false)

// 表单数据（开发环境默认填入测试账号）
const formData = reactive({
  username: 'admin',
  password: '123456',
  rememberMe: true
})

// 表单验证规则
const rules = {
  username: [
    {
      required: true,
      message: '请输入用户名',
      trigger: ['blur', 'input']
    },
    {
      min: 3,
      max: 20,
      message: '用户名长度应为 3-20 个字符',
      trigger: ['blur']
    }
  ],
  password: [
    {
      required: true,
      message: '请输入密码',
      trigger: ['blur', 'input']
    },
    {
      min: 6,
      message: '密码长度至少为 6 个字符',
      trigger: ['blur']
    }
  ]
}

/**
 * 处理登录
 */
const handleLogin = async () => {
  try {
    // 验证表单
    await formRef.value?.validate()

    loading.value = true

    // 调用登录 API
    const response = await login({
      username: formData.username,
      password: formData.password
    })
    alert(response)
    // 保存 token 到 localStorage
    if (response?.data?.token) {
      alert(response?.data?.token)
      localStorage.setItem('token', response.data.token)

      // 如果选择记住我，保存用户信息
      if (formData.rememberMe) {
        localStorage.setItem('userInfo', JSON.stringify({
          username: formData.username,
          userId: response.userId
        }))
      }
      // 显示成功消息
      message.success('登录成功！')

      // 跳转到 Dashboard
      setTimeout(() => {
        router.push('/dashboard')
      }, 500)
    } else {
      alert('登录失败，未返回有效令牌')
      message.error('登录失败，未返回有效令牌')
    }
  } catch (error) {
    // 处理验证错误
    if (error?.errors) {
      message.error('请检查输入信息')
      return
    }

    // 处理 API 错误
    console.error('登录错误:', error)
    const errorMessage = error.response?.data?.message || error.message || '登录失败，请稍后重试'
    message.error(errorMessage)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
  overflow: hidden;
  z-index: 9999;
}

.login-card {
  position: relative;
  width: 100%;
  max-width: 420px;
  padding: 40px;
  border-radius: 20px !important;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  z-index: 1;
}

/* Logo 和标题 */
.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  display: inline-block;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.login-title {
  font-size: 28px;
  font-weight: 700;
  color: #007AFF;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

/* 表单选项 */
.login-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.forgot-password {
  font-size: 14px;
  color: #007AFF;
  text-decoration: none;
  transition: opacity 0.2s;
}

.forgot-password:hover {
  opacity: 0.8;
}

/* 登录按钮 */
.login-button {
  margin-bottom: 24px;
  height: 44px;
  font-size: 16px;
  font-weight: 600;
}

/* 底部链接 */
.login-footer {
  text-align: center;
  font-size: 14px;
  color: rgba(0, 0, 0, 0.6);
}

.register-link {
  color: #007AFF;
  text-decoration: none;
  font-weight: 600;
  margin-left: 8px;
  transition: opacity 0.2s;
}

.register-link:hover {
  opacity: 0.8;
}

/* 背景装饰 */
.bg-decoration {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  overflow: hidden;
  z-index: 0;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float-circle 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -150px;
  right: -150px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -100px;
  left: -100px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  left: -75px;
  animation-delay: 10s;
}

@keyframes float-circle {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    padding: 30px 20px;
  }

  .login-title {
    font-size: 24px;
  }

  .logo svg {
    width: 50px;
    height: 50px;
  }
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .login-subtitle {
    color: rgba(255, 255, 255, 0.5);
  }

  .login-footer {
    color: rgba(255, 255, 255, 0.6);
  }
}
</style>

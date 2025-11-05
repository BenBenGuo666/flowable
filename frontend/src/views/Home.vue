<template>
  <div class="home-page">
    <div class="welcome-section apple-card">
      <h1>欢迎使用 Flowable 工作流系统</h1>
      <p>轻量化、易维护的工作流管理平台</p>
    </div>

    <n-grid :cols="3" :x-gap="20" :y-gap="20" class="quick-actions">
      <n-gi>
        <div class="action-card apple-card" @click="goToApply">
          <div class="icon">📝</div>
          <h3>发起请假</h3>
          <p>快速提交请假申请</p>
        </div>
      </n-gi>
      <n-gi>
        <div class="action-card apple-card" @click="goToMyLeave">
          <div class="icon">📋</div>
          <h3>我的请假</h3>
          <p>查看我的请假记录</p>
        </div>
      </n-gi>
      <n-gi>
        <div class="action-card apple-card" @click="goToPending">
          <div class="icon">✅</div>
          <h3>待我审批</h3>
          <p>处理待审批的请假</p>
        </div>
      </n-gi>
    </n-grid>

    <div class="features-section">
      <n-grid :cols="2" :x-gap="20" :y-gap="20">
        <n-gi>
          <div class="feature-card apple-card">
            <h3>🚀 高效便捷</h3>
            <p>简化流程审批，提升工作效率</p>
            <ul>
              <li>一键发起申请</li>
              <li>实时审批提醒</li>
              <li>移动端支持</li>
            </ul>
          </div>
        </n-gi>
        <n-gi>
          <div class="feature-card apple-card">
            <h3>📊 数据可视</h3>
            <p>直观的数据展示，清晰的流程追踪</p>
            <ul>
              <li>流程图可视化</li>
              <li>统计数据分析</li>
              <li>审批进度追踪</li>
            </ul>
          </div>
        </n-gi>
      </n-grid>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goToApply = () => {
  router.push('/leave/apply')
}

const goToMyLeave = () => {
  router.push('/leave/list')
}

const goToPending = () => {
  router.push('/leave/pending')
}
</script>

<style scoped>
.home-page {
  max-width: 1200px;
  margin: 0 auto;
}

.welcome-section {
  text-align: center;
  padding: 60px 40px;
  margin-bottom: 40px;
}

.welcome-section h1 {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 16px;
  background: linear-gradient(135deg, #007AFF 0%, #5856D6 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.welcome-section p {
  font-size: 18px;
  color: rgba(0, 0, 0, 0.6);
}

.quick-actions {
  margin-bottom: 40px;
}

.action-card {
  text-align: center;
  padding: 40px 20px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.action-card:hover {
  transform: translateY(-4px);
}

.action-card .icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.action-card h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 8px;
  color: #007AFF;
}

.action-card p {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

.features-section {
  margin-top: 40px;
}

.feature-card {
  padding: 30px;
}

.feature-card h3 {
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 12px;
}

.feature-card p {
  font-size: 16px;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 16px;
}

.feature-card ul {
  list-style: none;
  padding: 0;
}

.feature-card li {
  padding: 8px 0;
  color: rgba(0, 0, 0, 0.7);
  font-size: 14px;
}

.feature-card li:before {
  content: "• ";
  color: #007AFF;
  font-weight: bold;
  margin-right: 8px;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .welcome-section p {
    color: rgba(255, 255, 255, 0.6);
  }

  .action-card p {
    color: rgba(255, 255, 255, 0.5);
  }

  .feature-card p {
    color: rgba(255, 255, 255, 0.6);
  }

  .feature-card li {
    color: rgba(255, 255, 255, 0.7);
  }
}
</style>

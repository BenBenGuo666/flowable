<template>
  <div class="dashboard-page">
    <h1 class="page-title">工作台</h1>

    <!-- 统计卡片 -->
    <n-grid :cols="4" :x-gap="20" :y-gap="20" class="stats-grid">
      <n-gi>
        <div class="stat-card apple-card" style="border-left: 4px solid #007AFF;">
          <div class="stat-value">{{ stats.total }}</div>
          <div class="stat-label">总申请数</div>
        </div>
      </n-gi>
      <n-gi>
        <div class="stat-card apple-card" style="border-left: 4px solid #34C759;">
          <div class="stat-value">{{ stats.approved }}</div>
          <div class="stat-label">已批准</div>
        </div>
      </n-gi>
      <n-gi>
        <div class="stat-card apple-card" style="border-left: 4px solid #FF9500;">
          <div class="stat-value">{{ stats.pending }}</div>
          <div class="stat-label">待审批</div>
        </div>
      </n-gi>
      <n-gi>
        <div class="stat-card apple-card" style="border-left: 4px solid #FF3B30;">
          <div class="stat-value">{{ stats.rejected }}</div>
          <div class="stat-label">已拒绝</div>
        </div>
      </n-gi>
    </n-grid>

    <!-- 图表区域 -->
    <n-grid :cols="2" :x-gap="20" :y-gap="20" class="charts-grid">
      <n-gi>
        <div class="chart-card apple-card">
          <h3>月度请假趋势</h3>
          <canvas ref="lineChartRef"></canvas>
        </div>
      </n-gi>
      <n-gi>
        <div class="chart-card apple-card">
          <h3>请假类型分布</h3>
          <canvas ref="pieChartRef"></canvas>
        </div>
      </n-gi>
    </n-grid>

    <!-- 最近活动 -->
    <div class="recent-activity apple-card">
      <h3>最近活动</h3>
      <n-timeline>
        <n-timeline-item
          v-for="item in recentActivities"
          :key="item.id"
          :type="item.type"
          :title="item.title"
          :content="item.content"
          :time="item.time"
        />
      </n-timeline>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Chart, registerables } from 'chart.js'
import { getLeaveStatistics } from '../api/leave'

Chart.register(...registerables)

// 统计数据
const stats = ref({
  total: 0,
  approved: 0,
  pending: 0,
  rejected: 0
})

// 图表引用
const lineChartRef = ref(null)
const pieChartRef = ref(null)

// 最近活动
const recentActivities = ref([
  {
    id: 1,
    type: 'success',
    title: '请假申请已批准',
    content: '张三的病假申请已通过',
    time: '2小时前'
  },
  {
    id: 2,
    type: 'info',
    title: '新的请假申请',
    content: '李四提交了事假申请',
    time: '4小时前'
  },
  {
    id: 3,
    type: 'warning',
    title: '待审批提醒',
    content: '您有3个请假申请待审批',
    time: '1天前'
  }
])

// 初始化折线图
const initLineChart = () => {
  const ctx = lineChartRef.value.getContext('2d')
  new Chart(ctx, {
    type: 'line',
    data: {
      labels: ['1月', '2月', '3月', '4月', '5月', '6月'],
      datasets: [{
        label: '请假申请数',
        data: [12, 19, 15, 25, 22, 30],
        borderColor: '#007AFF',
        backgroundColor: 'rgba(0, 122, 255, 0.1)',
        tension: 0.4,
        fill: true
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          display: false
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: {
            color: 'rgba(0, 0, 0, 0.05)'
          }
        },
        x: {
          grid: {
            display: false
          }
        }
      }
    }
  })
}

// 初始化饼图
const initPieChart = () => {
  const ctx = pieChartRef.value.getContext('2d')
  new Chart(ctx, {
    type: 'doughnut',
    data: {
      labels: ['事假', '病假', '年假', '调休'],
      datasets: [{
        data: [30, 25, 35, 10],
        backgroundColor: [
          '#007AFF',
          '#34C759',
          '#FF9500',
          '#5856D6'
        ],
        borderWidth: 0
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: true,
      plugins: {
        legend: {
          position: 'bottom'
        }
      }
    }
  })
}

// 加载统计数据
const loadStats = async () => {
  try {
    const data = await getLeaveStatistics()
    stats.value = data
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 使用模拟数据
    stats.value = {
      total: 123,
      approved: 95,
      pending: 18,
      rejected: 10
    }
  }
}

onMounted(() => {
  loadStats()
  initLineChart()
  initPieChart()
})
</script>

<style scoped>
.dashboard-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  margin-bottom: 30px;
  color: #007AFF;
}

.stats-grid {
  margin-bottom: 30px;
}

.stat-card {
  padding: 24px;
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #007AFF;
}

.stat-label {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

.charts-grid {
  margin-bottom: 30px;
}

.chart-card {
  padding: 24px;
}

.chart-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: rgba(0, 0, 0, 0.9);
}

.recent-activity {
  padding: 24px;
}

.recent-activity h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: rgba(0, 0, 0, 0.9);
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .stat-label {
    color: rgba(255, 255, 255, 0.5);
  }

  .chart-card h3,
  .recent-activity h3 {
    color: rgba(255, 255, 255, 0.9);
  }
}
</style>

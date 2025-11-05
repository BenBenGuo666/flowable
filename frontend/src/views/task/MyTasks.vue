<template>
  <div class="my-tasks-page">
    <h1 class="page-title">任务中心</h1>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <n-space :size="20">
        <div class="stat-card apple-card" @click="currentTab = 'pending'" :class="{ active: currentTab === 'pending' }">
          <div class="stat-icon">📋</div>
          <div class="stat-value">{{ pendingTasks.length }}</div>
          <div class="stat-label">待办任务</div>
        </div>
        <div class="stat-card apple-card" @click="currentTab = 'completed'" :class="{ active: currentTab === 'completed' }">
          <div class="stat-icon">✅</div>
          <div class="stat-value">{{ completedTasks.length }}</div>
          <div class="stat-label">已办任务</div>
        </div>
        <div class="stat-card apple-card" @click="currentTab = 'candidate'" :class="{ active: currentTab === 'candidate' }">
          <div class="stat-icon">👥</div>
          <div class="stat-value">{{ candidateTasks.length }}</div>
          <div class="stat-label">候选任务</div>
        </div>
      </n-space>
    </div>

    <!-- 标签页 -->
    <div class="content-card apple-card">
      <n-tabs v-model:value="currentTab" type="line" @update:value="handleTabChange">
        <n-tab-pane name="pending" tab="待办任务">
          <n-data-table
            :columns="pendingColumns"
            :data="pendingTasks"
            :loading="loading"
            :bordered="false"
          />
        </n-tab-pane>

        <n-tab-pane name="completed" tab="已办任务">
          <n-data-table
            :columns="completedColumns"
            :data="completedTasks"
            :loading="loading"
            :bordered="false"
          />
        </n-tab-pane>

        <n-tab-pane name="candidate" tab="候选任务">
          <n-data-table
            :columns="candidateColumns"
            :data="candidateTasks"
            :loading="loading"
            :bordered="false"
          />
        </n-tab-pane>
      </n-tabs>
    </div>

    <!-- 任务处理对话框 -->
    <n-modal
      v-model:show="showTaskModal"
      preset="card"
      title="处理任务"
      style="width: 600px"
    >
      <div class="task-modal-content" v-if="currentTask">
        <n-descriptions :column="1" bordered>
          <n-descriptions-item label="任务名称">
            {{ currentTask.name }}
          </n-descriptions-item>
          <n-descriptions-item label="流程名称">
            {{ currentTask.processDefinitionName }}
          </n-descriptions-item>
          <n-descriptions-item label="创建时间">
            {{ currentTask.createTime }}
          </n-descriptions-item>
          <n-descriptions-item label="任务描述">
            {{ currentTask.description || '无' }}
          </n-descriptions-item>
        </n-descriptions>

        <n-form style="margin-top: 20px">
          <n-form-item label="处理意见">
            <n-input
              v-model:value="approvalComment"
              type="textarea"
              placeholder="请输入处理意见"
              :rows="4"
            />
          </n-form-item>
        </n-form>

        <n-space justify="end" style="margin-top: 20px">
          <n-button @click="showTaskModal = false">取消</n-button>
          <n-button type="error" @click="handleReject">驳回</n-button>
          <n-button type="success" @click="handleApprove">通过</n-button>
        </n-space>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NButton, NSpace, NTag } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { getMyPendingTasks, getMyCompletedTasks, getCandidateTasks, completeTask, rejectTask, claimTask } from '../../api/task'

const message = useMessage()
const loading = ref(false)
const currentTab = ref('pending')
const pendingTasks = ref([])
const completedTasks = ref([])
const candidateTasks = ref([])

const showTaskModal = ref(false)
const currentTask = ref(null)
const approvalComment = ref('')

// 待办任务列
const pendingColumns = [
  {
    title: '任务名称',
    key: 'name',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '流程名称',
    key: 'processDefinitionName',
    width: 180,
    ellipsis: { tooltip: true }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '优先级',
    key: 'priority',
    width: 100,
    render(row) {
      const priorityMap = {
        high: { text: '高', type: 'error' },
        normal: { text: '中', type: 'info' },
        low: { text: '低', type: 'default' }
      }
      const priority = row.priority > 50 ? 'high' : row.priority > 25 ? 'normal' : 'low'
      const config = priorityMap[priority]
      return h(NTag, { type: config.type, size: 'small' }, { default: () => config.text })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: 'primary',
              onClick: () => openTaskModal(row)
            },
            { default: () => '处理' }
          ),
          h(
            NButton,
            {
              size: 'small',
              onClick: () => viewDetail(row)
            },
            { default: () => '详情' }
          )
        ]
      })
    }
  }
]

// 已办任务列
const completedColumns = [
  {
    title: '任务名称',
    key: 'name',
    width: 200
  },
  {
    title: '流程名称',
    key: 'processDefinitionName',
    width: 180
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          onClick: () => viewDetail(row)
        },
        { default: () => '查看' }
      )
    }
  }
]

// 候选任务列
const candidateColumns = [
  {
    title: '任务名称',
    key: 'name',
    width: 200
  },
  {
    title: '流程名称',
    key: 'processDefinitionName',
    width: 180
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render(row) {
      return h(
        NButton,
        {
          size: 'small',
          type: 'primary',
          onClick: () => handleClaim(row)
        },
        { default: () => '认领' }
      )
    }
  }
]

// 标签页切换
const handleTabChange = (value) => {
  currentTab.value = value
  loadData(value)
}

// 打开任务处理对话框
const openTaskModal = (task) => {
  currentTask.value = task
  approvalComment.value = ''
  showTaskModal.value = true
}

// 查看详情
const viewDetail = (task) => {
  message.info('查看详情功能待实现')
}

// 通过
const handleApprove = async () => {
  try {
    await completeTask(currentTask.value.id, {
      comment: approvalComment.value
    })
    message.success('任务已通过')
    showTaskModal.value = false
    loadData('pending')
  } catch (error) {
    message.error('操作失败')
  }
}

// 驳回
const handleReject = async () => {
  try {
    await rejectTask(currentTask.value.id, {
      comment: approvalComment.value
    })
    message.success('任务已驳回')
    showTaskModal.value = false
    loadData('pending')
  } catch (error) {
    message.error('操作失败')
  }
}

// 认领任务
const handleClaim = async (task) => {
  try {
    await claimTask(task.id, 'user1')
    message.success('任务已认领')
    loadData('candidate')
    loadData('pending')
  } catch (error) {
    message.error('认领失败')
  }
}

// 加载数据
const loadData = async (type = 'pending') => {
  loading.value = true
  try {
    if (type === 'pending' || !type) {
      const data = await getMyPendingTasks()
      pendingTasks.value = data || []
    }
    if (type === 'completed') {
      const data = await getMyCompletedTasks()
      completedTasks.value = data || []
    }
    if (type === 'candidate') {
      const data = await getCandidateTasks()
      candidateTasks.value = data || []
    }
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData('pending')
  loadData('completed')
  loadData('candidate')
})
</script>

<style scoped>
.my-tasks-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 30px;
  color: #007AFF;
}

.stats-row {
  margin-bottom: 30px;
}

.stat-card {
  padding: 24px 32px;
  text-align: center;
  min-width: 160px;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 2px solid transparent;
}

.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.07), 0 2px 4px rgba(0, 0, 0, 0.05);
}

.stat-card.active {
  border-color: #007AFF;
}

.stat-icon {
  font-size: 36px;
  margin-bottom: 12px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #007AFF;
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

.content-card {
  padding: 24px;
}

.task-modal-content {
  padding: 20px 0;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .stat-label {
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>

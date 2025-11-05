<template>
  <div class="process-instances-page">
    <h1 class="page-title">流程实例监控</h1>

    <div class="stats-row">
      <n-space :size="20">
        <div class="stat-card apple-card">
          <div class="stat-label">运行中</div>
          <div class="stat-value">{{ runningCount }}</div>
        </div>
        <div class="stat-card apple-card">
          <div class="stat-label">已结束</div>
          <div class="stat-value">{{ endedCount }}</div>
        </div>
        <div class="stat-card apple-card">
          <div class="stat-label">已挂起</div>
          <div class="stat-value">{{ suspendedCount }}</div>
        </div>
      </n-space>
    </div>

    <div class="content-card apple-card">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :bordered="false"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, h, onMounted, computed } from 'vue'
import { NTag, NButton, NSpace } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { getAllProcessInstances, suspendProcessInstance, activateProcessInstance, deleteProcessInstance } from '../../api/process'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])

// 统计数据
const runningCount = computed(() => tableData.value.filter(item => !item.ended && !item.suspended).length)
const endedCount = computed(() => tableData.value.filter(item => item.ended).length)
const suspendedCount = computed(() => tableData.value.filter(item => item.suspended).length)

// 表格列
const columns = [
  {
    title: '流程名称',
    key: 'processDefinitionName',
    width: 200
  },
  {
    title: '业务Key',
    key: 'businessKey',
    width: 150
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 180
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      if (row.ended) {
        return h(NTag, { type: 'default' }, { default: () => '已结束' })
      } else if (row.suspended) {
        return h(NTag, { type: 'warning' }, { default: () => '已挂起' })
      } else {
        return h(NTag, { type: 'success' }, { default: () => '运行中' })
      }
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 200,
    fixed: 'right',
    render(row) {
      if (row.ended) return '无可用操作'

      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              type: row.suspended ? 'success' : 'warning',
              onClick: () => toggleStatus(row)
            },
            { default: () => (row.suspended ? '激活' : '挂起') }
          ),
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              onClick: () => handleDelete(row.id)
            },
            { default: () => '删除' }
          )
        ]
      })
    }
  }
]

// 切换状态
const toggleStatus = async (row) => {
  try {
    if (row.suspended) {
      await activateProcessInstance(row.id)
      message.success('实例已激活')
    } else {
      await suspendProcessInstance(row.id)
      message.success('实例已挂起')
    }
    loadData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 删除实例
const handleDelete = async (id) => {
  try {
    await deleteProcessInstance(id, '手动删除')
    message.success('删除成功')
    loadData()
  } catch (error) {
    message.error('删除失败')
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const data = await getAllProcessInstances()
    tableData.value = data || []
  } catch (error) {
    console.error('加载失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.process-instances-page {
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
  padding: 20px 30px;
  text-align: center;
  min-width: 150px;
}

.stat-label {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
  margin-bottom: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #007AFF;
}

.content-card {
  padding: 24px;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .stat-label {
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>

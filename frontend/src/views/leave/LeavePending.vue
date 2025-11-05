<template>
  <div class="pending-list-page">
    <div class="page-header">
      <h1>待我审批</h1>
      <n-tag :bordered="false" type="warning" size="large">
        {{ tableData.length }} 个待审批
      </n-tag>
    </div>

    <div class="list-container apple-card">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :bordered="false"
      />
    </div>

    <!-- 审批对话框 -->
    <n-modal
      v-model:show="showApproveModal"
      preset="dialog"
      title="审批请假申请"
      positive-text="批准"
      negative-text="拒绝"
      @positive-click="handleApprove"
      @negative-click="handleReject"
    >
      <div class="approve-modal-content">
        <n-form>
          <n-form-item label="审批意见">
            <n-input
              v-model:value="approveComment"
              type="textarea"
              placeholder="请输入审批意见（可选）"
              :rows="4"
            />
          </n-form-item>
        </n-form>
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { NTag, NButton, NSpace } from 'naive-ui'
import { useMessage } from 'naive-ui'
import { getPendingLeaveList, approveLeave, rejectLeave } from '../../api/leave'

const message = useMessage()
const loading = ref(false)
const tableData = ref([])
const showApproveModal = ref(false)
const currentTask = ref(null)
const approveComment = ref('')

// 分页配置
const pagination = ref({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 30],
  onChange: (page) => {
    pagination.value.page = page
    loadData()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.value.pageSize = pageSize
    pagination.value.page = 1
    loadData()
  }
})

// 表格列配置
const columns = [
  {
    title: '申请人',
    key: 'applicant',
    width: 120
  },
  {
    title: '申请时间',
    key: 'applyTime',
    width: 180
  },
  {
    title: '请假类型',
    key: 'type',
    width: 100,
    render(row) {
      const typeMap = {
        personal: '事假',
        sick: '病假',
        annual: '年假',
        compensatory: '调休'
      }
      return typeMap[row.type] || row.type
    }
  },
  {
    title: '开始时间',
    key: 'startTime',
    width: 180
  },
  {
    title: '结束时间',
    key: 'endTime',
    width: 180
  },
  {
    title: '天数',
    key: 'days',
    width: 80
  },
  {
    title: '请假原因',
    key: 'reason',
    ellipsis: {
      tooltip: true
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
              type: 'success',
              onClick: () => openApproveModal(row, 'approve')
            },
            { default: () => '批准' }
          ),
          h(
            NButton,
            {
              size: 'small',
              type: 'error',
              onClick: () => openApproveModal(row, 'reject')
            },
            { default: () => '拒绝' }
          )
        ]
      })
    }
  }
]

// 打开审批对话框
const openApproveModal = (task, action) => {
  currentTask.value = { ...task, action }
  approveComment.value = ''
  showApproveModal.value = true
}

// 批准
const handleApprove = async () => {
  if (!currentTask.value) return false

  try {
    await approveLeave(currentTask.value.taskId, {
      comment: approveComment.value
    })
    message.success('已批准该请假申请')
    showApproveModal.value = false
    loadData()
    return true
  } catch (error) {
    message.error('操作失败: ' + (error.message || '未知错误'))
    return false
  }
}

// 拒绝
const handleReject = async () => {
  if (!currentTask.value) return false

  try {
    await rejectLeave(currentTask.value.taskId, {
      comment: approveComment.value
    })
    message.success('已拒绝该请假申请')
    showApproveModal.value = false
    loadData()
    return true
  } catch (error) {
    message.error('操作失败: ' + (error.message || '未知错误'))
    return false
  }
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    const data = await getPendingLeaveList(params)
    tableData.value = data.list || []
    pagination.value.itemCount = data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    // 使用模拟数据
    tableData.value = [
      {
        id: '1',
        taskId: 'task-001',
        applicant: '张三',
        applyTime: '2024-11-04 10:00:00',
        type: 'sick',
        startTime: '2024-11-06 09:00:00',
        endTime: '2024-11-07 18:00:00',
        days: 2,
        reason: '感冒发烧，需要休息'
      },
      {
        id: '2',
        taskId: 'task-002',
        applicant: '李四',
        applyTime: '2024-11-04 14:30:00',
        type: 'personal',
        startTime: '2024-11-08 09:00:00',
        endTime: '2024-11-08 18:00:00',
        days: 1,
        reason: '家中有事需要处理'
      }
    ]
    pagination.value.itemCount = 2
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.pending-list-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #007AFF;
}

.list-container {
  padding: 24px;
}

.approve-modal-content {
  padding: 20px 0;
}
</style>

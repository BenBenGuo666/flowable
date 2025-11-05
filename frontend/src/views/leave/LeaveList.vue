<template>
  <div class="leave-list-page">
    <div class="page-header">
      <h1>我的请假</h1>
      <n-button type="primary" @click="goToApply">
        发起新申请
      </n-button>
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
  </div>
</template>

<script setup>
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NTag, NButton, NSpace } from 'naive-ui'
import { getMyLeaveList } from '../../api/leave'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])

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
    title: '状态',
    key: 'status',
    width: 100,
    render(row) {
      const statusMap = {
        pending: { text: '待审批', type: 'warning' },
        approved: { text: '已批准', type: 'success' },
        rejected: { text: '已拒绝', type: 'error' }
      }
      const status = statusMap[row.status] || { text: row.status, type: 'default' }
      return h(NTag, { type: status.type }, { default: () => status.text })
    }
  },
  {
    title: '操作',
    key: 'actions',
    width: 150,
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              onClick: () => viewDetail(row.id)
            },
            { default: () => '查看详情' }
          )
        ]
      })
    }
  }
]

// 查看详情
const viewDetail = (id) => {
  router.push(`/leave/detail/${id}`)
}

// 跳转到申请页面
const goToApply = () => {
  router.push('/leave/apply')
}

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    const data = await getMyLeaveList(params)
    tableData.value = data.list || []
    pagination.value.itemCount = data.total || 0
  } catch (error) {
    console.error('加载数据失败:', error)
    // 使用模拟数据
    tableData.value = [
      {
        id: '1',
        applyTime: '2024-11-01 10:00:00',
        type: 'sick',
        startTime: '2024-11-05 09:00:00',
        endTime: '2024-11-06 18:00:00',
        days: 2,
        status: 'approved',
        reason: '感冒发烧'
      },
      {
        id: '2',
        applyTime: '2024-11-03 14:30:00',
        type: 'personal',
        startTime: '2024-11-10 09:00:00',
        endTime: '2024-11-10 18:00:00',
        days: 1,
        status: 'pending',
        reason: '家中有事'
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
.leave-list-page {
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
</style>

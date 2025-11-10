<template>
  <div class="process-definitions-page">
    <div class="page-header">
      <h1>流程定义管理</h1>
      <n-button type="primary" @click="goToDesigner">
        创建新流程
      </n-button>
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
import { ref, h, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { NTag, NButton, NSpace, NPopconfirm } from 'naive-ui'
import { useMessage } from 'naive-ui'
import {
  getProcessDefinitions,
  deleteProcessDefinition,
  activateProcessDefinition,
  suspendProcessDefinition
} from '../../api/process'

const router = useRouter()
const message = useMessage()
const loading = ref(false)
const tableData = ref([])

// 表格列配置
const columns = [
  {
    title: '流程名称',
    key: 'name',
    width: 200
  },
  {
    title: '流程Key',
    key: 'key',
    width: 150
  },
  {
    title: '版本',
    key: 'version',
    width: 80
  },
  {
    title: '分类',
    key: 'category',
    width: 120
  },
  {
    title: '状态',
    key: 'suspended',
    width: 100,
    render(row) {
      return h(
        NTag,
        { type: row.suspended ? 'warning' : 'success' },
        { default: () => (row.suspended ? '已挂起' : '激活') }
      )
    }
  },
  {
    title: '部署时间',
    key: 'deploymentTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 300,
    fixed: 'right',
    render(row) {
      return h(NSpace, null, {
        default: () => [
          h(
            NButton,
            {
              size: 'small',
              onClick: () => editProcess(row.id)
            },
            { default: () => '编辑' }
          ),
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
            NPopconfirm,
            {
              onPositiveClick: () => handleDelete(row.deploymentId)
            },
            {
              default: () => '确定要删除此流程定义吗？',
              trigger: () =>
                h(
                  NButton,
                  {
                    size: 'small',
                    type: 'error'
                  },
                  { default: () => '删除' }
                )
            }
          )
        ]
      })
    }
  }
]

// 跳转到设计器
const goToDesigner = () => {
  router.push('/process/designer')
}

// 编辑流程
const editProcess = (id) => {
  router.push(`/process/designer?id=${id}`)
}

// 切换状态
const toggleStatus = async (row) => {
  try {
    if (row.suspended) {
      await activateProcessDefinition(row.id)
      message.success('流程已激活')
    } else {
      await suspendProcessDefinition(row.id)
      message.success('流程已挂起')
    }
    loadData()
  } catch (error) {
    message.error('操作失败')
  }
}

// 删除流程
const handleDelete = async (deploymentId) => {
  try {
    await deleteProcessDefinition(deploymentId, true)
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
    const data = await getProcessDefinitions()
    alert(data)
    tableData.value = data || []
    alert(tableData)
  } catch (error) {
    console.error('加载失败:', error)
    message.error('加载数据失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.process-definitions-page {
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

.content-card {
  padding: 24px;
}
</style>

<template>
  <div class="user-management-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
      <p class="page-description">管理系统用户和角色分配</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar apple-card">
      <div class="search-box">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索用户名、邮箱或手机号..."
          clearable
          @input="handleSearch"
        >
          <template #prefix>
            <n-icon :component="SearchOutline" />
          </template>
        </n-input>
      </div>
      <n-button type="primary" @click="handleCreate">
        <template #icon>
          <n-icon :component="AddOutline" />
        </template>
        新建用户
      </n-button>
    </div>

    <!-- 用户列表 -->
    <div class="table-container apple-card">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        :scroll-x="1200"
        striped
      />
    </div>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      :mask-closable="false"
      preset="card"
      :title="modalTitle"
      class="user-modal"
      :style="{ width: '600px' }"
    >
      <n-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-placement="left"
        label-width="100"
        require-mark-placement="left"
      >
        <n-form-item label="用户名" path="username">
          <n-input
            v-model:value="formData.username"
            placeholder="请输入用户名"
            :disabled="isEdit"
          />
        </n-form-item>

        <n-form-item label="密码" path="password" v-if="!isEdit">
          <n-input
            v-model:value="formData.password"
            type="password"
            placeholder="请输入密码"
            show-password-on="click"
          />
        </n-form-item>

        <n-form-item label="邮箱" path="email">
          <n-input
            v-model:value="formData.email"
            placeholder="请输入邮箱"
          />
        </n-form-item>

        <n-form-item label="手机号" path="phone">
          <n-input
            v-model:value="formData.phone"
            placeholder="请输入手机号"
          />
        </n-form-item>

        <n-form-item label="真实姓名" path="realName">
          <n-input
            v-model:value="formData.realName"
            placeholder="请输入真实姓名"
          />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-select
            v-model:value="formData.status"
            :options="statusOptions"
            placeholder="请选择状态"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <div class="modal-footer">
          <n-button @click="showModal = false">取消</n-button>
          <n-button type="primary" :loading="submitting" @click="handleSubmit">
            确定
          </n-button>
        </div>
      </template>
    </n-modal>

    <!-- 分配角色弹窗 -->
    <n-modal
      v-model:show="showRoleModal"
      preset="card"
      title="分配角色"
      class="role-modal"
      :style="{ width: '500px' }"
    >
      <n-checkbox-group v-model:value="selectedRoles">
        <n-space vertical>
          <n-checkbox
            v-for="role in roleList"
            :key="role.id"
            :value="role.id"
            :label="role.name"
          >
            <div class="role-item">
              <span class="role-name">{{ role.name }}</span>
              <span class="role-desc">{{ role.description }}</span>
            </div>
          </n-checkbox>
        </n-space>
      </n-checkbox-group>

      <template #footer>
        <div class="modal-footer">
          <n-button @click="showRoleModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="assigningRole"
            @click="handleAssignRoles"
          >
            确定
          </n-button>
        </div>
      </template>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted } from 'vue'
import { NButton, NTag, NSpace, NIcon, useMessage, useDialog } from 'naive-ui'
import { SearchOutline, AddOutline, CreateOutline, TrashOutline, PeopleOutline } from '@vicons/ionicons5'
import {
  getUserList,
  createUser,
  updateUser,
  deleteUser,
  assignUserRoles
} from '../../api/user'
import { getRoleList } from '../../api/role'

// 消息提示和对话框
const message = useMessage()
const dialog = useDialog()

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 搜索关键词
const searchKeyword = ref('')

// 分页配置
const pagination = reactive({
  page: 1,
  pageSize: 10,
  showSizePicker: true,
  pageSizes: [10, 20, 50],
  itemCount: 0,
  prefix: ({ itemCount }) => `共 ${itemCount} 条`,
  onChange: (page) => {
    pagination.page = page
    loadData()
  },
  onUpdatePageSize: (pageSize) => {
    pagination.pageSize = pageSize
    pagination.page = 1
    loadData()
  }
})

// 表格列配置
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80,
    fixed: 'left'
  },
  {
    title: '用户名',
    key: 'username',
    width: 120,
    ellipsis: { tooltip: true }
  },
  {
    title: '真实姓名',
    key: 'realName',
    width: 120,
    ellipsis: { tooltip: true }
  },
  {
    title: '邮箱',
    key: 'email',
    width: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '手机号',
    key: 'phone',
    width: 140
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      const statusMap = {
        1: { type: 'success', text: '正常' },
        0: { type: 'error', text: '禁用' }
      }
      const status = statusMap[row.status] || { type: 'default', text: '未知' }
      return h(NTag, { type: status.type }, { default: () => status.text })
    }
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 240,
    fixed: 'right',
    render: (row) => {
      return h(
        NSpace,
        {},
        {
          default: () => [
            h(
              NButton,
              {
                size: 'small',
                type: 'primary',
                text: true,
                onClick: () => handleEdit(row)
              },
              { default: () => '编辑', icon: () => h(NIcon, { component: CreateOutline }) }
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'info',
                text: true,
                onClick: () => handleAssignRole(row)
              },
              { default: () => '分配角色', icon: () => h(NIcon, { component: PeopleOutline }) }
            ),
            h(
              NButton,
              {
                size: 'small',
                type: 'error',
                text: true,
                onClick: () => handleDelete(row)
              },
              { default: () => '删除', icon: () => h(NIcon, { component: TrashOutline }) }
            )
          ]
        }
      )
    }
  }
]

// 模态框相关
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新建用户')
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  password: '',
  email: '',
  phone: '',
  realName: '',
  status: 1
})

// 表单验证规则
const formRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度为 3-20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为 6 个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入有效的手机号', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  status: [
    { required: true, type: 'number', message: '请选择状态', trigger: 'change' }
  ]
}

// 状态选项
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

// 角色相关
const showRoleModal = ref(false)
const roleList = ref([])
const selectedRoles = ref([])
const currentUserId = ref(null)
const assigningRole = ref(false)

/**
 * 加载用户列表
 */
const loadData = async () => {
  try {
    loading.value = true
    const response = await getUserList({
      page: pagination.page,
      pageSize: pagination.pageSize,
      keyword: searchKeyword.value
    })

    // 适配不同的响应格式
    if (response.list) {
      tableData.value = response.list
      pagination.itemCount = response.total || 0
    } else if (Array.isArray(response)) {
      tableData.value = response
      pagination.itemCount = response.length
    } else {
      tableData.value = []
      pagination.itemCount = 0
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    message.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索处理
 */
const handleSearch = () => {
  pagination.page = 1
  loadData()
}

/**
 * 新建用户
 */
const handleCreate = () => {
  isEdit.value = false
  modalTitle.value = '新建用户'
  resetForm()
  showModal.value = true
}

/**
 * 编辑用户
 */
const handleEdit = (row) => {
  isEdit.value = true
  modalTitle.value = '编辑用户'
  Object.assign(formData, {
    id: row.id,
    username: row.username,
    email: row.email,
    phone: row.phone,
    realName: row.realName,
    status: row.status
  })
  showModal.value = true
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  try {
    // 验证表单
    await formRef.value?.validate()

    submitting.value = true

    if (isEdit.value) {
      // 更新用户
      await updateUser(formData.id, {
        email: formData.email,
        phone: formData.phone,
        realName: formData.realName,
        status: formData.status
      })
      message.success('更新用户成功')
    } else {
      // 创建用户
      await createUser(formData)
      message.success('创建用户成功')
    }

    showModal.value = false
    loadData()
  } catch (error) {
    if (error?.errors) {
      return
    }
    console.error('提交失败:', error)
    message.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

/**
 * 删除用户
 */
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除用户"${row.username}"吗？此操作不可撤销。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteUser(row.id)
        message.success('删除成功')
        loadData()
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

/**
 * 分配角色
 */
const handleAssignRole = async (row) => {
  currentUserId.value = row.id
  selectedRoles.value = row.roleIds || []
  showRoleModal.value = true

  // 加载角色列表
  try {
    const response = await getRoleList()
    roleList.value = Array.isArray(response) ? response : response.list || []
  } catch (error) {
    console.error('加载角色列表失败:', error)
    message.error('加载角色列表失败')
  }
}

/**
 * 确认分配角色
 */
const handleAssignRoles = async () => {
  try {
    assigningRole.value = true
    await assignUserRoles(currentUserId.value, selectedRoles.value)
    message.success('分配角色成功')
    showRoleModal.value = false
    loadData()
  } catch (error) {
    console.error('分配角色失败:', error)
    message.error('分配角色失败')
  } finally {
    assigningRole.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    username: '',
    password: '',
    email: '',
    phone: '',
    realName: '',
    status: 1
  })
  formRef.value?.restoreValidation()
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.user-management-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 32px;
  font-weight: 700;
  color: #007AFF;
  margin-bottom: 8px;
}

.page-description {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
  margin-bottom: 20px;
  gap: 16px;
}

.search-box {
  flex: 1;
  max-width: 400px;
}

.table-container {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.role-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.role-name {
  font-weight: 600;
  color: rgba(0, 0, 0, 0.9);
}

.role-desc {
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .action-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    max-width: 100%;
  }

  .page-title {
    font-size: 24px;
  }
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .page-description {
    color: rgba(255, 255, 255, 0.5);
  }

  .role-name {
    color: rgba(255, 255, 255, 0.9);
  }

  .role-desc {
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>

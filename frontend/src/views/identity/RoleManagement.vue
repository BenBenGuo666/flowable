<template>
  <div class="role-management-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">角色管理</h1>
      <p class="page-description">管理系统角色和权限分配</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar apple-card">
      <div class="search-box">
        <n-input
          v-model:value="searchKeyword"
          placeholder="搜索角色名称或描述..."
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
        新建角色
      </n-button>
    </div>

    <!-- 角色列表 -->
    <div class="table-container apple-card">
      <n-data-table
        :columns="columns"
        :data="tableData"
        :loading="loading"
        :pagination="pagination"
        :row-key="row => row.id"
        :scroll-x="1000"
        striped
      />
    </div>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      :mask-closable="false"
      preset="card"
      :title="modalTitle"
      class="role-modal"
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
        <n-form-item label="角色名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入角色名称"
          />
        </n-form-item>

        <n-form-item label="角色代码" path="code">
          <n-input
            v-model:value="formData.code"
            placeholder="请输入角色代码（如：ADMIN）"
            :disabled="isEdit"
          />
        </n-form-item>

        <n-form-item label="角色描述" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="请输入角色描述"
            :autosize="{ minRows: 3, maxRows: 6 }"
          />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-select
            v-model:value="formData.status"
            :options="statusOptions"
            placeholder="请选择状态"
          />
        </n-form-item>

        <n-form-item label="排序" path="sort">
          <n-input-number
            v-model:value="formData.sort"
            placeholder="请输入排序值"
            :min="0"
            style="width: 100%"
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

    <!-- 分配权限弹窗 -->
    <n-modal
      v-model:show="showPermissionModal"
      preset="card"
      title="分配权限"
      class="permission-modal"
      :style="{ width: '600px' }"
    >
      <n-spin :show="loadingPermissions">
        <div class="permission-tree-container">
          <n-tree
            ref="treeRef"
            :data="permissionTree"
            :checked-keys="checkedPermissions"
            checkable
            cascade
            selectable
            expand-on-click
            :default-expanded-keys="expandedKeys"
            key-field="id"
            label-field="name"
            children-field="children"
            @update:checked-keys="handlePermissionCheck"
          />
        </div>
      </n-spin>

      <template #footer>
        <div class="modal-footer">
          <n-button @click="showPermissionModal = false">取消</n-button>
          <n-button
            type="primary"
            :loading="assigningPermission"
            @click="handleAssignPermissions"
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
import { SearchOutline, AddOutline, CreateOutline, TrashOutline, LockClosedOutline } from '@vicons/ionicons5'
import {
  getRoleList,
  createRole,
  updateRole,
  deleteRole,
  assignRolePermissions
} from '../../api/role'
import { getPermissionTree } from '../../api/permission'

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
    title: '角色名称',
    key: 'name',
    width: 150,
    ellipsis: { tooltip: true }
  },
  {
    title: '角色代码',
    key: 'code',
    width: 150,
    render: (row) => {
      return h(NTag, { type: 'info', bordered: false }, { default: () => row.code })
    }
  },
  {
    title: '描述',
    key: 'description',
    minWidth: 200,
    ellipsis: { tooltip: true }
  },
  {
    title: '排序',
    key: 'sort',
    width: 80,
    align: 'center'
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
                type: 'warning',
                text: true,
                onClick: () => handleAssignPermission(row)
              },
              { default: () => '分配权限', icon: () => h(NIcon, { component: LockClosedOutline }) }
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
const modalTitle = ref('新建角色')
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1,
  sort: 0
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 50, message: '角色名称长度为 2-50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色代码', trigger: 'blur' },
    { pattern: /^[A-Z_]+$/, message: '角色代码只能包含大写字母和下划线', trigger: 'blur' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过 200 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, type: 'number', message: '请选择状态', trigger: 'change' }
  ],
  sort: [
    { required: true, type: 'number', message: '请输入排序值', trigger: 'blur' }
  ]
}

// 状态选项
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

// 权限相关
const showPermissionModal = ref(false)
const permissionTree = ref([])
const checkedPermissions = ref([])
const expandedKeys = ref([])
const currentRoleId = ref(null)
const treeRef = ref(null)
const loadingPermissions = ref(false)
const assigningPermission = ref(false)

/**
 * 加载角色列表
 */
const loadData = async () => {
  try {
    loading.value = true
    const response = await getRoleList({
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
    console.error('加载角色列表失败:', error)
    message.error('加载角色列表失败')
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
 * 新建角色
 */
const handleCreate = () => {
  isEdit.value = false
  modalTitle.value = '新建角色'
  resetForm()
  showModal.value = true
}

/**
 * 编辑角色
 */
const handleEdit = (row) => {
  isEdit.value = true
  modalTitle.value = '编辑角色'
  Object.assign(formData, {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description,
    status: row.status,
    sort: row.sort || 0
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
      // 更新角色
      await updateRole(formData.id, formData)
      message.success('更新角色成功')
    } else {
      // 创建角色
      await createRole(formData)
      message.success('创建角色成功')
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
 * 删除角色
 */
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除角色"${row.name}"吗？此操作不可撤销。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deleteRole(row.id)
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
 * 分配权限
 */
const handleAssignPermission = async (row) => {
  currentRoleId.value = row.id
  checkedPermissions.value = row.permissionIds || []
  showPermissionModal.value = true

  // 加载权限树
  try {
    loadingPermissions.value = true
    const response = await getPermissionTree()
    permissionTree.value = Array.isArray(response) ? response : response.tree || []

    // 默认展开所有节点
    expandedKeys.value = getAllNodeKeys(permissionTree.value)
  } catch (error) {
    console.error('加载权限树失败:', error)
    message.error('加载权限树失败')
  } finally {
    loadingPermissions.value = false
  }
}

/**
 * 获取所有节点的 key（用于展开所有节点）
 */
const getAllNodeKeys = (nodes) => {
  const keys = []
  const traverse = (nodeList) => {
    nodeList.forEach(node => {
      keys.push(node.id)
      if (node.children && node.children.length) {
        traverse(node.children)
      }
    })
  }
  traverse(nodes)
  return keys
}

/**
 * 权限树选择变化
 */
const handlePermissionCheck = (keys) => {
  checkedPermissions.value = keys
}

/**
 * 确认分配权限
 */
const handleAssignPermissions = async () => {
  try {
    assigningPermission.value = true
    await assignRolePermissions(currentRoleId.value, checkedPermissions.value)
    message.success('分配权限成功')
    showPermissionModal.value = false
    loadData()
  } catch (error) {
    console.error('分配权限失败:', error)
    message.error('分配权限失败')
  } finally {
    assigningPermission.value = false
  }
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    name: '',
    code: '',
    description: '',
    status: 1,
    sort: 0
  })
  formRef.value?.restoreValidation()
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.role-management-page {
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

.permission-tree-container {
  max-height: 400px;
  overflow-y: auto;
  padding: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.02);
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

  .permission-tree-container {
    border-color: rgba(255, 255, 255, 0.15);
    background-color: rgba(255, 255, 255, 0.02);
  }
}
</style>

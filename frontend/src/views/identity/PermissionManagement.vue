<template>
  <div class="permission-management-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">权限管理</h1>
      <p class="page-description">管理系统权限和菜单结构</p>
    </div>

    <!-- 操作栏 -->
    <div class="action-bar apple-card">
      <div class="toolbar-left">
        <n-button type="primary" @click="handleCreate()">
          <template #icon>
            <n-icon :component="AddOutline" />
          </template>
          新建根权限
        </n-button>
        <n-button @click="expandAll">
          <template #icon>
            <n-icon :component="ExpandOutline" />
          </template>
          展开全部
        </n-button>
        <n-button @click="collapseAll">
          <template #icon>
            <n-icon :component="ContractOutline" />
          </template>
          收起全部
        </n-button>
      </div>
    </div>

    <!-- 权限树 -->
    <div class="tree-container apple-card">
      <n-spin :show="loading">
        <div class="tree-wrapper">
          <n-tree
            ref="treeRef"
            :data="treeData"
            :expanded-keys="expandedKeys"
            :render-label="renderLabel"
            :render-suffix="renderSuffix"
            key-field="id"
            label-field="name"
            children-field="children"
            block-line
            @update:expanded-keys="handleExpandedKeysChange"
          />
        </div>
      </n-spin>
    </div>

    <!-- 新建/编辑弹窗 -->
    <n-modal
      v-model:show="showModal"
      :mask-closable="false"
      preset="card"
      :title="modalTitle"
      class="permission-modal"
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
        <n-form-item label="上级权限">
          <n-input
            :value="parentName"
            disabled
            placeholder="根权限"
          />
        </n-form-item>

        <n-form-item label="权限名称" path="name">
          <n-input
            v-model:value="formData.name"
            placeholder="请输入权限名称"
          />
        </n-form-item>

        <n-form-item label="权限代码" path="code">
          <n-input
            v-model:value="formData.code"
            placeholder="请输入权限代码（如：user:create）"
          />
        </n-form-item>

        <n-form-item label="权限类型" path="type">
          <n-select
            v-model:value="formData.type"
            :options="typeOptions"
            placeholder="请选择权限类型"
          />
        </n-form-item>

        <n-form-item label="路由路径" path="path" v-if="formData.type === 1">
          <n-input
            v-model:value="formData.path"
            placeholder="请输入路由路径（如：/user/list）"
          />
        </n-form-item>

        <n-form-item label="图标" path="icon">
          <n-input
            v-model:value="formData.icon"
            placeholder="请输入图标名称"
          >
            <template #suffix>
              <n-icon v-if="formData.icon" :component="getIconComponent(formData.icon)" />
            </template>
          </n-input>
        </n-form-item>

        <n-form-item label="排序" path="sort">
          <n-input-number
            v-model:value="formData.sort"
            placeholder="请输入排序值"
            :min="0"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="状态" path="status">
          <n-select
            v-model:value="formData.status"
            :options="statusOptions"
            placeholder="请选择状态"
          />
        </n-form-item>

        <n-form-item label="描述" path="description">
          <n-input
            v-model:value="formData.description"
            type="textarea"
            placeholder="请输入权限描述"
            :autosize="{ minRows: 3, maxRows: 6 }"
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
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted, computed } from 'vue'
import { NButton, NTag, NSpace, NIcon, useMessage, useDialog } from 'naive-ui'
import {
  AddOutline,
  CreateOutline,
  TrashOutline,
  ExpandOutline,
  ContractOutline,
  FolderOutline,
  DocumentOutline,
  KeyOutline
} from '@vicons/ionicons5'
import {
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission
} from '../../api/permission'

// 消息提示和对话框
const message = useMessage()
const dialog = useDialog()

// 树形数据
const treeData = ref([])
const loading = ref(false)
const treeRef = ref(null)
const expandedKeys = ref([])

// 模态框相关
const showModal = ref(false)
const isEdit = ref(false)
const modalTitle = ref('新建权限')
const formRef = ref(null)
const submitting = ref(false)
const parentName = ref('')

// 表单数据
const formData = reactive({
  id: null,
  parentId: null,
  name: '',
  code: '',
  type: 1,
  path: '',
  icon: '',
  sort: 0,
  status: 1,
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入权限名称', trigger: 'blur' },
    { min: 2, max: 50, message: '权限名称长度为 2-50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入权限代码', trigger: 'blur' },
    { pattern: /^[a-z:_]+$/, message: '权限代码只能包含小写字母、冒号和下划线', trigger: 'blur' }
  ],
  type: [
    { required: true, type: 'number', message: '请选择权限类型', trigger: 'change' }
  ],
  status: [
    { required: true, type: 'number', message: '请选择状态', trigger: 'change' }
  ],
  sort: [
    { required: true, type: 'number', message: '请输入排序值', trigger: 'blur' }
  ]
}

// 权限类型选项
const typeOptions = [
  { label: '菜单', value: 1 },
  { label: '按钮', value: 2 },
  { label: 'API', value: 3 }
]

// 状态选项
const statusOptions = [
  { label: '正常', value: 1 },
  { label: '禁用', value: 0 }
]

/**
 * 加载权限树
 */
const loadData = async () => {
  try {
    loading.value = true
    const response = await getPermissionTree()
    treeData.value = Array.isArray(response) ? response : response.tree || []

    // 默认展开第一层
    expandedKeys.value = treeData.value.map(item => item.id)
  } catch (error) {
    console.error('加载权限树失败:', error)
    message.error('加载权限树失败')
  } finally {
    loading.value = false
  }
}

/**
 * 展开所有节点
 */
const expandAll = () => {
  expandedKeys.value = getAllNodeKeys(treeData.value)
}

/**
 * 收起所有节点
 */
const collapseAll = () => {
  expandedKeys.value = []
}

/**
 * 获取所有节点的 key
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
 * 处理展开/收起状态变化
 */
const handleExpandedKeysChange = (keys) => {
  expandedKeys.value = keys
}

/**
 * 渲染节点标签
 */
const renderLabel = ({ option }) => {
  const typeIcons = {
    1: FolderOutline,
    2: KeyOutline,
    3: DocumentOutline
  }
  const typeColors = {
    1: '#007AFF',
    2: '#34C759',
    3: '#FF9500'
  }

  return h(
    'div',
    { class: 'tree-label' },
    [
      h(NIcon, {
        component: typeIcons[option.type] || FolderOutline,
        size: 18,
        color: typeColors[option.type] || '#007AFF',
        style: { marginRight: '8px' }
      }),
      h('span', { class: 'label-text' }, option.name),
      h(NTag, {
        size: 'small',
        type: option.status === 1 ? 'success' : 'error',
        bordered: false,
        style: { marginLeft: '8px' }
      }, { default: () => option.status === 1 ? '正常' : '禁用' }),
      h('span', {
        class: 'label-code',
        style: { marginLeft: '8px', fontSize: '12px', color: 'rgba(0, 0, 0, 0.4)' }
      }, option.code)
    ]
  )
}

/**
 * 渲染节点后缀（操作按钮）
 */
const renderSuffix = ({ option }) => {
  return h(
    NSpace,
    { size: 4 },
    {
      default: () => [
        h(
          NButton,
          {
            size: 'tiny',
            type: 'primary',
            text: true,
            onClick: (e) => {
              e.stopPropagation()
              handleCreate(option)
            }
          },
          { default: () => '新建子权限' }
        ),
        h(
          NButton,
          {
            size: 'tiny',
            type: 'info',
            text: true,
            onClick: (e) => {
              e.stopPropagation()
              handleEdit(option)
            }
          },
          { default: () => '编辑' }
        ),
        h(
          NButton,
          {
            size: 'tiny',
            type: 'error',
            text: true,
            onClick: (e) => {
              e.stopPropagation()
              handleDelete(option)
            }
          },
          { default: () => '删除' }
        )
      ]
    }
  )
}

/**
 * 新建权限
 */
const handleCreate = (parent = null) => {
  isEdit.value = false
  modalTitle.value = parent ? '新建子权限' : '新建根权限'
  resetForm()

  if (parent) {
    formData.parentId = parent.id
    parentName.value = parent.name
  } else {
    formData.parentId = null
    parentName.value = ''
  }

  showModal.value = true
}

/**
 * 编辑权限
 */
const handleEdit = (node) => {
  isEdit.value = true
  modalTitle.value = '编辑权限'
  Object.assign(formData, {
    id: node.id,
    parentId: node.parentId,
    name: node.name,
    code: node.code,
    type: node.type,
    path: node.path || '',
    icon: node.icon || '',
    sort: node.sort || 0,
    status: node.status,
    description: node.description || ''
  })

  // 查找父节点名称
  if (node.parentId) {
    const parent = findNodeById(treeData.value, node.parentId)
    parentName.value = parent ? parent.name : ''
  } else {
    parentName.value = ''
  }

  showModal.value = true
}

/**
 * 根据 ID 查找节点
 */
const findNodeById = (nodes, id) => {
  for (const node of nodes) {
    if (node.id === id) {
      return node
    }
    if (node.children && node.children.length) {
      const found = findNodeById(node.children, id)
      if (found) return found
    }
  }
  return null
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
      // 更新权限
      await updatePermission(formData.id, formData)
      message.success('更新权限成功')
    } else {
      // 创建权限
      await createPermission(formData)
      message.success('创建权限成功')
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
 * 删除权限
 */
const handleDelete = (node) => {
  // 检查是否有子节点
  if (node.children && node.children.length > 0) {
    message.warning('该权限下存在子权限，无法删除')
    return
  }

  dialog.warning({
    title: '确认删除',
    content: `确定要删除权限"${node.name}"吗？此操作不可撤销。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        await deletePermission(node.id)
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
 * 获取图标组件（简单示例）
 */
const getIconComponent = (iconName) => {
  const iconMap = {
    'folder': FolderOutline,
    'document': DocumentOutline,
    'key': KeyOutline
  }
  return iconMap[iconName] || FolderOutline
}

/**
 * 重置表单
 */
const resetForm = () => {
  Object.assign(formData, {
    id: null,
    parentId: null,
    name: '',
    code: '',
    type: 1,
    path: '',
    icon: '',
    sort: 0,
    status: 1,
    description: ''
  })
  formRef.value?.restoreValidation()
}

// 初始化
onMounted(() => {
  loadData()
})
</script>

<style scoped>
.permission-management-page {
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
  padding: 20px;
  margin-bottom: 20px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tree-container {
  padding: 20px;
  min-height: 400px;
}

.tree-wrapper {
  padding: 12px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  background-color: rgba(0, 0, 0, 0.02);
}

.tree-label {
  display: flex;
  align-items: center;
  flex: 1;
}

.label-text {
  font-weight: 500;
  color: rgba(0, 0, 0, 0.9);
}

.label-code {
  font-family: ui-monospace, SFMono-Regular, 'SF Mono', Menlo, Consolas, monospace;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .toolbar-left {
    flex-direction: column;
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

  .tree-wrapper {
    border-color: rgba(255, 255, 255, 0.15);
    background-color: rgba(255, 255, 255, 0.02);
  }

  .label-text {
    color: rgba(255, 255, 255, 0.9);
  }

  .label-code {
    color: rgba(255, 255, 255, 0.4);
  }
}
</style>

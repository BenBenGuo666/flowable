<template>
  <div class="form-management">
    <n-card title="表单管理" :bordered="false">
      <!-- 搜索栏 -->
      <n-space vertical :size="16">
        <n-space justify="space-between">
          <n-space>
            <n-input
              v-model:value="searchParams.formName"
              placeholder="搜索表单名称"
              clearable
              style="width: 200px"
              @keyup.enter="handleSearch"
            >
              <template #prefix>
                <n-icon :component="SearchOutline" />
              </template>
            </n-input>

            <n-select
              v-model:value="searchParams.status"
              placeholder="状态"
              clearable
              style="width: 120px"
              :options="statusOptions"
              @update:value="handleSearch"
            />

            <n-select
              v-model:value="searchParams.category"
              placeholder="分类"
              clearable
              style="width: 150px"
              :options="categoryOptions"
              @update:value="handleSearch"
            />

            <n-button type="primary" @click="handleSearch">
              <template #icon>
                <n-icon :component="SearchOutline" />
              </template>
              搜索
            </n-button>

            <n-button @click="handleReset">
              <template #icon>
                <n-icon :component="RefreshOutline" />
              </template>
              重置
            </n-button>
          </n-space>

          <n-button type="primary" @click="handleCreate">
            <template #icon>
              <n-icon :component="AddOutline" />
            </template>
            新建表单
          </n-button>
        </n-space>

        <!-- 表格 -->
        <n-data-table
          :columns="columns"
          :data="tableData"
          :loading="loading"
          :pagination="pagination"
          :bordered="false"
          @update:page="handlePageChange"
          @update:page-size="handlePageSizeChange"
        />
      </n-space>
    </n-card>

    <!-- 表单详情/编辑对话框 -->
    <n-modal
      v-model:show="showFormDialog"
      preset="card"
      :title="dialogTitle"
      style="width: 800px"
      :mask-closable="false"
    >
      <n-form
        ref="formRef"
        :model="formModel"
        :rules="formRules"
        label-placement="left"
        label-width="100px"
      >
        <n-form-item label="表单Key" path="formKey">
          <n-input
            v-model:value="formModel.formKey"
            placeholder="请输入表单Key（唯一标识）"
            :disabled="isEdit"
          />
        </n-form-item>

        <n-form-item label="表单名称" path="formName">
          <n-input
            v-model:value="formModel.formName"
            placeholder="请输入表单名称"
          />
        </n-form-item>

        <n-form-item label="分类" path="category">
          <n-input
            v-model:value="formModel.category"
            placeholder="请输入分类"
          />
        </n-form-item>

        <n-form-item label="描述" path="description">
          <n-input
            v-model:value="formModel.description"
            type="textarea"
            placeholder="请输入描述"
            :autosize="{ minRows: 3, maxRows: 5 }"
          />
        </n-form-item>

        <n-form-item label="表单Schema" path="formSchema">
          <n-input
            v-model:value="formModel.formSchema"
            type="textarea"
            placeholder='请输入表单Schema (JSON格式)'
            :autosize="{ minRows: 6, maxRows: 10 }"
          />
        </n-form-item>

        <n-form-item label="表单配置" path="formConfig">
          <n-input
            v-model:value="formModel.formConfig"
            type="textarea"
            placeholder="请输入表单配置 (JSON格式，可选)"
            :autosize="{ minRows: 4, maxRows: 8 }"
          />
        </n-form-item>
      </n-form>

      <template #footer>
        <n-space justify="end">
          <n-button @click="showFormDialog = false">取消</n-button>
          <n-button type="primary" @click="handleSubmit" :loading="submitLoading">
            保存
          </n-button>
        </n-space>
      </template>
    </n-modal>

    <!-- 版本历史对话框 -->
    <n-modal
      v-model:show="showVersionDialog"
      preset="card"
      title="版本历史"
      style="width: 700px"
    >
      <n-list bordered>
        <n-list-item v-for="version in versionList" :key="version.id">
          <template #prefix>
            <n-tag :type="version.status === 'published' ? 'success' : 'default'">
              v{{ version.version }}
            </n-tag>
          </template>

          <n-thing :title="version.formName">
            <template #description>
              <n-space vertical size="small">
                <span>状态: {{ version.status === 'published' ? '已发布' : '草稿' }}</span>
                <span>创建时间: {{ version.createdTime }}</span>
                <span v-if="version.updatedBy">更新人: {{ version.updatedBy }}</span>
              </n-space>
            </template>
          </n-thing>

          <template #suffix>
            <n-space>
              <n-button size="small" @click="handleViewVersion(version)">
                查看
              </n-button>
              <n-button
                v-if="version.status === 'draft'"
                size="small"
                type="primary"
                @click="handleEditVersion(version)"
              >
                编辑
              </n-button>
            </n-space>
          </template>
        </n-list-item>
      </n-list>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, reactive, h, onMounted } from 'vue'
import { useMessage, useDialog } from 'naive-ui'
import {
  SearchOutline,
  RefreshOutline,
  AddOutline,
  CreateOutline,
  TrashOutline,
  EyeOutline,
  CloudUploadOutline,
  CopyOutline,
  TimeOutline
} from '@vicons/ionicons5'
import {
  getFormDefinitionList,
  createFormDefinition,
  updateFormDefinition,
  deleteFormDefinition,
  publishFormDefinition,
  createNewVersion,
  getAllVersions
} from '@/api/form'

const message = useMessage()
const dialog = useDialog()

// 搜索参数
const searchParams = reactive({
  formName: '',
  status: null,
  category: null
})

// 状态选项
const statusOptions = [
  { label: '草稿', value: 'draft' },
  { label: '已发布', value: 'published' }
]

// 分类选项 (可以从后端获取)
const categoryOptions = ref([
  { label: 'HR', value: 'HR' },
  { label: '财务', value: 'FINANCE' },
  { label: '行政', value: 'ADMIN' },
  { label: '采购', value: 'PURCHASE' },
  { label: '其他', value: 'OTHER' }
])

// 表格数据
const tableData = ref([])
const loading = ref(false)

// 分页
const pagination = reactive({
  page: 1,
  pageSize: 20,
  itemCount: 0,
  showSizePicker: true,
  pageSizes: [10, 20, 50, 100]
})

// 表格列定义
const columns = [
  {
    title: 'ID',
    key: 'id',
    width: 80
  },
  {
    title: '表单名称',
    key: 'formName',
    width: 200
  },
  {
    title: '表单Key',
    key: 'formKey',
    width: 180
  },
  {
    title: '分类',
    key: 'category',
    width: 100
  },
  {
    title: '版本',
    key: 'version',
    width: 80
  },
  {
    title: '状态',
    key: 'status',
    width: 100,
    render: (row) => {
      return h(
        'n-tag',
        {
          type: row.status === 'published' ? 'success' : 'default'
        },
        { default: () => (row.status === 'published' ? '已发布' : '草稿') }
      )
    }
  },
  {
    title: '创建时间',
    key: 'createdTime',
    width: 180
  },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    render: (row) => {
      return h('n-space', null, {
        default: () => [
          h(
            'n-button',
            {
              size: 'small',
              onClick: () => handleView(row)
            },
            { default: () => '查看', icon: () => h('n-icon', { component: EyeOutline }) }
          ),
          h(
            'n-button',
            {
              size: 'small',
              type: 'primary',
              disabled: row.status === 'published',
              onClick: () => handleEdit(row)
            },
            { default: () => '编辑', icon: () => h('n-icon', { component: CreateOutline }) }
          ),
          h(
            'n-button',
            {
              size: 'small',
              type: 'success',
              disabled: row.status === 'published',
              onClick: () => handlePublish(row)
            },
            { default: () => '发布', icon: () => h('n-icon', { component: CloudUploadOutline }) }
          ),
          h(
            'n-button',
            {
              size: 'small',
              onClick: () => handleViewVersions(row)
            },
            { default: () => '版本', icon: () => h('n-icon', { component: TimeOutline }) }
          ),
          h(
            'n-button',
            {
              size: 'small',
              onClick: () => handleNewVersion(row)
            },
            { default: () => '新版本', icon: () => h('n-icon', { component: CopyOutline }) }
          ),
          h(
            'n-button',
            {
              size: 'small',
              type: 'error',
              onClick: () => handleDelete(row)
            },
            { default: () => '删除', icon: () => h('n-icon', { component: TrashOutline }) }
          )
        ]
      })
    }
  }
]

// 表单对话框
const showFormDialog = ref(false)
const dialogTitle = ref('新建表单')
const isEdit = ref(false)
const formRef = ref(null)
const submitLoading = ref(false)

const formModel = reactive({
  id: null,
  formKey: '',
  formName: '',
  category: '',
  description: '',
  formSchema: '',
  formConfig: ''
})

const formRules = {
  formKey: [
    { required: true, message: '请输入表单Key', trigger: 'blur' },
    { pattern: /^[a-zA-Z][a-zA-Z0-9_]*$/, message: '表单Key必须以字母开头，只能包含字母、数字和下划线', trigger: 'blur' }
  ],
  formName: [
    { required: true, message: '请输入表单名称', trigger: 'blur' }
  ],
  formSchema: [
    { required: true, message: '请输入表单Schema', trigger: 'blur' }
  ]
}

// 版本历史对话框
const showVersionDialog = ref(false)
const versionList = ref([])

// 加载表单列表
const loadFormList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page,
      size: pagination.pageSize,
      ...searchParams
    }

    const response = await getFormDefinitionList(params)
    if (response.data.code === 200) {
      const pageData = response.data.data
      tableData.value = pageData.records || []
      pagination.itemCount = pageData.total || 0
    } else {
      message.error(response.data.message || '加载失败')
    }
  } catch (error) {
    console.error('加载表单列表失败:', error)
    message.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  loadFormList()
}

// 重置
const handleReset = () => {
  searchParams.formName = ''
  searchParams.status = null
  searchParams.category = null
  handleSearch()
}

// 分页变化
const handlePageChange = (page) => {
  pagination.page = page
  loadFormList()
}

const handlePageSizeChange = (pageSize) => {
  pagination.pageSize = pageSize
  pagination.page = 1
  loadFormList()
}

// 新建表单
const handleCreate = () => {
  isEdit.value = false
  dialogTitle.value = '新建表单'
  Object.assign(formModel, {
    id: null,
    formKey: '',
    formName: '',
    category: '',
    description: '',
    formSchema: '{}',
    formConfig: '{}'
  })
  showFormDialog.value = true
}

// 编辑表单
const handleEdit = (row) => {
  isEdit.value = true
  dialogTitle.value = '编辑表单'
  Object.assign(formModel, {
    id: row.id,
    formKey: row.formKey,
    formName: row.formName,
    category: row.category,
    description: row.description,
    formSchema: row.formSchema,
    formConfig: row.formConfig
  })
  showFormDialog.value = true
}

// 查看表单
const handleView = (row) => {
  isEdit.value = true
  dialogTitle.value = '查看表单'
  Object.assign(formModel, row)
  showFormDialog.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value?.validate()

    submitLoading.value = true

    const data = {
      formKey: formModel.formKey,
      formName: formModel.formName,
      category: formModel.category,
      description: formModel.description,
      formSchema: formModel.formSchema,
      formConfig: formModel.formConfig
    }

    let response
    if (isEdit.value && formModel.id) {
      response = await updateFormDefinition(formModel.id, data)
    } else {
      response = await createFormDefinition(data)
    }

    if (response.data.code === 200) {
      message.success(response.data.message || '保存成功')
      showFormDialog.value = false
      loadFormList()
    } else {
      message.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    if (error.errors) {
      // 验证错误
      return
    }
    message.error('保存失败')
  } finally {
    submitLoading.value = false
  }
}

// 发布表单
const handlePublish = (row) => {
  dialog.warning({
    title: '确认发布',
    content: `确定要发布表单"${row.formName}"吗？发布后将不可修改，只能创建新版本。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const response = await publishFormDefinition(row.id)
        if (response.data.code === 200) {
          message.success('发布成功')
          loadFormList()
        } else {
          message.error(response.data.message || '发布失败')
        }
      } catch (error) {
        console.error('发布失败:', error)
        message.error('发布失败')
      }
    }
  })
}

// 创建新版本
const handleNewVersion = (row) => {
  dialog.info({
    title: '创建新版本',
    content: `确定要基于表单"${row.formName}"创建新版本吗？`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const response = await createNewVersion(row.formKey)
        if (response.data.code === 200) {
          message.success('新版本创建成功')
          loadFormList()
        } else {
          message.error(response.data.message || '创建失败')
        }
      } catch (error) {
        console.error('创建新版本失败:', error)
        message.error('创建失败')
      }
    }
  })
}

// 查看版本历史
const handleViewVersions = async (row) => {
  try {
    const response = await getAllVersions(row.formKey)
    if (response.data.code === 200) {
      versionList.value = response.data.data || []
      showVersionDialog.value = true
    } else {
      message.error(response.data.message || '加载版本历史失败')
    }
  } catch (error) {
    console.error('加载版本历史失败:', error)
    message.error('加载版本历史失败')
  }
}

// 查看版本
const handleViewVersion = (version) => {
  isEdit.value = true
  dialogTitle.value = `查看版本 v${version.version}`
  Object.assign(formModel, version)
  showFormDialog.value = true
  showVersionDialog.value = false
}

// 编辑版本
const handleEditVersion = (version) => {
  isEdit.value = true
  dialogTitle.value = `编辑版本 v${version.version}`
  Object.assign(formModel, version)
  showFormDialog.value = true
  showVersionDialog.value = false
}

// 删除表单
const handleDelete = (row) => {
  dialog.warning({
    title: '确认删除',
    content: `确定要删除表单"${row.formName}"吗？此操作不可恢复。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      try {
        const response = await deleteFormDefinition(row.id)
        if (response.data.code === 200) {
          message.success('删除成功')
          loadFormList()
        } else {
          message.error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除失败:', error)
        message.error('删除失败')
      }
    }
  })
}

// 初始化
onMounted(() => {
  loadFormList()
})
</script>

<style scoped>
.form-management {
  padding: 20px;
}
</style>

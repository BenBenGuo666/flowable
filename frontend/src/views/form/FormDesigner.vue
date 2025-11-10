<template>
  <div class="form-designer">
    <n-card title="表单设计器" :bordered="false">
      <template #header-extra>
        <n-space>
          <n-button @click="handleBack">
            <template #icon>
              <n-icon :component="ArrowBackOutline" />
            </template>
            返回
          </n-button>
          <n-button type="primary" @click="handleSave" :loading="saveLoading">
            <template #icon>
              <n-icon :component="SaveOutline" />
            </template>
            保存
          </n-button>
          <n-button
            v-if="formData.status === 'draft'"
            type="success"
            @click="handlePublish"
            :loading="publishLoading"
          >
            <template #icon>
              <n-icon :component="CloudUploadOutline" />
            </template>
            发布
          </n-button>
        </n-space>
      </template>

      <n-space vertical :size="20">
        <!-- 基本信息 -->
        <n-card title="基本信息" size="small">
          <n-form :model="formData" label-placement="left" label-width="120px">
            <n-grid :cols="2" :x-gap="24">
              <n-form-item-gi label="表单Key">
                <n-input v-model:value="formData.formKey" :disabled="isEdit" />
              </n-form-item-gi>
              <n-form-item-gi label="表单名称">
                <n-input v-model:value="formData.formName" />
              </n-form-item-gi>
              <n-form-item-gi label="分类">
                <n-input v-model:value="formData.category" />
              </n-form-item-gi>
              <n-form-item-gi label="状态">
                <n-tag :type="formData.status === 'published' ? 'success' : 'default'">
                  {{ formData.status === 'published' ? '已发布' : '草稿' }}
                </n-tag>
              </n-form-item-gi>
            </n-grid>
            <n-form-item label="描述">
              <n-input
                v-model:value="formData.description"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 4 }"
              />
            </n-form-item>
          </n-form>
        </n-card>

        <!-- 表单设计区域 -->
        <n-card title="表单设计" size="small">
          <n-tabs type="line" animated>
            <!-- 可视化设计 -->
            <n-tab-pane name="visual" tab="可视化设计">
              <n-alert type="info" style="margin-bottom: 16px">
                <template #icon>
                  <n-icon :component="InformationCircleOutline" />
                </template>
                可视化表单设计器功能正在开发中。当前请使用 JSON 编辑器定义表单结构。
              </n-alert>

              <!-- 表单预览区域 -->
              <div class="form-preview">
                <n-card title="表单预览" size="small">
                  <div v-if="parsedSchema && parsedSchema.fields" class="preview-content">
                    <n-form label-placement="left" label-width="120px">
                      <n-form-item
                        v-for="field in parsedSchema.fields"
                        :key="field.id"
                        :label="field.label"
                      >
                        <n-input
                          v-if="field.type === 'text'"
                          :placeholder="'请输入' + field.label"
                          :disabled="true"
                        />
                        <n-input-number
                          v-else-if="field.type === 'number'"
                          :placeholder="'请输入' + field.label"
                          :disabled="true"
                          style="width: 100%"
                        />
                        <n-date-picker
                          v-else-if="field.type === 'date'"
                          :placeholder="'请选择' + field.label"
                          :disabled="true"
                          style="width: 100%"
                        />
                        <n-select
                          v-else-if="field.type === 'select'"
                          :placeholder="'请选择' + field.label"
                          :options="field.options || []"
                          :disabled="true"
                        />
                        <n-checkbox v-else-if="field.type === 'checkbox'" :disabled="true">
                          {{ field.label }}
                        </n-checkbox>
                        <n-upload v-else-if="field.type === 'file'" :disabled="true">
                          <n-button>选择文件</n-button>
                        </n-upload>
                      </n-form-item>
                    </n-form>
                  </div>
                  <n-empty
                    v-else
                    description="暂无表单字段，请在 JSON 编辑器中定义"
                    style="margin: 40px 0"
                  />
                </n-card>
              </div>
            </n-tab-pane>

            <!-- JSON 编辑器 -->
            <n-tab-pane name="json" tab="JSON 编辑器">
              <n-space vertical :size="16">
                <n-alert type="warning">
                  <template #icon>
                    <n-icon :component="WarningOutline" />
                  </template>
                  请确保输入的是有效的 JSON 格式。表单 Schema 应包含 fields 数组。
                </n-alert>

                <n-card title="表单 Schema" size="small">
                  <n-input
                    v-model:value="formData.formSchema"
                    type="textarea"
                    :autosize="{ minRows: 15, maxRows: 30 }"
                    placeholder="请输入表单 Schema (JSON 格式)"
                    @blur="validateJson"
                  />
                  <template #footer>
                    <n-space justify="space-between">
                      <n-text depth="3">
                        提示: 使用 fields 数组定义表单字段，每个字段包含 id, type, label 等属性
                      </n-text>
                      <n-button size="small" @click="handleFormatJson">
                        <template #icon>
                          <n-icon :component="CodeSlashOutline" />
                        </template>
                        格式化
                      </n-button>
                    </n-space>
                  </template>
                </n-card>

                <n-card title="表单配置 (可选)" size="small">
                  <n-input
                    v-model:value="formData.formConfig"
                    type="textarea"
                    :autosize="{ minRows: 8, maxRows: 15 }"
                    placeholder="请输入表单配置 (JSON 格式，可选)"
                  />
                </n-card>

                <!-- 示例代码 -->
                <n-collapse>
                  <n-collapse-item title="查看示例" name="example">
                    <n-code :code="exampleSchema" language="json" />
                    <template #header-extra>
                      <n-button size="tiny" @click="useExample">
                        使用此示例
                      </n-button>
                    </template>
                  </n-collapse-item>
                </n-collapse>
              </n-space>
            </n-tab-pane>
          </n-tabs>
        </n-card>
      </n-space>
    </n-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useDialog } from 'naive-ui'
import {
  ArrowBackOutline,
  SaveOutline,
  CloudUploadOutline,
  InformationCircleOutline,
  WarningOutline,
  CodeSlashOutline
} from '@vicons/ionicons5'
import {
  getFormDefinitionById,
  createFormDefinition,
  updateFormDefinition,
  publishFormDefinition
} from '@/api/form'

const route = useRoute()
const router = useRouter()
const message = useMessage()
const dialog = useDialog()

const isEdit = ref(false)
const saveLoading = ref(false)
const publishLoading = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  formKey: '',
  formName: '',
  category: '',
  description: '',
  status: 'draft',
  formSchema: '{}',
  formConfig: '{}'
})

// 解析 Schema 用于预览
const parsedSchema = computed(() => {
  try {
    return JSON.parse(formData.formSchema)
  } catch (e) {
    return null
  }
})

// 示例 Schema
const exampleSchema = `{
  "fields": [
    {
      "id": "name",
      "type": "text",
      "label": "姓名",
      "required": true,
      "placeholder": "请输入姓名"
    },
    {
      "id": "age",
      "type": "number",
      "label": "年龄",
      "required": false
    },
    {
      "id": "gender",
      "type": "select",
      "label": "性别",
      "required": true,
      "options": [
        { "label": "男", "value": "male" },
        { "label": "女", "value": "female" }
      ]
    },
    {
      "id": "birthday",
      "type": "date",
      "label": "出生日期",
      "required": false
    },
    {
      "id": "agree",
      "type": "checkbox",
      "label": "我已阅读并同意相关条款",
      "required": true
    }
  ]
}`

// 加载表单数据
const loadFormData = async () => {
  const id = route.params.id
  if (id) {
    isEdit.value = true
    try {
      const response = await getFormDefinitionById(id)
      if (response.data.code === 200) {
        Object.assign(formData, response.data.data)
      } else {
        message.error('加载表单失败')
      }
    } catch (error) {
      console.error('加载表单失败:', error)
      message.error('加载表单失败')
    }
  }
}

// 验证 JSON
const validateJson = () => {
  try {
    JSON.parse(formData.formSchema)
    if (formData.formConfig) {
      JSON.parse(formData.formConfig)
    }
  } catch (e) {
    message.error('JSON 格式错误: ' + e.message)
  }
}

// 格式化 JSON
const handleFormatJson = () => {
  try {
    const formatted = JSON.stringify(JSON.parse(formData.formSchema), null, 2)
    formData.formSchema = formatted
    message.success('格式化成功')
  } catch (e) {
    message.error('JSON 格式错误，无法格式化')
  }
}

// 使用示例
const useExample = () => {
  formData.formSchema = exampleSchema
  formData.formConfig = '{}'
  message.success('已加载示例')
}

// 保存表单
const handleSave = async () => {
  if (!formData.formKey || !formData.formName) {
    message.error('请填写表单Key和名称')
    return
  }

  // 验证 JSON
  try {
    JSON.parse(formData.formSchema)
    if (formData.formConfig) {
      JSON.parse(formData.formConfig)
    }
  } catch (e) {
    message.error('JSON 格式错误: ' + e.message)
    return
  }

  saveLoading.value = true
  try {
    const data = {
      formKey: formData.formKey,
      formName: formData.formName,
      category: formData.category,
      description: formData.description,
      formSchema: formData.formSchema,
      formConfig: formData.formConfig
    }

    let response
    if (isEdit.value && formData.id) {
      response = await updateFormDefinition(formData.id, data)
    } else {
      response = await createFormDefinition(data)
    }

    if (response.data.code === 200) {
      message.success('保存成功')
      if (!isEdit.value) {
        // 新建后跳转到编辑模式
        const newId = response.data.data.id
        router.replace(`/form/designer/${newId}`)
        isEdit.value = true
        formData.id = newId
      }
    } else {
      message.error(response.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存失败:', error)
    message.error('保存失败')
  } finally {
    saveLoading.value = false
  }
}

// 发布表单
const handlePublish = () => {
  dialog.warning({
    title: '确认发布',
    content: `确定要发布表单"${formData.formName}"吗？发布后将不可修改。`,
    positiveText: '确定',
    negativeText: '取消',
    onPositiveClick: async () => {
      publishLoading.value = true
      try {
        const response = await publishFormDefinition(formData.id)
        if (response.data.code === 200) {
          message.success('发布成功')
          formData.status = 'published'
        } else {
          message.error(response.data.message || '发布失败')
        }
      } catch (error) {
        console.error('发布失败:', error)
        message.error('发布失败')
      } finally {
        publishLoading.value = false
      }
    }
  })
}

// 返回
const handleBack = () => {
  router.push('/form/management')
}

// 初始化
onMounted(() => {
  loadFormData()
})
</script>

<style scoped>
.form-designer {
  padding: 20px;
}

.form-preview {
  margin-top: 16px;
}

.preview-content {
  max-width: 800px;
  margin: 0 auto;
}
</style>

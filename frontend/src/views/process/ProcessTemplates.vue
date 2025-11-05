<template>
  <div class="process-templates-page">
    <h1 class="page-title">流程模板库</h1>

    <n-grid :cols="3" :x-gap="20" :y-gap="20">
      <n-gi v-for="template in templates" :key="template.id">
        <div class="template-card apple-card">
          <div class="template-icon">{{ template.icon }}</div>
          <h3>{{ template.name }}</h3>
          <p class="template-desc">{{ template.description }}</p>
          <div class="template-category">
            <n-tag size="small" :bordered="false">{{ template.category }}</n-tag>
          </div>
          <n-space class="template-actions">
            <n-button size="small" @click="viewTemplate(template)">
              预览
            </n-button>
            <n-button size="small" type="primary" @click="useTemplate(template)">
              使用模板
            </n-button>
          </n-space>
        </div>
      </n-gi>
    </n-grid>

    <!-- 预览对话框 -->
    <n-modal
      v-model:show="showPreview"
      preset="card"
      title="模板预览"
      style="width: 80%; max-width: 1000px"
    >
      <div v-if="currentTemplate">
        <h3>{{ currentTemplate.name }}</h3>
        <p>{{ currentTemplate.description }}</p>
        <n-code :code="currentTemplate.bpmnXml" language="xml" :word-wrap="true" />
      </div>
    </n-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { getAllTemplates, deployProcessDefinition } from '../../api/process'

const router = useRouter()
const message = useMessage()
const templates = ref([])
const showPreview = ref(false)
const currentTemplate = ref(null)

// 加载模板
const loadTemplates = async () => {
  try {
    const data = await getAllTemplates()
    templates.value = data || []
  } catch (error) {
    console.error('加载模板失败:', error)
    message.error('加载模板失败')
  }
}

// 预览模板
const viewTemplate = (template) => {
  currentTemplate.value = template
  showPreview.value = true
}

// 使用模板
const useTemplate = async (template) => {
  try {
    await deployProcessDefinition({
      name: template.name,
      key: template.id,
      category: template.category,
      bpmnXml: template.bpmnXml
    })
    message.success('模板部署成功')
    setTimeout(() => {
      router.push('/process/definitions')
    }, 1000)
  } catch (error) {
    message.error('部署失败')
  }
}

onMounted(() => {
  loadTemplates()
})
</script>

<style scoped>
.process-templates-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 30px;
  color: #007AFF;
}

.template-card {
  padding: 30px;
  text-align: center;
  transition: all 0.3s ease;
  cursor: pointer;
}

.template-card:hover {
  transform: translateY(-4px);
}

.template-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.template-card h3 {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 12px;
  color: #007AFF;
}

.template-desc {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.6);
  margin-bottom: 16px;
  min-height: 60px;
}

.template-category {
  margin-bottom: 20px;
}

.template-actions {
  justify-content: center;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .template-desc {
    color: rgba(255, 255, 255, 0.6);
  }
}
</style>

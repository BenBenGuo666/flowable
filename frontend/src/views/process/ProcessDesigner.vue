<template>
  <div class="process-designer-page">
    <div class="designer-header apple-card">
      <div class="header-left">
        <n-button @click="goBack">← 返回</n-button>
        <n-input
          v-model:value="processName"
          placeholder="流程名称"
          style="width: 300px; margin-left: 12px"
        />
      </div>
      <div class="header-right">
        <n-space>
          <n-button @click="handleImport">
            📥 导入 BPMN
          </n-button>
          <n-button @click="handleExport">
            📤 导出 BPMN
          </n-button>
          <n-button type="primary" @click="handleSave" :loading="saving">
            💾 保存
          </n-button>
          <n-button type="success" @click="handleDeploy" :loading="deploying">
            🚀 部署
          </n-button>
        </n-space>
      </div>
    </div>

    <div class="designer-workspace">
      <div class="designer-container">
        <div ref="designerRef" class="bpmn-canvas"></div>
      </div>
      <div class="properties-panel-container">
        <div ref="propertiesPanelRef" class="properties-panel"></div>
      </div>
    </div>

    <!-- 文件上传 -->
    <input
      ref="fileInput"
      type="file"
      accept=".bpmn,.xml"
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import {
  BpmnPropertiesPanelModule,
  BpmnPropertiesProviderModule
} from 'bpmn-js-properties-panel'
import { deployProcessDefinition, getProcessDefinitionXml } from '../../api/process'

// 导入样式
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'

const router = useRouter()
const route = useRoute()
const message = useMessage()

const designerRef = ref(null)
const propertiesPanelRef = ref(null)
const fileInput = ref(null)
const processName = ref('新流程')
const saving = ref(false)
const deploying = ref(false)

let bpmnModeler = null

// 默认的BPMN XML
const defaultBpmnXML = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:flowable="http://flowable.org/bpmn"
             targetNamespace="http://flowable.org/test">
  <process id="process_${Date.now()}" name="${processName.value}" isExecutable="true">
    <startEvent id="startEvent" name="开始"/>
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_process">
    <bpmndi:BPMNPlane id="BPMNPlane_process" bpmnElement="process_${Date.now()}">
      <bpmndi:BPMNShape id="BPMNShape_startEvent" bpmnElement="startEvent">
        <omgdc:Bounds x="100" y="100" width="36" height="36"/>
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`

// 初始化BPMN建模器
const initBpmnModeler = async () => {
  bpmnModeler = new BpmnModeler({
    container: designerRef.value,
    keyboard: {
      bindTo: document
    },
    propertiesPanel: {
      parent: propertiesPanelRef.value
    },
    additionalModules: [
      BpmnPropertiesPanelModule,
      BpmnPropertiesProviderModule
    ]
  })

  try {
    // 如果有流程ID，加载流程定义
    if (route.query.id) {
      const xml = await getProcessDefinitionXml(route.query.id)
      await bpmnModeler.importXML(xml)
    } else {
      await bpmnModeler.importXML(defaultBpmnXML)
    }

    const canvas = bpmnModeler.get('canvas')
    canvas.zoom('fit-viewport')
  } catch (error) {
    console.error('初始化BPMN建模器失败:', error)
    message.error('初始化失败')
  }
}

// 返回
const goBack = () => {
  router.back()
}

// 导入BPMN
const handleImport = () => {
  fileInput.value.click()
}

// 处理文件选择
const handleFileChange = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    const text = await file.text()
    await bpmnModeler.importXML(text)
    message.success('导入成功')
  } catch (error) {
    console.error('导入失败:', error)
    message.error('导入失败')
  }

  // 清空input
  event.target.value = ''
}

// 导出BPMN
const handleExport = async () => {
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })
    const blob = new Blob([xml], { type: 'application/xml' })
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `${processName.value || 'process'}.bpmn`
    link.click()
    URL.revokeObjectURL(url)
    message.success('导出成功')
  } catch (error) {
    console.error('导出失败:', error)
    message.error('导出失败')
  }
}

// 保存
const handleSave = async () => {
  // 这里可以保存到本地存储或后端
  message.info('保存功能待实现')
}

// 部署
const handleDeploy = async () => {
  deploying.value = true
  try {
    const { xml } = await bpmnModeler.saveXML({ format: true })

    // 从XML中提取流程Key
    const keyMatch = xml.match(/process id="([^"]+)"/)
    const processKey = keyMatch ? keyMatch[1] : `process_${Date.now()}`

    await deployProcessDefinition({
      name: processName.value,
      key: processKey,
      category: 'custom',
      bpmnXml: xml
    })

    message.success('流程部署成功')
    setTimeout(() => {
      router.push('/process/definitions')
    }, 1000)
  } catch (error) {
    console.error('部署失败:', error)
    message.error('部署失败: ' + (error.message || '未知错误'))
  } finally {
    deploying.value = false
  }
}

onMounted(() => {
  initBpmnModeler()
})

onBeforeUnmount(() => {
  if (bpmnModeler) {
    bpmnModeler.destroy()
  }
})
</script>

<style scoped>
.process-designer-page {
  height: calc(100vh - 88px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.designer-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  margin-bottom: 16px;
  flex-shrink: 0;
}

.header-left,
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.designer-workspace {
  flex: 1;
  display: flex;
  gap: 16px;
  min-height: 0;
  overflow: hidden;
}

.designer-container {
  flex: 1;
  position: relative;
  background: #FAFAFA;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.1);
}

.bpmn-canvas {
  width: 100%;
  height: 100%;
}

.properties-panel-container {
  width: 320px;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05), 0 1px 2px rgba(0, 0, 0, 0.1);
  flex-shrink: 0;
}

.properties-panel {
  width: 100%;
  height: 100%;
  overflow-y: auto;
  overflow-x: hidden;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .designer-container {
    background: #1C1C1E;
  }

  .properties-panel-container {
    background: #2C2C2E;
  }
}

/* BPMN.js核心样式覆盖 */
:deep(.djs-container) {
  background: #FAFAFA !important;
}

:deep(.djs-palette) {
  left: 20px;
  top: 20px;
  border-radius: 8px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: white;
}

:deep(.djs-palette .entry) {
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

:deep(.djs-palette .entry:hover) {
  background: rgba(0, 122, 255, 0.1);
}

:deep(.djs-context-pad) {
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

:deep(.djs-shape .djs-visual > :nth-child(1)) {
  stroke-width: 2px !important;
}

/* 属性面板样式优化 */
:deep(.bio-properties-panel) {
  background: transparent;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

:deep(.bio-properties-panel-header) {
  background: linear-gradient(180deg, #F9F9FB 0%, #F5F5F7 100%);
  border-bottom: 1px solid #E5E5E7;
  padding: 16px;
  font-weight: 600;
  font-size: 15px;
  color: #1D1D1F;
}

:deep(.bio-properties-panel-group) {
  border-bottom: 1px solid #F0F0F2;
}

:deep(.bio-properties-panel-group-header) {
  background: #FAFAFA;
  padding: 10px 16px;
  font-size: 13px;
  font-weight: 600;
  color: #86868B;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

:deep(.bio-properties-panel-entry) {
  padding: 12px 16px;
  border-bottom: 1px solid #F5F5F7;
}

:deep(.bio-properties-panel-label) {
  font-size: 13px;
  font-weight: 500;
  color: #1D1D1F;
  margin-bottom: 6px;
}

:deep(.bio-properties-panel-entry input),
:deep(.bio-properties-panel-entry select),
:deep(.bio-properties-panel-entry textarea) {
  width: 100%;
  border: 1px solid #D2D2D7;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  background: white;
}

:deep(.bio-properties-panel-entry input:hover),
:deep(.bio-properties-panel-entry select:hover),
:deep(.bio-properties-panel-entry textarea:hover) {
  border-color: #B8B8BD;
}

:deep(.bio-properties-panel-entry input:focus),
:deep(.bio-properties-panel-entry select:focus),
:deep(.bio-properties-panel-entry textarea:focus) {
  border-color: #007AFF;
  outline: none;
  box-shadow: 0 0 0 4px rgba(0, 122, 255, 0.1);
}

:deep(.bio-properties-panel-entry textarea) {
  min-height: 60px;
  resize: vertical;
}

/* 滚动条样式 */
.properties-panel::-webkit-scrollbar {
  width: 10px;
}

.properties-panel::-webkit-scrollbar-track {
  background: transparent;
  margin: 8px 0;
}

.properties-panel::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.15);
  border-radius: 10px;
  border: 2px solid transparent;
  background-clip: padding-box;
}

.properties-panel::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.25);
  background-clip: padding-box;
}

/* 元素选中高亮 */
:deep(.djs-shape.selected .djs-visual > :nth-child(1)),
:deep(.djs-connection.selected .djs-visual > :nth-child(1)) {
  stroke: #007AFF !important;
  stroke-width: 2px !important;
}
</style>

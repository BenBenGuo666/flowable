<template>
  <div class="leave-detail-page">
    <div class="page-header">
      <n-button @click="goBack">
        ← 返回
      </n-button>
      <h1>请假详情</h1>
    </div>

    <n-spin :show="loading">
      <div class="detail-container">
        <!-- 基本信息 -->
        <div class="info-card apple-card">
          <h3>基本信息</h3>
          <n-descriptions :column="2" bordered>
            <n-descriptions-item label="申请人">
              {{ detail.applicant }}
            </n-descriptions-item>
            <n-descriptions-item label="申请时间">
              {{ detail.applyTime }}
            </n-descriptions-item>
            <n-descriptions-item label="请假类型">
              {{ getTypeLabel(detail.type) }}
            </n-descriptions-item>
            <n-descriptions-item label="状态">
              <n-tag :type="getStatusType(detail.status)">
                {{ getStatusLabel(detail.status) }}
              </n-tag>
            </n-descriptions-item>
            <n-descriptions-item label="开始时间">
              {{ detail.startTime }}
            </n-descriptions-item>
            <n-descriptions-item label="结束时间">
              {{ detail.endTime }}
            </n-descriptions-item>
            <n-descriptions-item label="请假天数">
              {{ detail.days }} 天
            </n-descriptions-item>
            <n-descriptions-item label="请假原因" :span="2">
              {{ detail.reason }}
            </n-descriptions-item>
          </n-descriptions>
        </div>

        <!-- 审批历史 -->
        <div class="history-card apple-card">
          <h3>审批历史</h3>
          <n-timeline>
            <n-timeline-item
              v-for="(item, index) in detail.history"
              :key="index"
              :type="item.type"
              :title="item.title"
              :content="item.content"
              :time="item.time"
            />
          </n-timeline>
        </div>

        <!-- 流程图 -->
        <div class="diagram-card apple-card">
          <h3>流程图</h3>
          <div ref="diagramContainer" class="diagram-container"></div>
        </div>
      </div>
    </n-spin>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLeaveDetail } from '../../api/leave'
import BpmnViewer from 'bpmn-js/lib/NavigatedViewer'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const diagramContainer = ref(null)

// 详情数据
const detail = ref({
  applicant: '',
  applyTime: '',
  type: '',
  status: '',
  startTime: '',
  endTime: '',
  days: 0,
  reason: '',
  history: []
})

// 类型映射
const getTypeLabel = (type) => {
  const map = {
    personal: '事假',
    sick: '病假',
    annual: '年假',
    compensatory: '调休'
  }
  return map[type] || type
}

// 状态映射
const getStatusLabel = (status) => {
  const map = {
    pending: '待审批',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return map[status] || status
}

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'error'
  }
  return map[status] || 'default'
}

// 返回
const goBack = () => {
  router.back()
}

// 初始化流程图
const initDiagram = async (xml) => {
  if (!diagramContainer.value) return

  const viewer = new BpmnViewer({
    container: diagramContainer.value,
    width: '100%',
    height: '500px'
  })

  try {
    await viewer.importXML(xml)
    const canvas = viewer.get('canvas')
    canvas.zoom('fit-viewport')
  } catch (error) {
    console.error('流程图加载失败:', error)
  }
}

// 加载详情
const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const data = await getLeaveDetail(id)
    detail.value = data

    // 加载流程图
    if (data.processXml) {
      await initDiagram(data.processXml)
    }
  } catch (error) {
    console.error('加载详情失败:', error)
    // 使用模拟数据
    detail.value = {
      applicant: '张三',
      applyTime: '2024-11-04 10:00:00',
      type: 'sick',
      status: 'approved',
      startTime: '2024-11-06 09:00:00',
      endTime: '2024-11-07 18:00:00',
      days: 2,
      reason: '感冒发烧，需要休息',
      history: [
        {
          type: 'success',
          title: '申请已提交',
          content: '张三提交了请假申请',
          time: '2024-11-04 10:00:00'
        },
        {
          type: 'success',
          title: '审批通过',
          content: '李经理批准了该请假申请',
          time: '2024-11-04 14:30:00'
        }
      ]
    }

    // 使用示例 BPMN XML（简单的流程图）
    const sampleXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:omgdc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:omgdi="http://www.omg.org/spec/DD/20100524/DI"
             id="sid-38422fae-e03e-43a3-bef4-bd33b32041b2">
  <process id="Process_1" isExecutable="false">
    <startEvent id="StartEvent_1"/>
    <userTask id="Task_1" name="申请提交"/>
    <userTask id="Task_2" name="经理审批"/>
    <endEvent id="EndEvent_1"/>
    <sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="Task_1"/>
    <sequenceFlow id="Flow_2" sourceRef="Task_1" targetRef="Task_2"/>
    <sequenceFlow id="Flow_3" sourceRef="Task_2" targetRef="EndEvent_1"/>
  </process>
</definitions>`
    await initDiagram(sampleXml)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDetail()
})
</script>

<style scoped>
.leave-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  color: #007AFF;
}

.detail-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.info-card,
.history-card,
.diagram-card {
  padding: 24px;
}

.info-card h3,
.history-card h3,
.diagram-card h3 {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 20px;
  color: rgba(0, 0, 0, 0.9);
}

.diagram-container {
  width: 100%;
  height: 500px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .info-card h3,
  .history-card h3,
  .diagram-card h3 {
    color: rgba(255, 255, 255, 0.9);
  }

  .diagram-container {
    border-color: rgba(255, 255, 255, 0.15);
  }
}
</style>

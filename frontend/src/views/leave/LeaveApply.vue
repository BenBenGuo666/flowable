<template>
  <div class="leave-apply-page">
    <div class="page-header">
      <h1>发起请假申请</h1>
      <p>填写以下信息提交请假申请</p>
    </div>

    <div class="apply-form apple-card">
      <n-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-placement="left"
        label-width="100"
        require-mark-placement="right-hanging"
      >
        <n-form-item label="请假类型" path="type">
          <n-select
            v-model:value="formData.type"
            placeholder="请选择请假类型"
            :options="leaveTypeOptions"
          />
        </n-form-item>

        <n-form-item label="开始时间" path="startTime">
          <n-date-picker
            v-model:value="formData.startTime"
            type="datetime"
            placeholder="选择开始时间"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="结束时间" path="endTime">
          <n-date-picker
            v-model:value="formData.endTime"
            type="datetime"
            placeholder="选择结束时间"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="请假天数" path="days">
          <n-input-number
            v-model:value="formData.days"
            placeholder="请假天数"
            :min="0.5"
            :step="0.5"
            style="width: 100%"
          />
        </n-form-item>

        <n-form-item label="请假原因" path="reason">
          <n-input
            v-model:value="formData.reason"
            type="textarea"
            placeholder="请输入请假原因"
            :rows="5"
            maxlength="500"
            show-count
          />
        </n-form-item>

        <n-form-item>
          <n-space>
            <n-button
              type="primary"
              :loading="submitting"
              @click="handleSubmit"
            >
              提交申请
            </n-button>
            <n-button @click="handleReset">
              重置
            </n-button>
          </n-space>
        </n-form-item>
      </n-form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMessage } from 'naive-ui'
import { applyLeave } from '../../api/leave'

const router = useRouter()
const message = useMessage()
const formRef = ref(null)
const submitting = ref(false)

// 表单数据
const formData = ref({
  type: null,
  startTime: null,
  endTime: null,
  days: 1,
  reason: ''
})

// 请假类型选项
const leaveTypeOptions = [
  { label: '事假', value: 'personal' },
  { label: '病假', value: 'sick' },
  { label: '年假', value: 'annual' },
  { label: '调休', value: 'compensatory' }
]

// 表单验证规则
const rules = {
  type: {
    required: true,
    message: '请选择请假类型',
    trigger: 'change'
  },
  startTime: {
    required: true,
    type: 'number',
    message: '请选择开始时间',
    trigger: 'change'
  },
  endTime: {
    required: true,
    type: 'number',
    message: '请选择结束时间',
    trigger: 'change'
  },
  days: {
    required: true,
    type: 'number',
    message: '请输入请假天数',
    trigger: 'blur'
  },
  reason: {
    required: true,
    message: '请输入请假原因',
    trigger: 'blur'
  }
}

// 提交申请
const handleSubmit = () => {
  formRef.value?.validate(async (errors) => {
    if (!errors) {
      submitting.value = true
      try {
        await applyLeave(formData.value)
        message.success('请假申请提交成功')
        router.push('/leave/list')
      } catch (error) {
        message.error('提交失败: ' + (error.message || '未知错误'))
      } finally {
        submitting.value = false
      }
    }
  })
}

// 重置表单
const handleReset = () => {
  formRef.value?.restoreValidation()
  formData.value = {
    type: null,
    startTime: null,
    endTime: null,
    days: 1,
    reason: ''
  }
}
</script>

<style scoped>
.leave-apply-page {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  color: #007AFF;
}

.page-header p {
  font-size: 14px;
  color: rgba(0, 0, 0, 0.5);
}

.apply-form {
  padding: 40px;
}

/* 深色模式 */
@media (prefers-color-scheme: dark) {
  .page-header p {
    color: rgba(255, 255, 255, 0.5);
  }
}
</style>

/**
 * 部署请假流程Demo脚本
 */
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// API基础URL
const API_BASE_URL = 'http://localhost:8080'

// 读取BPMN文件
const bpmnFilePath = path.join(__dirname, 'demo-leave-process.bpmn')
const bpmnXml = fs.readFileSync(bpmnFilePath, 'utf-8')

console.log('📄 已读取BPMN文件')
console.log('=' .repeat(60))

// 部署流程定义
async function deployProcess() {
  try {
    console.log('\n🚀 开始部署流程定义...')

    const response = await fetch(`${API_BASE_URL}/api/process-definitions`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        name: '请假流程Demo',
        key: 'leave_request_demo',
        category: 'demo',
        bpmnXml: bpmnXml
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      console.log('✅ 流程定义部署成功!')
      console.log('   部署ID:', result.data)
      return result.data
    } else {
      console.error('❌ 部署失败:', result.message)
      return null
    }
  } catch (error) {
    console.error('❌ 部署出错:', error.message)
    return null
  }
}

// 获取流程定义列表
async function getProcessDefinitions() {
  try {
    console.log('\n📋 查询流程定义列表...')

    const response = await fetch(`${API_BASE_URL}/api/process-definitions`)
    const result = await response.json()

    if (result.code === 200) {
      console.log('✅ 查询成功')
      console.log('\n当前已部署的流程定义:')
      console.log('-'.repeat(60))

      result.data.forEach((def, index) => {
        console.log(`\n${index + 1}. ${def.name}`)
        console.log(`   ID: ${def.id}`)
        console.log(`   Key: ${def.key}`)
        console.log(`   版本: v${def.version}`)
        console.log(`   分类: ${def.category}`)
        console.log(`   状态: ${def.suspended ? '已挂起' : '激活'}`)
        console.log(`   部署时间: ${def.deploymentTime}`)
      })

      return result.data
    } else {
      console.error('❌ 查询失败:', result.message)
      return []
    }
  } catch (error) {
    console.error('❌ 查询出错:', error.message)
    return []
  }
}

// 启动流程实例
async function startProcessInstance(processDefinitionKey) {
  try {
    console.log('\n🎯 启动流程实例...')

    const response = await fetch(`${API_BASE_URL}/api/process-instances`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        processDefinitionKey: processDefinitionKey,
        businessKey: `LEAVE_${Date.now()}`,
        variables: {
          applicant: 'zhangsan',
          reason: '因个人事务需要请假3天',
          startDate: '2025-11-10',
          endDate: '2025-11-12',
          days: 3
        }
      })
    })

    const result = await response.json()

    if (result.code === 200) {
      console.log('✅ 流程实例启动成功!')
      console.log('   实例ID:', result.data.id)
      console.log('   业务Key:', result.data.businessKey)
      console.log('   流程定义ID:', result.data.processDefinitionId)
      console.log('   开始时间:', result.data.startTime)
      return result.data
    } else {
      console.error('❌ 启动失败:', result.message)
      return null
    }
  } catch (error) {
    console.error('❌ 启动出错:', error.message)
    return null
  }
}

// 查询待办任务
async function getPendingTasks(assignee) {
  try {
    console.log(`\n📝 查询用户 [${assignee}] 的待办任务...`)

    const response = await fetch(`${API_BASE_URL}/api/tasks/pending?assignee=${assignee}`)
    const result = await response.json()

    if (result.code === 200) {
      console.log(`✅ 查询成功，共 ${result.data.length} 个待办任务`)

      if (result.data.length > 0) {
        console.log('\n待办任务列表:')
        console.log('-'.repeat(60))

        result.data.forEach((task, index) => {
          console.log(`\n${index + 1}. ${task.name}`)
          console.log(`   任务ID: ${task.id}`)
          console.log(`   流程实例ID: ${task.processInstanceId}`)
          console.log(`   负责人: ${task.assignee || '待分配'}`)
          console.log(`   创建时间: ${task.createTime}`)
          console.log(`   优先级: ${task.priority}`)
        })
      }

      return result.data
    } else {
      console.error('❌ 查询失败:', result.message)
      return []
    }
  } catch (error) {
    console.error('❌ 查询出错:', error.message)
    return []
  }
}

// 主函数
async function main() {
  console.log('\n')
  console.log('═'.repeat(60))
  console.log('   🎬 Flowable 请假流程Demo演示')
  console.log('═'.repeat(60))

  // 步骤1: 部署流程定义
  const deploymentId = await deployProcess()
  if (!deploymentId) {
    console.log('\n❌ 部署失败，终止演示')
    return
  }

  // 等待1秒
  await new Promise(resolve => setTimeout(resolve, 1000))

  // 步骤2: 查询流程定义
  const definitions = await getProcessDefinitions()

  // 等待1秒
  await new Promise(resolve => setTimeout(resolve, 1000))

  // 步骤3: 启动流程实例
  const instance = await startProcessInstance('leave_request_demo')
  if (!instance) {
    console.log('\n❌ 启动实例失败，终止演示')
    return
  }

  // 等待1秒
  await new Promise(resolve => setTimeout(resolve, 1000))

  // 步骤4: 查询待办任务
  await getPendingTasks('zhangsan')

  console.log('\n')
  console.log('═'.repeat(60))
  console.log('   ✨ Demo演示完成!')
  console.log('═'.repeat(60))
  console.log('\n💡 提示:')
  console.log('   1. 流程已成功部署到数据库')
  console.log('   2. 流程实例已启动')
  console.log('   3. 可以在前端页面查看流程定义和任务')
  console.log('   4. 访问 http://localhost:3000 进入系统')
  console.log('\n')
}

// 运行
main().catch(console.error)

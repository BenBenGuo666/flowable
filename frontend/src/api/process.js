import request from './request'

// 流程定义相关API

/**
 * 获取流程定义列表
 */
export function getProcessDefinitions() {
  return request({
    url: '/api/process-definition/list',
    method: 'get'
  })
}

/**
 * 根据ID获取流程定义
 */
export function getProcessDefinitionById(id) {
  return request({
    url: `/api/process-definition/${id}`,
    method: 'get'
  })
}

/**
 * 获取流程定义的XML
 */
export function getProcessDefinitionXml(id) {
  return request({
    url: `/api/process-definition/${id}/xml`,
    method: 'get'
  })
}

/**
 * 部署流程定义
 */
export function deployProcessDefinition(data) {
  return request({
    url: '/api/process-definition/deploy',
    method: 'post',
    data
  })
}

/**
 * 上传BPMN文件
 */
export function uploadBpmnFile(formData) {
  return request({
    url: '/api/process-definition/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除流程定义
 */
export function deleteProcessDefinition(deploymentId, cascade = false) {
  return request({
    url: `/api/process-definition/${deploymentId}`,
    method: 'delete',
    params: { cascade }
  })
}

/**
 * 激活流程定义
 */
export function activateProcessDefinition(id) {
  return request({
    url: `/api/process-definition/${id}/activate`,
    method: 'put'
  })
}

/**
 * 挂起流程定义
 */
export function suspendProcessDefinition(id) {
  return request({
    url: `/api/process-definition/${id}/suspend`,
    method: 'put'
  })
}

// 流程实例相关API

/**
 * 获取运行中的流程实例
 */
export function getRunningProcessInstances() {
  return request({
    url: '/api/process-instance/running',
    method: 'get'
  })
}

/**
 * 获取所有流程实例
 */
export function getAllProcessInstances() {
  return request({
    url: '/api/process-instance/all',
    method: 'get'
  })
}

/**
 * 根据ID获取流程实例
 */
export function getProcessInstanceById(id) {
  return request({
    url: `/api/process-instance/${id}`,
    method: 'get'
  })
}

/**
 * 挂起流程实例
 */
export function suspendProcessInstance(id) {
  return request({
    url: `/api/process-instance/${id}/suspend`,
    method: 'put'
  })
}

/**
 * 激活流程实例
 */
export function activateProcessInstance(id) {
  return request({
    url: `/api/process-instance/${id}/activate`,
    method: 'put'
  })
}

/**
 * 删除流程实例
 */
export function deleteProcessInstance(id, reason) {
  return request({
    url: `/api/process-instance/${id}`,
    method: 'delete',
    params: { reason }
  })
}

// 流程模板相关API

/**
 * 获取所有流程模板
 */
export function getAllTemplates() {
  return request({
    url: '/api/process-template/list',
    method: 'get'
  })
}

/**
 * 根据ID获取流程模板
 */
export function getTemplateById(id) {
  return request({
    url: `/api/process-template/${id}`,
    method: 'get'
  })
}

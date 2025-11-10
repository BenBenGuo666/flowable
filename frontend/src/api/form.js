import request from './request'

/**
 * 表单定义 API
 */

// 创建表单定义
export function createFormDefinition(data) {
  return request({
    url: '/api/form-definition',
    method: 'post',
    data
  })
}

// 更新表单定义
export function updateFormDefinition(id, data) {
  return request({
    url: `/api/form-definition/${id}`,
    method: 'put',
    data
  })
}

// 发布表单定义
export function publishFormDefinition(id) {
  return request({
    url: `/api/form-definition/${id}/publish`,
    method: 'post'
  })
}

// 创建新版本
export function createNewVersion(formKey) {
  return request({
    url: `/api/form-definition/${formKey}/new-version`,
    method: 'post'
  })
}

// 删除表单定义
export function deleteFormDefinition(id) {
  return request({
    url: `/api/form-definition/${id}`,
    method: 'delete'
  })
}

// 根据ID查询表单定义
export function getFormDefinitionById(id) {
  return request({
    url: `/api/form-definition/${id}`,
    method: 'get'
  })
}

// 根据formKey查询最新版本
export function getLatestFormByKey(formKey) {
  return request({
    url: `/api/form-definition/by-key/${formKey}`,
    method: 'get'
  })
}

// 查询表单所有版本
export function getAllVersions(formKey) {
  return request({
    url: `/api/form-definition/versions/${formKey}`,
    method: 'get'
  })
}

// 分页查询表单定义列表
export function getFormDefinitionList(params) {
  return request({
    url: '/api/form-definition/list',
    method: 'get',
    params
  })
}

// 查询已发布的表单列表
export function getPublishedForms(params) {
  return request({
    url: '/api/form-definition/published',
    method: 'get',
    params
  })
}

/**
 * 表单数据 API
 */

// 提交表单数据
export function submitFormData(data) {
  return request({
    url: '/api/form-data/submit',
    method: 'post',
    data
  })
}

// 更新表单数据
export function updateFormData(id, data) {
  return request({
    url: `/api/form-data/${id}`,
    method: 'put',
    data
  })
}

// 根据ID查询表单数据
export function getFormDataById(id) {
  return request({
    url: `/api/form-data/${id}`,
    method: 'get'
  })
}

// 根据流程实例ID查询表单数据
export function getFormDataByProcessInstance(processInstanceId) {
  return request({
    url: `/api/form-data/by-process/${processInstanceId}`,
    method: 'get'
  })
}

// 根据任务ID查询表单数据
export function getFormDataByTaskId(taskId) {
  return request({
    url: `/api/form-data/by-task/${taskId}`,
    method: 'get'
  })
}

// 根据业务Key查询表单数据
export function getFormDataByBusinessKey(businessKey) {
  return request({
    url: `/api/form-data/by-business/${businessKey}`,
    method: 'get'
  })
}

// 根据表单Key查询数据
export function getFormDataByFormKey(formKey) {
  return request({
    url: `/api/form-data/by-form-key/${formKey}`,
    method: 'get'
  })
}

// 删除表单数据
export function deleteFormData(id) {
  return request({
    url: `/api/form-data/${id}`,
    method: 'delete'
  })
}

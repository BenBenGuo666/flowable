import request from './request'

// 任务相关API

/**
 * 获取我的待办任务
 */
export function getMyPendingTasks(userId = 'user1') {
  return request({
    url: '/task/pending',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取我的已办任务
 */
export function getMyCompletedTasks(userId = 'user1') {
  return request({
    url: '/task/completed',
    method: 'get',
    params: { userId }
  })
}

/**
 * 获取候选任务（可认领）
 */
export function getCandidateTasks(userId = 'user1') {
  return request({
    url: '/task/candidate',
    method: 'get',
    params: { userId }
  })
}

/**
 * 根据ID获取任务详情
 */
export function getTaskById(taskId) {
  return request({
    url: `/task/${taskId}`,
    method: 'get'
  })
}

/**
 * 完成任务（审批通过）
 */
export function completeTask(taskId, data) {
  return request({
    url: `/task/${taskId}/complete`,
    method: 'post',
    data
  })
}

/**
 * 驳回任务
 */
export function rejectTask(taskId, data) {
  return request({
    url: `/task/${taskId}/reject`,
    method: 'post',
    data
  })
}

/**
 * 转办任务
 */
export function transferTask(taskId, targetUserId) {
  return request({
    url: `/task/${taskId}/transfer`,
    method: 'post',
    params: { targetUserId }
  })
}

/**
 * 委派任务
 */
export function delegateTask(taskId, delegateUserId) {
  return request({
    url: `/task/${taskId}/delegate`,
    method: 'post',
    params: { delegateUserId }
  })
}

/**
 * 认领任务
 */
export function claimTask(taskId, userId) {
  return request({
    url: `/task/${taskId}/claim`,
    method: 'post',
    params: { userId }
  })
}

/**
 * 获取任务变量
 */
export function getTaskVariables(taskId) {
  return request({
    url: `/task/${taskId}/variables`,
    method: 'get'
  })
}

/**
 * 设置任务变量
 */
export function setTaskVariables(taskId, variables) {
  return request({
    url: `/task/${taskId}/variables`,
    method: 'put',
    data: variables
  })
}

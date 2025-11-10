import request from './request'

// 请假相关的 API

/**
 * 发起请假申请
 * @param {Object} data - 请假数据
 * @returns {Promise}
 */
export function applyLeave(data) {
  return request({
    url: '/api/leave/apply',
    method: 'post',
    data
  })
}

/**
 * 获取我的请假列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMyLeaveList(params) {
  return request({
    url: '/api/leave/my-list',
    method: 'get',
    params
  })
}

/**
 * 获取待审批的请假列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getPendingLeaveList(params) {
  return request({
    url: '/api/leave/pending-list',
    method: 'get',
    params
  })
}

/**
 * 审批请假申请
 * @param {String} taskId - 任务ID
 * @param {Object} data - 审批数据
 * @returns {Promise}
 */
export function approveLeave(taskId, data) {
  return request({
    url: `/api/leave/approve/${taskId}`,
    method: 'post',
    data
  })
}

/**
 * 拒绝请假申请
 * @param {String} taskId - 任务ID
 * @param {Object} data - 拒绝数据
 * @returns {Promise}
 */
export function rejectLeave(taskId, data) {
  return request({
    url: `/api/leave/reject/${taskId}`,
    method: 'post',
    data
  })
}

/**
 * 获取请假详情
 * @param {String} id - 请假ID
 * @returns {Promise}
 */
export function getLeaveDetail(id) {
  return request({
    url: `/api/leave/detail/${id}`,
    method: 'get'
  })
}

/**
 * 获取流程图
 * @param {String} processInstanceId - 流程实例ID
 * @returns {Promise}
 */
export function getProcessDiagram(processInstanceId) {
  return request({
    url: `/api/leave/process-diagram/${processInstanceId}`,
    method: 'get',
    responseType: 'blob' // 返回图片
  })
}

/**
 * 获取请假统计数据
 * @returns {Promise}
 */
export function getLeaveStatistics() {
  return request({
    url: '/api/leave/statistics',
    method: 'get'
  })
}

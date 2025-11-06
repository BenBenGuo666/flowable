import request from './request'

/**
 * 获取权限列表
 */
export function getPermissionList(params) {
  return request({
    url: '/permission/list',
    method: 'get',
    params
  })
}

/**
 * 获取权限树
 */
export function getPermissionTree() {
  return request({
    url: '/permission/tree',
    method: 'get'
  })
}

/**
 * 获取权限详情
 */
export function getPermissionById(id) {
  return request({
    url: `/permission/${id}`,
    method: 'get'
  })
}

/**
 * 创建权限
 */
export function createPermission(data) {
  return request({
    url: '/permission',
    method: 'post',
    data
  })
}

/**
 * 更新权限
 */
export function updatePermission(id, data) {
  return request({
    url: `/permission/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除权限
 */
export function deletePermission(id) {
  return request({
    url: `/permission/${id}`,
    method: 'delete'
  })
}

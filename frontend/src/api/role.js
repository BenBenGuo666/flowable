import request from './request'

/**
 * 获取角色列表
 */
export function getRoleList(params) {
  return request({
    url: '/api/role/list',
    method: 'get',
    params
  })
}

/**
 * 获取角色详情
 */
export function getRoleById(id) {
  return request({
    url: `/api/role/${id}`,
    method: 'get'
  })
}

/**
 * 创建角色
 */
export function createRole(data) {
  return request({
    url: '/api/role',
    method: 'post',
    data
  })
}

/**
 * 更新角色
 */
export function updateRole(id, data) {
  return request({
    url: `/api/role/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除角色
 */
export function deleteRole(id) {
  return request({
    url: `/api/role/${id}`,
    method: 'delete'
  })
}

/**
 * 为角色分配权限
 */
export function assignRolePermissions(id, permissionIds) {
  return request({
    url: `/api/role/${id}/permissions`,
    method: 'post',
    data: permissionIds
  })
}

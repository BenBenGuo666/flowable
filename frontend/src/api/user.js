import request from './request'

/**
 * 获取用户列表
 */
export function getUserList(params) {
  return request({
    url: '/user/list',
    method: 'get',
    params
  })
}

/**
 * 获取用户详情
 */
export function getUserById(id) {
  return request({
    url: `/user/${id}`,
    method: 'get'
  })
}

/**
 * 创建用户
 */
export function createUser(data) {
  return request({
    url: '/user',
    method: 'post',
    data
  })
}

/**
 * 更新用户
 */
export function updateUser(id, data) {
  return request({
    url: `/user/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 */
export function deleteUser(id) {
  return request({
    url: `/user/${id}`,
    method: 'delete'
  })
}

/**
 * 为用户分配角色
 */
export function assignUserRoles(id, roleIds) {
  return request({
    url: `/user/${id}/roles`,
    method: 'post',
    data: roleIds
  })
}

import request from '@/utils/request'

export function getUsers(params) {
  return request.get('/admin/users', { params })
}

export function getUserDetail(id) {
  return request.get(`/admin/users/${id}`)
}

export function createUser(data) {
  return request.post('/admin/users', data)
}

export function updateUser(id, data) {
  return request.put(`/admin/users/${id}`, data)
}

export function deleteUser(id) {
  return request.delete(`/admin/users/${id}`)
}

export function resetPassword(id) {
  return request.put(`/admin/users/${id}/reset-password`)
}

export function updateUserStatus(id, status) {
  return request.put(`/admin/users/${id}/status`, null, { params: { status } })
}

export function assignRoles(id, roleIds) {
  return request.put(`/admin/users/${id}/roles`, roleIds)
}

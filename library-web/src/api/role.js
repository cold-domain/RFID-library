import request from '@/utils/request'

export function getRoles() {
  return request.get('/admin/roles')
}

export function getRoleDetail(id) {
  return request.get(`/admin/roles/${id}`)
}

export function createRole(data) {
  return request.post('/admin/roles', data)
}

export function updateRole(id, data) {
  return request.put(`/admin/roles/${id}`, data)
}

export function deleteRole(id) {
  return request.delete(`/admin/roles/${id}`)
}

export function assignPermissions(id, permissionIds) {
  return request.put(`/admin/roles/${id}/permissions`, permissionIds)
}

import request from '@/utils/request'

export function getPermissionTree() {
  return request.get('/admin/permissions/tree')
}

export function getPermissions() {
  return request.get('/admin/permissions')
}

export function getPermissionsByRole(roleId) {
  return request.get(`/admin/permissions/role/${roleId}`)
}

export function createPermission(data) {
  return request.post('/admin/permissions', data)
}

export function updatePermission(id, data) {
  return request.put(`/admin/permissions/${id}`, data)
}

export function deletePermission(id) {
  return request.delete(`/admin/permissions/${id}`)
}

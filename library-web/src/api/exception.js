import request from '@/utils/request'

export function getExceptions(params) {
  return request.get('/admin/exceptions', { params })
}

export function getExceptionDetail(id) {
  return request.get(`/admin/exceptions/${id}`)
}

export function resolveException(id, resolveNote) {
  return request.post(`/admin/exceptions/${id}/resolve`, null, { params: { resolveNote } })
}

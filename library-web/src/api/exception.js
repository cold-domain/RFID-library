import request from '@/utils/request'

export function getExceptions(params) {
  return request.get('/admin/exceptions', { params })
}

export function getExceptionOverview() {
  return request.get('/admin/exceptions/overview')
}

export function getExceptionDetail(id) {
  return request.get(`/admin/exceptions/${id}`)
}

export function resolveException(id, resolveNote) {
  return request.post(`/admin/exceptions/${id}/resolve`, null, { params: { resolveNote } })
}

export function ignoreException(id, resolveNote) {
  return request.post(`/admin/exceptions/${id}/ignore`, null, { params: { resolveNote } })
}

import request from '@/utils/request'

export function getAuditLogs(params) {
  return request.get('/admin/audit-logs', { params })
}

export function getAuditLogDetail(id) {
  return request.get(`/admin/audit-logs/${id}`)
}

export function cleanExpiredLogs(retentionDays) {
  return request.post('/admin/audit-logs/clean', null, { params: { retentionDays } })
}

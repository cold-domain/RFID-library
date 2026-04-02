import request from '@/utils/request'

export function getFines(params) {
  return request.get('/librarian/fines', { params })
}

export function createFine(data) {
  return request.post('/librarian/fines', null, { params: data })
}

export function payFine(id, amount) {
  return request.post(`/librarian/fines/${id}/pay`, null, { params: { amount } })
}

export function waiveFine(id, reason) {
  return request.post(`/librarian/fines/${id}/waive`, null, { params: { reason } })
}

export function getUnpaidTotal(userId) {
  return request.get(`/librarian/fines/unpaid/${userId}`)
}

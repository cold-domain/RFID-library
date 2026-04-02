import request from '@/utils/request'

export function getReservations(params) {
  return request.get('/librarian/reservations', { params })
}

export function confirmPickup(id) {
  return request.post(`/librarian/reservations/${id}/pickup`)
}

export function checkExpired() {
  return request.post('/librarian/reservations/check-expired')
}

import request from '@/utils/request'

export function getProfile() {
  return request.get('/reader/profile')
}

export function updateProfile(data) {
  return request.put('/reader/profile', data)
}

export function changePassword(data) {
  return request.put('/reader/password', data)
}

export function getMyBorrows(params) {
  return request.get('/reader/borrows', { params })
}

export function renewBorrow(id) {
  return request.post(`/reader/borrows/${id}/renew`)
}

export function reserveBook(bookId) {
  return request.post('/reader/reservations', null, { params: { bookId } })
}

export function getMyReservations(params) {
  return request.get('/reader/reservations', { params })
}

export function cancelReservation(id) {
  return request.post(`/reader/reservations/${id}/cancel`)
}

export function getMyFines(params) {
  return request.get('/reader/fines', { params })
}

export function payMyFine(id, amount) {
  return request.post(`/reader/fines/${id}/pay`, null, { params: { amount } })
}

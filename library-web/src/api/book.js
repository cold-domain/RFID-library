import request from '@/utils/request'

export function getBooks(params) {
  return request.get('/librarian/books', { params })
}

export function getBookDetail(id) {
  return request.get(`/librarian/books/${id}`)
}

export function createBook(data) {
  return request.post('/librarian/books', data)
}

export function updateBook(id, data) {
  return request.put(`/librarian/books/${id}`, data)
}

export function deleteBook(id) {
  return request.delete(`/librarian/books/${id}`)
}

export function bindRfid(id, rfidTag) {
  return request.post(`/librarian/books/${id}/rfid/bind`, null, { params: { rfidTag } })
}

export function unbindRfid(id) {
  return request.post(`/librarian/books/${id}/rfid/unbind`)
}

export function updateBookStatus(id, status) {
  return request.put(`/librarian/books/${id}/status`, null, { params: { status } })
}

export function getRfidHistory(id) {
  return request.get(`/librarian/books/${id}/rfid/history`)
}

export function getStatusHistory(id) {
  return request.get(`/librarian/books/${id}/status/history`)
}

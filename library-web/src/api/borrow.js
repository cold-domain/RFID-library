import request from '@/utils/request'

export function getBorrowRecords(params) {
  return request.get('/librarian/borrows', { params })
}

export function borrowBook(data) {
  return request.post('/librarian/borrows/borrow', null, { params: data })
}

export function returnBook(id) {
  return request.post(`/librarian/borrows/${id}/return`)
}

export function renewBorrow(id) {
  return request.post(`/librarian/borrows/${id}/renew`)
}

export function checkOverdue() {
  return request.post('/librarian/borrows/check-overdue')
}

export function searchBooksForBorrow(params) {
  return request.get('/librarian/borrows/search-books', { params })
}

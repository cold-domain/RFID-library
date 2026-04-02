import request from '@/utils/request'

export function getPublicBooks(params) {
  return request.get('/books/public', { params })
}

export function getPublicBookDetail(id) {
  return request.get(`/books/public/${id}`)
}

export function getPublicCategories() {
  return request.get('/books/public/categories')
}

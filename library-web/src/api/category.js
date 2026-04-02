import request from '@/utils/request'

export function getCategoryTree() {
  return request.get('/librarian/categories/tree')
}

export function getCategories() {
  return request.get('/librarian/categories')
}

export function createCategory(data) {
  return request.post('/librarian/categories', data)
}

export function updateCategory(id, data) {
  return request.put(`/librarian/categories/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/librarian/categories/${id}`)
}

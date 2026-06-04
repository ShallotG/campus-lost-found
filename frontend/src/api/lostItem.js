import request from './index'

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/lost-items/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

export function createLostItem(data) {
  return request.post('/lost-items', data)
}

export function getLostItemList(params) {
  return request.get('/lost-items', { params })
}

export function getLostItemById(id) {
  return request.get(`/lost-items/${id}`)
}

export function updateLostItem(id, data) {
  return request.put(`/lost-items/${id}`, data)
}

export function deleteLostItem(id) {
  return request.delete(`/lost-items/${id}`)
}

import request from './index'

export function createFoundItem(data) {
  return request.post('/found-items', data)
}

export function getFoundItemDetail(id) {
  return request.get(`/found-items/${id}`)
}

export function getMyFoundItems(params) {
  return request.get('/found-items/my', { params })
}

export function getFoundItemList(params) {
  return request.get('/found-items', { params })
}

export function closeFoundItem(id) {
  return request.put(`/found-items/${id}/close`)
}

export function deleteFoundItem(id) {
  return request.delete(`/found-items/${id}`)
}

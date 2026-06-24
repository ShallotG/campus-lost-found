import request from './index'

export function getBrowseItems(params) {
  return request.get('/browse/items', { params })
}

export function getBrowseCategories() {
  return request.get('/browse/categories')
}
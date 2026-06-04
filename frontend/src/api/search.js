import request from './index'

export function semanticSearch(data) {
  return request.post('/search', data)
}

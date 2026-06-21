import request from './index'

export function confirmMatch(data) {
  return request.post('/match-confirm', data)
}

export function getMyConfirms(params) {
  return request.get('/match-confirm/my', { params })
}

import request from './index'

export function claimItem(data) {
  return request.post('/claims', data)
}

export function getClaimList(params) {
  return request.get('/claims', { params })
}

import request from './index'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function refreshToken(data) {
  return request.post('/auth/refresh', data)
}

export function logout() {
  return request.post('/auth/logout')
}

export function getUserInfo() {
  return request.get('/users/me')
}

export function updateProfile(data) {
  return request.put('/users/me', data)
}

export function changePassword(data) {
  return request.put('/users/me/password', data)
}
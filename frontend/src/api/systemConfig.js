import request from './index'

export function getConfigList() {
  return request.get('/configs')
}

export function updateConfig(key, configValue) {
  return request.put(`/configs/${key}`, { configValue })
}

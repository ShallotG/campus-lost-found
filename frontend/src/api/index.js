import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

const instance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器 — 自动添加Token
instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器 — 统一处理错误
instance.interceptors.response.use(
  (response) => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    }
    ElMessage.error(message || '操作失败')
    return Promise.reject(new Error(message))
  },
  async (error) => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          // Token过期，尝试刷新
          const refreshed = await tryRefreshToken()
          if (refreshed) {
            // 重试原请求
            error.config.headers.Authorization = `Bearer ${localStorage.getItem('accessToken')}`
            return instance(error.config)
          }
          // 刷新失败，跳转登录
          localStorage.clear()
          router.push('/user/login')
          ElMessage.error('登录已过期，请重新登录')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 413:
          ElMessage.error('文件大小超过限制')
          break
        default:
          ElMessage.error(data?.message || '服务器错误')
      }
    } else {
      ElMessage.error('网络连接异常')
    }
    return Promise.reject(error)
  }
)

// Token刷新逻辑
let isRefreshing = false
async function tryRefreshToken() {
  if (isRefreshing) return false
  isRefreshing = true
  try {
    const refreshToken = localStorage.getItem('refreshToken')
    if (!refreshToken) return false
    const res = await axios.post('/api/auth/refresh', { refreshToken })
    if (res.data.code === 200) {
      localStorage.setItem('accessToken', res.data.data.accessToken)
      localStorage.setItem('refreshToken', res.data.data.refreshToken)
      return true
    }
  } catch (e) {
    // ignore
  } finally {
    isRefreshing = false
  }
  return false
}

export default instance

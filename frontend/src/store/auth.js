import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(sessionStorage.getItem('accessToken') || '')
  const refreshToken = ref(sessionStorage.getItem('refreshToken') || '')
  const userInfo = ref(JSON.parse(sessionStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!accessToken.value)
  const userRole = computed(() => userInfo.value?.role || '')
  const userName = computed(() => userInfo.value?.realName || userInfo.value?.username || '')

  function setTokens(access, refresh) {
    accessToken.value = access
    refreshToken.value = refresh
    sessionStorage.setItem('accessToken', access)
    sessionStorage.setItem('refreshToken', refresh)
  }

  function setUserInfo(info) {
    userInfo.value = info
    sessionStorage.setItem('userInfo', JSON.stringify(info))
    sessionStorage.setItem('userRole', info.role)
  }

  function logout() {
    accessToken.value = ''
    refreshToken.value = ''
    userInfo.value = null
    sessionStorage.clear()
  }

  return {
    accessToken,
    refreshToken,
    userInfo,
    isLoggedIn,
    userRole,
    userName,
    setTokens,
    setUserInfo,
    logout,
  }
})

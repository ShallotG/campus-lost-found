import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useAppStore = defineStore('app', () => {
  const sidebarCollapsed = ref(false)
  const globalLoading = ref(false)
  const isDark = ref(localStorage.getItem('theme') === 'dark')

  watch(isDark, (val) => {
    localStorage.setItem('theme', val ? 'dark' : 'light')
    document.documentElement.setAttribute('data-theme', val ? 'dark' : 'light')
  }, { immediate: true })

  function toggleSidebar() { sidebarCollapsed.value = !sidebarCollapsed.value }
  function setLoading(loading) { globalLoading.value = loading }
  function toggleTheme() { isDark.value = !isDark.value }

  return { sidebarCollapsed, globalLoading, isDark, toggleSidebar, setLoading, toggleTheme }
})

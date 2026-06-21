<template>
  <div class="admin-layout">
    <el-container>
      <!-- 侧边栏 -->
      <el-aside :width="sidebarCollapsed ? '64px' : '220px'" class="admin-aside">
        <div class="aside-logo">
          <span v-if="!sidebarCollapsed">失物招领管理</span>
          <span v-else>管理</span>
        </div>
        <el-menu
          :default-active="activeMenu"
          :collapse="sidebarCollapsed"
          :collapse-transition="false"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/admin/dashboard">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据看板</span>
          </el-menu-item>
          <el-menu-item index="/admin/items/register">
            <el-icon><Plus /></el-icon>
            <span>拾物登记</span>
          </el-menu-item>
          <el-menu-item index="/admin/items/list">
            <el-icon><List /></el-icon>
            <span>拾物列表</span>
          </el-menu-item>
          <template v-if="userRole === 'ROLE_ADMIN'">
            <el-menu-item index="/admin/users">
              <el-icon><User /></el-icon>
              <span>用户管理</span>
            </el-menu-item>
            <el-menu-item index="/admin/configs">
              <el-icon><Setting /></el-icon>
              <span>系统配置</span>
            </el-menu-item>
            <el-menu-item index="/admin/logs">
              <el-icon><Document /></el-icon>
              <span>系统日志</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <!-- 主内容区 -->
      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleSidebar">
              <Fold v-if="!sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
            <span class="header-title">{{ $route.meta.title }}</span>
          </div>
          <div class="header-right">
            <span class="user-info">{{ userName }} ({{ roleText }})</span>
            <el-button text @click="handleLogout">退出</el-button>
          </div>
        </el-header>
        <el-main class="admin-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useAppStore } from '@/store/app'
import { logout as logoutApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const userRole = computed(() => authStore.userRole)
const userName = computed(() => authStore.userName)

const roleText = computed(() => {
  const map = { ROLE_ADMIN: '管理员', ROLE_STAFF: '工作人员' }
  return map[authStore.userRole] || ''
})

function toggleSidebar() {
  appStore.toggleSidebar()
}

function handleLogout() {
  logoutApi().finally(() => {
    authStore.logout()
    router.push('/admin/login')
    ElMessage.success('已退出登录')
  })
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.admin-layout .el-container {
  height: 100%;
}
.admin-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.aside-logo {
  height: 64px;
  line-height: 64px;
  text-align: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  border-bottom: 1px solid rgba(255,255,255,0.1);
}
.el-menu {
  border-right: none;
}
.admin-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}
.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}
.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
}
.header-title {
  font-size: 16px;
  font-weight: 500;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.user-info {
  color: #606266;
  font-size: 14px;
}
.admin-main {
  background: #f5f7fa;
  min-height: calc(100vh - 60px);
}
</style>

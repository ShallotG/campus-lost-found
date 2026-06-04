<template>
  <div class="user-layout">
    <header class="user-header">
      <div class="header-content">
        <router-link to="/user/search" class="logo">
          <el-icon><Search /></el-icon>
          <span>校园失物招领</span>
        </router-link>
        <nav class="header-nav">
          <router-link to="/user/search">物品检索</router-link>
          <router-link to="/user/profile">个人中心</router-link>
          <el-button v-if="!authStore.isLoggedIn" type="primary" size="small" @click="$router.push('/user/login')">
            登录
          </el-button>
          <el-dropdown v-else @command="handleCommand">
            <span class="user-name">{{ authStore.userName }}</span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </nav>
      </div>
    </header>
    <main class="user-main">
      <router-view />
    </main>
    <footer class="user-footer">
      <p>© 2024 智能校园失物招领平台 | XX大学</p>
    </footer>
  </div>
</template>

<script setup>
import { useAuthStore } from '@/store/auth'
import { useRouter } from 'vue-router'
import { logout as logoutApi } from '@/api/auth'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const router = useRouter()

function handleCommand(command) {
  if (command === 'logout') {
    logoutApi().finally(() => {
      authStore.logout()
      router.push('/user/login')
      ElMessage.success('已退出登录')
    })
  } else if (command === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.user-header {
  background: #fff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.logo {
  font-size: 20px;
  font-weight: 600;
  color: #409eff;
  text-decoration: none;
  display: flex;
  align-items: center;
  gap: 8px;
}
.header-nav {
  display: flex;
  align-items: center;
  gap: 24px;
}
.header-nav a {
  color: #606266;
  text-decoration: none;
  font-size: 15px;
}
.header-nav a.router-link-active {
  color: #409eff;
}
.user-name {
  cursor: pointer;
  color: #409eff;
}
.user-main {
  flex: 1;
  max-width: 1200px;
  width: 100%;
  margin: 24px auto;
  padding: 0 20px;
}
.user-footer {
  text-align: center;
  padding: 20px;
  color: #909399;
  font-size: 13px;
}
</style>

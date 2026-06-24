<template>
  <div class="admin-layout">
    <el-container>
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
          <el-menu-item index="/admin/found-items">
            <el-icon><Document /></el-icon>
            <span>寻物启事</span>
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
              <el-icon><Tickets /></el-icon>
              <span>系统日志</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header class="admin-header">
          <div class="header-left">
            <el-icon class="collapse-btn" @click="toggleSidebar">
              <Fold v-if="!sidebarCollapsed" />
              <Expand v-else />
            </el-icon>
            <el-breadcrumb separator="/">
              <el-breadcrumb-item :to="{ path: '/admin/dashboard' }">首页</el-breadcrumb-item>
              <el-breadcrumb-item v-if="breadcrumbTitle">{{ breadcrumbTitle }}</el-breadcrumb-item>
            </el-breadcrumb>
          </div>
          <div class="header-right">
            <!-- 暗色模式切换 -->
            <el-icon :size="18" style="cursor:pointer" @click="appStore.toggleTheme()">
              <Sunny v-if="appStore.isDark" />
              <Moon v-else />
            </el-icon>
            <!-- 通知 -->
            <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0" class="header-badge">
              <el-icon :size="20" style="cursor:pointer" @click="showNotifications = true"><Bell /></el-icon>
            </el-badge>
            <span class="user-info">{{ userName }} ({{ roleText }})</span>
            <el-button text @click="handleLogout">退出</el-button>
          </div>
        </el-header>
        <el-main class="admin-main">
          <router-view v-slot="{ Component }">
            <transition name="fade" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>

    <!-- 通知抽屉 -->
    <el-drawer v-model="showNotifications" title="消息通知" size="380px">
      <div v-if="notifications.length === 0" style="text-align:center;padding:40px 0">
        <el-empty description="暂无通知" />
      </div>
      <div v-else>
        <div v-for="n in notifications" :key="n.id" class="drawer-notif" :class="{ unread: !n.isRead }">
          <div class="notif-title">{{ n.title }}</div>
          <div class="notif-content">{{ n.content }}</div>
          <div class="notif-time">{{ n.createTime }}</div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useAppStore } from '@/store/app'
import { logout as logoutApi } from '@/api/auth'
import { getNotifications, getUnreadCount } from '@/api/notification'
import { ElMessage } from 'element-plus'
import { Bell, Sunny, Moon } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => appStore.sidebarCollapsed)
const activeMenu = computed(() => route.path)
const userRole = computed(() => authStore.userRole)
const userName = computed(() => authStore.userName)
const breadcrumbTitle = computed(() => route.meta.title || '')
const roleText = computed(() => ({ ROLE_ADMIN: '管理员', ROLE_STAFF: '工作人员' }[authStore.userRole] || ''))

const unreadCount = ref(0)
const notifications = ref([])
const showNotifications = ref(false)

onMounted(async () => {
  try { const res = await getUnreadCount(); unreadCount.value = res.count || 0 } catch { /* */ }
})

function toggleSidebar() { appStore.toggleSidebar() }
function handleLogout() {
  logoutApi().finally(() => { authStore.logout(); router.push('/admin/login'); ElMessage.success('已退出登录') })
}
</script>

<style scoped>
.admin-layout { height: 100vh; }
.admin-layout .el-container { height: 100%; }
.admin-aside { background-color: #304156; transition: width 0.3s; overflow: hidden; }
.aside-logo { height: 64px; line-height: 64px; text-align: center; color: #fff; font-size: 16px; font-weight: 600; border-bottom: 1px solid rgba(255,255,255,0.1); }
.el-menu { border-right: none; }
.admin-header { background: var(--header-bg); display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-color); padding: 0 20px; }
.header-left { display: flex; align-items: center; gap: 16px; }
.collapse-btn { font-size: 20px; cursor: pointer; color: var(--text-secondary); }
.header-right { display: flex; align-items: center; gap: 16px; }
.header-badge { margin-top: 4px; }
.user-info { color: var(--text-secondary); font-size: 14px; }
.admin-main { background: var(--bg-primary); min-height: calc(100vh - 60px); }

.drawer-notif { padding: 12px; border-bottom: 1px solid var(--border-color); }
.drawer-notif.unread { background: #ecf5ff; }
.drawer-notif .notif-title { font-size: 14px; font-weight: 500; }
.drawer-notif .notif-content { font-size: 13px; color: var(--text-secondary); margin-top: 4px; }
.drawer-notif .notif-time { font-size: 12px; color: var(--text-muted); margin-top: 4px; }

.fade-enter-active, .fade-leave-active { transition: opacity 0.2s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
</style>

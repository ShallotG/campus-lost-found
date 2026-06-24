<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': sidebarCollapsed }">
    <!-- ==================== 顶栏 ==================== -->
    <header class="topbar">
      <div class="topbar-left">
        <el-icon class="collapse-btn" :size="20" @click="toggleSidebar">
          <Fold v-if="!sidebarCollapsed" />
          <Expand v-else />
        </el-icon>
        <router-link to="/user/browse" class="topbar-logo">
          <el-icon :size="24"><Search /></el-icon>
          <span class="logo-text">校园失物招领</span>
        </router-link>
      </div>

      <div class="topbar-center">
        <el-input
          v-model="quickSearch"
          placeholder="快速搜索失物..."
          clearable
          size="default"
          class="quick-search"
          @keyup.enter="goSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>

      <div class="topbar-right">
        <!-- 主题切换 -->
        <el-tooltip :content="appStore.isDark ? '切换亮色模式' : '切换暗色模式'" placement="bottom">
          <el-icon :size="18" class="topbar-icon" @click="appStore.toggleTheme()">
            <Sunny v-if="appStore.isDark" />
            <Moon v-else />
          </el-icon>
        </el-tooltip>

        <!-- 通知 -->
        <el-tooltip :content="unreadCount > 0 ? `${unreadCount} 条未读通知` : '消息通知'" placement="bottom">
          <el-badge :value="unreadCount" :max="99" :hidden="unreadCount === 0">
            <el-icon :size="18" class="topbar-icon" :class="{ 'has-notif': unreadCount > 0 }" @click="$router.push('/user/notifications')">
              <Bell />
            </el-icon>
          </el-badge>
        </el-tooltip>

        <template v-if="authStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleCommand">
            <span class="user-chip">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="user-chip-name">{{ authStore.userName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon> 个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon> 退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="small" @click="$router.push('/user/login')">登录</el-button>
        </template>
      </div>
    </header>

    <div class="layout-body">
      <!-- ==================== 左侧导航 ==================== -->
      <aside class="left-sidebar" :class="{ collapsed: sidebarCollapsed }">
        <nav class="side-nav">
          <router-link to="/user/browse" class="nav-item" :class="{ active: $route.name === 'Browse' }">
            <el-icon :size="20"><Grid /></el-icon>
            <span class="nav-label">浏览失物</span>
          </router-link>
          <router-link to="/user/search" class="nav-item" :class="{ active: $route.name === 'Search' }">
            <el-icon :size="20"><MagicStick /></el-icon>
            <span class="nav-label">智能检索</span>
          </router-link>
          <router-link to="/user/publish" class="nav-item" :class="{ active: $route.name === 'PublishLost' }">
            <el-icon :size="20"><EditPen /></el-icon>
            <span class="nav-label">发布寻物</span>
          </router-link>
          <router-link to="/user/my-posts" class="nav-item" :class="{ active: $route.name === 'MyPosts' }">
            <el-icon :size="20"><Tickets /></el-icon>
            <span class="nav-label">我的发布</span>
          </router-link>

          <div class="nav-divider"></div>

          <router-link to="/user/profile" class="nav-item" :class="{ active: $route.name === 'Profile' }">
            <el-icon :size="20"><User /></el-icon>
            <span class="nav-label">个人中心</span>
          </router-link>
          <router-link to="/user/notifications" class="nav-item" :class="{ active: $route.name === 'Notifications' }">
            <el-icon :size="20"><Bell /></el-icon>
            <span class="nav-label">消息通知</span>
            <span v-if="unreadCount > 0" class="nav-dot">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </router-link>
        </nav>
      </aside>

      <!-- ==================== 中间内容区 ==================== -->
      <main class="center-content">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <!-- ==================== 右侧信息面板 ==================== -->
      <aside class="right-panel">
        <div v-if="!authStore.isLoggedIn" class="panel-card login-card">
          <el-icon :size="40" color="#409eff"><UserFilled /></el-icon>
          <p class="login-hint">登录后可发布寻物启事、查看匹配记录</p>
          <el-button type="primary" @click="$router.push('/user/login')" style="width:100%">立即登录</el-button>
          <el-button @click="$router.push('/user/register')" style="width:100%;margin-top:8px">注册账号</el-button>
        </div>

        <div class="panel-card stats-card">
          <h4 class="panel-title">平台数据</h4>
          <div class="stat-row">
            <div class="stat-item">
              <span class="stat-num" style="color:#409eff">{{ panelStats.todayNew }}</span>
              <span class="stat-desc">今日新增</span>
            </div>
            <div class="stat-item">
              <span class="stat-num" style="color:#67c23a">{{ panelStats.totalReturned }}</span>
              <span class="stat-desc">累计归还</span>
            </div>
            <div class="stat-item">
              <span class="stat-num" style="color:#e6a23c">{{ panelStats.currentUnclaimed }}</span>
              <span class="stat-desc">待领取</span>
            </div>
          </div>
        </div>

        <div class="panel-card recent-card">
          <h4 class="panel-title">最新拾物</h4>
          <div v-if="recentItems.length === 0" class="panel-empty">暂无数据</div>
          <div
            v-for="item in recentItems"
            :key="item.id"
            class="recent-item"
            @click="$router.push(`/user/item/${item.id}`)"
          >
            <el-image :src="item.imageUrl" class="recent-thumb" fit="cover">
              <template #error><div class="thumb-placeholder"><el-icon><Picture /></el-icon></div></template>
            </el-image>
            <div class="recent-info">
              <div class="recent-cat">{{ item.category }}</div>
              <div class="recent-loc">{{ item.storageLocation }}</div>
            </div>
          </div>
        </div>
      </aside>
    </div>

    <!-- ==================== 底栏 ==================== -->
    <footer class="layout-footer">
      <span>© {{ currentYear }} 智能校园失物招领平台 | XX大学</span>
    </footer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { useAppStore } from '@/store/app'
import { logout as logoutApi } from '@/api/auth'
import { getUnreadCount } from '@/api/notification'
import { getDashboardStats } from '@/api/dashboard'
import { getBrowseItems } from '@/api/browse'
import { ElMessage } from 'element-plus'
import {
  Search, Sunny, Moon, Bell, User, ArrowDown, SwitchButton,
  Grid, MagicStick, EditPen, Tickets, Fold, Expand, UserFilled, Picture,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const appStore = useAppStore()

const sidebarCollapsed = ref(false)
const unreadCount = ref(0)
const quickSearch = ref('')
const currentYear = new Date().getFullYear()

const panelStats = reactive({ todayNew: 0, totalReturned: 0, currentUnclaimed: 0 })
const recentItems = ref([])

onMounted(async () => {
  if (authStore.isLoggedIn) {
    try { const res = await getUnreadCount(); unreadCount.value = res.count || 0 } catch { /* */ }
  }
  try {
    const stats = await getDashboardStats()
    panelStats.todayNew = stats.todayNewItems || 0
    panelStats.totalReturned = stats.totalReturned || 0
    panelStats.currentUnclaimed = stats.currentUnclaimed || 0
  } catch { /* */ }
  try {
    const data = await getBrowseItems({ page: 1, size: 5 })
    recentItems.value = data.records || []
  } catch { /* */ }
})

function toggleSidebar() { sidebarCollapsed.value = !sidebarCollapsed.value }

function goSearch() {
  if (quickSearch.value.trim()) {
    router.push({ path: '/user/search', query: { q: quickSearch.value.trim() } })
  }
}

function handleCommand(cmd) {
  if (cmd === 'logout') {
    logoutApi().finally(() => {
      authStore.logout()
      router.push('/user/login')
      ElMessage.success('已退出登录')
    })
  } else if (cmd === 'profile') {
    router.push('/user/profile')
  }
}
</script>

<style scoped>
/* ==================== 全局布局 ==================== */
.app-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: var(--bg-primary);
}

/* ==================== 顶栏 ==================== */
.topbar {
  height: 56px;
  background: var(--header-bg);
  border-bottom: 1px solid var(--border-color);
  display: flex;
  align-items: center;
  padding: 0 16px;
  gap: 16px;
  position: sticky;
  top: 0;
  z-index: 200;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.topbar-left { display: flex; align-items: center; gap: 12px; flex-shrink: 0; }
.collapse-btn { cursor: pointer; color: var(--text-secondary); padding: 4px; border-radius: 6px; transition: background 0.2s; }
.collapse-btn:hover { background: rgba(0, 0, 0, 0.05); }
.topbar-logo { display: flex; align-items: center; gap: 8px; text-decoration: none; color: #409eff; font-weight: 700; font-size: 18px; }
.logo-text { white-space: nowrap; }
.topbar-center { flex: 1; max-width: 480px; margin: 0 auto; }
.quick-search { width: 100%; }
.topbar-right { display: flex; align-items: center; gap: 16px; flex-shrink: 0; }
.topbar-icon {
  cursor: pointer;
  color: var(--text-secondary);
  padding: 6px;
  border-radius: 8px;
  transition: background 0.15s, color 0.15s;
}
.topbar-icon:hover { background: rgba(0, 0, 0, 0.05); color: #409eff; }
.topbar-icon.has-notif { color: #409eff; position: relative; }
.topbar-icon.has-notif::after {
  content: '';
  position: absolute;
  top: 4px;
  right: 4px;
  width: 8px;
  height: 8px;
  background: #f56c6c;
  border-radius: 50%;
  border: 1.5px solid var(--header-bg);
}
.user-chip { display: flex; align-items: center; gap: 8px; cursor: pointer; padding: 4px 8px; border-radius: 8px; transition: background 0.15s; }
.user-chip:hover { background: rgba(0, 0, 0, 0.04); }
.user-chip-name { font-size: 14px; color: var(--text-primary); max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ==================== 主体三栏 ==================== */
.layout-body { flex: 1; display: flex; max-width: 1400px; width: 100%; margin: 0 auto; }

/* ==================== 左侧导航 ==================== */
.left-sidebar {
  width: 220px; flex-shrink: 0;
  background: var(--header-bg);
  border-right: 1px solid var(--border-color);
  padding: 12px 8px;
  display: flex; flex-direction: column;
  transition: width 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
}
.left-sidebar.collapsed { width: 64px; }
.side-nav { display: flex; flex-direction: column; gap: 2px; }
.nav-item {
  display: flex; align-items: center; gap: 10px;
  padding: 10px 12px; border-radius: 8px;
  text-decoration: none; color: var(--text-secondary);
  font-size: 14px; transition: all 0.15s;
  white-space: nowrap; overflow: hidden;
  position: relative;
}
.nav-item:hover { background: rgba(64, 158, 255, 0.08); color: #409eff; }
.nav-item.active { background: rgba(64, 158, 255, 0.12); color: #409eff; font-weight: 600; }
.nav-label { opacity: 1; transition: opacity 0.15s; }
.collapsed .nav-label { opacity: 0; width: 0; }
.nav-dot {
  position: absolute; right: 10px;
  background: #f56c6c; color: #fff;
  font-size: 11px; font-weight: 600;
  min-width: 18px; height: 18px;
  line-height: 18px; text-align: center;
  border-radius: 9px; padding: 0 5px;
}
.nav-divider { height: 1px; background: var(--border-color); margin: 8px 12px; }

/* ==================== 中间内容 ==================== */
.center-content { flex: 1; min-width: 0; padding: 24px; overflow-y: auto; }

/* ==================== 右侧面板 ==================== */
.right-panel { width: 280px; flex-shrink: 0; padding: 16px 12px; display: flex; flex-direction: column; gap: 16px; overflow-y: auto; }
.panel-card { background: var(--header-bg); border-radius: 10px; padding: 16px; box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04); }
.panel-title { font-size: 14px; font-weight: 600; color: var(--text-primary); margin-bottom: 12px; padding-bottom: 8px; border-bottom: 1px solid var(--border-color); }
.panel-empty { text-align: center; color: var(--text-muted); font-size: 13px; padding: 16px 0; }

.login-card { text-align: center; }
.login-hint { font-size: 13px; color: var(--text-secondary); margin: 12px 0; line-height: 1.6; }

.stat-row { display: flex; justify-content: space-around; }
.stat-item { text-align: center; }
.stat-num { font-size: 24px; font-weight: 700; display: block; }
.stat-desc { font-size: 12px; color: var(--text-muted); margin-top: 2px; }

.recent-item { display: flex; gap: 10px; padding: 8px; border-radius: 8px; cursor: pointer; transition: background 0.15s; }
.recent-item:hover { background: rgba(0, 0, 0, 0.03); }
.recent-thumb { width: 44px; height: 44px; border-radius: 6px; flex-shrink: 0; }
.thumb-placeholder { width: 44px; height: 44px; display: flex; align-items: center; justify-content: center; background: #f5f7fa; border-radius: 6px; color: #c0c4cc; }
.recent-info { flex: 1; min-width: 0; }
.recent-cat { font-size: 13px; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.recent-loc { font-size: 12px; color: var(--text-muted); margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

/* ==================== 底栏 ==================== */
.layout-footer { height: 44px; display: flex; align-items: center; justify-content: center; border-top: 1px solid var(--border-color); font-size: 12px; color: var(--text-muted); background: var(--header-bg); }

/* ==================== 动画 ==================== */
.page-fade-enter-active, .page-fade-leave-active { transition: opacity 0.2s ease, transform 0.2s ease; }
.page-fade-enter-from { opacity: 0; transform: translateY(6px); }
.page-fade-leave-to { opacity: 0; transform: translateY(-6px); }

/* ==================== 响应式 ==================== */
@media (max-width: 1100px) { .right-panel { display: none; } }
@media (max-width: 768px) {
  .left-sidebar { position: fixed; left: 0; top: 56px; bottom: 0; z-index: 150; box-shadow: 2px 0 8px rgba(0,0,0,0.1); transform: translateX(-100%); }
  .left-sidebar:not(.collapsed) { transform: translateX(0); }
  .center-content { padding: 16px; }
  .topbar-center { display: none; }
  .logo-text { display: none; }
}
</style>
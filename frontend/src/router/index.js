import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/user/login',
  },
  // ==================== 失主端 ====================
  {
    path: '/user',
    component: () => import('@/layouts/UserLayout.vue'),
    children: [
      {
        path: 'login',
        name: 'UserLogin',
        component: () => import('@/views/user/LoginView.vue'),
        meta: { title: '失主登录' },
      },
      {
        path: 'register',
        name: 'UserRegister',
        component: () => import('@/views/user/RegisterView.vue'),
        meta: { title: '失主注册' },
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/user/SearchView.vue'),
        meta: { title: '物品检索', requiresAuth: true, role: 'ROLE_USER' },
      },
      {
        path: 'confirm-result',
        name: 'ConfirmResult',
        component: () => import('@/views/user/ConfirmResultView.vue'),
        meta: { title: '确认结果', requiresAuth: true, role: 'ROLE_USER' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/user/ProfileView.vue'),
        meta: { title: '个人中心', requiresAuth: true },
      },
    ],
  },
  // ==================== 管理端 ====================
  {
    path: '/admin',
    redirect: '/admin/login',
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/LoginView.vue'),
    meta: { title: '管理端登录' },
  },
  {
    path: '/admin',
    component: () => import('@/layouts/AdminLayout.vue'),
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/admin/DashboardView.vue'),
        meta: { title: '数据看板', requiresAuth: true, role: ['ROLE_STAFF', 'ROLE_ADMIN'] },
      },
      {
        path: 'items/register',
        name: 'ItemRegister',
        component: () => import('@/views/admin/ItemRegisterView.vue'),
        meta: { title: '拾物登记', requiresAuth: true, role: ['ROLE_STAFF', 'ROLE_ADMIN'] },
      },
      {
        path: 'items/list',
        name: 'ItemList',
        component: () => import('@/views/admin/ItemListView.vue'),
        meta: { title: '拾物列表', requiresAuth: true, role: ['ROLE_STAFF', 'ROLE_ADMIN'] },
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManageView.vue'),
        meta: { title: '用户管理', requiresAuth: true, role: 'ROLE_ADMIN' },
      },
      {
        path: 'configs',
        name: 'ConfigManage',
        component: () => import('@/views/admin/ConfigManageView.vue'),
        meta: { title: '系统配置', requiresAuth: true, role: 'ROLE_ADMIN' },
      },
      {
        path: 'logs',
        name: 'SystemLog',
        component: () => import('@/views/admin/SystemLogView.vue'),
        meta: { title: '系统日志', requiresAuth: true, role: 'ROLE_ADMIN' },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 失物招领平台` : '智能校园失物招领平台'

  const requiresAuth = to.meta.requiresAuth
  const requiredRole = to.meta.role

  const accessToken = sessionStorage.getItem('accessToken')
  const userRole = sessionStorage.getItem('userRole')

  if (requiresAuth) {
    if (!accessToken) {
      // 未登录
      const loginPath = to.path.startsWith('/admin') ? '/admin/login' : '/user/login'
      next({ path: loginPath, query: { redirect: to.fullPath } })
      return
    }

    if (requiredRole) {
      const roles = Array.isArray(requiredRole) ? requiredRole : [requiredRole]
      if (!roles.includes(userRole)) {
        next({ path: '/403' })
        return
      }
    }
  }

  // 已登录用户访问登录页，重定向到首页
  if (accessToken && (to.name === 'UserLogin' || to.name === 'AdminLogin')) {
    if (userRole === 'ROLE_USER') {
      next('/user/search')
    } else {
      next('/admin/dashboard')
    }
    return
  }

  next()
})

export default router

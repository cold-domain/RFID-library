import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      { path: '', name: 'Login', component: () => import('@/views/login/LoginView.vue') }
    ]
  },
  {
    path: '/register',
    component: () => import('@/layouts/BlankLayout.vue'),
    children: [
      { path: '', name: 'Register', component: () => import('@/views/register/RegisterView.vue') }
    ]
  },
  {
    path: '/',
    component: () => import('@/layouts/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/DashboardView.vue'),
        meta: { title: '控制中心' }
      },
      {
        path: 'public/books',
        name: 'BookSearch',
        component: () => import('@/views/public/BookSearch.vue'),
        meta: { title: '图书搜索' }
      },
      // 读者页面
      {
        path: 'reader/profile',
        name: 'Profile',
        component: () => import('@/views/reader/ProfileView.vue'),
        meta: { title: '个人信息', roles: ['reader', 'librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'reader/borrows',
        name: 'MyBorrows',
        component: () => import('@/views/reader/MyBorrowsView.vue'),
        meta: { title: '我的借阅', roles: ['reader', 'librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'reader/reservations',
        name: 'MyReservations',
        component: () => import('@/views/reader/MyReservationsView.vue'),
        meta: { title: '我的预约', roles: ['reader', 'librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'reader/fines',
        name: 'MyFines',
        component: () => import('@/views/reader/MyFinesView.vue'),
        meta: { title: '我的罚款', roles: ['reader', 'librarian', 'super_admin', 'system_admin'] }
      },
      // 馆员页面
      {
        path: 'librarian/books',
        name: 'BookManage',
        component: () => import('@/views/librarian/BookManage.vue'),
        meta: { title: '图书管理', roles: ['librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'librarian/categories',
        name: 'CategoryManage',
        component: () => import('@/views/librarian/CategoryManage.vue'),
        meta: { title: '分类管理', roles: ['librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'librarian/borrows',
        name: 'BorrowManage',
        component: () => import('@/views/librarian/BorrowManage.vue'),
        meta: { title: '借阅管理', roles: ['librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'librarian/reservations',
        name: 'ReservationManage',
        component: () => import('@/views/librarian/ReservationManage.vue'),
        meta: { title: '预约管理', roles: ['librarian', 'super_admin', 'system_admin'] }
      },
      {
        path: 'librarian/fines',
        name: 'FineManage',
        component: () => import('@/views/librarian/FineManage.vue'),
        meta: { title: '罚款管理', roles: ['librarian', 'super_admin', 'system_admin'] }
      },
      // 管理员页面
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', roles: ['system_admin'] }
      },
      {
        path: 'admin/roles',
        name: 'RoleManage',
        component: () => import('@/views/admin/RoleManage.vue'),
        meta: { title: '角色管理', roles: ['system_admin'] }
      },
      {
        path: 'admin/permissions',
        name: 'PermissionManage',
        component: () => import('@/views/admin/PermissionManage.vue'),
        meta: { title: '权限管理', roles: ['system_admin'] }
      },
      {
        path: 'admin/audit-logs',
        name: 'AuditLog',
        component: () => import('@/views/admin/AuditLogView.vue'),
        meta: { title: '审计日志', roles: ['system_admin'] }
      },
      {
        path: 'admin/exceptions',
        name: 'ExceptionView',
        component: () => import('@/views/admin/ExceptionView.vue'),
        meta: { title: '异常管理', roles: ['system_admin'] }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 标记是否已在本次会话中刷新过用户信息
let userInfoFetched = false

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 公开页面
  if (to.path === '/login' || to.path === '/register') {
    if (userStore.isLoggedIn) {
      return next('/dashboard')
    }
    userInfoFetched = false
    return next()
  }

  // 需要登录
  if (!userStore.isLoggedIn) {
    userInfoFetched = false
    return next('/login')
  }

  // 已登录但未刷新用户信息，从服务器获取最新角色
  if (!userInfoFetched) {
    userInfoFetched = true
    await userStore.fetchUserInfo()
    // fetchUserInfo失败会调用logout跳转到登录页，这里检查是否仍登录
    if (!userStore.isLoggedIn) {
      return next('/login')
    }
  }

  // 检查角色权限
  const requiredRoles = to.meta.roles
  if (requiredRoles && requiredRoles.length > 0) {
    const hasPermission = userStore.roles.some(role => requiredRoles.includes(role))
    if (!hasPermission) {
      ElMessage.error('没有权限访问该页面')
      return next('/dashboard')
    }
  }

  next()
})

export default router

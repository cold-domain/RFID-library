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
        meta: { title: '控制中心', permission: 'dashboard' }
      },
      {
        path: 'public/books',
        name: 'BookSearch',
        component: () => import('@/views/public/BookSearch.vue'),
        meta: { title: '图书搜索', permission: 'public:book' }
      },
      {
        path: 'reader/profile',
        name: 'Profile',
        component: () => import('@/views/reader/ProfileView.vue'),
        meta: { title: '个人信息', permission: 'reader:profile' }
      },
      {
        path: 'reader/borrows',
        name: 'MyBorrows',
        component: () => import('@/views/reader/MyBorrowsView.vue'),
        meta: { title: '我的借阅', permission: 'reader:borrows' }
      },
      {
        path: 'reader/reservations',
        name: 'MyReservations',
        component: () => import('@/views/reader/MyReservationsView.vue'),
        meta: { title: '我的预约', permission: 'reader:reservations' }
      },
      {
        path: 'reader/fines',
        name: 'MyFines',
        component: () => import('@/views/reader/MyFinesView.vue'),
        meta: { title: '我的罚款', permission: 'reader:fines' }
      },
      {
        path: 'librarian/books',
        name: 'BookManage',
        component: () => import('@/views/librarian/BookManage.vue'),
        meta: { title: '图书管理', permission: 'book' }
      },
      {
        path: 'librarian/categories',
        name: 'CategoryManage',
        component: () => import('@/views/librarian/CategoryManage.vue'),
        meta: { title: '分类管理', permission: 'category' }
      },
      {
        path: 'librarian/borrows',
        name: 'BorrowManage',
        component: () => import('@/views/librarian/BorrowManage.vue'),
        meta: { title: '借阅管理', permission: 'borrow' }
      },
      {
        path: 'librarian/reservations',
        name: 'ReservationManage',
        component: () => import('@/views/librarian/ReservationManage.vue'),
        meta: { title: '预约管理', permission: 'reservation' }
      },
      {
        path: 'librarian/fines',
        name: 'FineManage',
        component: () => import('@/views/librarian/FineManage.vue'),
        meta: { title: '罚款管理', permission: 'fine' }
      },
      {
        path: 'admin/users',
        name: 'UserManage',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理', permission: 'user' }
      },
      {
        path: 'admin/roles',
        name: 'RoleManage',
        component: () => import('@/views/admin/RoleManage.vue'),
        meta: { title: '角色管理', permission: 'role' }
      },
      {
        path: 'admin/permissions',
        name: 'PermissionManage',
        component: () => import('@/views/admin/PermissionManage.vue'),
        meta: { title: '权限管理', permission: 'permission' }
      },
      {
        path: 'admin/audit-logs',
        name: 'AuditLog',
        component: () => import('@/views/admin/AuditLogView.vue'),
        meta: { title: '审计日志', permission: 'audit' }
      },
      {
        path: 'admin/exceptions',
        name: 'ExceptionView',
        component: () => import('@/views/admin/ExceptionView.vue'),
        meta: { title: '异常管理', permission: 'exception' }
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

router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  if (to.path === '/login' || to.path === '/register') {
    if (userStore.isLoggedIn) {
      return next(userStore.getDefaultRoutePath())
    }
    return next()
  }

  if (!userStore.isLoggedIn) {
    return next('/login')
  }

  await userStore.fetchUserInfo()
  if (!userStore.isLoggedIn) {
    return next('/login')
  }

  const requiredPermission = to.meta.permission
  if (requiredPermission && !userStore.hasPermission(requiredPermission) && !userStore.canAccessPath(to.path)) {
    ElMessage.error('没有权限访问该页面')
    return next(userStore.getDefaultRoutePath())
  }

  next()
})

export default router

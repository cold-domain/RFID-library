import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getToken, setToken, getUserInfo, setUserInfo, clearAuth } from '@/utils/auth'
import { login as loginApi, register as registerApi, getMe } from '@/api/auth'
import router from '@/router'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const userInfo = ref(getUserInfo() || {})

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value.roles || [])
  const permissionCodes = computed(() => userInfo.value.permissionCodes || [])
  const menus = computed(() => userInfo.value.menus || [])
  const username = computed(() => userInfo.value.username || '')
  const realName = computed(() => userInfo.value.realName || '')
  const userId = computed(() => userInfo.value.userId || null)

  function hasRole(role) {
    return roles.value.includes(role)
  }

  function hasPermission(permissionCode) {
    return permissionCodes.value.includes(permissionCode)
  }

  function flattenMenuPaths(nodes = []) {
    const paths = []
    for (const node of nodes) {
      if (node?.url) paths.push(node.url)
      if (node?.children?.length) {
        paths.push(...flattenMenuPaths(node.children))
      }
    }
    return paths
  }

  const menuPaths = computed(() => flattenMenuPaths(menus.value))

  function canAccessPath(path) {
    return menuPaths.value.includes(path)
  }

  function getDefaultRoutePath() {
    return menuPaths.value[0] || '/dashboard'
  }

  async function login(form) {
    const res = await loginApi(form)
    const data = res.data
    token.value = data.token
    setToken(data.token)
    const info = {
      userId: data.userId,
      username: data.username,
      realName: data.realName,
      roles: data.roles || [],
      permissionCodes: data.permissionCodes || [],
      menus: data.menus || []
    }
    userInfo.value = info
    setUserInfo(info)
    return data
  }

  async function register(form) {
    return await registerApi(form)
  }

  async function fetchUserInfo() {
    try {
      const res = await getMe()
      const data = res.data
      const info = {
        userId: data.userId,
        username: data.username,
        realName: data.realName,
        roles: data.roles || [],
        permissionCodes: data.permissionCodes || [],
        menus: data.menus || []
      }
      userInfo.value = info
      setUserInfo(info)
    } catch (e) {
      // token无效或过期，清除登录状态
      logout()
    }
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    clearAuth()
    router.push('/login')
  }

  return {
    token, userInfo, isLoggedIn, roles, permissionCodes, menus, menuPaths, username, realName, userId,
    hasRole, hasPermission, canAccessPath, getDefaultRoutePath, login, register, logout, fetchUserInfo
  }
})

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
  const username = computed(() => userInfo.value.username || '')
  const realName = computed(() => userInfo.value.realName || '')
  const userId = computed(() => userInfo.value.userId || null)

  const roleLevelMap = {
    system_admin: 5,
    super_admin: 4,
    librarian: 3,
    reader: 2,
    visitor: 1
  }

  const maxRoleLevel = computed(() => {
    let max = 0
    for (const r of roles.value) {
      const level = roleLevelMap[r] || 0
      if (level > max) max = level
    }
    return max
  })

  function hasRole(role) {
    return roles.value.includes(role)
  }

  function hasAccess(requiredRole) {
    const requiredLevel = roleLevelMap[requiredRole] || 0
    return maxRoleLevel.value >= requiredLevel
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
      roles: data.roles
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
        roles: data.roles
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
    token, userInfo, isLoggedIn, roles, username, realName, userId,
    maxRoleLevel, hasRole, hasAccess, login, register, logout, fetchUserInfo
  }
})

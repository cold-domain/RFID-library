<template>
  <div class="navbar">
    <div class="left">
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar" :size="20">
        <Fold v-if="!appStore.sidebarCollapsed" />
        <Expand v-else />
      </el-icon>
    </div>
    <div class="right">
      <el-dropdown trigger="click" @command="handleCommand">
        <span class="user-info">
          <el-icon><UserFilled /></el-icon>
          <span class="username">{{ userStore.realName || userStore.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人中心</el-dropdown-item>
            <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/reader/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
  }
}
</script>

<style scoped>
.navbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  height: 100%;
}
.collapse-btn {
  cursor: pointer;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 4px;
}
.username {
  margin: 0 4px;
  font-size: 14px;
}
</style>

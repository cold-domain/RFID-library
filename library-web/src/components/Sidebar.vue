<template>
  <div class="sidebar">
    <div class="logo">
      <el-icon v-if="appStore.sidebarCollapsed" :size="24"><Reading /></el-icon>
      <span v-else>图书馆管理系统</span>
    </div>
    <el-menu
      :default-active="route.path"
      :collapse="appStore.sidebarCollapsed"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      router
    >
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <template #title>控制中心</template>
      </el-menu-item>

      <el-menu-item index="/public/books">
        <el-icon><Search /></el-icon>
        <template #title>图书搜索</template>
      </el-menu-item>

      <!-- 读者菜单 -->
      <el-sub-menu v-if="userStore.hasAccess('reader')" index="/reader">
        <template #title>
          <el-icon><User /></el-icon>
          <span>读者中心</span>
        </template>
        <el-menu-item index="/reader/profile">个人信息</el-menu-item>
        <el-menu-item index="/reader/borrows">我的借阅</el-menu-item>
        <el-menu-item index="/reader/reservations">我的预约</el-menu-item>
        <el-menu-item index="/reader/fines">我的罚款</el-menu-item>
      </el-sub-menu>

      <!-- 馆员菜单 -->
      <el-sub-menu v-if="userStore.hasAccess('librarian')" index="/librarian">
        <template #title>
          <el-icon><Management /></el-icon>
          <span>馆员管理</span>
        </template>
        <el-menu-item index="/librarian/books">图书管理</el-menu-item>
        <el-menu-item index="/librarian/categories">分类管理</el-menu-item>
        <el-menu-item index="/librarian/borrows">借阅管理</el-menu-item>
        <el-menu-item index="/librarian/reservations">预约管理</el-menu-item>
        <el-menu-item index="/librarian/fines">罚款管理</el-menu-item>
      </el-sub-menu>

      <!-- 管理员菜单 -->
      <el-sub-menu v-if="userStore.hasAccess('system_admin')" index="/admin">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
        <el-menu-item index="/admin/roles">角色管理</el-menu-item>
        <el-menu-item index="/admin/permissions">权限管理</el-menu-item>
        <el-menu-item index="/admin/audit-logs">审计日志</el-menu-item>
        <el-menu-item index="/admin/exceptions">异常管理</el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
</script>

<style scoped>
.sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}
.logo {
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: bold;
  white-space: nowrap;
  border-bottom: 1px solid #3a4a5e;
}
.el-menu {
  border-right: none;
  flex: 1;
  overflow-y: auto;
}
</style>

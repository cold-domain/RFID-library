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
      <template v-for="item in renderMenus" :key="item.id || item.permissionCode || item.url">
        <el-sub-menu v-if="hasChildren(item)" :index="menuIndex(item)">
          <template #title>
            <el-icon><Menu /></el-icon>
            <span>{{ menuTitle(item) }}</span>
          </template>
          <el-menu-item
            v-for="child in visibleChildren(item)"
            :key="child.id || child.permissionCode || child.url"
            :index="child.url"
          >
            <el-icon><Menu /></el-icon>
            <template #title>{{ menuTitle(child) }}</template>
          </el-menu-item>
        </el-sub-menu>
        <el-menu-item v-else-if="item.url" :index="item.url">
          <el-icon><Menu /></el-icon>
          <template #title>{{ menuTitle(item) }}</template>
        </el-menu-item>
      </template>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'

const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()

const titleMap = {
  dashboard: '控制中心',
  'public:book': '图书搜索',
  reader: '读者中心',
  'reader:profile': '个人信息',
  'reader:borrows': '我的借阅',
  'reader:reservations': '我的预约',
  'reader:fines': '我的罚款',
  book: '图书管理',
  category: '分类管理',
  borrow: '借阅管理',
  reservation: '预约管理',
  fine: '罚款管理',
  user: '用户管理',
  role: '角色管理',
  permission: '权限管理',
  audit: '审计日志',
  exception: '异常管理'
}

const renderMenus = computed(() => (userStore.menus || []).filter(item => item?.url || (item?.children || []).length > 0))

function visibleChildren(item) {
  return (item?.children || []).filter(child => child?.url || (child?.children || []).length > 0)
}

function hasChildren(item) {
  return visibleChildren(item).length > 0
}

function menuIndex(item) {
  return item.url || String(item.id || item.permissionCode || Math.random())
}

function menuTitle(item) {
  return titleMap[item?.permissionCode] || item?.name || '未命名菜单'
}
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

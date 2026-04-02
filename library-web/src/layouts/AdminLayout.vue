<template>
  <el-container class="admin-layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="sidebar-container">
      <Sidebar />
    </el-aside>
    <el-container>
      <el-header class="header-container">
        <Navbar />
      </el-header>
      <el-main class="main-container">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import Sidebar from '@/components/Sidebar.vue'
import Navbar from '@/components/Navbar.vue'
import { useAppStore } from '@/store/app'

const appStore = useAppStore()
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}
.sidebar-container {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}
.header-container {
  background: #fff;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
  display: flex;
  align-items: center;
  height: 50px;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);
}
.main-container {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>

<template>
  <div class="dashboard">
    <h2>{{ texts.dashboard }}</h2>
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="30"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalBooks }}</div>
            <div class="stat-label">{{ texts.totalBooks }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #67c23a;">
            <el-icon :size="30"><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalUsers }}</div>
            <div class="stat-label">{{ texts.totalUsers }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #e6a23c;">
            <el-icon :size="30"><Tickets /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.activeBorrows }}</div>
            <div class="stat-label">{{ texts.activeBorrows }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #f56c6c;">
            <el-icon :size="30"><Warning /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.overdueCount }}</div>
            <div class="stat-label">{{ texts.overdueCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>{{ texts.borrowTrend }}</template>
          <div ref="borrowTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>{{ texts.categoryStats }}</template>
          <div ref="categoryRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>{{ texts.quickActions }}</template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/public/books')">
              <el-icon><Search /></el-icon>{{ texts.bookSearch }}
            </el-button>
            <el-button v-if="userStore.hasPermission('reader:borrows')" @click="$router.push('/reader/borrows')">
              <el-icon><Tickets /></el-icon>{{ texts.myBorrows }}
            </el-button>
            <el-button v-if="userStore.hasPermission('book')" type="success" @click="$router.push('/librarian/books')">
              <el-icon><Management /></el-icon>{{ texts.bookManage }}
            </el-button>
            <el-button v-if="userStore.hasPermission('user')" type="danger" @click="$router.push('/admin/users')">
              <el-icon><Setting /></el-icon>{{ texts.userManage }}
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>{{ texts.profile }}</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item :label="texts.username">{{ userStore.username }}</el-descriptions-item>
            <el-descriptions-item :label="texts.realName">{{ userStore.realName }}</el-descriptions-item>
            <el-descriptions-item :label="texts.roles">
              <el-tag v-for="role in userStore.roles" :key="role" size="small" style="margin-right: 4px;">
                {{ roleLabels[role] || role }}
              </el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { getDashboardStats } from '@/api/dashboard'

const texts = {
  dashboard: '\u63a7\u5236\u4e2d\u5fc3',
  totalBooks: '\u56fe\u4e66\u603b\u6570',
  totalUsers: '\u7528\u6237\u603b\u6570',
  activeBorrows: '\u5f53\u524d\u501f\u9605',
  overdueCount: '\u903e\u671f\u672a\u8fd8',
  borrowTrend: '\u8fd1\u4e03\u5929\u501f\u9605\u8d8b\u52bf',
  categoryStats: '\u56fe\u4e66\u5206\u7c7b\u5206\u5e03',
  quickActions: '\u5feb\u6377\u64cd\u4f5c',
  bookSearch: '\u641c\u7d22\u56fe\u4e66',
  myBorrows: '\u6211\u7684\u501f\u9605',
  bookManage: '\u56fe\u4e66\u7ba1\u7406',
  userManage: '\u7528\u6237\u7ba1\u7406',
  profile: '\u4e2a\u4eba\u4fe1\u606f',
  username: '\u7528\u6237\u540d',
  realName: '\u59d3\u540d',
  roles: '\u89d2\u8272'
}

const userStore = useUserStore()
const roleLabels = {
  system_admin: '\u7cfb\u7edf\u7ba1\u7406\u5458',
  super_admin: '\u8d85\u7ea7\u7ba1\u7406\u5458',
  librarian: '\u9986\u5458',
  reader: '\u8bfb\u8005',
  visitor: '\u8bbf\u5ba2'
}

const stats = ref({
  totalBooks: '-',
  totalUsers: '-',
  activeBorrows: '-',
  overdueCount: '-'
})

const borrowTrendRef = ref(null)
const categoryRef = ref(null)
let borrowTrendChart = null
let categoryChart = null

async function loadStats() {
  try {
    const res = await getDashboardStats()
    const data = res.data
    stats.value = {
      totalBooks: data.totalBooks ?? '-',
      totalUsers: data.totalUsers ?? '-',
      activeBorrows: data.activeBorrows ?? '-',
      overdueCount: data.overdueCount ?? '-'
    }

    await nextTick()
    initBorrowTrendChart(data.borrowTrend || [])
    initCategoryChart(data.categoryStats || [])
  } catch (e) {
    // handled by interceptor
  }
}

function initBorrowTrendChart(trendData) {
  if (!borrowTrendRef.value) return
  borrowTrendChart?.dispose()
  borrowTrendChart = echarts.init(borrowTrendRef.value)
  borrowTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: { type: 'category', data: trendData.map(item => item.date) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [{
      type: 'bar',
      data: trendData.map(item => item.count),
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#409eff' },
          { offset: 1, color: '#a0cfff' }
        ]),
        borderRadius: [4, 4, 0, 0]
      },
      barWidth: '40%'
    }]
  })
}

function initCategoryChart(categoryData) {
  if (!categoryRef.value) return
  categoryChart?.dispose()
  categoryChart = echarts.init(categoryRef.value)
  categoryChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 20,
      bottom: 20
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: { label: { show: true, fontSize: 14, fontWeight: 'bold' } },
      data: categoryData
    }]
  })
}

function handleResize() {
  borrowTrendChart?.resize()
  categoryChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  borrowTrendChart?.dispose()
  categoryChart?.dispose()
})
</script>

<style scoped>
.stat-cards {
  margin-top: 16px;
}
.stat-card {
  cursor: pointer;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  padding: 20px;
}
.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  margin-right: 16px;
}
.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}
.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}
.chart-container {
  width: 100%;
  height: 300px;
}
.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>

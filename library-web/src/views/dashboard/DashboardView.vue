<template>
  <div class="dashboard">
    <h2>{{ texts.dashboard }}</h2>
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="24" :sm="12" :lg="6">
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
      <el-col :xs="24" :sm="12" :lg="6">
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
      <el-col :xs="24" :sm="12" :lg="6">
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
      <el-col :xs="24" :sm="12" :lg="6">
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

    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :xl="12">
        <el-card>
          <template #header>{{ texts.borrowTrend }}</template>
          <div ref="borrowTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card>
          <template #header>{{ texts.categoryStats }}</template>
          <div ref="categoryRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :xl="14">
        <el-card>
          <template #header>
            <div class="section-header">
              <span>{{ texts.hotChart }}</span>
              <el-tag v-if="hotBooks.length" type="danger" effect="dark">
                {{ texts.topBookPrefix }}{{ hotBooks[0].title }}
              </el-tag>
            </div>
          </template>
          <div ref="hotBookChartRef" class="chart-container hot-chart"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="10">
        <el-card>
          <template #header>{{ texts.hotRanking }}</template>
          <div v-if="hotBooks.length" class="hot-list">
            <div
              v-for="book in hotBooks.slice(0, 5)"
              :key="book.id"
              class="hot-item"
            >
              <div class="hot-rank" :class="`rank-${book.rank}`">{{ book.rank }}</div>
              <div class="hot-main">
                <div class="hot-title-row">
                  <span class="hot-title">{{ book.title }}</span>
                  <el-tag size="small" :type="statusTagType(book.status)">{{ statusLabel(book.status) }}</el-tag>
                </div>
                <div class="hot-meta">
                  <span>{{ book.author || texts.unknownAuthor }}</span>
                  <span>{{ book.categoryName || texts.unknownCategory }}</span>
                </div>
                <div class="hot-progress-row">
                  <el-progress
                    :percentage="hotProgress(book.borrowCount)"
                    :show-text="false"
                    :stroke-width="10"
                    color="#f56c6c"
                  />
                  <span class="hot-count">{{ book.borrowCount }} {{ texts.borrowUnit }}</span>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else :description="texts.noHotData" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :xl="12">
        <el-card>
          <template #header>{{ texts.quickActions }}</template>
          <div class="quick-actions">
            <el-button type="danger" plain @click="$router.push('/public/hot-books')">
              <el-icon><Reading /></el-icon>{{ texts.hotBooksPage }}
            </el-button>
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
      <el-col :xs="24" :xl="12">
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
  hotChart: '\u70ed\u95e8\u56fe\u4e66\u699c\u5355',
  hotRanking: '\u501f\u9605 TOP 5',
  topBookPrefix: '\u9986\u5185\u6700\u70ed\uff1a',
  borrowUnit: '\u6b21',
  noHotData: '\u6682\u65e0\u70ed\u95e8\u501f\u9605\u6570\u636e',
  quickActions: '\u5feb\u6377\u64cd\u4f5c',
  hotBooksPage: '\u70ed\u95e8\u699c\u5355',
  bookSearch: '\u641c\u7d22\u56fe\u4e66',
  myBorrows: '\u6211\u7684\u501f\u9605',
  bookManage: '\u56fe\u4e66\u7ba1\u7406',
  userManage: '\u7528\u6237\u7ba1\u7406',
  profile: '\u4e2a\u4eba\u4fe1\u606f',
  username: '\u7528\u6237\u540d',
  realName: '\u59d3\u540d',
  roles: '\u89d2\u8272',
  available: '\u5728\u67b6',
  borrowed: '\u5df2\u501f\u51fa',
  exception: '\u5f02\u5e38',
  unknownAuthor: '\u4f5c\u8005\u672a\u77e5',
  unknownCategory: '\u672a\u5206\u7c7b'
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
const hotBooks = ref([])

const borrowTrendRef = ref(null)
const categoryRef = ref(null)
const hotBookChartRef = ref(null)
let borrowTrendChart = null
let categoryChart = null
let hotBookChart = null

function statusLabel(status) {
  return {
    on_shelf: texts.available,
    borrowed: texts.borrowed,
    exception: texts.exception
  }[status] || status
}

function statusTagType(status) {
  return {
    on_shelf: 'success',
    borrowed: 'warning',
    exception: 'danger'
  }[status] || 'info'
}

function hotProgress(count) {
  const maxCount = hotBooks.value[0]?.borrowCount || 0
  if (!maxCount) {
    return 0
  }
  return Math.max(5, Math.round((count / maxCount) * 100))
}

async function loadStats() {
  try {
    const res = await getDashboardStats()
    const data = res.data || {}
    stats.value = {
      totalBooks: data.totalBooks ?? '-',
      totalUsers: data.totalUsers ?? '-',
      activeBorrows: data.activeBorrows ?? '-',
      overdueCount: data.overdueCount ?? '-'
    }
    hotBooks.value = data.hotBooks || []

    await nextTick()
    initBorrowTrendChart(data.borrowTrend || [])
    initCategoryChart(data.categoryStats || [])
    initHotBookChart(hotBooks.value)
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

function initHotBookChart(bookData) {
  if (!hotBookChartRef.value) return
  hotBookChart?.dispose()
  hotBookChart = echarts.init(hotBookChartRef.value)
  hotBookChart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 130, right: 30, top: 20, bottom: 20 },
    xAxis: {
      type: 'value',
      minInterval: 1
    },
    yAxis: {
      type: 'category',
      data: bookData.map(item => item.title),
      inverse: true,
      axisLabel: {
        width: 120,
        overflow: 'truncate'
      }
    },
    series: [{
      type: 'bar',
      data: bookData.map(item => item.borrowCount),
      barWidth: 18,
      label: {
        show: true,
        position: 'right',
        formatter: '{c}'
      },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [
          { offset: 0, color: '#f89898' },
          { offset: 1, color: '#f56c6c' }
        ]),
        borderRadius: [0, 9, 9, 0]
      }
    }]
  })
}

function handleResize() {
  borrowTrendChart?.resize()
  categoryChart?.resize()
  hotBookChart?.resize()
}

onMounted(() => {
  loadStats()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  borrowTrendChart?.dispose()
  categoryChart?.dispose()
  hotBookChart?.dispose()
})
</script>

<style scoped>
.stat-cards {
  margin-top: 16px;
}
.content-row {
  margin-top: 20px;
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
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.chart-container {
  width: 100%;
  height: 300px;
}
.hot-chart {
  height: 360px;
}
.hot-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.hot-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #fff5f5 0%, #fff 100%);
  border: 1px solid #fde2e2;
}
.hot-rank {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-weight: 700;
  background: #c0c4cc;
}
.rank-1 {
  background: #f56c6c;
}
.rank-2 {
  background: #e6a23c;
}
.rank-3 {
  background: #409eff;
}
.hot-main {
  flex: 1;
  min-width: 0;
}
.hot-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.hot-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}
.hot-meta {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.hot-progress-row {
  margin-top: 12px;
  display: flex;
  align-items: center;
  gap: 12px;
}
.hot-progress-row :deep(.el-progress) {
  flex: 1;
}
.hot-count {
  font-size: 13px;
  color: #606266;
  white-space: nowrap;
}
.quick-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
@media (max-width: 768px) {
  .section-header {
    align-items: flex-start;
    flex-direction: column;
  }
  .hot-title-row {
    align-items: flex-start;
    flex-direction: column;
  }
  .hot-progress-row {
    align-items: flex-start;
    flex-direction: column;
  }
}
</style>

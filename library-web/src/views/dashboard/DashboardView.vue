<template>
  <div class="dashboard">
    <h2>控制中心</h2>
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-icon" style="background: #409eff;">
            <el-icon :size="30"><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.totalBooks }}</div>
            <div class="stat-label">图书总数</div>
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
            <div class="stat-label">用户总数</div>
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
            <div class="stat-label">当前借阅</div>
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
            <div class="stat-label">逾期未还</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- ECharts 图表区 -->
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>近7天借阅趋势</template>
          <div ref="borrowTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>图书分类分布</template>
          <div ref="categoryRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card>
          <template #header>快捷操作</template>
          <div class="quick-actions">
            <el-button type="primary" @click="$router.push('/public/books')">
              <el-icon><Search /></el-icon>搜索图书
            </el-button>
            <el-button v-if="userStore.hasAccess('reader')" @click="$router.push('/reader/borrows')">
              <el-icon><Tickets /></el-icon>我的借阅
            </el-button>
            <el-button v-if="userStore.hasAccess('librarian')" type="success" @click="$router.push('/librarian/books')">
              <el-icon><Management /></el-icon>图书管理
            </el-button>
            <el-button v-if="userStore.hasAccess('system_admin')" type="danger" @click="$router.push('/admin/users')">
              <el-icon><Setting /></el-icon>系统管理
            </el-button>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>个人信息</template>
          <el-descriptions :column="1" border size="small">
            <el-descriptions-item label="用户名">{{ userStore.username }}</el-descriptions-item>
            <el-descriptions-item label="姓名">{{ userStore.realName }}</el-descriptions-item>
            <el-descriptions-item label="角色">
              <el-tag v-for="role in userStore.roles" :key="role" size="small" style="margin-right: 4px;">{{ roleLabels[role] || role }}</el-tag>
            </el-descriptions-item>
          </el-descriptions>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/user'
import { getDashboardStats } from '@/api/dashboard'

const userStore = useUserStore()

const roleLabels = {
  system_admin: '系统管理员',
  super_admin: '超级管理员',
  librarian: '馆员',
  reader: '读者',
  visitor: '访客'
}

const stats = ref({
  totalBooks: '-',
  totalUsers: '-',
  activeBorrows: '-',
  overdueCount: '-'
})

// 图表 DOM 引用
const borrowTrendRef = ref(null)
const categoryRef = ref(null)

// 图表实例
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
  } catch (e) { /* handled */ }
}

function initBorrowTrendChart(trendData) {
  if (!borrowTrendRef.value) return
  borrowTrendChart = echarts.init(borrowTrendRef.value)
  borrowTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: trendData.map(item => item.date),
      axisLabel: { fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { fontSize: 12 }
    },
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
  categoryChart = echarts.init(categoryRef.value)
  categoryChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 本 ({d}%)'
    },
    legend: {
      type: 'scroll',
      orient: 'vertical',
      right: 10,
      top: 20,
      bottom: 20,
      textStyle: { fontSize: 12 }
    },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['35%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
      label: { show: false },
      emphasis: {
        label: { show: true, fontSize: 14, fontWeight: 'bold' }
      },
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

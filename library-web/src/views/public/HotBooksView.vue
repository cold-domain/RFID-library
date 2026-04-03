<template>
  <div class="hot-books-page">
    <el-card class="hero-card" shadow="never">
      <div class="hero">
        <div>
          <div class="hero-kicker">{{ texts.kicker }}</div>
          <h2>{{ texts.title }}</h2>
          <p>{{ texts.subtitle }}</p>
        </div>
        <div class="hero-actions">
          <el-button type="primary" @click="$router.push('/public/books')">
            <el-icon><Search /></el-icon>{{ texts.goSearch }}
          </el-button>
        </div>
      </div>
    </el-card>

    <el-row :gutter="20" class="content-row">
      <el-col :xs="24" :xl="15">
        <el-card>
          <template #header>{{ texts.chartTitle }}</template>
          <div ref="chartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="9">
        <el-card>
          <template #header>{{ texts.topTen }}</template>
          <div v-if="list.length" class="ranking-list">
            <div
              v-for="book in list"
              :key="book.id"
              class="ranking-item"
            >
              <div class="rank-badge" :class="`rank-${Math.min(book.rank, 3)}`">{{ book.rank }}</div>
              <div class="rank-main">
                <div class="rank-title-row">
                  <span class="rank-title">{{ book.title }}</span>
                  <span class="rank-count">{{ book.borrowCount }}{{ texts.borrowUnit }}</span>
                </div>
                <div class="rank-meta">
                  <span>{{ book.author || texts.unknownAuthor }}</span>
                  <span>{{ book.categoryName || texts.unknownCategory }}</span>
                  <el-tag size="small" :type="statusTagType(book.status)">{{ statusLabel(book.status) }}</el-tag>
                </div>
              </div>
            </div>
          </div>
          <el-empty v-else :description="texts.empty" />
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>{{ texts.tableTitle }}</template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="rank" :label="texts.rank" width="80" />
        <el-table-column prop="title" :label="texts.bookTitle" min-width="220" />
        <el-table-column prop="author" :label="texts.author" width="160" />
        <el-table-column prop="categoryName" :label="texts.category" width="140" />
        <el-table-column prop="borrowCount" :label="texts.borrowCount" width="120" />
        <el-table-column prop="status" :label="texts.status" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getPublicHotBooks } from '@/api/publicBook'

const texts = {
  kicker: '\u501f\u9605\u70ed\u5ea6',
  title: '\u70ed\u95e8\u56fe\u4e66\u699c',
  subtitle: '\u7ed3\u5408 Redis \u5b9e\u65f6\u6392\u884c\u548c\u6570\u636e\u5e93\u7d2f\u8ba1\u501f\u9605\u6b21\u6570\uff0c\u5feb\u901f\u770b\u5230\u5f53\u524d\u6700\u53d7\u6b22\u8fce\u7684\u56fe\u4e66\u3002',
  goSearch: '\u53bb\u641c\u7d22\u56fe\u4e66',
  chartTitle: '\u70ed\u95e8\u501f\u9605\u53ef\u89c6\u5316',
  topTen: 'TOP 10',
  tableTitle: '\u699c\u5355\u660e\u7ec6',
  rank: '\u6392\u540d',
  bookTitle: '\u4e66\u540d',
  author: '\u4f5c\u8005',
  category: '\u5206\u7c7b',
  borrowCount: '\u501f\u9605\u6b21\u6570',
  borrowUnit: '\u6b21',
  status: '\u72b6\u6001',
  empty: '\u6682\u65e0\u70ed\u95e8\u501f\u9605\u6570\u636e',
  available: '\u5728\u67b6',
  borrowed: '\u5df2\u501f\u51fa',
  exception: '\u5f02\u5e38',
  unknownAuthor: '\u4f5c\u8005\u672a\u77e5',
  unknownCategory: '\u672a\u5206\u7c7b'
}

const loading = ref(false)
const list = ref([])
const chartRef = ref(null)
let chart = null

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

async function loadData() {
  loading.value = true
  try {
    const res = await getPublicHotBooks({ limit: 10 })
    list.value = res.data || []
    await nextTick()
    initChart(list.value)
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

function initChart(data) {
  if (!chartRef.value) return
  chart?.dispose()
  chart = echarts.init(chartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: 40, right: 20, top: 30, bottom: 60 },
    xAxis: {
      type: 'category',
      data: data.map(item => item.title),
      axisLabel: {
        interval: 0,
        rotate: 25
      }
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [{
      type: 'bar',
      data: data.map(item => item.borrowCount),
      barWidth: '45%',
      label: {
        show: true,
        position: 'top'
      },
      itemStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: '#ff8a7a' },
          { offset: 1, color: '#f56c6c' }
        ]),
        borderRadius: [10, 10, 0, 0]
      }
    }]
  })
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.hero-card {
  border: none;
  background:
    radial-gradient(circle at top right, rgba(245, 108, 108, 0.15), transparent 28%),
    linear-gradient(135deg, #fff6f3 0%, #fff 55%, #fff7ef 100%);
}

.hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.hero-kicker {
  color: #f56c6c;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
}

.hero h2 {
  margin: 8px 0 10px;
  font-size: 32px;
  color: #303133;
}

.hero p {
  margin: 0;
  max-width: 720px;
  color: #606266;
  line-height: 1.7;
}

.content-row,
.table-card {
  margin-top: 20px;
}

.chart-container {
  width: 100%;
  height: 380px;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.ranking-item {
  display: flex;
  gap: 14px;
  padding: 14px;
  border: 1px solid #ebeef5;
  border-radius: 12px;
  background: linear-gradient(135deg, #ffffff 0%, #fff8f7 100%);
}

.rank-badge {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
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

.rank-main {
  flex: 1;
  min-width: 0;
}

.rank-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.rank-title {
  color: #303133;
  font-weight: 600;
}

.rank-count {
  color: #f56c6c;
  font-weight: 700;
  white-space: nowrap;
}

.rank-meta {
  margin-top: 8px;
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  color: #909399;
  font-size: 13px;
}

@media (max-width: 768px) {
  .hero {
    flex-direction: column;
    align-items: flex-start;
  }

  .hero h2 {
    font-size: 26px;
  }

  .rank-title-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

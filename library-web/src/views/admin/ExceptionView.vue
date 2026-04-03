<template>
  <div class="exception-view">
    <el-row :gutter="16" class="summary-row">
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">异常总数</div><div class="summary-value">{{ overview.totalCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">今日新增</div><div class="summary-value">{{ overview.todayCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">待处理</div><div class="summary-value danger">{{ overview.pendingCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">已处理</div><div class="summary-value success">{{ overview.resolvedCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">已忽略</div><div class="summary-value">{{ overview.ignoredCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">高风险</div><div class="summary-value warning">{{ overview.highRiskCount }}</div></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>异常管理</template>

      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-select v-model="queryParams.exceptionType" placeholder="异常类型" clearable style="width: 160px;">
            <el-option label="RFID 异常" value="rfid" />
            <el-option label="业务异常" value="business" />
            <el-option label="系统异常" value="system" />
            <el-option label="安全异常" value="security" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.exceptionLevel" placeholder="异常级别" clearable style="width: 140px;">
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.resolvedStatus" placeholder="处理状态" clearable style="width: 140px;">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
            <el-option label="已忽略" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="exceptionType" label="异常类型" width="120">
          <template #default="{ row }">{{ typeLabel(row.exceptionType) }}</template>
        </el-table-column>
        <el-table-column prop="exceptionLevel" label="异常级别" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.exceptionLevel)" size="small">{{ levelLabel(row.exceptionLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionSource" label="异常来源" width="120" />
        <el-table-column prop="username" label="关联用户" width="120" />
        <el-table-column prop="exceptionContent" label="异常内容" min-width="240" show-overflow-tooltip />
        <el-table-column prop="resolvedStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.resolvedStatus)" size="small">{{ statusLabel(row.resolvedStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button
              v-if="row.resolvedStatus === 0 && userStore.hasPermission('exception:resolve')"
              type="success"
              link
              @click="openHandleDialog(row, 'resolve')"
            >
              处理
            </el-button>
            <el-button
              v-if="row.resolvedStatus === 0 && userStore.hasPermission('exception:resolve')"
              type="warning"
              link
              @click="openHandleDialog(row, 'ignore')"
            >
              忽略
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="detailVisible" title="异常详情" width="760px">
      <el-descriptions v-if="currentException" :column="2" border>
        <el-descriptions-item label="ID">{{ currentException.id }}</el-descriptions-item>
        <el-descriptions-item label="关联用户">{{ currentException.username || '匿名用户' }}</el-descriptions-item>
        <el-descriptions-item label="异常类型">{{ typeLabel(currentException.exceptionType) }}</el-descriptions-item>
        <el-descriptions-item label="异常级别">{{ levelLabel(currentException.exceptionLevel) }}</el-descriptions-item>
        <el-descriptions-item label="异常来源">{{ currentException.exceptionSource }}</el-descriptions-item>
        <el-descriptions-item label="处理状态">{{ statusLabel(currentException.resolvedStatus) }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentException.requestMethod || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ currentException.requestUrl || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentException.ipAddress || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentException.createTime }}</el-descriptions-item>
        <el-descriptions-item label="异常内容" :span="2">{{ currentException.exceptionContent }}</el-descriptions-item>
        <el-descriptions-item label="请求摘要" :span="2"><pre class="detail-block">{{ currentException.requestSummary || '暂无' }}</pre></el-descriptions-item>
        <el-descriptions-item label="异常详情" :span="2"><pre class="detail-block">{{ currentException.exceptionDetails || '暂无' }}</pre></el-descriptions-item>
        <el-descriptions-item label="堆栈摘要" :span="2"><pre class="detail-block">{{ currentException.stackTraceSummary || '暂无' }}</pre></el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2">{{ currentException.resolveNote || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间" :span="2">{{ currentException.resolveTime || '暂无' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <el-dialog v-model="handleVisible" :title="handleMode === 'resolve' ? '处理异常' : '忽略异常'" width="420px">
      <el-form label-width="90px">
        <el-form-item label="处理备注">
          <el-input v-model="resolveNote" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button :type="handleMode === 'resolve' ? 'primary' : 'warning'" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getExceptions, getExceptionOverview, getExceptionDetail, resolveException, ignoreException } from '@/api/exception'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const handleVisible = ref(false)
const currentException = ref(null)
const resolveNote = ref('')
const handleMode = ref('resolve')
const overview = reactive({ totalCount: 0, todayCount: 0, pendingCount: 0, resolvedCount: 0, ignoredCount: 0, highRiskCount: 0 })
const queryParams = reactive({ pageNum: 1, pageSize: 10, exceptionType: '', exceptionLevel: '', resolvedStatus: '' })

function typeLabel(type) {
  return { rfid: 'RFID 异常', business: '业务异常', system: '系统异常', security: '安全异常' }[type] || type
}

function levelLabel(level) {
  return { medium: '中', high: '高', critical: '严重' }[level] || level
}

function levelTagType(level) {
  return { medium: 'warning', high: 'danger', critical: 'danger' }[level] || 'info'
}

function statusLabel(status) {
  return { 0: '未处理', 1: '已处理', 2: '已忽略' }[status] || status
}

function statusTagType(status) {
  return { 0: 'danger', 1: 'success', 2: 'info' }[status] || 'info'
}

async function loadOverview() {
  try {
    const res = await getExceptionOverview()
    Object.assign(overview, res.data || {})
  } catch (e) { /* handled */ }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getExceptions(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function handleReset() {
  queryParams.exceptionType = ''
  queryParams.exceptionLevel = ''
  queryParams.resolvedStatus = ''
  queryParams.pageNum = 1
  loadData()
}

async function handleDetail(row) {
  try {
    const res = await getExceptionDetail(row.id)
    currentException.value = res.data
    detailVisible.value = true
  } catch (e) { /* handled */ }
}

function openHandleDialog(row, mode) {
  currentException.value = row
  handleMode.value = mode
  resolveNote.value = ''
  handleVisible.value = true
}

async function handleSubmit() {
  if (!resolveNote.value) {
    return ElMessage.warning('请输入处理说明')
  }
  try {
    if (handleMode.value === 'resolve') {
      await resolveException(currentException.value.id, resolveNote.value)
      ElMessage.success('异常处理成功')
    } else {
      await ignoreException(currentException.value.id, resolveNote.value)
      ElMessage.success('异常已忽略')
    }
    handleVisible.value = false
    loadOverview()
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(() => {
  loadOverview()
  loadData()
})
</script>

<style scoped>
.summary-row {
  margin-bottom: 16px;
}
.summary-title {
  color: #909399;
  font-size: 14px;
}
.summary-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 600;
  color: #303133;
}
.summary-value.success {
  color: #67c23a;
}
.summary-value.danger {
  color: #f56c6c;
}
.summary-value.warning {
  color: #e6a23c;
}
.filter-form {
  margin-bottom: 16px;
}
.detail-block {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: Consolas, Monaco, monospace;
}
</style>

<template>
  <div class="audit-log-view">
    <el-row :gutter="16" class="summary-row">
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">日志总数</div><div class="summary-value">{{ overview.totalCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">今日新增</div><div class="summary-value">{{ overview.todayCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">信息日志</div><div class="summary-value">{{ overview.successCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">错误日志</div><div class="summary-value danger">{{ overview.errorCount }}</div></el-card></el-col>
      <el-col :span="4"><el-card shadow="hover"><div class="summary-title">安全日志</div><div class="summary-value warning">{{ overview.securityCount }}</div></el-card></el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
          <div class="header-actions">
            <el-input-number v-model="retentionDays" :min="1" :max="365" controls-position="right" />
            <el-button v-if="userStore.hasPermission('audit:clean')" type="danger" @click="handleClean">清理过期日志</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" class="filter-form">
        <el-form-item>
          <el-select v-model="queryParams.operationType" placeholder="操作类型" clearable style="width: 180px;">
            <el-option v-for="item in operationOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.logLevel" placeholder="日志级别" clearable style="width: 140px;">
            <el-option label="信息" value="INFO" />
            <el-option label="错误" value="ERROR" />
            <el-option label="安全" value="SECURITY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.userId" placeholder="用户 ID" clearable style="width: 140px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户" width="120" />
        <el-table-column prop="moduleName" label="所属模块" width="120" />
        <el-table-column prop="operationType" label="操作类型" width="150">
          <template #default="{ row }">{{ operationTypeLabel(row.operationType) }}</template>
        </el-table-column>
        <el-table-column prop="logLevel" label="日志级别" width="100">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.logLevel)" size="small">{{ levelLabel(row.logLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="resultCode" label="结果码" width="90" />
        <el-table-column prop="executionTime" label="耗时(ms)" width="100" />
        <el-table-column prop="operationContent" label="操作内容" min-width="220" show-overflow-tooltip />
        <el-table-column prop="createTime" label="时间" width="170" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
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

    <el-dialog v-model="detailVisible" title="日志详情" width="720px">
      <el-descriptions v-if="currentLog" :column="2" border>
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentLog.username || '匿名用户' }}</el-descriptions-item>
        <el-descriptions-item label="所属模块">{{ currentLog.moduleName }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ operationTypeLabel(currentLog.operationType) }}</el-descriptions-item>
        <el-descriptions-item label="日志级别">{{ levelLabel(currentLog.logLevel) }}</el-descriptions-item>
        <el-descriptions-item label="结果码">{{ currentLog.resultCode }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="请求地址">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="IP 地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="耗时(ms)">{{ currentLog.executionTime || 0 }}</el-descriptions-item>
        <el-descriptions-item label="用户代理" :span="2">{{ currentLog.userAgent || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="操作内容" :span="2">{{ currentLog.operationContent || '暂无' }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2"><pre class="detail-block">{{ currentLog.requestParams || '暂无' }}</pre></el-descriptions-item>
        <el-descriptions-item label="响应摘要" :span="2"><pre class="detail-block">{{ currentLog.responseData || '暂无' }}</pre></el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2"><pre class="detail-block">{{ currentLog.errorMessage || '暂无' }}</pre></el-descriptions-item>
        <el-descriptions-item label="记录时间" :span="2">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAuditLogs, getAuditOverview, getAuditLogDetail, cleanExpiredLogs } from '@/api/auditLog'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref(null)
const retentionDays = ref(30)
const overview = reactive({ totalCount: 0, todayCount: 0, errorCount: 0, securityCount: 0, successCount: 0 })
const queryParams = reactive({ pageNum: 1, pageSize: 10, operationType: '', logLevel: '', userId: '' })

const operationOptions = [
  { label: '登录', value: 'login' },
  { label: '借书', value: 'borrow' },
  { label: '还书', value: 'return' },
  { label: '续借', value: 'renew' },
  { label: '预约', value: 'reserve' },
  { label: '新增图书', value: 'add_book' },
  { label: '编辑图书', value: 'update_book' },
  { label: '删除图书', value: 'delete_book' },
  { label: '新增用户', value: 'add_user' },
  { label: '编辑用户', value: 'update_user' },
  { label: '删除用户', value: 'delete_user' },
  { label: '修改权限', value: 'update_permission' },
  { label: '未授权访问', value: 'unauthorized' },
  { label: '拒绝访问', value: 'forbidden' }
]

function operationTypeLabel(type) {
  const match = operationOptions.find(item => item.value === type)
  return match?.label || type
}

function levelLabel(level) {
  return { INFO: '信息', ERROR: '错误', SECURITY: '安全' }[level] || level
}

function levelTagType(level) {
  return { INFO: 'info', ERROR: 'danger', SECURITY: 'warning' }[level] || 'info'
}

async function loadOverview() {
  try {
    const res = await getAuditOverview()
    Object.assign(overview, res.data || {})
  } catch (e) { /* handled */ }
}

async function loadData() {
  loading.value = true
  try {
    const res = await getAuditLogs(queryParams)
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
  queryParams.operationType = ''
  queryParams.logLevel = ''
  queryParams.userId = ''
  queryParams.pageNum = 1
  loadData()
}

async function handleDetail(row) {
  try {
    const res = await getAuditLogDetail(row.id)
    currentLog.value = res.data
    detailVisible.value = true
  } catch (e) { /* handled */ }
}

async function handleClean() {
  await ElMessageBox.confirm(`确定清理 ${retentionDays.value} 天前的审计日志吗？`, '提示', { type: 'warning' })
  try {
    await cleanExpiredLogs(retentionDays.value)
    ElMessage.success('日志清理完成')
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
.summary-value.danger {
  color: #f56c6c;
}
.summary-value.warning {
  color: #e6a23c;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
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

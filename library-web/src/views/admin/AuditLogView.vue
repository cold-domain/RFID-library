<template>
  <div class="audit-log-view">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
          <el-button type="danger" @click="handleClean">清理过期日志</el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item>
          <el-select v-model="queryParams.operationType" placeholder="操作类型" clearable style="width: 140px;">
            <el-option label="登录" value="login" />
            <el-option label="登出" value="logout" />
            <el-option label="借书" value="borrow" />
            <el-option label="还书" value="return" />
            <el-option label="续借" value="renew" />
            <el-option label="预约" value="reserve" />
            <el-option label="添加图书" value="add_book" />
            <el-option label="更新图书" value="update_book" />
            <el-option label="删除图书" value="delete_book" />
            <el-option label="添加用户" value="add_user" />
            <el-option label="更新用户" value="update_user" />
            <el-option label="删除用户" value="delete_user" />
            <el-option label="修改权限" value="update_permission" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.logLevel" placeholder="日志级别" clearable style="width: 120px;">
            <el-option label="INFO" value="INFO" />
            <el-option label="ERROR" value="ERROR" />
            <el-option label="SECURITY" value="SECURITY" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width: 120px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="operationType" label="操作类型" width="110" />
        <el-table-column prop="logLevel" label="级别" width="90">
          <template #default="{ row }">
            <el-tag :type="{ INFO: 'info', ERROR: 'danger', SECURITY: 'warning' }[row.logLevel]" size="small">{{ row.logLevel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operationContent" label="操作内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="130" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end;" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="日志详情" width="550px">
      <el-descriptions :column="1" border v-if="currentLog">
        <el-descriptions-item label="ID">{{ currentLog.id }}</el-descriptions-item>
        <el-descriptions-item label="用户">{{ currentLog.username }}</el-descriptions-item>
        <el-descriptions-item label="操作类型">{{ currentLog.operationType }}</el-descriptions-item>
        <el-descriptions-item label="级别">{{ currentLog.logLevel }}</el-descriptions-item>
        <el-descriptions-item label="操作内容">{{ currentLog.operationContent }}</el-descriptions-item>
        <el-descriptions-item label="IP地址">{{ currentLog.ipAddress }}</el-descriptions-item>
        <el-descriptions-item label="请求URL">{{ currentLog.requestUrl }}</el-descriptions-item>
        <el-descriptions-item label="请求方法">{{ currentLog.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="时间">{{ currentLog.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAuditLogs, getAuditLogDetail, cleanExpiredLogs } from '@/api/auditLog'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const currentLog = ref(null)
const queryParams = reactive({ pageNum: 1, pageSize: 10, operationType: '', logLevel: '', userId: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getAuditLogs(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }

async function handleDetail(row) {
  try {
    const res = await getAuditLogDetail(row.id)
    currentLog.value = res.data
    detailVisible.value = true
  } catch (e) { /* handled */ }
}

async function handleClean() {
  await ElMessageBox.confirm('确定清理超过30天的日志吗？', '提示', { type: 'warning' })
  try {
    await cleanExpiredLogs(30)
    ElMessage.success('清理完成')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

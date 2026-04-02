<template>
  <div class="exception-view">
    <el-card>
      <template #header>异常管理</template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item>
          <el-select v-model="queryParams.exceptionType" placeholder="异常类型" clearable style="width: 140px;">
            <el-option label="RFID异常" value="rfid" />
            <el-option label="系统异常" value="system" />
            <el-option label="安全异常" value="security" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.exceptionLevel" placeholder="异常级别" clearable style="width: 120px;">
            <el-option label="低" value="low" />
            <el-option label="中" value="medium" />
            <el-option label="高" value="high" />
            <el-option label="严重" value="critical" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.resolvedStatus" placeholder="处理状态" clearable style="width: 120px;">
            <el-option label="未处理" :value="0" />
            <el-option label="已处理" :value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="exceptionType" label="类型" width="100" />
        <el-table-column prop="exceptionLevel" label="级别" width="80">
          <template #default="{ row }">
            <el-tag :type="{ low: 'info', medium: 'warning', high: 'danger', critical: 'danger' }[row.exceptionLevel]" size="small">
              {{ { low: '低', medium: '中', high: '高', critical: '严重' }[row.exceptionLevel] || row.exceptionLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="exceptionContent" label="异常内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="resolvedStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.resolvedStatus === 1 ? 'success' : 'danger'" size="small">{{ row.resolvedStatus === 1 ? '已处理' : '未处理' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="row.resolvedStatus === 0" type="success" link @click="openResolveDialog(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end;" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="异常详情" width="550px">
      <el-descriptions :column="1" border v-if="currentException">
        <el-descriptions-item label="ID">{{ currentException.id }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ currentException.exceptionType }}</el-descriptions-item>
        <el-descriptions-item label="级别">{{ currentException.exceptionLevel }}</el-descriptions-item>
        <el-descriptions-item label="内容">{{ currentException.exceptionContent }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ currentException.resolvedStatus === 1 ? '已处理' : '未处理' }}</el-descriptions-item>
        <el-descriptions-item label="处理备注">{{ currentException.resolveNote || '无' }}</el-descriptions-item>
        <el-descriptions-item label="处理时间">{{ currentException.resolveTime || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentException.createTime }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 处理对话框 -->
    <el-dialog v-model="resolveVisible" title="处理异常" width="400px">
      <el-form label-width="80px">
        <el-form-item label="处理备注">
          <el-input v-model="resolveNote" type="textarea" :rows="3" placeholder="请输入处理说明" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resolveVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResolve">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getExceptions, getExceptionDetail, resolveException } from '@/api/exception'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const resolveVisible = ref(false)
const currentException = ref(null)
const resolveNote = ref('')
const queryParams = reactive({ pageNum: 1, pageSize: 10, exceptionType: '', exceptionLevel: '', resolvedStatus: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getExceptions(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }

async function handleDetail(row) {
  try {
    const res = await getExceptionDetail(row.id)
    currentException.value = res.data
    detailVisible.value = true
  } catch (e) { /* handled */ }
}

function openResolveDialog(row) {
  currentException.value = row
  resolveNote.value = ''
  resolveVisible.value = true
}

async function handleResolve() {
  if (!resolveNote.value) return ElMessage.warning('请输入处理说明')
  try {
    await resolveException(currentException.value.id, resolveNote.value)
    ElMessage.success('处理成功')
    resolveVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

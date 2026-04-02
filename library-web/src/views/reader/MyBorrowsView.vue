<template>
  <div class="my-borrows">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的借阅</span>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px;" @change="loadData">
            <el-option label="借阅中" value="borrowed" />
            <el-option label="已归还" value="returned" />
            <el-option label="已逾期" value="overdue" />
          </el-select>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="bookTitle" label="图书名称" min-width="150" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="borrowDate" label="借阅日期" width="110" />
        <el-table-column prop="dueDate" label="应还日期" width="110" />
        <el-table-column prop="returnDate" label="归还日期" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'borrowed'" type="primary" link @click="handleRenew(row)">续借</el-button>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMyBorrows, renewBorrow } from '@/api/reader'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, status: '' })

function statusType(s) {
  return { borrowed: 'primary', returned: 'success', overdue: 'danger' }[s] || 'info'
}
function statusLabel(s) {
  return { borrowed: '借阅中', returned: '已归还', overdue: '已逾期' }[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMyBorrows(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function handleRenew(row) {
  await ElMessageBox.confirm('确定要续借该图书吗？', '提示')
  try {
    await renewBorrow(row.id)
    ElMessage.success('续借成功')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

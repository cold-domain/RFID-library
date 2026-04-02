<template>
  <div class="my-reservations">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的预约</span>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px;" @change="loadData">
            <el-option label="待取书" value="pending" />
            <el-option label="可取书" value="available" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="bookTitle" label="图书名称" min-width="150" />
        <el-table-column prop="reserveDate" label="预约日期" width="110" />
        <el-table-column prop="expireDate" label="过期日期" width="110" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending' || row.status === 'available'" type="danger" link @click="handleCancel(row)">取消</el-button>
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
import { getMyReservations, cancelReservation } from '@/api/reader'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, status: '' })

function statusType(s) {
  return { pending: 'warning', available: '', completed: 'success', cancelled: 'info', expired: 'danger' }[s] || 'info'
}
function statusLabel(s) {
  return { pending: '待取书', available: '可取书', completed: '已完成', cancelled: '已取消', expired: '已过期' }[s] || s
}

async function loadData() {
  loading.value = true
  try {
    const res = await getMyReservations(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function handleCancel(row) {
  await ElMessageBox.confirm('确定取消该预约吗？', '提示')
  try {
    await cancelReservation(row.id)
    ElMessage.success('已取消预约')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

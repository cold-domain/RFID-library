<template>
  <div class="reservation-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>预约管理</span>
          <el-button type="warning" @click="handleCheckExpired">处理过期预约</el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item>
          <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px;">
            <el-option label="待取书" value="pending" />
            <el-option label="可取书" value="available" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
            <el-option label="已过期" value="expired" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="bookTitle" label="图书" min-width="150" />
        <el-table-column prop="reserveDate" label="预约日期" width="110" />
        <el-table-column prop="expireDate" label="过期日期" width="110" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="{ pending: 'warning', available: '', completed: 'success', cancelled: 'info', expired: 'danger' }[row.status]" size="small">
              {{ { pending: '待取书', available: '可取书', completed: '已完成', cancelled: '已取消', expired: '已过期' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'pending' || row.status === 'available'" type="success" link @click="handlePickup(row)">确认取书</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end;" @size-change="loadData" @current-change="loadData" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getReservations, confirmPickup, checkExpired } from '@/api/reservation'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, userId: '', status: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getReservations(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }

async function handlePickup(row) {
  await ElMessageBox.confirm('确认该读者已取书吗？', '提示')
  try {
    await confirmPickup(row.id)
    ElMessage.success('取书确认成功')
    loadData()
  } catch (e) { /* handled */ }
}

async function handleCheckExpired() {
  try {
    await checkExpired()
    ElMessage.success('过期处理完成')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

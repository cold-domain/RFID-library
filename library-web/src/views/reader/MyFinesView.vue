<template>
  <div class="my-fines">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>我的罚款</span>
          <el-select v-model="queryParams.fineStatus" placeholder="全部状态" clearable style="width: 150px;" @change="loadData">
            <el-option label="未缴纳" value="unpaid" />
            <el-option label="已缴纳" value="paid" />
            <el-option label="已减免" value="waived" />
          </el-select>
        </div>
      </template>
      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="bookTitle" label="图书名称" min-width="150" />
        <el-table-column prop="fineType" label="罚款类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">
              {{ { overdue: '逾期罚款', damage: '损坏赔偿', lost: '丢失赔偿' }[row.fineType] || row.fineType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="罚款金额" width="100">
          <template #default="{ row }">￥{{ row.amount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="paidAmount" label="已缴金额" width="100">
          <template #default="{ row }">￥{{ row.paidAmount?.toFixed(2) || '0.00' }}</template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="90" />
        <el-table-column prop="fineStatus" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="{ unpaid: 'danger', paid: 'success', waived: 'info' }[row.fineStatus]" size="small">
              {{ { unpaid: '未缴纳', paid: '已缴纳', waived: '已减免' }[row.fineStatus] || row.fineStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.fineStatus === 'unpaid'" type="success" link @click="openPayDialog(row)">缴费</el-button>
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

    <!-- 缴费对话框 -->
    <el-dialog v-model="payVisible" title="缴费" width="350px">
      <el-form label-width="80px">
        <el-form-item label="图书">{{ currentFine?.bookTitle || '-' }}</el-form-item>
        <el-form-item label="应缴金额">￥{{ remainAmount.toFixed(2) }}</el-form-item>
        <el-form-item label="缴费金额">
          <el-input-number v-model="payAmount" :min="0.01" :max="remainAmount" :precision="2" style="width:100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePay">确认缴费</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { getMyFines, payMyFine } from '@/api/reader'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const payVisible = ref(false)
const currentFine = ref(null)
const payAmount = ref(0)
const queryParams = reactive({ pageNum: 1, pageSize: 10, fineStatus: '' })

const remainAmount = computed(() => {
  if (!currentFine.value) return 0
  return (currentFine.value.amount || 0) - (currentFine.value.paidAmount || 0)
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyFines(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function openPayDialog(row) {
  currentFine.value = row
  payAmount.value = (row.amount || 0) - (row.paidAmount || 0)
  payVisible.value = true
}

async function handlePay() {
  try {
    await payMyFine(currentFine.value.id, payAmount.value)
    ElMessage.success('缴费成功')
    payVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

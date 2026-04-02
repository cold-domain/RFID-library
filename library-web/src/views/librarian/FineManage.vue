<template>
  <div class="fine-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>罚款管理</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>创建罚款
          </el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item>
          <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.fineStatus" placeholder="全部状态" clearable style="width: 130px;">
            <el-option label="未缴纳" value="unpaid" />
            <el-option label="已缴纳" value="paid" />
            <el-option label="已减免" value="waived" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" label="用户" width="100" />
        <el-table-column prop="bookTitle" label="图书名称" min-width="150" />
        <el-table-column prop="fineType" label="罚款类型" width="100">
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
        <el-table-column prop="fineStatus" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="{ unpaid: 'danger', paid: 'success', waived: 'info' }[row.fineStatus]" size="small">
              {{ { unpaid: '未缴纳', paid: '已缴纳', waived: '已减免' }[row.fineStatus] || row.fineStatus }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.fineStatus === 'unpaid'">
              <el-button type="success" link @click="openPayDialog(row)">缴费</el-button>
              <el-button type="warning" link @click="openWaiveDialog(row)">减免</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end;" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <!-- 创建罚款对话框 -->
    <el-dialog v-model="createVisible" title="创建罚款" width="450px">
      <el-form ref="createRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="createForm.userId" type="number" />
        </el-form-item>
        <el-form-item label="借阅记录ID" prop="borrowRecordId">
          <el-input v-model="createForm.borrowRecordId" type="number" />
        </el-form-item>
        <el-form-item label="罚款类型" prop="fineType">
          <el-select v-model="createForm.fineType" style="width:100%;">
            <el-option label="逾期罚款" value="overdue" />
            <el-option label="损坏赔偿" value="damage" />
            <el-option label="丢失赔偿" value="lost" />
          </el-select>
        </el-form-item>
        <el-form-item label="金额" prop="amount">
          <el-input-number v-model="createForm.amount" :min="0" :precision="2" style="width:100%;" />
        </el-form-item>
        <el-form-item label="逾期天数">
          <el-input-number v-model="createForm.overdueDays" :min="0" style="width:100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 缴费对话框 -->
    <el-dialog v-model="payVisible" title="缴费" width="350px">
      <el-form label-width="80px">
        <el-form-item label="应缴金额">￥{{ currentFine?.amount?.toFixed(2) }}</el-form-item>
        <el-form-item label="缴费金额">
          <el-input-number v-model="payAmount" :min="0" :max="currentFine?.amount" :precision="2" style="width:100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="payVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePay">确认缴费</el-button>
      </template>
    </el-dialog>

    <!-- 减免对话框 -->
    <el-dialog v-model="waiveVisible" title="罚款减免" width="400px">
      <el-form label-width="80px">
        <el-form-item label="减免原因">
          <el-input v-model="waiveReason" type="textarea" :rows="3" placeholder="请输入减免原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="waiveVisible = false">取消</el-button>
        <el-button type="primary" @click="handleWaive">确认减免</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFines, createFine, payFine, waiveFine } from '@/api/fine'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const createVisible = ref(false)
const payVisible = ref(false)
const waiveVisible = ref(false)
const createRef = ref(null)
const currentFine = ref(null)
const payAmount = ref(0)
const waiveReason = ref('')

const queryParams = reactive({ pageNum: 1, pageSize: 10, userId: '', fineStatus: '' })
const createForm = reactive({ userId: '', borrowRecordId: '', fineType: 'overdue', amount: 0, overdueDays: 0 })
const createRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  borrowRecordId: [{ required: true, message: '请输入借阅记录ID', trigger: 'blur' }],
  fineType: [{ required: true, message: '请选择罚款类型', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getFines(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }

function openCreateDialog() {
  Object.assign(createForm, { userId: '', borrowRecordId: '', fineType: 'overdue', amount: 0, overdueDays: 0 })
  createVisible.value = true
}

async function handleCreate() {
  const valid = await createRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await createFine(createForm)
    ElMessage.success('罚款创建成功')
    createVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

function openPayDialog(row) {
  currentFine.value = row
  payAmount.value = row.amount - (row.paidAmount || 0)
  payVisible.value = true
}

async function handlePay() {
  try {
    await payFine(currentFine.value.id, payAmount.value)
    ElMessage.success('缴费成功')
    payVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

function openWaiveDialog(row) {
  currentFine.value = row
  waiveReason.value = ''
  waiveVisible.value = true
}

async function handleWaive() {
  if (!waiveReason.value) return ElMessage.warning('请输入减免原因')
  try {
    await waiveFine(currentFine.value.id, waiveReason.value)
    ElMessage.success('减免成功')
    waiveVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

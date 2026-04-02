<template>
  <div class="borrow-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>借阅管理</span>
          <div>
            <el-button type="primary" @click="borrowDialogVisible = true">
              <el-icon><Plus /></el-icon>办理借书
            </el-button>
            <el-button type="warning" @click="handleCheckOverdue">检查逾期</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item>
          <el-input v-model="queryParams.userId" placeholder="用户ID" clearable style="width: 150px;" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 130px;">
            <el-option label="借阅中" value="borrowed" />
            <el-option label="已归还" value="returned" />
            <el-option label="已逾期" value="overdue" />
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
        <el-table-column prop="borrowDate" label="借阅日期" width="110" />
        <el-table-column prop="dueDate" label="应还日期" width="110" />
        <el-table-column prop="returnDate" label="归还日期" width="110" />
        <el-table-column prop="renewCount" label="续借次数" width="80" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="{ borrowed: 'primary', returned: 'success', overdue: 'danger' }[row.status]" size="small">
              {{ { borrowed: '借阅中', returned: '已归还', overdue: '已逾期' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <template v-if="row.status === 'borrowed' || row.status === 'overdue'">
              <el-button type="success" link @click="handleReturn(row)">还书</el-button>
              <el-button type="primary" link @click="handleRenew(row)">续借</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end;" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <!-- 借书对话框 -->
    <el-dialog v-model="borrowDialogVisible" title="办理借书" width="750px" @open="handleBorrowDialogOpen">
      <el-form ref="borrowFormRef" :model="borrowForm" :rules="borrowRules" label-width="80px">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="borrowForm.userId" type="number" placeholder="请输入借阅用户ID" />
        </el-form-item>
        <el-form-item label="选择图书" prop="bookId">
          <div style="width: 100%;">
            <div style="display: flex; gap: 8px; margin-bottom: 10px;">
              <el-input v-model="bookSearchKeyword" placeholder="书名/作者/ISBN" clearable style="flex: 1;" @keyup.enter="handleBookSearch" />
              <el-button type="primary" @click="handleBookSearch">搜索图书</el-button>
            </div>
            <el-table :data="bookSearchList" v-loading="bookSearchLoading" size="small" max-height="250" highlight-current-row @current-change="handleBookSelect" style="width: 100%;">
              <el-table-column width="55">
                <template #default="{ row }">
                  <el-radio :label="row.id" v-model="borrowForm.bookId">{{ '' }}</el-radio>
                </template>
              </el-table-column>
              <el-table-column prop="id" label="ID" width="55" />
              <el-table-column prop="title" label="书名" min-width="130" show-overflow-tooltip />
              <el-table-column prop="author" label="作者" width="90" show-overflow-tooltip />
              <el-table-column prop="isbn" label="ISBN" width="130" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="{ row }">
                  <el-tag :type="{ on_shelf: 'success', borrowed: 'warning', exception: 'danger' }[row.status]" size="small">
                    {{ { on_shelf: '在架', borrowed: '已借出', exception: '异常' }[row.status] || row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination v-if="bookSearchTotal > 0" v-model:current-page="bookSearchPage" :page-size="5" :total="bookSearchTotal" layout="total, prev, pager, next" size="small" style="margin-top: 8px; justify-content: flex-end;" @current-change="loadBookSearch" />
            <div v-if="borrowForm.bookId" style="margin-top: 8px; color: #409eff; font-size: 13px;">
              已选择图书ID: {{ borrowForm.bookId }}
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="borrowDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleBorrow">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBorrowRecords, borrowBook, returnBook, renewBorrow, checkOverdue, searchBooksForBorrow } from '@/api/borrow'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const borrowDialogVisible = ref(false)
const borrowFormRef = ref(null)

const queryParams = reactive({ pageNum: 1, pageSize: 10, userId: '', status: '' })
const borrowForm = reactive({ userId: '', bookId: '' })
const borrowRules = {
  userId: [{ required: true, message: '请输入用户ID', trigger: 'blur' }],
  bookId: [{ required: true, message: '请选择图书', trigger: 'change' }]
}

// 图书搜索相关
const bookSearchKeyword = ref('')
const bookSearchList = ref([])
const bookSearchTotal = ref(0)
const bookSearchPage = ref(1)
const bookSearchLoading = ref(false)

async function loadData() {
  loading.value = true
  try {
    const res = await getBorrowRecords(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }

async function handleBorrow() {
  const valid = await borrowFormRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await borrowBook(borrowForm)
    ElMessage.success('借书成功')
    borrowDialogVisible.value = false
    borrowForm.userId = ''
    borrowForm.bookId = ''
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

function handleBorrowDialogOpen() {
  bookSearchKeyword.value = ''
  bookSearchList.value = []
  bookSearchTotal.value = 0
  bookSearchPage.value = 1
  borrowForm.userId = ''
  borrowForm.bookId = ''
}

async function loadBookSearch() {
  bookSearchLoading.value = true
  try {
    const res = await searchBooksForBorrow({ pageNum: bookSearchPage.value, pageSize: 5, keyword: bookSearchKeyword.value })
    bookSearchList.value = res.data.records || res.data.list || []
    bookSearchTotal.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { bookSearchLoading.value = false }
}

function handleBookSearch() {
  bookSearchPage.value = 1
  loadBookSearch()
}

function handleBookSelect(row) {
  if (row) {
    borrowForm.bookId = row.id
  }
}

async function handleReturn(row) {
  await ElMessageBox.confirm('确定办理还书吗？', '提示')
  try {
    await returnBook(row.id)
    ElMessage.success('还书成功')
    loadData()
  } catch (e) { /* handled */ }
}

async function handleRenew(row) {
  await ElMessageBox.confirm('确定续借吗？', '提示')
  try {
    await renewBorrow(row.id)
    ElMessage.success('续借成功')
    loadData()
  } catch (e) { /* handled */ }
}

async function handleCheckOverdue() {
  try {
    await checkOverdue()
    ElMessage.success('逾期检查完成')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

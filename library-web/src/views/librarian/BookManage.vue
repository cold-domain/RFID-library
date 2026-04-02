<template>
  <div class="book-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>图书管理</span>
          <el-button type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增图书
          </el-button>
        </div>
      </template>

      <el-form :inline="true" @submit.prevent="handleSearch" style="margin-bottom: 16px;">
        <el-form-item>
          <el-input v-model="queryParams.keyword" placeholder="书名/作者/ISBN" clearable style="width: 200px;" />
        </el-form-item>
        <el-form-item>
          <el-tree-select v-model="queryParams.categoryId" :data="categoryTree" :props="treeProp" placeholder="分类" clearable check-strictly style="width: 160px;" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px;">
            <el-option label="在架" value="on_shelf" />
            <el-option label="已借出" value="borrowed" />
            <el-option label="异常" value="exception" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="书名" min-width="150" />
        <el-table-column prop="author" label="作者" width="100" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="publisher" label="出版社" width="120" />
        <el-table-column prop="categoryName" label="分类" width="90" />
        <el-table-column prop="rfidTag" label="RFID标签" width="130" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 'on_shelf' ? 'success' : row.status === 'borrowed' ? 'warning' : 'danger'" size="small">
              {{ { on_shelf: '在架', borrowed: '已借出', exception: '异常' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button type="success" link @click="openRfidDialog(row)">RFID</el-button>
            <el-button type="warning" link @click="openHistoryDialog(row)">历史</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="queryParams.pageNum" v-model:page-size="queryParams.pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" style="margin-top:16px;justify-content:flex-end;" @size-change="loadData" @current-change="loadData" />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑图书' : '新增图书'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="书名" prop="title">
          <el-input v-model="form.title" />
        </el-form-item>
        <el-form-item label="作者" prop="author">
          <el-input v-model="form.author" />
        </el-form-item>
        <el-form-item label="ISBN" prop="isbn">
          <el-input v-model="form.isbn" />
        </el-form-item>
        <el-form-item label="出版社" prop="publisher">
          <el-input v-model="form.publisher" />
        </el-form-item>
        <el-form-item label="出版日期">
          <el-date-picker v-model="form.publishDate" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-tree-select v-model="form.categoryId" :data="categoryTree" :props="treeProp" placeholder="选择分类" check-strictly style="width:100%;" />
        </el-form-item>
        <el-form-item label="馆藏位置">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- RFID管理对话框 -->
    <el-dialog v-model="rfidVisible" title="RFID标签管理" width="450px">
      <div v-if="currentBook">
        <p>图书：{{ currentBook.title }}</p>
        <p>当前RFID：{{ currentBook.rfidTag || '未绑定' }}</p>
        <el-divider />
        <el-form v-if="!currentBook.rfidTag" :inline="true">
          <el-form-item>
            <el-input v-model="rfidTag" placeholder="输入RFID标签号" style="width:250px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleBindRfid">绑定</el-button>
          </el-form-item>
        </el-form>
        <el-button v-else type="danger" @click="handleUnbindRfid">解绑RFID</el-button>
      </div>
    </el-dialog>

    <!-- 历史记录对话框 -->
    <el-dialog v-model="historyVisible" title="历史记录" width="650px">
      <el-tabs>
        <el-tab-pane label="RFID绑定历史">
          <el-table :data="rfidHistory" size="small">
            <el-table-column prop="rfidTag" label="RFID标签" />
            <el-table-column prop="operationType" label="操作" />
            <el-table-column prop="operationTime" label="时间" />
            <el-table-column prop="operatorName" label="操作人" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="状态变更历史">
          <el-table :data="statusHistory" size="small">
            <el-table-column prop="oldStatus" label="原状态" />
            <el-table-column prop="newStatus" label="新状态" />
            <el-table-column prop="changeTime" label="变更时间" />
            <el-table-column prop="operatorName" label="操作人" />
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getBooks, createBook, updateBook, deleteBook, bindRfid, unbindRfid, getRfidHistory, getStatusHistory } from '@/api/book'
import { getCategoryTree } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

const treeProp = { label: 'name', value: 'id', children: 'children' }
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const categoryTree = ref([])
const dialogVisible = ref(false)
const rfidVisible = ref(false)
const historyVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const currentBook = ref(null)
const rfidTag = ref('')
const rfidHistory = ref([])
const statusHistory = ref([])

const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: '', status: '' })

const defaultForm = { title: '', author: '', isbn: '', publisher: '', publishDate: '', categoryId: '', location: '', description: '' }
const form = reactive({ ...defaultForm })

const rules = {
  title: [{ required: true, message: '请输入书名', trigger: 'blur' }],
  author: [{ required: true, message: '请输入作者', trigger: 'blur' }],
  isbn: [{ required: true, message: '请输入ISBN', trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getBooks(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function loadCategories() {
  try {
    const res = await getCategoryTree()
    categoryTree.value = res.data || []
  } catch (e) { /* handled */ }
}

function handleSearch() { queryParams.pageNum = 1; loadData() }
function resetSearch() { Object.assign(queryParams, { pageNum: 1, keyword: '', categoryId: '', status: '' }); loadData() }

function openDialog(row) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row } : { ...defaultForm })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateBook(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createBook(form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除图书「${row.title}」吗？`, '提示', { type: 'warning' })
  try {
    await deleteBook(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* handled */ }
}

function openRfidDialog(row) {
  currentBook.value = { ...row }
  rfidTag.value = ''
  rfidVisible.value = true
}

async function handleBindRfid() {
  if (!rfidTag.value) return ElMessage.warning('请输入RFID标签号')
  try {
    await bindRfid(currentBook.value.id, rfidTag.value)
    ElMessage.success('绑定成功')
    rfidVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

async function handleUnbindRfid() {
  await ElMessageBox.confirm('确定解绑RFID标签吗？', '提示', { type: 'warning' })
  try {
    await unbindRfid(currentBook.value.id)
    ElMessage.success('解绑成功')
    rfidVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
}

async function openHistoryDialog(row) {
  currentBook.value = row
  historyVisible.value = true
  try {
    const [rfidRes, statusRes] = await Promise.all([getRfidHistory(row.id), getStatusHistory(row.id)])
    rfidHistory.value = rfidRes.data || []
    statusHistory.value = statusRes.data || []
  } catch (e) { /* handled */ }
}

onMounted(() => { loadData(); loadCategories() })
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

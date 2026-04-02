<template>
  <div class="book-search">
    <el-card>
      <template #header>图书搜索</template>
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item>
          <el-input v-model="queryParams.keyword" placeholder="书名/作者/ISBN" clearable style="width: 250px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-tree-select
            v-model="queryParams.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="选择分类"
            clearable
            check-strictly
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" label="书名" min-width="180" />
        <el-table-column prop="author" label="作者" width="120" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="publisher" label="出版社" width="140" />
        <el-table-column prop="categoryName" label="分类" width="100" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 'on_shelf' ? 'success' : row.status === 'borrowed' ? 'warning' : 'danger'">
              {{ { on_shelf: '在架', borrowed: '已借出', exception: '异常' }[row.status] || row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">详情</el-button>
            <el-button v-if="userStore.hasAccess('reader')" type="warning" link @click="handleReserve(row)">预约</el-button>
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

    <!-- 图书详情对话框 -->
    <el-dialog v-model="detailVisible" title="图书详情" width="500px">
      <el-descriptions :column="1" border v-if="currentBook">
        <el-descriptions-item label="书名">{{ currentBook.title }}</el-descriptions-item>
        <el-descriptions-item label="作者">{{ currentBook.author }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ currentBook.isbn }}</el-descriptions-item>
        <el-descriptions-item label="出版社">{{ currentBook.publisher }}</el-descriptions-item>
        <el-descriptions-item label="出版日期">{{ currentBook.publishDate }}</el-descriptions-item>
        <el-descriptions-item label="分类">{{ currentBook.categoryName }}</el-descriptions-item>
        <el-descriptions-item label="馆藏位置">{{ currentBook.location }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ { on_shelf: '在架', borrowed: '已借出', exception: '异常' }[currentBook.status] || currentBook.status }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ currentBook.description || '暂无' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer v-if="userStore.hasAccess('reader')">
        <el-button type="warning" @click="handleReserve(currentBook)">预约此书</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getPublicBooks, getPublicBookDetail, getPublicCategories } from '@/api/publicBook'
import { reserveBook } from '@/api/reader'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categoryTree = ref([])
const detailVisible = ref(false)
const currentBook = ref(null)

const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: '' })

async function loadData() {
  loading.value = true
  try {
    const res = await getPublicBooks(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function loadCategories() {
  try {
    const res = await getPublicCategories()
    categoryTree.value = res.data || []
  } catch (e) { /* handled */ }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

async function handleDetail(row) {
  try {
    const res = await getPublicBookDetail(row.id)
    currentBook.value = res.data
    detailVisible.value = true
  } catch (e) { /* handled */ }
}

async function handleReserve(book) {
  if (!book) return
  await ElMessageBox.confirm(`确定预约《${book.title}》吗？`, '预约确认')
  try {
    await reserveBook(book.id)
    ElMessage.success('预约成功')
    detailVisible.value = false
  } catch (e) { /* handled */ }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

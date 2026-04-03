<template>
  <div class="book-search">
    <el-card>
      <template #header>{{ texts.pageTitle }}</template>
      <el-form :inline="true" @submit.prevent="handleSearch">
        <el-form-item>
          <el-input
            v-model="queryParams.keyword"
            :placeholder="texts.keywordPlaceholder"
            clearable
            style="width: 250px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-tree-select
            v-model="queryParams.categoryId"
            :data="categoryTree"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            :placeholder="texts.categoryPlaceholder"
            clearable
            check-strictly
            style="width: 200px;"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>{{ texts.search }}
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="title" :label="texts.title" min-width="180" />
        <el-table-column prop="author" :label="texts.author" width="120" />
        <el-table-column prop="isbn" label="ISBN" width="140" />
        <el-table-column prop="publisher" :label="texts.publisher" width="140" />
        <el-table-column prop="categoryName" :label="texts.category" width="120" />
        <el-table-column prop="status" :label="texts.status" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="texts.operation" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">{{ texts.detail }}</el-button>
            <el-button
              v-if="userStore.hasPermission('reader:reservations')"
              type="warning"
              link
              @click="handleReserve(row)"
            >
              {{ texts.reserve }}
            </el-button>
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

    <el-dialog v-model="detailVisible" :title="texts.detailTitle" width="560px">
      <el-descriptions v-if="currentBook" :column="1" border>
        <el-descriptions-item :label="texts.title">{{ currentBook.title }}</el-descriptions-item>
        <el-descriptions-item :label="texts.author">{{ currentBook.author }}</el-descriptions-item>
        <el-descriptions-item label="ISBN">{{ currentBook.isbn }}</el-descriptions-item>
        <el-descriptions-item :label="texts.publisher">{{ currentBook.publisher }}</el-descriptions-item>
        <el-descriptions-item :label="texts.publishDate">{{ currentBook.publishDate }}</el-descriptions-item>
        <el-descriptions-item :label="texts.category">{{ currentBook.categoryName }}</el-descriptions-item>
        <el-descriptions-item :label="texts.location">{{ currentBook.location }}</el-descriptions-item>
        <el-descriptions-item :label="texts.status">{{ statusLabel(currentBook.status) }}</el-descriptions-item>
        <el-descriptions-item :label="texts.description">{{ currentBook.description || texts.none }}</el-descriptions-item>
      </el-descriptions>
      <template #footer v-if="userStore.hasPermission('reader:reservations')">
        <el-button type="warning" @click="handleReserve(currentBook)">{{ texts.reserveThisBook }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPublicBooks, getPublicBookDetail, getPublicCategories } from '@/api/publicBook'
import { reserveBook } from '@/api/reader'
import { useUserStore } from '@/store/user'

const texts = {
  pageTitle: '\u56fe\u4e66\u641c\u7d22',
  keywordPlaceholder: '\u4e66\u540d / \u4f5c\u8005 / ISBN',
  categoryPlaceholder: '\u9009\u62e9\u5206\u7c7b',
  search: '\u641c\u7d22',
  title: '\u4e66\u540d',
  author: '\u4f5c\u8005',
  publisher: '\u51fa\u7248\u793e',
  category: '\u5206\u7c7b',
  status: '\u72b6\u6001',
  operation: '\u64cd\u4f5c',
  detail: '\u8be6\u60c5',
  reserve: '\u9884\u7ea6',
  detailTitle: '\u56fe\u4e66\u8be6\u60c5',
  publishDate: '\u51fa\u7248\u65e5\u671f',
  location: '\u9986\u85cf\u4f4d\u7f6e',
  description: '\u7b80\u4ecb',
  none: '\u6682\u65e0',
  reserveThisBook: '\u9884\u7ea6\u6b64\u4e66',
  reserveConfirm: '\u9884\u7ea6\u786e\u8ba4',
  reserveSuccess: '\u9884\u7ea6\u6210\u529f',
  available: '\u5728\u67b6',
  borrowed: '\u5df2\u501f\u51fa',
  exception: '\u5f02\u5e38'
}

const userStore = useUserStore()
const loading = ref(false)
const list = ref([])
const total = ref(0)
const categoryTree = ref([])
const detailVisible = ref(false)
const currentBook = ref(null)
const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: '' })

function statusLabel(status) {
  return {
    on_shelf: texts.available,
    borrowed: texts.borrowed,
    exception: texts.exception
  }[status] || status
}

function statusTagType(status) {
  return {
    on_shelf: 'success',
    borrowed: 'warning',
    exception: 'danger'
  }[status] || 'info'
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPublicBooks(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function loadCategories() {
  try {
    const res = await getPublicCategories()
    categoryTree.value = res.data || []
  } catch (e) {
    // handled by interceptor
  }
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
  } catch (e) {
    // handled by interceptor
  }
}

async function handleReserve(book) {
  if (!book) return
  await ElMessageBox.confirm(`\u786e\u5b9a\u9884\u7ea6\u300a${book.title}\u300b\u5417\uff1f`, texts.reserveConfirm, { type: 'warning' })
  try {
    await reserveBook(book.id)
    ElMessage.success(texts.reserveSuccess)
    detailVisible.value = false
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  loadData()
  loadCategories()
})
</script>

<template>
  <div class="book-search">
    <el-card class="hot-recommend-card" shadow="never">
      <div class="hot-recommend-header">
        <div>
          <div class="hot-kicker">{{ texts.hotKicker }}</div>
          <h3>{{ texts.hotTitle }}</h3>
          <p>{{ texts.hotSubtitle }}</p>
        </div>
        <el-button type="danger" plain @click="goToHotBooks">
          {{ texts.viewHotRanking }}
        </el-button>
      </div>
      <div v-loading="hotLoading" class="hot-recommend-grid">
        <div
          v-for="book in hotBooks.slice(0, 4)"
          :key="book.id"
          class="hot-book-card"
          @click="handleHotDetail(book)"
        >
          <div class="hot-book-rank">TOP {{ book.rank }}</div>
          <div class="hot-book-title">{{ book.title }}</div>
          <div class="hot-book-meta">{{ book.author || texts.unknownAuthor }}</div>
          <div class="hot-book-meta">{{ book.categoryName || texts.unknownCategory }}</div>
          <div class="hot-book-footer">
            <el-tag size="small" :type="statusTagType(book.status)">{{ statusLabel(book.status) }}</el-tag>
            <span class="hot-book-count">{{ book.borrowCount }} {{ texts.hotBorrowUnit }}</span>
          </div>
        </div>
      </div>
    </el-card>

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
        <el-form-item>
          <el-button type="danger" plain @click="goToHotBooks">
            {{ texts.viewHotRanking }}
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
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPublicBooks, getPublicBookDetail, getPublicCategories, getPublicHotBooks } from '@/api/publicBook'
import { reserveBook } from '@/api/reader'
import { useUserStore } from '@/store/user'

const texts = {
  pageTitle: '\u56fe\u4e66\u641c\u7d22',
  hotKicker: '\u70ed\u95e8\u63a8\u8350',
  hotTitle: '\u672c\u5468\u70ed\u95e8\u56fe\u4e66',
  hotSubtitle: '\u57fa\u4e8e\u501f\u9605\u6392\u884c\u7684\u5feb\u901f\u63a8\u8350\uff0c\u53ef\u76f4\u63a5\u67e5\u770b\u8be6\u60c5\u6216\u8fdb\u5165\u5b8c\u6574\u699c\u5355\u3002',
  viewHotRanking: '\u67e5\u770b\u5b8c\u6574\u699c\u5355',
  hotBorrowUnit: '\u6b21',
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
  exception: '\u5f02\u5e38',
  unknownAuthor: '\u4f5c\u8005\u672a\u77e5',
  unknownCategory: '\u672a\u5206\u7c7b'
}

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const hotLoading = ref(false)
const list = ref([])
const hotBooks = ref([])
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

async function loadHotBooks() {
  hotLoading.value = true
  try {
    const res = await getPublicHotBooks({ limit: 4 })
    hotBooks.value = res.data || []
  } catch (e) {
    // handled by interceptor
  } finally {
    hotLoading.value = false
  }
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function goToHotBooks() {
  router.push('/public/hot-books')
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

function handleHotDetail(book) {
  handleDetail(book)
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
  loadHotBooks()
  loadData()
  loadCategories()
})
</script>

<style scoped>
.hot-recommend-card {
  margin-bottom: 20px;
  border: none;
  background:
    radial-gradient(circle at top right, rgba(245, 108, 108, 0.14), transparent 30%),
    linear-gradient(135deg, #fff8f5 0%, #fff 50%, #fff7ef 100%);
}

.hot-recommend-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.hot-kicker {
  color: #f56c6c;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 1px;
}

.hot-recommend-header h3 {
  margin: 8px 0;
  font-size: 28px;
  color: #303133;
}

.hot-recommend-header p {
  margin: 0;
  color: #606266;
  line-height: 1.7;
}

.hot-recommend-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
}

.hot-book-card {
  padding: 16px;
  border-radius: 14px;
  border: 1px solid #fde2e2;
  background: rgba(255, 255, 255, 0.92);
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.hot-book-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(245, 108, 108, 0.12);
}

.hot-book-rank {
  color: #f56c6c;
  font-size: 12px;
  font-weight: 700;
}

.hot-book-title {
  margin-top: 10px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.5;
}

.hot-book-meta {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}

.hot-book-footer {
  margin-top: 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.hot-book-count {
  color: #f56c6c;
  font-weight: 700;
  white-space: nowrap;
}

@media (max-width: 1200px) {
  .hot-recommend-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hot-recommend-header {
    flex-direction: column;
  }

  .hot-recommend-header h3 {
    font-size: 24px;
  }

  .hot-recommend-grid {
    grid-template-columns: 1fr;
  }
}
</style>

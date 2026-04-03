<template>
  <div class="permission-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button v-if="userStore.hasPermission('permission:create')" type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增权限
          </el-button>
        </div>
      </template>

      <el-table :data="treeData" v-loading="loading" row-key="id" :tree-props="{ children: 'children' }" stripe>
        <el-table-column prop="name" label="权限名称" min-width="200" />
        <el-table-column prop="permissionCode" label="权限编码" width="180" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ typeLabel(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="url" label="路由地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('permission:update')" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('permission:create')" type="warning" link @click="openDialog(null, row.id)">新增子项</el-button>
            <el-button v-if="userStore.hasPermission('permission:delete')" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑权限' : '新增权限'" width="560px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级权限">
          <el-tree-select
            v-model="form.parentId"
            :data="treeData"
            :props="{ label: 'name', value: 'id', children: 'children' }"
            placeholder="为空表示顶级权限"
            clearable
            check-strictly
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="权限名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%;">
            <el-option label="菜单" value="menu" />
            <el-option label="按钮" value="button" />
            <el-option label="接口" value="api" />
          </el-select>
        </el-form-item>
        <el-form-item label="路由地址">
          <el-input v-model="form.url" placeholder="例如：/admin/users" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getPermissionTree, createPermission, updatePermission, deletePermission } from '@/api/permission'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const treeData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const permissionNameMap = {
  dashboard: '控制中心',
  'public:book': '图书搜索',
  reader: '读者中心',
  'reader:profile': '个人信息',
  'reader:borrows': '我的借阅',
  'reader:reservations': '我的预约',
  'reader:fines': '我的罚款',
  book: '图书管理',
  'book:view': '图书查看',
  'book:create': '图书新增',
  'book:update': '图书编辑',
  'book:delete': '图书删除',
  category: '分类管理',
  'category:view': '分类查看',
  'category:create': '分类新增',
  'category:update': '分类编辑',
  'category:delete': '分类删除',
  borrow: '借阅管理',
  'borrow:view': '借阅查看',
  'borrow:operate': '借阅操作',
  reservation: '预约管理',
  'reservation:view': '预约查看',
  'reservation:operate': '预约处理',
  fine: '罚款管理',
  'fine:view': '罚款查看',
  'fine:operate': '罚款处理',
  user: '用户管理',
  'user:view': '用户查看',
  'user:create': '用户新增',
  'user:update': '用户编辑',
  'user:delete': '用户删除',
  role: '角色管理',
  'role:view': '角色查看',
  'role:create': '角色新增',
  'role:update': '角色编辑',
  'role:delete': '角色删除',
  permission: '权限管理',
  'permission:view': '权限查看',
  'permission:create': '权限新增',
  'permission:update': '权限编辑',
  'permission:delete': '权限删除',
  audit: '审计日志',
  'audit:view': '日志查看',
  'audit:clean': '日志清理',
  exception: '异常管理',
  'exception:view': '异常查看',
  'exception:resolve': '异常处理'
}

const defaultForm = { name: '', permissionCode: '', type: 'menu', parentId: null, url: '', sort: 0, status: 1 }
const form = reactive({ ...defaultForm })
const rules = {
  name: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  type: [{ required: true, message: '请选择权限类型', trigger: 'change' }]
}

function typeLabel(type) {
  return { menu: '菜单', button: '按钮', api: '接口' }[type] || type
}

function localizeTree(nodes = []) {
  return nodes.map(node => ({
    ...node,
    name: permissionNameMap[node.permissionCode] || node.name,
    children: localizeTree(node.children || [])
  }))
}

async function loadData() {
  loading.value = true
  try {
    const res = await getPermissionTree()
    treeData.value = localizeTree(res.data || [])
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function openDialog(row, parentId) {
  isEdit.value = !!row
  if (row) {
    Object.assign(form, { ...row })
  } else {
    Object.assign(form, { ...defaultForm, parentId: parentId || null })
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updatePermission(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createPermission(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除权限“${row.name}”吗？`, '提示', { type: 'warning' })
  try {
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* handled */ }
}

onMounted(loadData)
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>

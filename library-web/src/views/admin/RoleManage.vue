<template>
  <div class="role-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button v-if="userStore.hasPermission('role:create')" type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>新增角色
          </el-button>
        </div>
      </template>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="160" />
        <el-table-column prop="description" label="描述" min-width="220" />
        <el-table-column label="系统内置" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isSystemRole === 1 ? 'warning' : 'info'" size="small">
              {{ row.isSystemRole === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('role:update')" type="primary" link @click="openDialog(row)">编辑</el-button>
            <el-button v-if="userStore.hasPermission('role:update')" type="warning" link @click="openPermDialog(row)">分配权限</el-button>
            <el-button
              v-if="userStore.hasPermission('role:delete') && row.isSystemRole !== 1"
              type="danger"
              link
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="480px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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

    <el-dialog v-model="permDialogVisible" title="分配权限" width="520px">
      <el-tree
        ref="permTreeRef"
        :data="permTree"
        :props="{ label: 'name', children: 'children' }"
        show-checkbox
        node-key="id"
        :default-checked-keys="checkedPermIds"
      />
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssignPerms">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoles, createRole, updateRole, deleteRole, assignPermissions } from '@/api/role'
import { getPermissionTree, getPermissionsByRole } from '@/api/permission'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const permDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const permTreeRef = ref(null)
const permTree = ref([])
const checkedPermIds = ref([])
const currentRoleId = ref(null)

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

const defaultForm = { roleCode: '', roleName: '', description: '', status: 1 }
const form = reactive({ ...defaultForm })
const rules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

function localizePermissionTree(nodes = []) {
  return nodes.map(node => ({
    ...node,
    name: permissionNameMap[node.permissionCode] || node.name,
    children: localizePermissionTree(node.children || [])
  }))
}

async function loadData() {
  loading.value = true
  try {
    const res = await getRoles()
    list.value = res.data || []
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

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
      await updateRole(form.id, form)
      ElMessage.success('更新成功')
    } else {
      await createRole(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`确定删除角色“${row.roleName}”吗？`, '提示', { type: 'warning' })
  try {
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (e) { /* handled */ }
}

async function openPermDialog(row) {
  currentRoleId.value = row.id
  try {
    const [treeRes, rolePermRes] = await Promise.all([getPermissionTree(), getPermissionsByRole(row.id)])
    permTree.value = localizePermissionTree(treeRes.data || [])
    checkedPermIds.value = (rolePermRes.data || []).map(item => item.id)
    permDialogVisible.value = true
  } catch (e) { /* handled */ }
}

async function handleAssignPerms() {
  const checked = permTreeRef.value?.getCheckedKeys?.() || []
  const halfChecked = permTreeRef.value?.getHalfCheckedKeys?.() || []
  try {
    await assignPermissions(currentRoleId.value, [...checked, ...halfChecked])
    ElMessage.success('权限分配成功')
    permDialogVisible.value = false
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

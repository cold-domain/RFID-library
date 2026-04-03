<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ texts.pageTitle }}</span>
          <el-button v-if="userStore.hasPermission('user:create')" type="primary" @click="openDialog()">
            <el-icon><Plus /></el-icon>{{ texts.createUser }}
          </el-button>
        </div>
      </template>

      <el-form :inline="true" style="margin-bottom: 16px;">
        <el-form-item>
          <el-input
            v-model="queryParams.keyword"
            :placeholder="texts.searchPlaceholder"
            clearable
            style="width: 220px;"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">{{ texts.search }}</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="username" :label="texts.username" width="140" />
        <el-table-column prop="realName" :label="texts.realName" width="120" />
        <el-table-column prop="email" :label="texts.email" min-width="180" />
        <el-table-column prop="phone" :label="texts.phone" width="140" />
        <el-table-column :label="texts.roles" min-width="260">
          <template #default="{ row }">
            <el-tag v-for="role in (row.roleAssignments || [])" :key="`${row.id}-${role.roleId}`" size="small" style="margin: 2px;">
              {{ role.roleName }}<span v-if="role.expireDate">({{ texts.expireTo }} {{ role.expireDate }})</span>
            </el-tag>
            <el-tag v-if="!(row.roleAssignments || []).length" v-for="role in (row.roles || [])" :key="`${row.id}-${role}`" size="small" style="margin: 2px;">
              {{ role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" :label="texts.status" width="90">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? texts.enabled : texts.disabled }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="texts.operation" width="340" fixed="right">
          <template #default="{ row }">
            <el-button v-if="userStore.hasPermission('user:update')" type="primary" link @click="openDialog(row)">{{ texts.edit }}</el-button>
            <el-button v-if="userStore.hasPermission('user:update')" type="warning" link @click="openRoleDialog(row)">{{ texts.assignRoles }}</el-button>
            <el-button v-if="userStore.hasPermission('user:update')" type="info" link @click="handleResetPwd(row)">{{ texts.resetPassword }}</el-button>
            <el-button v-if="userStore.hasPermission('user:update')" :type="row.status === 1 ? 'danger' : 'success'" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? texts.disable : texts.enable }}
            </el-button>
            <el-button v-if="userStore.hasPermission('user:delete')" type="danger" link @click="handleDelete(row)">{{ texts.delete }}</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :total="total"
        :page-sizes="[10,20,50]"
        layout="total, sizes, prev, pager, next"
        style="margin-top: 16px; justify-content: flex-end;"
        @size-change="loadData"
        @current-change="loadData"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? texts.editUser : texts.createUser" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="texts.username" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item v-if="!isEdit" :label="texts.password" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item :label="texts.realName" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item :label="texts.email" prop="email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="texts.phone" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ texts.cancel }}</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">{{ texts.confirm }}</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="roleDialogVisible" :title="texts.assignRoles" width="560px">
      <div class="role-tip">{{ texts.roleTip }}</div>
      <div class="role-list">
        <div v-for="role in allRoles" :key="role.id" class="role-row">
          <el-checkbox v-model="selectedRoleMap[role.id]" @change="handleRoleToggle(role.id)">{{ role.roleName }}</el-checkbox>
          <el-date-picker
            v-model="roleExpireMap[role.id]"
            type="date"
            value-format="YYYY-MM-DD"
            :placeholder="texts.expireDate"
            clearable
            :disabled="!selectedRoleMap[role.id]"
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="roleDialogVisible = false">{{ texts.cancel }}</el-button>
        <el-button type="primary" @click="handleAssignRoles">{{ texts.confirm }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getUsers, createUser, updateUser, deleteUser, resetPassword, updateUserStatus, assignRoles } from '@/api/user'
import { getRoles } from '@/api/role'
import { useUserStore } from '@/store/user'

const texts = {
  pageTitle: '\u7528\u6237\u7ba1\u7406',
  createUser: '\u65b0\u589e\u7528\u6237',
  searchPlaceholder: '\u7528\u6237\u540d / \u771f\u5b9e\u59d3\u540d',
  search: '\u641c\u7d22',
  username: '\u7528\u6237\u540d',
  realName: '\u771f\u5b9e\u59d3\u540d',
  email: '\u90ae\u7bb1',
  phone: '\u624b\u673a\u53f7',
  roles: '\u89d2\u8272',
  expireTo: '\u81f3',
  status: '\u72b6\u6001',
  enabled: '\u6b63\u5e38',
  disabled: '\u7981\u7528',
  operation: '\u64cd\u4f5c',
  edit: '\u7f16\u8f91',
  assignRoles: '\u5206\u914d\u89d2\u8272',
  resetPassword: '\u91cd\u7f6e\u5bc6\u7801',
  disable: '\u7981\u7528',
  enable: '\u542f\u7528',
  delete: '\u5220\u9664',
  editUser: '\u7f16\u8f91\u7528\u6237',
  password: '\u5bc6\u7801',
  cancel: '\u53d6\u6d88',
  confirm: '\u786e\u5b9a',
  roleTip: '\u8fc7\u671f\u65f6\u95f4\u7559\u7a7a\u8868\u793a\u6c38\u4e45\u6388\u6743\u3002',
  expireDate: '\u8fc7\u671f\u65e5\u671f',
  enterUsername: '\u8bf7\u8f93\u5165\u7528\u6237\u540d',
  enterPassword: '\u8bf7\u8f93\u5165\u5bc6\u7801',
  enterRealName: '\u8bf7\u8f93\u5165\u771f\u5b9e\u59d3\u540d',
  updateSuccess: '\u66f4\u65b0\u6210\u529f',
  createSuccess: '\u521b\u5efa\u6210\u529f',
  hint: '\u63d0\u793a',
  deleteSuccess: '\u5220\u9664\u6210\u529f',
  resetSuccess: '\u5bc6\u7801\u91cd\u7f6e\u6210\u529f',
  enableSuccess: '\u542f\u7528\u6210\u529f',
  disableSuccess: '\u7981\u7528\u6210\u529f',
  assignSuccess: '\u89d2\u8272\u5206\u914d\u6210\u529f\uff0c\u76ee\u6807\u7528\u6237\u5237\u65b0\u9875\u9762\u6216\u91cd\u65b0\u767b\u5f55\u540e\u5373\u53ef\u770b\u5230\u6700\u65b0\u83dc\u5355'
}

const userStore = useUserStore()
const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const roleDialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const allRoles = ref([])
const currentUserId = ref(null)
const selectedRoleMap = reactive({})
const roleExpireMap = reactive({})

const queryParams = reactive({ pageNum: 1, pageSize: 10, keyword: '' })
const defaultForm = { username: '', password: '', realName: '', email: '', phone: '' }
const form = reactive({ ...defaultForm })
const rules = {
  username: [{ required: true, message: texts.enterUsername, trigger: 'blur' }],
  password: [{ required: true, message: texts.enterPassword, trigger: 'blur' }],
  realName: [{ required: true, message: texts.enterRealName, trigger: 'blur' }]
}

async function loadData() {
  loading.value = true
  try {
    const res = await getUsers(queryParams)
    list.value = res.data.records || res.data.list || []
    total.value = res.data.total || 0
  } catch (e) {
    // handled by interceptor
  } finally {
    loading.value = false
  }
}

async function loadRoles() {
  try {
    const res = await getRoles()
    allRoles.value = res.data || []
  } catch (e) {
    // handled by interceptor
  }
}

function resetRoleSelection() {
  Object.keys(selectedRoleMap).forEach(key => delete selectedRoleMap[key])
  Object.keys(roleExpireMap).forEach(key => delete roleExpireMap[key])
}

function handleSearch() {
  queryParams.pageNum = 1
  loadData()
}

function openDialog(row) {
  isEdit.value = !!row
  Object.assign(form, row ? { ...row, password: '' } : { ...defaultForm })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.id, form)
      ElMessage.success(texts.updateSuccess)
    } else {
      await createUser(form)
      ElMessage.success(texts.createSuccess)
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    // handled by interceptor
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(`\u786e\u5b9a\u5220\u9664\u7528\u6237 ${row.username} \u5417\uff1f`, texts.hint, { type: 'warning' })
  try {
    await deleteUser(row.id)
    ElMessage.success(texts.deleteSuccess)
    loadData()
  } catch (e) {
    // handled by interceptor
  }
}

async function handleResetPwd(row) {
  await ElMessageBox.confirm(`\u786e\u5b9a\u5c06\u7528\u6237 ${row.username} \u7684\u5bc6\u7801\u91cd\u7f6e\u4e3a 123456 \u5417\uff1f`, texts.hint, { type: 'warning' })
  try {
    await resetPassword(row.id)
    ElMessage.success(texts.resetSuccess)
  } catch (e) {
    // handled by interceptor
  }
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(newStatus === 1 ? texts.enableSuccess : texts.disableSuccess)
    loadData()
  } catch (e) {
    // handled by interceptor
  }
}

function handleRoleToggle(roleId) {
  if (!selectedRoleMap[roleId]) {
    roleExpireMap[roleId] = ''
  }
}

function openRoleDialog(row) {
  currentUserId.value = row.id
  resetRoleSelection()
  const assignments = row.roleAssignments || []
  if (assignments.length > 0) {
    assignments.forEach(item => {
      selectedRoleMap[item.roleId] = true
      roleExpireMap[item.roleId] = item.expireDate || ''
    })
  } else {
    ;(row.roleIds || []).forEach(roleId => {
      selectedRoleMap[roleId] = true
      roleExpireMap[roleId] = ''
    })
  }
  roleDialogVisible.value = true
}

async function handleAssignRoles() {
  const payload = allRoles.value
    .filter(role => selectedRoleMap[role.id])
    .map(role => ({
      roleId: role.id,
      expireDate: roleExpireMap[role.id] || null
    }))

  try {
    await assignRoles(currentUserId.value, payload)
    ElMessage.success(texts.assignSuccess)
    roleDialogVisible.value = false
    loadData()
  } catch (e) {
    // handled by interceptor
  }
}

onMounted(() => {
  loadData()
  loadRoles()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.role-tip {
  color: #909399;
  font-size: 13px;
  margin-bottom: 12px;
}
.role-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.role-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.role-row :deep(.el-date-editor) {
  width: 180px;
}
</style>

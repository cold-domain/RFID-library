<template>
  <div class="profile-view">
    <el-card>
      <template #header>个人信息</template>
      <el-form ref="profileRef" :model="profileForm" :rules="profileRules" label-width="100px" style="max-width: 500px;">
        <el-form-item label="用户名">
          <el-input :model-value="profileForm.username" disabled />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="profileForm.realName" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="profileForm.email" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="profileForm.phone" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card style="margin-top: 20px;">
      <template #header>修改密码</template>
      <el-form ref="pwdRef" :model="pwdForm" :rules="pwdRules" label-width="100px" style="max-width: 500px;">
        <el-form-item label="当前密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :loading="changingPwd" @click="handleChangePassword">修改密码</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProfile, updateProfile, changePassword } from '@/api/reader'
import { ElMessage } from 'element-plus'

const profileRef = ref(null)
const pwdRef = ref(null)
const saving = ref(false)
const changingPwd = ref(false)

const profileForm = reactive({ username: '', realName: '', email: '', phone: '' })

const profileRules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱', trigger: 'blur' }]
}

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, cb) => {
        value !== pwdForm.newPassword ? cb(new Error('两次密码不一致')) : cb()
      },
      trigger: 'blur'
    }
  ]
}

onMounted(async () => {
  try {
    const res = await getProfile()
    Object.assign(profileForm, res.data)
  } catch (e) { /* handled */ }
})

async function handleSave() {
  const valid = await profileRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    await updateProfile({ realName: profileForm.realName, email: profileForm.email, phone: profileForm.phone })
    ElMessage.success('保存成功')
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function handleChangePassword() {
  const valid = await pwdRef.value.validate().catch(() => false)
  if (!valid) return
  changingPwd.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功')
    pwdRef.value.resetFields()
  } catch (e) { /* handled */ }
  finally { changingPwd.value = false }
}
</script>

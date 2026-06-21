<template>
  <div class="admin-login-page">
    <el-card class="login-card">
      <h2>管理端登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="工号" prop="username">
          <el-input v-model="form.username" placeholder="请输入工号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <p class="link"><router-link to="/user/login">返回失主端</router-link></p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'
import { login } from '@/api/auth'
import { ElMessage } from 'element-plus'

const router = useRouter()
const authStore = useAuthStore()
const loading = ref(false)
const formRef = ref(null)
const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    authStore.logout()
    const data = await login(form)
    if (!['ROLE_STAFF', 'ROLE_ADMIN'].includes(data.userInfo.role)) {
      ElMessage.error('该账号无管理权限')
      return
    }
    authStore.setTokens(data.accessToken, data.refreshToken)
    authStore.setUserInfo(data.userInfo)
    ElMessage.success('登录成功')
    await router.push('/admin/dashboard')
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请重试')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.admin-login-page { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: #f0f2f5; }
.login-card { width: 400px; }
.login-card h2 { text-align: center; margin-bottom: 24px; }
.link { text-align: center; font-size: 13px; }
.link a { color: #409eff; }
</style>
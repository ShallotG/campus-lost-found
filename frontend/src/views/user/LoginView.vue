<template>
  <div class="login-page">
    <div class="login-wrapper">
      <div class="login-brand">
        <div class="brand-icon">
          <el-icon :size="36"><Search /></el-icon>
        </div>
        <h1>校园失物招领</h1>
        <p>智能检索，快速找回您的遗失物品</p>
      </div>
      <el-card class="login-card" shadow="always">
        <h2 class="login-title">用户登录</h2>
        <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
          <el-form-item label="用户名（学号/工号）" prop="username">
            <el-input v-model="form.username" placeholder="请输入学号或工号" prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" prefix-icon="Lock" show-password size="large" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="loading" style="width:100%" size="large" @click="handleLogin">登 录</el-button>
          </el-form-item>
        </el-form>
        <div class="login-links">
          <p>还没有账号？<router-link to="/user/register">立即注册</router-link></p>
          <p><router-link to="/admin/login">管理端登录</router-link></p>
        </div>
      </el-card>
    </div>
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    authStore.logout()
    const data = await login(form)
    authStore.setTokens(data.accessToken, data.refreshToken)
    authStore.setUserInfo(data.userInfo)
    ElMessage.success('登录成功')
    await router.push('/user/search')
  } catch (e) {
    ElMessage.error(e?.message || '登录失败，请重试')
  } finally { loading.value = false }
}
</script>

<style scoped>
.login-page { display: flex; justify-content: center; align-items: center; min-height: calc(100vh - 200px); padding: 20px; }
.login-wrapper { display: flex; align-items: center; gap: 60px; max-width: 800px; width: 100%; }
.login-brand { flex: 1; text-align: center; }
.brand-icon { color: #409eff; margin-bottom: 12px; }
.login-brand h1 { font-size: 28px; margin: 0 0 8px; color: #303133; }
.login-brand p { color: #909399; font-size: 14px; margin: 0; }
.login-card { width: 400px; flex-shrink: 0; }
.login-title { text-align: center; margin-bottom: 24px; font-size: 20px; color: #303133; }
.login-links { text-align: center; font-size: 13px; color: #909399; margin-top: 4px; }
.login-links p { margin: 4px 0; }
.login-links a { color: #409eff; }

@media (max-width: 700px) {
  .login-wrapper { flex-direction: column; gap: 24px; }
  .login-brand { flex: none; }
  .login-card { width: 100%; max-width: 400px; }
}
</style>

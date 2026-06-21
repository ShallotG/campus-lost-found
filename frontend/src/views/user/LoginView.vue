<template>
  <div class="login-page">
    <el-card class="login-card">
      <h2 class="login-title">失主登录</h2>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="用户名（学号/工号�? prop="username">
          <el-input v-model="form.username" placeholder="请输入学号或工号" prefix-icon="User" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密�? prefix-icon="Lock" show-password />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" style="width:100%" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
      <p class="login-link">还没有账号？<router-link to="/user/register">立即注册</router-link></p>
      <p class="login-link"><router-link to="/admin/login">管理端登�?/router-link></p>
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
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密�?, trigger: 'blur' }],
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
    ElMessage.error(e?.message || '��¼ʧ�ܣ�������')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 200px);
}
.login-card {
  width: 420px;
}
.login-title {
  text-align: center;
  margin-bottom: 24px;
  font-size: 22px;
}
.login-link {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}
.login-link a {
  color: #409eff;
}
</style>

<template>
  <div class="profile-page">
    <h2>个人中心</h2>

    <!-- 个人信息卡片 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>我的信息</span>
          <el-button type="primary" text @click="toggleEdit">{{ editing ? '取消' : '编辑' }}</el-button>
        </div>
      </template>
      <el-descriptions v-if="!editing" :column="2" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱" :span="2">{{ userInfo.email || '未设置' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名"><el-input :model-value="userInfo.username" disabled /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" placeholder="请输入姓名" /></el-form-item>
        <el-form-item label="手机" prop="phone"><el-input v-model="form.phone" placeholder="请输入手机号" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" placeholder="请输入邮箱" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="toggleEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 安全设置卡片 -->
    <el-card class="security-card">
      <template #header><span>安全设置</span></template>
      <div class="security-item">
        <div>
          <span class="security-label">登录密码</span>
          <span class="security-desc">定期更换密码保护账号安全</span>
        </div>
        <el-button text type="primary" @click="pwdDialogVisible = true">修改</el-button>
      </div>
    </el-card>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="6-50位新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <!-- 匹配记录卡片 -->
    <el-card class="history-card">
      <template #header><span>确认匹配记录</span></template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="confirmId" label="确认编号" width="100" />
        <el-table-column prop="lostItemId" label="物品编号" width="100" />
        <el-table-column prop="matchScore" label="匹配度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.matchScore || 0) * 100)" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" width="180" />
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchData"
        style="margin-top:16px;justify-content:center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { getUserInfo, updateProfile, changePassword } from '@/api/auth'
import { getMyConfirms } from '@/api/matchConfirm'
import { ElMessage } from 'element-plus'

const authStore = useAuthStore()
const userInfo = ref({ username: '', realName: '', phone: '', email: '', role: '' })
const roleLabel = computed(() => ({ ROLE_USER: '失主', ROLE_STAFF: '工作人员', ROLE_ADMIN: '管理员' }[userInfo.value.role] || userInfo.value.role))

// ========== 编辑资料 ==========
const editing = ref(false)
const saving = ref(false)
const formRef = ref(null)
const form = reactive({ realName: '', phone: '', email: '' })
const rules = {
  realName: [{ max: 50, message: '姓名不超过50字', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ pattern: /^[\w.-]+@[\w.-]+\.\w+$/, message: '邮箱格式不正确', trigger: 'blur' }],
}

function toggleEdit() {
  if (editing.value) {
    editing.value = false
    form.realName = userInfo.value.realName || ''
    form.phone = userInfo.value.phone || ''
    form.email = userInfo.value.email || ''
  } else {
    form.realName = userInfo.value.realName || ''
    form.phone = userInfo.value.phone || ''
    form.email = userInfo.value.email || ''
    editing.value = true
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  saving.value = true
  try {
    const updated = await updateProfile({ realName: form.realName, phone: form.phone, email: form.email })
    userInfo.value = { ...userInfo.value, ...updated }
    authStore.setUserInfo({ ...authStore.userInfo, realName: updated.realName, phone: updated.phone, email: updated.email })
    editing.value = false
    ElMessage.success('保存成功')
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

// ========== 修改密码 ==========
const pwdDialogVisible = ref(false)
const changingPwd = ref(false)
const pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) callback(new Error('两次密码不一致'))
  else callback()
}
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [{ required: true, min: 6, max: 50, message: '密码长度6-50位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
}

async function handleChangePassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return
  changingPwd.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdDialogVisible.value = false
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
    authStore.logout()
    setTimeout(() => window.location.href = '/user/login', 800)
  } catch (e) {
    ElMessage.error(e?.message || '密码修改失败')
  } finally {
    changingPwd.value = false
  }
}

// ========== 匹配记录 ==========
const loading = ref(false)
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function fetchData() {
  loading.value = true
  try {
    const data = await getMyConfirms({ page: page.value, size: size.value })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

// ========== 初始化 ==========
onMounted(async () => {
  try {
    const info = await getUserInfo()
    userInfo.value = info
    authStore.setUserInfo({ ...authStore.userInfo, realName: info.realName, phone: info.phone, email: info.email })
  } catch { /* 使用store缓存数据 */ }
  fetchData()
})
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
.profile-page h2 { margin-bottom: 20px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.info-card { margin-bottom: 16px; }
.security-card { margin-bottom: 16px; }
.security-item { display: flex; justify-content: space-between; align-items: center; }
.security-label { font-size: 14px; margin-right: 12px; }
.security-desc { font-size: 12px; color: #909399; }
.history-card { margin-top: 16px; }
</style>
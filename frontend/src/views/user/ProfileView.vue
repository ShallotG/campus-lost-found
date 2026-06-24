<template>
  <div class="profile-page">
    <h2>个人中心</h2>

    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>我的信息</span>
          <el-button type="primary" text @click="toggleEdit">{{ editing ? '取消' : '编辑' }}</el-button>
        </div>
      </template>
      <el-descriptions v-if="!editing" :column="3" border>
        <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ userInfo.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">{{ roleLabel }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ userInfo.phone || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ userInfo.email || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ userInfo.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-form v-else ref="formRef" :model="form" :rules="rules" label-width="80px" style="max-width:480px">
        <el-form-item label="用户名"><el-input :model-value="userInfo.username" disabled /></el-form-item>
        <el-form-item label="姓名" prop="realName"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="手机" prop="phone"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱" prop="email"><el-input v-model="form.email" /></el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="toggleEdit">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="section-card">
      <template #header>
        <div class="card-header">
          <span>安全设置</span>
        </div>
      </template>
      <div class="security-row">
        <div>
          <span class="sec-label">登录密码</span>
          <span class="sec-desc">定期更换密码保护账号安全</span>
        </div>
        <el-button text type="primary" @click="pwdDialogVisible = true">修改</el-button>
      </div>
    </el-card>

    <el-dialog v-model="pwdDialogVisible" title="修改密码" width="420px" :close-on-click-modal="false">
      <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="80px">
        <el-form-item label="原密码" prop="oldPassword"><el-input v-model="pwdForm.oldPassword" type="password" show-password /></el-form-item>
        <el-form-item label="新密码" prop="newPassword"><el-input v-model="pwdForm.newPassword" type="password" show-password /></el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword"><el-input v-model="pwdForm.confirmPassword" type="password" show-password /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="changingPwd" @click="handleChangePassword">确认修改</el-button>
      </template>
    </el-dialog>

    <el-card class="section-card">
      <template #header><span>确认匹配记录</span></template>
      <el-table :data="records" v-loading="confLoading" stripe>
        <el-table-column label="图片" width="80">
          <template #default="{ row }">
            <el-image v-if="row.itemImageUrl" :src="row.itemImageUrl" style="width:50px;height:50px;border-radius:6px" fit="cover" />
            <span v-else style="color:#c0c4cc;font-size:12px">无图</span>
          </template>
        </el-table-column>
        <el-table-column prop="itemCategory" label="类别" width="110" />
        <el-table-column prop="itemStorageLocation" label="存放位置" min-width="160" />
        <el-table-column prop="matchScore" label="匹配度" width="130">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.matchScore || 0) * 100)" :stroke-width="6" :status="(row.matchScore || 0) > 0.5 ? 'success' : ''" />
          </template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" width="170" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag :type="row.itemStatus === 'UNCLAIMED' ? 'warning' : 'success'" size="small">{{ row.itemStatus === 'UNCLAIMED' ? '待领取' : '已领取' }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="confPage" :total="confTotal" :page-size="10" layout="prev, pager, next" @current-change="fetchConfirms" style="margin-top:16px;justify-content:center" />
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
const userInfo = ref({ username: '', realName: '', phone: '', email: '', role: '', createTime: '' })
const roleLabel = computed(() => ({ ROLE_USER: '用户', ROLE_STAFF: '工作人员', ROLE_ADMIN: '管理员' }[userInfo.value.role] || ''))

const editing = ref(false), saving = ref(false), formRef = ref(null)
const form = reactive({ realName: '', phone: '', email: '' })
const rules = {
  realName: [{ max: 50, message: '姓名不超过50字', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ pattern: /^[\w.-]+@[\w.-]+\.\w+$/, message: '邮箱格式不正确', trigger: 'blur' }],
}
function toggleEdit() {
  if (!editing.value) { form.realName = userInfo.value.realName || ''; form.phone = userInfo.value.phone || ''; form.email = userInfo.value.email || '' }
  editing.value = !editing.value
}
async function handleSave() {
  if (!await formRef.value.validate().catch(() => false)) return
  saving.value = true
  try {
    const updated = await updateProfile({ realName: form.realName, phone: form.phone, email: form.email })
    userInfo.value = { ...userInfo.value, ...updated }
    authStore.setUserInfo({ ...authStore.userInfo, realName: updated.realName, phone: updated.phone, email: updated.email })
    editing.value = false; ElMessage.success('保存成功')
  } catch (e) { ElMessage.error(e?.message || '保存失败') } finally { saving.value = false }
}

const pwdDialogVisible = ref(false), changingPwd = ref(false), pwdFormRef = ref(null)
const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdRules = {
  oldPassword: [{ required: true, message: '请输入原密码' }],
  newPassword: [{ required: true, min: 6, max: 50, message: '密码长度6-50位' }],
  confirmPassword: [{ required: true, message: '请确认密码' }, { validator: (_, v, cb) => v === pwdForm.newPassword ? cb() : cb(new Error('两次密码不一致')), trigger: 'blur' }],
}
async function handleChangePassword() {
  if (!await pwdFormRef.value.validate().catch(() => false)) return
  changingPwd.value = true
  try {
    await changePassword({ oldPassword: pwdForm.oldPassword, newPassword: pwdForm.newPassword })
    ElMessage.success('密码修改成功，请重新登录')
    pwdDialogVisible.value = false; pwdForm.oldPassword = ''; pwdForm.newPassword = ''; pwdForm.confirmPassword = ''
    authStore.logout(); setTimeout(() => window.location.href = '/user/login', 800)
  } catch (e) { ElMessage.error(e?.message || '密码修改失败') } finally { changingPwd.value = false }
}

const confLoading = ref(false), records = ref([]), confPage = ref(1), confTotal = ref(0)
async function fetchConfirms() {
  confLoading.value = true
  try { const d = await getMyConfirms({ page: confPage.value, size: 10 }); records.value = d.records; confTotal.value = d.total } finally { confLoading.value = false }
}
onMounted(async () => {
  try { const info = await getUserInfo(); userInfo.value = info; authStore.setUserInfo({ ...authStore.userInfo, realName: info.realName, phone: info.phone, email: info.email }) } catch { /* */ }
  fetchConfirms()
})
</script>

<style scoped>
.profile-page h2 { margin-bottom: 20px; }
.section-card { margin-bottom: 16px; border-radius: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.security-row { display: flex; justify-content: space-between; align-items: center; }
.sec-label { font-size: 14px; margin-right: 12px; }
.sec-desc { font-size: 12px; color: var(--text-muted); }
</style>
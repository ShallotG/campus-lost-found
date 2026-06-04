<template>
  <div class="user-manage">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="openCreate">新增用户</el-button>
        </div>
      </template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="row.role === 'ROLE_ADMIN' ? 'danger' : row.role === 'ROLE_STAFF' ? 'warning' : 'info'">
              {{ roleMap[row.role] || row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="enabled" label="状态" width="80">
          <template #default="{ row }">
            <el-switch :model-value="row.enabled" @change="(val) => toggleEnable(row.id, val)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140">
          <template #default="{ row }">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        style="margin-top:16px"
      />
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="450px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="密码" :prop="isEdit ? '' : 'password'">
          <el-input v-model="form.password" type="password" :placeholder="isEdit ? '留空则不修改' : '请输入密码'" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role">
            <el-option label="失主" value="ROLE_USER" />
            <el-option label="工作人员" value="ROLE_STAFF" />
            <el-option label="管理员" value="ROLE_ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, createUser, updateUser, deleteUser } from '@/api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const roleMap = { ROLE_ADMIN: '管理员', ROLE_STAFF: '工作人员', ROLE_USER: '失主' }
const loading = ref(false)
const records = ref([])
const page = ref(1), size = ref(10), total = ref(0)

const dialogVisible = ref(false), dialogTitle = ref(''), isEdit = ref(false)
const submitting = ref(false), formRef = ref(null)
const form = reactive({ id: null, username: '', password: '', realName: '', role: 'ROLE_USER' })
const rules = {
  username: [{ required: true, message: '请输入用户名' }],
  password: [{ required: true, min: 6, message: '密码至少6位' }],
  role: [{ required: true, message: '请选择角色' }],
}

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try { const data = await getUserList({ page: page.value, size: size.value }); records.value = data.records; total.value = data.total } finally { loading.value = false }
}

function openCreate() {
  dialogTitle.value = '新增用户'; isEdit.value = false; resetForm()
  dialogVisible.value = true
}
function openEdit(row) {
  dialogTitle.value = '编辑用户'; isEdit.value = true
  Object.assign(form, { id: row.id, username: row.username, password: '', realName: row.realName || '', role: row.role })
  dialogVisible.value = true
}
function resetForm() {
  form.id = null; form.username = ''; form.password = ''; form.realName = ''; form.role = 'ROLE_USER'
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateUser(form.id, { realName: form.realName, role: form.role, password: form.password || undefined })
    } else {
      await createUser(form)
    }
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false; fetchData()
  } finally { submitting.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该用户？', '确认', { type: 'warning' })
    await deleteUser(id); ElMessage.success('删除成功'); fetchData()
  } catch { /* cancelled */ }
}

async function toggleEnable(id, val) {
  await updateUser(id, { enabled: val }); ElMessage.success(val ? '已启用' : '已禁用')
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

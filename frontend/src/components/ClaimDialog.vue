<template>
  <el-dialog v-model="visible" title="登记领取" width="480px" @close="resetForm">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="物品信息">
        <span>{{ itemInfo?.category }} — {{ itemInfo?.storageLocation }}</span>
      </el-form-item>
      <el-form-item label="领取人姓名" prop="claimerName">
        <el-input v-model="form.claimerName" placeholder="请输入领取人姓名" />
      </el-form-item>
      <el-form-item label="学号/工号" prop="claimerIdentity">
        <el-input v-model="form.claimerIdentity" placeholder="请输入学号或工号" />
      </el-form-item>
      <el-form-item label="联系电话">
        <el-input v-model="form.claimerPhone" placeholder="请输入联系电话（可选）" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="核对备注（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确认领取</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { claimItem } from '@/api/claim'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['success'])
const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const itemInfo = ref(null)
const form = reactive({ claimerName: '', claimerIdentity: '', claimerPhone: '', remark: '' })
const rules = {
  claimerName: [{ required: true, message: '请输入领取人姓名' }],
  claimerIdentity: [{ required: true, message: '请输入学号/工号' }],
}

function open(item) {
  itemInfo.value = item
  form.lostItemId = item.id
  visible.value = true
}

function resetForm() {
  form.claimerName = ''; form.claimerIdentity = ''; form.claimerPhone = ''; form.remark = ''
  itemInfo.value = null
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  submitting.value = true
  try {
    await claimItem({ lostItemId: itemInfo.value.id, ...form })
    ElMessage.success('领取登记成功')
    visible.value = false
    emit('success')
  } finally { submitting.value = false }
}

defineExpose({ open })
</script>

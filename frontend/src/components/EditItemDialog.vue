<template>
  <el-dialog v-model="visible" title="编辑拾物信息" width="500px" @close="resetForm">
    <el-form ref="formRef" :model="form" label-width="100px">
      <el-form-item label="物品类别">
        <el-input v-model="form.category" placeholder="物品类别" />
      </el-form-item>
      <el-form-item label="存放位置">
        <el-input v-model="form.storageLocation" placeholder="存放位置" />
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="补充信息（可选）" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { updateLostItem } from '@/api/lostItem'
import { ElMessage } from 'element-plus'

const emit = defineEmits(['success'])
const visible = ref(false)
const saving = ref(false)
const formRef = ref(null)
const editId = ref(null)
const form = reactive({ category: '', storageLocation: '', remark: '' })

function open(item) {
  editId.value = item.id
  form.category = item.category || ''
  form.storageLocation = item.storageLocation || ''
  form.remark = item.remark || ''
  visible.value = true
}

function resetForm() {
  form.category = ''; form.storageLocation = ''; form.remark = ''
}

async function handleSave() {
  saving.value = true
  try {
    await updateLostItem(editId.value, { ...form })
    ElMessage.success('更新成功')
    visible.value = false
    emit('success')
  } catch (e) {
    ElMessage.error(e?.message || '更新失败')
  } finally {
    saving.value = false
  }
}

defineExpose({ open })
</script>

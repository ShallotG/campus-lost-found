<template>
  <div class="publish-page">
    <h2>发布寻物启事</h2>
    <el-card class="pub-card">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="pub-form">
        <el-row :gutter="24">
          <el-col :span="16">
            <el-form-item label="物品名称" prop="itemName">
              <el-input v-model="form.itemName" placeholder="如：黑色双肩包、白色iPhone 15" size="large" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="物品类别">
              <el-input v-model="form.category" placeholder="如：电子产品" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item label="颜色"><el-input v-model="form.color" placeholder="如：黑色" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="品牌"><el-input v-model="form.brand" placeholder="如：Apple" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="联系电话"><el-input v-model="form.contactPhone" placeholder="选填" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="24">
          <el-col :span="12">
            <el-form-item label="丢失时间">
              <el-date-picker v-model="form.lostTime" type="datetime" placeholder="选择时间" style="width:100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="丢失地点"><el-input v-model="form.lostLocation" placeholder="如：图书馆二楼" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="详细描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述物品特征..." maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" :loading="submitting" @click="handleSubmit">
            <el-icon><Check /></el-icon> 发布寻物启事
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { createFoundItem } from '@/api/foundItem'
import { ElMessage } from 'element-plus'
import { Check } from '@element-plus/icons-vue'

const router = useRouter()
const formRef = ref(null), submitting = ref(false)
const form = reactive({ itemName: '', category: '', color: '', brand: '', description: '', lostTime: null, lostLocation: '', contactPhone: '' })
const rules = { itemName: [{ required: true, message: '请输入物品名称', trigger: 'blur' }] }

async function handleSubmit() {
  if (!await formRef.value.validate().catch(() => false)) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (payload.lostTime) payload.lostTime = new Date(payload.lostTime).toISOString()
    await createFoundItem(payload)
    ElMessage.success('发布成功'); router.push('/user/my-posts')
  } catch (e) { ElMessage.error(e?.message || '发布失败') } finally { submitting.value = false }
}
function resetForm() { formRef.value?.resetFields() }
</script>

<style scoped>
.publish-page h2 { margin-bottom: 20px; }
.pub-card { border-radius: 10px; }
.pub-form { max-width: 800px; }
</style>
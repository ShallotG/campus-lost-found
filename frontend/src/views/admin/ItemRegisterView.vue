<template>
  <div class="item-register">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拾物登记</span>
          <el-button @click="$router.push('/admin/items/list')" text>
            <el-icon><ArrowLeft /></el-icon> 返回列表
          </el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <!-- 图片上传（AI检测 + Top3标签选择） -->
        <el-form-item label="物品图片" prop="imageUrl">
          <ImageUploader
            ref="uploaderRef"
            v-model="form.imageUrl"
            v-model:category="form.category"
            v-model:confidence="form.categoryConfidence"
          />
          <div class="form-tip">支持 JPG/PNG，最大 5MB。上传后AI自动识别物品类别</div>
        </el-form-item>

        <!-- 物品类别 -->
        <el-form-item label="物品类别" prop="category">
          <el-input
            v-model="form.category"
            placeholder="点击AI识别标签自动填充，或手动输入自定义标签（如「白色透明水杯」）"
          >
            <template #append>
              <el-tag v-if="form.categoryConfidence > 0" size="small" effect="dark" type="success">
                {{ (form.categoryConfidence * 100).toFixed(0) }}%
              </el-tag>
            </template>
          </el-input>
        </el-form-item>

        <el-divider content-position="left">物品信息</el-divider>

        <!-- 存放位置 -->
        <el-form-item label="存放位置" prop="storageLocation">
          <el-input v-model="form.storageLocation" placeholder="例如：A柜台第3层、图书馆一楼失物招领处" />
        </el-form-item>

        <!-- 备注 -->
        <el-form-item label="备注">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="捡拾时间、地点、捡拾人联系方式等补充信息（可选）"
          />
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-button type="primary" :loading="submitting" @click="handleSubmit">
            <el-icon><Check /></el-icon> 提交登记
          </el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { createLostItem } from '@/api/lostItem'
import { ElMessage } from 'element-plus'
import ImageUploader from '@/components/ImageUploader.vue'

const formRef = ref(null)
const uploaderRef = ref(null)
const submitting = ref(false)
const form = reactive({
  imageUrl: '',
  category: '',
  categoryConfidence: 0,
  storageLocation: '',
  remark: '',
})
const rules = {
  category: [{ required: true, message: '请输入或选择物品类别', trigger: 'blur' }],
  storageLocation: [{ required: true, message: '请输入存放位置', trigger: 'blur' }],
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    // 1. 先上传文件到OSS/本地（此前仅做了AI检测，文件尚未保存）
    const imageUrl = await uploaderRef.value.uploadFile()
    form.imageUrl = imageUrl

    // 2. 创建拾物记录
    await createLostItem({
      imageUrl: form.imageUrl,
      category: form.category,
      categoryConfidence: form.categoryConfidence,
      storageLocation: form.storageLocation,
      remark: form.remark,
    })
    ElMessage.success('拾物登记成功')
    resetForm()
  } catch (e) {
    ElMessage.error('登记失败: ' + (e.message || '请重试'))
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  formRef.value?.resetFields()
  form.imageUrl = ''
  form.category = ''
  form.categoryConfidence = 0
  form.storageLocation = ''
  form.remark = ''
  uploaderRef.value?.reset()
}
</script>

<style scoped>
.item-register {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
</style>

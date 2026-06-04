<template>
  <div class="image-uploader">
    <!-- 拖拽选择区（不自动上传） -->
    <el-upload
      ref="uploadRef"
      class="upload-area"
      :auto-upload="false"
      :show-file-list="false"
      accept=".jpg,.jpeg,.png"
      drag
      :on-change="onFileChange"
    >
      <template v-if="!previewUrl">
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">拖拽图片到此处或<em>点击上传</em></div>
        <div class="upload-hint">仅支持 JPG/PNG 格式，最大 5MB</div>
      </template>
      <img v-else :src="previewUrl" class="preview-image" />
    </el-upload>

    <!-- AI 检测中 -->
    <div v-if="detecting" class="upload-tip">
      <el-icon class="is-loading"><Loading /></el-icon> AI正在识别物品类别...
    </div>

    <!-- AI 检测结果 -->
    <div v-if="detectResult && !detecting" class="detect-result">
      <div class="detect-header">
        <el-icon><MagicStick /></el-icon>
        <span class="detect-label">AI 识别结果：</span>
        <el-tag type="success" size="large" effect="dark" class="desc-tag">
          {{ detectResult.description || detectResult.categoryCn }}
        </el-tag>
        <span class="desc-conf">置信度 {{ ((detectResult.confidence || 0) * 100).toFixed(0) }}%</span>
      </div>

      <div class="detect-title">点击以下候选标签可替换类别：</div>
      <div class="top3-options">
        <el-tag
          v-for="(opt, i) in detectResult.top3"
          :key="i"
          :type="selectedOption === i ? 'success' : 'info'"
          size="large"
          class="option-tag"
          :effect="selectedOption === i ? 'dark' : 'plain'"
          @click="selectOption(i)"
        >
          {{ opt.categoryCn }} ({{ (opt.confidence * 100).toFixed(0) }}%)
        </el-tag>
      </div>
      <div class="detect-hint">
        也可以在下方的输入框中手动输入自定义标签（如「白色透明水杯」）
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick } from '@element-plus/icons-vue'
import axios from 'axios'

const props = defineProps({
  modelValue: String,
  category: String,
  confidence: Number,
})

const emit = defineEmits(['update:modelValue', 'update:category', 'update:confidence'])

const uploadRef = ref(null)
const detecting = ref(false)
const detectResult = ref(null)
const previewUrl = ref(null)
const pendingFile = ref(null)
const selectedOption = ref(-1)

const imageUrl = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val),
})

function onFileChange(file) {
  const isValidType = ['image/jpeg', 'image/png'].includes(file.raw.type)
  const isValidSize = file.raw.size <= 5 * 1024 * 1024
  if (!isValidType) { ElMessage.error('仅支持 JPG/PNG 格式'); return }
  if (!isValidSize) { ElMessage.error('图片大小不能超过5MB'); return }

  previewUrl.value = URL.createObjectURL(file.raw)
  pendingFile.value = file.raw
  imageUrl.value = ''
  detectResult.value = null

  // 调用检测API（不保存文件）
  doDetect(file.raw)
}

async function doDetect(rawFile) {
  detecting.value = true
  selectedOption.value = -1
  try {
    const formData = new FormData()
    formData.append('file', rawFile)
    const token = localStorage.getItem('accessToken')
    const { data: res } = await axios.post('/api/lost-items/detect', formData, {
      headers: { 'Content-Type': 'multipart/form-data', Authorization: `Bearer ${token}` },
    })
    const result = res.data
    detectResult.value = {
      category: result.category,
      categoryCn: result.categoryCn,
      confidence: result.confidence,
      description: result.description || result.categoryCn || '',
      top3: result.top3 || [],
    }
    // 默认使用AI细致描述作为类别
    if (result.description) {
      emit('update:category', result.description)
      emit('update:confidence', result.confidence || 0)
    } else if (result.top3 && result.top3.length > 0) {
      selectOption(0)
    }
    ElMessage.success('AI识别完成')
  } catch (e) {
    ElMessage.error('AI检测失败，请重试')
  } finally {
    detecting.value = false
  }
}

/** 点击候选标签 */
function selectOption(i) {
  selectedOption.value = i
  const opt = detectResult.value.top3[i]
  if (opt) {
    emit('update:category', opt.categoryCn)
    emit('update:confidence', opt.confidence)
  }
}

/**
 * 上传文件到服务器（OSS/本地），返回 imageUrl
 * 由父组件在提交表单时调用
 */
async function uploadFile() {
  if (!pendingFile.value) {
    throw new Error('请先选择图片')
  }
  const formData = new FormData()
  formData.append('file', pendingFile.value)
  const token = localStorage.getItem('accessToken')
  const { data: res } = await axios.post('/api/lost-items/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data', Authorization: `Bearer ${token}` },
  })
  // res = { code: 200, data: { imageUrl, category, ... } }
  imageUrl.value = res.data.imageUrl
  return res.data.imageUrl
}

/** 重置组件状态 */
function reset() {
  previewUrl.value = null
  pendingFile.value = null
  detectResult.value = null
  selectedOption.value = -1
  imageUrl.value = ''
  emit('update:category', '')
  emit('update:confidence', 0)
}

defineExpose({ uploadFile, reset })
</script>

<style scoped>
.upload-area { width: 100%; }
.upload-icon { font-size: 48px; color: #c0c4cc; }
.upload-text { color: #606266; margin-top: 8px; }
.upload-text em { color: #409eff; font-style: normal; }
.upload-hint { color: #c0c4cc; font-size: 12px; margin-top: 4px; }
.preview-image { width: 100%; max-height: 300px; object-fit: contain; border-radius: 8px; }
.upload-tip { margin-top: 8px; color: #409eff; display: flex; align-items: center; gap: 4px; }
.detect-result { margin-top: 12px; padding: 12px; background: #f5f7fa; border-radius: 8px; }
.detect-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; flex-wrap: wrap; }
.detect-label { font-size: 13px; color: #606266; }
.desc-tag { font-size: 15px !important; padding: 8px 16px !important; }
.desc-conf { font-size: 12px; color: #67c23a; }
.detect-title { font-size: 12px; color: #909399; margin-bottom: 8px; }
.top3-options { display: flex; gap: 10px; flex-wrap: wrap; margin-bottom: 6px; }
.option-tag { cursor: pointer; font-size: 14px; padding: 8px 16px; transition: transform 0.15s; }
.option-tag:hover { transform: scale(1.05); }
.detect-hint { font-size: 12px; color: #909399; }
</style>

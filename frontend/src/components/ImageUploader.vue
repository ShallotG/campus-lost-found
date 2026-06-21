<template>
  <div class="image-uploader">
    <el-upload ref="uploadRef" class="upload-area" :auto-upload="false" :show-file-list="false" accept=".jpg,.jpeg,.png" drag :on-change="onFileChange">
      <template v-if="!previewUrl">
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">拖拽图片到此处或<em>点击上传</em></div>
        <div class="upload-hint">仅支持 JPG/PNG 格式，最大 5MB</div>
      </template>
      <img v-else :src="previewUrl" alt="预览图片" class="preview-image" />
    </el-upload>
    <div v-if="detecting" class="upload-tip"><el-icon class="is-loading"><Loading /></el-icon> AI正在识别物品类别...</div>
    <div v-if="detectResult && !detecting" class="detect-result">
      <div class="detect-header">
        <el-icon><MagicStick /></el-icon>
        <span class="detect-label">AI 识别结果：</span>
        <el-tag type="success" size="large" effect="dark" class="desc-tag">{{ detectResult.description || detectResult.categoryCn }}</el-tag>
        <span class="desc-conf">置信度 {{ ((detectResult.confidence || 0) * 100).toFixed(0) }}%</span>
      </div>
      <div class="detect-title">点击以下候选标签可替换类别</div>
      <div class="top3-options">
        <el-tag v-for="(opt, i) in detectResult.top3" :key="i" :type="selectedOption === i ? 'success' : 'info'" size="large" class="option-tag" :effect="selectedOption === i ? 'dark' : 'plain'" @click="selectOption(i)">{{ opt.categoryCn }} ({{ (opt.confidence * 100).toFixed(0) }}%)</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { MagicStick, UploadFilled, Loading } from '@element-plus/icons-vue'
import request from '@/api/index'

const props = defineProps({ modelValue: String, category: String, confidence: Number })
const emit = defineEmits(['update:modelValue', 'update:category', 'update:confidence'])

const uploadRef = ref(null)
const detecting = ref(false)
const detectResult = ref(null)
const previewUrl = ref(null)
const pendingFile = ref(null)
const selectedOption = ref(-1)

const imageUrl = computed({ get: () => props.modelValue, set: (val) => emit('update:modelValue', val) })

function onFileChange(file) {
  if (!['image/jpeg', 'image/png'].includes(file.raw.type)) { ElMessage.error('仅支持 JPG/PNG 格式'); return }
  if (file.raw.size > 5 * 1024 * 1024) { ElMessage.error('图片大小不能超过5MB'); return }
  previewUrl.value = URL.createObjectURL(file.raw)
  pendingFile.value = file.raw
  imageUrl.value = ''
  detectResult.value = null
  doDetect(file.raw)
}

async function doDetect(rawFile) {
  detecting.value = true
  selectedOption.value = -1
  try {
    const formData = new FormData()
    formData.append('file', rawFile)
    const result = await request.post('/lost-items/detect', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
    detectResult.value = { category: result.category, categoryCn: result.categoryCn, confidence: result.confidence, description: result.description || result.categoryCn || '', top3: result.top3 || [] }
    const label = result.categoryCn || result.description || ''
    if (label) { emit('update:category', label); emit('update:confidence', result.confidence || 0) }
    if (result.top3 && result.top3.length > 0) selectOption(0)
    ElMessage.success('AI识别完成')
  } catch (e) { ElMessage.error('AI检测失败') }
  finally { detecting.value = false }
}

function selectOption(i) {
  selectedOption.value = i
  const opt = detectResult.value.top3[i]
  if (opt) { emit('update:category', opt.categoryCn); emit('update:confidence', opt.confidence) }
}

async function uploadFile() {
  if (!pendingFile.value) throw new Error('请先选择图片')
  const formData = new FormData()
  formData.append('file', pendingFile.value)
  const result = await request.post('/lost-items/upload', formData, { headers: { 'Content-Type': 'multipart/form-data' } })
  imageUrl.value = result.imageUrl
  return result.imageUrl
}

function reset() { previewUrl.value = null; pendingFile.value = null; detectResult.value = null; selectedOption.value = -1; imageUrl.value = ''; emit('update:category', ''); emit('update:confidence', 0) }
defineExpose({ uploadFile, reset, detectResult })
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
</style>
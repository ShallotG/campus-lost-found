<template>
  <div class="search-page">
    <div class="search-hero">
      <h1>智能失物检索</h1>
      <p>用自然语言描述您丢失的物品，AI帮您智能匹配</p>
      <div class="search-box">
        <el-input
          v-model="query"
          type="textarea"
          :rows="3"
          placeholder="例如：昨天下午在图书馆二楼丢失了一个黑色双肩包，里面有ThinkPad笔记本电脑和几本教材..."
          maxlength="500"
          show-word-limit
        />
        <el-button type="primary" size="large" :loading="searching" @click="handleSearch">
          <el-icon><Search /></el-icon> 智能检索
        </el-button>
      </div>
    </div>

    <div v-if="searchResult" class="search-result">
      <div class="result-header">
        <h3>检索结果</h3>
        <span class="result-meta">
          共比对 {{ searchResult.totalCompared }} 条记录，耗时 {{ searchResult.queryTime }}ms
        </span>
      </div>

      <el-empty v-if="!searchResult.results?.length" description="未找到匹配的物品，请尝试用不同方式描述" />

      <div v-else class="result-list">
        <el-card v-for="item in searchResult.results" :key="item.id" class="result-item">
          <div class="item-content">
            <div class="item-image">
              <img :src="item.imageUrl" alt="物品图片" @error="onImageError" />
            </div>
            <div class="item-info">
              <div class="item-category">
                <el-tag type="primary">{{ item.category }}</el-tag>
                <span class="match-score">
                  <el-progress :percentage="Math.round(item.matchScore * 100)" :stroke-width="8" :color="scoreColor(item.matchScore)" />
                  <span class="score-text">匹配度 {{ (item.matchScore * 100).toFixed(1) }}%</span>
                </span>
              </div>
              <p><strong>存放位置：</strong>{{ item.storageLocation }}</p>
              <p v-if="item.remark"><strong>备注：</strong>{{ item.remark }}</p>
              <p><strong>登记时间：</strong>{{ item.createTime }}</p>
              <p v-if="item.explanation" class="explanation">
                <el-icon><InfoFilled /></el-icon> {{ item.explanation }}
              </p>
              <div class="item-actions">
                <el-button type="success" @click="handleConfirm(item)">确认是我的物品</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { semanticSearch } from '@/api/search'
import { confirmMatch } from '@/api/matchConfirm'
import { ElMessage } from 'element-plus'

const router = useRouter()
const query = ref('')
const searching = ref(false)
const searchResult = ref(null)

async function handleSearch() {
  if (!query.value.trim()) {
    ElMessage.warning('请输入物品描述')
    return
  }
  searching.value = true
  try {
    searchResult.value = await semanticSearch({ query: query.value.trim(), topK: 5 })
  } finally {
    searching.value = false
  }
}

async function handleConfirm(item) {
  try {
    const result = await confirmMatch({ lostItemId: item.id, matchScore: item.matchScore })
    router.push({
      path: '/user/confirm-result',
      query: {
        itemId: item.id,
        contactInfo: JSON.stringify(result.contactInfo),
        confirmId: result.confirmId,
      },
    })
  } catch (e) {
    // error handled by interceptor
  }
}

function scoreColor(score) {
  if (score >= 0.8) return '#67c23a'
  if (score >= 0.5) return '#e6a23c'
  return '#f56c6c'
}

function onImageError(e) {
  e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><rect fill="%23f0f0f0" width="100" height="100"/><text x="50" y="55" text-anchor="middle" fill="%23999" font-size="12">暂无图片</text></svg>'
}
</script>

<style scoped>
.search-page { max-width: 900px; margin: 0 auto; }
.search-hero { text-align: center; padding: 40px 0 24px; }
.search-hero h1 { font-size: 28px; margin-bottom: 8px; }
.search-hero p { color: #909399; margin-bottom: 24px; }
.search-box { display: flex; gap: 12px; max-width: 700px; margin: 0 auto; }
.search-box .el-textarea { flex: 1; }
.search-box .el-button { align-self: flex-end; }
.result-header { display: flex; justify-content: space-between; align-items: center; margin: 24px 0 16px; }
.result-meta { font-size: 13px; color: #909399; }
.result-item { margin-bottom: 16px; }
.item-content { display: flex; gap: 20px; }
.item-image { width: 180px; height: 140px; overflow: hidden; border-radius: 8px; background: #f5f7fa; }
.item-image img { width: 100%; height: 100%; object-fit: cover; }
.item-info { flex: 1; }
.item-category { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.match-score { flex: 1; display: flex; align-items: center; gap: 8px; }
.score-text { font-size: 14px; font-weight: 500; white-space: nowrap; }
.item-info p { margin: 6px 0; color: #606266; font-size: 14px; }
.explanation { color: #409eff; font-size: 13px; background: #ecf5ff; padding: 8px 12px; border-radius: 4px; }
.item-actions { margin-top: 12px; }
</style>

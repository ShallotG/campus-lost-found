<template>
  <div class="search-page">
    <div class="search-header">
      <h1>智能失物检索</h1>
      <p>用自然语言描述您丢失的物品，AI 帮您智能匹配</p>
    </div>

    <div class="search-input-area">
      <el-input
        v-model="query"
        type="textarea"
        :rows="3"
        placeholder="例如：昨天下午在图书馆二楼丢失了一个黑色双肩包，里面有ThinkPad笔记本电脑和几本教材..."
        maxlength="500"
        show-word-limit
        class="search-textarea"
        @keyup.enter.ctrl="handleSearch"
      />
      <el-button type="primary" size="large" :loading="searching" class="search-btn" @click="handleSearch">
        <el-icon><Search /></el-icon> 智能检索
      </el-button>
    </div>

    <div class="search-tags" v-if="!searchResult">
      <span class="tag-label">热门搜索：</span>
      <el-tag v-for="tag in hotTags" :key="tag" class="clickable-tag" @click="query = tag; handleSearch()">{{ tag }}</el-tag>
    </div>

    <div class="search-tags" v-if="searchHistory.length > 0 && !searchResult">
      <span class="tag-label">最近搜索：</span>
      <el-tag v-for="(h, i) in searchHistory" :key="i" class="clickable-tag" type="info" size="small" @click="query = h; handleSearch()">{{ h }}</el-tag>
      <el-button text size="small" type="danger" @click="clearHistory">清除</el-button>
    </div>

    <div v-if="searchResult" class="search-result-area">
      <div class="result-bar">
        <h3>检索结果</h3>
        <span class="result-meta">共匹配 <strong>{{ searchResult.total }}</strong> 条，耗时 {{ searchResult.queryTime }}ms</span>
      </div>

      <template v-if="searching">
        <el-skeleton v-for="i in 3" :key="i" animated style="margin-bottom:16px">
          <template #template>
            <div style="display:flex;gap:20px">
              <el-skeleton-item variant="image" style="width:160px;height:130px" />
              <div style="flex:1">
                <el-skeleton-item variant="text" style="width:35%" />
                <el-skeleton-item variant="text" style="width:60%" />
                <el-skeleton-item variant="text" style="width:45%" />
              </div>
            </div>
          </template>
        </el-skeleton>
      </template>

      <el-empty v-else-if="!searchResult.results?.length" description="未找到匹配的物品" />

      <div v-else class="result-list">
        <el-card v-for="item in searchResult.results" :key="item.id" class="result-card" shadow="hover">
          <div class="result-row">
            <div class="result-img" @click="$router.push(`/user/item/${item.id}`)">
              <img :src="item.imageUrl" alt="物品图片" @error="onImgErr" />
            </div>
            <div class="result-body">
              <div class="result-top">
                <el-tag type="primary" size="small">{{ item.category }}</el-tag>
                <span class="match-badge" :class="{ high: item.matchScore >= 0.7 }">匹配 {{ (item.matchScore * 100).toFixed(0) }}%</span>
              </div>
              <p><strong>存放位置：</strong>{{ item.storageLocation }}</p>
              <p v-if="item.remark"><strong>备注：</strong>{{ item.remark }}</p>
              <p class="result-time">登记时间：{{ item.createTime }}</p>
              <p v-if="item.explanation" class="ai-explain"><el-icon><MagicStick /></el-icon> {{ item.explanation }}</p>
              <div class="result-actions">
                <el-button size="small" @click="$router.push(`/user/item/${item.id}`)">查看详情</el-button>
                <el-button size="small" type="success" @click="handleConfirm(item)">确认是我的</el-button>
              </div>
            </div>
          </div>
        </el-card>
      </div>

      <el-pagination v-if="searchResult && searchResult.total > 10" v-model:current-page="searchPage" :total="searchResult.total" :page-size="10" layout="total, prev, pager, next" background @current-change="handlePageChange" style="margin-top:24px;justify-content:center" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { semanticSearch } from '@/api/search'
import { confirmMatch } from '@/api/matchConfirm'
import { ElMessage } from 'element-plus'
import { Search, MagicStick } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const query = ref('')
const searching = ref(false)
const searchResult = ref(null)
const searchPage = ref(1)

// 顶栏快捷搜索传入的预填词
onMounted(() => {
  if (route.query.q) {
    query.value = route.query.q
    handleSearch()
  }
})

const hotTags = ['黑色双肩包', '校园卡', '手机', '水杯', '钥匙', '雨伞', '笔记本电脑']
const HISTORY_KEY = 'search_history'
const searchHistory = ref(JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]'))

function saveHistory(q) { let list = JSON.parse(localStorage.getItem(HISTORY_KEY) || '[]'); list = [q, ...list.filter(h => h !== q)].slice(0, 5); localStorage.setItem(HISTORY_KEY, JSON.stringify(list)); searchHistory.value = list }
function clearHistory() { localStorage.removeItem(HISTORY_KEY); searchHistory.value = [] }

async function doSearch(pageOverride) {
  if (!query.value.trim()) { ElMessage.warning('请输入物品描述'); return }
  searching.value = true; saveHistory(query.value.trim())
  try { searchResult.value = await semanticSearch({ query: query.value.trim(), topK: 10, page: pageOverride || 1, size: 10 }); searchPage.value = pageOverride || 1 } finally { searching.value = false }
}
function handleSearch() { doSearch(1) }
function handlePageChange(p) { doSearch(p); window.scrollTo({ top: 300, behavior: 'smooth' }) }

async function handleConfirm(item) {
  try {
    const result = await confirmMatch({ lostItemId: item.id, matchScore: item.matchScore })
    router.push({ path: '/user/confirm-result', query: { itemId: item.id, contactInfo: JSON.stringify(result.contactInfo), itemInfo: JSON.stringify({ itemImageUrl: result.itemImageUrl, itemCategory: result.itemCategory, itemStorageLocation: result.itemStorageLocation }), confirmId: result.confirmId } })
  } catch { /* */ }
}
function onImgErr(e) { e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 160 130"><rect fill="%23f0f0f0" width="160" height="130"/><text x="80" y="70" text-anchor="middle" fill="%23999" font-size="12">暂无图片</text></svg>' }
</script>

<style scoped>
.search-page { max-width: 100%; }
.search-header { text-align: center; margin-bottom: 24px; }
.search-header h1 { font-size: 24px; margin-bottom: 6px; }
.search-header p { color: var(--text-muted); font-size: 14px; }
.search-input-area { display: flex; gap: 14px; max-width: 750px; margin: 0 auto 16px; }
.search-textarea { flex: 1; }
.search-btn { align-self: flex-end; white-space: nowrap; }
.search-tags { display: flex; align-items: center; gap: 8px; justify-content: center; flex-wrap: wrap; margin-bottom: 12px; }
.tag-label { font-size: 13px; color: var(--text-muted); }
.clickable-tag { cursor: pointer; }
.search-result-area { margin-top: 20px; }
.result-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.result-bar h3 { font-size: 18px; }
.result-meta { font-size: 13px; color: var(--text-muted); }
.result-card { margin-bottom: 14px; border-radius: 10px; border: none; }
.result-row { display: flex; gap: 18px; }
.result-img { width: 160px; height: 130px; overflow: hidden; border-radius: 8px; background: #f5f7fa; cursor: pointer; flex-shrink: 0; }
.result-img img { width: 100%; height: 100%; object-fit: cover; }
.result-body { flex: 1; min-width: 0; }
.result-top { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.match-badge { font-size: 13px; color: #909399; }
.match-badge.high { color: #67c23a; }
.result-body p { margin: 5px 0; color: var(--text-secondary); font-size: 14px; }
.result-time { font-size: 13px; color: var(--text-muted); }
.ai-explain { color: #409eff; font-size: 13px; background: #ecf5ff; padding: 8px 12px; border-radius: 6px; display: flex; align-items: flex-start; gap: 4px; }
.result-actions { margin-top: 12px; display: flex; gap: 8px; }

@media (max-width: 600px) {
  .search-input-area { flex-direction: column; max-width: 100%; }
  .search-btn { align-self: stretch; }
  .result-row { flex-direction: column; }
  .result-img { width: 100%; height: 200px; }
}
</style>
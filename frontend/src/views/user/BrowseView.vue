<template>
  <div class="browse-page">
    <div class="browse-header">
      <h1>浏览失物</h1>
      <p>按分类浏览校园内所有待认领的失物，点击卡片查看详情</p>
    </div>

    <!-- 搜索 + 分类 工具栏 -->
    <div class="browse-toolbar">
      <div class="toolbar-left">
        <el-input
          v-model="keyword"
          placeholder="搜索物品关键词..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          class="keyword-input"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
      </div>
      <div class="toolbar-right">
        <span class="toolbar-label">分类筛选：</span>
        <el-radio-group v-model="activeCategory" size="small" @change="onCategoryChange">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button v-for="cat in categories" :key="cat" :value="cat">{{ cat }}</el-radio-button>
        </el-radio-group>
      </div>
    </div>

    <div class="toolbar-meta">
      <span v-if="total > 0">共 <strong>{{ total }}</strong> 件待认领物品</span>
      <span v-else-if="!loading">暂无匹配物品</span>
    </div>

    <!-- 骨架屏 -->
    <div v-if="loading" class="item-grid">
      <div v-for="i in 8" :key="i" class="item-card-skeleton">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width:100%;height:200px;border-radius:10px" />
            <div style="padding:12px">
              <el-skeleton-item variant="text" style="width:60%;margin-bottom:8px" />
              <el-skeleton-item variant="text" style="width:85%" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else-if="items.length === 0" description="暂无待认领的物品">
      <el-button type="primary" @click="activeCategory = ''; keyword = ''; fetchData()">清除筛选</el-button>
    </el-empty>

    <!-- 物品卡片网格 -->
    <div v-else class="item-grid">
      <el-card
        v-for="item in items"
        :key="item.id"
        class="item-card"
        shadow="hover"
        :body-style="{ padding: '0' }"
        @click="$router.push(`/user/item/${item.id}`)"
      >
        <div class="card-img-wrap">
          <img :src="item.imageUrl" :alt="item.category" @error="onImgErr" />
          <div class="card-img-overlay">
            <el-button type="primary" size="small" round>查看详情</el-button>
          </div>
          <el-tag class="card-badge" type="warning" size="small" effect="dark">待领取</el-tag>
        </div>
        <div class="card-info">
          <div class="card-category">
            <el-icon :size="14"><Collection /></el-icon>
            {{ item.category }}
          </div>
          <div class="card-location">
            <el-icon :size="14"><Location /></el-icon>
            {{ item.storageLocation }}
          </div>
          <div class="card-time">{{ item.createTime }}</div>
        </div>
      </el-card>
    </div>

    <!-- 分页 -->
    <div class="browse-pagination" v-if="total > size">
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        background
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getBrowseItems, getBrowseCategories } from '@/api/browse'
import { Search, Collection, Location } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const items = ref([])
const categories = ref([])
const activeCategory = ref('')
const keyword = ref('')
const page = ref(1), size = ref(12), total = ref(0)

onMounted(async () => {
  try { categories.value = (await getBrowseCategories()) || [] } catch { /* */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const data = await getBrowseItems({
      page: page.value, size: size.value,
      category: activeCategory.value || undefined,
      keyword: keyword.value || undefined,
    })
    items.value = data.records; total.value = data.total
  } finally { loading.value = false }
}

function onCategoryChange() { page.value = 1; fetchData() }
function handleSearch() { page.value = 1; fetchData() }
function onImgErr(e) { e.target.src = 'data:image/svg+xml,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 200 160"><rect fill="%23f0f0f0" width="200" height="160"/><text x="100" y="85" text-anchor="middle" fill="%23999" font-size="14">暂无图片</text></svg>' }
</script>

<style scoped>
.browse-page { max-width: 100%; }
.browse-header { margin-bottom: 20px; }
.browse-header h1 { font-size: 24px; margin-bottom: 4px; color: var(--text-primary); }
.browse-header p { font-size: 14px; color: var(--text-muted); }

/* 工具栏 */
.browse-toolbar {
  display: flex;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
  background: var(--header-bg);
  padding: 14px 18px;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  margin-bottom: 12px;
}
.toolbar-left { flex-shrink: 0; }
.keyword-input { width: 240px; }
.toolbar-right { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.toolbar-label { font-size: 13px; color: var(--text-secondary); white-space: nowrap; }
.toolbar-meta { font-size: 13px; color: var(--text-muted); margin-bottom: 16px; }
.toolbar-meta strong { color: var(--text-primary); }

/* 卡片网格 */
.item-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: 18px; }
.item-card { cursor: pointer; border-radius: 12px; overflow: hidden; transition: transform 0.25s, box-shadow 0.25s; border: none; }
.item-card:hover { transform: translateY(-4px); box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1); }

.card-img-wrap { position: relative; height: 190px; overflow: hidden; background: #f5f7fa; }
.card-img-wrap img { width: 100%; height: 100%; object-fit: cover; transition: transform 0.3s; }
.item-card:hover .card-img-wrap img { transform: scale(1.05); }
.card-img-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.3);
  opacity: 0;
  transition: opacity 0.25s;
}
.item-card:hover .card-img-overlay { opacity: 1; }
.card-badge { position: absolute; top: 10px; right: 10px; }

.card-info { padding: 14px; }
.card-category { font-size: 14px; font-weight: 600; color: var(--text-primary); display: flex; align-items: center; gap: 4px; margin-bottom: 6px; }
.card-location { font-size: 13px; color: var(--text-secondary); display: flex; align-items: center; gap: 4px; margin-bottom: 4px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-time { font-size: 12px; color: var(--text-muted); }

.item-card-skeleton { background: var(--header-bg); border-radius: 12px; padding: 14px; box-shadow: 0 1px 4px rgba(0,0,0,0.04); }

.browse-pagination { display: flex; justify-content: center; margin-top: 28px; }

/* 响应式 */
@media (max-width: 1200px) { .item-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 860px)  { .item-grid { grid-template-columns: repeat(2, 1fr); } .browse-toolbar { flex-direction: column; align-items: flex-start; gap: 12px; } .keyword-input { width: 100%; } }
@media (max-width: 500px)  { .item-grid { grid-template-columns: 1fr; } }
</style>
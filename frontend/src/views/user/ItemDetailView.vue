<template>
  <div class="detail-page">
    <el-button text @click="$router.back()" style="margin-bottom:16px">
      <el-icon><ArrowLeft /></el-icon> 返回
    </el-button>

    <el-card v-loading="loading" class="detail-card">
      <div v-if="item" class="detail-layout">
        <div class="detail-image" v-if="item.imageUrl">
          <el-image :src="item.imageUrl" fit="contain" class="main-image" />
        </div>
        <div class="detail-body">
          <h1 class="detail-title">{{ item.category }}</h1>
          <el-tag :type="item.status === 'UNCLAIMED' ? 'warning' : 'success'" size="large">
            {{ item.status === 'UNCLAIMED' ? '待领取' : '已领取' }}
          </el-tag>

          <el-descriptions :column="2" border style="margin-top:20px">
            <el-descriptions-item label="存放位置">{{ item.storageLocation }}</el-descriptions-item>
            <el-descriptions-item label="登记时间">{{ item.createTime }}</el-descriptions-item>
            <el-descriptions-item label="登记人">{{ item.createUserName || '未知' }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ item.updateTime }}</el-descriptions-item>
            <el-descriptions-item label="备注" :span="2">{{ item.remark || '无' }}</el-descriptions-item>
          </el-descriptions>

          <div class="detail-actions" v-if="item.status === 'UNCLAIMED'">
            <el-button type="success" size="large" @click="handleConfirm">
              <el-icon><Check /></el-icon> 确认是我的物品
            </el-button>
          </div>
        </div>
      </div>
      <el-empty v-else description="物品不存在" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getLostItemById } from '@/api/lostItem'
import { confirmMatch } from '@/api/matchConfirm'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'

const route = useRoute(), router = useRouter()
const loading = ref(false), item = ref(null)

onMounted(async () => {
  const id = route.params.id; if (!id) return
  loading.value = true
  try { item.value = await getLostItemById(id) } catch { ElMessage.error('加载失败') } finally { loading.value = false }
})

async function handleConfirm() {
  try {
    const result = await confirmMatch({ lostItemId: item.value.id, matchScore: 1.0 })
    router.push({ path: '/user/confirm-result', query: { itemId: item.value.id, contactInfo: JSON.stringify(result.contactInfo), confirmId: result.confirmId } })
  } catch { /* */ }
}
</script>

<style scoped>
.detail-card { border-radius: 12px; }
.detail-layout { display: flex; gap: 32px; }
.detail-image { flex-shrink: 0; width: 360px; }
.main-image { width: 100%; max-height: 360px; border-radius: 10px; }
.detail-body { flex: 1; min-width: 0; }
.detail-title { font-size: 22px; margin: 0 0 10px; }
.detail-actions { margin-top: 24px; }

@media (max-width: 768px) {
  .detail-layout { flex-direction: column; }
  .detail-image { width: 100%; }
}
</style>
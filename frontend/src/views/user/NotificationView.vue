<template>
  <div class="notification-page">
    <div class="page-header">
      <h2>消息通知</h2>
      <el-button text type="primary" @click="handleMarkAll">全部标为已读</el-button>
    </div>

    <el-card>
      <div v-if="records.length === 0 && !loading" class="empty-notif">
        <el-empty description="暂无通知" />
      </div>
      <div v-else v-loading="loading">
        <div
          v-for="n in records"
          :key="n.id"
          class="notification-item"
          :class="{ unread: !n.isRead }"
          @click="handleClick(n)"
        >
          <div class="notif-icon">
            <el-icon :size="20">
              <Bell v-if="n.type === 'MATCH_CONFIRM'" style="color:#409eff" />
              <CircleCheck v-else-if="n.type === 'ITEM_CLAIMED'" style="color:#67c23a" />
              <InfoFilled v-else style="color:#909399" />
            </el-icon>
          </div>
          <div class="notif-body">
            <div class="notif-title">
              {{ n.title }}
              <span v-if="!n.isRead" class="unread-dot"></span>
            </div>
            <div class="notif-content">{{ n.content }}</div>
            <div class="notif-time">{{ n.createTime }}</div>
          </div>
        </div>
        <el-pagination
          v-if="total > size"
          v-model:current-page="page"
          :total="total"
          :page-size="size"
          layout="prev, pager, next"
          @current-change="fetchData"
          style="margin-top:16px;justify-content:center"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getNotifications, markAsRead, markAllAsRead } from '@/api/notification'
import { Bell, CircleCheck, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const records = ref([])
const page = ref(1), size = ref(10), total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const data = await getNotifications({ page: page.value, size: size.value })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function handleClick(n) {
  if (!n.isRead) {
    try {
      await markAsRead(n.id)
      n.isRead = true
    } catch { /* ignore */ }
  }
  if (n.relatedItemId) {
    router.push(`/user/item/${n.relatedItemId}`)
  }
}

async function handleMarkAll() {
  try {
    await markAllAsRead()
    records.value.forEach(r => { r.isRead = true })
    ElMessage.success('全部已标为已读')
  } catch { /* ignore */ }
}
</script>

<style scoped>
.notification-page { max-width: 700px; margin: 0 auto; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.empty-notif { padding: 40px 0; }
.notification-item {
  display: flex; gap: 12px; padding: 14px 12px;
  border-bottom: 1px solid #ebeef5; cursor: pointer; transition: background .2s;
}
.notification-item:hover { background: #f5f7fa; }
.notification-item.unread { background: #ecf5ff; }
.notif-icon { flex-shrink: 0; padding-top: 2px; }
.notif-body { flex: 1; min-width: 0; }
.notif-title { font-size: 14px; font-weight: 500; display: flex; align-items: center; gap: 6px; }
.unread-dot { width: 7px; height: 7px; border-radius: 50%; background: #f56c6c; }
.notif-content { font-size: 13px; color: #606266; margin-top: 4px; line-height: 1.5; }
.notif-time { font-size: 12px; color: #c0c4cc; margin-top: 6px; }
</style>

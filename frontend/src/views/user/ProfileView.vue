<template>
  <div class="profile-page">
    <h2>个人中心</h2>
    <el-card class="user-info-card">
      <el-descriptions title="我的信息" :column="2" border>
        <el-descriptions-item label="用户名">{{ authStore.userInfo?.username }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ authStore.userInfo?.realName || '未设置' }}</el-descriptions-item>
        <el-descriptions-item label="角色">失主</el-descriptions-item>
        <el-descriptions-item label="手机">{{ authStore.userInfo?.phone || '未设置' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card class="history-card">
      <template #header><span>确认匹配记录</span></template>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="confirmId" label="确认编号" width="100" />
        <el-table-column prop="lostItemId" label="物品编号" width="100" />
        <el-table-column prop="matchScore" label="匹配度" width="120">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.matchScore || 0) * 100)" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="confirmTime" label="确认时间" width="180" />
      </el-table>
      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="prev, pager, next"
        @current-change="fetchData"
        style="margin-top:16px;justify-content:center"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/store/auth'
import { getMyConfirms } from '@/api/matchConfirm'

const authStore = useAuthStore()
const loading = ref(false)
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const data = await getMyConfirms({ page: page.value, size: size.value })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.profile-page { max-width: 800px; margin: 0 auto; }
.profile-page h2 { margin-bottom: 20px; }
.history-card { margin-top: 20px; }
</style>

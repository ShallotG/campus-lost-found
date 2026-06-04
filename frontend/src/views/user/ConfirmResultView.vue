<template>
  <div class="confirm-page">
    <el-result icon="success" title="确认成功！">
      <template #sub-title>
        <p>您已成功确认物品匹配，请携带有效证件前往失物招领处领取。</p>
      </template>
      <template #extra>
        <el-card class="contact-card">
          <h3>失物招领处联系方式</h3>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="地址">{{ contactInfo.address }}</el-descriptions-item>
            <el-descriptions-item label="电话">{{ contactInfo.phone }}</el-descriptions-item>
            <el-descriptions-item label="工作时间">{{ contactInfo.workingHours }}</el-descriptions-item>
            <el-descriptions-item label="领取须知">{{ contactInfo.claimNotice }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        <div style="margin-top:20px">
          <el-button type="primary" @click="$router.push('/user/search')">继续检索</el-button>
          <el-button @click="$router.push('/user/profile')">查看我的记录</el-button>
        </div>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const contactInfo = computed(() => {
  try {
    return JSON.parse(route.query.contactInfo || '{}')
  } catch { return {} }
})
</script>

<style scoped>
.confirm-page { padding: 40px 0; }
.contact-card { max-width: 500px; margin: 20px auto; text-align: left; }
.contact-card h3 { margin-bottom: 12px; }
</style>

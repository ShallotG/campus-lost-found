<template>
  <div class="confirm-page">
    <el-result icon="success" title="确认成功！">
      <template #sub-title>
        <p>您已成功确认物品匹配，请携带有效证件前往失物招领处领取。</p>
      </template>
      <template #extra>
        <!-- 匹配物品信息 -->
        <el-card class="info-card" v-if="itemInfo.itemCategory">
          <h3>匹配物品</h3>
          <div class="matched-item">
            <el-image
              v-if="itemInfo.itemImageUrl"
              :src="itemInfo.itemImageUrl"
              style="width:80px;height:80px;border-radius:8px"
              fit="cover"
            />
            <div>
              <p><strong>{{ itemInfo.itemCategory }}</strong></p>
              <p>存放位置：{{ itemInfo.itemStorageLocation }}</p>
            </div>
          </div>
        </el-card>

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
  try { return JSON.parse(route.query.contactInfo || '{}') } catch { return {} }
})
const itemInfo = computed(() => {
  try { return JSON.parse(route.query.itemInfo || '{}') } catch { return {} }
})
</script>

<style scoped>
.confirm-page { padding: 40px 0; }
.info-card, .contact-card { max-width: 500px; margin: 16px auto; text-align: left; }
.info-card h3, .contact-card h3 { margin-bottom: 12px; }
.matched-item { display: flex; gap: 16px; align-items: center; }
.matched-item p { margin: 4px 0; font-size: 14px; }
</style>

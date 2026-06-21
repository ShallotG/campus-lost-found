<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ stats.todayNewItems }}</div>
          <div class="stat-label">今日新增拾物</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value" style="color:#67c23a">{{ stats.totalReturned }}</div>
          <div class="stat-label">累计归还</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value" style="color:#e6a23c">{{ stats.currentUnclaimed }}</div>
          <div class="stat-label">当前待领取</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="12">
        <el-card>
          <template #header>热门物品类别</template>
          <div ref="categoryChart" style="height:300px" />
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card>
          <template #header>近7天趋势</template>
          <div ref="trendChart" style="height:300px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { getDashboardStats } from '@/api/dashboard'
import * as echarts from 'echarts'

const stats = ref({ todayNewItems: 0, totalReturned: 0, currentUnclaimed: 0, categoryStats: [], weeklyTrend: [] })
const categoryChart = ref(null)
const trendChart = ref(null)

onMounted(async () => {
  stats.value = await getDashboardStats()
  await nextTick()
  initCategoryChart()
  initTrendChart()
})

function initCategoryChart() {
  if (!categoryChart.value) return
  const chart = echarts.init(categoryChart.value)
  chart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      data: stats.value.categoryStats?.map(c => ({ name: c.category, value: c.count })) || [],
    }],
  })
}

function initTrendChart() {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增', '归还'] },
    xAxis: { type: 'category', data: stats.value.weeklyTrend?.map(t => t.date) || [] },
    yAxis: { type: 'value' },
    series: [
      { name: '新增', type: 'line', data: stats.value.weeklyTrend?.map(t => t.newItems) || [], smooth: true },
      { name: '归还', type: 'line', data: stats.value.weeklyTrend?.map(t => t.claimedItems) || [], smooth: true },
    ],
  })
}
</script>

<style scoped>
.stat-card { text-align: center; }
.stat-value { font-size: 36px; font-weight: 700; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>

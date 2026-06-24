<template>
  <div class="dashboard">
    <!-- 统计卡片 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="hover" @click="$router.push('/admin/items/list')">
          <div class="stat-value">{{ stats.todayNewItems }}</div>
          <div class="stat-label">今日新增拾物</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="hover" @click="$router.push('/admin/items/list?status=CLAIMED')">
          <div class="stat-value" style="color:#67c23a">{{ stats.totalReturned }}</div>
          <div class="stat-label">累计归还</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="8">
        <el-card class="stat-card" shadow="hover" @click="$router.push('/admin/items/list?status=UNCLAIMED')">
          <div class="stat-value" style="color:#e6a23c">{{ stats.currentUnclaimed }}</div>
          <div class="stat-label">当前待领取</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>热门物品类别</template>
          <div ref="categoryChart" style="height:320px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12">
        <el-card>
          <template #header>近7天趋势</template>
          <div ref="trendChart" style="height:320px" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { getDashboardStats } from '@/api/dashboard'
import * as echarts from 'echarts'

const stats = ref({ todayNewItems: 0, totalReturned: 0, currentUnclaimed: 0, categoryStats: [], weeklyTrend: [] })
const categoryChart = ref(null)
const trendChart = ref(null)
let catChart = null, trdChart = null

onMounted(async () => {
  stats.value = await getDashboardStats()
  await nextTick()
  initCategoryChart()
  initTrendChart()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  catChart?.dispose()
  trdChart?.dispose()
})

function handleResize() {
  catChart?.resize()
  trdChart?.resize()
}

function initCategoryChart() {
  if (!categoryChart.value) return
  catChart?.dispose()
  catChart = echarts.init(categoryChart.value)
  catChart.setOption({
    tooltip: { trigger: 'item' },
    series: [{
      type: 'pie',
      radius: ['40%', '70%'],
      center: ['50%', '55%'],
      data: stats.value.categoryStats?.map(c => ({ name: c.category, value: c.count })) || [],
      label: { show: true, formatter: '{b}\n{d}%' },
    }],
  })
}

function initTrendChart() {
  if (!trendChart.value) return
  trdChart?.dispose()
  trdChart = echarts.init(trendChart.value)
  trdChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增', '归还'] },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'category', data: stats.value.weeklyTrend?.map(t => t.date) || [], boundaryGap: false },
    yAxis: { type: 'value' },
    series: [
      { name: '新增', type: 'line', data: stats.value.weeklyTrend?.map(t => t.newItems) || [], smooth: true, areaStyle: { opacity: 0.15 } },
      { name: '归还', type: 'line', data: stats.value.weeklyTrend?.map(t => t.claimedItems) || [], smooth: true, areaStyle: { opacity: 0.15 } },
    ],
  })
}
</script>

<style scoped>
.stat-card { text-align: center; cursor: pointer; transition: transform 0.2s; }
.stat-card:hover { transform: translateY(-2px); }
.stat-value { font-size: 36px; font-weight: 700; color: #409eff; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>

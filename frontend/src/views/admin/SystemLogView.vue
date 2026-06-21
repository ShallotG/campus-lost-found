<template>
  <div class="system-log">
    <el-card>
      <template #header><span>系统日志</span></template>
      <el-form inline>
        <el-form-item label="日志类型">
          <el-select v-model="filters.logType" clearable @change="fetchData">
            <el-option label="API" value="API" />
            <el-option label="AI" value="AI" />
            <el-option label="系统" value="SYSTEM" />
          </el-select>
        </el-form-item>
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            @change="fetchData"
          />
        </el-form-item>
      </el-form>

      <el-table :data="records" v-loading="loading" stripe max-height="500">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="logType" label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.logType === 'ERROR' ? 'danger' : row.logType === 'AI' ? 'warning' : 'info'" size="small">
              {{ row.logType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="requestUri" label="请求URI" width="180" show-overflow-tooltip />
        <el-table-column prop="requestMethod" label="方法" width="80" />
        <el-table-column prop="durationMs" label="耗时(ms)" width="90" />
        <el-table-column prop="ipAddress" label="IP" width="140" />
        <el-table-column prop="createTime" label="时间" width="170" />
      </el-table>

      <el-pagination
        v-model:current-page="page"
        :total="total"
        :page-size="size"
        layout="total, prev, pager, next"
        @current-change="fetchData"
        style="margin-top:16px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import axios from 'axios'

const loading = ref(false)
const records = ref([])
const page = ref(1), size = ref(20), total = ref(0)
const filters = reactive({ logType: '', dateRange: null })

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: size.value }
    if (filters.logType) params.logType = filters.logType
    if (filters.dateRange) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }
    const { data } = await axios.get('/api/system-logs', {
      params,
      headers: { Authorization: `Bearer ${localStorage.getItem('accessToken')}` },
    })
    if (data.code === 200) {
      records.value = data.data.records
      total.value = data.data.total
    }
  } finally { loading.value = false }
}
</script>

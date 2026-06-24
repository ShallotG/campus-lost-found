<template>
  <div class="found-item-list">
    <el-card>
      <template #header><span>寻物启事列表</span></template>
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部" @change="fetchData">
            <el-option label="寻找中" value="OPEN" />
            <el-option label="已匹配" value="MATCHED" />
            <el-option label="已关闭" value="CLOSED" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="userName" label="发布人" width="100" />
        <el-table-column prop="itemName" label="物品名称" min-width="140" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="color" label="颜色" width="80" />
        <el-table-column prop="lostLocation" label="丢失地点" width="140" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
      </el-table>
      <el-pagination
        v-model:current-page="page" :total="total" :page-size="size"
        layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:16px"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getFoundItemList } from '@/api/foundItem'

const loading = ref(false), records = ref([])
const page = ref(1), size = ref(10), total = ref(0)
const filters = reactive({ status: '' })

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const data = await getFoundItemList({ page: page.value, size: size.value, status: filters.status || undefined })
    records.value = data.records; total.value = data.total
  } finally { loading.value = false }
}

function statusType(s) { return { OPEN: 'warning', MATCHED: 'success', CLOSED: 'info' }[s] || 'info' }
function statusLabel(s) { return { OPEN: '寻找中', MATCHED: '已匹配', CLOSED: '已关闭' }[s] || s }
</script>

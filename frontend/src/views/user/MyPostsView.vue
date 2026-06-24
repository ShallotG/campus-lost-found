<template>
  <div class="my-posts-page">
    <div class="page-header">
      <h2>我的寻物启事</h2>
      <el-button type="primary" @click="$router.push('/user/publish')">
        <el-icon><Plus /></el-icon> 发布新启事
      </el-button>
    </div>

    <el-card class="section-card">
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="itemName" label="物品名称" min-width="150" />
        <el-table-column prop="category" label="类别" width="110" />
        <el-table-column prop="color" label="颜色" width="80" />
        <el-table-column prop="lostLocation" label="丢失地点" width="150" />
        <el-table-column prop="lostTime" label="丢失时间" width="170" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'OPEN'" size="small" type="warning" @click="handleClose(row.id)">关闭</el-button>
            <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="total, prev, pager, next" @current-change="fetchData" style="margin-top:16px" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyFoundItems, closeFoundItem, deleteFoundItem } from '@/api/foundItem'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false), records = ref([]), page = ref(1), size = ref(10), total = ref(0)
onMounted(() => fetchData())
async function fetchData() { loading.value = true; try { const d = await getMyFoundItems({ page: page.value, size: size.value }); records.value = d.records; total.value = d.total } finally { loading.value = false } }
function statusType(s) { return { OPEN: 'warning', MATCHED: 'success', CLOSED: 'info' }[s] || 'info' }
function statusLabel(s) { return { OPEN: '寻找中', MATCHED: '已匹配', CLOSED: '已关闭' }[s] || s }
async function handleClose(id) { try { await ElMessageBox.confirm('确定关闭？', '确认', { type: 'warning' }); await closeFoundItem(id); ElMessage.success('已关闭'); fetchData() } catch { /* */ } }
async function handleDelete(id) { try { await ElMessageBox.confirm('确定删除？', '确认', { type: 'warning' }); await deleteFoundItem(id); ElMessage.success('已删除'); fetchData() } catch { /* */ } }
</script>

<style scoped>
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { margin: 0; }
.section-card { border-radius: 10px; }
</style>
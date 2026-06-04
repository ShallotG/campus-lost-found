<template>
  <div class="item-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>拾物列表</span>
          <el-button type="primary" @click="$router.push('/admin/items/register')">登记新物品</el-button>
        </div>
      </template>

      <!-- 筛选栏 -->
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="filters.status" clearable placeholder="全部" @change="fetchData">
            <el-option label="未领取" value="UNCLAIMED" />
            <el-option label="已领取" value="CLAIMED" />
          </el-select>
        </el-form-item>
        <el-form-item label="类别">
          <el-input v-model="filters.category" placeholder="物品类别" clearable @change="fetchData" />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="filters.keyword" placeholder="搜索关键词" clearable @change="fetchData" />
        </el-form-item>
      </el-form>

      <!-- 表格 -->
      <el-table :data="records" v-loading="loading" stripe>
        <el-table-column label="图片" width="100">
          <template #default="{ row }">
            <el-image :src="row.imageUrl" style="width:60px;height:60px;border-radius:4px" fit="cover" />
          </template>
        </el-table-column>
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="category" label="类别" width="100" />
        <el-table-column prop="storageLocation" label="存放位置" min-width="140" />
        <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UNCLAIMED' ? 'warning' : 'success'">
              {{ row.status === 'UNCLAIMED' ? '未领取' : '已领取' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="登记时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'UNCLAIMED'" type="success" size="small" @click="openClaimDialog(row)">
              领取
            </el-button>
            <el-button v-if="row.status === 'UNCLAIMED'" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button v-if="userRole === 'ROLE_ADMIN'" type="danger" size="small" @click="handleDelete(row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
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

    <!-- 领取登记弹窗 -->
    <ClaimDialog ref="claimDialogRef" @success="fetchData" />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useAuthStore } from '@/store/auth'
import { getLostItemList, deleteLostItem } from '@/api/lostItem'
import { ElMessage, ElMessageBox } from 'element-plus'
import ClaimDialog from '@/components/ClaimDialog.vue'

const authStore = useAuthStore()
const userRole = computed(() => authStore.userRole)

const loading = ref(false)
const records = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const filters = reactive({ status: '', category: '', keyword: '' })
const claimDialogRef = ref(null)

onMounted(() => fetchData())

async function fetchData() {
  loading.value = true
  try {
    const data = await getLostItemList({
      page: page.value,
      size: size.value,
      ...filters,
    })
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

function openClaimDialog(row) {
  claimDialogRef.value?.open(row)
}

function handleEdit(row) {
  // 简化处理：弹出编辑框
  ElMessage.info('编辑功能请在更新API中实现')
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确认删除该拾物记录？', '确认删除', { type: 'warning' })
    await deleteLostItem(id)
    ElMessage.success('删除成功')
    fetchData()
  } catch { /* cancelled */ }
}
</script>

<style scoped>
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

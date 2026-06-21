<template>
  <div class="config-manage">
    <el-card>
      <template #header><span>系统配置管理</span></template>
      <el-table :data="configs" stripe>
        <el-table-column prop="configKey" label="配置键" width="180" />
        <el-table-column prop="configValue" label="配置值" min-width="300">
          <template #default="{ row }">
            <template v-if="editingKey === row.configKey">
              <el-input v-model="editValue" />
              <el-button type="primary" size="small" style="margin-left:8px" @click="saveConfig(row)">保存</el-button>
              <el-button size="small" @click="editingKey = null">取消</el-button>
            </template>
            <span v-else>{{ row.configKey === 'deepseek_api_key' ? '******' : row.configValue }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="说明" width="250" show-overflow-tooltip />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button size="small" @click="startEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getConfigList, updateConfig } from '@/api/systemConfig'
import { ElMessage } from 'element-plus'

const configs = ref([])
const editingKey = ref(null)
const editValue = ref('')

onMounted(async () => {
  configs.value = await getConfigList()
})

function startEdit(row) {
  editingKey.value = row.configKey
  editValue.value = row.configValue
}

async function saveConfig(row) {
  await updateConfig(row.configKey, editValue.value)
  row.configValue = editValue.value
  editingKey.value = null
  ElMessage.success('配置更新成功')
}
</script>

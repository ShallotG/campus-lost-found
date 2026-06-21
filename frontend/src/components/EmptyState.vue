<template>
  <div class="empty-state">
    <el-icon :size="64" color="#c0c4cc"><component :is="icon" /></el-icon>
    <p class="empty-text">{{ description }}</p>
    <slot />
  </div>
</template>

<script setup>
defineProps({
  icon: { type: String, default: 'FolderOpened' },
  description: { type: String, default: '暂无数据' },
})
</script>

<style scoped>
.empty-state { text-align: center; padding: 60px 0; }
.empty-text { color: #909399; font-size: 14px; margin-top: 12px; }
</style>

<template>
  <router-view />
</template>

<script setup>
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }

:root {
  --bg-primary: #f5f7fa;
  --bg-card: #ffffff;
  --text-primary: #303133;
  --text-secondary: #606266;
  --text-muted: #909399;
  --border-color: #e4e7ed;
  --header-bg: #ffffff;
  --header-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

[data-theme='dark'] {
  --bg-primary: #141414;
  --bg-card: #1f1f1f;
  --text-primary: #e5e5e5;
  --text-secondary: #a0a0a0;
  --text-muted: #6b6b6b;
  --border-color: #333333;
  --header-bg: #1f1f1f;
  --header-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

body {
  font-family: 'Microsoft YaHei', 'PingFang SC', sans-serif;
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s, color 0.3s;
}

#app { min-height: 100vh; }

/* 响应式基础 */
@media (max-width: 768px) {
  .search-box { flex-direction: column; }
  .search-box .el-button { align-self: stretch; }
  .item-content { flex-direction: column; }
  .item-image { width: 100% !important; height: 200px !important; }
}
</style>

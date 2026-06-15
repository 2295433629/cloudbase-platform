<template>
  <router-view />
</template>

<script setup>
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* ========== 暗色主题 ========== */
:root.dark {
  --bg-color: #141414;
  --bg-color-light: #1a1a1a;
  --text-color: #e0e0e0;
  --border-color: #333;
}

:root.dark .el-card {
  --el-bg-color: var(--bg-color-light);
  --el-text-color-primary: var(--text-color);
}

:root.dark .el-table {
  --el-table-header-bg-color: #1e1e1e;
  --el-table-header-text-color: var(--text-color);
  --el-table-row-bg-color: var(--bg-color-light);
  --el-table-border-color: #333;
}

:root.dark .el-dialog {
  --el-bg-color: var(--bg-color-light);
  --el-text-color-primary: var(--text-color);
}

:root.dark .el-form-item__label {
  color: var(--text-color);
}

:root.dark .el-input__inner {
  --el-input-bg-color: var(--bg-color-light);
  --el-text-color-primary: var(--text-color);
}

:root.dark .el-select .el-input__wrapper {
  background-color: var(--bg-color-light);
}

:root.dark .el-pagination {
  --el-text-color-primary: var(--text-color);
}

:root.dark .el-tabs__item {
  color: var(--text-color);
}

:root.dark .el-tab-pane {
  color: var(--text-color);
}
</style>

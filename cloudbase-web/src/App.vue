<template>
  <router-view />
</template>

<script setup>
</script>

<style>
/* ========== 基础重置 ========== */
html, body, #app {
  height: 100%;
  margin: 0;
  padding: 0;
}

/* ========== 设计令牌 ========== */
:root {
  --cb-primary: #409EFF;
  --cb-success: #67C23A;
  --cb-warning: #E6A23C;
  --cb-danger: #F56C6C;
  --cb-info: #909399;
  --cb-bg: #f0f2f5;
  --cb-bg-light: #fafbfc;
  --cb-sidebar-bg: #1e2a3a;
  --cb-sidebar-hover: rgba(255, 255, 255, 0.06);
  --cb-sidebar-active: rgba(64, 158, 255, 0.15);
  --cb-card-radius: 10px;
  --cb-btn-radius: 6px;
  --cb-input-radius: 6px;
  --cb-tag-radius: 12px;
  --cb-dialog-radius: 14px;
  --cb-card-shadow: 0 1px 3px rgba(0, 0, 0, 0.04), 0 1px 2px rgba(0, 0, 0, 0.03);
  --cb-card-shadow-hover: 0 4px 16px rgba(0, 0, 0, 0.08);
}

/* ========== 全局 Element Plus 组件覆写 ========== */

/* 卡片：统一圆角 + 柔和阴影 + 去掉硬边框 */
.el-card {
  border-radius: var(--cb-card-radius) !important;
  border: none !important;
  box-shadow: var(--cb-card-shadow) !important;
  transition: box-shadow 0.3s ease;
}
.el-card:hover {
  box-shadow: var(--cb-card-shadow-hover) !important;
}
.el-card__header {
  border-bottom: 1px solid #f0f0f0 !important;
  padding: 14px 20px !important;
  font-weight: 600;
  font-size: 15px;
}

/* 按钮：统一圆角 */
.el-button {
  border-radius: var(--cb-btn-radius) !important;
  font-weight: 500;
}

/* 输入框 / 选择器 / 日期：统一圆角 */
.el-input__wrapper,
.el-textarea__inner,
.el-select .el-input__wrapper,
.el-input-number,
.el-date-editor {
  border-radius: var(--cb-input-radius) !important;
}

/* 标签：圆角 pill 风格 */
.el-tag {
  border-radius: var(--cb-tag-radius) !important;
}

/* 对话框：大圆角 */
.el-dialog {
  border-radius: var(--cb-dialog-radius) !important;
  overflow: hidden;
}
.el-dialog__header {
  border-bottom: 1px solid #f0f0f0;
  padding: 16px 20px !important;
  margin-right: 0 !important;
}
.el-dialog__footer {
  border-top: 1px solid #f0f0f0;
  padding: 12px 20px !important;
}
.el-dialog__body {
  padding: 20px !important;
}

/* 表格：去掉外边框和竖线，更现代 */
.el-table {
  --el-table-border-color: transparent;
}
.el-table th.el-table__cell {
  background-color: var(--cb-bg-light) !important;
  color: #1f2937 !important;
  font-weight: 600 !important;
  font-size: 13px;
  border-bottom: 2px solid #e5e7eb !important;
}
.el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: #fafbfc !important;
}
.el-table td.el-table__cell {
  border-bottom: 1px solid #f0f0f0 !important;
}
/* 去掉表格竖线，只保留横线 */
.el-table--border .el-table__cell {
  border-right: none !important;
}
.el-table--border::after,
.el-table::before {
  background-color: transparent !important;
}
.el-table__inner-wrapper::before {
  background-color: transparent !important;
}

/* 分页器 */
.el-pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
.el-pagination .btn-prev,
.el-pagination .btn-next,
.el-pagination .el-pager li {
  border-radius: var(--cb-btn-radius) !important;
}

/* 表单标签 */
.el-form-item__label {
  font-weight: 500;
  color: #374151;
}

/* 消息提示：圆角 */
.el-message {
  border-radius: 8px !important;
}

/* Popconfirm 圆角 */
.el-popconfirm {
  border-radius: 8px;
}

/* ========== 暗色主题 ========== */
:root.dark {
  --bg-color: #141414;
  --bg-color-light: #1a1a1a;
  --text-color: #e0e0e0;
  --border-color: #333;
  --cb-bg: #0d0d0d;
  --cb-bg-light: #1a1a1a;
  --cb-card-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  --cb-card-shadow-hover: 0 4px 16px rgba(0, 0, 0, 0.3);
}

:root.dark .el-card {
  --el-bg-color: var(--bg-color-light);
  --el-text-color-primary: var(--text-color);
}
:root.dark .el-card__header {
  border-bottom-color: #2a2a2a !important;
}

:root.dark .el-table {
  --el-table-header-bg-color: #1e1e1e;
  --el-table-header-text-color: var(--text-color);
  --el-table-row-bg-color: var(--bg-color-light);
  --el-table-border-color: transparent;
}
:root.dark .el-table th.el-table__cell {
  background-color: #1e1e1e !important;
  color: var(--text-color) !important;
  border-bottom-color: #333 !important;
}
:root.dark .el-table td.el-table__cell {
  border-bottom-color: #2a2a2a !important;
}
:root.dark .el-table--striped .el-table__body tr.el-table__row--striped td.el-table__cell {
  background-color: #1a1a1a !important;
}

:root.dark .el-dialog {
  --el-bg-color: var(--bg-color-light);
  --el-text-color-primary: var(--text-color);
}
:root.dark .el-dialog__header {
  border-bottom-color: #2a2a2a;
}
:root.dark .el-dialog__footer {
  border-top-color: #2a2a2a;
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

/* ========== 滚动条美化 ========== */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-track {
  background: transparent;
}
::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
:root.dark ::-webkit-scrollbar-thumb {
  background: #444;
}
:root.dark ::-webkit-scrollbar-thumb:hover {
  background: #555;
}
</style>

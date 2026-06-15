<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">5 0 0</h1>
      <h2 class="error-desc">服务器内部错误</h2>
      <p class="error-message">服务可能暂时不可用，请稍后重试</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
      <el-button @click="reload">刷新页面</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useRouter} from 'vue-router'

const router = useRouter()

function goHome(): void {
  router.push('/')
}

function reload(): void {
  window.location.reload()
}
</script>

<style scoped>
.error-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

:root.dark .error-page {
  background: var(--bg-color);
}

.error-content {
  text-align: center;
}

.error-code {
  font-size: 120px;
  font-weight: bold;
  color: #f56c6c;
  letter-spacing: 10px;
  margin: 0;
}

.error-desc {
  font-size: 28px;
  color: #333;
  margin: 20px 0 10px;
}

:root.dark .error-desc {
  color: var(--text-color);
}

.error-message {
  font-size: 16px;
  color: #909399;
  margin-bottom: 30px;
}
</style>

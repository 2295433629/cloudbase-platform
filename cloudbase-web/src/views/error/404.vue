<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">4 0 4</h1>
      <h2 class="error-desc">页面不存在</h2>
      <p class="error-message">请检查您访问的链接是否正确</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
      <el-button @click="goBack">返回上一页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useRouter} from 'vue-router'

const router = useRouter()

function goHome(): void {
  router.push('/')
}

function goBack(): void {
  router.back()
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
  font-weight: 800;
  background: linear-gradient(135deg, #409EFF, #79bbff);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 10px;
  margin: 0;
}

.error-desc {
  font-size: 24px;
  color: #374151;
  margin: 16px 0 8px;
  font-weight: 600;
}

:root.dark .error-desc {
  color: var(--text-color);
}

.error-message {
  font-size: 15px;
  color: #909399;
  margin-bottom: 32px;
}
</style>

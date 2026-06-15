<template>
  <div class="error-page">
    <div class="error-content">
      <h1 class="error-code">4 0 3</h1>
      <h2 class="error-desc">权限不足，无法访问此页面</h2>
      <p class="error-message">{{ message }}</p>
      <el-button type="primary" @click="goHome">返回首页</el-button>
      <el-button @click="goBack">返回上一页</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useRoute, useRouter} from 'vue-router'

const route = useRoute()
const router = useRouter()

const message = route.query?.message || '请联系管理员分配相应权限'

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
  font-weight: bold;
  color: #409eff;
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

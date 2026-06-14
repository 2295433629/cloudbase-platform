<template>
  <el-container class="layout-container">
    <Sidebar :collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
    <el-main class="layout-main">
      <router-view />
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import Sidebar from './Sidebar.vue'

const isCollapse = ref(false)
</script>

<style scoped>
.layout-container { height: 100vh; }
.layout-main {
  background: #f0f2f5;
  padding: 20px;
}
</style>

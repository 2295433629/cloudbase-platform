<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="40" color="#409EFF"><User /></el-icon>
            <div>
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.userCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="40" color="#67C23A"><UserFilled /></el-icon>
            <div>
              <div class="stat-label">角色总数</div>
              <div class="stat-value">{{ stats.roleCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="40" color="#E6A23C"><Menu /></el-icon>
            <div>
              <div class="stat-label">菜单总数</div>
              <div class="stat-value">{{ stats.menuCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <el-icon :size="40" color="#F56C6C"><Notebook /></el-icon>
            <div>
              <div class="stat-label">字典总数</div>
              <div class="stat-value">{{ stats.dictCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px">
      <template #header>
        <span>欢迎使用 CloudBase 基础平台</span>
      </template>
      <p>当前登录用户：{{ userStore.userInfo?.realName || '管理员' }}</p>
      <p>技术栈：Vue3 + Element Plus + SpringBoot 3 + MyBatis Plus + MySQL + Redis</p>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const stats = reactive({
  userCount: 1,
  roleCount: 0,
  menuCount: 8,
  dictCount: 0
})
</script>

<style scoped>
.stat-card { cursor: pointer; }
.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-label { font-size: 14px; color: #909399; }
.stat-value { font-size: 28px; font-weight: bold; color: #333; }
</style>

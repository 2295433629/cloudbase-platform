<template>
  <div class="page-container">
    <el-table :data="tableData" border>
      <el-table-column prop="realName" label="用户名称" width="120" />
      <el-table-column prop="account" label="账号" width="120" />
      <el-table-column prop="ipAddress" label="IP地址" width="150" />
      <el-table-column label="剩余时间" width="120">
        <template #default="{ row }">
          {{ formatTime(row.expire) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-popconfirm title="强制下线?" @confirm="handleLogout(row)">
            <template #reference><el-button size="small" type="danger">下线</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <div style="margin-top:10px;color:#999">当前在线: {{ tableData.length }} 人</div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import api from '@/api'

const tableData = ref([])

const loadData = () => {
  api.post('/sys/online/list').then(res => { tableData.value = res })
}
loadData()

const formatTime = (s) => {
  if (!s || s < 0) return '已过期'
  const h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60)
  return `${h}小时${m}分钟`
}

const handleLogout = (row) => {
  api.post('/sys/online/forceLogout', { token: row.token }).then(() => { ElMessage.success('已下线'); loadData() })
}
</script>

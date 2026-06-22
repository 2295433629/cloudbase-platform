<template>
  <div class="online-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>在线用户</span>
          <div>
            <el-tag type="success" size="large" effect="plain" style="margin-right: 12px">
              <el-icon style="vertical-align: middle; margin-right: 4px"><User /></el-icon>
              当前在线: {{ tableData.length }} 人
            </el-tag>
            <el-button type="primary" size="small" @click="fetchData">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>
      <el-table :data="pagedData" stripe v-loading="loading">
        <el-table-column type="index" label="#" width="55" align="center" />
        <el-table-column prop="realName" label="用户名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="account" label="账号" min-width="120" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" min-width="150" show-overflow-tooltip />
        <el-table-column label="登录部门" min-width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.deptName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="剩余时间" width="140" align="center">
          <template #default="{ row }">
            <el-tag :type="getExpireType(row.expire)" size="small">
              {{ formatTime(row.expire) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right" align="center">
          <template #default="{ row }">
            <el-popconfirm title="确定强制下线该用户？" @confirm="handleLogout(row)">
              <template #reference>
                <el-button link type="danger" size="small">下线</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :total="tableData.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handlePageChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {ElMessage} from 'element-plus'
import {Refresh, User} from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const tableData = ref([])
const pageNo = ref(1)
const pageSize = ref(10)

const pagedData = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value
  return tableData.value.slice(start, start + pageSize.value)
})

function handlePageChange() {
  // 当页码或每页条数变化时，确保不超出数据范围
  const maxPage = Math.max(1, Math.ceil(tableData.value.length / pageSize.value))
  if (pageNo.value > maxPage) pageNo.value = maxPage
}

async function fetchData() {
  loading.value = true
  try {
    const res = await api.post('/sys/online/list')
    tableData.value = res || []
  } catch {
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const formatTime = (s) => {
  if (!s || s < 0) return '已过期'
  const h = Math.floor(s / 3600), m = Math.floor((s % 3600) / 60)
  if (h > 0) return `${h}小时${m}分钟`
  return `${m}分钟`
}

const getExpireType = (s) => {
  if (!s || s < 0) return 'info'
  if (s < 300) return 'danger'
  if (s < 1800) return 'warning'
  return 'success'
}

const handleLogout = (row) => {
  api.post('/sys/online/forceLogout', { token: row.token }).then(() => {
    ElMessage.success('已强制下线')
    fetchData()
  })
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.online-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

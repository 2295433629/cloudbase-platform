<template>
  <div class="operlog-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="模块">
          <el-input v-model="query.module" placeholder="请输入模块名称" clearable />
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="query.operUserName" placeholder="请输入操作人" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="danger" @click="handleClear">清空日志</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="module" label="模块" width="120" />
        <el-table-column prop="operType" label="操作类型" width="100">
          <template #default="{ row }">
            <el-tag :type="operTypeTag(row.operType)" size="small">{{ row.operType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="method" label="方法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operUserName" label="操作人" width="100" />
        <el-table-column prop="operIp" label="IP" width="140" />
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.success === 1 ? 'success' : 'danger'" size="small">
              {{ row.success === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="180" />
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getOperLogPage, clearOperLog } from '@/api/system'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ module: '', operUserName: '', pageNo: 1, pageSize: 20 })

const operTypeTag = (type) => ({
  INSERT: 'primary', UPDATE: 'warning', DELETE: 'danger', QUERY: 'info', EXPORT: 'success', OTHER: 'info'
}[type] || 'info')

async function fetchData() {
  loading.value = true
  try {
    const res = await getOperLogPage(query)
    tableData.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.module = ''
  query.operUserName = ''
  query.pageNo = 1
  fetchData()
}

async function handleClear() {
  await ElMessageBox.confirm('确认清空所有操作日志？', '警告', { type: 'warning' })
  await clearOperLog()
  ElMessage.success('已清空')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.operlog-container { display: flex; flex-direction: column; gap: 16px; }
</style>

<template>
  <div class="loginlog-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="用户名称">
          <el-input v-model="query.userName" placeholder="请输入用户名称" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 360px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>登录日志</span>
          <div>
            <el-button type="success" size="small" @click="handleExport" :disabled="tableData.length === 0">
              <el-icon><Download /></el-icon>导出
            </el-button>
            <el-button type="danger" size="small" @click="handleClear">
              <el-icon><Delete /></el-icon>清空
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
      >
        <el-table-column type="index" label="#" width="55" align="center" :index="(p) => (pageNo - 1) * 20 + p + 1" />
        <el-table-column prop="userName" label="用户名称" width="120" show-overflow-tooltip />
        <el-table-column prop="ipAddress" label="IP地址" width="150" />
        <el-table-column prop="os" label="操作系统" width="130" show-overflow-tooltip />
        <el-table-column prop="browser" label="浏览器" width="130" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="消息" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="180" />
      </el-table>
    </el-card>
    <el-pagination
      v-model:current-page="pageNo"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[20, 50, 100]"
      :hide-on-single-page="false"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 16px; justify-content: flex-end"
    />
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import {clearLoginLog, getLoginLogPage} from '@/api/system'
import * as XLSX from 'xlsx'
import {Delete, Download} from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dateRange = ref<string[]>([])
const query = reactive({ userName: '', status: undefined as number | undefined })

async function fetchData() {
  loading.value = true
  try {
    const params: any = { pageNo: pageNo.value, pageSize: pageSize.value, ...query }
    if (dateRange.value && dateRange.value.length === 2) {
      params.beginTime = dateRange.value[0]
      params.endTime = dateRange.value[1]
    }
    const res = await getLoginLogPage(params)
    tableData.value = res.rows || []
    total.value = Number(res.total) || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.userName = ''
  query.status = undefined
  dateRange.value = []
  pageNo.value = 1
  fetchData()
}

async function handleClear() {
  await ElMessageBox.confirm('确认清空所有登录日志？此操作不可恢复。', '警告', { type: 'warning' })
  await clearLoginLog()
  ElMessage.success('已清空')
  fetchData()
}

// 导出 Excel
function handleExport() {
  const exportData = tableData.value.map((row: any) => ({
    '用户名称': row.userName,
    'IP地址': row.ipAddress,
    '操作系统': row.os,
    '浏览器': row.browser,
    '状态': row.status === 1 ? '成功' : '失败',
    '消息': row.msg,
    '登录时间': row.loginTime
  }))

  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '登录日志')
  ws['!cols'] = [
    { wch: 15 }, { wch: 18 }, { wch: 18 }, { wch: 18 }, { wch: 8 }, { wch: 40 }, { wch: 20 }
  ]
  XLSX.writeFile(wb, `登录日志_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.loginlog-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

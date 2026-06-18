<template>
  <div class="operlog-container">
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="模块"><el-input v-model="query.module" placeholder="请输入模块名称" clearable /></el-form-item>
        <el-form-item label="操作人"><el-input v-model="query.operUserName" placeholder="请输入操作人" clearable /></el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="query.operType" placeholder="全部" clearable style="width: 120px">
            <el-option label="新增" value="INSERT" /><el-option label="编辑" value="UPDATE" /><el-option label="删除" value="DELETE" />
            <el-option label="查询" value="QUERY" /><el-option label="导出" value="EXPORT" /><el-option label="授权" value="GRANT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-select v-model="query.status" placeholder="全部" clearable style="width: 100px"><el-option label="成功" :value="1" /><el-option label="失败" :value="0" /></el-select></el-form-item>
        <el-form-item><el-button type="primary" @click="fetchData">查询</el-button><el-button @click="resetQuery">重置</el-button></el-form-item>
      </el-form>
    </el-card>
    <el-card>
      <template #header><div class="card-header"><span>操作日志</span><div><el-button type="success" size="small" @click="handleExport" :disabled="tableData.length === 0"><el-icon><Download /></el-icon>导出</el-button><el-button type="danger" size="small" @click="handleClear"><el-icon><Delete /></el-icon>清空</el-button></div></div></template>
      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="#" width="55" align="center" :index="(p) => (query.pageNo - 1) * query.pageSize + p + 1" />
        <el-table-column prop="module" label="模块" width="120" show-overflow-tooltip />
        <el-table-column label="操作类型" width="100" align="center"><template #default="{ row }"><el-tag :type="operTypeTag(row.operType)" size="small">{{ row.operType }}</el-tag></template></el-table-column>
        <el-table-column prop="method" label="请求方法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="operUserName" label="操作人" width="100" />
        <el-table-column prop="operIp" label="IP地址" width="140" />
        <el-table-column prop="costTime" label="耗时(ms)" width="90" align="right"><template #default="{ row }"><span :style="{ color: row.costTime > 3000 ? '#f56c6c' : '' }">{{ row.costTime }}</span></template></el-table-column>
        <el-table-column label="状态" width="80" align="center"><template #default="{ row }"><el-tag :type="row.success === 1 || row.status === 1 ? 'success' : 'danger'" size="small">{{ (row.success === 1 || row.status === 1) ? '成功' : '失败' }}</el-tag></template></el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="180" />
      </el-table>
      <el-pagination v-model:current-page="query.pageNo" v-model:page-size="query.pageSize" :total="total" :page-sizes="[20, 50, 100]" layout="total, sizes, prev, pager, next" @size-change="fetchData" @current-change="fetchData" style="margin-top: 20px; justify-content: flex-end" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {clearOperLog, getOperLogPage} from '@/api/system'
import {ElMessage, ElMessageBox} from 'element-plus'
import * as XLSX from 'xlsx'
import {Delete, Download} from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ module: '', operUserName: '', operType: '', status: null as number | null, pageNo: 1, pageSize: 20 })
const operTypeTag = (type: string) => ({ INSERT: 'primary', UPDATE: 'warning', DELETE: 'danger', QUERY: 'info', EXPORT: 'success', GRANT: 'success', OTHER: 'info' }[type] || 'info')

async function fetchData() { loading.value = true; try { const res = await getOperLogPage(query); tableData.value = res.rows; total.value = res.total } finally { loading.value = false } }
function resetQuery() { query.module = ''; query.operUserName = ''; query.operType = ''; query.status = null; query.pageNo = 1; fetchData() }
async function handleClear() { await ElMessageBox.confirm('确认清空所有操作日志？此操作不可恢复。', '警告', { type: 'warning' }); await clearOperLog(); ElMessage.success('已清空'); fetchData() }
function handleExport() { const exportData = tableData.value.map((row: any) => ({ '模块': row.module, '操作类型': row.operType, '请求方法': row.method || '', '操作人': row.operUserName, 'IP地址': row.operIp, '耗时(ms)': row.costTime, '状态': (row.success === 1 || row.status === 1) ? '成功' : '失败', '操作时间': row.operTime })); const ws = XLSX.utils.json_to_sheet(exportData); const wb = XLSX.utils.book_new(); XLSX.utils.book_append_sheet(wb, ws, '操作日志'); ws['!cols'] = [{ wch: 15 }, { wch: 12 }, { wch: 30 }, { wch: 12 }, { wch: 18 }, { wch: 10 }, { wch: 8 }, { wch: 20 }]; XLSX.writeFile(wb, `操作日志_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`); ElMessage.success('导出成功') }
onMounted(() => { fetchData() })
</script>

<style scoped>
.operlog-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

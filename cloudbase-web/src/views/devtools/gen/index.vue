<template>
  <div class="page-container">
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 已导入表（主工作区） -->
      <el-tab-pane label="已导入表" name="imported">
        <div class="search-bar">
          <el-input v-model="importedSearch" placeholder="搜索表名" style="width: 250px" clearable @clear="loadImportedTables" @keyup.enter="loadImportedTables" />
          <el-button type="primary" @click="loadImportedTables">查询</el-button>
          <el-button type="success" :disabled="selectedImported.length === 0" @click="batchGenerate">
            批量生成（{{ selectedImported.length }}）
          </el-button>
        </div>
        <el-table :data="importedTables" @selection-change="(rows) => selectedImported = rows" v-loading="importedLoading">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="tableName" label="表名" width="180" />
          <el-table-column prop="tableComment" label="表描述" show-overflow-tooltip />
          <el-table-column prop="className" label="实体类名" width="160" />
          <el-table-column prop="packageName" label="包名" width="220" show-overflow-tooltip />
          <el-table-column prop="functionName" label="功能名" width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="280" fixed="right">
            <template #default="{ row }">
              <el-button size="small" type="primary" @click="previewCode(row)">预览</el-button>
              <el-button size="small" type="success" @click="generateSingle(row)">生成</el-button>
              <el-button size="small" @click="showDetail(row)">配置</el-button>
              <el-button size="small" @click="syncTable(row)">同步</el-button>
              <el-button size="small" type="danger" @click="deleteTable(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-pagination
          v-model:current-page="importedPageNo"
          v-model:page-size="importedPageSize"
          :total="importedTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          :hide-on-single-page="false"
          style="margin-top: 20px; justify-content: flex-end"
          @size-change="loadImportedTables"
          @current-change="loadImportedTables"
        />
      </el-tab-pane>

      <!-- 数据库表（导入源） -->
      <el-tab-pane label="数据库表" name="database">
        <div class="search-bar">
          <el-input v-model="dbSearch" placeholder="搜索表名" style="width: 250px" clearable />
          <el-button type="primary" @click="loadDbTables">查询</el-button>
          <el-button type="success" :disabled="selectedDb.length === 0" @click="importTables">
            导入选中（{{ selectedDb.length }}）
          </el-button>
        </div>
        <el-table :data="filteredDbTables" @selection-change="(rows) => selectedDb = rows" v-loading="dbLoading">
          <el-table-column type="selection" width="50" />
          <el-table-column prop="tableName" label="表名" width="220" />
          <el-table-column prop="tableComment" label="表描述" show-overflow-tooltip />
        </el-table>
        <el-pagination
          v-model:current-page="dbPageNo"
          v-model:page-size="dbPageSize"
          :total="filteredDbTablesRaw.length"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          background
          :hide-on-single-page="false"
          style="margin-top: 20px; justify-content: flex-end"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 代码预览弹窗 -->
    <el-dialog title="代码预览" v-model="previewVisible" width="70%" top="5vh">
      <el-tabs v-model="previewTab" type="card">
        <el-tab-pane v-for="(code, name) in previewContent" :key="name" :label="name" :name="name">
          <pre class="code-block"><code>{{ code }}</code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>

    <!-- 表配置弹窗 -->
    <el-dialog title="表生成配置" v-model="detailVisible" width="85%" top="3vh">
      <el-form :model="detailForm" label-width="100px" v-if="detailForm">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="表名">
              <el-input v-model="detailForm.tableName" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="实体类名">
              <el-input v-model="detailForm.className" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="包名">
              <el-input v-model="detailForm.packageName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="模块名">
              <el-input v-model="detailForm.moduleName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="业务名">
              <el-input v-model="detailForm.businessName" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="功能名">
              <el-input v-model="detailForm.functionName" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <h4 style="margin: 12px 0 8px">列配置</h4>
      <el-table :data="detailForm?.columns" size="small" border max-height="400" v-if="detailForm">
        <el-table-column prop="columnName" label="列名" width="140" />
        <el-table-column prop="columnComment" label="列描述" width="140" />
        <el-table-column prop="javaField" label="Java字段" width="130" />
        <el-table-column prop="javaType" label="Java类型" width="120" />
        <el-table-column label="主键" width="60" align="center">
          <template #default="{ row }"><el-tag v-if="row.isPk === 1" type="warning" size="small">是</el-tag></template>
        </el-table-column>
        <el-table-column label="插入" width="60" align="center">
          <template #default="{ row }"><el-switch v-model="row.isInsert" :active-value="1" :inactive-value="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="编辑" width="60" align="center">
          <template #default="{ row }"><el-switch v-model="row.isEdit" :active-value="1" :inactive-value="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="列表" width="60" align="center">
          <template #default="{ row }"><el-switch v-model="row.isList" :active-value="1" :inactive-value="0" size="small" /></template>
        </el-table-column>
        <el-table-column label="查询" width="60" align="center">
          <template #default="{ row }"><el-switch v-model="row.isQuery" :active-value="1" :inactive-value="0" size="small" /></template>
        </el-table-column>
        <el-table-column prop="queryType" label="查询方式" width="100">
          <template #default="{ row }">
            <el-select v-model="row.queryType" size="small" style="width:80px">
              <el-option label="=" value="=" /><el-option label="LIKE" value="LIKE" />
              <el-option label="!=" value="!=" /><el-option label=">" value=">" /><el-option label="<" value="<" />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column prop="htmlType" label="显示类型" width="110">
          <template #default="{ row }">
            <el-select v-model="row.htmlType" size="small" style="width:90px">
              <el-option label="input" value="input" /><el-option label="textarea" value="textarea" />
              <el-option label="select" value="select" /><el-option label="radio" value="radio" />
              <el-option label="date" value="date" /><el-option label="image" value="image" />
            </el-select>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="detailVisible = false">取消</el-button>
        <el-button type="primary" @click="saveDetail" :loading="detailSaving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import api from '@/api'

// ========================= 已导入表 =========================
const activeTab = ref('imported')
const importedTables = ref([])
const importedLoading = ref(false)
const importedSearch = ref('')
const importedPageNo = ref(1)
const importedPageSize = ref(20)
const importedTotal = ref(0)
const selectedImported = ref([])

async function loadImportedTables() {
  importedLoading.value = true
  try {
    const res = await api.post('/sys/gen/page', {
      pageNo: importedPageNo.value,
      pageSize: importedPageSize.value,
      tableName: importedSearch.value || undefined
    })
    importedTables.value = res.rows || []
    importedTotal.value = Number(res.total) || 0
  } finally {
    importedLoading.value = false
  }
}

// ========================= 数据库表 =========================
const dbTables = ref([])
const dbLoading = ref(false)
const dbSearch = ref('')
const dbPageNo = ref(1)
const dbPageSize = ref(20)
const selectedDb = ref([])

const filteredDbTablesRaw = computed(() => {
  if (!dbSearch.value) return dbTables.value
  return dbTables.value.filter(t => t.tableName.includes(dbSearch.value))
})
const filteredDbTables = computed(() => {
  const start = (dbPageNo.value - 1) * dbPageSize.value
  return filteredDbTablesRaw.value.slice(start, start + dbPageSize.value)
})

async function loadDbTables() {
  dbLoading.value = true
  try {
    const res = await api.post('/sys/gen/db/list')
    dbTables.value = res || []
  } finally {
    dbLoading.value = false
  }
}

async function importTables() {
  const names = selectedDb.value.map(r => r.tableName)
  try {
    await api.post('/sys/gen/import', { tableNames: names })
    ElMessage.success(`成功导入 ${names.length} 张表`)
    selectedDb.value = []
    activeTab.value = 'imported'
    loadImportedTables()
    loadDbTables()
  } catch (e) {
    ElMessage.error('导入失败')
  }
}

// ========================= 操作 =========================
async function previewCode(row) {
  try {
    const res = await api.post('/sys/gen/preview', { tableIds: [row.tableId] })
    previewContent.value = res
    previewTab.value = Object.keys(res)[0] || ''
    previewVisible.value = true
  } catch (e) {
    ElMessage.error('预览失败')
  }
}

function generateSingle(row) {
  downloadZip([row.tableId])
}

function batchGenerate() {
  const ids = selectedImported.value.map(r => r.tableId)
  downloadZip(ids)
}

function downloadZip(tableIds) {
  api.post('/sys/gen/generate', { tableIds }, { responseType: 'blob' }).then(blob => {
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'generated.zip'
    a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('代码生成成功')
  }).catch(() => ElMessage.error('生成失败'))
}

async function syncTable(row) {
  try {
    await ElMessageBox.confirm(`确认同步表 "${row.tableName}" 的列信息？`, '提示')
    await api.post('/sys/gen/sync', { tableId: row.tableId })
    ElMessage.success('同步成功')
    loadImportedTables()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('同步失败')
  }
}

async function deleteTable(row) {
  try {
    await ElMessageBox.confirm(`确认删除表 "${row.tableName}" 的生成配置？`, '警告', { type: 'warning' })
    await api.post('/sys/gen/delete', { tableIds: [row.tableId] })
    ElMessage.success('删除成功')
    loadImportedTables()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// ========================= 表配置弹窗 =========================
const detailVisible = ref(false)
const detailForm = ref(null)
const detailSaving = ref(false)

async function showDetail(row) {
  try {
    const res = await api.post('/sys/gen/detail', { tableId: row.tableId })
    detailForm.value = res
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载配置失败')
  }
}

async function saveDetail() {
  detailSaving.value = true
  try {
    await api.post('/sys/gen/edit', detailForm.value)
    ElMessage.success('保存成功')
    detailVisible.value = false
    loadImportedTables()
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    detailSaving.value = false
  }
}

// ========================= 代码预览弹窗 =========================
const previewVisible = ref(false)
const previewTab = ref('')
const previewContent = ref({})

// ========================= Tab 切换 =========================
function handleTabChange(tab) {
  if (tab === 'imported') loadImportedTables()
  else loadDbTables()
}

onMounted(() => {
  loadImportedTables()
  loadDbTables()
})
</script>

<style scoped>
.code-block {
  max-height: 500px;
  overflow: auto;
  background: #f5f7fa;
  padding: 12px;
  border-radius: 4px;
  font-size: 13px;
  line-height: 1.6;
  margin: 0;
}
</style>

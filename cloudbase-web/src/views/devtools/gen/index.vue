<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.tableName" placeholder="表名" style="width:250px" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
    </div>
    <el-table :data="pagedTables" @selection-change="handleSelect">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="tableName" label="表名" width="200" />
      <el-table-column prop="tableComment" label="表注释" show-overflow-tooltip />
      <el-table-column label="操作" width="150">
        <template #default="{ row }">
          <el-button size="small" @click="previewCode(row)">预览</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      v-model:current-page="pageNo"
      v-model:page-size="pageSize"
      :total="filteredTables.length"
      :page-sizes="[10, 20, 50]"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="handlePageChange"
      @current-change="handlePageChange"
      style="margin-top: 20px; justify-content: flex-end"
    />
    <el-button type="primary" style="margin-top:10px" :disabled="selected.length===0" @click="generateCode">生成（{{ selected.length }}张表）</el-button>

    <el-dialog title="代码预览" v-model="previewVisible" width="70%" top="5vh">
      <el-tabs v-model="previewTab" type="card">
        <el-tab-pane v-for="(code, name) in previewContent" :key="name" :label="name" :name="name">
          <pre style="max-height:500px;overflow:auto;background:#f5f5f5;padding:12px"><code>{{ code }}</code></pre>
        </el-tab-pane>
      </el-tabs>
    </el-dialog>
  </div>
</template>

<script setup>
import {computed, onMounted, reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import api from '@/api'

const tables = ref([])
const selected = ref([])
const searchForm = reactive({ tableName: '' })
const previewVisible = ref(false)
const previewTab = ref('')
const previewContent = ref({})

const filteredTables = computed(() => {
  if (!searchForm.tableName) return tables.value
  return tables.value.filter(t => t.tableName.includes(searchForm.tableName))
})

const pageNo = ref(1)
const pageSize = ref(10)

const pagedTables = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value
  return filteredTables.value.slice(start, start + pageSize.value)
})

function handlePageChange() {
  const maxPage = Math.max(1, Math.ceil(filteredTables.value.length / pageSize.value))
  if (pageNo.value > maxPage) pageNo.value = maxPage
}

const loadData = () => { api.post('/sys/gen/db/list').then(res => { tables.value = res }) }
onMounted(loadData)

const handleSelect = (rows) => { selected.value = rows }

const previewCode = (row) => {
  api.post('/sys/gen/preview', { tableNames: [row.tableName] }).then(res => {
    previewContent.value = res; previewTab.value = Object.keys(res)[0]; previewVisible.value = true
  })
}

const generateCode = () => {
  const names = selected.value.map(r => r.tableName)
  api.post('/sys/gen/generate', { tableNames: names }, { responseType: 'blob' }).then(blob => {
    const url = window.URL.createObjectURL(blob)
    const a = document.createElement('a'); a.href = url; a.download = 'generated.zip'; a.click()
    window.URL.revokeObjectURL(url)
    ElMessage.success('代码生成成功')
  })
}
</script>

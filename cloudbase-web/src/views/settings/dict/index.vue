<template>
  <div class="dict-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="字典类型">
          <el-input v-model="query.dictType" placeholder="请输入字典类型" clearable />
        </el-form-item>
        <el-form-item label="字典标签">
          <el-input v-model="query.dictLabel" placeholder="请输入字典标签" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>字典列表</span>
          <div>
            <el-upload
              :show-file-list="false"
              :before-upload="handleImport"
              accept=".xlsx,.xls"
            >
              <el-button type="warning" size="small">
                <el-icon><Upload /></el-icon>导入
              </el-button>
            </el-upload>
            <el-button type="success" size="small" @click="handleExport" :disabled="tableData.length === 0">
              <el-icon><Download /></el-icon>导出
            </el-button>
            <el-button type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增字典
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
      >
        <el-table-column type="index" label="#" width="55" align="center" :index="(p) => (query.pageNo - 1) * query.pageSize + p + 1" />
        <el-table-column prop="dictType" label="字典类型" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dictLabel" label="字典标签" min-width="150" show-overflow-tooltip />
        <el-table-column prop="dictValue" label="字典值" min-width="120" show-overflow-tooltip />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该字典？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-pagination
      v-model:current-page="query.pageNo"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50, 100]"
      :hide-on-single-page="false"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑字典' : '新增字典'" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="form.dictType" placeholder="如 sys_user_sex" />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="form.dictLabel" placeholder="显示文字，如 男" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="form.dictValue" placeholder="实际值，如 1" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {addDict, batchImportDict, deleteDict, editDict, getDictPage, updateDictStatus} from '@/api/system'
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import * as XLSX from 'xlsx'
import {Download, Plus, Upload} from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ dictType: '', dictLabel: '', status: undefined as number | undefined, pageNo: 1, pageSize: 20 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  dictId: undefined as number | string | undefined,
  dictType: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }],
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDictPage(query)
    tableData.value = res.rows || []
    total.value = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.dictType = ''
  query.dictLabel = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { dictId: undefined, dictType: '', dictLabel: '', dictValue: '', sort: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(form, {
    dictId: row.dictId, dictType: row.dictType, dictLabel: row.dictLabel,
    dictValue: row.dictValue, sort: row.sort, status: row.status, remark: row.remark || ''
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await editDict(form)
      ElMessage.success('编辑成功')
    } else {
      await addDict(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

async function handleStatus(row: any) {
  const status = row.status === 1 ? 0 : 1
  await updateDictStatus({ dictId: row.dictId, status })
  ElMessage.success('状态更新成功')
  fetchData()
}

async function handleDelete(row: any) {
  await deleteDict({ id: row.dictId })
  ElMessage.success('删除成功')
  fetchData()
}

// 导出 Excel
function handleExport() {
  const exportData = tableData.value.map((row: any) => ({
    '字典类型': row.dictType,
    '字典标签': row.dictLabel,
    '字典值': row.dictValue,
    '排序': row.sort,
    '状态': row.status === 1 ? '启用' : '禁用',
    '备注': row.remark || '',
    '创建时间': row.createTime
  }))

  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '字典列表')
  ws['!cols'] = [
    { wch: 18 }, { wch: 15 }, { wch: 15 }, { wch: 8 }, { wch: 8 }, { wch: 30 }, { wch: 20 }
  ]
  XLSX.writeFile(wb, `字典列表_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`)
  ElMessage.success('导出成功')
}

// 导入 Excel
function handleImport(file: File): boolean {
  const reader = new FileReader()
  reader.onload = async (e) => {
    try {
      const data = new Uint8Array(e.target?.result as unknown as Uint8Array)
      const workbook = XLSX.read(data, { type: 'array' })
      const sheet = workbook.Sheets[workbook.SheetNames[0]]
      const json: any[] = XLSX.utils.sheet_to_json(sheet)

      if (json.length === 0) {
        ElMessage.warning('导入文件为空')
        return
      }

      // 校验并转换
      const importData = json.map((item: any) => ({
        dictType: String(item['字典类型'] || item['dictType'] || ''),
        dictLabel: String(item['字典标签'] || item['dictLabel'] || ''),
        dictValue: String(item['字典值'] || item['dictValue'] || ''),
        sort: Number(item['排序'] || item['sort'] || 0),
        status: (item['状态'] === '禁用' || item['status'] === 0) ? 0 : 1,
        remark: String(item['备注'] || item['remark'] || '')
      }))

      // 过滤空行
      const validData = importData.filter(d => d.dictType && d.dictLabel && d.dictValue)
      if (validData.length === 0) {
        ElMessage.warning('没有有效的数据行')
        return
      }
      ElMessage.info(`解析到 ${validData.length} 条有效数据，正在导入...`)

      // 调用后端批量导入接口
      await batchImportDict(validData)
      ElMessage.success(`成功导入 ${validData.length} 条数据`)
      fetchData()
    } catch {
      ElMessage.error('导入失败，请检查文件格式')
    }
  }
  reader.readAsArrayBuffer(file)
  return false
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.dict-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

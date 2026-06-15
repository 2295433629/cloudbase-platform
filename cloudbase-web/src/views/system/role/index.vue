<template>
  <div class="role-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="角色名称">
          <el-input v-model="query.roleName" placeholder="请输入角色名称" clearable />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="query.roleCode" placeholder="请输入角色编码" clearable />
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
          <span>角色列表</span>
          <div>
            <el-button type="success" size="small" @click="handleExport" :disabled="tableData.length === 0">
              <el-icon><Download /></el-icon>导出
            </el-button>
            <el-button type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增角色
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
        <el-table-column prop="roleName" label="角色名称" min-width="130" show-overflow-tooltip />
        <el-table-column prop="roleCode" label="角色编码" min-width="130" show-overflow-tooltip />
        <el-table-column label="数据权限" width="140">
          <template #default="{ row }">
            <el-tag size="small">{{ dataScopeMap[row.dataScope] || '全部' }}</el-tag>
          </template>
        </el-table-column>
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
            <el-popconfirm title="确定删除该角色？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" placeholder="请输入角色编码（如 admin）" />
        </el-form-item>
        <el-form-item label="数据权限">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option label="全部数据权限" :value="1" />
            <el-option label="自定义数据权限" :value="2" />
            <el-option label="本部门数据权限" :value="3" />
            <el-option label="本部门及以下" :value="4" />
            <el-option label="仅本人权限" :value="5" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
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
import {addRole, deleteRole, editRole, getRolePage, updateRoleStatus} from '@/api/system'
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import * as XLSX from 'xlsx'
import {Download, Plus} from '@element-plus/icons-vue'

const dataScopeMap: Record<number, string> = { 1: '全部', 2: '自定义', 3: '本部门', 4: '本部门及以下', 5: '仅本人' }

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ roleName: '', roleCode: '', status: undefined as number | undefined, pageNo: 1, pageSize: 20 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  roleId: undefined as number | undefined,
  roleName: '',
  roleCode: '',
  dataScope: 1,
  sort: 0,
  status: 1,
  remark: ''
})

const formRules: FormRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRolePage(query)
    tableData.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.roleName = ''
  query.roleCode = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { roleId: null, roleName: '', roleCode: '', dataScope: 1, sort: 0, status: 1, remark: '' })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(form, {
    roleId: row.roleId,
    roleName: row.roleName,
    roleCode: row.roleCode,
    dataScope: row.dataScope,
    sort: row.sort,
    status: row.status,
    remark: row.remark || ''
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
      await editRole(form)
      ElMessage.success('编辑成功')
    } else {
      await addRole(form)
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
  await updateRoleStatus({ roleId: row.roleId, status })
  ElMessage.success('状态更新成功')
  fetchData()
}

async function handleDelete(row: any) {
  await deleteRole({ id: row.roleId })
  ElMessage.success('删除成功')
  fetchData()
}

// 导出 Excel
function handleExport() {
  const exportData = tableData.value.map((row: any) => ({
    '角色名称': row.roleName,
    '角色编码': row.roleCode,
    '数据权限': dataScopeMap[row.dataScope] || '全部',
    '排序': row.sort,
    '状态': row.status === 1 ? '启用' : '禁用',
    '备注': row.remark || '',
    '创建时间': row.createTime
  }))

  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '角色列表')

  ws['!cols'] = [
    { wch: 15 }, { wch: 15 }, { wch: 15 }, { wch: 8 }, { wch: 8 }, { wch: 30 }, { wch: 20 }
  ]

  XLSX.writeFile(wb, `角色列表_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.role-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

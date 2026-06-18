<template>
  <div class="dept-container">
    <el-card class="search-card">
      <div class="toolbar">
        <el-input v-model="searchName" placeholder="部门名称" style="width: 200px" clearable @clear="fetchData" />
        <el-button type="primary" @click="fetchData">查询</el-button>
        <el-button type="success" size="small" @click="handleExport" :disabled="filteredData.length === 0">
          <el-icon><Download /></el-icon>导出
        </el-button>
        <el-button type="primary" @click="handleAdd(null)">
          <el-icon><Plus /></el-icon>新增部门
        </el-button>
      </div>
    </el-card>

    <el-card class="table-card">
      <el-table
        :data="filteredData"
        row-key="deptId"
        default-expand-all
        v-loading="loading"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="deptName" label="部门名称" min-width="180" />
        <el-table-column prop="sort" label="排序" width="80" align="center" />
        <el-table-column prop="leader" label="负责人" width="120" />
        <el-table-column prop="phone" label="电话" width="150" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleAdd(row)">新增</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="warning" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该部门？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑部门' : '新增部门'" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="上级部门">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            filterable
            placeholder="无（顶级部门）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="负责人">
          <el-input v-model="form.leader" placeholder="请输入负责人" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="请输入联系电话" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
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
import {computed, onMounted, reactive, ref} from 'vue'
import {addDept, deleteDept, editDept, getDeptTree, updateDeptStatus} from '@/api/system'
import {ElMessage} from 'element-plus'
import * as XLSX from 'xlsx'
import {Download, Plus} from '@element-plus/icons-vue'
import type {SysDept} from '@/types/system'

const loading = ref(false)
const treeData = ref<SysDept[]>([])
const searchName = ref('')
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref()

const form = reactive({
  deptId: undefined as number | undefined,
  parentId: 0,
  deptName: '',
  sort: 0,
  leader: '',
  phone: '',
  status: 1
})

const formRules = {
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }]
}

const treeSelectData = ref<SysDept[]>([])

const flatAllDepts = computed(() => {
  const list: SysDept[] = []
  const collect = (nodes: SysDept[]) => {
    for (const n of nodes) {
      list.push(n)
      if (n.children?.length) collect(n.children)
    }
  }
  collect(treeData.value)
  return list
})

const filteredData = computed(() => {
  if (!searchName.value) return treeData.value
  return filterTree(treeData.value, searchName.value.toLowerCase())
})

function filterTree(nodes: SysDept[], keyword: string): SysDept[] {
  return nodes.reduce((acc, node) => {
    const children = node.children ? filterTree(node.children, keyword) : []
    if (node.deptName.toLowerCase().includes(keyword) || children.length > 0) {
      acc.push({ ...node, children })
    }
    return acc
  }, [] as SysDept[])
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getDeptTree()
    treeData.value = res
    treeSelectData.value = [{ deptId: 0, deptName: '顶级部门', parentId: 0, sort: 0, leader: '', phone: '', email: '', status: 1, children: res }] as any
  } finally {
    loading.value = false
  }
}

function handleAdd(row: any) {
  isEdit.value = false
  Object.assign(form, {
    deptId: undefined, parentId: row ? row.deptId : 0,
    deptName: '', sort: 0, leader: '', phone: '', status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(form, {
    deptId: row.deptId, parentId: row.parentId || 0,
    deptName: row.deptName, sort: row.sort || 0,
    leader: row.leader || '', phone: row.phone || '', status: row.status
  })
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await editDept(form)
      ElMessage.success('编辑成功')
    } else {
      await addDept(form)
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
  await updateDeptStatus({ deptId: row.deptId, status })
  ElMessage.success('状态更新成功')
  fetchData()
}

async function handleDelete(row: any) {
  await deleteDept({ id: row.deptId })
  ElMessage.success('删除成功')
  fetchData()
}

function handleExport() {
  const exportData: any[] = []
  let level = 0
  const collect = (nodes: SysDept[], lvl: number) => {
    for (const n of nodes) {
      exportData.push({
        '部门层级': '  '.repeat(lvl) + '└ ' + (lvl + 1),
        '部门名称': n.deptName,
        '排序': n.sort,
        '负责人': n.leader,
        '电话': n.phone,
        '状态': n.status === 1 ? '启用' : '禁用'
      })
      if (n.children?.length) collect(n.children, lvl + 1)
    }
  }
  collect(flatAllDepts.value, 0)

  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '部门列表')
  ws['!cols'] = [{ wch: 20 }, { wch: 20 }, { wch: 8 }, { wch: 12 }, { wch: 15 }, { wch: 8 }]
  XLSX.writeFile(wb, `部门列表_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`)
  ElMessage.success('导出成功')
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.dept-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; gap: 12px; justify-content: flex-end; align-items: center; }
</style>

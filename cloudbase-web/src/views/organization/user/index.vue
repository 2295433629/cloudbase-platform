<template>
  <div class="user-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="账号">
          <el-input v-model="query.account" placeholder="请输入账号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="query.realName" placeholder="请输入姓名" clearable />
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
          <span>用户列表</span>
          <div>
            <el-button type="success" size="small" @click="handleExport" :disabled="tableData.length === 0">
              <el-icon><Download /></el-icon>导出
            </el-button>
            <el-button type="danger" size="small" @click="handleBatchDelete" :disabled="selectedRows.length === 0">
              <el-icon><Delete /></el-icon>批量删除
            </el-button>
            <el-button type="primary" size="small" @click="handleAdd">
              <el-icon><Plus /></el-icon>新增用户
            </el-button>
          </div>
        </div>
      </template>
      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        @selection-change="handleSelectionChange"
        row-key="userId"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column type="index" label="#" width="55" align="center" :index="(p) => (query.pageNo - 1) * query.pageSize + p + 1" />
        <el-table-column prop="account" label="账号" width="120" show-overflow-tooltip />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" show-overflow-tooltip />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip />
        <el-table-column label="部门" width="120">
          <template #default="{ row }">
            <span>{{ deptNameMap[row.deptId] || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="岗位" width="120">
          <template #default="{ row }">
            <span>{{ postNameMap[row.postId] || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button link type="warning" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="info" size="small" @click="handleResetPwd(row)">重置密码</el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="账号" prop="account">
          <el-input v-model="form.account" :disabled="isEdit" placeholder="请输入账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="form.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            placeholder="请选择部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="岗位">
          <el-select v-model="form.postId" placeholder="请选择岗位" clearable style="width: 100%">
            <el-option v-for="post in postList" :key="post.postId" :label="post.postName" :value="post.postId" />
          </el-select>
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

    <!-- 分配角色弹窗 -->
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="480px" destroy-on-close>
      <div style="margin-bottom: 12px; color: #606266">
        用户：<b>{{ roleAssignUser.realName }}（{{ roleAssignUser.account }}）</b>
      </div>
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.roleId" :value="role.roleId" style="display:block;margin-bottom:8px">
          {{ role.roleName }}
          <span style="color:#999;font-size:12px;margin-left:4px">（{{ role.roleCode }}）</span>
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="allRoles.length === 0" description="暂无可用角色" :image-size="60" />
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignRoles" :loading="roleSubmitLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetPwdVisible" title="重置密码" width="420px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ resetPwdForm.realName }}（{{ resetPwdForm.account }}）</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetPwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd" :loading="resetPwdLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import {
  addUser,
  assignUserRoles,
  batchDeleteUsers,
  deleteUser,
  editUser,
  getDeptTree,
  getPostList,
  getRoleList,
  getUserPage,
  getUserRoles,
  resetUserPwd,
  updateUserStatus
} from '@/api/system'
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage, ElMessageBox} from 'element-plus'
import * as XLSX from 'xlsx'
import {Delete, Download, Plus} from '@element-plus/icons-vue'
import type {SysDept, SysPost} from '@/types/system'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ account: '', realName: '', status: undefined as number | undefined, pageNo: 1, pageSize: 20 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref<FormInstance>()
const selectedRows = ref<any[]>([])

const deptTree = ref<SysDept[]>([])
const postList = ref<SysPost[]>([])

// 部门/岗位 ID→Name 映射表
const deptNameMap = ref<Record<number, string>>({})
const postNameMap = ref<Record<number, string>>({})

const form = reactive({
  userId: undefined as number | string | undefined,
  account: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  deptId: undefined as number | string | undefined,
  postId: undefined as number | string | undefined,
  status: 1
})

const formRules: FormRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ]
}

// 分配角色
const roleDialogVisible = ref(false)
const roleSubmitLoading = ref(false)
const roleAssignUser = reactive({ userId: undefined as number | string | undefined, account: '', realName: '' })
const selectedRoleIds = ref<(number | string)[]>([])
const allRoles = ref<any[]>([])

// 重置密码
const resetPwdVisible = ref(false)
const resetPwdLoading = ref(false)
const resetPwdForm = reactive({ userId: undefined as number | string | undefined, account: '', realName: '', newPassword: '' })

// 加载部门树
async function loadDeptTree() {
  try {
    deptTree.value = await getDeptTree()
    // 构建部门 ID→Name 映射（递归展平树）
    const map: Record<number, string> = {}
    const walk = (nodes: SysDept[]) => {
      for (const n of nodes) {
        if (n.deptId) map[n.deptId] = n.deptName
        if (n.children?.length) walk(n.children)
      }
    }
    walk(deptTree.value)
    deptNameMap.value = map
  } catch { /* ignore */ }
}

// 加载岗位列表
async function loadPostList() {
  try {
    postList.value = await getPostList()
    const map: Record<number, string> = {}
    for (const p of postList.value) {
      if (p.postId) map[p.postId] = p.postName
    }
    postNameMap.value = map
  } catch { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage(query)
    tableData.value = res.rows || []
    total.value = Number(res.total) || 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.account = ''
  query.realName = ''
  query.status = undefined
  query.pageNo = 1
  fetchData()
}

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { userId: undefined, account: '', password: '', realName: '', phone: '', email: '', deptId: undefined, postId: undefined, status: 1 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await editUser(form)
      ElMessage.success('编辑成功')
    } else {
      await addUser(form)
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
  await updateUserStatus({ userId: row.userId, status })
  ElMessage.success('状态更新成功')
  fetchData()
}

async function handleDelete(row: any) {
  await deleteUser({ id: row.userId })
  ElMessage.success('删除成功')
  fetchData()
}

// 批量删除
async function handleBatchDelete() {
  if (selectedRows.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedRows.value.length} 个用户？`, '批量删除', {
      type: 'warning'
    })
    const ids = selectedRows.value.map(row => row.userId)
    await batchDeleteUsers({ ids })
    ElMessage.success('批量删除成功')
    fetchData()
  } catch { /* cancelled */ }
}

// 导出 Excel
function handleExport() {
  const exportData = tableData.value.map((row: any) => ({
    '账号': row.account,
    '姓名': row.realName,
    '手机号': row.phone,
    '邮箱': row.email,
    '部门': row.deptName || '-',
    '状态': row.status === 1 ? '启用' : '禁用',
    '创建时间': row.createTime
  }))

  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '用户列表')

  ws['!cols'] = [
    { wch: 15 }, { wch: 12 }, { wch: 15 }, { wch: 25 }, { wch: 15 }, { wch: 8 }, { wch: 20 }
  ]

  XLSX.writeFile(wb, `用户列表_${new Date().toLocaleDateString().replace(/\//g, '-')}.xlsx`)
  ElMessage.success('导出成功')
}

// 分配角色
async function handleAssignRole(row: any) {
  roleAssignUser.userId = row.userId
  roleAssignUser.account = row.account
  roleAssignUser.realName = row.realName

  const [rolesRes, userRolesRes] = await Promise.all([
    getRoleList(),
    getUserRoles({ id: row.userId })
  ])
  allRoles.value = rolesRes
  selectedRoleIds.value = Array.isArray(userRolesRes) ? userRolesRes : []
  roleDialogVisible.value = true
}

async function submitAssignRoles() {
  roleSubmitLoading.value = true
  try {
    await assignUserRoles({ userId: roleAssignUser.userId!, roleIds: selectedRoleIds.value })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } finally {
    roleSubmitLoading.value = false
  }
}

// 重置密码
function handleResetPwd(row: any) {
  resetPwdForm.userId = row.userId
  resetPwdForm.account = row.account
  resetPwdForm.realName = row.realName
  resetPwdForm.newPassword = ''
  resetPwdVisible.value = true
}

async function submitResetPwd() {
  if (!resetPwdForm.newPassword || resetPwdForm.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  resetPwdLoading.value = true
  try {
    await resetUserPwd({ userId: resetPwdForm.userId!, newPassword: resetPwdForm.newPassword })
    ElMessage.success('密码重置成功')
    resetPwdVisible.value = false
  } catch {
    // ignore
  } finally {
    resetPwdLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  loadDeptTree()
  loadPostList()
})
</script>

<style scoped>
.user-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

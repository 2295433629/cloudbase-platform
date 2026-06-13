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
          <el-button type="primary" size="small" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="account" label="账号" width="120" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="handleAssignRole(row)">分配角色</el-button>
            <el-button link type="warning" size="small" @click="handleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-popconfirm title="确定删除该用户？" @confirm="handleDelete(row)">
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
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="500px">
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
        <el-form-item label="手机号">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
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
    <el-dialog v-model="roleDialogVisible" title="分配角色" width="480px">
      <div style="margin-bottom: 12px; color: #606266">
        用户：<b>{{ roleAssignUser.realName }}（{{ roleAssignUser.account }}）</b>
      </div>
      <el-checkbox-group v-model="selectedRoleIds">
        <el-checkbox v-for="role in allRoles" :key="role.roleId" :value="role.roleId" style="display:block;margin-bottom:8px">
          {{ role.roleName }}
          <span style="color:#999;font-size:12px;margin-left:4px">（{{ role.roleCode }}）</span>
        </el-checkbox>
      </el-checkbox-group>
      <el-empty v-if="allRoles.length === 0" description="暂无可用角色，请先在角色管理中添加" :image-size="60" />
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssignRoles" :loading="roleSubmitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import {
  getUserPage, addUser, editUser, deleteUser, updateUserStatus,
  getUserRoles, assignUserRoles, getRoleList
} from '@/api/system'
import { ElMessage } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const total = ref(0)
const query = reactive({ account: '', realName: '', pageNo: 1, pageSize: 20 })
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  userId: null,
  account: '',
  password: '',
  realName: '',
  phone: '',
  email: '',
  status: 1
})

const formRules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }]
}

// 分配角色相关
const roleDialogVisible = ref(false)
const roleSubmitLoading = ref(false)
const roleAssignUser = reactive({ userId: null, account: '', realName: '' })
const selectedRoleIds = ref([])
const allRoles = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage(query)
    tableData.value = res.rows
    total.value = res.total
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.account = ''
  query.realName = ''
  query.pageNo = 1
  fetchData()
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { userId: null, account: '', password: '', realName: '', phone: '', email: '', status: 1 })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submitForm() {
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

async function handleStatus(row) {
  const status = row.status === 1 ? 0 : 1
  await updateUserStatus({ userId: row.userId, status })
  ElMessage.success('状态更新成功')
  fetchData()
}

async function handleDelete(row) {
  await deleteUser({ userId: row.userId })
  ElMessage.success('删除成功')
  fetchData()
}

// 分配角色
async function handleAssignRole(row) {
  roleAssignUser.userId = row.userId
  roleAssignUser.account = row.account
  roleAssignUser.realName = row.realName

  // 并行加载所有启用角色 + 该用户已有角色
  const [rolesRes, userRolesRes] = await Promise.all([
    getRoleList(),
    getUserRoles({ userId: row.userId })
  ])
  allRoles.value = rolesRes
  selectedRoleIds.value = userRolesRes
  roleDialogVisible.value = true
}

async function submitAssignRoles() {
  roleSubmitLoading.value = true
  try {
    await assignUserRoles({ userId: roleAssignUser.userId, roleIds: selectedRoleIds.value })
    ElMessage.success('角色分配成功')
    roleDialogVisible.value = false
  } finally {
    roleSubmitLoading.value = false
  }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.user-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

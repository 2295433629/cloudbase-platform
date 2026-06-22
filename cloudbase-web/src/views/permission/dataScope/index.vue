<template>
  <div class="datascope-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>数据权限配置</span>
        </div>
      </template>
      <el-alert
        title="数据权限说明"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 20px"
      >
        <template #default>
          <p>数据权限用于控制不同角色可查看的数据范围，可在<strong>角色管理</strong>中为每个角色配置数据权限。</p>
          <p style="margin-top: 8px">支持以下权限范围：</p>
          <ul style="margin: 8px 0 0 16px">
            <li><strong>全部数据权限</strong> — 可查看系统所有数据</li>
            <li><strong>自定义数据权限</strong> — 按部门自定义数据范围</li>
            <li><strong>本部门数据权限</strong> — 仅查看本部门数据</li>
            <li><strong>本部门及以下</strong> — 查看本部门及所有子部门数据</li>
            <li><strong>仅本人权限</strong> — 仅查看自己创建的数据</li>
          </ul>
        </template>
      </el-alert>

      <el-table :data="pagedData" stripe v-loading="loading">
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column label="数据权限范围" width="200">
          <template #default="{ row }">
            <el-tag :type="dataScopeTag(row.dataScope)" size="small">
              {{ dataScopeMap[row.dataScope] || '全部' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleEdit(row)">配置</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        v-model:current-page="pageNo"
        v-model:page-size="pageSize"
        :total="roleData.length"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="handlePageChange"
        @current-change="handlePageChange"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 配置弹窗 -->
    <el-dialog v-model="dialogVisible" title="配置数据权限" width="480px">
      <div style="margin-bottom: 12px; color: #606266">
        角色：<b>{{ currentRole.roleName }}（{{ currentRole.roleCode }}）</b>
      </div>
      <el-form label-width="100px">
        <el-form-item label="数据权限">
          <el-select v-model="currentRole.dataScope" style="width: 100%">
            <el-option label="全部数据权限" :value="1" />
            <el-option label="自定义数据权限" :value="2" />
            <el-option label="本部门数据权限" :value="3" />
            <el-option label="本部门及以下" :value="4" />
            <el-option label="仅本人权限" :value="5" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {computed, onMounted, reactive, ref} from 'vue'
import {getRoleList} from '@/api/system'
import {ElMessage} from 'element-plus'
import api from '@/api'

const dataScopeMap: Record<number, string> = { 1: '全部', 2: '自定义', 3: '本部门', 4: '本部门及以下', 5: '仅本人' }
const dataScopeTag = (scope: number) => ['', '', 'warning', 'info', 'success', 'danger'][scope] || ''

const loading = ref(false)
const roleData = ref<any[]>([])
const pageNo = ref(1)
const pageSize = ref(10)

const pagedData = computed(() => {
  const start = (pageNo.value - 1) * pageSize.value
  return roleData.value.slice(start, start + pageSize.value)
})

function handlePageChange() {
  const maxPage = Math.max(1, Math.ceil(roleData.value.length / pageSize.value))
  if (pageNo.value > maxPage) pageNo.value = maxPage
}
const dialogVisible = ref(false)
const currentRole = reactive({ roleId: 0, roleName: '', roleCode: '', dataScope: 1 })

async function fetchData() {
  loading.value = true
  try {
    roleData.value = await getRoleList()
  } catch {
    roleData.value = []
  } finally {
    loading.value = false
  }
}

function handleEdit(row: any) {
  Object.assign(currentRole, {
    roleId: row.roleId,
    roleName: row.roleName,
    roleCode: row.roleCode,
    dataScope: row.dataScope
  })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    await api.post('/sys/role/edit', {
      roleId: currentRole.roleId,
      roleName: currentRole.roleName,
      roleCode: currentRole.roleCode,
      dataScope: currentRole.dataScope
    })
    ElMessage.success('数据权限配置成功')
    dialogVisible.value = false
    fetchData()
  } catch { /* ignore */ }
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.datascope-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
</style>

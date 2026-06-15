<template>
  <div class="menu-container">
    <div class="toolbar">
      <el-button type="primary" @click="handleAdd(null)">新增菜单</el-button>
    </div>

    <el-table :data="treeData" row-key="menuId" border default-expand-all v-loading="loading"
              :tree-props="{ children: 'children' }">
      <el-table-column prop="menuName" label="菜单名称" min-width="150" />
      <el-table-column label="类型" width="80">
        <template #default="{ row }">
          <el-tag :type="row.menuType === 1 ? '' : row.menuType === 2 ? 'success' : 'warning'" size="small">
            {{ menuTypeMap[row.menuType] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="icon" label="图标" width="80" />
      <el-table-column prop="path" label="路由路径" min-width="140" />
      <el-table-column prop="perms" label="权限标识" min-width="130" />
      <el-table-column prop="sort" label="排序" width="70" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" size="small" @click="handleAdd(row)">新增子项</el-button>
          <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="warning" size="small" @click="handleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
          <el-popconfirm title="确定删除该菜单？" @confirm="handleDelete(row)">
            <template #reference>
              <el-button link type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑菜单' : '新增菜单'" width="580px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="90px">
        <el-form-item label="上级菜单">
          <el-tree-select
            v-model="form.parentId"
            :data="treeSelectData"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            check-strictly
            filterable
            placeholder="无（顶级菜单）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio :value="1">目录</el-radio>
            <el-radio :value="2">菜单</el-radio>
            <el-radio :value="3">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="菜单名称" prop="menuName">
          <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
        </el-form-item>
        <el-form-item label="路由路径" v-if="form.menuType !== 3">
          <el-input v-model="form.path" placeholder="如 /system/user" />
        </el-form-item>
        <el-form-item label="组件路径" v-if="form.menuType === 2">
          <el-input v-model="form.component" placeholder="如 system/user/index" />
        </el-form-item>
        <el-form-item label="权限标识" v-if="form.menuType === 3">
          <el-input v-model="form.perms" placeholder="如 sys:user:add" />
        </el-form-item>
        <el-form-item label="图标" v-if="form.menuType !== 3">
          <el-input v-model="form.icon" placeholder="Element Plus 图标名" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="是否隐藏" v-if="form.menuType === 2">
          <el-switch v-model="form.hidden" :active-value="1" :inactive-value="0" active-text="是" inactive-text="否" />
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

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {addMenu, deleteMenu, editMenu, getMenuTree, updateMenuStatus} from '@/api/system'
import {ElMessage} from 'element-plus'

const menuTypeMap = { 1: '目录', 2: '菜单', 3: '按钮' }

const loading = ref(false)
const treeData = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)

const form = reactive({
  menuId: null,
  parentId: 0,
  menuName: '',
  menuType: 1,
  path: '',
  component: '',
  perms: '',
  icon: '',
  sort: 0,
  hidden: 0,
  status: 1
})

const formRules = {
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }]
}

// 树选择数据（在顶级加一个"无"节点）
const treeSelectData = ref([])

async function fetchData() {
  loading.value = true
  try {
    const res = await getMenuTree()
    treeData.value = res
    treeSelectData.value = [{ menuId: 0, menuName: '顶级菜单', children: res }]
  } finally {
    loading.value = false
  }
}

function handleAdd(row) {
  isEdit.value = false
  Object.assign(form, {
    menuId: null, parentId: row ? row.menuId : 0,
    menuName: '', menuType: 1, path: '', component: '', perms: '', icon: '', sort: 0, hidden: 0, status: 1
  })
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    menuId: row.menuId, parentId: row.parentId || 0,
    menuName: row.menuName, menuType: row.menuType,
    path: row.path || '', component: row.component || '',
    perms: row.perms || '', icon: row.icon || '',
    sort: row.sort || 0, hidden: row.hidden || 0, status: row.status
  })
  dialogVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitLoading.value = true
  try {
    if (isEdit.value) {
      await editMenu(form)
      ElMessage.success('编辑成功')
    } else {
      await addMenu(form)
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
  await updateMenuStatus({ menuId: row.menuId, status })
  ElMessage.success('状态更新成功')
  fetchData()
}

async function handleDelete(row) {
  await deleteMenu({ id: row.menuId })
  ElMessage.success('删除成功')
  fetchData()
}

onMounted(() => { fetchData() })
</script>

<style scoped>
.menu-container { display: flex; flex-direction: column; gap: 16px; }
.toolbar { display: flex; justify-content: flex-end; }
</style>

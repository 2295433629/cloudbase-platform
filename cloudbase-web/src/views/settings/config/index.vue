<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.configName" placeholder="参数名称" style="width:200px" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData">
      <el-table-column prop="configName" label="参数名称" width="180" />
      <el-table-column prop="configKey" label="键名" width="200" />
      <el-table-column prop="configValue" label="键值" show-overflow-tooltip />
      <el-table-column label="系统内置" width="100">
        <template #default="{ row }"><el-tag :type="row.configType===1?'primary':'info'">{{ row.configType===1?'内置':'自定义' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNo" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" :hide-on-single-page="false" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadData" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="参数名称"><el-input v-model="form.configName" /></el-form-item>
        <el-form-item label="键名"><el-input v-model="form.configKey" /></el-form-item>
        <el-form-item label="键值"><el-input v-model="form.configValue" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import {reactive, ref} from 'vue'
import {ElMessage} from 'element-plus'
import api from '@/api'

const tableData = ref([])
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({ configId: null, configName: '', configKey: '', configValue: '', remark: '' })
const searchForm = reactive({ configName: '' })

const loadData = () => {
  api.post('/sys/config/page', { pageNo: pageNo.value, pageSize: pageSize.value, ...searchForm }).then(res => {
    tableData.value = res.rows || []; total.value = Number(res.total) || 0
  })
}
loadData()

const handleAdd = () => { dialogTitle.value = '新增参数'; Object.assign(form, { configId: null, configName: '', configKey: '', configValue: '', remark: '' }); dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑参数'; Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row) => { api.post('/sys/config/delete', { id: row.configId }).then(() => { ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = () => {
  const url = form.configId ? '/sys/config/edit' : '/sys/config/add'
  api.post(url, form).then(() => { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() })
}
</script>

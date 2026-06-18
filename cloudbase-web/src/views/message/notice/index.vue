<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.noticeTitle" placeholder="公告标题" style="width:200px" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData">
      <el-table-column prop="noticeTitle" label="标题" show-overflow-tooltip />
      <el-table-column label="类型" width="100">
        <template #default="{ row }"><el-tag :type="row.noticeType===1?'warning':'info'">{{ row.noticeType===1?'通知':'公告' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="createTime" label="发布时间" width="180" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'启用':'关闭' }}</el-tag></template>
      </el-table-column>
      <el-table-column label="操作" width="250">
        <template #default="{ row }">
          <el-button size="small" @click="handleDetail(row)">查看</el-button>
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNo" :page-size="20" :total="total" layout="total, prev, pager, next" @current-change="loadData" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="600px">
      <el-form v-if="!viewMode" :model="form" label-width="80px">
        <el-form-item label="标题"><el-input v-model="form.noticeTitle" /></el-form-item>
        <el-form-item label="类型"><el-radio-group v-model="form.noticeType"><el-radio :label="1">通知</el-radio><el-radio :label="2">公告</el-radio></el-radio-group></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.noticeContent" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="1" :inactive-value="0" /></el-form-item>
      </el-form>
      <div v-else>
        <h3>{{ form.noticeTitle }}</h3>
        <el-divider />
        <div v-html="form.noticeContent" style="min-height:200px" />
      </div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button v-if="!viewMode" type="primary" @click="handleSubmit">确定</el-button>
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
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const viewMode = ref(false)
const form = reactive({ noticeId: null, noticeTitle: '', noticeType: 1, noticeContent: '', status: 1 })
const searchForm = reactive({ noticeTitle: '' })

const loadData = () => {
  api.post('/sys/notice/page', { pageNo: pageNo.value, pageSize: 20, ...searchForm }).then(res => {
    tableData.value = res.rows; total.value = res.total
  })
}
loadData()

const handleAdd = () => { dialogTitle.value = '新增公告'; viewMode.value = false; Object.assign(form, { noticeId: null, noticeTitle: '', noticeType: 1, noticeContent: '', status: 1 }); dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑公告'; viewMode.value = false; Object.assign(form, row); dialogVisible.value = true }
const handleDetail = (row) => { dialogTitle.value = '查看公告'; viewMode.value = true; Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row) => { api.post('/sys/notice/delete', { id: row.noticeId }).then(() => { ElMessage.success('删除成功'); loadData() }) }
const handleSubmit = () => {
  const url = form.noticeId ? '/sys/notice/edit' : '/sys/notice/add'
  api.post(url, form).then(() => { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() })
}
</script>

<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.userName" placeholder="用户名称" style="width:200px" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button type="danger" @click="handleClear">清空日志</el-button>
    </div>
    <el-table :data="tableData" border>
      <el-table-column prop="userName" label="用户" width="120" />
      <el-table-column prop="ipAddress" label="IP地址" width="150" />
      <el-table-column prop="os" label="操作系统" width="120" />
      <el-table-column prop="browser" label="浏览器" width="120" />
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="msg" label="消息" show-overflow-tooltip />
      <el-table-column prop="loginTime" label="登录时间" width="180" />
    </el-table>
    <el-pagination v-model:current-page="pageNo" :page-size="20" :total="total" layout="total, prev, pager, next" @current-change="loadData" />
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pageNo = ref(1)
const total = ref(0)
const searchForm = reactive({ userName: '' })

const loadData = () => {
  request.post('/sys/loginLog/page', { pageNo: pageNo.value, pageSize: 20, ...searchForm }).then(res => {
    tableData.value = res.rows; total.value = res.total
  })
}
loadData()

const handleClear = () => {
  ElMessageBox.confirm('确认清空?', '警告', { type: 'warning' }).then(() => {
    request.post('/sys/loginLog/clear').then(() => { ElMessage.success('已清空'); loadData() })
  })
}
</script>

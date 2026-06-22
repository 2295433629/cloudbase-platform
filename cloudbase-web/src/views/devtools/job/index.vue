<template>
  <div class="page-container">
    <div class="search-bar">
      <el-input v-model="searchForm.jobName" placeholder="任务名称" style="width:200px" clearable />
      <el-button type="primary" @click="loadData">查询</el-button>
      <el-button @click="handleAdd">新增</el-button>
    </div>
    <el-table :data="tableData">
      <el-table-column prop="jobName" label="任务名称" width="150" />
      <el-table-column prop="jobGroup" label="任务组" width="100" />
      <el-table-column prop="invokeTarget" label="调用目标" show-overflow-tooltip />
      <el-table-column prop="cronExpression" label="Cron表达式" width="150" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'运行中':'已暂停' }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" show-overflow-tooltip />
      <el-table-column label="操作" width="300">
        <template #default="{ row }">
          <el-button size="small" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" @click="handleRun(row)">执行一次</el-button>
          <el-button v-if="row.status===1" size="small" type="warning" @click="handlePause(row)">暂停</el-button>
          <el-button v-else size="small" type="success" @click="handleResume(row)">恢复</el-button>
          <el-popconfirm title="确认删除?" @confirm="handleDelete(row)">
            <template #reference><el-button size="small" type="danger">删除</el-button></template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="pageNo" v-model:page-size="pageSize" :total="total" :page-sizes="[10, 20, 50]" :hide-on-single-page="false" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadData" @current-change="loadData" style="margin-top: 16px; justify-content: flex-end" />

    <el-dialog :title="dialogTitle" v-model="dialogVisible" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="任务名称"><el-input v-model="form.jobName" /></el-form-item>
        <el-form-item label="任务组"><el-input v-model="form.jobGroup" /></el-form-item>
        <el-form-item label="调用目标"><el-input v-model="form.invokeTarget" placeholder="com.xxx.Bean.method()" /></el-form-item>
        <el-form-item label="Cron表达式"><el-input v-model="form.cronExpression" placeholder="0 */1 * * * ?" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-card shadow="hover" style="margin-top:20px">
      <template #header><span>📋 执行日志</span><el-button size="small" type="danger" style="float:right" @click="handleClearLog">清空</el-button></template>
      <el-table :data="logData" size="small" v-loading="logLoading">
        <el-table-column type="index" label="#" width="50" />
        <el-table-column prop="jobName" label="任务名称" width="150" />
        <el-table-column label="状态" width="80"><template #default="{ row }"><el-tag :type="row.status===1?'success':'danger'">{{ row.status===1?'成功':'失败' }}</el-tag></template></el-table-column>
        <el-table-column prop="costTime" label="耗时(ms)" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="exceptionInfo" label="异常" show-overflow-tooltip />
      </el-table>
    </el-card>
    <el-pagination v-model:current-page="logPageNo" v-model:page-size="logPageSize" :total="logTotal" :page-sizes="[10, 20, 50, 100]" :hide-on-single-page="false" layout="total, sizes, prev, pager, next, jumper" background @size-change="loadLogData" @current-change="loadLogData" style="margin-top: 12px; justify-content: flex-end" />
  </div>
</template>

<script setup>
import {onMounted, reactive, ref} from 'vue'
import {ElMessage, ElMessageBox} from 'element-plus'
import api from '@/api'

const tableData = ref([])
const logData = ref([])
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)
const logPageNo = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)
const logLoading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = reactive({ jobId: null, jobName: '', jobGroup: 'DEFAULT', invokeTarget: '', cronExpression: '', remark: '', concurrent: 1 })
const searchForm = reactive({ jobName: '' })

const loadData = () => {
  api.post('/sys/job/page', { pageNo: pageNo.value, pageSize: pageSize.value, ...searchForm }).then(res => {
    tableData.value = res.rows || []; total.value = Number(res.total) || 0
  })
  loadLogData()
}
const loadLogData = () => {
  logLoading.value = true
  api.post('/sys/job/log/page', { pageNo: logPageNo.value, pageSize: logPageSize.value }).then(res => {
    logData.value = res.rows || []; logTotal.value = Number(res.total) || 0
  }).finally(() => { logLoading.value = false })
}
onMounted(loadData)

const handleAdd = () => { dialogTitle.value = '新增任务'; Object.assign(form, { jobId: null, jobName: '', jobGroup: 'DEFAULT', invokeTarget: '', cronExpression: '', remark: '', concurrent: 1 }); dialogVisible.value = true }
const handleEdit = (row) => { dialogTitle.value = '编辑任务'; Object.assign(form, row); dialogVisible.value = true }
const handleDelete = (row) => { api.post('/sys/job/delete', { id: row.jobId }).then(() => { ElMessage.success('已删除'); loadData() }) }
const handleRun = (row) => { api.post('/sys/job/run', { id: row.jobId }).then(() => { ElMessage.success('已触发执行'); loadData() }) }
const handlePause = (row) => { api.post('/sys/job/pause', { id: row.jobId }).then(() => { ElMessage.success('已暂停'); loadData() }) }
const handleResume = (row) => { api.post('/sys/job/resume', { id: row.jobId }).then(() => { ElMessage.success('已恢复'); loadData() }) }
const handleSubmit = () => {
  const url = form.jobId ? '/sys/job/edit' : '/sys/job/add'
  api.post(url, form).then(() => { ElMessage.success('操作成功'); dialogVisible.value = false; loadData() })
}
const handleClearLog = () => { ElMessageBox.confirm('清空执行日志?', '警告', { type: 'warning' }).then(() => { api.post('/sys/job/log/clear').then(() => { ElMessage.success('已清空'); loadLogData() }) }) }
</script>

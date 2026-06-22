<template>
  <div class="notice-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="标题">
          <el-input v-model="query.noticeTitle" placeholder="请输入标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="query.noticeType" placeholder="全部" clearable style="width: 120px">
            <el-option label="通知" :value="1" />
            <el-option label="公告" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>通知公告列表</span>
          <el-button type="primary" size="small" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增公告
          </el-button>
        </div>
      </template>

      <el-table :data="tableData" stripe v-loading="loading">
        <el-table-column type="index" label="#" width="55" align="center"
          :index="(p) => (pageNo - 1) * pageSize + p + 1" />
        <el-table-column prop="noticeTitle" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.noticeType === 1 ? '' : 'warning'" size="small">
              {{ row.noticeType === 1 ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="handleDetail(row)">查看</el-button>
            <el-button link type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确认删除该公告？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button link type="danger" size="small">删除</el-button>
              </template>
            </el-popconfirm>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-pagination
      v-model:current-page="pageNo"
      v-model:page-size="pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      :hide-on-single-page="false"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="loadData"
      @current-change="loadData"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="650px" destroy-on-close>
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="80px">
        <el-form-item label="标题" prop="noticeTitle">
          <el-input v-model="form.noticeTitle" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="类型" prop="noticeType">
          <el-radio-group v-model="form.noticeType">
            <el-radio :label="1">通知</el-radio>
            <el-radio :label="2">公告</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="内容" prop="noticeContent">
          <el-input v-model="form.noticeContent" type="textarea" :rows="8" placeholder="请输入公告内容，支持HTML" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0"
            active-text="发布" inactive-text="草稿" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="detailData.noticeTitle || '查看详情'" width="700px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ detailData.noticeTitle }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="detailData.noticeType === 1 ? '' : 'warning'" size="small">
            {{ detailData.noticeType === 1 ? '通知' : '公告' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detailData.status === 1 ? 'success' : 'info'" size="small">
            {{ detailData.status === 1 ? '已发布' : '草稿' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间" :span="2">{{ detailData.createTime }}</el-descriptions-item>
      </el-descriptions>
      <div class="notice-content" v-html="detailData.noticeContent || ''" style="margin-top: 20px"></div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, reactive, ref} from 'vue'
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {Plus} from '@element-plus/icons-vue'
import api from '@/api'

const loading = ref(false)
const tableData = ref<any[]>([])
const pageNo = ref(1)
const pageSize = ref(20)
const total = ref(0)
const query = reactive({ noticeTitle: '', noticeType: undefined as number | undefined })

const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref<FormInstance>()
const form = reactive({
  noticeId: null as number | string | null,
  noticeTitle: '',
  noticeType: 1,
  noticeContent: '',
  status: 1
})

const formRules: FormRules = {
  noticeTitle: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  noticeType: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

const detailVisible = ref(false)
const detailData = ref<any>({})

function loadData() {
  loading.value = true
  const params: any = { pageNo: pageNo.value, pageSize: pageSize.value }
  if (query.noticeTitle) params.noticeTitle = query.noticeTitle
  if (query.noticeType != null) params.noticeType = query.noticeType
  api.post('/sys/notice/page', params).then((res: any) => {
    tableData.value = res.rows || []
    total.value = Number(res.total) || 0
  }).finally(() => { loading.value = false })
}

function resetQuery() {
  query.noticeTitle = ''
  query.noticeType = undefined
  pageNo.value = 1
  loadData()
}

function handleAdd() {
  dialogTitle.value = '新增公告'
  Object.assign(form, { noticeId: null, noticeTitle: '', noticeType: 1, noticeContent: '', status: 1 })
  dialogVisible.value = true
}

function handleEdit(row: any) {
  dialogTitle.value = '编辑公告'
  Object.assign(form, {
    noticeId: row.noticeId,
    noticeTitle: row.noticeTitle,
    noticeType: row.noticeType,
    noticeContent: row.noticeContent,
    status: row.status
  })
  dialogVisible.value = true
}

function handleDetail(row: any) {
  detailData.value = row
  detailVisible.value = true
}

function handleDelete(row: any) {
  api.post('/sys/notice/delete', { id: row.noticeId }).then(() => {
    ElMessage.success('删除成功')
    loadData()
  })
}

function handleSubmit() {
  const url = form.noticeId ? '/sys/notice/edit' : '/sys/notice/add'
  api.post(url, form).then(() => {
    ElMessage.success('操作成功')
    dialogVisible.value = false
    loadData()
  })
}

onMounted(() => { loadData() })
</script>

<style scoped>
.notice-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

.notice-content {
  line-height: 1.8;
  color: #303133;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  min-height: 100px;
}
:root.dark .notice-content {
  color: var(--text-color);
  background: #1a1a1a;
}
</style>

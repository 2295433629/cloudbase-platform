<template>
  <div class="inbox-container">
    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="query" inline>
        <el-form-item label="消息类型">
          <el-select v-model="query.type" placeholder="全部" clearable style="width: 140px">
            <el-option label="通知" value="NOTICE" />
            <el-option label="公告" value="ANNOUNCEMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="阅读状态">
          <el-select v-model="query.readStatus" placeholder="全部" clearable style="width: 120px">
            <el-option label="未读" value="unread" />
            <el-option label="已读" value="read" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区 -->
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>
            站内消息
            <el-badge v-if="unreadCount > 0" :value="unreadCount" :max="99" style="margin-left: 8px" />
          </span>
          <div>
            <el-button type="success" size="small" @click="handleMarkAllRead" :disabled="unreadCount === 0">
              <el-icon><Check /></el-icon>全部标为已读
            </el-button>
            <el-button size="small" @click="fetchData">
              <el-icon><Refresh /></el-icon>刷新
            </el-button>
          </div>
        </div>
      </template>

      <el-table
        :data="tableData"
        stripe
        v-loading="loading"
        highlight-current-row
        @row-click="handleRowClick"
        :row-class-name="tableRowClassName"
        style="cursor: pointer"
      >
        <el-table-column type="index" label="#" width="55" align="center"
          :index="(p) => (query.pageNo - 1) * query.pageSize + p + 1" />
        <el-table-column label="" width="40" align="center">
          <template #default="{ row }">
            <span v-if="row.isRead === 0" class="unread-dot"></span>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="250" show-overflow-tooltip>
          <template #default="{ row }">
            <span :class="{ 'title-unread': row.isRead === 0 }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.msgType === 'NOTICE' ? '' : 'warning'" size="small">
              {{ row.msgType === 'NOTICE' ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '已撤回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" />
      </el-table>
    </el-card>

    <el-pagination
      v-model:current-page="query.pageNo"
      v-model:page-size="query.pageSize"
      :total="total"
      :page-sizes="[10, 20, 50]"
      :hide-on-single-page="false"
      layout="total, sizes, prev, pager, next, jumper"
      background
      @size-change="fetchData"
      @current-change="fetchData"
      style="margin-top: 16px; justify-content: flex-end"
    />

    <!-- 消息详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentMsg?.title || '消息详情'" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentMsg">
        <el-descriptions-item label="标题" :span="2">{{ currentMsg.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="currentMsg.msgType === 'NOTICE' ? '' : 'warning'" size="small">
            {{ currentMsg.msgType === 'NOTICE' ? '通知' : '公告' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送方式">
          {{ currentMsg.sendType === 'ALL' ? '全部发送' : '按角色发送' }}
        </el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentMsg.createTime }}</el-descriptions-item>
        <el-descriptions-item label="阅读时间">
          {{ currentMsg.readTime || '未阅读' }}
        </el-descriptions-item>
      </el-descriptions>
      <div class="message-content" v-html="currentMsg?.content || ''" style="margin-top: 20px"></div>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
        <el-button type="primary" @click="markCurrentRead" v-if="currentMsg?.isRead === 0">标为已读</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {onMounted, onUnmounted, reactive, ref} from 'vue'
import {getMessagePage, getUnreadCount, markAllMessagesRead, markMessageRead} from '@/api/message'
import {ElMessage} from 'element-plus'
import {Check, Refresh} from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ type: '', readStatus: '' as string, pageNo: 1, pageSize: 20 })
const detailVisible = ref(false)
const currentMsg = ref<any>(null)
const unreadCount = ref(0)

let refreshTimer: ReturnType<typeof setInterval> | null = null

async function fetchData() {
  loading.value = true
  try {
    const params: any = { ...query }
    // readStatus 不需要传给后端，前端过滤
    delete params.readStatus
    const res = await getMessagePage(params)
    let rows = res.rows || []
    // 前端按阅读状态过滤
    if (query.readStatus === 'unread') {
      rows = rows.filter(r => r.isRead === 0)
    } else if (query.readStatus === 'read') {
      rows = rows.filter(r => r.isRead === 1)
    }
    tableData.value = rows
    total.value = Number(res.total) || 0
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.type = ''
  query.readStatus = ''
  query.pageNo = 1
  fetchData()
}

async function fetchUnreadCount() {
  try {
    unreadCount.value = await getUnreadCount()
  } catch {
    unreadCount.value = 0
  }
}

function tableRowClassName({ row }: { row: any }) {
  return row.isRead === 0 ? 'unread-row' : ''
}

async function handleRowClick(row: any) {
  currentMsg.value = row
  detailVisible.value = true
  if (row.isRead === 0) {
    try {
      await markMessageRead({ messageId: row.id })
      row.isRead = 1
      fetchUnreadCount()
    } catch { /* ignore */ }
  }
}

async function handleMarkAllRead() {
  try {
    await markAllMessagesRead()
    ElMessage.success('已全部标为已读')
    fetchUnreadCount()
    fetchData()
  } catch { /* ignore */ }
}

async function markCurrentRead() {
  if (!currentMsg.value) return
  try {
    await markMessageRead({ messageId: currentMsg.value.id })
    currentMsg.value.isRead = 1
    ElMessage.success('已标为已读')
    fetchUnreadCount()
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchData()
  fetchUnreadCount()
  refreshTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.inbox-container { display: flex; flex-direction: column; gap: 16px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }

/* 未读圆点指示器 */
.unread-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: #409eff;
}

/* 未读标题加粗 */
.title-unread {
  font-weight: 600;
  color: #303133;
}

/* 未读行背景 */
:deep(.unread-row) {
  background-color: #f0f7ff !important;
}
:deep(.unread-row:hover > td) {
  background-color: #e6f0fa !important;
}
:root.dark :deep(.unread-row) {
  background-color: #1a2332 !important;
}
:root.dark :deep(.unread-row:hover > td) {
  background-color: #1e2a3a !important;
}
:root.dark .title-unread {
  color: #e0e0e0;
}

/* 消息内容样式 */
.message-content {
  line-height: 1.8;
  color: #303133;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
  min-height: 100px;
}
:root.dark .message-content {
  color: var(--text-color);
  background: #1a1a1a;
}
</style>

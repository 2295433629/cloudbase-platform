<template>
  <div class="message-container">
    <el-card class="search-card">
      <el-row :gutter="20">
        <el-col :span="8">
          <el-form :inline="true">
            <el-form-item label="类型">
              <el-select v-model="query.type" placeholder="全部" clearable style="width: 150px">
                <el-option label="通知" value="NOTICE" />
                <el-option label="公告" value="ANNOUNCEMENT" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchData">查询</el-button>
              <el-button @click="fetchData">
                <el-icon><Refresh /></el-icon>
                刷新
              </el-button>
              <el-button type="success" @click="handleMarkAllRead" v-if="unreadCount > 0">
                <el-icon><Check /></el-icon>
                全部标为已读
                <el-badge :value="unreadCount" class="unread-badge" />
              </el-button>
            </el-form-item>
          </el-form>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" stripe v-loading="loading" highlight-current-row
                @current-change="handleRowClick">
        <el-table-column type="index" label="#" width="50" :index="(p) => (p + 1) * 10" />
        <el-table-column label="未读" width="60">
          <template #default="{ row }">
            <el-badge :value="row.isRead === 0 ? 1 : 0" :hidden="row.isRead === 1" :value-max="1">
              <el-icon :size="16"><BellFilled /></el-icon>
            </el-badge>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="类型" width="80">
          <template #default="{ row }">
            <el-tag :type="row.msgType === 'NOTICE' ? '' : 'warning'" size="small">
              {{ row.msgType === 'NOTICE' ? '通知' : '公告' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '已发布' : '已撤回' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180" />
      </el-table>

      <el-pagination
        v-model:current-page="query.pageNo"
        v-model:page-size="query.pageSize"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchData"
        @current-change="fetchData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 消息详情弹窗 -->
    <el-dialog v-model="detailVisible" :title="currentMsg?.title || '消息详情'" width="700px" destroy-on-close>
      <el-descriptions :column="2" border v-if="currentMsg">
        <el-descriptions-item label="标题">{{ currentMsg.title }}</el-descriptions-item>
        <el-descriptions-item label="类型">
          <el-tag :type="currentMsg.msgType === 'NOTICE' ? '' : 'warning'" size="small">
            {{ currentMsg.msgType === 'NOTICE' ? '通知' : '公告' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发送方式">{{ currentMsg.sendType === 'ALL' ? '全部发送' : '按角色发送' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ currentMsg.createTime }}</el-descriptions-item>
        <el-descriptions-item label="阅读时间" :span="2">
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
import {BellFilled, Check, Refresh} from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref<any[]>([])
const total = ref(0)
const query = reactive({ type: '', pageNo: 1, pageSize: 10 })
const detailVisible = ref(false)
const currentMsg = ref<any>(null)
const unreadCount = ref(0)

let refreshTimer: ReturnType<typeof setInterval> | null = null

async function fetchData() {
  loading.value = true
  try {
    const res = await getMessagePage(query)
    tableData.value = res.rows
    total.value = res.total
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

async function fetchUnreadCount() {
  try {
    unreadCount.value = await getUnreadCount()
  } catch {
    unreadCount.value = 0
  }
}

async function handleRowClick(row: any) {
  currentMsg.value = row
  detailVisible.value = true
  if (row.isRead === 0) {
    try {
      await markMessageRead({ messageId: row.id })
      row.isRead = 1
      fetchUnreadCount()
      fetchData()
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
    fetchData()
  } catch { /* ignore */ }
}

onMounted(() => {
  fetchData()
  fetchUnreadCount()
  // 每30秒刷新未读数
  refreshTimer = setInterval(fetchUnreadCount, 30000)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
})
</script>

<style scoped>
.message-container { display: flex; flex-direction: column; gap: 16px; }
.message-content {
  line-height: 1.8;
  color: #303133;
}
:root.dark .message-content {
  color: var(--text-color);
}
.unread-badge {
  margin-left: 8px;
}
</style>

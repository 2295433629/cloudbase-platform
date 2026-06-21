<template>
  <div class="dashboard">
    <!-- 统计卡片（仅超级管理员可见） -->
    <el-row :gutter="20" v-if="isSuperAdmin">
      <el-col :span="6">
        <el-card class="stat-card stat-card-blue" shadow="hover" @click="goTo('/org/user')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">用户总数</div>
              <div class="stat-value">{{ stats.userCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-green" shadow="hover" @click="goTo('/org/role')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">角色总数</div>
              <div class="stat-value">{{ stats.roleCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-orange" shadow="hover" @click="goTo('/perm/menu')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><Menu /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">菜单总数</div>
              <div class="stat-value">{{ stats.menuCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card stat-card-red" shadow="hover" @click="goTo('/org/dept')">
          <div class="stat-content">
            <div class="stat-icon">
              <el-icon :size="36"><OfficeBuilding /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">部门总数</div>
              <div class="stat-value">{{ stats.deptCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" :style="{ marginTop: isSuperAdmin ? '20px' : '0' }">
      <!-- 操作趋势图 -->
      <el-col :span="16">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600">近7日操作趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <!-- 操作类型分布 -->
      <el-col :span="8">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600">操作类型分布</span>
          </template>
          <div ref="typeChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <!-- 快捷入口（仅超级管理员可见） -->
      <el-col :span="8" v-if="isSuperAdmin">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600">快捷入口</span>
          </template>
          <div class="quick-links">
            <div class="quick-link-item" @click="goTo('/org/user')">
              <el-icon :size="24" color="#409EFF"><User /></el-icon>
              <span>用户管理</span>
            </div>
            <div class="quick-link-item" @click="goTo('/org/role')">
              <el-icon :size="24" color="#67C23A"><UserFilled /></el-icon>
              <span>角色管理</span>
            </div>
            <div class="quick-link-item" @click="goTo('/perm/menu')">
              <el-icon :size="24" color="#E6A23C"><Menu /></el-icon>
              <span>菜单管理</span>
            </div>
            <div class="quick-link-item" @click="goTo('/settings/dict')">
              <el-icon :size="24" color="#F56C6C"><Notebook /></el-icon>
              <span>数据字典</span>
            </div>
            <div class="quick-link-item" @click="goTo('/settings/server')">
              <el-icon :size="24" color="#909399"><Monitor /></el-icon>
              <span>服务状态</span>
            </div>
            <div class="quick-link-item" @click="goTo('/audit/operlog')">
              <el-icon :size="24" color="#606266"><Document /></el-icon>
              <span>操作日志</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <!-- 最近操作 -->
      <el-col :span="isSuperAdmin ? 16 : 24">
        <el-card shadow="hover">
          <template #header>
            <span style="font-weight: 600">最近操作记录</span>
          </template>
          <el-table :data="recentLogs" stripe v-loading="logsLoading" max-height="300">
            <el-table-column prop="module" label="模块" width="120" />
            <el-table-column prop="operUserName" label="操作人" width="100" />
            <el-table-column label="操作类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getOperTypeTag(row.operType)" size="small">
                  {{ row.operType }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="operTime" label="操作时间" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import {computed, onBeforeUnmount, onMounted, reactive, ref} from 'vue'
import {useRouter} from 'vue-router'
import {useUserStore} from '@/stores/user'
import {getDashboardStats, getDashboardTrend, getDashboardTypeDistribution, getRecentOperLogs} from '@/api/system'
import * as echarts from 'echarts'
import {Document, Menu, Monitor, Notebook, OfficeBuilding, User, UserFilled} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

/** 超级管理员标识（拥有 *:*:* 通配权限） */
const isSuperAdmin = computed(() => {
  return userStore.permissions.includes('*') || userStore.permissions.includes('*:*:*')
})

const goTo = (path: string) => router.push(path)

// 统计数据
const stats = reactive({
  userCount: 0,
  roleCount: 0,
  menuCount: 0,
  dictCount: 0,
  deptCount: 0,
  postCount: 0,
  noticeCount: 0,
  onlineUserCount: 0,
  operLogTodayCount: 0
})

// 最近操作日志
const recentLogs = ref<Array<{ module: string; operUserName: string; operType: string; operTime: string }>>([])
const logsLoading = ref(false)

async function fetchStats() {
  try {
    const data = await getDashboardStats()
    Object.assign(stats, data)
  } catch {
    // 后端无此接口时使用默认值
  }
}

async function fetchRecentLogs() {
  logsLoading.value = true
  try {
    const res = await getRecentOperLogs({ pageNo: 1, pageSize: 5 })
    recentLogs.value = res.rows || []
  } catch {
    recentLogs.value = []
  } finally {
    logsLoading.value = false
  }
}

// 图表真实数据
let trendData = { dates: [] as string[], newUsers: [] as number[], operations: [] as number[] }
let typeData: Array<{ name: string; value: number }> = []

async function fetchTrendData() {
  try {
    trendData = await getDashboardTrend()
  } catch {
    trendData = { dates: getLast7Days(), newUsers: [], operations: [] }
  }
  updateTrendChart()
}

async function fetchTypeData() {
  try {
    typeData = await getDashboardTypeDistribution()
  } catch {
    typeData = []
  }
  updateTypeChart()
}

function getOperTypeTag(type: string) {
  const map: Record<string, string> = {
    INSERT: 'success',
    UPDATE: 'warning',
    DELETE: 'danger',
    QUERY: 'info',
    EXPORT: 'success',
    GRANT: 'success'
  }
  return map[type] || 'info'
}

// ==================== ECharts ====================
const trendChartRef = ref<HTMLDivElement>()
const typeChartRef = ref<HTMLDivElement>()
let trendChart: echarts.ECharts | null = null
let typeChart: echarts.ECharts | null = null

function initTrendChart() {
  if (!trendChartRef.value) return
  trendChart = echarts.init(trendChartRef.value)
  updateTrendChart()
}

function updateTrendChart() {
  if (!trendChart) return
  const isDark = document.documentElement.classList.contains('dark')
  const textColor = isDark ? '#e0e0e0' : '#333'
  const lineColor = isDark ? '#333' : '#e8e8e8'

  const dates = trendData.dates.length > 0 ? trendData.dates : getLast7Days()
  const newUsers = trendData.newUsers.length > 0 ? trendData.newUsers : new Array(7).fill(0)
  const operations = trendData.operations.length > 0 ? trendData.operations : new Array(7).fill(0)

  trendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['新增用户', '操作次数'], textStyle: { color: textColor }, bottom: 0 },
    grid: { top: 40, right: 20, bottom: 40, left: 50 },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: lineColor } },
      axisLabel: { color: textColor }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      splitLine: { lineStyle: { color: lineColor } },
      axisLabel: { color: textColor }
    },
    series: [
      {
        name: '新增用户',
        type: 'line',
        smooth: true,
        data: newUsers,
        itemStyle: { color: '#409EFF' },
        areaStyle: { color: 'rgba(64,158,255,0.1)' }
      },
      {
        name: '操作次数',
        type: 'line',
        smooth: true,
        data: operations,
        itemStyle: { color: '#67C23A' },
        areaStyle: { color: 'rgba(103,194,58,0.1)' }
      }
    ]
  })
}

function initTypeChart() {
  if (!typeChartRef.value) return
  typeChart = echarts.init(typeChartRef.value)
  updateTypeChart()
}

function updateTypeChart() {
  if (!typeChart) return
  const isDark = document.documentElement.classList.contains('dark')
  const textColor = isDark ? '#e0e0e0' : '#333'

  const colorMap: Record<string, string> = {
    '新增': '#409EFF', '编辑': '#E6A23C', '删除': '#F56C6C',
    '查询': '#909399', '导出': '#67C23A', '授权': '#00BCD4', '其他': '#795548'
  }

  const chartData = typeData.length > 0
    ? typeData.map(item => ({
        value: item.value,
        name: item.name,
        itemStyle: { color: colorMap[item.name] || '#909399' }
      }))
    : [
        { value: 0, name: '新增', itemStyle: { color: '#409EFF' } },
        { value: 0, name: '编辑', itemStyle: { color: '#E6A23C' } },
        { value: 0, name: '删除', itemStyle: { color: '#F56C6C' } },
        { value: 0, name: '查询', itemStyle: { color: '#909399' } },
        { value: 0, name: '导出', itemStyle: { color: '#67C23A' } }
      ]

  typeChart.setOption({
    tooltip: { trigger: 'item' },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      textStyle: { color: textColor }
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: isDark ? '#1a1a1a' : '#fff', borderWidth: 3 },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 16, fontWeight: 'bold', color: textColor }
        },
        data: chartData
      }
    ]
  })
}

function getLast7Days(): string[] {
  const days: string[] = []
  for (let i = 6; i >= 0; i--) {
    const d = new Date()
    d.setDate(d.getDate() - i)
    days.push(`${d.getMonth() + 1}/${d.getDate()}`)
  }
  return days
}

// 窗口缩放自适应
function handleResize() {
  trendChart?.resize()
  typeChart?.resize()
}

onMounted(() => {
  if (isSuperAdmin.value) {
    fetchStats()
  }
  fetchRecentLogs()
  fetchTrendData().then(() => initTrendChart())
  fetchTypeData().then(() => initTypeChart())
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  typeChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.stat-card {
  cursor: pointer;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-3px);
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-card-blue .stat-icon { background: linear-gradient(135deg, #409EFF, #79bbff); }
.stat-card-green .stat-icon { background: linear-gradient(135deg, #67C23A, #95d475); }
.stat-card-orange .stat-icon { background: linear-gradient(135deg, #E6A23C, #eebe77); }
.stat-card-red .stat-icon { background: linear-gradient(135deg, #f56c6c, #fab6b6); }

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #1f2937;
  letter-spacing: -0.5px;
}

:root.dark .stat-value {
  color: var(--text-color);
}

.chart-container {
  width: 100%;
  height: 300px;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.quick-link-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 14px 12px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.2s;
  color: #606266;
  font-size: 13px;
}

.quick-link-item:hover {
  background: rgba(64, 158, 255, 0.06);
  color: #409eff;
  transform: translateY(-1px);
}

:root.dark .quick-link-item:hover {
  background: var(--bg-color-light);
}
</style>

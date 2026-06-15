<template>
  <el-container class="layout-container">
    <Sidebar :collapse="isCollapse" @toggle-collapse="isCollapse = !isCollapse" />
    <el-container class="main-container">
      <!-- 顶部导航栏：标签页 + 面包屑 + 操作按钮 -->
      <el-header class="layout-header" height="56px">
        <div class="header-left">
          <!-- 标签页 -->
          <el-tabs
            v-model="activeTab"
            type="card"
            class="header-tabs"
            @tab-click="handleTabClick"
            @tab-remove="handleTabRemove"
          >
            <el-tab-pane
              v-for="tab in appStore.tabs"
              :key="tab.path"
              :label="tab.title"
              :name="tab.path"
              :closable="tab.path !== '/dashboard'"
            >
            </el-tab-pane>
          </el-tabs>
        </div>
        <div class="header-right">
          <!-- 主题切换 -->
          <el-tooltip :content="appStore.theme === 'dark' ? '切换到亮色' : '切换到暗色'" placement="bottom">
            <el-icon class="header-action-icon" @click="appStore.toggleTheme()">
              <component :is="appStore.theme === 'dark' ? 'Sunny' : 'Moon'" />
            </el-icon>
          </el-tooltip>
          <!-- 全屏 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-action-icon" @click="toggleFullscreen">
              <component :is="isFullscreen ? 'FullScreen' : 'FullScreen'" />
            </el-icon>
          </el-tooltip>
        </div>
      </el-header>
      <!-- 内容区 -->
      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {useAppStore} from '@/stores/app'
import Sidebar from './Sidebar.vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()

const isCollapse = ref(false)
const activeTab = ref('/dashboard')

// 全屏
const isFullscreen = ref(false)

function toggleFullscreen(): void {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullscreen.value = true
  } else {
    document.exitFullscreen()
    isFullscreen.value = false
  }
}

// 标签页点击
function handleTabClick(tab: { props: { name: string } }): void {
  router.push(tab.props.name)
}

// 关闭标签页
function handleTabRemove(targetName: string): void {
  appStore.removeTab(targetName)
  if (activeTab.value === targetName) {
    const remaining = appStore.tabs
    const next = remaining[remaining.length - 1]?.path || '/dashboard'
    router.push(next)
  }
}

// 路由变化 → 更新面包屑 + 标签页
watch(
  () => route.path,
  (newPath) => {
    activeTab.value = newPath
    // 添加标签页
    const title = (route.meta.title as string) || '未命名'
    const icon = route.meta.icon as string | undefined
    appStore.addTab({ path: newPath, title, icon })

    // 构建面包屑
    const matched = route.matched
    if (matched.length > 0) {
      // 跳过 Layout 本身
      appStore.setBreadcrumbs(
        matched.filter(r => r.meta?.title).map(r => ({
          path: r.path === '/' ? '/' : r.path,
          title: r.meta.title as string,
          icon: r.meta.icon as string | undefined
        }))
      )
    }
  },
  { immediate: true }
)

onMounted(() => {
  // 初始化暗色主题
  if (appStore.theme === 'dark') {
    document.documentElement.classList.add('dark')
  }
  // 监听全屏变化
  document.addEventListener('fullscreenchange', () => {
    isFullscreen.value = !!document.fullscreenElement
  })
})
</script>

<style scoped>
.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: row;
}

.main-container {
  display: flex;
  flex-direction: column;
  flex: 1;
  min-width: 0;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  border-bottom: 1px solid #f0f0f0;
  padding: 0 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

:root.dark .layout-header {
  background: var(--bg-color-light);
  border-bottom-color: var(--border-color);
}

.header-left {
  flex: 1;
  overflow: hidden;
}

.header-tabs {
  --el-tabs-header-height: 36px;
  overflow: auto;
}

.header-tabs :deep(.el-tabs__item) {
  height: 28px !important;
  line-height: 28px !important;
  font-size: 13px;
  border-radius: 4px;
  transition: all 0.2s;
}

.header-tabs :deep(.el-tabs__item.is-active) {
  background-color: rgba(64, 158, 255, 0.08);
  color: #409EFF;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-action-icon {
  font-size: 20px;
  cursor: pointer;
  color: #606266;
  transition: color 0.2s;
  padding: 4px;
  border-radius: 6px;
}

.header-action-icon:hover {
  color: #409eff;
  background-color: rgba(64, 158, 255, 0.06);
}

:root.dark .header-action-icon {
  color: #ccc;
}

.layout-main {
  background: #f0f2f5;
  padding: 16px;
  flex: 1;
  overflow: auto;
}

:root.dark .layout-main {
  background: var(--bg-color);
}

/* 页面切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>

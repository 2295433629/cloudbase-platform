import {defineStore} from 'pinia'
import {ref} from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏
  const isCollapsed = ref(false)

  // 主题: 'light' | 'dark'
  const theme = ref<string>(localStorage.getItem('app-theme') || 'light')

  // 面包屑跟随路由
  const breadcrumbs = ref<{ path: string; title: string; icon?: string }[]>([])

  // 顶部标签页
  const tabs = ref<{ path: string; title: string; icon?: string }[]>([
    { path: '/dashboard', title: '首页', icon: 'HomeFilled' }
  ])
  const activeTab = ref('/dashboard')

  const darkClass = document.documentElement.classList.contains('dark')

  function toggleTheme(): void {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.classList.toggle('dark', theme.value === 'dark')
    localStorage.setItem('app-theme', theme.value)
  }

  function setCollapsed(collapsed: boolean): void {
    isCollapsed.value = collapsed
  }

  function setBreadcrumbs(items: { path: string; title: string; icon?: string }[]): void {
    breadcrumbs.value = items
  }

  function addTab(tab: { path: string; title: string; icon?: string }): void {
    if (tabs.value.find(t => t.path === tab.path)) return
    tabs.value.push(tab)
  }

  function removeTab(path: string): void {
    if (path === '/dashboard') return
    const idx = tabs.value.findIndex(t => t.path === path)
    if (idx === -1) return
    tabs.value.splice(idx, 1)
    // 切换到相邻标签
    if (activeTab.value === path) {
      activeTab.value = tabs.value[Math.min(idx, tabs.value.length - 1)]?.path || '/dashboard'
    }
  }

  function closeTab(path: string): void {
    removeTab(path)
  }

  function closeOtherTabs(path: string): void {
    tabs.value = tabs.value.filter(t => t.path === path || t.path === '/dashboard')
    activeTab.value = path
  }

  function closeAllTabs(): void {
    tabs.value = [{ path: '/dashboard', title: '首页', icon: 'HomeFilled' }]
    activeTab.value = '/dashboard'
  }

  return {
    isCollapsed,
    theme,
    breadcrumbs,
    tabs,
    activeTab,
    toggleTheme,
    setCollapsed,
    setBreadcrumbs,
    addTab,
    removeTab,
    closeTab,
    closeOtherTabs,
    closeAllTabs
  }
})

import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { fetchDynamicRoutes } from './dynamic'

export const staticRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes
})

// 是否已加载动态路由
let dynamicRoutesLoaded = false

/**
 * 重置动态路由标记（登出时调用）
 */
export function resetDynamicRoutes(): void {
  dynamicRoutesLoaded = false
}

// 路由守卫
router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('token')

  // 未登录 → 跳转登录页
  if (to.path !== '/login' && !token) {
    next('/login')
    return
  }

  // 已登录访问登录页 → 跳转首页
  if (to.path === '/login' && token) {
    next('/dashboard')
    return
  }

  // 已登录且动态路由未加载 → 加载动态路由
  if (token && !dynamicRoutesLoaded) {
    try {
      const routes = await fetchDynamicRoutes()
      routes.forEach(route => router.addRoute('Layout', route))
      dynamicRoutesLoaded = true
      // 重新导航到目标路由（此时路由已注册）
      next({ ...to, replace: true })
    } catch (e) {
      console.warn('加载动态路由失败', e)
      dynamicRoutesLoaded = true // 避免反复重试
      next()
    }
    return
  }

  next()
})

export default router

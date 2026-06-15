import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import {fetchDynamicRoutes} from './dynamic'

/**
 * catch-all 通配路由（单独提取，便于动态路由加载后重新注册以确保最低优先级）
 */
const catchAllRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'NotFoundRedirect',
  redirect: '/404',
  meta: { title: '页面不存在' }
}

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
  },
  // 错误页面（放在 Layout 之外，独立渲染）
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { title: '无权限' }
  },
  {
    path: '/404',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '页面不存在' }
  },
  {
    path: '/500',
    name: 'ServerError',
    component: () => import('@/views/error/500.vue'),
    meta: { title: '服务器错误' }
  },
  // 通配符（所有未匹配的路由 → 404）
  catchAllRoute
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes
})

let dynamicRoutesLoaded = false

/**
 * 重置动态路由标记（登出时调用）
 */
export function resetDynamicRoutes(): void {
  dynamicRoutesLoaded = false
}

router.beforeEach(async (to, _from, next) => {
  const token = localStorage.getItem('token')

  // 错误页面无需登录
  if (to.path.startsWith('/403') || to.path.startsWith('/404') || to.path.startsWith('/500')) {
    next()
    return
  }

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
      // 关键：先移除 catch-all 通配路由，避免它在 Vue Router 4.5+ 内部排序中
      // 优先于后添加的动态子路由被匹配到（这是 Vue Router 4 的已知行为）
      if (router.hasRoute('NotFoundRedirect')) {
        router.removeRoute('NotFoundRedirect')
      }
      // 将所有动态路由注册到 Layout 下
      routes.forEach(route => router.addRoute('Layout', route))
      // 重新添加 catch-all，确保它在路由匹配器中处于最低优先级
      router.addRoute(catchAllRoute)
      dynamicRoutesLoaded = true
      console.log(`[router] 动态路由加载完成，共注册 ${routes.length} 条路由`)
      next({ ...to, replace: true })
    } catch (e) {
      console.warn('加载动态路由失败', e)
      dynamicRoutesLoaded = true
      next('/403')
    }
    return
  }

  next()
})

export default router

import {createRouter, createWebHistory, type RouteRecordRaw} from 'vue-router'
import {fetchDynamicRoutes} from './dynamic'
import {useUserStore} from '@/stores/user'

/**
 * catch-all 通配路由
 * 不在 staticRoutes 中注册！仅在动态路由加载完成后动态添加，
 * 彻底避免刷新时 catch-all 抢先匹配动态路由路径导致 404。
 */
const catchAllRoute: RouteRecordRaw = {
  path: '/:pathMatch(.*)*',
  name: 'NotFoundRedirect',
  component: () => import('@/views/error/404.vue'),
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
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes: staticRoutes
})

let dynamicRoutesLoaded = false
/** Promise 锁：防止并发导航重复加载动态路由 */
let dynamicRoutesPromise: Promise<void> | null = null

/**
 * 重置动态路由标记（登出时调用）
 */
export function resetDynamicRoutes(): void {
  dynamicRoutesLoaded = false
  dynamicRoutesPromise = null
}

/**
 * 加载动态路由（带 Promise 锁，确保全局只加载一次）
 * 多个并发导航会等待同一个 Promise，避免重复拉取和注册
 */
async function ensureDynamicRoutes(): Promise<void> {
  if (dynamicRoutesLoaded) return
  if (dynamicRoutesPromise) {
    await dynamicRoutesPromise
    return
  }

  dynamicRoutesPromise = (async () => {
    try {
      const userStore = useUserStore()
      if (!userStore.permissions.length && !localStorage.getItem('permissions')) {
        await userStore.fetchPermissions()
      }
      const routes = await fetchDynamicRoutes()
      if (routes.length === 0) {
        console.warn('[router] 动态路由为空，可能接口异常，下次导航将重试')
        return  // 不标记为已加载，下次导航会重试
      }
      routes.forEach(route => router.addRoute('Layout', route))
      console.log(`[router] 动态路由加载完成，共注册 ${routes.length} 条路由`)
      if (import.meta.env.DEV) {
        console.log('[router] 已注册路由:', router.getRoutes().map(r => `${r.name}(${r.path})`).join(', '))
      }
      // 路由注册成功后再添加 catch-all
      if (!router.hasRoute('NotFoundRedirect')) {
        router.addRoute(catchAllRoute)
      }
      dynamicRoutesLoaded = true
    } catch (e) {
      console.warn('[router] 加载动态路由失败，下次导航将重试', e)
      // 不设置 dynamicRoutesLoaded = true，确保下次导航会重试
    }
  })()
  await dynamicRoutesPromise
  // Promise 锁只保护一次加载，后续调用应重新判断
  dynamicRoutesPromise = null
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

  // 已登录且动态路由未加载 → 等待加载完成（Promise 锁防止并发重复加载）
  if (token && !dynamicRoutesLoaded) {
    console.log(`[router] 导航到 ${to.fullPath}，动态路由未加载，正在加载...`)
    await ensureDynamicRoutes()
    // 加载完成后，如果动态路由确实已注册，重新导航触发路由重新匹配
    if (dynamicRoutesLoaded) {
      console.log(`[router] 动态路由已就绪，重新导航到 ${to.fullPath}`)
      next({ path: to.fullPath, replace: true })
    } else if (!localStorage.getItem('token')) {
      // 加载过程中 token 被清除（如 401 拦截器触发），直接去登录页
      console.warn('[router] token 已在加载过程中被清除，跳转登录')
      next('/login')
    } else {
      // 加载失败（接口异常），先去首页，下次点击菜单会重试
      console.warn('[router] 动态路由加载失败，降级到首页')
      next('/dashboard')
    }
    return
  }

  // 动态路由已加载完毕，若当前仍命中 catch-all 说明页面确实不存在
  if (dynamicRoutesLoaded && to.name === 'NotFoundRedirect') {
    console.warn(`[router] 路由未匹配: ${to.fullPath} → 404`)
    next({ path: '/404', replace: true })
    return
  }

  next()
})

export default router

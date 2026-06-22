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
      // 刷新页面时确保权限数据已加载
      const userStore = useUserStore()
      if (!userStore.permissions.length && !localStorage.getItem('permissions')) {
        await userStore.fetchPermissions()
      }
      const routes = await fetchDynamicRoutes()
      // 将所有动态路由注册到 Layout 下
      routes.forEach(route => router.addRoute('Layout', route))
      console.log(`[router] 动态路由加载完成，共注册 ${routes.length} 条路由`)
    } catch (e) {
      console.warn('加载动态路由失败', e)
    }

    // 无论成功与否，都注册 catch-all（确保未知路径能匹配到 404）
    // catch-all 只在此处注册，不在 staticRoutes 中，
    // 保证它永远在动态路由之后才被匹配
    if (!router.hasRoute('NotFoundRedirect')) {
      router.addRoute(catchAllRoute)
    }
    dynamicRoutesLoaded = true

    // 用 path 重新导航，触发路由重新匹配（此时动态路由已注册）
    next({ path: to.fullPath, replace: true })
    return
  }

  // 动态路由已加载完毕，若当前仍命中 catch-all 说明页面确实不存在
  if (dynamicRoutesLoaded && to.name === 'NotFoundRedirect') {
    next({ path: '/404', replace: true })
    return
  }

  next()
})

export default router

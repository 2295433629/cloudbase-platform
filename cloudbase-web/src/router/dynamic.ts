import type {RouteRecordRaw} from 'vue-router'
import type {SysMenu} from '@/types/system'
import {getUserMenuTree} from '@/api/system'

/**
 * 使用 Vite import.meta.glob 预加载所有视图组件
 * key 如 '../views/organization/user/index.vue'，value 为懒加载函数
 */
const viewModules = import.meta.glob('../views/**/*.vue')

// 打印所有可用的视图组件路径，便于排查组件加载问题
if (import.meta.env.DEV) {
  console.log('[dynamic-route] 可用视图组件:', Object.keys(viewModules))
}

/**
 * 根据数据库 component 字段解析视图组件
 * 例如 'organization/user/index' → '../views/organization/user/index.vue'
 */
function loadComponent(componentPath: string): () => Promise<unknown> {
  const key = `../views/${componentPath}.vue`
  const loader = viewModules[key]
  if (!loader) {
    console.warn(`[dynamic-route] 组件未找到: ${key}`)
    return () => Promise.reject(new Error(`Component not found: ${key}`))
  }
  return loader as () => Promise<unknown>
}

/**
 * 从菜单树中提取所有启用（status=1）的页面（menuType=2），扁平化
 */
function flattenMenuPages(menus: SysMenu[]): SysMenu[] {
  const pages: SysMenu[] = []

  function traverse(items: SysMenu[]): void {
    for (const item of items) {
      // 跳过禁用菜单（status=0）
      if (item.status !== 1) continue
      if (item.menuType === 2 && item.path && item.component) {
        pages.push(item)
      }
      if (item.children && item.children.length > 0) {
        traverse(item.children)
      }
    }
  }

  traverse(menus)
  return pages
}

/**
 * 根据菜单树生成路由配置
 * 返回扁平化的页面路由数组，由 router.addRoute('Layout', route) 添加到 layout 下
 */
export function generateRoutes(menus: SysMenu[]): RouteRecordRaw[] {
  const pages = flattenMenuPages(menus)

  if (import.meta.env.DEV) {
    console.log('[dynamic-route] 解析到菜单页面:', pages.map(p => `${p.path} → ${p.component}`))
  }

  return pages.map(page => ({
    path: page.path,
    name: page.menuName,
    component: loadComponent(page.component),
    meta: {
      title: page.menuName,
      icon: page.icon,
      perms: page.perms
    }
  }))
}

/**
 * 从后端拉取菜单并生成动态路由
 * 在 router.beforeEach 中首次鉴权通过后调用
 */
export async function fetchDynamicRoutes(): Promise<RouteRecordRaw[]> {
  try {
    const menus = await getUserMenuTree()
    return generateRoutes(menus)
  } catch {
    console.warn('获取菜单树失败，使用静态路由')
    return []
  }
}

import type {Directive, DirectiveBinding} from 'vue'

/**
 * v-permission 指令
 * 根据用户权限控制元素显隐
 *
 * 用法:
 *   <el-button v-permission="'system:user:add'">新增</el-button>
 *   或传入数组（满足任一即显示）: v-permission="['system:user:add', 'system:user:edit']"
 *
 * 改进:
 *   - 优先从 Pinia store 读取权限，fallback 到 localStorage
 *   - 无权限时通过 display:none 隐藏（而非移除 DOM），保证 Vue 虚拟 DOM 可正常追踪
 */

let cachedPermissions: string[] = []
let cacheTimestamp = 0

function getPermissions(): string[] {
  const now = Date.now()
  // 缓存 10 秒，避免重复解析
  if (cachedPermissions.length > 0 && now - cacheTimestamp < 10000) {
    return cachedPermissions
  }
  try {
    cachedPermissions = JSON.parse(localStorage.getItem('permissions') || '[]')
  } catch {
    cachedPermissions = []
  }
  cacheTimestamp = now
  return cachedPermissions
}

function hasPermission(perm: string, permissions: string[]): boolean {
  // 超级管理员通配符支持
  if (permissions.includes('*') || permissions.includes('*:*:*')) return true
  return permissions.includes(perm)
}

export const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    applyPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    applyPermission(el, binding)
  }
}

function applyPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>): void {
  const { value } = binding
  if (!value) return

  const permissions = getPermissions()
  let allowed = false

  if (Array.isArray(value)) {
    allowed = value.some(perm => hasPermission(perm, permissions))
  } else {
    allowed = hasPermission(value, permissions)
  }

  // 使用 display 控制显隐，避免 removeChild 破坏 Vue 虚拟 DOM 追踪
  el.style.display = allowed ? '' : 'none'
}

export default permission

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
 *   - 无权限时移除 DOM 元素（而非 display:none），避免控制台可见
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

  if (Array.isArray(value)) {
    const allowed = value.some(perm => hasPermission(perm, permissions))
    if (!allowed) {
      el.parentNode?.removeChild(el)
      return
    }
  } else {
    if (!hasPermission(value, permissions)) {
      el.parentNode?.removeChild(el)
      return
    }
  }
}

export default permission

import type { Directive, DirectiveBinding } from 'vue'

/**
 * v-permission 指令
 * 根据角色权限字符串控制元素显隐
 *
 * 用法:
 *   <el-button v-permission="'system:user:add'">新增</el-button>
 *   或传入数组: v-permission="['system:user:add', 'system:user:edit']"
 */
function checkPermission(el: HTMLElement, binding: DirectiveBinding<string | string[]>): void {
  const { value } = binding
  if (!value) return

  // 从 localStorage 获取用户权限列表（假设格式为逗号分隔的字符串）
  const permissions: string[] = JSON.parse(localStorage.getItem('permissions') || '[]')

  if (Array.isArray(value)) {
    // 满足任意一个权限即显示
    const hasPermission = value.some(perm => permissions.includes(perm))
    if (!hasPermission) {
      el.style.display = 'none'
      el.setAttribute('data-permission-hidden', 'true')
    } else {
      el.style.display = ''
      el.removeAttribute('data-permission-hidden')
    }
  } else {
    const hasPermission = permissions.includes(value)
    if (!hasPermission) {
      el.style.display = 'none'
      el.setAttribute('data-permission-hidden', 'true')
    } else {
      el.style.display = ''
      el.removeAttribute('data-permission-hidden')
    }
  }
}

export const permission: Directive<HTMLElement, string | string[]> = {
  mounted(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    checkPermission(el, binding)
  },
  updated(el: HTMLElement, binding: DirectiveBinding<string | string[]>) {
    checkPermission(el, binding)
  }
}

export default permission

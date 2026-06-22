import {defineStore} from 'pinia'
import {ref} from 'vue'
import {getPermissions, loginApi} from '@/api/auth'
import type {LoginParams, UserInfo} from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(JSON.parse(localStorage.getItem('userInfo') || 'null'))
  const permissions = ref<string[]>(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const roleCodes = ref<string[]>(JSON.parse(localStorage.getItem('roleCodes') || '[]'))

  async function login(account: string, password: string, uuid: string, captcha: string): Promise<void> {
    const params: LoginParams = { account, password, uuid, captcha }
    const res = await loginApi(params)

    // 登录成功后，先清除旧的会话数据，避免上次残留的 permissions/roleCodes 污染新会话
    permissions.value = []
    roleCodes.value = []
    localStorage.removeItem('permissions')
    localStorage.removeItem('roleCodes')

    // 设置新 token 和用户信息
    token.value = res.token
    userInfo.value = res.userInfo
    localStorage.setItem('token', res.token)
    localStorage.setItem('userInfo', JSON.stringify(res.userInfo))

    // 获取新用户的权限信息
    await fetchPermissions()
  }

  async function fetchPermissions(): Promise<void> {
    try {
      const data = await getPermissions()
      permissions.value = data.permissions || []
      roleCodes.value = data.roleCodes || []
      localStorage.setItem('permissions', JSON.stringify(permissions.value))
      localStorage.setItem('roleCodes', JSON.stringify(roleCodes.value))
    } catch {
      permissions.value = []
      roleCodes.value = []
    }
  }

  function logout(): void {
    token.value = ''
    userInfo.value = null
    permissions.value = []
    roleCodes.value = []
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    localStorage.removeItem('roleCodes')
  }

  return { token, userInfo, permissions, roleCodes, login, logout, fetchPermissions }
})

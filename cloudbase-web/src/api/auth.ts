import request from '@/utils/request'
import type {LoginParams, LoginResult, UserInfo} from '@/types/user'

export function loginApi(data: LoginParams): Promise<LoginResult> {
  return request({ url: '/auth/login', method: 'post', data })
}

export function getCaptcha(): Promise<{ uuid: string; img: string }> {
  return request({ url: '/auth/captcha', method: 'get' })
}

export function logoutApi(data?: Record<string, unknown>): Promise<void> {
  return request({ url: '/auth/logout', method: 'post', data })
}

// 个人中心
export function getProfile(data?: Record<string, unknown>): Promise<UserInfo & { phone: string; email: string; lastLoginTime?: string; status?: number }> {
  return request({ url: '/auth/profile', method: 'post', data })
}

export function updateProfile(data: Partial<UserInfo & { phone: string; email: string }>): Promise<void> {
  return request({ url: '/auth/updateProfile', method: 'post', data })
}

export function changePassword(data: { oldPassword: string; newPassword: string }): Promise<void> {
  return request({ url: '/auth/changePassword', method: 'post', data })
}

// 获取当前用户权限信息
export function getPermissions(): Promise<{ permissions: string[]; roleCodes: string[] }> {
  return request({ url: '/auth/permissions', method: 'post' })
}

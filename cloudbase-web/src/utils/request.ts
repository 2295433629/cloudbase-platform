import axios, {type AxiosResponse, type InternalAxiosRequestConfig} from 'axios'
import {ElMessage} from 'element-plus'
import type {ApiResponse, TableResponse} from '@/types/api'

const pendingMap = new Map<string, AbortController>()

function getRequestKey(config: InternalAxiosRequestConfig): string {
  const { method, url, params, data } = config
  const queryStr = data ? JSON.stringify(data) : ''
  return [method, url, queryStr].join('&')
}

function addPending(config: InternalAxiosRequestConfig): void {
  // GET 请求不需要去重/取消机制，多次相同 GET（如验证码）都是正常的
  if (config.method?.toUpperCase() === 'GET') return
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    pendingMap.get(key)!.abort()
    pendingMap.delete(key)
  }
  const controller = new AbortController()
  config.signal = controller.signal
  pendingMap.set(key, controller)
}

function removePending(config: InternalAxiosRequestConfig): void {
  if (config.method?.toUpperCase() === 'GET') return
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    pendingMap.delete(key)
  }
}

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  addPending(config)
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

// 延迟导入 router，避免循环依赖
function getRouter() {
  return import('@/router').then(m => m.default)
}

/** 跳转登录页（避免重复导航） */
function redirectToLogin(): void {
  getRouter().then(router => {
    if (router.currentRoute.value.path !== '/login') {
      router.push('/login')
    }
  })
}

/** 需要跳转登录页的认证错误码 */
const AUTH_ERROR_CODES = new Set(['A0200', 'A0201', 'A0202'])

/**
 * 清除所有认证相关的 localStorage 数据 + Pinia store 状态
 * Token 失效时一并清除 permissions/roleCodes，避免下次登录时残留旧数据
 * 同时清理标签页和动态路由，确保切换账号时会话完全隔离
 */
function clearAuthStorage(): void {
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  localStorage.removeItem('permissions')
  localStorage.removeItem('roleCodes')
  // 异步清理 Pinia store 和动态路由（避免循环依赖）
  Promise.all([
    import('@/stores/user'),
    import('@/stores/app'),
    import('@/router')
  ]).then(([userMod, appMod, routerMod]) => {
    const userStore = userMod.useUserStore()
    const appStore = appMod.useAppStore()
    userStore.logout()
    appStore.closeAllTabs()
    appStore.setBreadcrumbs([])
    routerMod.resetDynamicRoutes()
  }).catch(() => {})
}

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse | TableResponse>) => {
    removePending(response.config as InternalAxiosRequestConfig)
    const res = response.data
    // 成功：业务码 "00000"
    if (res.code === '00000') {
      return 'rows' in res ? res : res.data
    }
    // 业务错误（HTTP 200 但 code 非 "00000"）
    ElMessage.error(res.msg || '请求失败')
    // code 为 null/undefined 视为认证状态异常（兜底：后端 controller 未抛标准异常时）
    if (res.code == null || AUTH_ERROR_CODES.has(res.code)) {
      clearAuthStorage()
      redirectToLogin()
    }
    return Promise.reject(new Error(res.msg || '请求失败'))
  },
  (error: any) => {
    if (error.config) {
      removePending(error.config as InternalAxiosRequestConfig)
    }
    if (axios.isCancel(error)) {
      console.log('请求已取消:', error.message)
      return Promise.reject(error)
    }

    const status = error.response?.status
    const data = error.response?.data || {}

    if (status === 401 || AUTH_ERROR_CODES.has(data?.code)) {
      // 认证失败 → 清除所有会话数据 → 跳转登录
      ElMessage.error(data?.msg || '登录已过期，请重新登录')
      clearAuthStorage()
      redirectToLogin()
    } else if (status === 403 || data?.code === 'A0300' || data?.code === 'A0301') {
      // 权限不足 → 跳转 403 页
      ElMessage.error(data?.msg || '权限不足')
      getRouter().then(router => router.push('/403'))
    } else {
      // 其他错误（400/404/500 等）
      ElMessage.error(data?.msg || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export function setupAxios(): void {
  // 预留扩展：可在此处添加 baseURL 动态配置、额外拦截器等
}

export function cancelPendingRequests(): void {
  pendingMap.forEach(controller => controller.abort())
  pendingMap.clear()
}

export default request

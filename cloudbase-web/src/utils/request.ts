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
  // GET 请求不注册到 pendingMap，也不需要移除
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

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse | TableResponse>) => {
    removePending(response.config as InternalAxiosRequestConfig)
    const res = response.data
    if (res.code === 200) {
      return 'rows' in res ? res : res.data
    }
    ElMessage.error(res.msg || '请求失败')
    if (res.code === 401) {
      localStorage.removeItem('token')
      getRouter().then(router => router.push('/login'))
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
    if (error.response && error.response.status === 401) {
      ElMessage.error(error.response.data?.msg || '登录已过期，请重新登录')
      localStorage.removeItem('token')
      getRouter().then(router => router.push('/login'))
    } else if (error.response && error.response.status === 403) {
      ElMessage.error('权限不足')
      getRouter().then(router => router.push('/403'))
    } else {
      ElMessage.error(error.response?.data?.msg || '网络异常，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export function setupAxios(): void {}

export function cancelPendingRequests(): void {
  pendingMap.forEach(controller => controller.abort())
  pendingMap.clear()
}

export default request

import axios, { type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse, TableResponse } from '@/types/api'

// 扩展 InternalAxiosRequestConfig 添加取消令牌
const pendingMap = new Map<string, AbortController>()

function getRequestKey(config: AxiosRequestConfig): string {
  const { method, url } = config
  return [method, url].join('&')
}

function addPending(config: InternalAxiosRequestConfig): void {
  const key = getRequestKey(config)
  if (pendingMap.has(key)) {
    pendingMap.get(key)!.abort()
    pendingMap.delete(key)
  }
  const controller = new AbortController()
  config.signal = controller.signal
  pendingMap.set(key, controller)
}

function removePending(config: AxiosRequestConfig): void {
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

// 响应拦截器
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse | TableResponse>) => {
    removePending(response.config)
    const res = response.data
    if (res.code === 200) {
      // TableDataInfo 返回 {code, msg, rows, total}，AjaxResult 返回 {code, msg, data}
      return 'rows' in res ? res : res.data
    }
    ElMessage.error(res.msg || '请求失败')
    if (res.code === 401) {
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(res)
  },
  (error: any) => {
    if (error.config) {
      removePending(error.config)
    }
    if (axios.isCancel(error)) {
      console.log('请求已取消:', error.message)
      return Promise.reject(error)
    }
    if (error.response && error.response.status === 401) {
      ElMessage.error(error.response.data?.msg || '登录已过期，请重新登录')
      localStorage.removeItem('token')
      window.location.href = '/login'
    } else {
      ElMessage.error('网络异常，请稍后重试')
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

import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers['Authorization'] = 'Bearer ' + token
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => {
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
  error => {
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

export function setupAxios() {}

export default request

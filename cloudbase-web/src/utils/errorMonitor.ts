/**
 * 前端全局错误监控
 * <p>
 * 捕获 Vue 组件异常、未处理的 Promise 拒绝、以及 window.onerror，
 * 统一收集并通过 API 上报到后端（用于排查线上问题）。
 * </p>
 */

import type {App} from 'vue'
import request from '@/utils/request'

interface ErrorLog {
  /** 错误消息 */
  message: string
  /** 错误堆栈 */
  stack?: string
  /** 来源（vue / promise / window） */
  source: string
  /** 当前页面 URL */
  url: string
  /** 用户 agent */
  userAgent: string
  /** 发生时间 */
  time: string
  /** 当前用户（若有） */
  username?: string
}

/** 错误日志缓冲队列，满 10 条批量上报 */
const buffer: ErrorLog[] = []
const BATCH_SIZE = 10
/** 防抖定时器（5秒内自动 flush） */
let flushTimer: ReturnType<typeof setTimeout> | null = null

function pushLog(log: ErrorLog) {
  buffer.push(log)
  if (buffer.length >= BATCH_SIZE) {
    flush()
  } else if (!flushTimer) {
    flushTimer = setTimeout(flush, 5000)
  }
}

function flush() {
  if (buffer.length === 0) return
  const logs = buffer.splice(0)
  if (flushTimer) {
    clearTimeout(flushTimer)
    flushTimer = null
  }
  // 静默上报，不阻断业务
  request.post('/sys/monitor/errorLog', logs).catch(() => {
    console.warn('[ErrorMonitor] 上报失败，日志丢失', logs.length, '条')
  })
}

/** 页面关闭前强制 flush */
function setupBeforeUnload() {
  window.addEventListener('beforeunload', () => {
    if (buffer.length > 0) {
      // beforeunload 时只能使用 sendBeacon（同步可靠）
      const blob = new Blob([JSON.stringify(buffer)], { type: 'application/json' })
      navigator.sendBeacon?.('/api/sys/monitor/errorLog', blob)
    }
  })
}

export function setupErrorMonitor(app: App) {
  // 1. Vue 组件内异常
  app.config.errorHandler = (err, instance, info) => {
    console.error('[Vue Error]', info, err)
    pushLog({
      message: (err as Error).message,
      stack: (err as Error).stack,
      source: `vue:${info}`,
      url: location.href,
      userAgent: navigator.userAgent,
      time: new Date().toISOString(),
    })
  }

  // 2. 未捕获的 Promise 异常
  window.addEventListener('unhandledrejection', (event) => {
    console.error('[Promise Error]', event.reason)
    pushLog({
      message: String(event.reason?.message || event.reason),
      stack: event.reason?.stack,
      source: 'promise',
      url: location.href,
      userAgent: navigator.userAgent,
      time: new Date().toISOString(),
    })
  })

  // 3. 全局 JS 异常（非 Vue 组件内）
  window.addEventListener('error', (event) => {
    // 过滤掉 Vue 已处理的异常
    if (event.error && (event.error as any).__vueHandled) return
    console.error('[Window Error]', event.error)
    pushLog({
      message: event.message,
      stack: event.error?.stack,
      source: 'window',
      url: location.href,
      userAgent: navigator.userAgent,
      time: new Date().toISOString(),
    })
  })

  setupBeforeUnload()
}

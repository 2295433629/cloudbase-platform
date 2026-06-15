import request from '@/utils/request'
import type {PageParams, TableResponse} from '@/types/api'

export interface MessageItem {
  id: number
  title: string
  content: string
  msgType: string
  sendType: string
  status: number
  createTime: string
  isRead?: number
  readTime?: string
}

export function getMessagePage(data: PageParams & { type?: string }): Promise<TableResponse<MessageItem>> {
  return request({ url: '/sys/message/page', method: 'post', data })
}

export function getMessageDetail(id: number): Promise<MessageItem> {
  return request({ url: '/sys/message/detail', method: 'post', params: { id } })
}

export function markMessageRead(data: { messageId: number }): Promise<void> {
  return request({ url: '/sys/message/read', method: 'post', data })
}

export function markAllMessagesRead(): Promise<void> {
  return request({ url: '/sys/message/readAll', method: 'post' })
}

export function getUnreadCount(): Promise<number> {
  return request({ url: '/sys/message/unreadCount', method: 'post' })
}

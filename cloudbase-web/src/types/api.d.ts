/** 通用响应结构 */
export interface ApiResponse<T = any> {
  code: string
  msg: string
  data: T
}

/** 分页响应结构 */
export interface TableResponse<T = any> {
  code: string
  msg: string
  rows: T[]
  total: number
}

/** 分页请求参数 */
export interface PageParams {
  pageNo: number
  pageSize: number
}

export interface ApiResponse<T = any> {
  code: number
  msg: string
  data: T
}

export interface TableResponse<T = any> {
  code: number
  msg: string
  rows: T[]
  total: number
}

export interface PageParams {
  pageNo: number
  pageSize: number
}

// 通用响应类型
export interface Result<T> {
  code: number
  message: string
  data: T
  timestamp?: number
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
  pages: number
}
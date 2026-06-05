export interface Department {
  id: number
  name: string
  code: string
  sortOrder?: number
  createTime?: string
}

export interface Major {
  id: number
  name: string
  code: string
  departmentId: number
  departmentName?: string
  years?: number
  createTime?: string
}

export interface Grade {
  id: number
  name: string
  year?: number
  createTime?: string
}

export interface ClassInfo {
  id: number
  className: string
  departmentId?: number
  departmentName?: string
  majorId?: number
  majorName?: string
  gradeId?: number
  gradeName?: string
  advisor?: string
  counselorId?: number
  counselorName?: string
  createTime?: string
}
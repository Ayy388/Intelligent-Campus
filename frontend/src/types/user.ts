export interface User {
  id: number
  username: string
  password?: string
  realName: string
  gender?: number
  phone?: string
  email?: string
  avatar?: string
  roleId: number
  roleName?: string
  departmentId?: number
  departmentName?: string
  majorId?: number
  majorName?: string
  classId?: number
  className?: string
  gradeName?: string
  counselorId?: number
  counselorName?: string
  counselorClasses?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  userId: number
  username: string
  realName: string
  role: string
  avatar?: string
}

export interface Role {
  id: number
  roleCode: string
  roleName: string
  description?: string
  createTime?: string
}
export interface CardRecharge {
  id: number
  userId: number
  studentId?: number
  amount: number
  balance?: number
  rechargedAt?: string
  createTime?: string
}

export interface LostFound {
  id: number
  userId?: number
  userName?: string
  title: string
  description?: string
  images?: string
  location?: string
  contact?: string
  type: number
  status: number
  reporterId?: number
  createTime?: string
}
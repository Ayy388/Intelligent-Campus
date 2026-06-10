export interface Notification {
  id: number
  title: string
  content: string
  category?: string
  targetType?: string
  publisherId?: number
  publisherName?: string
  isTop?: number
  isUrgent?: number
  attachment?: string
  viewCount?: number
  targetRole?: string
  read?: boolean
  createTime?: string
  updateTime?: string
}

export interface LeaveApplication {
  id: number
  studentId: number
  studentName?: string
  leaveType: string
  reason: string
  attachment?: string
  teacherId?: number
  type?: string
  startDate: string
  endDate: string
  startTime?: string
  endTime?: string
  status: number
  approverId?: number
  rejectReason?: string
  applyTime?: string
  approveTime?: string
  createTime?: string
}
import type { Dayjs } from 'dayjs'

export interface Activity {
  id: number
  title: string
  description?: string
  content?: string
  location?: string
  clubId?: number
  clubName?: string
  activityType?: string
  category?: string
  coverImage?: string
  summary?: string
  images?: string
  startTime: string
  endTime: string
  maxParticipants?: number
  currentParticipants?: number
  creatorId?: number
  creatorName?: string
  creatorRole?: string
  registered?: boolean
  organizerId?: number
  organizerName?: string
  status: number
  approverId?: number
  rejectReason?: string
  approveTime?: string
  confirmTime?: string
  createTime?: string
}

export interface ActivityRegistration {
  id: number
  activityId: number
  userId: number
  userName?: string
  activityTitle?: string
  registerTime?: string
  registeredAt?: string
  status: number
}
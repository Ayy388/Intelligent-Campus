export interface Club {
  id: number
  name: string
  description?: string
  logo?: string
  advisorId?: number
  advisorName?: string
  presidentId: number
  presidentName?: string
  memberCount?: number
  status: number
  createTime?: string
}

export interface ClubMember {
  id: number
  clubId: number
  userId: number
  userName?: string
  clubName?: string
  role: string
  status?: number
  applyReason?: string
  applyTime?: string
  approveTime?: string
  joinedAt?: string
}

export interface Venue {
  id: number
  name: string
  location: string
  capacity: number
  description?: string
  status: number
  createTime?: string
}

export interface VenueBooking {
  id: number
  venueId?: number
  clubId?: number
  userId?: number
  reason?: string
  startTime?: string
  endTime?: string
  status: number
  rejectReason?: string
  createTime?: string
}
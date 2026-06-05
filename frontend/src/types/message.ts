export interface Conversation {
  id: number
  user1Id?: number
  user2Id?: number
  participantIds?: string
  participantNames?: string
  lastMessage?: string
  lastTime?: string
  lastMessageTime?: string
  peerName?: string
  unreadCount?: number
  createTime?: string
}

export interface Message {
  id: number
  conversationId: number
  senderId: number
  senderName?: string
  content: string
  isRead?: number
  createTime?: string
  sentAt?: string
  readAt?: string
}

export interface Announcement {
  id: number
  title: string
  content: string
  targetType?: string
  targetValue?: string
  targetRoles?: string
  publisherId?: number
  publisherName?: string
  sendTime?: string
  createTime?: string
  status?: number
}
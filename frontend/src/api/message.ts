import request from '@/utils/request'

export function getConversations(params: any) {
  return request.get('/message/conversations', { params })
}

export function getConversationMessages(id: number) {
  return request.get(`/message/conversations/${id}/detail`)
}

export function sendMessage(peerId: number, content: string, conversationId?: number) {
  return request.post('/message/send', null, { params: { peerId, content, conversationId } })
}

export function markRead(id: number) {
  return request.put(`/message/read/${id}`)
}

export function getAnnouncements(params: any) {
  return request.get('/message/announcement', { params })
}

export function pushAnnouncement(data: any) {
  return request.post('/message/announcement', data)
}

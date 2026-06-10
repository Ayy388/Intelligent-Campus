import request from '@/utils/request'

export function getConversations(params?: Record<string, any>) {
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

// 班级群聊
export function getMyGroups() {
  return request.get('/message/groups')
}

export function getGroupMessages(groupId: number, page = 1, size = 50) {
  return request.get(`/message/groups/${groupId}/messages`, { params: { page, size } })
}

export function sendGroupMessage(groupId: number, content: string) {
  return request.post(`/message/groups/${groupId}/send`, { content })
}

export function markGroupRead(groupId: number) {
  return request.post(`/message/groups/${groupId}/read`)
}
import request from '@/utils/request'

export function getConversations() {
  return request.get('/ai/conversations')
}

export function getMessages(conversationId: number) {
  return request.get(`/ai/conversations/${conversationId}`)
}

export function deleteConversation(id: number) {
  return request.delete(`/ai/conversations/${id}`)
}

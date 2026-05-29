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

export function chat(question: string, conversationId?: number): Promise<Response> {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams({ question })
  if (conversationId) params.append('conversationId', String(conversationId))
  return fetch(`/api/ai/chat?${params}`, {
    method: 'POST',
    headers: { Authorization: `Bearer ${token}` }
  })
}

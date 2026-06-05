import request from '@/utils/request';
export function getConversations() {
    return request.get('/ai/conversations');
}
export function getMessages(conversationId) {
    return request.get(`/ai/conversations/${conversationId}`);
}
export function deleteConversation(id) {
    return request.delete(`/ai/conversations/${id}`);
}
export function chat(question, conversationId) {
    const token = localStorage.getItem('token');
    const params = new URLSearchParams({ question });
    if (conversationId)
        params.append('conversationId', String(conversationId));
    return fetch(`/api/ai/chat?${params}`, {
        method: 'POST',
        headers: { Authorization: `Bearer ${token}` }
    });
}

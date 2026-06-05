import request from '@/utils/request';
export function getConversations(params) {
    return request.get('/message/conversations', { params });
}
export function getConversationMessages(id) {
    return request.get(`/message/conversations/${id}/detail`);
}
export function sendMessage(peerId, content, conversationId) {
    return request.post('/message/send', null, { params: { peerId, content, conversationId } });
}
export function markRead(id) {
    return request.put(`/message/read/${id}`);
}
export function getAnnouncements(params) {
    return request.get('/message/announcement', { params });
}
export function pushAnnouncement(data) {
    return request.post('/message/announcement', data);
}

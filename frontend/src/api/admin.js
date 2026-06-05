import request from '@/utils/request';
export function getNotifications(params) {
    return request.get('/admin/notifications', { params });
}
export function getNotification(id) {
    return request.get(`/admin/notifications/${id}`);
}
export function addNotification(data) {
    return request.post('/admin/notifications', data);
}
export function updateNotification(id, data) {
    return request.put(`/admin/notifications/${id}`, data);
}
export function deleteNotification(id) {
    return request.delete(`/admin/notifications/${id}`);
}
export function getUnreadNotificationCount() {
    return request.get('/admin/notifications/unread-count');
}
export function markNotificationRead(id) {
    return request.post(`/admin/notifications/${id}/read`);
}
export function getLeaves(params) {
    return request.get('/admin/leaves', { params });
}
export function getLeave(id) {
    return request.get(`/admin/leaves/${id}`);
}
export function applyLeave(data) {
    return request.post('/admin/leaves', data);
}
export function approveLeave(id, status, reason) {
    return request.put(`/admin/leaves/${id}/approve`, null, { params: { status, reason } });
}

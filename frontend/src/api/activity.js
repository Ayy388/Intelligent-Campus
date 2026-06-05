import request from '@/utils/request';
export function getPublicActivities(params) {
    return request.get('/activity/public', { params });
}
export function getClubs() {
    return request.get('/club/list');
}
export function updateActivitySummary(id, summary, images) {
    return request.put(`/activity/${id}/summary`, null, { params: { summary, images } });
}
export function createActivity(data) {
    return request.post('/activity', data);
}
export function getMyActivities(params) {
    return request.get('/activity/my', { params });
}
export function getPendingActivities(params) {
    return request.get('/activity/pending', { params });
}
export function getActivity(id) {
    return request.get(`/activity/${id}`);
}
export function approveActivity(id, status, rejectReason) {
    return request.put(`/activity/${id}/approve`, null, { params: { status, rejectReason } });
}
export function confirmActivity(id) {
    return request.put(`/activity/${id}/confirm`);
}
export function registerActivity(id) {
    return request.post(`/activity/${id}/register`);
}
export function cancelRegistration(activityId) {
    return request.delete(`/activity/${activityId}/register`);
}
export function getRegistrations(activityId, params) {
    return request.get(`/activity/${activityId}/registrations`, { params });
}
export function getMyRegistrations() {
    return request.get('/activity/my-registrations');
}
export function withdrawActivity(id) {
    return request.delete(`/activity/${id}`);
}
export function finishActivity(id) {
    return request.put(`/activity/${id}/finish`);
}

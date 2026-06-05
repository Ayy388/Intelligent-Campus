import request from '@/utils/request';
export function getClubs() { return request.get('/club/list'); }
export function getClub(id) { return request.get(`/club/${id}`); }
export function createClub(data) { return request.post('/club', data); }
export function updateClub(id, data) { return request.put(`/club/${id}`, data); }
export function deleteClub(id) { return request.delete(`/club/${id}`); }
export function approveClub(id, status) {
    return request.put(`/club/${id}/approve`, null, { params: { status } });
}
export function leaveClub(clubId) {
    return request.delete(`/club/member/${clubId}`);
}
export function disbandClub(clubId) {
    return request.post(`/club/${clubId}/disband`);
}
export function approveDisband(clubId, status) {
    return request.post(`/club/${clubId}/approve-disband`, null, { params: { status } });
}
export function cancelDisband(clubId) {
    return request.post(`/club/${clubId}/cancel-disband`);
}
export function removeMember(clubId, memberId) {
    return request.delete(`/club/${clubId}/members/${memberId}`);
}
export function transferPresident(clubId, targetUserId) {
    return request.post(`/club/${clubId}/transfer/${targetUserId}`);
}
export function setMemberRole(clubId, memberId, role) {
    return request.put(`/club/${clubId}/members/${memberId}/role`, null, { params: { role } });
}
export function applyMember(clubId, reason) {
    return request.post('/club/member/apply', null, { params: { clubId, reason } });
}
export function approveMember(id, status) {
    return request.put(`/club/member/${id}`, null, { params: { status } });
}
export function getMembers(clubId) { return request.get(`/club/${clubId}/members`); }
export function getMyMemberships() { return request.get('/club/member/my'); }
export function getVenues() { return request.get('/club/venue'); }
export function getBookings(params) { return request.get('/club/venue/booking', { params }); }
export function applyBooking(data) { return request.post('/club/venue/booking', data); }
export function approveBooking(id, status, reason) {
    return request.put(`/club/venue/booking/${id}`, null, { params: { status, reason } });
}
export function addVenue(data) { return request.post('/club/venue/add', data); }
export function updateVenue(id, data) { return request.put(`/club/venue/${id}`, data); }
export function deleteVenue(id) { return request.delete(`/club/venue/${id}`); }

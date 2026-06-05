import request from '@/utils/request'

export function getClubs() { return request.get('/club/list') }
export function getClub(id: number) { return request.get(`/club/${id}`) }
export function createClub(data: Record<string, any>) { return request.post('/club', data) }
export function updateClub(id: number, data: Record<string, any>) { return request.put(`/club/${id}`, data) }
export function deleteClub(id: number) { return request.delete(`/club/${id}`) }
export function approveClub(id: number, status: number) {
  return request.put(`/club/${id}/approve`, null, { params: { status } })
}
export function leaveClub(clubId: number) {
  return request.delete(`/club/member/${clubId}`)
}
export function disbandClub(clubId: number) {
  return request.post(`/club/${clubId}/disband`)
}
export function approveDisband(clubId: number, status: number) {
  return request.post(`/club/${clubId}/approve-disband`, null, { params: { status } })
}
export function cancelDisband(clubId: number) {
  return request.post(`/club/${clubId}/cancel-disband`)
}

export function removeMember(clubId: number, memberId: number) {
  return request.delete(`/club/${clubId}/members/${memberId}`)
}
export function transferPresident(clubId: number, targetUserId: number) {
  return request.post(`/club/${clubId}/transfer/${targetUserId}`)
}
export function setMemberRole(clubId: number, memberId: number, role: string) {
  return request.put(`/club/${clubId}/members/${memberId}/role`, null, { params: { role } })
}

export function applyMember(clubId: number, reason: string) {
  return request.post('/club/member/apply', null, { params: { clubId, reason } })
}
export function approveMember(id: number, status: number) {
  return request.put(`/club/member/${id}`, null, { params: { status } })
}
export function getMembers(clubId: number) { return request.get(`/club/${clubId}/members`) }
export function getMyMemberships() { return request.get('/club/member/my') }

export function getVenues() { return request.get('/club/venue') }
export function getBookings(params?: Record<string, any>) { return request.get('/club/venue/booking', { params }) }
export function applyBooking(data: Record<string, any>) { return request.post('/club/venue/booking', data) }
export function approveBooking(id: number, status: number, reason?: string) {
  return request.put(`/club/venue/booking/${id}`, null, { params: { status, reason } })
}
export function addVenue(data: Record<string, any>) { return request.post('/club/venue/add', data) }
export function updateVenue(id: number, data: Record<string, any>) { return request.put(`/club/venue/${id}`, data) }
export function deleteVenue(id: number) { return request.delete(`/club/venue/${id}`) }
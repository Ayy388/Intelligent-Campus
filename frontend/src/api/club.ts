import request from '@/utils/request'

export function getClubs() { return request.get('/club/list') }
export function getClub(id: number) { return request.get(`/club/${id}`) }
export function createClub(data: any) { return request.post('/club', data) }
export function updateClub(id: number, data: any) { return request.put(`/club/${id}`, data) }
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

export function applyMember(clubId: number, reason: string) {
  return request.post('/club/member/apply', null, { params: { clubId, reason } })
}
export function approveMember(id: number, status: number) {
  return request.put(`/club/member/${id}`, null, { params: { status } })
}
export function getMembers(clubId: number) { return request.get(`/club/${clubId}/members`) }
export function getMyMemberships() { return request.get('/club/member/my') }

export function getActivities(params: any) { return request.get('/club/activity', { params }) }
export function createActivity(data: any) { return request.post('/club/activity', data) }
export function enrollActivity(activityId: number) {
  return request.post('/club/activity/enroll', null, { params: { activityId } })
}
export function getEnrollments(activityId: number) {
  return request.get(`/club/activity/${activityId}/enrollments`)
}
export function updateActivitySummary(id: number, summary: string, images?: string) {
  return request.put(`/club/activity/${id}/summary`, null, { params: { summary, images } })
}

export function getVenues() { return request.get('/club/venue') }
export function getBookings(params: any) { return request.get('/club/venue/booking', { params }) }
export function applyBooking(data: any) { return request.post('/club/venue/booking', data) }
export function approveBooking(id: number, status: number, reason?: string) {
  return request.put(`/club/venue/booking/${id}`, null, { params: { status, reason } })
}

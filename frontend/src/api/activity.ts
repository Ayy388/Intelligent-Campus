import request from '@/utils/request'

export function getPublicActivities(params: { page?: number; size?: number; clubId?: number }) {
  return request.get('/activity/public', { params })
}

export function getClubs() {
  return request.get('/club/list')
}

export function updateActivitySummary(id: number, summary: string, images?: string) {
  return request.put(`/activity/${id}/summary`, null, { params: { summary, images } })
}

export function createActivity(data: any) {
  return request.post('/activity', data)
}

export function getMyActivities(params: { page?: number; size?: number }) {
  return request.get('/activity/my', { params })
}

export function getPendingActivities(params: { page?: number; size?: number }) {
  return request.get('/activity/pending', { params })
}

export function getActivity(id: number) {
  return request.get(`/activity/${id}`)
}

export function approveActivity(id: number, status: number, rejectReason?: string) {
  return request.put(`/activity/${id}/approve`, null, { params: { status, rejectReason } })
}

export function confirmActivity(id: number) {
  return request.put(`/activity/${id}/confirm`)
}

export function registerActivity(id: number) {
  return request.post(`/activity/${id}/register`)
}

export function cancelRegistration(activityId: number) {
  return request.delete(`/activity/${activityId}/register`)
}

export function getRegistrations(activityId: number, params: { page?: number; size?: number }) {
  return request.get(`/activity/${activityId}/registrations`, { params })
}

export function getMyRegistrations() {
  return request.get('/activity/my-registrations')
}

export function withdrawActivity(id: number) {
  return request.delete(`/activity/${id}`)
}

export function finishActivity(id: number) {
  return request.put(`/activity/${id}/finish`)
}

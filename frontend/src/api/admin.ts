import request from '@/utils/request'

export function getNotifications(params: any) {
  return request.get('/admin/notifications', { params })
}

export function getNotification(id: number) {
  return request.get(`/admin/notifications/${id}`)
}

export function addNotification(data: any) {
  return request.post('/admin/notifications', data)
}

export function updateNotification(id: number, data: any) {
  return request.put(`/admin/notifications/${id}`, data)
}

export function deleteNotification(id: number) {
  return request.delete(`/admin/notifications/${id}`)
}

export function getLeaves(params: any) {
  return request.get('/admin/leaves', { params })
}

export function applyLeave(data: any) {
  return request.post('/admin/leaves', data)
}

export function approveLeave(id: number, status: number, reason?: string) {
  return request.put(`/admin/leaves/${id}/approve`, null, { params: { status, reason } })
}

export function getGuides(params: any) {
  return request.get('/admin/guides', { params })
}

export function getGuide(id: number) {
  return request.get(`/admin/guides/${id}`)
}

export function addGuide(data: any) {
  return request.post('/admin/guides', data)
}

export function updateGuide(id: number, data: any) {
  return request.put(`/admin/guides/${id}`, data)
}

export function deleteGuide(id: number) {
  return request.delete(`/admin/guides/${id}`)
}

import request from '@/utils/request'

export function getUsers(params: any) {
  return request.get('/sys/users', { params })
}

export function createUser(data: any) {
  return request.post('/sys/users', data)
}

export function updateUser(id: number, data: any) {
  return request.put(`/sys/users/${id}`, data)
}

export function toggleUserStatus(id: number, status: number) {
  return request.put(`/sys/users/${id}/status`, null, { params: { status } })
}

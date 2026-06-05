import request from '@/utils/request'

export function login(username: string, password: string) {
  return request.post('/auth/login', { username, password })
}

export function getCurrentUser() {
  return request.get('/auth/me')
}

export function updateProfile(data: { realName?: string; phone?: string; email?: string; gender?: number; avatar?: string }) {
  return request.put('/auth/profile', data)
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request.put('/auth/password', { oldPassword, newPassword })
}

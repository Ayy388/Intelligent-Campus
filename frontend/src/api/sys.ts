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

export function getClasses(params: any) {
  return request.get('/sys/classes', { params })
}

export function getAllClasses() {
  return request.get('/sys/classes/all')
}

export function getClass(id: number) {
  return request.get(`/sys/classes/${id}`)
}

export function createClass(data: any) {
  return request.post('/sys/classes', data)
}

export function updateClass(id: number, data: any) {
  return request.put(`/sys/classes/${id}`, data)
}

export function deleteClass(id: number) {
  return request.delete(`/sys/classes/${id}`)
}

// === 院系管理 ===
export function getDepartments(params: any) {
  return request.get('/sys/departments', { params })
}
export function getAllDepartments() {
  return request.get('/sys/departments/all')
}
export function createDepartment(data: any) {
  return request.post('/sys/departments', data)
}
export function updateDepartment(id: number, data: any) {
  return request.put(`/sys/departments/${id}`, data)
}
export function deleteDepartment(id: number) {
  return request.delete(`/sys/departments/${id}`)
}

// === 专业管理 ===
export function getMajors(params: any) {
  return request.get('/sys/majors', { params })
}
export function getMajorsByDept(deptId: number) {
  return request.get('/sys/majors/all', { params: { deptId } })
}
export function createMajor(data: any) {
  return request.post('/sys/majors', data)
}
export function updateMajor(id: number, data: any) {
  return request.put(`/sys/majors/${id}`, data)
}
export function deleteMajor(id: number) {
  return request.delete(`/sys/majors/${id}`)
}

// === 年级管理 ===
export function getGrades(params: any) {
  return request.get('/sys/grades', { params })
}
export function getAllGrades() {
  return request.get('/sys/grades/all')
}
export function createGrade(data: any) {
  return request.post('/sys/grades', data)
}
export function updateGrade(id: number, data: any) {
  return request.put(`/sys/grades/${id}`, data)
}
export function deleteGrade(id: number) {
  return request.delete(`/sys/grades/${id}`)
}

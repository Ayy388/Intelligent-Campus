import request from '@/utils/request'

export function getCourses(params: any) {
  return request.get('/edu/courses', { params })
}

export function getCourse(id: number) {
  return request.get(`/edu/courses/${id}`)
}

export function addCourse(data: any) {
  return request.post('/edu/courses', data)
}

export function updateCourse(id: number, data: any) {
  return request.put(`/edu/courses/${id}`, data)
}

export function deleteCourse(id: number) {
  return request.delete(`/edu/courses/${id}`)
}

export function getSelections() {
  return request.get('/edu/selections')
}

export function selectCourse(courseId: number, semester: string) {
  return request.post('/edu/selections', null, { params: { courseId, semester } })
}

export function dropCourse(selectionId: number) {
  return request.delete(`/edu/selections/${selectionId}`)
}

export function getMyGrades() {
  return request.get('/edu/grades')
}

export function inputGrade(data: any) {
  return request.post('/edu/grades', data)
}

export function getSchedule() {
  return request.get('/edu/schedule')
}

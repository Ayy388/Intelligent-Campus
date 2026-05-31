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

export function confirmCourse(id: number) {
  return request.post(`/edu/courses/${id}/confirm`)
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

export function getSchedule(semester?: string, week?: number) {
  return request.get('/edu/schedule', { params: { semester, week } })
}

export function importCourses(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/edu/courses/import', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function getSemesters(params?: any) {
  return request.get('/edu/semesters', { params })
}

export function getSemester(id: number) {
  return request.get(`/edu/semesters/${id}`)
}

export function addSemester(data: any) {
  return request.post('/edu/semesters', data)
}

export function updateSemester(id: number, data: any) {
  return request.put(`/edu/semesters/${id}`, data)
}

export function deleteSemester(id: number) {
  return request.delete(`/edu/semesters/${id}`)
}

export function getCourseStudents(courseId: number) {
  return request.get(`/edu/selections/course/${courseId}`)
}

export function getTeacherCourses() {
  return request.get('/edu/courses/teacher')
}

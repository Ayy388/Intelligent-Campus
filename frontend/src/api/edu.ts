import request from '@/utils/request'

export function getCourses(params?: Record<string, any>) {
  return request.get('/edu/courses', { params })
}

export function getCourse(id: number) {
  return request.get(`/edu/courses/${id}`)
}

export function addCourse(data: Record<string, any>) {
  return request.post('/edu/courses', data)
}

export function updateCourse(id: number, data: Record<string, any>) {
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

export function getMyGrades(semester?: string) {
  return request.get('/edu/grades/all', { params: { semester } })
}

export function inputGrade(data: Record<string, any>) {
  return request.post('/edu/grades', data)
}

export function updateGrade(id: number, data: Record<string, any>) {
  return request.put(`/edu/grades/${id}`, data)
}

export function deleteGrade(id: number) {
  return request.delete(`/edu/grades/${id}`)
}

export function getCourseGradesPage(courseId: number, params?: Record<string, any>) {
  return request.get(`/edu/grades/course/${courseId}`, { params })
}

export function getCourseGrades(courseId: number) {
  return request.get(`/edu/grades/course/${courseId}/all`)
}

export function getCourseStatistics(courseId: number) {
  return request.get(`/edu/grades/course/${courseId}/statistics`)
}

export function getTranscript() {
  return request.get('/edu/grades/transcript')
}

export function getTeacherGradeStatus(semester?: string) {
  return request.get('/edu/grades/teacher/status', { params: { semester } })
}

export function getSchedule(semester?: string, week?: number) {
  return request.get('/edu/schedule', { params: { semester, week } })
}

// ===== Schedule Management =====
export function addScheduleItem(courseId: number, data: Record<string, any>) {
  return request.post(`/edu/courses/${courseId}/schedule`, data)
}

export function updateScheduleItem(courseId: number, index: number, data: Record<string, any>) {
  return request.put(`/edu/courses/${courseId}/schedule/${index}`, data)
}

export function removeScheduleItem(courseId: number, index: number) {
  return request.delete(`/edu/courses/${courseId}/schedule/${index}`)
}

export function getScheduleByClass(classId: number) {
  return request.get(`/edu/schedule/class/${classId}`)
}

export function getScheduleByTeacher(teacherId: number) {
  return request.get(`/edu/schedule/teacher/${teacherId}`)
}

export function getScheduleByRoom(classroom: string) {
  return request.get(`/edu/schedule/room/${encodeURIComponent(classroom)}`)
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

export function getSemesters(params?: Record<string, any>) {
  return request.get('/edu/semesters', { params })
}

export function getSemester(id: number) {
  return request.get(`/edu/semesters/${id}`)
}

export function addSemester(data: Record<string, any>) {
  return request.post('/edu/semesters', data)
}

export function updateSemester(id: number, data: Record<string, any>) {
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

export function assignCourseClasses(courseId: number, classIds: number[]) {
  return request.post(`/edu/courses/${courseId}/assign-classes`, classIds)
}

export function getCourseClasses(courseId: number) {
  return request.get(`/edu/courses/${courseId}/classes`)
}

export function setCourseClasses(courseId: number, classes: Record<string, any>[]) {
  return request.put(`/edu/courses/${courseId}/classes`, classes)
}

export function getAvailableCourses() {
  return request.get('/edu/courses/available')
}

export function enrollCourse(courseId: number) {
  return request.post(`/edu/courses/${courseId}/enroll`)
}

export function confirmOpening(courseId: number) {
  return request.post(`/edu/courses/${courseId}/confirm-opening`)
}

export function cancelOpening(courseId: number) {
  return request.post(`/edu/courses/${courseId}/cancel-opening`)
}

// ===== Training Plans =====
export function getTrainingPlans(params?: Record<string, any>) {
  return request.get('/edu/training-plans', { params })
}

export function getTrainingPlan(id: number) {
  return request.get(`/edu/training-plans/${id}`)
}

export function createTrainingPlan(data: Record<string, any>) {
  return request.post('/edu/training-plans', data)
}

export function updateTrainingPlan(id: number, data: Record<string, any>) {
  return request.put(`/edu/training-plans/${id}`, data)
}

export function deleteTrainingPlan(id: number) {
  return request.delete(`/edu/training-plans/${id}`)
}

export function getPlanItems(planId: number, semesterNumber: number) {
  return request.get(`/edu/training-plans/${planId}/items`, { params: { semesterNumber } })
}

export function addPlanItem(planId: number, data: Record<string, any>) {
  return request.post(`/edu/training-plans/${planId}/items`, data)
}

export function updatePlanItem(itemId: number, data: Record<string, any>) {
  return request.put(`/edu/training-plans/items/${itemId}`, data)
}

export function deletePlanItem(itemId: number) {
  return request.delete(`/edu/training-plans/items/${itemId}`)
}

export function generateSemester(planId: number, semesterNumber: number) {
  return request.post(`/edu/training-plans/${planId}/generate/${semesterNumber}`)
}

export function getMyTrainingPlan() {
  return request.get('/edu/training-plans/my-plan')
}

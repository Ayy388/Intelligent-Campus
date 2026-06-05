import request from '@/utils/request';
export function getCourses(params) {
    return request.get('/edu/courses', { params });
}
export function getCourse(id) {
    return request.get(`/edu/courses/${id}`);
}
export function addCourse(data) {
    return request.post('/edu/courses', data);
}
export function updateCourse(id, data) {
    return request.put(`/edu/courses/${id}`, data);
}
export function deleteCourse(id) {
    return request.delete(`/edu/courses/${id}`);
}
export function confirmCourse(id) {
    return request.post(`/edu/courses/${id}/confirm`);
}
export function getSelections() {
    return request.get('/edu/selections');
}
export function selectCourse(courseId, semester) {
    return request.post('/edu/selections', null, { params: { courseId, semester } });
}
export function dropCourse(selectionId) {
    return request.delete(`/edu/selections/${selectionId}`);
}
export function getMyGrades(semester) {
    return request.get('/edu/grades/all', { params: { semester } });
}
export function inputGrade(data) {
    return request.post('/edu/grades', data);
}
export function updateGrade(id, data) {
    return request.put(`/edu/grades/${id}`, data);
}
export function deleteGrade(id) {
    return request.delete(`/edu/grades/${id}`);
}
export function getCourseGradesPage(courseId, params) {
    return request.get(`/edu/grades/course/${courseId}`, { params });
}
export function getCourseGrades(courseId) {
    return request.get(`/edu/grades/course/${courseId}/all`);
}
export function getCourseStatistics(courseId) {
    return request.get(`/edu/grades/course/${courseId}/statistics`);
}
export function getTranscript() {
    return request.get('/edu/grades/transcript');
}
export function getTeacherGradeStatus(semester) {
    return request.get('/edu/grades/teacher/status', { params: { semester } });
}
export function getSchedule(semester, week) {
    return request.get('/edu/schedule', { params: { semester, week } });
}
// ===== Schedule Management =====
export function addScheduleItem(courseId, data) {
    return request.post(`/edu/courses/${courseId}/schedule`, data);
}
export function updateScheduleItem(courseId, index, data) {
    return request.put(`/edu/courses/${courseId}/schedule/${index}`, data);
}
export function removeScheduleItem(courseId, index) {
    return request.delete(`/edu/courses/${courseId}/schedule/${index}`);
}
export function getScheduleByClass(classId) {
    return request.get(`/edu/schedule/class/${classId}`);
}
export function getScheduleByTeacher(teacherId) {
    return request.get(`/edu/schedule/teacher/${teacherId}`);
}
export function getScheduleByRoom(classroom) {
    return request.get(`/edu/schedule/room/${encodeURIComponent(classroom)}`);
}
export function importCourses(file) {
    const formData = new FormData();
    formData.append('file', file);
    return request.post('/edu/courses/import', formData, {
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    });
}
export function getSemesters(params) {
    return request.get('/edu/semesters', { params });
}
export function getSemester(id) {
    return request.get(`/edu/semesters/${id}`);
}
export function addSemester(data) {
    return request.post('/edu/semesters', data);
}
export function updateSemester(id, data) {
    return request.put(`/edu/semesters/${id}`, data);
}
export function deleteSemester(id) {
    return request.delete(`/edu/semesters/${id}`);
}
export function getCourseStudents(courseId) {
    return request.get(`/edu/selections/course/${courseId}`);
}
export function getTeacherCourses() {
    return request.get('/edu/courses/teacher');
}
export function assignCourseClasses(courseId, classIds) {
    return request.post(`/edu/courses/${courseId}/assign-classes`, classIds);
}
export function getCourseClasses(courseId) {
    return request.get(`/edu/courses/${courseId}/classes`);
}
export function setCourseClasses(courseId, classes) {
    return request.put(`/edu/courses/${courseId}/classes`, classes);
}
export function getAvailableCourses() {
    return request.get('/edu/courses/available');
}
export function enrollCourse(courseId) {
    return request.post(`/edu/courses/${courseId}/enroll`);
}
export function confirmOpening(courseId) {
    return request.post(`/edu/courses/${courseId}/confirm-opening`);
}
export function cancelOpening(courseId) {
    return request.post(`/edu/courses/${courseId}/cancel-opening`);
}
// ===== Training Plans =====
export function getTrainingPlans(params) {
    return request.get('/edu/training-plans', { params });
}
export function getTrainingPlan(id) {
    return request.get(`/edu/training-plans/${id}`);
}
export function createTrainingPlan(data) {
    return request.post('/edu/training-plans', data);
}
export function updateTrainingPlan(id, data) {
    return request.put(`/edu/training-plans/${id}`, data);
}
export function deleteTrainingPlan(id) {
    return request.delete(`/edu/training-plans/${id}`);
}
export function getPlanItems(planId, semesterNumber) {
    return request.get(`/edu/training-plans/${planId}/items`, { params: { semesterNumber } });
}
export function addPlanItem(planId, data) {
    return request.post(`/edu/training-plans/${planId}/items`, data);
}
export function updatePlanItem(itemId, data) {
    return request.put(`/edu/training-plans/items/${itemId}`, data);
}
export function deletePlanItem(itemId) {
    return request.delete(`/edu/training-plans/items/${itemId}`);
}
export function generateSemester(planId, semesterNumber) {
    return request.post(`/edu/training-plans/${planId}/generate/${semesterNumber}`);
}
export function getMyTrainingPlan() {
    return request.get('/edu/training-plans/my-plan');
}

export interface Course {
  id: number
  courseCode: string
  courseName: string
  teacherId?: number
  teacherName?: string
  credit: number
  hours: number
  semester: string
  classroom?: string
  schedule?: string
  startWeek?: number
  endWeek?: number
  capacity: number
  courseType: string
  minStudents?: number
  enrollEnd?: string
  enrolled: number
  description?: string
  status: number
  createTime?: string
  // 排课信息字段（从 schedule JSON 解析后动态赋值）
  timeSlot?: number
  weeks?: string
}

export interface CourseSelection {
  id: number
  studentId: number
  courseId: number
  courseName?: string
  studentName?: string
  teacherName?: string
  credit?: number
  courseStatus?: number
  studentClassName?: string
  studentDepartment?: string
  studentPhone?: string
  studentUsername?: string
  graded?: boolean
  semester: string
  selectTime?: string
  status?: number
  selectType?: string
}

export interface ScheduleItem {
  day: number
  timeSlot: number
  weeks: string
}

export interface Grade {
  id: number
  studentId: number
  courseId: number
  teacherId?: number
  courseName?: string
  studentName?: string
  studentUsername?: string
  score?: number
  gradeType?: string
  gradeLevel?: string
  credit?: number
  semester: string
  remark?: string
  status?: number
  createTime?: string
}

export interface CourseClass {
  id: number
  courseId: number
  classId: number
  isRequired: number
}
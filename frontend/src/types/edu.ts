export interface Semester {
  id: number
  xn: string
  xqjc: string
  xqqc: string
  name?: string        // 兼容前端访问 name 的写法
  semester?: string    // 兼容前端访问 semester 的写法
  ksrq?: string
  jsrq?: string
  zc?: number
  status: number
  currentWeek?: number
  createTime?: string
}

export interface TrainingPlan {
  id: number
  name?: string
  majorId: number
  majorName?: string
  gradeId: number
  gradeName?: string
  totalSemesters: number
  status?: number
  description?: string
  createTime?: string
  updateTime?: string
}

export interface TrainingPlanItem {
  id: number
  planId: number
  semesterNumber: number
  courseName: string
  courseCode?: string
  credit: number
  hours: number
  isRequired?: number
  courseType?: string
  status?: number
  generatedCourseId?: number
  generatedCourseName?: string
  generatedCourseStatus?: number
  sortOrder?: number
  createTime?: string
}
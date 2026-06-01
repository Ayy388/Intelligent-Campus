import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  { path: '/login', name: 'Login', component: () => import('@/views/login/LoginView.vue') },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/dashboard/DashboardView.vue') },
      { path: 'edu/courses', name: 'CourseList', component: () => import('@/views/edu/CourseList.vue') },
      { path: 'edu/schedule', name: 'ScheduleView', component: () => import('@/views/edu/ScheduleView.vue') },
      { path: 'edu/course-students', name: 'CourseStudents', component: () => import('@/views/edu/CourseStudents.vue') },
      { path: 'edu/teaching', name: 'TeacherTeaching', component: () => import('@/views/edu/TeacherTeaching.vue') },
      { path: 'edu/selection', name: 'CourseSelection', component: () => import('@/views/edu/CourseSelection.vue') },
      { path: 'edu/my-select', name: 'MySelectCourse', component: () => import('@/views/edu/MySelectCourse.vue') },
      { path: 'edu/grades', name: 'GradeQuery', component: () => import('@/views/edu/GradeQuery.vue') },
      { path: 'edu/grade-entry', name: 'GradeEntry', component: () => import('@/views/edu/GradeEntry.vue') },
      { path: 'admin/classes', name: 'ClassManagement', component: () => import('@/views/sys/ClassManagement.vue'), meta: { title: '班级管理' } },
      { path: 'admin/departments', name: 'DepartmentManagement', component: () => import('@/views/sys/DepartmentManagement.vue'), meta: { title: '院系管理' } },
      { path: 'admin/majors', name: 'MajorManagement', component: () => import('@/views/sys/MajorManagement.vue'), meta: { title: '专业管理' } },
      { path: 'admin/grades', name: 'GradeManagement', component: () => import('@/views/sys/GradeManagement.vue'), meta: { title: '年级管理' } },
      { path: 'admin/notifications', name: 'Notifications', component: () => import('@/views/admin/NotificationList.vue') },
      { path: 'admin/leave', name: 'LeaveApply', component: () => import('@/views/admin/LeaveApply.vue') },
      { path: 'admin/leave-approval', name: 'LeaveApproval', component: () => import('@/views/admin/LeaveApproval.vue') },
      { path: 'life/card', name: 'CardRecharge', component: () => import('@/views/life/CardRecharge.vue') },
      { path: 'life/lost-found', name: 'LostFound', component: () => import('@/views/life/LostFound.vue') },
      { path: 'club/list', name: 'ClubList', component: () => import('@/views/club/ClubList.vue') },
      { path: 'club/space/:id', name: 'ClubSpace', component: () => import('@/views/club/ClubSpace.vue') },
      { path: 'club/activity', name: 'ActivityList', component: () => import('@/views/club/ActivityList.vue') },
      { path: 'admin/venue', name: 'VenueBooking', component: () => import('@/views/club/VenueBooking.vue') },
      { path: 'activity/center', name: 'ActivityCenter', component: () => import('@/views/activity/ActivityCenter.vue') },
      { path: 'activity/my', name: 'MyActivities', component: () => import('@/views/activity/MyActivities.vue') },
      { path: 'admin/activity-approval', name: 'ActivityApproval', component: () => import('@/views/activity/ActivityApproval.vue'), meta: { title: '活动审核' } },
      { path: 'growth/profile', name: 'StudentProfile', component: () => import('@/views/growth/StudentProfile.vue') },
      { path: 'growth/checkin', name: 'CheckIn', component: () => import('@/views/growth/CheckIn.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/profile/ProfileView.vue') },
      { path: 'todo', name: 'Todo', component: () => import('@/views/todo/TodoView.vue') },
      { path: 'message/chat', name: 'MessageDetail', component: () => import('@/views/message/MessageDetail.vue') },
      { path: 'message/announcement', name: 'AnnouncementPush', component: () => import('@/views/message/AnnouncementPush.vue') },
      { path: 'admin/club-approval', name: 'ClubApproval', component: () => import('@/views/club/ClubApproval.vue'), meta: { title: '社团审核' } },
      { path: 'admin/grade-stats', name: 'GradeStatistics', component: () => import('@/views/edu/GradeStatistics.vue'), meta: { title: '成绩统计' } },
      { path: 'admin/checkin-stats', name: 'CheckinStatistics', component: () => import('@/views/growth/CheckinStatistics.vue'), meta: { title: '签到统计' } },
      { path: 'admin/venue-approval', name: 'VenueApproval', component: () => import('@/views/admin/VenueApproval.vue'), meta: { title: '场地预约审核' } },
      { path: 'admin/venue-manage', name: 'VenueManage', component: () => import('@/views/admin/VenueManage.vue'), meta: { title: '场地管理' } },
      { path: 'ai/chat', name: 'AiChat', component: () => import('@/views/ai/AiChat.vue') },
      { path: 'manage/users', name: 'UserManage', component: () => import('@/views/manage/UserManage.vue') },
      { path: 'manage/courses', name: 'CourseManage', component: () => import('@/views/manage/CourseManage.vue') },
      { path: 'manage/semesters', name: 'SemesterManage', component: () => import('@/views/manage/SemesterManage.vue') }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, _from, next) => {
  const userStore = useUserStore()
  if (to.path === '/login') {
    next()
  } else if (!userStore.token) {
    next('/login')
  } else {
    next()
  }
})

export default router

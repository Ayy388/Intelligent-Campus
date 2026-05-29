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
      { path: 'edu/selection', name: 'CourseSelection', component: () => import('@/views/edu/CourseSelection.vue') },
      { path: 'edu/grades', name: 'GradeQuery', component: () => import('@/views/edu/GradeQuery.vue') },
      { path: 'admin/notifications', name: 'Notifications', component: () => import('@/views/admin/NotificationList.vue') },
      { path: 'admin/leave', name: 'LeaveApply', component: () => import('@/views/admin/LeaveApply.vue') },
      { path: 'admin/leave-approval', name: 'LeaveApproval', component: () => import('@/views/admin/LeaveApproval.vue') },
      { path: 'admin/guides', name: 'GuideList', component: () => import('@/views/admin/GuideList.vue') },
      { path: 'life/canteen', name: 'CanteenReview', component: () => import('@/views/life/CanteenReview.vue') },
      { path: 'life/card', name: 'CardRecharge', component: () => import('@/views/life/CardRecharge.vue') },
      { path: 'life/lost-found', name: 'LostFound', component: () => import('@/views/life/LostFound.vue') },
      { path: 'message/chat', name: 'ConversationList', component: () => import('@/views/message/ConversationList.vue') },
      { path: 'message/announcement', name: 'AnnouncementPush', component: () => import('@/views/message/AnnouncementPush.vue') },
      { path: 'ai/chat', name: 'AiChat', component: () => import('@/views/ai/AiChat.vue') },
      { path: 'manage/users', name: 'UserManage', component: () => import('@/views/manage/UserManage.vue') },
      { path: 'manage/courses', name: 'CourseManage', component: () => import('@/views/manage/CourseManage.vue') }
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

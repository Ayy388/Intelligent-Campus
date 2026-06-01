<template>
  <div class="sidebar-container">
    <div class="sidebar-logo">
      <div class="logo-icon">
        <span class="logo-emoji">🏫</span>
      </div>
      <transition name="logo-text">
        <div v-if="!appStore.sidebarCollapsed" class="logo-text-wrapper">
          <span class="logo-title">智能校园</span>
          <span class="logo-subtitle">Intelligent Campus</span>
        </div>
      </transition>
    </div>

    <div class="sidebar-menu-wrapper">
      <el-menu
        :default-active="route.path"
        router
        :collapse="appStore.sidebarCollapsed"
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <template #title>
            <span>首页</span>
          </template>
        </el-menu-item>

        <el-menu-item index="/profile">
          <el-icon><UserFilled /></el-icon>
          <template #title>
            <span>个人信息</span>
          </template>
        </el-menu-item>

        <el-menu-item index="/todo">
          <el-icon><Check /></el-icon>
          <template #title>
            <span>我的待办</span>
          </template>
        </el-menu-item>

        <el-sub-menu v-if="userStore.role !== 'admin' && userStore.role !== 'counselor'" index="edu">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>教务学习</span>
          </template>
          <el-menu-item v-if="userStore.role === 'teacher'" index="/edu/teaching">我的教学</el-menu-item>
          <el-menu-item v-if="userStore.role === 'teacher'" index="/edu/courses">课程列表</el-menu-item>
          <el-menu-item index="/edu/schedule">课程表</el-menu-item>
          <el-menu-item v-if="userStore.role === 'student'" index="/edu/selection">在线选课</el-menu-item>
          <el-menu-item v-if="userStore.role === 'student'" index="/edu/my-select">我的选课</el-menu-item>

          <el-menu-item index="/edu/grades">成绩查询</el-menu-item>
          <el-menu-item v-if="userStore.role === 'teacher'" index="/edu/grade-entry">成绩录入</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="admin">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>行政服务</span>
          </template>
          <el-menu-item index="/admin/notifications">通知公告</el-menu-item>
          <el-menu-item v-if="userStore.role === 'counselor' || userStore.role === 'admin'" index="/message/announcement">公告推送</el-menu-item>
          <el-menu-item v-if="userStore.role === 'counselor'" index="/admin/classes">班级管理</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/departments">院系管理</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/majors">专业管理</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/grades">年级管理</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/club-approval">社团审核</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/activity-approval">活动审核</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/venue-approval">场地预约审核</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin'" index="/admin/venue-manage">场地管理</el-menu-item>
          <el-menu-item v-if="userStore.role === 'counselor'" index="/admin/leave-approval">请假审批</el-menu-item>
          <el-menu-item v-if="userStore.role === 'admin' || userStore.role === 'teacher'" index="/admin/grade-stats">成绩统计</el-menu-item>
          <el-menu-item v-if="userStore.role === 'counselor'" index="/admin/checkin-stats">签到统计</el-menu-item>
          <el-menu-item v-if="userStore.role === 'student'" index="/admin/leave">请假申请</el-menu-item>
          <el-menu-item v-if="userStore.role !== 'admin' && userStore.role !== 'counselor'" index="/admin/venue">场地预约</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.role !== 'admin' && userStore.role !== 'counselor'" index="life">
          <template #title>
            <el-icon><DishDot /></el-icon>
            <span>校园生活</span>
          </template>
          <el-menu-item v-if="userStore.role === 'student'" index="/life/card">校园卡充值</el-menu-item>
          <el-menu-item index="/life/lost-found">失物招领</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.role !== 'admin' && userStore.role !== 'counselor'" index="club">
          <template #title>
            <el-icon><TrophyBase /></el-icon>
            <span>社团活动</span>
          </template>
          <el-menu-item index="/club/list">社团列表</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.role !== 'admin' && userStore.role !== 'counselor'" index="activity">
          <template #title>
            <el-icon><Calendar /></el-icon>
            <span>活动</span>
          </template>
          <el-menu-item index="/activity/center">活动广场</el-menu-item>
          <el-menu-item index="/activity/my">我的活动</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.role !== 'admin'" index="growth">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>成长档案</span>
          </template>
          <el-menu-item v-if="userStore.role==='student'" index="/growth/profile">我的档案</el-menu-item>
          <el-menu-item index="/growth/checkin">签到打卡</el-menu-item>
        </el-sub-menu>

        <el-sub-menu v-if="userStore.role !== 'admin'" index="message">
          <template #title>
            <el-icon><Message /></el-icon>
            <span>师生沟通</span>
          </template>
          <el-menu-item index="/message/chat">即时消息</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/ai/chat">
          <el-icon><ChatDotRound /></el-icon>
          <template #title>
            <span>AI 助手</span>
          </template>
        </el-menu-item>

        <el-sub-menu v-if="userStore.role === 'admin'" index="manage">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/manage/users?role=student">学生管理</el-menu-item>
          <el-menu-item index="/manage/users?role=teacher">教师管理</el-menu-item>
          <el-menu-item index="/manage/users?role=counselor">辅导员管理</el-menu-item>
          <el-menu-item index="/manage/users?role=admin">管理员管理</el-menu-item>
          <el-menu-item index="/manage/courses">课程管理</el-menu-item>
          <el-menu-item index="/manage/semesters">学期管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
const route = useRoute()
const userStore = useUserStore()
const appStore = useAppStore()
</script>

<style scoped>
.sidebar-container {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.sidebar-logo {
  height: 60px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  padding: 0 16px;
  position: relative;
  overflow: hidden;
}

.sidebar-logo::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 20%;
  right: 20%;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.3), transparent);
}

.logo-icon {
  width: 34px;
  height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.2), rgba(139, 92, 246, 0.2));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  position: relative;
}

.logo-icon::before {
  content: '';
  position: absolute;
  inset: -1px;
  border-radius: 11px;
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.4), rgba(139, 92, 246, 0.4));
  z-index: -1;
}

.logo-emoji {
  font-size: 18px;
  line-height: 1;
}

.logo-text-wrapper {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.logo-title {
  font-size: 16px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 1px;
  line-height: 1.3;
}

.logo-subtitle {
  font-size: 10px;
  color: rgba(255, 255, 255, 0.35);
  letter-spacing: 2px;
  text-transform: uppercase;
  line-height: 1;
}

.logo-text-enter-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}

.logo-text-leave-active {
  transition: opacity 0.1s ease, transform 0.1s ease;
}

.logo-text-enter-from,
.logo-text-leave-to {
  opacity: 0;
  transform: translateX(-8px);
}

.sidebar-menu-wrapper {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.sidebar-menu-wrapper::-webkit-scrollbar {
  width: 3px;
}

.sidebar-menu-wrapper::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-menu-wrapper::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 2px;
}

:deep(.el-menu) {
  border-right: none !important;
  background: transparent !important;
}

:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}

:deep(.el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.35) !important;
  font-size: 11px !important;
  font-weight: 700 !important;
  letter-spacing: 1.5px !important;
  text-transform: uppercase !important;
  height: 40px !important;
  line-height: 40px !important;
  margin: 4px 12px !important;
  padding: 0 12px !important;
  border-radius: 6px !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative !important;
}

:deep(.el-sub-menu__title::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 2px;
  height: 12px;
  border-radius: 1px;
  background: rgba(255, 255, 255, 0.08);
  transition: all 0.2s ease;
}

:deep(.el-sub-menu__title:hover) {
  color: rgba(255, 255, 255, 0.7) !important;
  background: rgba(255, 255, 255, 0.04) !important;
}

:deep(.el-sub-menu__title:hover::before) {
  background: rgba(139, 92, 246, 0.5);
  height: 16px;
}

:deep(.el-sub-menu__title .el-icon) {
  color: rgba(255, 255, 255, 0.4) !important;
  font-size: 15px !important;
}

:deep(.el-sub-menu__title:hover .el-icon) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.6) !important;
  font-size: 13.5px !important;
  font-weight: 500 !important;
  height: 40px !important;
  line-height: 40px !important;
  margin: 2px 12px !important;
  padding: 0 12px !important;
  border-radius: 8px !important;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1) !important;
  position: relative !important;
}

:deep(.el-menu-item .el-icon) {
  color: rgba(255, 255, 255, 0.4) !important;
  font-size: 16px !important;
  transition: all 0.2s ease !important;
}

:deep(.el-menu-item:hover) {
  color: #ffffff !important;
  background: rgba(255, 255, 255, 0.06) !important;
}

:deep(.el-menu-item:hover .el-icon) {
  color: rgba(255, 255, 255, 0.8) !important;
  transform: scale(1.1);
}

:deep(.el-menu-item.is-active) {
  color: #ffffff !important;
  font-weight: 600 !important;
  background: linear-gradient(90deg, rgba(99, 102, 241, 0.15), rgba(139, 92, 246, 0.08)) !important;
  padding-left: 12px !important;
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 0 3px 3px 0;
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  box-shadow: 0 0 8px rgba(99, 102, 241, 0.4);
}

:deep(.el-menu-item.is-active .el-icon) {
  color: #a78bfa !important;
}

:deep(.el-sub-menu .el-menu-item) {
  padding-left: 44px !important;
}

:deep(.el-sub-menu .el-menu-item.is-active) {
  padding-left: 44px !important;
}

:deep(.el-menu--collapse) .el-sub-menu__title {
  padding: 0 !important;
  justify-content: center !important;
  margin: 4px 8px !important;
  width: auto !important;
}

:deep(.el-menu--collapse) .el-sub-menu__title::before {
  display: none;
}

:deep(.el-menu--collapse) .el-menu-item {
  padding: 0 !important;
  justify-content: center !important;
  margin: 2px 8px !important;
  width: auto !important;
}

:deep(.el-menu--collapse) .el-menu-item.is-active::before {
  left: -4px;
  height: 16px;
  width: 2px;
}

:deep(.el-menu--collapse) .el-menu-item:hover .el-icon {
  transform: scale(1.15);
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title::before) {
  background: linear-gradient(180deg, #6366f1, #8b5cf6);
  height: 20px;
  opacity: 0.6;
}

:deep(.el-sub-menu.is-opened > .el-sub-menu__title .el-icon) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-menu--collapse .el-tooltip__trigger) {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}
</style>
<template>
  <div class="h-full flex flex-col">
    <div class="h-[60px] flex items-center justify-center border-b border-white/10 flex-shrink-0">
      <span v-if="!appStore.sidebarCollapsed" class="text-white text-lg font-bold tracking-tight animate__animated animate__fadeIn">
        智能校园
      </span>
      <span v-else class="text-white/60 text-sm font-bold">🏫</span>
    </div>
    <div class="flex-1 overflow-y-auto py-2">
      <el-menu
        :default-active="route.path"
        router
        :collapse="appStore.sidebarCollapsed"
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><HomeFilled /></el-icon>
          <span>首页</span>
        </el-menu-item>

        <el-sub-menu index="edu">
          <template #title>
            <el-icon><Reading /></el-icon>
            <span>教务学习</span>
          </template>
          <el-menu-item index="/edu/courses">课程列表</el-menu-item>
          <el-menu-item index="/edu/schedule">我的课表</el-menu-item>
          <el-menu-item v-if="userStore.role === 'student'" index="/edu/selection">在线选课</el-menu-item>
          <el-menu-item index="/edu/grades">成绩查询</el-menu-item>
          <el-menu-item v-if="userStore.role==='teacher' || userStore.role==='admin'" index="/edu/grade-entry">成绩录入</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="admin">
          <template #title>
            <el-icon><OfficeBuilding /></el-icon>
            <span>行政服务</span>
          </template>
          <el-menu-item index="/admin/notifications">通知公告</el-menu-item>
          <el-menu-item v-if="userStore.role === 'student'" index="/admin/leave">请假申请</el-menu-item>
          <el-menu-item v-if="userStore.role === 'teacher'" index="/admin/leave-approval">请假审批</el-menu-item>
          <el-menu-item index="/admin/guides">办事指南</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="life">
          <template #title>
            <el-icon><DishDot /></el-icon>
            <span>校园生活</span>
          </template>
          <el-menu-item index="/life/canteen">食堂点评</el-menu-item>
          <el-menu-item v-if="userStore.role === 'student'" index="/life/card">校园卡充值</el-menu-item>
          <el-menu-item index="/life/lost-found">失物招领</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="club">
          <template #title>
            <el-icon><TrophyBase /></el-icon>
            <span>社团活动</span>
          </template>
          <el-menu-item index="/club/list">社团列表</el-menu-item>
          <el-menu-item index="/club/activity">活动中心</el-menu-item>
          <el-menu-item index="/club/venue">场地预约</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="growth">
          <template #title>
            <el-icon><TrendCharts /></el-icon>
            <span>成长档案</span>
          </template>
          <el-menu-item v-if="userStore.role==='student'" index="/growth/profile">我的档案</el-menu-item>
          <el-menu-item index="/growth/checkin">签到打卡</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="message">
          <template #title>
            <el-icon><Message /></el-icon>
            <span>师生沟通</span>
          </template>
          <el-menu-item index="/message/chat">即时消息</el-menu-item>
          <el-menu-item v-if="userStore.role !== 'student'" index="/message/announcement">公告推送</el-menu-item>
        </el-sub-menu>

        <el-menu-item index="/ai/chat">
          <el-icon><ChatDotRound /></el-icon>
          <span>AI 助手</span>
        </el-menu-item>

        <el-sub-menu v-if="userStore.role === 'admin'" index="manage">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/manage/users">用户管理</el-menu-item>
          <el-menu-item index="/manage/courses">课程管理</el-menu-item>
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
:deep(.el-sub-menu__title) {
  color: #6B7280 !important;
  font-size: 13px !important;
  font-weight: 600 !important;
  letter-spacing: 0.5px;
  height: 44px !important;
  line-height: 44px !important;
  margin: 2px 8px !important;
  border-radius: 8px !important;
  transition: all 0.15s ease !important;
}
:deep(.el-sub-menu__title:hover) {
  color: #FFFFFF !important;
  background: rgba(255,255,255,0.06) !important;
}
:deep(.el-menu-item) {
  color: #9CA3AF !important;
  font-size: 14px !important;
  height: 44px !important;
  line-height: 44px !important;
  margin: 2px 8px !important;
  border-radius: 8px !important;
  transition: all 0.15s ease !important;
}
:deep(.el-menu-item:hover) {
  color: #FFFFFF !important;
  background: rgba(255,255,255,0.08) !important;
}
:deep(.el-menu-item.is-active) {
  color: #FFFFFF !important;
  background: rgba(255,255,255,0.12) !important;
  border-left: 3px solid #FFFFFF !important;
  padding-left: 17px !important;
}
:deep(.el-menu) {
  border-right: none !important;
  background: transparent !important;
}
:deep(.el-sub-menu .el-menu) {
  background: transparent !important;
}
:deep(.el-sub-menu .el-menu-item) {
  padding-left: 52px !important;
}
:deep(.el-sub-menu .el-menu-item.is-active) {
  padding-left: 49px !important;
}
:deep(.el-menu--collapse) .el-sub-menu__title {
  padding: 0 !important;
  justify-content: center;
}
:deep(.el-menu--collapse) .el-menu-item {
  padding: 0 !important;
  justify-content: center;
}
</style>

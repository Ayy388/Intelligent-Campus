<template>
  <div class="sidebar">
    <div class="logo">🏫 智能校园</div>
    <el-menu :default-active="route.path" router background-color="#304156" text-color="#bfcbd9"
      active-text-color="#409eff" :collapse="appStore.sidebarCollapsed">
      <el-menu-item index="/dashboard"><el-icon><HomeFilled /></el-icon><span>首页</span></el-menu-item>
      <el-sub-menu index="edu">
        <template #title><el-icon><Reading /></el-icon><span>教务学习</span></template>
        <el-menu-item index="/edu/courses">课程列表</el-menu-item>
        <el-menu-item index="/edu/selection" v-if="userStore.role==='student'">在线选课</el-menu-item>
        <el-menu-item index="/edu/grades">成绩查询</el-menu-item>
      </el-sub-menu>
      <el-sub-menu index="admin">
        <template #title><el-icon><OfficeBuilding /></el-icon><span>行政服务</span></template>
        <el-menu-item index="/admin/notifications">通知公告</el-menu-item>
        <el-menu-item index="/admin/leave" v-if="userStore.role==='student'">请假申请</el-menu-item>
        <el-menu-item index="/admin/leave-approval" v-if="userStore.role==='teacher'">请假审批</el-menu-item>
        <el-menu-item index="/admin/guides">办事指南</el-menu-item>
      </el-sub-menu>
      <el-menu-item index="/ai/chat"><el-icon><ChatDotRound /></el-icon><span>AI助手</span></el-menu-item>
      <el-sub-menu index="manage" v-if="userStore.role==='admin'">
        <template #title><el-icon><Setting /></el-icon><span>系统管理</span></template>
        <el-menu-item index="/manage/users">用户管理</el-menu-item>
        <el-menu-item index="/manage/courses">课程管理</el-menu-item>
      </el-sub-menu>
    </el-menu>
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
.sidebar { overflow-y: auto; }
.logo { height: 60px; line-height: 60px; text-align: center; color: #fff; font-size: 18px; font-weight: bold; }
</style>

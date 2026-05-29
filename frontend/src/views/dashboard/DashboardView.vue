<template>
  <div>
    <h3>欢迎回来，{{ userStore.userInfo?.realName }}</h3>
    <el-row :gutter="20" style="margin-top:20px">
      <el-col :span="8"><el-card><el-statistic title="我的课程" :value="0" /></el-card></el-col>
      <el-col :span="8"><el-card><el-statistic title="未读通知" :value="0" /></el-card></el-col>
      <el-col :span="8"><el-card><el-statistic title="待审批" :value="0" /></el-card></el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
</script>

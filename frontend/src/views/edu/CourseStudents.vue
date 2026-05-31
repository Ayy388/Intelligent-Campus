<template>
  <div class="flex flex-col items-center justify-center py-20">
    <el-icon class="text-6xl text-mist mb-4"><InfoFilled /></el-icon>
    <div class="text-lg text-ink font-medium mb-2">页面已整合</div>
    <div class="text-sm text-mist mb-6">
      "课程学生名单"已合并至"我的教学"页面，您可以在那里查看课程、班级和学生信息
    </div>
    <el-button type="primary" @click="$router.push('/edu/teaching')">前往我的教学</el-button>
  </div>
</template>

<script setup lang="ts">
import { InfoFilled } from '@element-plus/icons-vue'
</script>
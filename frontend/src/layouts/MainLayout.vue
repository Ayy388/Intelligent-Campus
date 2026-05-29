<template>
  <el-container class="layout">
    <el-aside :width="appStore.sidebarCollapsed ? '64px' : '220px'" class="aside">
      <AppSidebar />
    </el-aside>
    <el-container>
      <el-header class="header"><AppHeader /></el-header>
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAppStore } from '@/store/app'
const appStore = useAppStore()
</script>

<style scoped>
.layout { height: 100%; }
.aside { background: #304156; transition: width 0.3s; overflow: hidden; }
.header { background: #fff; border-bottom: 1px solid #e6e6e6; padding: 0 20px; height: 60px; }
.main { padding: 20px; background: #f0f2f5; }
</style>

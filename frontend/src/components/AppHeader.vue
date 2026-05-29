<template>
  <div class="header-content">
    <div class="left">
      <el-icon class="collapse-btn" @click="appStore.toggleSidebar()"><Fold /></el-icon>
    </div>
    <div class="right">
      <el-dropdown>
        <span class="user-info">{{ userStore.userInfo?.realName || '用户' }} <el-icon><ArrowDown /></el-icon></span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { useAppStore } from '@/store/app'
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()
function logout() { userStore.logout(); router.push('/login') }
</script>

<style scoped>
.header-content { display: flex; justify-content: space-between; align-items: center; height: 100%; }
.collapse-btn { font-size: 20px; cursor: pointer; }
.user-info { cursor: pointer; display: flex; align-items: center; gap: 4px; }
</style>

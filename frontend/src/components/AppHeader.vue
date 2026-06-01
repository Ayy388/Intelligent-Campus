<template>
  <div class="flex items-center justify-between w-full h-full">
    <div class="flex items-center gap-3">
      <button
        class="w-8 h-8 flex items-center justify-center rounded-lg text-ash hover:text-ink hover:bg-wash transition-all duration-200"
        @click="appStore.toggleSidebar()"
      >
        <el-icon :size="18"><Fold /></el-icon>
      </button>
    </div>
    <div class="flex items-center gap-4">
      <el-dropdown trigger="click">
        <div class="flex items-center gap-2 cursor-pointer group">
          <div class="w-8 h-8 rounded-full overflow-hidden flex items-center justify-center text-white text-xs font-semibold transition-shadow duration-200 group-hover:shadow-md">
            <img v-if="userStore.userInfo?.avatar" :src="userStore.userInfo?.avatar" class="w-full h-full object-cover" />
            <span v-else class="w-full h-full bg-ink flex items-center justify-center">{{ (userStore.userInfo?.realName || 'U')[0] }}</span>
          </div>
          <span class="text-sm text-ink font-medium hidden sm:block">
            {{ userStore.userInfo?.realName || '用户' }}
          </span>
          <el-icon class="text-mist text-xs"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item>
              <div class="text-ash text-xs">{{ userStore.role === 'admin' ? '管理员' : userStore.role === 'teacher' ? '教师' : userStore.role === 'counselor' ? '辅导员' : '学生' }}</div>
            </el-dropdown-item>
            <el-dropdown-item @click="router.push('/profile')">
              <span class="text-ink">个人信息</span>
            </el-dropdown-item>
            <el-dropdown-item divided @click="logout">
              <span class="text-ink">退出登录</span>
            </el-dropdown-item>
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

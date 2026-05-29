<template>
  <div class="animate__animated animate__fadeInUp">
    <div class="mb-6">
      <h2 class="text-2xl font-bold text-ink tracking-tight">
        欢迎回来，{{ userStore.userInfo?.realName }}
      </h2>
      <p class="text-ash text-sm mt-1">
        {{ currentDate }}
      </p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-3 gap-5">
      <div
        v-for="(stat, i) in stats"
        :key="stat.label"
        class="bg-white rounded-xl border border-soft p-5 hover:shadow-sm transition-shadow duration-200"
        :style="{ animationDelay: `${0.1 * i}s` }"
        :class="`animate__animated animate__fadeInUp`"
      >
        <div class="text-ash text-sm font-medium mb-2">{{ stat.label }}</div>
        <div class="text-3xl font-bold text-ink tracking-tight">{{ stat.value }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-5 mt-6">
      <div class="bg-white rounded-xl border border-soft p-5">
        <div class="text-sm font-semibold text-ink mb-4">快捷入口</div>
        <div class="grid grid-cols-2 gap-3">
          <router-link
            v-for="link in quickLinks"
            :key="link.path"
            :to="link.path"
            class="flex items-center gap-3 p-3 rounded-lg bg-wash hover:bg-soft transition-colors duration-200 text-ink no-underline text-sm"
          >
            <el-icon :size="16" class="text-ash"><component :is="link.icon" /></el-icon>
            {{ link.label }}
          </router-link>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-soft p-5">
        <div class="text-sm font-semibold text-ink mb-3">最近通知</div>
        <div class="text-ash text-sm">暂无新通知</div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import { Reading, OfficeBuilding, ChatDotRound, Setting } from '@element-plus/icons-vue'

const userStore = useUserStore()

const currentDate = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · 星期${weekdays[d.getDay()]}`
})

const stats = [
  { label: '我的课程', value: 8 },
  { label: '未读通知', value: 3 },
  { label: '待审批', value: userStore.role === 'teacher' ? 2 : 0 },
]

const quickLinks = [
  { label: '课程列表', path: '/edu/courses', icon: Reading },
  { label: '通知公告', path: '/admin/notifications', icon: OfficeBuilding },
  { label: 'AI 助手', path: '/ai/chat', icon: ChatDotRound },
  { label: '办事指南', path: '/admin/guides', icon: Setting },
]
</script>

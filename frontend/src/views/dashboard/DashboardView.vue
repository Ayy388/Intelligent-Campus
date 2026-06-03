<template>
  <div>
    <div class="animate__animated animate__fadeInDown">
      <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-gray-900 via-gray-800 to-gray-700 p-6 sm:p-8 mb-6">
        <div class="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2"></div>
        <div class="absolute bottom-0 left-1/3 w-48 h-48 bg-white/[0.03] rounded-full translate-y-1/2"></div>
        <div class="relative flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
          <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-white tracking-tight">
              {{ greeting }}，<span class="text-transparent bg-clip-text bg-gradient-to-r from-amber-200 to-yellow-300">{{ userStore.userInfo?.realName || '用户' }}</span>
            </h1>
            <p class="text-gray-400 text-sm mt-2 tracking-wide flex items-center gap-2">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                <line x1="3" y1="10" x2="21" y2="10"/>
              </svg>
              {{ currentDate }}
            </p>
          </div>
          <div :class="roleBadgeClass" class="self-start px-4 py-1.5 rounded-full text-xs font-semibold shadow-lg backdrop-blur-sm">
            {{ roleLabel }}
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-6">
      <div
        v-for="(stat, i) in stats"
        :key="stat.label"
        class="relative rounded-xl p-5 overflow-hidden cursor-default transition-all duration-300 hover:-translate-y-1 hover:shadow-xl"
        :class="`animate__animated animate__fadeInUp`"
        :style="{ animationDelay: `${0.08 * i}s`, background: stat.gradient }"
      >
        <div class="absolute top-0 right-0 w-24 h-24 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/3"></div>
        <div class="relative flex items-start justify-between">
          <div class="flex-1">
            <div class="text-white/70 text-xs font-medium tracking-wide mb-1">{{ stat.label }}</div>
            <div class="text-3xl sm:text-4xl font-bold text-white tracking-tight">{{ stat.value }}</div>
          </div>
          <div class="w-10 h-10 rounded-lg bg-white/20 flex items-center justify-center backdrop-blur-sm shrink-0" v-html="stat.icon">
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-5 gap-4 mb-4">
      <div
        class="lg:col-span-2 bg-white rounded-xl p-5 shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-md"
        :class="`animate__animated animate__fadeInUp`"
        style="animation-delay: 0.32s"
      >
        <div class="flex items-center gap-2 mb-5">
          <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
              <rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
          </div>
          <h3 class="text-sm font-bold text-gray-800">快捷入口</h3>
        </div>
        <div class="grid grid-cols-2 gap-2.5">
          <router-link
            v-for="link in quickLinks"
            :key="link.path"
            :to="link.path"
            class="group flex items-center gap-3 px-3.5 py-3 rounded-xl text-gray-500 hover:text-gray-800 hover:bg-gray-50 transition-all duration-200 no-underline text-sm border border-transparent hover:border-gray-100"
          >
            <span class="w-9 h-9 rounded-lg flex items-center justify-center transition-all duration-200 group-hover:scale-110 group-hover:shadow-md" :class="link.bg">
              <span v-html="link.icon" class="w-5 h-5 flex items-center justify-center"></span>
            </span>
            <span class="font-medium text-gray-700 group-hover:text-gray-900">{{ link.label }}</span>
          </router-link>
        </div>
      </div>

      <div
        class="lg:col-span-3 bg-white rounded-xl p-5 shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-md"
        :class="`animate__animated animate__fadeInUp`"
        style="animation-delay: 0.4s"
      >
        <div class="flex items-center justify-between mb-5">
          <div class="flex items-center gap-2">
            <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center">
              <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
              </svg>
            </div>
            <h3 class="text-sm font-bold text-gray-800">最近通知</h3>
          </div>
          <router-link to="/admin/notifications" class="text-xs text-gray-400 hover:text-gray-600 transition-colors no-underline flex items-center gap-1">
            查看全部
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </router-link>
        </div>
        <div v-if="notifications.length > 0" class="divide-y divide-gray-50">
          <router-link
            v-for="n in notifications"
            :key="n.id"
            :to="`/admin/notifications`"
            class="flex items-start justify-between py-3.5 first:pt-0 last:pb-0 group cursor-pointer no-underline"
          >
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span v-if="n.read === false" class="w-2 h-2 rounded-full bg-blue-500 shrink-0"></span>
                <span class="text-sm text-gray-800 font-medium truncate group-hover:text-indigo-600 transition-colors">{{ n.title }}</span>
                <span v-if="n.isUrgent===1" class="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-red-50 text-red-500 border border-red-100">紧急</span>
              </div>
              <p class="text-xs text-gray-400 mt-1.5 flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/>
                </svg>
                {{ n.createTime?.substring(0, 16) || '' }}
              </p>
            </div>
            <svg class="w-4 h-4 text-gray-300 group-hover:text-gray-500 transition-colors shrink-0 ml-2 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </router-link>
        </div>
        <div v-else class="flex flex-col items-center justify-center py-10 text-gray-400">
          <svg class="w-10 h-10 mb-3 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          <span class="text-sm">暂无新通知</span>
        </div>
      </div>
    </div>

    <div
      class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-md"
      :class="`animate__animated animate__fadeInUp`"
      style="animation-delay: 0.48s"
    >
      <div class="flex items-center justify-between mb-5">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-amber-500 to-orange-600 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/>
              <rect x="9" y="3" width="6" height="4" rx="1"/>
              <line x1="9" y1="12" x2="15" y2="12"/>
              <line x1="9" y1="16" x2="13" y2="16"/>
            </svg>
          </div>
          <h3 class="text-sm font-bold text-gray-800">待办中心</h3>
          <span v-if="pendingTodos.length" class="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-600 font-medium border border-amber-100">
            {{ pendingTodos.length }} 项待处理
          </span>
        </div>
        <div class="flex items-center gap-2">
          <el-button size="small" @click="showQuickAdd = true" plain class="quick-add-btn">+ 快速新建</el-button>
          <router-link to="/todo" class="text-xs text-gray-400 hover:text-gray-600 transition-colors no-underline flex items-center gap-1">
            查看全部
            <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </router-link>
        </div>
      </div>
      <div v-if="pendingTodos.length > 0" class="divide-y divide-gray-50">
        <div
          v-for="t in pendingTodos.slice(0, 5)"
          :key="t.id"
          class="flex items-start gap-3 py-3.5 first:pt-0 last:pb-0 group"
        >
          <el-checkbox
            :model-value="t.completed === 1"
            @change="toggleTodo(t)"
            class="mt-0.5"
          />
          <div class="min-w-0 flex-1">
            <div class="flex items-center gap-2">
              <span class="text-sm text-gray-800" :class="t.completed === 1 ? 'line-through text-gray-300' : ''">{{ t.title }}</span>
              <span
                v-if="t.priority === 2"
                class="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-red-50 text-red-500 border border-red-100"
              >高</span>
              <span
                v-else-if="t.priority === 0"
                class="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-gray-50 text-gray-400 border border-gray-100"
              >低</span>
            </div>
            <p v-if="t.dueDate" class="text-xs text-gray-400 mt-0.5">
              <span v-if="isOverdue(t.dueDate)" class="text-red-400">已逾期</span>
              <span v-else>截止 {{ t.dueDate }}</span>
            </p>
          </div>
        </div>
      </div>
      <div v-else class="flex flex-col items-center justify-center py-8 text-gray-400">
        <svg class="w-9 h-9 mb-2 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/>
          <rect x="9" y="3" width="6" height="4" rx="1"/>
          <line x1="9" y1="12" x2="15" y2="12"/>
          <line x1="9" y1="16" x2="13" y2="16"/>
        </svg>
        <span class="text-sm">暂无待办事项</span>
      </div>
      <div v-if="showQuickAdd" class="mt-3 pt-3 border-t border-gray-100">
        <div class="flex gap-2">
          <el-input
            v-model="quickTitle"
            placeholder="输入待办内容，按回车保存"
            size="small"
            maxlength="200"
            @keyup.enter="quickCreateTodo"
          />
          <el-button size="small" type="primary" @click="quickCreateTodo" :loading="saving">添加</el-button>
        </div>
      </div>
    </div>

          <!-- teacher/counselor: today schedule panel -->
    <div v-if="userStore.role === 'teacher' || userStore.role === 'counselor'"
      class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-md mt-6"
      :class="`animate__animated animate__fadeInUp`"
      style="animation-delay: 0.56s"
    >
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <h3 class="text-sm font-bold text-gray-800">今日课表</h3>
        </div>
        <router-link to="/edu/schedule" class="text-xs text-gray-400 hover:text-gray-600 transition-colors no-underline flex items-center gap-1">
          查看全部
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </router-link>
      </div>
      <div v-if="todayCourses.length > 0" class="space-y-3">
        <div
          v-for="c in todayCourses"
          :key="c.id"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 transition-colors"
        >
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shrink-0">
            <span class="text-white text-xs font-bold">{{ c.courseName?.substring(0, 2) }}</span>
          </div>
          <div class="flex-1 min-w-0">
            <div class="text-sm font-medium text-gray-800 truncate">{{ c.courseName }}</div>
            <p class="text-xs text-gray-400 mt-0.5">第{{ c.timeSlot }}大节 · {{ c.weeks === 'odd' ? '单周' : c.weeks === 'even' ? '双周' : '全周' }}</p>
          </div>
          <el-button size="small" type="primary" plain @click="goCheckin(c.id)">发起签到</el-button>
        </div>
      </div>
      <div v-else class="flex flex-col items-center justify-center py-8 text-gray-400">
        <svg class="w-9 h-9 mb-2 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
        </svg>
        <span class="text-sm">今日无课程安排</span>
      </div>
    </div>
    <!-- student/others: lost and found panel -->
    <div v-else
      class="bg-white rounded-xl p-5 shadow-sm border border-gray-100 transition-all duration-300 hover:shadow-md mt-6"
      :class="`animate__animated animate__fadeInUp`"
      style="animation-delay: 0.56s"
    >
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2">
          <div class="w-7 h-7 rounded-lg bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/><line x1="8" y1="11" x2="14" y2="11"/>
            </svg>
          </div>
          <h3 class="text-sm font-bold text-gray-800">失物招领</h3>
        </div>
        <router-link to="/life/lost-found" class="text-xs text-gray-400 hover:text-gray-600 transition-colors no-underline flex items-center gap-1">
          查看全部
          <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <polyline points="9 18 15 12 9 6"/>
          </svg>
        </router-link>
      </div>
      <div v-if="lostItems.length > 0" class="space-y-2">
        <div
          v-for="item in lostItems"
          :key="item.id"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg hover:bg-gray-50 transition-colors cursor-pointer"
          @click="goLostFound"
        >
          <div v-if="item.images" class="shrink-0">
            <el-image :src="item.images.split(',')[0]" class="w-10 h-10 rounded-lg object-cover" />
          </div>
          <div v-else class="w-10 h-10 rounded-lg bg-gray-100 flex items-center justify-center shrink-0 text-gray-300 text-xs">无图</div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2">
              <span class="shrink-0 text-[10px] font-medium px-1.5 py-0.5 rounded" :class="item.type===0?'bg-blue-50 text-blue-500':'bg-orange-50 text-orange-500'">{{ item.type===0?'寻物':'招领' }}</span>
              <span class="text-sm text-gray-700 truncate">{{ item.title }}</span>
              <el-tag v-if="item.status===1" size="small" type="info" class="!bg-gray-50 !text-gray-400 !border-gray-200" style="height:20px;line-height:18px">已解决</el-tag>
            </div>
            <p class="text-xs text-gray-400 mt-0.5 truncate">{{ item.location }}</p>
          </div>
        </div>
      </div>
      <div v-else class="flex flex-col items-center justify-center py-8 text-gray-400">
        <svg class="w-9 h-9 mb-2 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/>
        </svg>
        <span class="text-sm">暂无失物招领</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { getNotifications, getLeaves } from '@/api/admin'
import { getSchedule, getSemesters, getCourses, getTeacherCourses, getCourseStudents, getCourseClasses } from '@/api/edu'
import { getClubs } from '@/api/club'
import { getCheckIns } from '@/api/growth'
import { getUsers, getAllClasses } from '@/api/sys'
import { getTodos, updateTodo, createTodo } from '@/api/todo'
import { getUnreadNotificationCount } from '@/api/admin'
import { getUncheckedCheckInCount } from '@/api/growth'
import { getLostFound } from '@/api/life'

const router = useRouter()
const userStore = useUserStore()

const roleLabel = computed(() => {
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[userStore.role] || '用户'
})

const roleBadgeClass = computed(() => {
  const map: Record<string, string> = {
    admin: 'bg-gradient-to-r from-purple-600 to-pink-500 text-white',
    teacher: 'bg-gradient-to-r from-blue-600 to-cyan-500 text-white',
    student: 'bg-gradient-to-r from-emerald-500 to-teal-400 text-white',
  }
  return map[userStore.role] || 'bg-gradient-to-r from-gray-500 to-gray-400 text-white'
})

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 12) return '上午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const currentDate = computed(() => {
  const d = new Date()
  const weekdays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 · 星期${weekdays[d.getDay()]}`
})

const stats = reactive<{ label: string; value: number; icon: string; gradient: string }[]>([])

const quickLinks = computed(() => {
  const role = userStore.role
  if (role === 'student') {
    return [
      { label: '选课中心', path: '/edu/selection', bg: 'bg-gradient-to-br from-blue-500 to-cyan-500', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>` },
      { label: '我的课表', path: '/edu/schedule', bg: 'bg-gradient-to-br from-violet-500 to-purple-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>` },
      { label: '成绩查询', path: '/edu/grades', bg: 'bg-gradient-to-br from-emerald-500 to-teal-500', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M22 12h-4l-3 9L9 3l-3 9H2"/></svg>` },
      { label: 'AI 助手', path: '/ai/chat', bg: 'bg-gradient-to-br from-rose-500 to-pink-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>` },
      { label: '通知公告', path: '/admin/notifications', bg: 'bg-gradient-to-br from-amber-500 to-orange-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>` },
      { label: '签到打卡', path: '/growth/checkin', bg: 'bg-gradient-to-br from-sky-500 to-indigo-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>` },
    ]
  }
  if (role === 'teacher' || role === 'counselor') {
    return [
      { label: '我的教学', path: '/edu/teaching', bg: 'bg-gradient-to-br from-blue-500 to-cyan-500', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>` },
      { label: '签到管理', path: '/growth/checkin', bg: 'bg-gradient-to-br from-sky-500 to-indigo-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>` },
      { label: '通知公告', path: '/admin/notifications', bg: 'bg-gradient-to-br from-amber-500 to-orange-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>` },
      { label: 'AI 助手', path: '/ai/chat', bg: 'bg-gradient-to-br from-rose-500 to-pink-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>` },
    ]
  }
  return [
    { label: '用户管理', path: '/manage/users', bg: 'bg-gradient-to-br from-blue-500 to-cyan-500', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>` },
    { label: '课程管理', path: '/manage/courses', bg: 'bg-gradient-to-br from-violet-500 to-purple-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>` },
    { label: '通知公告', path: '/admin/notifications', bg: 'bg-gradient-to-br from-amber-500 to-orange-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>` },
    { label: '院系管理', path: '/admin/departments', bg: 'bg-gradient-to-br from-emerald-500 to-teal-500', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/></svg>` },
    { label: '社团审核', path: '/admin/club-approval', bg: 'bg-gradient-to-br from-rose-500 to-pink-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>` },
    { label: 'AI 助手', path: '/ai/chat', bg: 'bg-gradient-to-br from-sky-500 to-indigo-600', icon: `<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>` },
  ]
})

const notifications = ref<any[]>([])
const customTodos = ref<any[]>([])
const lostItems = ref<any[]>([])
const todayCourses = ref<any[]>([])

const pendingTodos = computed(() => customTodos.value.filter((t: any) => t.completed !== 1))

function isOverdue(dateStr: string) {
  return dateStr < new Date().toISOString().substring(0, 10)
}

async function fetchStats() {
  try {
    const role = userStore.role
    if (role === 'student') {
      let courseCount = 0
      try {
        const semRes = await getSemesters()
        const active = (semRes.data || []).find((s: any) => s.status === 1)
        if (active) {
          const schedRes = await getSchedule(active.xqjc)
          courseCount = schedRes.data?.length || 0
        }
      } catch {}
      const [unreadRes, uncheckedRes] = await Promise.all([
        getUnreadNotificationCount(), getUncheckedCheckInCount()
      ])
      const notiCount = (unreadRes.data || 0) + (uncheckedRes.data || 0)
      let clubCount = 0
      try { const clubRes = await getClubs(); clubCount = clubRes.data?.length || 0 } catch {}
      stats.length = 0
      stats.push({ label: '我的课程', value: courseCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>', gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)' })
      stats.push({ label: '待处理', value: notiCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>', gradient: 'linear-gradient(135deg, #f59e0b, #ef4444)' })
      stats.push({ label: '待办事项', value: customTodos.value.length, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>', gradient: 'linear-gradient(135deg, #10b981, #14b8a6)' })
      stats.push({ label: '社团参与', value: clubCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>', gradient: 'linear-gradient(135deg, #ec4899, #f43f5e)' })
    } else if (role === 'teacher' || role === 'counselor') {
      const coursesRes = await getTeacherCourses()
      const courseCount = coursesRes.data?.length || 0
      const coursesData = coursesRes.data || []
      const classIds = new Set()
      for (const c of coursesData) {
        try {
          const ccRes = await getCourseClasses(c.id)
          ;(ccRes.data || []).forEach((cc) => { if (cc.classId) classIds.add(cc.classId) })
        } catch {}
      }
      const classCount = classIds.size

      const studentIds = new Set()
      for (const c of coursesData) {
        try {
          const stuRes = await getCourseStudents(c.id)
          ;(stuRes.data || []).forEach((s) => { if (s.studentId) studentIds.add(s.studentId) })
        } catch {}
      }
      const studentCount = studentIds.size

      let checkinCount = 0
      try { const checkinRes = await getCheckIns({ page: 1, size: 1 }); checkinCount = checkinRes.data?.total || 0 } catch {}
      stats.length = 0
      stats.push({ label: '我的课程', value: courseCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/><path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/></svg>', gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)' })
      stats.push({ label: '我的班级', value: classCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/></svg>', gradient: 'linear-gradient(135deg, #f59e0b, #ef4444)' })
      stats.push({ label: '授课学生', value: studentCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>', gradient: 'linear-gradient(135deg, #10b981, #14b8a6)' })
      stats.push({ label: '本周签到', value: checkinCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="20 6 9 17 4 12"/></svg>', gradient: 'linear-gradient(135deg, #ec4899, #f43f5e)' })
    } else {
      const usersRes = await getUsers({ page: 1, size: 1 })
      const userCount = usersRes.data?.total || 0
      const coursesRes = await getCourses({ page: 1, size: 1 })
      const courseCount = coursesRes.data?.total || 0
      const notiRes = await getNotifications({ page: 1, size: 1 })
      const notiCount = notiRes.data?.total || 0
      let clubCount = 0
      try { const clubRes = await getClubs(); clubCount = clubRes.data?.length || 0 } catch {}
      stats.length = 0
      stats.push({ label: '用户总数', value: userCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>', gradient: 'linear-gradient(135deg, #6366f1, #8b5cf6)' })
      stats.push({ label: '课程总数', value: courseCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M12 2L2 7l10 5 10-5-10-5z"/><path d="M2 17l10 5 10-5"/><path d="M2 12l10 5 10-5"/></svg>', gradient: 'linear-gradient(135deg, #f59e0b, #ef4444)' })
      stats.push({ label: '通知总数', value: notiCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/><path d="M13.73 21a2 2 0 0 1-3.46 0"/></svg>', gradient: 'linear-gradient(135deg, #10b981, #14b8a6)' })
      stats.push({ label: '社团总数', value: clubCount, icon: '<svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>', gradient: 'linear-gradient(135deg, #ec4899, #f43f5e)' })
    }
  } catch (e) {
    console.error('获取统计数据失败', e)
  }
}

async function fetchNotifications() {
  try {
    const r = await getNotifications({ page: 1, size: 5 })
    notifications.value = r.data.records || []
  } catch {}
}

async function fetchTodos() {
  try {
    const res = await getTodos({ page: 1, size: 50, completed: 0 })
    customTodos.value = res.data.records || []
  } catch {}
}

async function fetchLostFound() {
  try {
    const r = await getLostFound({ page: 1, size: 5 })
    lostItems.value = r.data.records || []
  } catch {}
}

function goLostFound() { router.push('/life/lost-found') }

async function fetchTodaySchedule() {
  try {
    const now = new Date()
    const dayOfWeek = now.getDay() || 7 // Monday=1, Sunday=7
    const res = await getSchedule()
    const all = res.data || []
    // Filter by today's day-of-week using schedule JSON, enrich with timeSlot/weeks
    todayCourses.value = all.filter((c: any) => {
      if (!c.schedule) return false
      try {
        const sched = typeof c.schedule === 'string' ? JSON.parse(c.schedule) : c.schedule
        const items = Array.isArray(sched) ? sched : [sched]
        const match = items.find((s: any) => s.day === dayOfWeek)
        if (match) {
          c.timeSlot = match.timeSlot
          c.weeks = match.weeks || 'all'
          return true
        }
        return false
      } catch { return false }
    })
  } catch {}
}

function goCheckin(courseId: number) {
  router.push(`/growth/checkin?courseId=${courseId}`)
}

async function toggleTodo(t: any) {
  try {
    const newVal = t.completed === 1 ? 0 : 1
    await updateTodo(t.id, { completed: newVal })
    t.completed = newVal
    if (newVal === 1) ElMessage.success('已标记完成')
  } catch {}
}

const showQuickAdd = ref(false)
const quickTitle = ref('')
const saving = ref(false)

async function quickCreateTodo() {
  if (!quickTitle.value.trim()) {
    ElMessage.warning('请输入待办内容')
    return
  }
  saving.value = true
  try {
    await createTodo({ title: quickTitle.value.trim() })
    ElMessage.success('已创建')
    quickTitle.value = ''
    showQuickAdd.value = false
    await fetchTodos()
  } catch {} finally {
    saving.value = false
  }
}

onMounted(async () => {
  await fetchTodos()
  fetchStats()
  fetchNotifications()
  fetchLostFound()
  if (userStore.role === 'teacher' || userStore.role === 'counselor') {
    fetchTodaySchedule()
  }
})
</script>

<style scoped>
.quick-add-btn {
  height: 26px !important;
  padding: 0 10px !important;
  font-size: 11px !important;
  border-radius: 6px !important;
  border-color: #e5e7eb !important;
  color: #6b7280 !important;
  background: transparent !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}

.quick-add-btn:hover {
  color: #6366f1 !important;
  border-color: #c7d2fe !important;
  background: #eef2ff !important;
}
</style>
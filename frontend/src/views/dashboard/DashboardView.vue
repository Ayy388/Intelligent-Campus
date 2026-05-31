<template>
  <div>
    <div class="animate__animated animate__fadeInUp">
      <div class="flex items-start justify-between mb-6">
        <div>
          <h2 class="text-2xl font-bold text-ink tracking-tight">
            {{ greeting }}，{{ userStore.userInfo?.realName }}
          </h2>
          <p class="text-mist text-sm mt-1.5 tracking-wide">{{ currentDate }}</p>
        </div>
        <div class="hidden sm:flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-wash text-mist text-xs">
          <span class="w-1.5 h-1.5 rounded-full bg-emerald-400"></span>
          {{ roleLabel }}
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-6">
      <div
        v-for="(stat, i) in stats"
        :key="stat.label"
        class="bg-white rounded-xl p-5 transition-all duration-300"
        :style="{ animationDelay: `${0.08 * i}s` }"
        :class="`animate__animated animate__fadeInUp`"
      >
        <div class="flex items-center justify-between mb-1">
          <span class="text-mist text-xs font-medium tracking-wide uppercase">{{ stat.label }}</span>
          <span class="text-mist/40">
            <el-icon :size="14"><component :is="stat.icon" /></el-icon>
          </span>
        </div>
        <div class="text-3xl font-bold text-ink tracking-tight">{{ stat.value }}</div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-5 gap-4">
      <div
        class="bg-white rounded-xl p-5 lg:col-span-2 transition-shadow duration-300"
        :class="`animate__animated animate__fadeInUp`"
        style="animation-delay: 0.24s"
      >
        <h3 class="text-xs font-semibold text-mist uppercase tracking-widest mb-4">快捷入口</h3>
        <div class="grid grid-cols-2 gap-2">
          <router-link
            v-for="link in quickLinks"
            :key="link.path"
            :to="link.path"
            class="flex items-center gap-3 px-3.5 py-3 rounded-lg text-steel hover:text-ink hover:bg-wash transition-all duration-200 no-underline text-sm group"
          >
            <span class="w-8 h-8 rounded-lg bg-wash flex items-center justify-center text-ash group-hover:bg-soft group-hover:text-ink transition-all duration-200">
              <el-icon :size="15"><component :is="link.icon" /></el-icon>
            </span>
            <span class="font-medium">{{ link.label }}</span>
          </router-link>
        </div>
      </div>

      <div
        class="bg-white rounded-xl p-5 lg:col-span-3 transition-shadow duration-300"
        :class="`animate__animated animate__fadeInUp`"
        style="animation-delay: 0.32s"
      >
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-xs font-semibold text-mist uppercase tracking-widest">最近通知</h3>
          <router-link to="/admin/notifications" class="text-mist hover:text-ink text-xs transition-colors no-underline">查看全部</router-link>
        </div>
        <div v-if="notifications.length > 0" class="divide-y divide-wash">
          <div
            v-for="n in notifications"
            :key="n.id"
            class="flex items-start justify-between py-3 first:pt-0 last:pb-0 group"
          >
            <div class="min-w-0 flex-1">
              <div class="flex items-center gap-2">
                <span class="text-sm text-ink font-medium truncate">{{ n.title }}</span>
                <span v-if="n.isUrgent===1" class="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-ink/5 text-ink">紧急</span>
              </div>
              <p class="text-xs text-mist mt-1 truncate">{{ n.createTime?.substring(0, 16) || '' }}</p>
            </div>
          </div>
        </div>
        <div v-else class="text-center text-mist text-sm py-8">暂无新通知</div>
      </div>
    </div>

    <div
      v-if="userStore.role === 'student'"
      class="bg-white rounded-xl p-5 mt-4 transition-shadow duration-300"
      :class="`animate__animated animate__fadeInUp`"
      style="animation-delay: 0.4s"
    >
      <h3 class="text-xs font-semibold text-mist uppercase tracking-widest mb-4">课程时间线</h3>
      <div v-if="courses.length > 0" class="relative">
        <div class="absolute left-[7px] top-2 bottom-2 w-px bg-soft"></div>
        <div
          v-for="(c, i) in courses"
          :key="c.id"
          class="relative pl-7 pb-5 last:pb-0"
        >
          <div class="absolute left-0 top-1.5 w-[15px] h-[15px] rounded-full border-2 border-ink bg-white flex items-center justify-center">
            <div class="w-[5px] h-[5px] rounded-full bg-ink"></div>
          </div>
          <div class="text-sm font-semibold text-ink">{{ c.courseName || c.name }}</div>
          <div class="text-xs text-mist mt-0.5">{{ c.semester || c.weeks || '' }}{{ c.teacherName ? ' · ' + c.teacherName : '' }}</div>
        </div>
      </div>
      <div v-else class="text-center text-mist text-sm py-8">暂无课程数据</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/store/user'
import { getNotifications, getLeaves } from '@/api/admin'
import { getSelections, getCourses } from '@/api/edu'
import { getUsers } from '@/api/sys'
import { Reading, OfficeBuilding, ChatDotRound, Setting, User, Document, Bell, List } from '@element-plus/icons-vue'

const userStore = useUserStore()

const roleLabel = computed(() => {
  const map: Record<string, string> = { admin: '管理员', teacher: '教师', student: '学生' }
  return map[userStore.role] || '用户'
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

const stats = reactive<{ label: string; value: number; icon: any }[]>([])

const quickLinks = computed(() => {
  const links: { label: string; path: string; icon: any }[] = [
    { label: '课程列表', path: '/edu/courses', icon: Reading },
    { label: '通知公告', path: '/admin/notifications', icon: OfficeBuilding },
    { label: 'AI 助手', path: '/ai/chat', icon: ChatDotRound },
    { label: '办事指南', path: '/admin/guides', icon: Setting },
  ]
  if (userStore.role === 'student') {
    links.splice(2, 0, { label: '选课中心', path: '/edu/selection', icon: List })
  }
  if (userStore.role === 'teacher') {
    links.splice(2, 0, { label: '请假审批', path: '/admin/leave-approval', icon: Document })
  }
  return links
})

const notifications = ref<any[]>([])
const courses = ref<any[]>([])

async function fetchStats() {
  try {
    const role = userStore.role
    if (role === 'student') {
      const selectionsRes = await getSelections()
      const courseCount = selectionsRes.data?.length || 0
      const notiRes = await getNotifications({ page: 1, size: 1 })
      const notiCount = notiRes.data?.total || 0
      stats.length = 0
      stats.push({ label: '我的课程', value: courseCount, icon: Reading })
      stats.push({ label: '通知总数', value: notiCount, icon: Bell })
      stats.push({ label: '待办事项', value: 0, icon: List })
    } else if (role === 'teacher') {
      const coursesRes = await getCourses({ page: 1, size: 100 })
      const courseCount = coursesRes.data?.records?.length || coursesRes.data?.total || 0
      const notiRes = await getNotifications({ page: 1, size: 1 })
      const notiCount = notiRes.data?.total || 0
      const leavesRes = await getLeaves({ page: 1, size: 1 })
      const leaveCount = leavesRes.data?.total || 0
      stats.length = 0
      stats.push({ label: '我的课程', value: courseCount, icon: Reading })
      stats.push({ label: '通知总数', value: notiCount, icon: Bell })
      stats.push({ label: '待审批请假', value: leaveCount, icon: Document })
    } else {
      const usersRes = await getUsers({ page: 1, size: 1 })
      const userCount = usersRes.data?.total || 0
      const coursesRes = await getCourses({ page: 1, size: 1 })
      const courseCount = coursesRes.data?.total || 0
      const notiRes = await getNotifications({ page: 1, size: 1 })
      const notiCount = notiRes.data?.total || 0
      stats.length = 0
      stats.push({ label: '用户总数', value: userCount, icon: User })
      stats.push({ label: '课程总数', value: courseCount, icon: Reading })
      stats.push({ label: '通知总数', value: notiCount, icon: Bell })
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

async function fetchCourses() {
  if (userStore.role !== 'student') return
  try {
    const r = await getCourses({ page: 1, size: 10 })
    courses.value = r.data.records || r.data || []
  } catch {}
}

onMounted(() => {
  fetchStats()
  fetchNotifications()
  fetchCourses()
})
</script>
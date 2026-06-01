<template>
  <div class="min-h-screen pb-8">
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-ink tracking-tight">成绩查询</h1>
      <p class="text-sm text-mist mt-1">查看全部课程成绩与学业分析</p>
    </div>

    <div v-if="loading" class="flex flex-col items-center justify-center py-24">
      <div class="relative w-16 h-16">
        <div class="absolute inset-0 rounded-full border-4 border-soft"></div>
        <div class="absolute inset-0 rounded-full border-4 border-transparent border-t-steel animate-spin"></div>
      </div>
      <p class="text-sm text-mist mt-4">正在加载成绩数据...</p>
    </div>

    <template v-else-if="grades.length === 0">
      <div class="flex flex-col items-center justify-center py-20 animate__animated animate__fadeIn">
        <svg class="w-48 h-48 mb-6" viewBox="0 0 200 200" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect x="40" y="30" width="120" height="140" rx="8" fill="#F3F4F6"/>
          <rect x="40" y="30" width="120" height="140" rx="8" stroke="#E5E7EB" stroke-width="1.5"/>
          <rect x="55" y="48" width="90" height="6" rx="3" fill="#E5E7EB"/>
          <rect x="55" y="62" width="70" height="6" rx="3" fill="#E5E7EB"/>
          <rect x="55" y="84" width="90" height="6" rx="3" fill="#E5E7EB"/>
          <rect x="55" y="98" width="60" height="6" rx="3" fill="#E5E7EB"/>
          <rect x="55" y="120" width="80" height="6" rx="3" fill="#E5E7EB"/>
          <rect x="55" y="134" width="50" height="6" rx="3" fill="#E5E7EB"/>
          <circle cx="100" cy="155" r="12" fill="#D1D5DB"/>
          <path d="M95 155L98 158L105 151" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M68 82L82 68" stroke="#E5E7EB" stroke-width="2" stroke-linecap="round"/>
          <path d="M68 68L82 82" stroke="#E5E7EB" stroke-width="2" stroke-linecap="round"/>
          <text x="100" y="195" text-anchor="middle" fill="#9CA3AF" font-size="13" font-family="sans-serif">暂无成绩数据</text>
        </svg>
      </div>
    </template>

    <template v-else>
      <div class="animate__animated animate__fadeIn animate__faster">
        <div class="relative overflow-hidden rounded-2xl px-8 py-7 mb-6 text-white" style="background: linear-gradient(135deg, #1e3a5f 0%, #1a1a3e 40%, #2d1b69 100%);">
          <div class="absolute top-0 right-0 w-64 h-64 opacity-10">
            <div class="absolute top-[-20px] right-[-20px] w-48 h-48 rounded-full bg-white"></div>
          </div>
          <div class="absolute bottom-0 left-0 w-80 h-80 opacity-5">
            <div class="absolute bottom-[-40px] left-[-40px] w-64 h-64 rounded-full bg-white"></div>
          </div>

          <div class="relative z-10">
            <div class="flex flex-col sm:flex-row sm:items-end sm:justify-between gap-4">
              <div>
                <p class="text-white/60 text-xs font-medium uppercase tracking-widest mb-1">GPA</p>
                <div class="flex items-baseline gap-1">
                  <span class="text-5xl sm:text-6xl font-bold tracking-tight tabular-nums">{{ displayGpa }}</span>
                  <span class="text-white/50 text-sm ml-1">/ 4.0</span>
                </div>
              </div>
              <div class="flex flex-wrap gap-6 sm:gap-8">
                <div class="text-center">
                  <p class="text-2xl sm:text-3xl font-bold tabular-nums">{{ displayCredits }}</p>
                  <p class="text-white/60 text-xs mt-0.5">总学分</p>
                </div>
                <div class="text-center">
                  <p class="text-2xl sm:text-3xl font-bold tabular-nums">{{ displayCourseCount }}</p>
                  <p class="text-white/60 text-xs mt-0.5">课程数</p>
                </div>
                <div class="text-center">
                  <p class="text-2xl sm:text-3xl font-bold tabular-nums">{{ displayMaxScore }}</p>
                  <p class="text-white/60 text-xs mt-0.5">最高分</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-5 gap-6 mb-6">
        <div class="lg:col-span-3 animate__animated animate__fadeIn animate__faster" style="animation-delay: 0.1s;">
          <div class="bg-white rounded-xl border border-soft p-5 h-full">
            <h3 class="text-sm font-semibold text-ink mb-4 flex items-center gap-2">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><rect x="3" y="3" width="18" height="18" rx="2"/><path d="M3 9h18"/><path d="M9 21V9"/></svg>
              成绩分布
            </h3>
            <div class="space-y-3">
              <div v-for="d in distributionData" :key="d.label" class="flex items-center gap-3">
                <span class="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0" :style="{ background: d.color + '20', color: d.color }">{{ d.count }}</span>
                <span class="text-xs text-steel w-16 flex-shrink-0">{{ d.label }}</span>
                <div class="flex-1 h-2.5 bg-wash rounded-full overflow-hidden">
                  <div class="h-full rounded-full transition-all duration-1000 ease-out" :style="{ width: distributionMax > 0 ? (d.count / distributionMax * 100) + '%' : '0%', background: d.color }"></div>
                </div>
                <span class="text-xs text-mist w-8 text-right">{{ distributionMax > 0 ? (d.count / totalCourses * 100).toFixed(0) : 0 }}%</span>
              </div>
            </div>
          </div>
        </div>

        <div class="lg:col-span-2 animate__animated animate__fadeIn animate__faster" style="animation-delay: 0.2s;">
          <div class="bg-white rounded-xl border border-soft p-5 h-full">
            <h3 class="text-sm font-semibold text-ink mb-4 flex items-center gap-2">
              <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"/></svg>
              学期GPA趋势
            </h3>
            <div v-if="semesterTrend.length > 0" class="relative pt-2">
              <div class="flex items-end justify-between gap-1" style="min-height: 100px;">
                <div v-for="(item, idx) in semesterTrend" :key="idx" class="flex-1 flex flex-col items-center gap-1.5">
                  <span class="text-[10px] font-bold tabular-nums" :style="{ color: getTrendColor(item.gpa) }">{{ item.gpa }}</span>
                  <div class="w-full rounded-t-md transition-all duration-1000 ease-out" :style="{ height: Math.max(item.gpa / 4 * 80, 8) + 'px', background: getTrendGradient(item.gpa) }"></div>
                  <span class="text-[10px] text-mist text-center leading-tight truncate w-full">{{ shortSemester(item.semester) }}</span>
                </div>
              </div>
            </div>
            <div v-else class="flex items-center justify-center h-24 text-sm text-mist">至少需要两个学期数据</div>
          </div>
        </div>
      </div>

      <div class="space-y-4">
        <div v-for="(group, semester) in semesterGroups" :key="semester" class="animate__animated animate__fadeIn animate__faster" :style="{ animationDelay: (Object.keys(semesterGroups).indexOf(semester) * 0.08 + 0.3) + 's' }">
          <div class="bg-white rounded-xl border border-soft overflow-hidden transition-shadow duration-200 hover:shadow-sm">
            <div class="flex items-center justify-between px-5 py-3.5 cursor-pointer select-none" @click="toggleSemester(semester)">
              <div class="flex items-center gap-3">
                <svg class="w-5 h-5 transition-transform duration-200" :class="isSemesterOpen(semester) ? 'rotate-90' : ''" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" style="color: #6B7280"><polyline points="9 18 15 12 9 6"/></svg>
                <span class="text-sm font-semibold text-ink">{{ semester }}</span>
                <span class="text-xs text-mist bg-wash px-2 py-0.5 rounded-full">{{ group.length }}门课</span>
              </div>
              <div class="flex items-center gap-4">
                <span class="hidden sm:inline text-xs text-mist">学期GPA</span>
                <span class="text-base font-bold" :style="{ color: getTrendColor(semesterGpaMap[semester] || 0) }">{{ (semesterGpaMap[semester] || 0).toFixed(2) }}</span>
              </div>
            </div>

            <transition name="collapse">
              <div v-show="isSemesterOpen(semester)">
                <div class="border-t border-wash">
                  <div v-for="(g, idx) in group" :key="g.id" class="flex items-center justify-between px-5 py-4 transition-colors duration-150 hover:bg-cloud" :class="{ 'border-t border-wash': idx > 0 }">
                    <div class="flex items-center gap-3 min-w-0 flex-1">
                      <span class="w-2.5 h-2.5 rounded-full flex-shrink-0" :style="{ background: scoreDotColor(g.score) }"></span>
                      <div class="min-w-0">
                        <p class="text-sm font-medium text-ink truncate">{{ g.courseName || '未知课程' }}</p>
                        <p class="text-xs text-mist mt-0.5 truncate">{{ g.semester }} · {{ g.gradeType || '百分制' }}{{ g.credit ? ' · ' + g.credit + '学分' : '' }}</p>
                      </div>
                    </div>
                    <div class="flex items-center gap-3 flex-shrink-0 ml-3">
                      <span class="text-xl sm:text-2xl font-bold tabular-nums" :style="{ color: scoreTextColor(g.score) }">{{ g.score }}</span>
                      <span class="text-[11px] font-medium px-2 py-0.5 rounded-full" :style="{ background: scoreBadgeBg(g.score), color: scoreBadgeText(g.score) }">{{ scoreLabel(g.score) }}</span>
                    </div>
                  </div>
                </div>
              </div>
            </transition>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyGrades } from '@/api/edu'

const grades = ref<any[]>([])
const loading = ref(false)
const openSemesters = ref<Set<string>>(new Set())

const gpa = computed(() => {
  if (grades.value.length === 0) return 0
  const points = grades.value.reduce((s: number, g: any) => {
    const score = g.score || 0
    if (score >= 90) return s + 4.0
    if (score >= 80) return s + 3.0
    if (score >= 70) return s + 2.0
    if (score >= 60) return s + 1.0
    return s
  }, 0)
  return points / grades.value.length
})

const totalCredits = computed(() => {
  return grades.value.reduce((s: number, g: any) => s + (g.credit || 0), 0)
})

const totalCourses = computed(() => grades.value.length)

const maxScore = computed(() => {
  if (grades.value.length === 0) return 0
  return Math.max(...grades.value.map((g: any) => g.score || 0))
})

const semesterGroups = computed(() => {
  const groups: Record<string, any[]> = {}
  grades.value.forEach((g: any) => {
    const sem = g.semester || '未知学期'
    if (!groups[sem]) groups[sem] = []
    groups[sem].push(g)
  })
  const order = Object.keys(groups).sort().reverse()
  const ordered: Record<string, any[]> = {}
  order.forEach(k => { ordered[k] = groups[k] })
  return ordered
})

const semesterGpaMap = computed(() => {
  const map: Record<string, number> = {}
  Object.entries(semesterGroups.value).forEach(([sem, list]) => {
    if (list.length === 0) { map[sem] = 0; return }
    const points = list.reduce((s: number, g: any) => {
      const score = g.score || 0
      if (score >= 90) return s + 4.0
      if (score >= 80) return s + 3.0
      if (score >= 70) return s + 2.0
      if (score >= 60) return s + 1.0
      return s
    }, 0)
    map[sem] = points / list.length
  })
  return map
})

const semesterTrend = computed(() => {
  const entries = Object.entries(semesterGpaMap.value)
  entries.sort(([a], [b]) => a.localeCompare(b))
  return entries.map(([semester, gpa]) => ({ semester, gpa: Math.round(gpa * 100) / 100 }))
})

const distributionData = computed(() => {
  const dist = [
    { label: '优秀 A', range: [90, 101], color: '#10b981', count: 0 },
    { label: '良好 B', range: [80, 90], color: '#3b82f6', count: 0 },
    { label: '中等 C', range: [70, 80], color: '#eab308', count: 0 },
    { label: '及格 D', range: [60, 70], color: '#f97316', count: 0 },
    { label: '不及格 F', range: [0, 60], color: '#ef4444', count: 0 },
  ]
  grades.value.forEach((g: any) => {
    const score = g.score || 0
    for (const d of dist) {
      if (score >= d.range[0] && score < d.range[1]) { d.count++; break }
    }
  })
  return dist
})

const distributionMax = computed(() => {
  return Math.max(...distributionData.value.map(d => d.count), 1)
})

const displayGpa = ref('0.00')
const displayCredits = ref('0')
const displayCourseCount = ref('0')
const displayMaxScore = ref('0')

function animateCountUp(target: number, setter: (v: string) => void, decimals: number = 0) {
  const duration = 1000
  const start = performance.now()
  function tick(now: number) {
    const elapsed = now - start
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    const current = target * eased
    setter(decimals > 0 ? current.toFixed(decimals) : Math.round(current).toString())
    if (progress < 1) requestAnimationFrame(tick)
  }
  requestAnimationFrame(tick)
}

function animateAll() {
  animateCountUp(gpa.value, (v) => { displayGpa.value = v }, 2)
  animateCountUp(totalCredits.value, (v) => { displayCredits.value = v }, 0)
  animateCountUp(totalCourses.value, (v) => { displayCourseCount.value = v }, 0)
  animateCountUp(maxScore.value, (v) => { displayMaxScore.value = v }, 0)
}

function toggleSemester(semester: string) {
  if (openSemesters.value.has(semester)) {
    openSemesters.value.delete(semester)
    openSemesters.value = new Set(openSemesters.value)
  } else {
    openSemesters.value.add(semester)
    openSemesters.value = new Set(openSemesters.value)
  }
}

function isSemesterOpen(semester: string) {
  return openSemesters.value.has(semester)
}

function scoreDotColor(score: number) {
  if (score >= 90) return '#10b981'
  if (score >= 80) return '#3b82f6'
  if (score >= 70) return '#eab308'
  if (score >= 60) return '#f97316'
  return '#ef4444'
}

function scoreTextColor(score: number) {
  if (score >= 90) return '#059669'
  if (score >= 80) return '#2563eb'
  if (score >= 70) return '#ca8a04'
  if (score >= 60) return '#ea580c'
  return '#dc2626'
}

function scoreBadgeBg(score: number) {
  if (score >= 90) return '#d1fae5'
  if (score >= 80) return '#dbeafe'
  if (score >= 70) return '#fef9c3'
  if (score >= 60) return '#ffedd5'
  return '#fee2e2'
}

function scoreBadgeText(score: number) {
  if (score >= 90) return '#065f46'
  if (score >= 80) return '#1e40af'
  if (score >= 70) return '#854d0e'
  if (score >= 60) return '#9a3412'
  return '#991b1b'
}

function scoreLabel(score: number) {
  if (score >= 90) return '优秀'
  if (score >= 80) return '良好'
  if (score >= 70) return '中等'
  if (score >= 60) return '及格'
  return '不及格'
}

function getTrendColor(val: number) {
  if (val >= 3.5) return '#10b981'
  if (val >= 3.0) return '#3b82f6'
  if (val >= 2.0) return '#eab308'
  if (val >= 1.0) return '#f97316'
  return '#ef4444'
}

function getTrendGradient(val: number) {
  if (val >= 3.5) return 'linear-gradient(to top, #10b981, #34d399)'
  if (val >= 3.0) return 'linear-gradient(to top, #3b82f6, #60a5fa)'
  if (val >= 2.0) return 'linear-gradient(to top, #eab308, #facc15)'
  if (val >= 1.0) return 'linear-gradient(to top, #f97316, #fb923c)'
  return 'linear-gradient(to top, #ef4444, #f87171)'
}

function shortSemester(sem: string) {
  if (sem.length <= 12) return sem
  const parts = sem.split(' ')
  if (parts.length >= 2) return parts[0] + (parts[1].includes('第') ? '' : '') + (parts[1] || '').substring(0, 2)
  return sem.substring(0, 10) + '...'
}

onMounted(async () => {
  loading.value = true
  try {
    const r = await getMyGrades()
    grades.value = r.data || []
    const sorted = Object.keys(semesterGroups.value).sort().reverse()
    if (sorted.length > 0) {
      openSemesters.value = new Set([sorted[0]])
    }
    if (grades.value.length > 0) {
      setTimeout(animateAll, 100)
    }
  } catch {
    grades.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.collapse-enter-active,
.collapse-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.collapse-enter-from,
.collapse-leave-to {
  max-height: 0;
  opacity: 0;
}

.collapse-enter-to,
.collapse-leave-from {
  max-height: 1000px;
  opacity: 1;
}

@keyframes slideInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.slide-in-up {
  animation: slideInUp 0.5s ease forwards;
}

.tabular-nums {
  font-variant-numeric: tabular-nums;
}
</style>
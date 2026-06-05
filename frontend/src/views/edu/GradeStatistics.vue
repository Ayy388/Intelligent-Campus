<template>
  <div class="animate__animated animate__fadeInUp">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-ink">成绩统计分析</h2>
    </div>

    <div class="bg-white rounded-xl border border-soft p-5 mb-6">
      <div class="flex items-center gap-4">
        <span class="text-sm text-ash whitespace-nowrap">筛选条件</span>
        <el-select v-model="selectedSemester" placeholder="选择学期" clearable class="w-52" @change="onSemesterChange">
          <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
        </el-select>
        <el-select v-model="selectedCourseId" placeholder="选择课程（可搜索）" clearable filterable class="w-72">
          <el-option v-for="c in courses" :key="c.id" :label="c.courseName" :value="c.id" />
        </el-select>
      </div>
    </div>

    <div class="grid grid-cols-4 gap-5 mb-6">
      <div class="bg-white rounded-xl border border-soft p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-lg bg-blue-50 flex items-center justify-center shrink-0">
          <svg class="w-6 h-6 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6.253v13m0-13C10.832 5.477 9.246 5 7.5 5S4.168 5.477 3 6.253v13C4.168 18.477 5.754 18 7.5 18s3.332.477 4.5 1.253m0-13C13.168 5.477 14.754 5 16.5 5c1.747 0 3.332.477 4.5 1.253v13C19.832 18.477 18.247 18 16.5 18c-1.746 0-3.332.477-4.5 1.253"/></svg>
        </div>
        <div>
          <div class="text-xs text-mist mb-0.5">总课程数</div>
          <div class="text-2xl font-bold text-ink">{{ totalCourses }}</div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-soft p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-lg bg-green-50 flex items-center justify-center shrink-0">
          <svg class="w-6 h-6 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"/></svg>
        </div>
        <div>
          <div class="text-xs text-mist mb-0.5">总学生数</div>
          <div class="text-2xl font-bold text-ink">{{ totalStudents }}</div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-soft p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-lg bg-amber-50 flex items-center justify-center shrink-0">
          <svg class="w-6 h-6 text-amber-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"/></svg>
        </div>
        <div>
          <div class="text-xs text-mist mb-0.5">平均分</div>
          <div class="text-2xl font-bold text-ink">{{ averageScore }}</div>
        </div>
      </div>
      <div class="bg-white rounded-xl border border-soft p-5 flex items-center gap-4">
        <div class="w-12 h-12 rounded-lg bg-purple-50 flex items-center justify-center shrink-0">
          <svg class="w-6 h-6 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
        </div>
        <div>
          <div class="text-xs text-mist mb-0.5">及格率</div>
          <div class="text-2xl font-bold text-ink">{{ passRate }}</div>
        </div>
      </div>
    </div>

    <div class="bg-white rounded-xl border border-soft">
      <el-table :data="displayCourses" v-loading="loading" row-key="id" @expand-change="onExpandChange" :empty-text="displayCourses.length === 0 && !loading ? '暂无课程数据' : ' '">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="px-6 py-4">
              <div v-if="!gradesMap[row.id]" class="text-center text-mist text-sm py-4">加载中...</div>
              <div v-else-if="gradesMap[row.id]!.length === 0" class="text-center text-mist text-sm py-4">该课程暂无成绩数据</div>
              <el-table v-else :data="gradesMap[row.id]!" size="small" max-height="360" stripe>
                <el-table-column prop="studentUsername" label="学号" width="140" />
                <el-table-column prop="studentName" label="姓名" width="140" />
                <el-table-column label="分数" width="120">
                  <template #default="{ row: s }">
                    <span :class="scoreColorClass(s.score)">{{ s.score }}</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="courseName" label="课程名称" min-width="180">
          <template #default="{ row }">
            <span class="font-medium text-ink">{{ row.courseName }}</span>
          </template>
        </el-table-column>
        <el-table-column label="学生数" width="100" align="center">
          <template #default="{ row }">
            <span>{{ getCourseGradesCount(row.id) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="平均分" width="110" align="center">
          <template #default="{ row }">
            <span v-if="getCourseStats(row.id)" :class="scoreColorClass(getCourseStats(row.id)!.avg)">{{ getCourseStats(row.id)!.avg }}</span>
            <span v-else class="text-mist">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最高分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="getCourseStats(row.id)" class="text-green-600 font-medium">{{ getCourseStats(row.id)!.max }}</span>
            <span v-else class="text-mist">—</span>
          </template>
        </el-table-column>
        <el-table-column label="最低分" width="100" align="center">
          <template #default="{ row }">
            <span v-if="getCourseStats(row.id)" class="text-red-500 font-medium">{{ getCourseStats(row.id)!.min }}</span>
            <span v-else class="text-mist">—</span>
          </template>
        </el-table-column>
        <el-table-column label="及格率" width="120" align="center">
          <template #default="{ row }">
            <span v-if="getCourseStats(row.id)">
              <el-tag :type="getCourseStats(row.id)!.passRateValue >= 80 ? 'success' : getCourseStats(row.id)!.passRateValue >= 60 ? 'warning' : 'danger'" size="small" effect="dark">
                {{ getCourseStats(row.id)!.passRate }}
              </el-tag>
            </span>
            <span v-else class="text-mist">—</span>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getCourses, getCourseGrades, getSemesters } from '@/api/edu'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import type { Course, Grade, Semester } from '@/types'

const userStore = useUserStore()
const semesters = ref<string[]>([])
const selectedSemester = ref('')
const selectedCourseId = ref<number | null>(null)
const courses = ref<Course[]>([])
const gradesMap = ref<Record<number, Grade[]>>({})
const loading = ref(false)

const displayCourses = computed(() => {
  let list = courses.value
  if (selectedCourseId.value) {
    list = list.filter(c => c.id === selectedCourseId.value)
  }
  return list
})

function flattenGrades(): Grade[] {
  return Object.values(gradesMap.value).flat()
}

const totalCourses = computed(() => displayCourses.value.length)

const totalStudents = computed(() => {
  const g = flattenGrades()
  const ids = new Set(g.map((s: Grade) => s.studentId))
  return ids.size
})

const averageScore = computed(() => {
  const g = flattenGrades()
  if (g.length === 0) return '—'
  const sum = g.reduce((s: number, item: Grade) => s + (Number(item.score) || 0), 0)
  return (sum / g.length).toFixed(1)
})

const passRate = computed(() => {
  const g = flattenGrades()
  if (g.length === 0) return '—'
  const passed = g.filter((item: Grade) => (Number(item.score) || 0) >= 60).length
  return ((passed / g.length) * 100).toFixed(1) + '%'
})

function scoreColorClass(score: number | string) {
  const s = Number(score)
  if (s >= 90) return 'text-green-600 font-medium'
  if (s >= 60) return 'text-ink font-medium'
  return 'text-red-500 font-medium'
}

function getCourseGradesCount(courseId: number): number {
  return gradesMap.value[courseId]?.length || 0
}

function getCourseStats(courseId: number): { avg: string; max: number; min: number; passRate: string; passRateValue: number } | null {
  const g = gradesMap.value[courseId]
  if (!g || g.length === 0) return null
  const scores = g.map((item: Grade) => Number(item.score) || 0)
  const sum = scores.reduce((s: number, v: number) => s + v, 0)
  const max = Math.max(...scores)
  const min = Math.min(...scores)
  const avg = (sum / scores.length).toFixed(1)
  const passed = scores.filter(v => v >= 60).length
  const passRateValue = (passed / scores.length) * 100
  const passRate = passRateValue.toFixed(1) + '%'
  return { avg, max, min, passRate, passRateValue }
}

async function loadSemesters() {
  try {
    const r = await getSemesters()
    const data = r.data || []
    semesters.value = data.map((s: Semester) => (typeof s === 'string' ? s : s.name || s.semester || ''))
  } catch {
    ElMessage.error('获取学期列表失败')
  }
}

async function loadCourses() {
  loading.value = true
  try {
    const params: Record<string, any> = { page: 1, size: 999 }
    if (selectedSemester.value) {
      params.semester = selectedSemester.value
    }
    const r = await getCourses(params)
    courses.value = r.data?.records || r.data || []
  } catch {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

async function loadCourseGradesData(courseId: number) {
  try {
    const r = await getCourseGrades(courseId)
    gradesMap.value[courseId] = r.data || []
  } catch {
    gradesMap.value[courseId] = []
  }
}

async function onExpandChange(row: Course, expandedRows: Course[]) {
  const expanded = expandedRows && expandedRows.some((r: Course) => r.id === row.id)
  if (expanded && !gradesMap.value[row.id]) {
    await loadCourseGradesData(row.id)
  }
}

function onSemesterChange() {
  selectedCourseId.value = null
  gradesMap.value = {}
  loadCourses()
}

onMounted(async () => {
  await loadSemesters()
  await loadCourses()
})
</script>

<style scoped>
.el-table {
  --el-table-border-color: #f0f0f0;
  --el-table-header-bg-color: #fafafa;
}
.el-table :deep(.el-table__expanded-cell) {
  padding: 0 !important;
  background-color: #f9fafb;
}
.el-table :deep(.el-loading-mask) {
  border-radius: 0.75rem;
}
</style>
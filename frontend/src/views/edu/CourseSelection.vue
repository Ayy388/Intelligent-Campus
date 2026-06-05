<template>
  <div>
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-ink m-0">在线选课</h2>
        <p class="text-steel text-sm mt-1">浏览可选课程并报名</p>
      </div>
      <el-button type="primary" @click="fetchData" :loading="loading">刷新</el-button>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      <div
        v-for="(course, idx) in courses"
        :key="course.id"
        class="bg-white rounded-2xl border border-gray-100 p-5 transition-all duration-300 hover:shadow-lg hover:-translate-y-0.5 animate__animated animate__fadeInUp"
        :style="{ animationDelay: idx * 0.06 + 's' }"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <span class="w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold text-sm flex-shrink-0"
              :class="course.courseType === 'elective' ? 'bg-gradient-to-br from-amber-400 to-orange-500' : 'bg-gradient-to-br from-blue-400 to-indigo-500'">
              {{ course.courseName?.charAt(0) || '课' }}
            </span>
            <div>
              <h4 class="text-base font-semibold text-ink m-0 leading-tight">{{ course.courseName }}</h4>
              <p class="text-xs text-steel mt-0.5">{{ course.teacherName || '待定' }}</p>
            </div>
          </div>
          <el-tag
            :type="course.courseType === 'elective' ? 'warning' : 'primary'"
            size="small"
            class="!rounded-full !px-3 !border-none"
          >
            {{ course.courseType === 'elective' ? '选修' : '必修' }}
          </el-tag>
        </div>

        <div class="grid grid-cols-2 gap-3 text-sm mb-2">
          <div class="bg-wash rounded-lg p-2.5 text-center">
            <span class="block text-xs text-steel mb-0.5">学分</span>
            <span class="font-semibold text-ink">{{ course.credit ?? '-' }}</span>
          </div>
          <div class="bg-wash rounded-lg p-2.5 text-center">
            <span class="block text-xs text-steel mb-0.5">教室</span>
            <span class="font-semibold text-ink">{{ course.classroom || '-' }}</span>
          </div>
        </div>

        <div class="mb-4">
          <div class="flex items-center justify-between text-xs mb-1.5">
            <span class="text-steel">已报名 {{ course.enrolled ?? 0 }}{{ course.capacity ? '/' + course.capacity : '' }} 人</span>
            <span v-if="course.minStudents" class="text-mist">最低开课 {{ course.minStudents }} 人</span>
          </div>
          <div class="w-full bg-wash rounded-full h-2 overflow-hidden">
            <div
              class="h-full rounded-full transition-all duration-700"
              :class="progressClass(course)"
              :style="{ width: course.capacity ? Math.min((course.enrolled || 0) / course.capacity * 100, 100) + '%' : '0%' }"
            />
          </div>
        </div>

        <div class="flex gap-2">
          <el-tag
            :type="course.status === 1 ? 'success' : 'info'"
            size="small"
            class="!rounded-full !flex-1 !justify-center !border-none"
          >
            {{ course.status === 1 ? '选课中' : '已确认' }}
          </el-tag>
          <el-button
            type="primary"
            size="small"
            class="!rounded-xl !flex-1"
            :disabled="(course.capacity && course.enrolled >= course.capacity) || course.status !== 1"
            @click="doEnroll(course)"
          >
            立即报名
          </el-button>
        </div>
      </div>
    </div>

    <div
      v-if="!loading && courses.length === 0"
      class="flex flex-col items-center justify-center py-24 animate__animated animate__fadeIn"
    >
      <svg class="w-24 h-24 mb-5" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="3" y="3" width="18" height="18" rx="3" stroke="#CBD5E1" stroke-width="1.5"/>
        <path d="M8 8h8M8 12h8M8 16h5" stroke="#CBD5E1" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <p class="text-steel text-base font-medium mb-1.5">暂无可选课程</p>
      <p class="text-mist text-sm">等待教师或管理员发布课程</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAvailableCourses, enrollCourse } from '@/api/edu'
import { ElMessage } from 'element-plus'
import type { Course } from '@/types'

const courses = ref<Course[]>([])
const loading = ref(false)

function progressClass(course: Course) {
  const rate = course.capacity ? (course.enrolled || 0) / course.capacity : 0
  if (rate >= 1) return 'bg-gradient-to-r from-red-400 to-rose-500'
  if (rate >= 0.8) return 'bg-gradient-to-r from-amber-400 to-orange-500'
  return 'bg-gradient-to-r from-emerald-400 to-teal-500'
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getAvailableCourses()
    courses.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function doEnroll(course: Course) {
  try {
    await enrollCourse(course.id)
    ElMessage.success('报名成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

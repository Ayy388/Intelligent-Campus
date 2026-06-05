<template>
  <div>
    <div class="mb-4">
      <span class="text-lg font-bold text-ink">成绩提交状态</span>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div class="flex items-center gap-4 mb-5">
        <span class="text-sm text-ash">选择学期：</span>
        <el-select v-model="selectedSemester" placeholder="全部学期" class="w-60" @change="fetchStatus" clearable>
          <el-option v-for="s in semesters" :key="s" :label="s" :value="s" />
        </el-select>
      </div>

      <div v-if="loading" class="flex flex-col items-center justify-center py-10">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span class="text-mist text-sm mt-3">加载数据中...</span>
      </div>

      <div v-else-if="statusList.length === 0" class="text-center text-mist py-10 text-sm">
        暂无课程数据
      </div>

      <div v-else class="space-y-4">
        <div v-for="item in statusList" :key="item.courseId"
          class="border border-soft rounded-lg p-4 hover:border-primary/30 transition-colors">
          <div class="flex items-center justify-between mb-3">
            <div>
              <span class="font-medium text-ink">{{ item.courseName }}</span>
              <span class="text-xs text-ash ml-2">{{ item.courseCode }}</span>
              <span class="text-xs text-mist ml-2">{{ item.semester }}</span>
            </div>
            <el-button size="small" type="primary" @click="goEntry(item.courseId)">继续录入</el-button>
          </div>
          <div class="flex items-center gap-4">
            <el-progress
              :percentage="Math.round(item.progress)"
              :status="item.progress >= 100 ? 'success' : undefined"
              :stroke-width="16"
              class="flex-1"
            >
              <span class="text-xs text-ash">{{ item.gradedCount }}/{{ item.totalStudents }}</span>
            </el-progress>
            <span class="text-sm text-ash whitespace-nowrap">{{ item.gradedCount }}/{{ item.totalStudents }} 人</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTeacherGradeStatus, getSemesters } from '@/api/edu'
import { ElMessage } from 'element-plus'
import type { Semester } from '@/types'

const router = useRouter()
const loading = ref(false)
const statusList = ref<any[]>([])
const semesters = ref<string[]>([])
const selectedSemester = ref('')

async function fetchSemesters() {
  try {
    const r = await getSemesters()
    semesters.value = (r.data || []).map((s: Semester) => s.name || s.semester || '')
  } catch {
    // ignore
  }
}

async function fetchStatus() {
  loading.value = true
  try {
    const r = await getTeacherGradeStatus(selectedSemester.value || undefined)
    statusList.value = r.data || []
  } catch {
    ElMessage.error('获取成绩状态失败')
  } finally {
    loading.value = false
  }
}

function goEntry(courseId: number) {
  router.push({ path: '/edu/grade-entry', query: { courseId: String(courseId) } })
}

onMounted(() => {
  fetchSemesters()
  fetchStatus()
})
</script>
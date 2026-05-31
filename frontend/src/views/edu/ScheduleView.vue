<template>
  <div class="animate__animated animate__fadeInUp">
    <div class="flex items-center justify-between mb-6">
      <h2 class="text-xl font-bold text-ink">我的课表</h2>
      <div class="flex items-center gap-4">
        <div class="flex items-center gap-2">
          <el-select v-model="currentSemester" placeholder="选择学期" class="w-44" @change="fetchSchedule">
            <el-option v-for="s in semesters" :key="s.id" :label="s.xqqc" :value="s.xqjc" />
          </el-select>
        </div>
        <div class="flex items-center gap-2">
          <el-input
            v-model="currentWeek"
            type="number"
            :min="1"
            :max="20"
            class="w-28"
            @change="fetchSchedule"
          />
          <span class="text-ash text-sm">周</span>
        </div>
        <el-button type="primary" @click="exportSchedule">
          导出课表
        </el-button>
      </div>
    </div>

    <div class="schedule-container bg-white rounded-xl border border-soft p-6">
      <div class="grid grid-cols-8 gap-2 mb-4">
        <div class="text-center text-sm font-semibold text-ash py-2">时间</div>
        <div v-for="day in weekDays" :key="day" class="text-center text-sm font-semibold text-ash py-2">
          {{ day }}
        </div>
      </div>
      
      <div v-for="timeSlot in timeSlots" :key="timeSlot.id" class="grid grid-cols-8 gap-2 mb-2">
        <div class="flex flex-col items-center justify-center text-center text-xs text-ash bg-cloud rounded-lg py-2">
          <span class="font-medium">{{ timeSlot.name }}</span>
          <span class="text-[10px]">{{ timeSlot.start }}-{{ timeSlot.end }}</span>
        </div>
        
        <div v-for="day in 7" :key="day" class="min-h-[80px]">
          <div
            v-for="course in getCourseForSlot(day, timeSlot.id)" :key="course.id"
            class="h-full rounded-lg p-2 text-white text-xs leading-tight cursor-pointer hover:opacity-90 transition-opacity"
            :style="{ backgroundColor: getCourseColor(course.id) }"
            @click="showDetail(course)"
          >
            <div class="font-medium truncate">{{ course.courseName }}</div>
            <div class="truncate opacity-90 mt-1">{{ course.teacherName || '未知' }}</div>
            <div v-if="course.classroom" class="truncate opacity-90">{{ course.classroom }}</div>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-4 text-sm text-ash">
      <el-icon class="mr-1 align-middle"><InfoFilled /></el-icon>
      <span>提示：点击课程卡片查看详情</span>
    </div>

    <el-dialog v-model="detailVisible" title="课程详情" width="500px">
      <div v-if="currentCourse" class="space-y-3">
        <div class="grid grid-cols-3 gap-3">
          <div class="text-ash text-sm">课程名称</div>
          <div class="col-span-2 text-ink font-medium">{{ currentCourse.courseName }}</div>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <div class="text-ash text-sm">课程编号</div>
          <div class="col-span-2 text-ink">{{ currentCourse.courseCode }}</div>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <div class="text-ash text-sm">授课教师</div>
          <div class="col-span-2 text-ink">{{ currentCourse.teacherName || '未知' }}</div>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <div class="text-ash text-sm">上课地点</div>
          <div class="col-span-2 text-ink">{{ currentCourse.classroom || '未安排' }}</div>
        </div>
        <div class="grid grid-cols-3 gap-3">
          <div class="text-ash text-sm">学分</div>
          <div class="col-span-2 text-ink">{{ currentCourse.credit }}</div>
        </div>
        <div v-if="currentCourse.description" class="grid grid-cols-3 gap-3">
          <div class="text-ash text-sm">课程简介</div>
          <div class="col-span-2 text-ink">{{ currentCourse.description }}</div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { InfoFilled } from '@element-plus/icons-vue'
import { getSchedule, getSemesters } from '@/api/edu'

const currentWeek = ref(1)
const currentSemester = ref('')
const semesters = ref<any[]>([])
const courses = ref<any[]>([])
const detailVisible = ref(false)
const currentCourse = ref<any>(null)

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const timeSlots = [
  { id: 1, name: '第1-2节', start: '08:00', end: '09:40' },
  { id: 2, name: '第3-4节', start: '10:00', end: '11:40' },
  { id: 3, name: '第5-6节', start: '14:00', end: '15:40' },
  { id: 4, name: '第7-8节', start: '16:00', end: '17:40' },
  { id: 5, name: '第9-10节', start: '19:00', end: '20:40' }
]

const courseColors = [
  '#3B82F6', '#10B981', '#F59E0B', '#EF4444', '#8B5CF6',
  '#EC4899', '#06B6D4', '#6366F1', '#14B8A6', '#F97316'
]

function getCourseColor(id: number): string {
  return courseColors[id % courseColors.length]
}

function getCourseForSlot(day: number, timeSlot: number): any[] {
  return courses.value.filter(c => {
    if (!c.schedule) return false
    try {
      const sched = JSON.parse(c.schedule)
      return sched.day === day && sched.timeSlot === timeSlot
    } catch {
      return false
    }
  })
}

function showDetail(course: any) {
  currentCourse.value = course
  detailVisible.value = true
}

async function fetchSchedule() {
  try {
    const res = await getSchedule(currentSemester.value, currentWeek.value)
    courses.value = res.data || []
  } catch {
    ElMessage.error('获取课表失败')
  }
}

async function exportSchedule() {
  try {
    // 使用html2canvas将课表导出为图片
    const scheduleElement = document.querySelector('.schedule-container') as HTMLElement
    if (!scheduleElement) {
      ElMessage.error('找不到课表元素')
      return
    }
    
    // 简单的文本导出
    let text = `课表 - ${currentSemester.value} 第${currentWeek.value}周\n\n`
    const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
    const timeSlots = [
      { id: 1, name: '第1-2节', start: '08:00', end: '09:40' },
      { id: 2, name: '第3-4节', start: '10:00', end: '11:40' },
      { id: 3, name: '第5-6节', start: '14:00', end: '15:40' },
      { id: 4, name: '第7-8节', start: '16:00', end: '17:40' },
      { id: 5, name: '第9-10节', start: '19:00', end: '20:40' }
    ]
    
    for (const timeSlot of timeSlots) {
      text += `\n${timeSlot.name} (${timeSlot.start}-${timeSlot.end})\n`
      for (let day = 1; day <= 7; day++) {
        const dayCourses = getCourseForSlot(day, timeSlot.id)
        if (dayCourses.length > 0) {
          text += `  ${weekDays[day-1]}: `
          text += dayCourses.map(c => `${c.courseName} - ${c.teacherName || '未知'} @ ${c.classroom || '未知'}`).join(', ')
          text += '\n'
        }
      }
    }
    
    const blob = new Blob([text], { type: 'text/plain;charset=utf-8' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `课表_${currentSemester.value}_第${currentWeek.value}周.txt`
    a.click()
    URL.revokeObjectURL(url)
    
    ElMessage.success('课表导出成功')
  } catch (e) {
    console.error(e)
    ElMessage.error('课表导出失败')
  }
}

onMounted(async () => {
  try {
    const r = await getSemesters({ status: 'active' })
    semesters.value = r.data || []
    const active = semesters.value.find((s: any) => s.status === 1)
    if (active) {
      currentSemester.value = active.xqjc
    }
  } catch {}
  fetchSchedule()
})
</script>

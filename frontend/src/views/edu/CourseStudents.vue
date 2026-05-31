<template>
  <div>
    <div class="mb-4">
      <span class="text-lg font-bold text-ink">课程学生名单</span>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div class="flex items-center gap-4 mb-5">
        <span class="text-sm text-ash">选择课程：</span>
        <el-select v-model="selectedCourseId" placeholder="请选择授课课程" class="w-80" @change="onCourseChange" clearable>
          <el-option v-for="c in courses" :key="c.id" :label="`${c.courseName} (${c.semester})`" :value="c.id" />
        </el-select>
        <el-tag v-if="selectedCourse" :type="selectedCourse.status===1?'success':selectedCourse.status===2?'info':'warning'" size="small">
          {{ selectedCourse.status===1?'选课中':selectedCourse.status===2?'已确认':'未发布' }}
        </el-tag>
        <el-button
          v-if="selectedCourse?.status === 1"
          type="success"
          size="small"
          @click="handleConfirm"
          :loading="confirming"
        >
          确认课程
        </el-button>
      </div>

      <div v-if="!selectedCourseId" class="text-center text-mist py-10 text-sm">
        请先选择一门课程
      </div>

      <div v-else-if="students.length === 0" class="text-center text-mist py-10 text-sm">
        该课程暂无选课学生
      </div>

      <div v-else>
        <div class="mb-3 text-sm text-ash">
          共 {{ students.length }} 名学生
        </div>
        <el-table :data="students" border>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="studentName" label="学生姓名" width="150" />
          <el-table-column label="学号" width="120">
            <template #default="{row}">{{ row.studentId }}</template>
          </el-table-column>
          <el-table-column prop="selectTime" label="选课时间">
            <template #default="{row}">{{ row.selectTime?.substring(0, 16) || '' }}</template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getTeacherCourses, getCourseStudents, confirmCourse } from '@/api/edu'
import { ElMessage, ElMessageBox } from 'element-plus'

const courses = ref<any[]>([])
const selectedCourseId = ref<number | null>(null)
const students = ref<any[]>([])
const confirming = ref(false)

const selectedCourse = computed(() => courses.value.find(c => c.id === selectedCourseId.value))

async function fetchCourses() {
  try {
    const r = await getTeacherCourses()
    courses.value = r.data || []
  } catch {
    ElMessage.error('获取课程列表失败')
  }
}

async function onCourseChange() {
  if (!selectedCourseId.value) {
    students.value = []
    return
  }
  try {
    const r = await getCourseStudents(selectedCourseId.value)
    students.value = r.data || []
  } catch {
    ElMessage.error('获取学生名单失败')
  }
}

async function handleConfirm() {
  if (!selectedCourseId.value) return
  try {
    await ElMessageBox.confirm(
      '确认后将无法再进行选课和退课，确定要确认吗？',
      '确认课程',
      { type: 'warning', confirmButtonText: '确认', cancelButtonText: '取消' }
    )
    confirming.value = true
    await confirmCourse(selectedCourseId.value)
    ElMessage.success('课程已确认')
    await fetchCourses()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  } finally {
    confirming.value = false
  }
}

onMounted(fetchCourses)
</script>
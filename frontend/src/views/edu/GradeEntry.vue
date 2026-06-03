<template>
  <div>
    <div class="mb-4">
      <span class="text-lg font-bold text-ink">成绩录入</span>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div class="flex items-center gap-4 mb-5">
        <span class="text-sm text-ash">选择课程：</span>
        <el-select v-model="selectedCourseId" placeholder="请选择授课课程" class="w-80" @change="onCourseChange" clearable :loading="loading">
          <el-option v-for="c in courses" :key="c.id" :label="`${c.courseName} (${c.semester})`" :value="c.id" />
        </el-select>
        <el-select
          v-if="classOptions.length > 0"
          v-model="selectedClassFilter"
          placeholder="按班级筛选"
          class="w-48"
          clearable
        >
          <el-option v-for="cls in classOptions" :key="cls" :label="cls" :value="cls" />
        </el-select>
      </div>

      <div v-if="!selectedCourseId" class="text-center text-mist py-10 text-sm">
        请先选择一门课程
      </div>

      <div v-else-if="studentsLoading" class="flex flex-col items-center justify-center py-10">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span class="text-mist text-sm mt-3">加载学生数据中...</span>
      </div>

      <div v-else-if="students.length === 0" class="text-center text-mist py-10 text-sm">
        该课程暂无选课学生
      </div>

      <div v-else>
        <div class="flex items-center justify-between mb-3">
          <span class="text-sm text-ash">
            共 {{ students.length }} 名学生
            <span v-if="selectedClassFilter" class="ml-1">(当前筛选: {{ filteredStudents.length }})</span>
          </span>
          <el-button type="primary" size="small" @click="submitAllGrades" :loading="submitting">批量提交成绩</el-button>
        </div>
        <el-table :data="filteredStudents" max-height="480" border>
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="studentUsername" label="学号" width="100" />
          <el-table-column label="状态" width="70">
            <template #default="{row}">
              <el-tag v-if="row.graded" type="success" size="small">已录入</el-tag>
              <el-tag v-else type="info" size="small">待录入</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="课程" width="160">
            <template #default="{row}">{{ row.courseName || '未获取' }}</template>
          </el-table-column>
          <el-table-column label="学期" width="120">
            <template #default="{row}">{{ row.semester }}</template>
          </el-table-column>
          <el-table-column label="成绩类型" width="100">
            <template #default="{row}">
              <el-select v-model="gradeTypes[row.id]" class="w-full">
                <el-option label="百分制" value="百分制" />
                <el-option label="等级制" value="等级制" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="分数" width="120">
            <template #default="{row}">
              <el-input-number v-model="scores[row.id]" :min="0" :max="100" :disabled="gradeTypes[row.id]==='等级制'" placeholder="分数" class="w-full" />
            </template>
          </el-table-column>
          <el-table-column label="等级" width="100">
            <template #default="{row}">
              <el-select v-model="gradeLevels[row.id]" class="w-full" :disabled="gradeTypes[row.id]!=='等级制'">
                <el-option label="优" value="优" />
                <el-option label="良" value="良" />
                <el-option label="中" value="中" />
                <el-option label="及格" value="及格" />
                <el-option label="不及格" value="不及格" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="备注" width="140">
            <template #default="{row}">
              <el-input v-model="remarks[row.id]" placeholder="可选" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{row}">
              <el-button size="small" type="primary" @click="doSubmitSingle(row)" :loading="submittingId === row.id" :disabled="submitting">录入</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getTeacherCourses, getCourseStudents, inputGrade } from '@/api/edu'
import { ElMessage } from 'element-plus'

const courses = ref<any[]>([])
const selectedCourseId = ref<number | null>(null)
const students = ref<any[]>([])

const loading = ref(false)
const studentsLoading = ref(false)
const submitting = ref(false)
const submittingId = ref<number | null>(null)

const scores = reactive<Record<number, number>>({})
const gradeTypes = reactive<Record<number, string>>({})
const gradeLevels = reactive<Record<number, string>>({})
const remarks = reactive<Record<number, string>>({})
const selectedClassFilter = ref('')

const classOptions = computed(() => {
  const names = new Set(students.value.map((s: any) => s.studentClassName).filter(Boolean))
  return Array.from(names).sort()
})

const filteredStudents = computed(() => {
  if (!selectedClassFilter.value) return students.value
  return students.value.filter((s: any) => s.studentClassName === selectedClassFilter.value)
})

async function fetchCourses() {
  loading.value = true
  try {
    const r = await getTeacherCourses()
    courses.value = r.data || []
  } catch {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

async function onCourseChange() {
  if (!selectedCourseId.value) {
    students.value = []
    return
  }
  selectedClassFilter.value = ''
  studentsLoading.value = true
  try {
    const r = await getCourseStudents(selectedCourseId.value)
    students.value = r.data || []
    students.value.forEach(s => {
      scores[s.id] = 0
      gradeTypes[s.id] = '百分制'
      gradeLevels[s.id] = '优'
      remarks[s.id] = ''
    })
  } catch {
    ElMessage.error('获取学生列表失败')
  } finally {
    studentsLoading.value = false
  }
}

async function doSubmitSingle(student: any, silent = false) {
  if (scores[student.id] === undefined && gradeTypes[student.id] !== '等级制') {
    if (!silent) ElMessage.warning('请输入分数')
    return
  }
  submittingId.value = student.id
  try {
    const data: any = {
      studentId: student.studentId,
      courseId: selectedCourseId.value,
      semester: student.semester,
      gradeType: gradeTypes[student.id],
      gradeLevel: gradeTypes[student.id] === '等级制' ? gradeLevels[student.id] : '',
      score: gradeTypes[student.id] === '等级制' ? 0 : scores[student.id],
      remark: remarks[student.id] || ''
    }
    if (gradeTypes[student.id] === '等级制') {
      const levelMap: Record<string, number> = { '优': 90, '良': 80, '中': 70, '及格': 60, '不及格': 50 }
      data.score = levelMap[gradeLevels[student.id]] || 0
    }
    await inputGrade(data)
    student.graded = true
    if (!silent) ElMessage.success(`${student.studentName} 成绩录入成功`)
    return true
  } catch {
    if (!silent) ElMessage.error(`${student.studentName} 录入失败`)
    return false
  } finally {
    submittingId.value = null
  }
}

async function submitAllGrades() {
  submitting.value = true
  const list = filteredStudents.value
  let success = 0, fail = 0
  for (let i = 0; i < list.length; i++) {
    const ok = await doSubmitSingle(list[i], true)
    if (ok) success++; else fail++
    ElMessage({
      message: `录入进度: ${i + 1}/${list.length} (成功 ${success}, 失败 ${fail})`,
      type: fail > 0 ? 'warning' : 'info',
      duration: 1500
    })
  }
  submitting.value = false
  if (fail === 0) {
    ElMessage.success(`批量录入完成，共 ${success} 人`)
  } else {
    ElMessage.warning(`录入完成: ${success} 成功, ${fail} 失败`)
  }
}
onMounted(fetchCourses)
</script>
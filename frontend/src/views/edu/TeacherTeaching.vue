<template>
  <div>
    <div class="mb-4 flex items-center justify-between">
      <span class="text-lg font-bold text-ink">我的教学</span>
    </div>

    <div class="bg-white rounded-xl border border-soft p-5 mb-5">
      <div class="flex items-center justify-between mb-4">
        <span class="text-sm font-semibold text-ink">我的课程</span>
        <div class="flex items-center gap-2">
          <el-select v-model="selectedSemester" placeholder="全部学期" class="w-40" clearable @change="fetchData">
            <el-option v-for="s in semesters" :key="s.id" :label="s.xqqc" :value="s.xqjc" />
          </el-select>
          <el-input v-model="searchKeyword" placeholder="搜索课程名称" class="w-48" clearable @input="filterCourses" />
        </div>
      </div>
      <div v-if="loading" class="flex flex-col items-center justify-center py-10">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span class="text-mist text-sm mt-3">加载课程数据中...</span>
      </div>
      <template v-else-if="filteredCourses.length === 0">
      <div class="text-center text-mist py-6 text-sm">
        暂无课程数据
      </div>
      </template>
      <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
          v-for="c in filteredCourses"
          :key="c.id"
          class="rounded-xl border cursor-pointer transition-all duration-200 p-4"
          :class="expandedCourseId === c.id ? 'border-blue-500 bg-blue-50 shadow-md' : 'border-soft hover:border-blue-300 hover:shadow-sm'"
          @click="expandCourse(c.id)"
        >
          <div class="flex items-start justify-between mb-2">
            <span class="text-sm font-semibold text-ink truncate flex-1">{{ c.courseName }}</span>
            <el-tag :type="c.status===1?'success':c.status===2?'info':'warning'" size="small" class="ml-2 flex-shrink-0">
              {{ c.status===1?'选课中':c.status===2?'已确认':'未发布' }}
            </el-tag>
          </div>
          <div class="text-xs text-ash space-y-1">
            <div class="flex justify-between">
              <span>{{ c.courseCode }}</span>
              <span>{{ c.semester }}</span>
            </div>
            <div v-if="c.classroom">地点：{{ c.classroom }}</div>
            <div class="flex justify-between">
              <span>选课：{{ c.enrolled || 0 }}/{{ c.capacity || 0 }}</span>
              <span>{{ c.credit }} 学分</span>
            </div>
          </div>
          <div v-if="expandedCourseId === c.id && courseClassMap[c.id]?.length" class="mt-3 pt-3 border-t border-soft">
            <div v-for="cc in courseClassMap[c.id]" :key="cc.id" class="flex items-center justify-between py-2 px-1 hover:bg-gray-50 rounded-lg">
              <span class="text-sm font-medium text-ink">{{ getClassName(cc.classId) }}</span>
              <div class="flex gap-2">
                <el-button size="small" @click.stop="viewClassStudents(c.id, cc.classId)">查看学生</el-button>
                
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="selectedCourseId" class="bg-white rounded-xl border border-soft p-5 mb-5">
      <div class="flex items-center justify-between mb-4">
        <div>
          <span class="text-sm font-semibold text-ink">{{ selectedCourse?.courseName }}</span>
          <span class="text-xs text-mist ml-2">{{ selectedCourse?.courseCode }}</span>
          <el-tag v-if="selectedCourse" :type="selectedCourse.status===1?'success':selectedCourse.status===2?'info':'warning'" size="small" class="ml-2">
            {{ selectedCourse.status===1?'选课中':selectedCourse.status===2?'已确认':'未发布' }}
          </el-tag>
        </div>
        <div class="flex items-center gap-2">
          <el-button size="small" @click="$router.push('/edu/course-students')">学生名单</el-button>
          <el-button size="small" @click="$router.push('/edu/grade-entry')">成绩录入</el-button>
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
      </div>

      <div v-if="studentsLoading" class="flex flex-col items-center justify-center py-10">
        <el-icon class="is-loading" :size="32"><Loading /></el-icon>
        <span class="text-mist text-sm mt-3">加载学生数据中...</span>
      </div>
      <div v-else-if="classGroups.length === 0" class="text-center text-mist py-6 text-sm">
        该课程暂无选课学生
      </div>
      <template v-else>
        <div class="mb-3 text-sm text-ash">
          共 <span class="font-semibold text-ink">{{ students.length }}</span> 名学生，
          来自 <span class="font-semibold text-ink">{{ classGroups.length }}</span> 个班级
        </div>

        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-3 mb-5">
          <div
            v-for="g in classGroups"
            :key="g.className"
            class="rounded-lg border border-soft p-3 cursor-pointer transition-all duration-150"
            :class="selectedClass === g.className ? 'border-blue-400 bg-blue-50' : 'hover:border-blue-200 hover:bg-gray-50'"
            @click="selectedClass = selectedClass === g.className ? '' : g.className"
          >
            <div class="text-sm font-medium text-ink">{{ g.className || '未分配班级' }}</div>
            <div class="text-xs text-mist mt-1">{{ g.count }} 名学生</div>
          </div>
        </div>

        <el-table :data="displayedStudents" border stripe>
          <el-table-column type="index" label="序号" width="55" />
          <el-table-column prop="studentName" label="学生姓名" width="110" />
          <el-table-column prop="studentUsername" label="学号" width="100" />
          <el-table-column prop="studentClassName" label="班级" width="120" />
          <el-table-column prop="studentDepartment" label="院系" width="120" />
          <el-table-column prop="studentPhone" label="联系方式" width="130" />
          <el-table-column prop="selectTime" label="选课时间">
            <template #default="{row}">{{ row.selectTime?.substring(0, 16) || '' }}</template>
          </el-table-column>
        </el-table>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getTeacherCourses, getCourseStudents, confirmCourse, getCourseClasses, getSemesters } from '@/api/edu'
import { getAllClasses } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const studentsLoading = ref(false)
const courses = ref<any[]>([])
const students = ref<any[]>([])
const semesters = ref<any[]>([])
const selectedCourseId = ref<number | null>(null)
const selectedSemester = ref('')
const searchKeyword = ref('')
const selectedClass = ref('')
const confirming = ref(false)

const router = useRouter()
const allClasses = ref<any[]>([])
const expandedCourseId = ref<number | null>(null)
const courseClassMap = ref<Record<number, any[]>>({})

const filteredCourses = computed(() => {
  let list = courses.value
  if (selectedSemester.value) {
    list = list.filter(c => c.semester === selectedSemester.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(c => c.courseName?.toLowerCase().includes(kw))
  }
  return list
})

const selectedCourse = computed(() => courses.value.find(c => c.id === selectedCourseId.value))

const classGroups = computed(() => {
  const map = new Map<string, number>()
  for (const s of students.value) {
    const cls = s.studentClassName || '未分配班级'
    map.set(cls, (map.get(cls) || 0) + 1)
  }
  return Array.from(map.entries())
    .map(([className, count]) => ({ className, count }))
    .sort((a, b) => a.className.localeCompare(b.className))
})

const displayedStudents = computed(() => {
  if (!selectedClass.value) return students.value
  return students.value.filter(s => (s.studentClassName || '未分配班级') === selectedClass.value)
})

async function fetchData() {
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

async function fetchSemesters() {
  try {
    const r = await getSemesters()
    semesters.value = r.data || []
  } catch {}
}

async function selectCourse(course: any) {
  selectedCourseId.value = course.id
  selectedClass.value = ''
  studentsLoading.value = true
  try {
    const r = await getCourseStudents(course.id)
    students.value = r.data || []
  } catch {
    ElMessage.error('获取学生名单失败')
  } finally {
    studentsLoading.value = false
  }
}

function filterCourses() {}

async function fetchAllClasses() {
  try {
    const r = await getAllClasses()
    allClasses.value = r.data || []
  } catch {}
}

async function expandCourse(courseId: number) {
  if (expandedCourseId.value === courseId) {
    expandedCourseId.value = null
    return
  }
  expandedCourseId.value = courseId
  if (!courseClassMap.value[courseId]) {
    const r = await getCourseClasses(courseId)
    courseClassMap.value[courseId] = r.data || []
  }
}

function getClassName(classId: number) {
  const c = allClasses.value.find((cls: any) => cls.id === classId)
  return c ? c.className : '班级' + classId
}



async function viewClassStudents(courseId: number, classId: number) {
  selectedCourseId.value = courseId
  studentsLoading.value = true
  try {
    const r = await getCourseStudents(courseId)
    students.value = r.data || []
    selectedClass.value = getClassName(classId)
  } catch {
    ElMessage.error('获取学生名单失败')
  } finally {
    studentsLoading.value = false
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
    await fetchData()
    selectedClass.value = ''
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  } finally {
    confirming.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchSemesters()
  fetchAllClasses()
})
</script>
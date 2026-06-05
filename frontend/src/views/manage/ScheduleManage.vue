<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>排课管理</span>
        <div style="display:flex;align-items:center;gap:8px">
          <span style="font-size:14px;color:#909399">学期</span>
          <el-select
            v-model="selectedSemester"
            style="width:220px"
            placeholder="选择学期"
            clearable
            @change="handleSemesterChange"
          >
            <el-option v-for="s in semesters" :key="s.id" :label="s.xqqc" :value="s.xqjc" />
          </el-select>
        </div>
      </div>
    </template>

    <div v-loading="loading" style="min-height:400px">
      <!-- Schedule Grid -->
      <div class="schedule-container">
        <table class="schedule-table">
          <thead>
            <tr>
              <th style="width:80px">节次</th>
              <th v-for="label in DAY_LABELS" :key="label" style="min-width:140px">{{ label }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(row, tIdx) in grid" :key="tIdx">
              <td class="time-cell">
                <div>{{ TIME_SLOT_LABELS[tIdx].label }}</div>
                <div class="time-sub">{{ TIME_SLOT_LABELS[tIdx].time }}</div>
              </td>
              <td
                v-for="(cell, dIdx) in row"
                :key="dIdx"
                class="schedule-cell"
                :class="{ 'is-empty': cell.items.length === 0 }"
                @click="handleCellClick(cell, dIdx + 1, tIdx + 1)"
              >
                <template v-if="cell.items.length > 0">
                  <div
                    v-for="item in sortGridItems(cell.items)"
                    :key="`${item.courseId}-${item.index}`"
                    class="schedule-item"
                    :class="{ 'is-active': item.courseId === selectedCourseId }"
                    :style="{ '--item-color': item.color }"
                    @click.stop="handleItemClick(item)"
                  >
                    <div class="item-name">{{ item.courseName }}</div>
                    <div class="item-info">{{ item.teacherName || item.classroom || '' }}</div>
                  </div>
                </template>
                <div v-else class="empty-hint">+ 点击添加</div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <!-- Course List -->
      <div class="course-section">
        <div class="course-section-title">课程列表</div>
        <div class="course-list">
          <div
            v-for="course in courses"
            :key="course.id"
            class="course-item"
            :class="{ 'is-selected': selectedCourseId === course.id }"
            @click="selectCourse(course.id)"
          >
            <div class="course-item-left">
              <span
                class="color-dot"
                :style="{ backgroundColor: getCourseColor(course.id) }"
              />
              <span class="course-item-name">{{ course.courseName }}</span>
              <span class="course-item-code">{{ course.courseCode }}</span>
            </div>
            <div class="course-item-right">
              <span class="course-item-teacher">教师: {{ course.teacherName || '未分配' }}</span>
              <el-tag
                :type="course.scheduled ? 'success' : 'info'"
                size="small"
                effect="plain"
              >
                {{ course.scheduled ? '已排课' : '未排课' }}
              </el-tag>
            </div>
          </div>
          <div v-if="courses.length === 0" class="empty-text">暂无课程数据</div>
        </div>
      </div>
    </div>

    <!-- Add Dialog -->
    <el-dialog v-model="addDialogVisible" title="添加排课" width="500px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="课程">
          <span style="font-weight:500">{{ dialogCourseName }}</span>
        </el-form-item>
        <el-form-item label="星期">
          <span>{{ DAY_LABELS[scheduleForm.day - 1] || '未知' }}</span>
        </el-form-item>
        <el-form-item label="节次">
          <span>{{ getTimeSlotLabel(scheduleForm.timeSlot) }}</span>
        </el-form-item>
        <el-form-item label="教室" required>
          <el-input v-model="scheduleForm.classroom" placeholder="如 教3-201" />
        </el-form-item>
        <el-form-item label="周次模式">
          <el-radio-group v-model="scheduleForm.weeks">
            <el-radio value="all">全周</el-radio>
            <el-radio value="odd">单周</el-radio>
            <el-radio value="even">双周</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="起止周">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input-number
                v-model="scheduleForm.startWeek"
                :min="1"
                :max="20"
                :precision="0"
                style="width:100%"
              />
            </el-col>
            <el-col :span="12">
              <el-input-number
                v-model="scheduleForm.endWeek"
                :min="scheduleForm.startWeek"
                :max="20"
                :precision="0"
                style="width:100%"
              />
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleAddSchedule">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- Edit Dialog -->
    <el-dialog v-model="editDialogVisible" title="编辑排课" width="500px" :close-on-click-modal="false">
      <el-form label-width="100px">
        <el-form-item label="课程">
          <span style="font-weight:500">{{ dialogCourseName }}</span>
        </el-form-item>
        <el-form-item label="星期">
          <span>{{ DAY_LABELS[scheduleForm.day - 1] || '未知' }}</span>
        </el-form-item>
        <el-form-item label="节次">
          <span>{{ getTimeSlotLabel(scheduleForm.timeSlot) }}</span>
        </el-form-item>
        <el-form-item label="教室" required>
          <el-input v-model="scheduleForm.classroom" placeholder="如 教3-201" />
        </el-form-item>
        <el-form-item label="周次模式">
          <el-radio-group v-model="scheduleForm.weeks">
            <el-radio value="all">全周</el-radio>
            <el-radio value="odd">单周</el-radio>
            <el-radio value="even">双周</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="起止周">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input-number
                v-model="scheduleForm.startWeek"
                :min="1"
                :max="20"
                :precision="0"
                style="width:100%"
              />
            </el-col>
            <el-col :span="12">
              <el-input-number
                v-model="scheduleForm.endWeek"
                :min="scheduleForm.startWeek"
                :max="20"
                :precision="0"
                style="width:100%"
              />
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="danger" :loading="submitting" @click="handleRemoveSchedule">
          删除
        </el-button>
        <el-button type="primary" :loading="submitting" @click="handleEditSchedule">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import {
  getCourses,
  getSemesters,
  addScheduleItem,
  updateScheduleItem,
  removeScheduleItem
} from '@/api/edu'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Course, Semester } from '@/types'

// ──────────────── Local Types ────────────────

interface ScheduleItemFull {
  day: number
  timeSlot: number
  weeks: string
  startWeek?: number
  endWeek?: number
  classroom?: string
}

interface CourseWithSchedule extends Course {
  parsedSchedule: ScheduleItemFull[]
  scheduled: boolean
}

interface GridScheduleItem {
  courseId: number
  courseCode: string
  courseName: string
  teacherName?: string
  classroom?: string
  weeks: string
  startWeek?: number
  endWeek?: number
  color: string
  index: number
  day: number
  timeSlot: number
}

interface GridCell {
  items: GridScheduleItem[]
}

// ──────────────── Constants ────────────────

const DAY_LABELS = ['周一', '周二', '周三', '周四', '周五']

const TIME_SLOT_LABELS = [
  { slot: 1, label: '第1节', time: '08:00' },
  { slot: 2, label: '第2节', time: '10:00' },
  { slot: 3, label: '第3节', time: '14:00' },
  { slot: 4, label: '第4节', time: '16:00' }
] as const

const COURSE_COLORS = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#909399',
  '#b37feb', '#36cfc9', '#ff85c0', '#ffc069', '#95de64'
]

// ──────────────── Reactive State ────────────────

const loading = ref(false)
const submitting = ref(false)
const semesters = ref<Semester[]>([])
const selectedSemester = ref('')
const courses = ref<CourseWithSchedule[]>([])
const selectedCourseId = ref<number | null>(null)
const grid = ref<GridCell[][]>([])
const addDialogVisible = ref(false)
const editDialogVisible = ref(false)

const scheduleForm = reactive({
  day: 1,
  timeSlot: 1,
  weeks: 'all' as string,
  startWeek: 1,
  endWeek: 20,
  classroom: '',
  courseId: 0,
  editIndex: null as number | null
})

// ──────────────── Computed ────────────────

const dialogCourseName = computed(() => {
  if (!scheduleForm.courseId) return ''
  const found = courses.value.find(c => c.id === scheduleForm.courseId)
  return found ? `${found.courseName} (${found.courseCode})` : ''
})

// ──────────────── Helper Functions ────────────────

function getCourseColor(courseId: number): string {
  return COURSE_COLORS[courseId % COURSE_COLORS.length]
}

function getTimeSlotLabel(timeSlot: number): string {
  const found = TIME_SLOT_LABELS.find(t => t.slot === timeSlot)
  return found ? `${found.label} (${found.time})` : `第${timeSlot}节`
}

function parseScheduleString(scheduleStr?: string): ScheduleItemFull[] {
  if (!scheduleStr) return []
  try {
    const parsed = JSON.parse(scheduleStr)
    if (Array.isArray(parsed)) {
      return parsed.map((item: Record<string, unknown>) => ({
        day: item.day as number,
        timeSlot: item.timeSlot as number,
        weeks: (item.weeks as string) || 'all',
        startWeek: item.startWeek as number | undefined,
        endWeek: item.endWeek as number | undefined,
        classroom: item.classroom as string | undefined
      }))
    }
    // Single object (backward compatibility)
    return [{
      day: parsed.day as number,
      timeSlot: parsed.timeSlot as number,
      weeks: (parsed.weeks as string) || 'all',
      startWeek: parsed.startWeek as number | undefined,
      endWeek: parsed.endWeek as number | undefined,
      classroom: parsed.classroom as string | undefined
    }]
  } catch {
    return []
  }
}

function sortGridItems(items: GridScheduleItem[]): GridScheduleItem[] {
  return [...items].sort((a, b) => a.courseId - b.courseId)
}

function buildGrid(): void {
  const newGrid: GridCell[][] = []
  for (let t = 0; t < 4; t++) {
    const row: GridCell[] = []
    for (let d = 0; d < 5; d++) {
      row.push({ items: [] })
    }
    newGrid.push(row)
  }

  for (const course of courses.value) {
    if (!course.parsedSchedule || course.parsedSchedule.length === 0) continue
    const color = getCourseColor(course.id)
    for (const [index, item] of course.parsedSchedule.entries()) {
      if (item.day >= 1 && item.day <= 5 && item.timeSlot >= 1 && item.timeSlot <= 4) {
        newGrid[item.timeSlot - 1][item.day - 1].items.push({
          courseId: course.id,
          courseCode: course.courseCode,
          courseName: course.courseName,
          teacherName: course.teacherName,
          classroom: item.classroom,
          weeks: item.weeks,
          startWeek: item.startWeek,
          endWeek: item.endWeek,
          color,
          index,
          day: item.day,
          timeSlot: item.timeSlot
        })
      }
    }
  }

  grid.value = newGrid
}

// ──────────────── Data Fetching ────────────────

async function fetchCourses(): Promise<void> {
  loading.value = true
  try {
    const params: Record<string, unknown> = { page: 1, size: 200 }
    if (selectedSemester.value) {
      params.semester = selectedSemester.value
    }
    const res = await getCourses(params)
    const records: Course[] = res.data?.records || res.data || []
    courses.value = records.map(c => {
      const parsed = parseScheduleString(c.schedule)
      return {
        ...c,
        parsedSchedule: parsed,
        scheduled: parsed.length > 0
      }
    })
    buildGrid()
  } catch {
    ElMessage.error('获取课程列表失败')
  } finally {
    loading.value = false
  }
}

async function fetchSemesters(): Promise<void> {
  try {
    const res = await getSemesters()
    semesters.value = res.data || []
    if (semesters.value.length > 0) {
      const active = semesters.value.find(s => s.status === 1)
      selectedSemester.value = active ? active.xqjc : semesters.value[0].xqjc
    }
  } catch {
    // Silently fail
  }
}

function handleSemesterChange(): void {
  fetchCourses()
}

// ──────────────── Course Selection ────────────────

function selectCourse(courseId: number): void {
  if (selectedCourseId.value === courseId) {
    selectedCourseId.value = null
  } else {
    selectedCourseId.value = courseId
  }
}

// ──────────────── Cell / Item Click Handlers ────────────────

function handleCellClick(cell: GridCell, day: number, timeSlot: number): void {
  if (cell.items.length > 0) return
  if (!selectedCourseId.value) {
    ElMessage.warning('请先在课程列表中选择一门课程')
    return
  }
  scheduleForm.day = day
  scheduleForm.timeSlot = timeSlot
  scheduleForm.weeks = 'all'
  scheduleForm.startWeek = 1
  scheduleForm.endWeek = 20
  scheduleForm.classroom = ''
  scheduleForm.courseId = selectedCourseId.value
  scheduleForm.editIndex = null
  addDialogVisible.value = true
}

function handleItemClick(item: GridScheduleItem): void {
  selectedCourseId.value = item.courseId
  scheduleForm.day = item.day
  scheduleForm.timeSlot = item.timeSlot
  scheduleForm.weeks = item.weeks
  scheduleForm.startWeek = item.startWeek || 1
  scheduleForm.endWeek = item.endWeek || 20
  scheduleForm.classroom = item.classroom || ''
  scheduleForm.courseId = item.courseId
  scheduleForm.editIndex = item.index
  editDialogVisible.value = true
}

// ──────────────── Add / Edit / Remove Schedule ────────────────

async function handleAddSchedule(): Promise<void> {
  if (!scheduleForm.classroom.trim()) {
    ElMessage.warning('请输入教室')
    return
  }
  if (scheduleForm.startWeek > scheduleForm.endWeek) {
    ElMessage.warning('起始周不能大于结束周')
    return
  }
  submitting.value = true
  try {
    await addScheduleItem(scheduleForm.courseId, {
      day: scheduleForm.day,
      timeSlot: scheduleForm.timeSlot,
      weeks: scheduleForm.weeks,
      startWeek: scheduleForm.startWeek,
      endWeek: scheduleForm.endWeek,
      classroom: scheduleForm.classroom.trim()
    })
    ElMessage.success('排课添加成功')
    addDialogVisible.value = false
    await fetchCourses()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error('添加排课失败')
  } finally {
    submitting.value = false
  }
}

async function handleEditSchedule(): Promise<void> {
  if (!scheduleForm.classroom.trim()) {
    ElMessage.warning('请输入教室')
    return
  }
  if (scheduleForm.startWeek > scheduleForm.endWeek) {
    ElMessage.warning('起始周不能大于结束周')
    return
  }
  if (scheduleForm.editIndex === null) return
  submitting.value = true
  try {
    await updateScheduleItem(scheduleForm.courseId, scheduleForm.editIndex, {
      day: scheduleForm.day,
      timeSlot: scheduleForm.timeSlot,
      weeks: scheduleForm.weeks,
      startWeek: scheduleForm.startWeek,
      endWeek: scheduleForm.endWeek,
      classroom: scheduleForm.classroom.trim()
    })
    ElMessage.success('排课修改成功')
    editDialogVisible.value = false
    await fetchCourses()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error('修改排课失败')
  } finally {
    submitting.value = false
  }
}

async function handleRemoveSchedule(): Promise<void> {
  if (scheduleForm.editIndex === null) return
  try {
    await ElMessageBox.confirm('确认删除该排课信息？', '删除确认', { type: 'warning' })
  } catch {
    // User cancelled the confirmation dialog
    return
  }
  submitting.value = true
  try {
    await removeScheduleItem(scheduleForm.courseId, scheduleForm.editIndex)
    ElMessage.success('排课删除成功')
    editDialogVisible.value = false
    await fetchCourses()
  } catch (e: unknown) {
    if (e === 'cancel') return
    ElMessage.error('删除排课失败')
  } finally {
    submitting.value = false
  }
}

// ──────────────── Lifecycle ────────────────

onMounted(async () => {
  await fetchSemesters()
  await fetchCourses()
})
</script>

<style scoped>
/* ────────────── Grid Table ────────────── */
.schedule-container {
  overflow-x: auto;
  margin-bottom: 24px;
}

.schedule-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.schedule-table th,
.schedule-table td {
  border: 1px solid #e4e7ed;
  text-align: center;
  vertical-align: middle;
  padding: 4px;
}

.schedule-table th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  height: 40px;
  font-size: 14px;
}

.time-cell {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
  font-size: 13px;
  width: 80px;
  height: 90px;
}

.time-sub {
  font-weight: 400;
  font-size: 11px;
  color: #909399;
  margin-top: 2px;
}

.schedule-cell {
  background: #fff;
  cursor: pointer;
  min-height: 90px;
  height: 90px;
  vertical-align: top;
  padding: 4px;
  transition: background-color 0.2s;
}

.schedule-cell.is-empty:hover {
  background: #ecf5ff;
}

.schedule-cell.is-empty .empty-hint {
  color: #c0c4cc;
  font-size: 12px;
  line-height: 82px;
  user-select: none;
}

/* ────────────── Schedule Item Cards ────────────── */
.schedule-item {
  background: #f0f9eb;
  border-left: 4px solid var(--item-color, #67c23a);
  border-radius: 4px;
  padding: 2px 6px;
  margin-bottom: 3px;
  cursor: pointer;
  transition: box-shadow 0.2s, transform 0.1s;
  text-align: left;
  user-select: none;
}

.schedule-item:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  transform: translateY(-1px);
}

.schedule-item.is-active {
  box-shadow: 0 0 0 2px var(--item-color, #67c23a);
}

.item-name {
  font-size: 12px;
  font-weight: 600;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-info {
  font-size: 10px;
  color: #909399;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ────────────── Course List ────────────── */
.course-section {
  margin-top: 8px;
}

.course-section-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
}

.course-list {
  max-height: 320px;
  overflow-y: auto;
}

.course-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 4px;
}

.course-item:hover {
  background-color: #f5f7fa;
}

.course-item.is-selected {
  background-color: #ecf5ff;
  border: 1px solid #b3d8ff;
}

.course-item-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.color-dot {
  display: inline-block;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}

.course-item-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.course-item-code {
  font-size: 12px;
  color: #909399;
  flex-shrink: 0;
}

.course-item-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.course-item-teacher {
  font-size: 12px;
  color: #909399;
}

.empty-text {
  text-align: center;
  color: #c0c4cc;
  padding: 24px 0;
  font-size: 14px;
}
</style>
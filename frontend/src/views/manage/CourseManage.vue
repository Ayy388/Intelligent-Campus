<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>课程管理</span>
        <div style="display:flex;gap:8px">
          <el-upload
            :show-file-list="false"
            :before-upload="handleImport"
            accept=".csv"
          >
            <el-button>导入课程</el-button>
          </el-upload>
          <el-button type="primary" @click="showDialog()">添加课程</el-button>
        </div>
      </div>
    </template>
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="courseCode" label="编号" width="100" />
      <el-table-column prop="courseName" label="名称" />
      <el-table-column prop="teacherName" label="授课教师" width="100" />
      <el-table-column prop="classroom" label="教室" width="120" />
      <el-table-column prop="credit" label="学分" width="60" />
      <el-table-column prop="semester" label="学期" width="100" />
      <el-table-column label="选课" width="100">
        <template #default="{row}">{{ row.enrolled || 0 }} / {{ row.capacity || 0 }}</template>
      </el-table-column>
      <el-table-column label="类型" width="80">
        <template #default="{row}">
          <el-tag :type="row.courseType==='required'?'primary':'warning'" size="small">
            {{ row.courseType==='required'?'必修':'选修' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">{{ row.status===1?'选课中':row.status===2?'已确认':'未发布' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="220">
        <template #default="{row}">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-dropdown trigger="click" @command="(cmd:string) => handleAction(cmd, row)" style="margin-left:6px">
            <el-button size="small">
              更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                <el-dropdown-item v-if="row.courseType==='required' && row.status===0" command="assign">下达教学任务</el-dropdown-item>
                <el-dropdown-item v-if="row.courseType==='elective' && row.status===1" command="confirm">确认开课</el-dropdown-item>
                <el-dropdown-item v-if="row.courseType==='elective' && row.status===1" command="cancel">取消开课</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑':'添加'" width="600px">
      <el-form :model="form">
        <el-form-item label="课程编号"><el-input v-model="form.courseCode" placeholder="如 CS101" /></el-form-item>
        <el-form-item label="课程名称"><el-input v-model="form.courseName" placeholder="如 Java程序设计" /></el-form-item>
        <el-form-item label="授课教师">
          <el-select v-model="form.teacherId" placeholder="选择教师" clearable class="w-full">
            <el-option v-for="t in teachers" :key="t.id" :label="t.realName" :value="t.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学分"><el-input-number v-model="form.credit" :min="0" :step="0.5" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="form.hours" :min="0" /></el-form-item>
        <el-form-item label="学期">
          <el-select v-model="form.semester" placeholder="选择学期" clearable class="w-full">
            <el-option v-for="s in semesters" :key="s.id" :label="s.xqqc" :value="s.xqjc" />
          </el-select>
        </el-form-item>
        <el-form-item label="教室"><el-input v-model="form.classroom" placeholder="如 教3-201" /></el-form-item>
        <el-form-item label="上课时间">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-select v-model="scheduleForm.day" placeholder="星期几" @change="updateSchedule">
                <el-option :value="1" label="周一" />
                <el-option :value="2" label="周二" />
                <el-option :value="3" label="周三" />
                <el-option :value="4" label="周四" />
                <el-option :value="5" label="周五" />
                <el-option :value="6" label="周六" />
                <el-option :value="7" label="周日" />
              </el-select>
            </el-col>
            <el-col :span="12">
              <el-select v-model="scheduleForm.timeSlot" placeholder="第几节" @change="updateSchedule">
                <el-option :value="1" label="第1-2节" />
                <el-option :value="2" label="第3-4节" />
                <el-option :value="3" label="第5-6节" />
                <el-option :value="4" label="第7-8节" />
                <el-option :value="5" label="第9-10节" />
              </el-select>
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="周数范围">
          <el-row :gutter="10">
            <el-col :span="12">
              <el-input-number v-model="form.startWeek" :min="1" :max="20" placeholder="起始周" />
            </el-col>
            <el-col :span="12">
              <el-input-number v-model="form.endWeek" :min="1" :max="20" placeholder="结束周" />
            </el-col>
          </el-row>
        </el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="0" /></el-form-item>
        <el-form-item label="课程描述"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="课程简介" /></el-form-item>
        <el-form-item label="课程类型">
          <el-radio-group v-model="form.courseType">
            <el-radio value="required">必修</el-radio>
            <el-radio value="elective">选修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="form.courseType==='elective'" label="最低开课人数">
          <el-input-number v-model="form.minStudents" :min="1" />
        </el-form-item>
        <el-form-item v-if="form.courseType==='elective'" label="选课截止时间">
          <el-date-picker v-model="form.enrollEnd" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" />
        </el-form-item>
        <el-form-item label="目标班级">
          <el-select v-model="selectedClasses" multiple placeholder="选择班级（不选则不限制）">
            <el-option v-for="c in allClasses" :key="c.id" :label="c.className" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option :value="0" label="未发布（不可选课）" /><el-option :value="1" label="开放选课" /><el-option :value="2" label="已确认（不可选课/退课）" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
    <el-dialog v-model="assignDialogVisible" title="下达教学任务 - 选择目标班级" width="500px">
      <el-select v-model="assignClassIds" multiple style="width:100%">
        <el-option v-for="c in allClasses" :key="c.id" :label="c.className" :value="c.id" />
      </el-select>
      <template #footer>
        <el-button @click="assignDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="doAssign">确认分配</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { getCourses, addCourse, updateCourse, deleteCourse, importCourses, getSemesters, setCourseClasses, confirmOpening, cancelOpening, assignCourseClasses } from '@/api/edu'
import { getAllClasses } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'
import type { Course, Semester, ClassInfo, User } from '@/types'

const tableData = ref<Course[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const teachers = ref<User[]>([])
const semesters = ref<Semester[]>([])
const allClasses = ref<ClassInfo[]>([])
const selectedClasses = ref<number[]>([])
const assignDialogVisible = ref(false)
const assignCourseId = ref<number | null>(null)
const assignClassIds = ref<number[]>([])
const form = reactive({
    courseCode: '',
    courseName: '',
    teacherId: null as number | null,
    credit: 0,
    hours: 0,
    semester: '',
    classroom: '',
    schedule: '',
    startWeek: 1,
    endWeek: 20,
    capacity: 0,
    description: '',
    status: 0,
    courseType: 'required' as string,
    minStudents: 1,
    enrollEnd: ''
})

const scheduleForm = reactive({
    day: null as number | null,
    timeSlot: null as number | null
})

function parseSchedule() {
  if (form.schedule) {
    try {
      const s = JSON.parse(form.schedule)
      scheduleForm.day = s.day || null
      scheduleForm.timeSlot = s.timeSlot || null
    } catch {
      scheduleForm.day = null
      scheduleForm.timeSlot = null
    }
  } else {
    scheduleForm.day = null
    scheduleForm.timeSlot = null
  }
}

function updateSchedule() {
  if (scheduleForm.day && scheduleForm.timeSlot) {
    form.schedule = JSON.stringify({ day: scheduleForm.day, timeSlot: scheduleForm.timeSlot })
  } else {
    form.schedule = ''
  }
}

async function fetch() {
  loading.value = true
  const r = await getCourses({ page: 1, size: 100 })
  tableData.value = r.data.records
  loading.value = false
}

async function fetchTeachers() {
  const r = await request.get('/sys/users', { params: { page: 1, size: 200 } })
  teachers.value = (r.data.records || []).filter((u: User) => u.roleName === '教师')
}

async function fetchSemesters() {
  try {
    const r = await getSemesters()
    semesters.value = r.data || []
  } catch {}
}

async function fetchAllClasses() {
  try {
    const r = await getAllClasses()
    allClasses.value = r.data || []
  } catch {}
}

function showDialog(row?: Course) {
  editId.value = row?.id || null
  selectedClasses.value = []
  Object.assign(form, row ? { ...row, courseType: row.courseType || 'required', minStudents: row.minStudents || 1, enrollEnd: row.enrollEnd || '' } : {
    courseCode: '',
    courseName: '',
    teacherId: null,
    credit: 0,
    hours: 0,
    semester: '',
    classroom: '',
    schedule: '',
    startWeek: 1,
    endWeek: 20,
    capacity: 0,
    description: '',
    status: 0,
    courseType: 'required',
    minStudents: 1,
    enrollEnd: ''
  })
  parseSchedule()
  dialogVisible.value = true
}

async function save() {
  if (!form.courseCode.trim() || !form.courseName.trim()) {
    ElMessage.warning('编号和名称不能为空')
    return
  }
  let courseId: number
  if (editId.value) {
    await updateCourse(editId.value, form)
    courseId = editId.value
  } else {
    const res = await addCourse(form)
    courseId = res.data?.id || res.data
  }
  if (selectedClasses.value.length > 0) {
    const classes = selectedClasses.value.map((classId: number) => ({
      courseId, classId, isRequired: form.courseType === 'required' ? 1 : 0
    }))
    await setCourseClasses(courseId, classes)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetch()
}

async function del(id: number) {
  try {
    await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
    await deleteCourse(id)
    ElMessage.success('删除成功')
    fetch()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

async function handleImport(file: File) {
  try {
    loading.value = true
    const res = await importCourses(file)
    ElMessage.success(res.data || '导入成功')
    fetch()
  } catch (e) {
    ElMessage.error('导入失败')
  } finally {
    loading.value = false
  }
  return false
}

function openAssignDialog(courseId: number) {
  assignCourseId.value = courseId
  assignClassIds.value = []
  assignDialogVisible.value = true
}

async function doAssign() {
  if (!assignCourseId.value || assignClassIds.value.length === 0) return
  await assignCourseClasses(assignCourseId.value, assignClassIds.value)
  ElMessage.success('教学任务已下达')
  assignDialogVisible.value = false
  fetch()
}

async function handleAction(cmd: string, row: Course) {
  if (cmd === 'delete') {
    try {
      await ElMessageBox.confirm('确认删除该课程？', '提示', { type: 'warning' })
      await deleteCourse(row.id)
      ElMessage.success('删除成功')
      fetch()
    } catch { /* cancelled */ }
  } else if (cmd === 'assign') {
    openAssignDialog(row.id)
  } else if (cmd === 'confirm') {
    await confirmOpening(row.id)
    ElMessage.success('课程已确认开课')
    fetch()
  } else if (cmd === 'cancel') {
    await cancelOpening(row.id)
    ElMessage.success('课程已取消')
    fetch()
  }
}

async function handleConfirm(courseId: number) {
  await confirmOpening(courseId)
  ElMessage.success('课程已确认开课')
  fetch()
}

async function handleCancel(courseId: number) {
  await cancelOpening(courseId)
  ElMessage.success('课程已取消')
  fetch()
}

onMounted(() => { fetch(); fetchTeachers(); fetchSemesters(); fetchAllClasses() })
</script>

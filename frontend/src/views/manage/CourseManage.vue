<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>课程管理</span><el-button type="primary" @click="showDialog()">添加课程</el-button></div></template>
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
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">{{ row.status===1?'可选':row.status===2?'结束':'未开放' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template>
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
        <el-form-item label="学期"><el-input v-model="form.semester" placeholder="如 2026-春" /></el-form-item>
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
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="0" /></el-form-item>
        <el-form-item label="课程描述"><el-input v-model="form.description" type="textarea" :rows="2" placeholder="课程简介" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option :value="0" label="未开放" /><el-option :value="1" label="开放选课" /><el-option :value="2" label="已结束" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { getCourses, addCourse, updateCourse, deleteCourse } from '@/api/edu'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const teachers = ref<any[]>([])
const form = reactive({
  courseCode: '',
  courseName: '',
  teacherId: null as number | null,
  credit: 0,
  hours: 0,
  semester: '',
  classroom: '',
  schedule: '',
  capacity: 0,
  description: '',
  status: 0
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
  teachers.value = (r.data.records || []).filter((u: any) => u.roleName === '教师')
}

function showDialog(row?: any) {
  editId.value = row?.id || null
  Object.assign(form, row ? { ...row } : {
    courseCode: '',
    courseName: '',
    teacherId: null,
    credit: 0,
    hours: 0,
    semester: '',
    classroom: '',
    schedule: '',
    capacity: 0,
    description: '',
    status: 0
  })
  parseSchedule()
  dialogVisible.value = true
}

async function save() {
  if (!form.courseCode.trim() || !form.courseName.trim()) {
    ElMessage.warning('编号和名称不能为空')
    return
  }
  if (editId.value) {
    await updateCourse(editId.value, form)
  } else {
    await addCourse(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetch()
}

async function del(id: number) {
  await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
  await deleteCourse(id)
  ElMessage.success('删除成功')
  fetch()
}

onMounted(() => { fetch(); fetchTeachers() })
</script>

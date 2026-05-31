<template>
  <div>
    <el-card>
      <template #header><span>在线选课</span></template>
      <el-table :data="courses" v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="教师" width="100" />
        <el-table-column prop="credit" label="学分" width="60" />
        <el-table-column prop="classroom" label="教室" width="120" />
        <el-table-column label="课程类型" width="80">
          <template #default="{row}">
            <el-tag :type="row.courseType==='elective'?'warning':'primary'" size="small">
              {{ row.courseType==='elective'?'选修':'必修' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="报名情况" width="140">
          <template #default="{row}">
            <div class="text-xs leading-relaxed">
              <div>最低开课: {{ row.minStudents ?? '-' }}人</div>
              <div>已报名: {{ row.enrolled ?? 0 }}人</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'选课中':'已确认' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button
              type="primary" size="small"
              @click="doEnroll(row)"
              :disabled="row.enrolled>=row.capacity || row.status!==1"
            >报名</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card style="margin-top:16px">
      <template #header><span>我的选课</span></template>
      <el-table :data="mySelections">
        <el-table-column prop="courseName" label="课程" />
        <el-table-column prop="semester" label="学期" width="100" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag v-if="row.courseStatus === 2" type="info" size="small">已确认</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button type="danger" size="small" @click="doDrop(row)" :disabled="row.courseStatus === 2">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getAvailableCourses, enrollCourse, getCourseClasses, getSelections, dropCourse } from '@/api/edu'
import { ElMessage } from 'element-plus'
const courses = ref([])
const mySelections = ref([])
const loading = ref(false)
async function fetchData() {
  loading.value = true
  const [r1, r2] = await Promise.all([getAvailableCourses(), getSelections()])
  courses.value = r1.data || []
  mySelections.value = r2.data
  loading.value = false
}
async function doEnroll(course: any) {
  try { await enrollCourse(course.id); ElMessage.success('报名成功'); fetchData() }
  catch (e: any) { ElMessage.error(e.message) }
}
async function doDrop(sel: any) {
  try { await dropCourse(sel.id); ElMessage.success('退课成功'); fetchData() }
  catch (e: any) { ElMessage.error(e.message) }
}
onMounted(fetchData)
</script>

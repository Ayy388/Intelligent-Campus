<template>
  <div>
    <el-card>
      <template #header><span>在线选课</span></template>
      <el-table :data="courses" v-loading="loading">
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="教师" width="100" />
        <el-table-column prop="credit" label="学分" width="60" />
        <el-table-column prop="classroom" label="教室" width="120" />
        <el-table-column label="容量" width="100">
          <template #default="{row}">{{ row.enrolled }}/{{ row.capacity }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button type="primary" size="small" @click="doSelect(row)" :disabled="row.enrolled>=row.capacity">选课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <el-card style="margin-top:16px">
      <template #header><span>我的选课</span></template>
      <el-table :data="mySelections">
        <el-table-column prop="courseName" label="课程" />
        <el-table-column prop="semester" label="学期" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{row}">
            <el-button type="danger" size="small" @click="doDrop(row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourses, getSelections, selectCourse, dropCourse } from '@/api/edu'
import { ElMessage } from 'element-plus'
const courses = ref([])
const mySelections = ref([])
const loading = ref(false)
async function fetchData() {
  loading.value = true
  const [r1, r2] = await Promise.all([getCourses({ page: 1, size: 100 }), getSelections()])
  courses.value = r1.data.records.filter((c: any) => c.status === 1)
  mySelections.value = r2.data
  loading.value = false
}
async function doSelect(course: any) {
  try { await selectCourse(course.id, course.semester || '2026-春'); ElMessage.success('选课成功'); fetchData() }
  catch (e: any) { ElMessage.error(e.message) }
}
async function doDrop(sel: any) {
  try { await dropCourse(sel.id); ElMessage.success('退课成功'); fetchData() }
  catch (e: any) { ElMessage.error(e.message) }
}
onMounted(fetchData)
</script>

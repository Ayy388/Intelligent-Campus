<template>
  <div>
    <el-card>
      <template #header><span>课程列表</span></template>
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="courseCode" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="semester" label="学期" width="100" />
        <el-table-column prop="classroom" label="教室" width="120" />
        <el-table-column label="状态" width="100">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">
              {{ row.status===1?'可选':row.status===2?'已结束':'未开放' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetchData" />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/edu'
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
async function fetchData() {
  loading.value = true
  const res = await getCourses({ page: page.value, size: 10 })
  tableData.value = res.data.records
  total.value = res.data.total
  loading.value = false
}
onMounted(fetchData)
</script>

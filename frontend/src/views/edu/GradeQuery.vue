<template>
  <el-card>
    <template #header><span>成绩查询</span></template>
    <el-table :data="grades" v-loading="loading">
      <el-table-column prop="courseName" label="课程" />
      <el-table-column prop="score" label="成绩" width="100" />
      <el-table-column prop="gradeType" label="类型" width="80" />
      <el-table-column prop="semester" label="学期" width="100" />
      <el-table-column prop="remark" label="备注" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyGrades } from '@/api/edu'
const grades = ref([])
const loading = ref(false)
onMounted(async () => { loading.value = true; const r = await getMyGrades(); grades.value = r.data; loading.value = false })
</script>

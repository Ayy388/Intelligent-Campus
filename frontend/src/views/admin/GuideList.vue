<template>
  <el-card>
    <template #header><span>办事指南</span></template>
    <el-table :data="guides" v-loading="loading" @row-click="(row:any) => ElMessage.info(row.title)">
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getGuides } from '@/api/admin'
import { ElMessage } from 'element-plus'
const guides = ref([])
const loading = ref(false)
onMounted(async () => { loading.value = true; const r = await getGuides({ page: 1, size: 20 }); guides.value = r.data.records; loading.value = false })
</script>

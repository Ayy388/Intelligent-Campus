<template>
  <el-card>
    <template #header><span>请假审批</span></template>
    <el-table :data="leaves" v-loading="loading">
      <el-table-column prop="studentName" label="学生" width="100" />
      <el-table-column prop="leaveType" label="类型" width="80" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <template v-if="row.status===0">
            <el-button type="success" size="small" @click="approve(row.id,1)">通过</el-button>
            <el-button type="danger" size="small" @click="approve(row.id,2)">驳回</el-button>
          </template>
          <el-tag v-else :type="row.status===1?'success':'danger'">{{ row.status===1?'已通过':'已驳回' }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLeaves, approveLeave } from '@/api/admin'
import { ElMessage } from 'element-plus'
const leaves = ref([])
const loading = ref(false)
async function fetch() { loading.value = true; const r = await getLeaves({ page: 1, size: 50 }); leaves.value = r.data.records; loading.value = false }
async function approve(id: number, status: number) { await approveLeave(id, status); ElMessage.success('操作成功'); fetch() }
onMounted(fetch)
</script>

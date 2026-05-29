<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>请假申请</span><el-button type="primary" @click="dialogVisible=true">提交申请</el-button></div></template>
    <el-table :data="leaves" v-loading="loading">
      <el-table-column prop="leaveType" label="类型" width="80" />
      <el-table-column prop="startTime" label="开始时间" width="180" />
      <el-table-column prop="endTime" label="结束时间" width="180" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column label="状态" width="100">
        <template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'danger':'warning'">{{ row.status===1?'通过':row.status===2?'驳回':'待审批' }}</el-tag></template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" title="提交请假">
      <el-form :model="form">
        <el-form-item label="类型"><el-select v-model="form.leaveType"><el-option label="事假" value="事假" /><el-option label="病假" value="病假" /><el-option label="公假" value="公假" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">提交</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLeaves, applyLeave } from '@/api/admin'
import { ElMessage } from 'element-plus'
const leaves = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ leaveType: '', startTime: '', endTime: '', reason: '' })
async function fetch() { loading.value = true; const r = await getLeaves({ page: 1, size: 50 }); leaves.value = r.data.records; loading.value = false }
async function submit() { await applyLeave(form); ElMessage.success('提交成功'); dialogVisible.value = false; fetch() }
onMounted(fetch)
</script>

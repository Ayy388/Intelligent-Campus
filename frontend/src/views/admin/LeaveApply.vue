<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>请假申请</span><el-button type="primary" @click="dialogVisible=true">提交申请</el-button></div></template>
    <el-table :data="leaves" v-loading="loading" @row-click="showDetail" style="cursor:pointer">
      <el-table-column prop="leaveType" label="类型" width="80" />
      <el-table-column label="开始时间" width="180">
        <template #default="{row}">{{ row.startTime?.substring(0,16) }}</template>
      </el-table-column>
      <el-table-column label="结束时间" width="180">
        <template #default="{row}">{{ row.endTime?.substring(0,16) }}</template>
      </el-table-column>
      <el-table-column prop="reason" label="原因" />
      <el-table-column label="状态" width="100">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':row.status===2?'danger':'warning'">
            {{ row.status===1?'通过':row.status===2?'驳回':'待审批' }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="detailVisible" title="请假详情" width="500px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="类型">{{ detail.leaveType }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime?.substring(0,16) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime?.substring(0,16) }}</el-descriptions-item>
        <el-descriptions-item label="原因">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="证明材料">
          <span v-if="detail.attachment">{{ detail.attachment }}</span>
          <span v-else class="text-mist">无</span>
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">{{ detail.applyTime?.substring(0,16) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="detail.status===1?'success':detail.status===2?'danger':'warning'" size="small">
            {{ detail.status===1?'已通过':detail.status===2?'已驳回':'待审批' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.status===2 && detail.rejectReason" label="驳回原因">
          <span style="color:#f56c6c">{{ detail.rejectReason }}</span>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.approveTime" label="审批时间">{{ detail.approveTime?.substring(0,16) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer><el-button @click="detailVisible=false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialogVisible" title="提交请假">
      <el-form :model="form">
        <el-form-item label="类型"><el-select v-model="form.leaveType"><el-option label="事假" value="事假" /><el-option label="病假" value="病假" /><el-option label="公假" value="公假" /></el-select></el-form-item>
        <el-form-item label="开始时间"><el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="结束时间"><el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" /></el-form-item>
        <el-form-item label="原因"><el-input v-model="form.reason" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="证明材料"><el-input v-model="form.attachment" placeholder="附件链接（可选）" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">提交</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLeaves, getLeave, applyLeave } from '@/api/admin'
import { ElMessage } from 'element-plus'

const leaves = ref<any[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ leaveType: '', startTime: '', endTime: '', reason: '', attachment: '' })

const detailVisible = ref(false)
const detail = ref<any>(null)

async function fetch() { loading.value = true; const r = await getLeaves({ page: 1, size: 50 }); leaves.value = r.data.records; loading.value = false }

async function submit() { await applyLeave(form); ElMessage.success('提交成功'); dialogVisible.value = false; fetch() }

async function showDetail(row: any) {
  try {
    const r = await getLeave(row.id)
    detail.value = r.data
    detailVisible.value = true
  } catch {
    ElMessage.error('获取请假详情失败')
  }
}

onMounted(fetch)
</script>
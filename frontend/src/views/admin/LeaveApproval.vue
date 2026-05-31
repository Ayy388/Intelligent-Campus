<template>
  <div>
    <span class="text-lg font-bold text-ink block mb-4">请假审批</span>
    <div v-if="leaves.length===0" class="bg-white rounded-xl border border-soft p-10 text-center text-mist text-sm">暂无请假记录</div>
    <div v-for="l in leaves" :key="l.id" class="bg-white rounded-xl border border-soft p-4 mb-3 cursor-pointer hover:shadow-sm transition-shadow" @click="showDetail(l)">
      <div class="flex items-start justify-between">
        <div class="flex-1">
          <div class="flex items-center gap-2 mb-1">
            <span class="text-sm font-semibold text-ink">{{ l.studentName || '学生ID:' + l.studentId }}</span>
            <el-tag size="small">{{ l.leaveType }}</el-tag>
            <el-tag v-if="l.status===0" size="small" type="warning">待审批</el-tag>
            <el-tag v-else :type="l.status===1?'success':'danger'" size="small">{{ l.status===1?'已通过':'已驳回' }}</el-tag>
          </div>
          <div class="text-xs text-mist">{{ l.startTime?.substring(0,16) }} ~ {{ l.endTime?.substring(0,16) }}</div>
          <div class="text-sm text-steel mt-1">{{ l.reason }}</div>
          <div v-if="l.attachment" class="text-xs text-mist mt-1">附件: {{ l.attachment }}</div>
          <div v-if="l.status===2 && l.rejectReason" class="text-xs text-red-400 mt-1">驳回原因: {{ l.rejectReason }}</div>
        </div>
        <div v-if="l.status===0" class="flex gap-2 shrink-0 ml-4">
          <button @click.stop="doApprove(l.id,1)" class="px-3 py-1 text-xs bg-ink text-white rounded-lg hover:bg-steel transition-colors">通过</button>
          <button @click.stop="startReject(l.id)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">驳回</button>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="请假详情" width="520px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="学生姓名">{{ detail.studentName || '学生ID:' + detail.studentId }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">{{ detail.leaveType }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime?.substring(0,16) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime?.substring(0,16) }}</el-descriptions-item>
        <el-descriptions-item label="请假原因">{{ detail.reason }}</el-descriptions-item>
        <el-descriptions-item label="证明材料">
          <span v-if="detail.attachment">{{ detail.attachment }}</span>
          <span v-else class="text-ash">无</span>
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
      <template #footer>
        <div v-if="detail?.status===0" class="flex gap-2 justify-end">
          <el-button type="primary" @click="doApproveFromDetail(detail.id, 1)">通过</el-button>
          <el-button @click="startRejectFromDetail(detail.id)">驳回</el-button>
        </div>
        <el-button v-else @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回申请" width="400px">
      <el-form><el-form-item label="驳回原因"><el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" /></el-form-item></el-form>
      <template #footer><el-button @click="rejectVisible=false">取消</el-button><el-button type="primary" @click="doReject">确认驳回</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getLeaves, getLeave, approveLeave } from '@/api/admin'
import { ElMessage } from 'element-plus'

const leaves = ref<any[]>([])
const loading = ref(false)
const rejectVisible = ref(false)
const rejectId = ref<number | null>(null)
const rejectReason = ref('')

const detailVisible = ref(false)
const detail = ref<any>(null)

async function fetch() { loading.value = true; const r = await getLeaves({ page: 1, size: 50 }); leaves.value = r.data.records; loading.value = false }

async function doApprove(id: number, status: number) { await approveLeave(id, status); ElMessage.success('操作成功'); detailVisible.value = false; fetch() }

async function doApproveFromDetail(id: number, status: number) {
  await approveLeave(id, status)
  ElMessage.success('操作成功')
  detailVisible.value = false
  fetch()
}

function startReject(id: number) { rejectId.value = id; rejectReason.value = ''; rejectVisible.value = true }

function startRejectFromDetail(id: number) {
  rejectId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() { await approveLeave(rejectId.value!, 2, rejectReason.value); ElMessage.success('已驳回'); rejectVisible.value = false; detailVisible.value = false; fetch() }

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
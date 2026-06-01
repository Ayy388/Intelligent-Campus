<template>
  <div>
    <div class="animate__animated animate__fadeInDown">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md mb-5">
        <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center shadow-sm">
              <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                <polyline points="14 2 14 8 20 8"/>
                <line x1="16" y1="13" x2="8" y2="13"/>
                <line x1="16" y1="17" x2="8" y2="17"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-bold text-ink">请假审批</h3>
              <p class="text-xs text-mist mt-0.5">审核和处理学生的请假申请</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-mist bg-wash px-3 py-1.5 rounded-lg">
              待处理
              <span class="font-semibold text-ink ml-1">{{ leaves.filter(l => l.status === 0).length }}</span>
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20">
      <svg class="animate-spin h-8 w-8 text-gray-300" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <div v-else-if="leaves.length === 0" class="animate__animated animate__fadeInUp bg-white rounded-xl shadow-sm border border-gray-100 p-10">
      <div class="flex flex-col items-center justify-center py-10">
        <svg class="w-16 h-16 text-gray-200 mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
          <polyline points="14 2 14 8 20 8"/>
          <line x1="16" y1="13" x2="8" y2="13"/>
        </svg>
        <p class="text-sm text-mist">暂无请假记录</p>
      </div>
    </div>

    <div v-else class="space-y-3">
      <div
        v-for="(l, i) in leaves"
        :key="l.id"
        class="animate__animated animate__fadeInUp bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md hover:-translate-y-0.5 cursor-pointer"
        :style="{ animationDelay: `${0.05 * i}s` }"
        @click="showDetail(l)"
      >
        <div class="p-5">
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2.5 mb-2">
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-xs font-bold shrink-0 shadow-sm">
                  {{ (l.studentName || '?').charAt(0) }}
                </div>
                <div>
                  <span class="text-sm font-semibold text-ink">{{ l.studentName || '学生ID:' + l.studentId }}</span>
                </div>
                <span class="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="leaveTypeClass(l.leaveType)"
                >
                  <span class="w-1.5 h-1.5 rounded-full" :class="leaveTypeDot(l.leaveType)"></span>
                  {{ l.leaveType }}
                </span>
                <span
                  class="inline-flex items-center px-3 py-0.5 rounded-full text-xs font-semibold shadow-sm"
                  :class="statusBadgeClass(l.status)"
                >
                  <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="statusDotClass(l.status)"></span>
                  {{ l.status === 1 ? '已通过' : l.status === 2 ? '已驳回' : '待审批' }}
                </span>
              </div>
              <div class="flex items-center gap-4 text-xs text-mist ml-10">
                <span class="flex items-center gap-1">
                  <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                    <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                    <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
                  </svg>
                  {{ l.startTime?.substring(0,16) }} ~ {{ l.endTime?.substring(0,16) }}
                </span>
              </div>
              <div class="text-sm text-steel mt-2 ml-10 line-clamp-2">{{ l.reason }}</div>
              <div v-if="l.attachment" class="flex items-center gap-1 text-xs text-blue-500 mt-1.5 ml-10">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/>
                </svg>
                {{ l.attachment.split(',').length }} 个附件
              </div>
              <div v-if="l.status === 2 && l.rejectReason" class="flex items-center gap-1 text-xs text-red-400 mt-1.5 ml-10">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
                </svg>
                驳回原因: {{ l.rejectReason }}
              </div>
            </div>
            <div v-if="l.status === 0" class="flex gap-2 shrink-0" @click.stop>
              <button
                @click="doApprove(l.id, 1)"
                class="inline-flex items-center gap-1 px-4 py-2 text-xs font-semibold text-white rounded-lg shadow-sm transition-all duration-200 bg-gradient-to-r from-emerald-500 to-teal-500 hover:from-emerald-600 hover:to-teal-600 hover:shadow-md active:scale-95"
              >
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                通过
              </button>
              <button
                @click="startReject(l.id)"
                class="inline-flex items-center gap-1 px-4 py-2 text-xs font-semibold rounded-lg border border-gray-200 transition-all duration-200 text-ash hover:text-red-500 hover:border-red-200 hover:bg-red-50 active:scale-95"
              >
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
                </svg>
                驳回
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" width="560px" top="5vh" class="leave-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
            </svg>
          </div>
          <span class="text-base font-bold text-ink">请假详情</span>
        </div>
      </template>
      <div v-if="detail" class="px-2">
        <div class="grid grid-cols-2 gap-4 mb-5">
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">学生姓名</div>
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-[10px] font-bold shrink-0">
                {{ (detail.studentName || '?').charAt(0) }}
              </div>
              <span class="text-sm font-semibold text-ink">{{ detail.studentName || '学生ID:' + detail.studentId }}</span>
            </div>
          </div>
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">当前状态</div>
            <span
              class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold shadow-sm"
              :class="statusBadgeClass(detail.status)"
            >
              <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="statusDotClass(detail.status)"></span>
              {{ detail.status === 1 ? '已通过' : detail.status === 2 ? '已驳回' : '待审批' }}
            </span>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4 mb-5">
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">请假类型</div>
            <div class="flex items-center gap-1.5">
              <span class="w-1.5 h-1.5 rounded-full" :class="leaveTypeDot(detail.leaveType)"></span>
              <span class="text-sm font-semibold text-ink">{{ detail.leaveType }}</span>
            </div>
          </div>
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">时间范围</div>
            <div class="text-sm font-semibold text-ink">{{ detail.startTime?.substring(0,16) }} ~ {{ detail.endTime?.substring(0,16) }}</div>
          </div>
        </div>
        <div class="bg-wash rounded-xl p-4 mb-5">
          <div class="text-xs text-mist mb-1">请假原因</div>
          <div class="text-sm text-steel leading-relaxed">{{ detail.reason }}</div>
        </div>
        <div class="grid grid-cols-2 gap-4 mb-5">
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">证明材料</div>
            <div v-if="detail.attachment" class="flex flex-wrap gap-2">
              <template v-for="(url, idx) in detail.attachment.split(',')" :key="idx">
                <el-image
                  v-if="isImage(url)"
                  :src="url"
                  class="w-16 h-16 rounded-lg object-cover cursor-pointer border border-gray-200"
                  :preview-src-list="imagePreviewList(detail.attachment)"
                  preview-teleported
                />
                <a v-else :href="url" target="_blank" class="text-sm text-blue-600 underline flex items-center gap-1">
                  <svg class="w-4 h-4 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><path d="M21.44 11.05l-9.19 9.19a6 6 0 0 1-8.49-8.49l9.19-9.19a4 4 0 0 1 5.66 5.66l-9.2 9.19a2 2 0 0 1-2.83-2.83l8.49-8.48"/></svg>
                  附件{{ idx + 1 }}
                </a>
              </template>
            </div>
            <div v-else class="text-sm text-ash">无</div>
          </div>
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">申请时间</div>
            <div class="text-sm font-semibold text-ink">{{ detail.applyTime?.substring(0,16) }}</div>
          </div>
        </div>
        <div v-if="detail.status === 2 && detail.rejectReason" class="bg-red-50 border border-red-100 rounded-xl p-4 mb-4">
          <div class="flex items-center gap-2 mb-1">
            <svg class="w-4 h-4 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
            </svg>
            <span class="text-xs font-semibold text-red-500">驳回原因</span>
          </div>
          <div class="text-sm text-red-600 ml-6">{{ detail.rejectReason }}</div>
        </div>
        <div v-if="detail.approveTime" class="bg-wash rounded-xl p-4">
          <div class="text-xs text-mist mb-1">审批时间</div>
          <div class="text-sm font-semibold text-ink">{{ detail.approveTime?.substring(0,16) }}</div>
        </div>
      </div>
      <template #footer>
        <div v-if="detail?.status === 0" class="flex gap-3 justify-end">
          <el-button @click="startRejectFromDetail(detail.id)" class="!rounded-lg">驳回</el-button>
          <el-button type="primary" @click="doApproveFromDetail(detail.id, 1)" class="!rounded-lg !shadow-sm">通过</el-button>
        </div>
        <div v-else class="flex justify-end">
          <el-button @click="detailVisible = false" class="!rounded-lg">关闭</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" width="480px" top="30vh" class="leave-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-red-400 to-rose-500 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"/><line x1="6" y1="6" x2="18" y2="18"/>
            </svg>
          </div>
          <span class="text-base font-bold text-ink">驳回申请</span>
        </div>
      </template>
      <div class="px-2">
        <div class="bg-amber-50 border border-amber-100 rounded-xl p-3 mb-4 text-xs text-amber-700 flex items-start gap-2">
          <svg class="w-4 h-4 text-amber-500 shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <path d="M10.29 3.86L1.82 18a2 2 0 0 0 1.71 3h16.94a2 2 0 0 0 1.71-3L13.71 3.86a2 2 0 0 0-3.42 0z"/><line x1="12" y1="9" x2="12" y2="13"/><line x1="12" y1="17" x2="12.01" y2="17"/>
          </svg>
          <span>驳回后将通知申请人，请填写驳回原因以便申请人了解情况。</span>
        </div>
        <el-form label-position="top">
          <el-form-item label="驳回原因">
            <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因（必填）" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="rejectVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="danger" @click="doReject" class="!rounded-lg !shadow-sm">确认驳回</el-button>
        </div>
      </template>
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

function isImage(url: string) { return /\.(jpg|jpeg|png|gif|bmp|webp|svg|ico)$/i.test(url) }
function imagePreviewList(attachment: string) { return (attachment || '').split(',').filter(isImage) }

async function showDetail(row: any) {
  try {
    const r = await getLeave(row.id)
    detail.value = r.data
    detailVisible.value = true
  } catch {
    ElMessage.error('获取请假详情失败')
  }
}

function leaveTypeClass(type: string) {
  const map: Record<string, string> = {
    '事假': 'bg-blue-50 text-blue-700 border border-blue-200',
    '病假': 'bg-rose-50 text-rose-700 border border-rose-200',
    '公假': 'bg-amber-50 text-amber-700 border border-amber-200',
  }
  return map[type] || 'bg-gray-50 text-gray-700 border border-gray-200'
}

function leaveTypeDot(type: string) {
  const map: Record<string, string> = {
    '事假': 'bg-blue-500',
    '病假': 'bg-rose-500',
    '公假': 'bg-amber-500',
  }
  return map[type] || 'bg-gray-500'
}

function statusBadgeClass(status: number) {
  if (status === 1) return 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white'
  if (status === 2) return 'bg-gradient-to-r from-red-400 to-rose-500 text-white'
  return 'bg-gradient-to-r from-amber-400 to-orange-500 text-white'
}

function statusDotClass(status: number) {
  if (status === 1) return 'bg-white'
  if (status === 2) return 'bg-white'
  return 'bg-white'
}

onMounted(fetch)
</script>
<template>
  <div class="animate__animated animate__fadeInDown">
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md">
      <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100">
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-sm">
            <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="16" y1="13" x2="8" y2="13"/>
              <line x1="16" y1="17" x2="8" y2="17"/>
            </svg>
          </div>
          <div>
            <h3 class="text-base font-bold text-ink">请假申请</h3>
            <p class="text-xs text-mist mt-0.5">管理我的请假记录与提交新申请</p>
          </div>
        </div>
        <el-button
          type="primary"
          @click="openDialog"
          class="!rounded-lg !shadow-sm hover:!shadow-md transition-all duration-200"
        >
          <svg class="w-4 h-4 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
          提交申请
        </el-button>
      </div>

      <div class="p-6">
        <el-table
          :data="leaves"
          v-loading="loading"
          @row-click="showDetail"
          style="cursor:pointer"
          class="!border-0"
          header-row-class-name="leave-table-header"
          row-class-name="leave-table-row"
        >
          <el-table-column prop="leaveType" label="类型" width="100">
            <template #default="{row}">
              <span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium"
                :class="leaveTypeClass(row.leaveType)"
              >
                <span class="w-1.5 h-1.5 rounded-full" :class="leaveTypeDot(row.leaveType)"></span>
                {{ row.leaveType }}
              </span>
            </template>
          </el-table-column>
          <el-table-column label="开始时间" width="180">
            <template #default="{row}">
              <span class="text-sm text-steel">{{ row.startTime?.substring(0,16) }}</span>
            </template>
          </el-table-column>
          <el-table-column label="结束时间" width="180">
            <template #default="{row}">
              <span class="text-sm text-steel">{{ row.endTime?.substring(0,16) }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="reason" label="原因" min-width="180">
            <template #default="{row}">
              <span class="text-sm text-ash truncate block max-w-[200px]">{{ row.reason }}</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120" fixed="right">
            <template #default="{row}">
              <span
                class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold shadow-sm"
                :class="statusBadgeClass(row.status)"
              >
                <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="statusDotClass(row.status)"></span>
                {{ row.status === 1 ? '已通过' : row.status === 2 ? '已驳回' : '待审批' }}
              </span>
            </template>
          </el-table-column>
        </el-table>

        <div v-if="!loading && leaves.length === 0" class="flex flex-col items-center justify-center py-16">
          <svg class="w-16 h-16 text-gray-200 mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
            <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
            <polyline points="14 2 14 8 20 8"/>
            <line x1="16" y1="13" x2="8" y2="13"/>
            <line x1="16" y1="17" x2="8" y2="17"/>
          </svg>
          <p class="text-sm text-mist">暂无请假记录</p>
          <el-button class="!mt-4" type="primary" plain @click="openDialog">提交第一份申请</el-button>
        </div>
      </div>
    </div>
  </div>

  <el-dialog v-model="detailVisible" width="560px" top="5vh" class="leave-dialog" :close-on-click-modal="false">
    <template #header>
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center">
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
          <div class="text-xs text-mist mb-1">请假类型</div>
          <div class="text-sm font-semibold text-ink">{{ detail.leaveType }}</div>
        </div>
        <div class="bg-wash rounded-xl p-4">
          <div class="text-xs text-mist mb-1">当前状态</div>
          <div>
            <span
              class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold shadow-sm"
              :class="statusBadgeClass(detail.status)"
            >
              <span class="w-1.5 h-1.5 rounded-full mr-1.5" :class="statusDotClass(detail.status)"></span>
              {{ detail.status === 1 ? '已通过' : detail.status === 2 ? '已驳回' : '待审批' }}
            </span>
          </div>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-4 mb-5">
        <div class="bg-wash rounded-xl p-4">
          <div class="text-xs text-mist mb-1">开始时间</div>
          <div class="text-sm font-semibold text-ink">{{ detail.startTime?.substring(0,16) }}</div>
        </div>
        <div class="bg-wash rounded-xl p-4">
          <div class="text-xs text-mist mb-1">结束时间</div>
          <div class="text-sm font-semibold text-ink">{{ detail.endTime?.substring(0,16) }}</div>
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
      <div class="flex justify-end gap-3">
        <el-button @click="detailVisible = false" class="!rounded-lg">关闭</el-button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="dialogVisible" width="560px" top="5vh" class="leave-dialog" :close-on-click-modal="false">
    <template #header>
      <div class="flex items-center gap-3">
        <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center">
          <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
          </svg>
        </div>
        <span class="text-base font-bold text-ink">提交请假申请</span>
      </div>
    </template>
    <el-form :model="form" label-position="top" class="px-2">
      <el-form-item label="请假类型">
        <el-select v-model="form.leaveType" placeholder="请选择请假类型" class="!w-full">
          <el-option label="事假" value="事假" />
          <el-option label="病假" value="病假" />
          <el-option label="公假" value="公假" />
        </el-select>
      </el-form-item>
      <div class="grid grid-cols-2 gap-4">
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" class="!w-full" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择结束时间" class="!w-full" />
        </el-form-item>
      </div>
      <el-form-item label="请假原因">
        <el-input v-model="form.reason" type="textarea" :rows="4" placeholder="请详细描述请假原因" />
      </el-form-item>
      <el-form-item label="证明材料">
        <el-upload
          ref="uploadRef"
          action="/api/upload/image"
          :headers="uploadHeaders"
          :on-success="handleUploadSuccess"
          :on-error="handleUploadError"
          :before-upload="beforeUpload"
          list-type="picture-card"
          multiple
          :limit="6"
          class="upload-card"
        >
          <span class="text-xs text-gray-400">+ 上传</span>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="flex justify-end gap-3">
        <el-button @click="dialogVisible = false" class="!rounded-lg">取消</el-button>
        <el-button type="primary" @click="submit" class="!rounded-lg !shadow-sm">提交申请</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getLeaves, getLeave, applyLeave } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import type { LeaveApplication } from '@/types'

const userStore = useUserStore()
const uploadRef = ref()
const leaves = ref<LeaveApplication[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const attachmentUrls = ref<string[]>([])
const form = reactive({ leaveType: '', startTime: '', endTime: '', reason: '', attachment: '' })

const detailVisible = ref(false)
const detail = ref<LeaveApplication | null>(null)

const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }))

function openDialog() {
  form.leaveType = ''; form.startTime = ''; form.endTime = ''; form.reason = ''; form.attachment = ''
  attachmentUrls.value = []
  uploadRef.value?.clearFiles()
  dialogVisible.value = true
}

function isImage(url: string) { return /\.(jpg|jpeg|png|gif|bmp|webp|svg|ico)$/i.test(url) }
function imagePreviewList(attachment: string) { return (attachment || '').split(',').filter(isImage) }

function beforeUpload(file: File) {
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) { ElMessage.warning('文件大小不能超过 5MB'); return false }
  return true
}
function handleUploadSuccess(response: any, _file: any, _files: any) {
  if (response.code === 200) {
    attachmentUrls.value.push(response.data)
    form.attachment = attachmentUrls.value.join(',')
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}
function handleUploadError() { ElMessage.error('上传失败，请重试') }

async function fetch() { loading.value = true; const r = await getLeaves({ page: 1, size: 50 }); leaves.value = r.data.records; loading.value = false }

async function submit() {
  await applyLeave(form)
  ElMessage.success('提交成功')
  dialogVisible.value = false
  attachmentUrls.value = []
  form.attachment = ''
  uploadRef.value?.clearFiles()
  fetch()
}

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

<style scoped>
.leave-table-header th {
  background-color: #F9FAFB !important;
  color: #6B7280 !important;
  font-size: 0.75rem !important;
  font-weight: 600 !important;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-top: 12px !important;
  padding-bottom: 12px !important;
}

.leave-table-row td {
  padding-top: 14px !important;
  padding-bottom: 14px !important;
  transition: background-color 0.2s ease;
}

.leave-table-row:hover td {
  background-color: #F3F4F6 !important;
}
</style>
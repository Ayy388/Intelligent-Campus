<template>
  <div>
    <div class="animate__animated animate__fadeInDown">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md">
        <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-orange-500 to-red-500 flex items-center justify-center shadow-sm">
              <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <line x1="3" y1="9" x2="21" y2="9"/>
                <line x1="9" y1="3" x2="9" y2="21"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-bold text-ink">场地预约审核</h3>
              <p class="text-xs text-mist mt-0.5">审核校园场地预约申请</p>
            </div>
          </div>
        </div>

        <div class="px-6 pt-4">
          <div class="flex gap-2 mb-4">
            <button v-for="tab in tabs" :key="tab.key" @click="activeTab=tab.key;page=1;fetchData()"
              class="px-5 py-2 rounded-xl text-sm font-medium transition-all duration-200"
              :class="activeTab===tab.key?'bg-gray-900 text-white shadow-md shadow-gray-900/10':'bg-white text-gray-500 border border-gray-200 hover:border-gray-300 hover:text-gray-700'">
              {{ tab.label }}
            </button>
          </div>
        </div>

        <div class="p-6 pt-0">
          <div v-if="loading" class="flex items-center justify-center py-16">
            <svg class="animate-spin h-8 w-8 text-gray-300" viewBox="0 0 24 24" fill="none">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
          </div>

          <div v-else-if="list.length === 0" class="flex flex-col items-center justify-center py-16">
            <svg class="w-16 h-16 text-gray-200 mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="3" x2="9" y2="21"/>
            </svg>
            <p class="text-sm text-mist">{{ emptyText }}</p>
          </div>

          <div v-else class="space-y-3">
            <div v-for="(b, i) in list" :key="b.id"
              class="bg-white rounded-xl border border-gray-100 p-5 hover:shadow-sm transition-all duration-200"
              :class="{ 'border-l-4 border-l-emerald-400': activeTab === 'approved', 'border-l-4 border-l-red-400': activeTab === 'rejected', 'border-l-4 border-l-amber-400': activeTab === 'pending' }">
              <div class="flex items-start justify-between gap-4">
                <div class="flex-1 min-w-0">
                  <div class="flex items-center gap-2.5 mb-2">
                    <span class="text-sm font-semibold text-ink">{{ b.title }}</span>
                    <el-tag size="small" round :type="tagType(b.status)" class="!border-0 !font-medium">
                      {{ statusLabel(b.status) }}
                    </el-tag>
                  </div>

                  <div class="grid grid-cols-2 gap-x-6 gap-y-1.5 text-xs text-ash">
                    <div class="flex items-center gap-1.5">
                      <svg class="w-3.5 h-3.5 text-mist shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                        <line x1="3" y1="9" x2="21" y2="9"/>
                        <line x1="9" y1="3" x2="9" y2="21"/>
                      </svg>
                      <span>{{ b.venueName || '未知场地' }}</span>
                    </div>
                    <div class="flex items-center gap-1.5">
                      <svg class="w-3.5 h-3.5 text-mist shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                        <circle cx="12" cy="7" r="4"/>
                      </svg>
                      <span>申请人: {{ b.userName || '用户' + b.userId }}</span>
                    </div>
                    <div class="flex items-center gap-1.5">
                      <svg class="w-3.5 h-3.5 text-mist shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                        <line x1="16" y1="2" x2="16" y2="6"/>
                        <line x1="8" y1="2" x2="8" y2="6"/>
                        <line x1="3" y1="10" x2="21" y2="10"/>
                      </svg>
                      <span>{{ b.startTime?.substring(0,10) || '' }} {{ b.startTime?.substring(11,16) || '' }} ~ {{ b.endTime?.substring(11,16) || '' }}</span>
                    </div>
                    <div class="flex items-center gap-1.5">
                      <svg class="w-3.5 h-3.5 text-mist shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <circle cx="12" cy="12" r="10"/>
                        <polyline points="12 6 12 12 16 14"/>
                      </svg>
                      <span>申请时间: {{ b.applyTime?.substring(0,16) || '--' }}</span>
                    </div>
                    <div v-if="b.purpose" class="col-span-2 flex items-start gap-1.5">
                      <svg class="w-3.5 h-3.5 text-mist shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                        <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                        <polyline points="14 2 14 8 20 8"/>
                        <line x1="16" y1="13" x2="8" y2="13"/>
                        <line x1="16" y1="17" x2="8" y2="17"/>
                      </svg>
                      <span>{{ b.purpose }}</span>
                    </div>
                  </div>

                  <div v-if="b.status === 2 && b.rejectReason" class="mt-2.5 flex items-start gap-1.5 text-xs text-red-400 bg-red-50/80 rounded-lg px-3 py-2">
                    <svg class="w-3.5 h-3.5 shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/>
                    </svg>
                    <span><span class="font-medium">驳回原因：</span>{{ b.rejectReason }}</span>
                  </div>
                </div>

                <div v-if="b.status === 0" class="flex gap-2 shrink-0">
                  <button @click="doApprove(b.id)"
                    class="px-4 py-2 bg-emerald-500 text-white rounded-lg text-xs font-semibold hover:bg-emerald-600 active:bg-emerald-700 transition-all duration-200 shadow-sm hover:shadow-md flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <polyline points="20 6 9 17 4 12"/>
                    </svg>
                    通过
                  </button>
                  <button @click="startReject(b.id)"
                    class="px-4 py-2 bg-red-400 text-white rounded-lg text-xs font-semibold hover:bg-red-500 active:bg-red-600 transition-all duration-200 shadow-sm hover:shadow-md flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                      <line x1="18" y1="6" x2="6" y2="18"/>
                      <line x1="6" y1="6" x2="18" y2="18"/>
                    </svg>
                    驳回
                  </button>
                </div>
              </div>
            </div>
          </div>

          <div v-if="total > pageSize" class="mt-4 flex justify-center">
            <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
              layout="prev,pager,next" small @current-change="fetchData" />
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="rejectVisible" width="420px" top="30vh" :close-on-click-modal="false">
      <template #header>
        <span class="text-base font-bold text-ink">填写驳回原因</span>
      </template>
      <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因" />
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="rejectVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="danger" @click="doReject" class="!rounded-lg">确认驳回</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getBookings, approveBooking } from '@/api/club'
import { ElMessage } from 'element-plus'

const tabs = [
  { key: 'pending', label: '待审批' },
  { key: 'approved', label: '已通过' },
  { key: 'rejected', label: '已驳回' },
]
const activeTab = ref('pending')
const list = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10

const rejectVisible = ref(false)
const rejectId = ref<number | null>(null)
const rejectReason = ref('')

const emptyText = computed(() => {
  if (activeTab.value === 'pending') return '暂无待审批的预约申请'
  if (activeTab.value === 'approved') return '暂无已通过的预约'
  return '暂无已驳回的预约'
})

function statusLabel(status: number) {
  if (status === 0) return '待审批'
  if (status === 1) return '已通过'
  if (status === 2) return '已驳回'
  return '未知'
}

function tagType(status: number) {
  if (status === 1) return 'success'
  if (status === 2) return 'danger'
  return 'warning'
}

function statusFilter() {
  if (activeTab.value === 'pending') return 0
  if (activeTab.value === 'approved') return 1
  if (activeTab.value === 'rejected') return 2
  return undefined
}

async function fetchData() {
  loading.value = true
  try {
    const r = await getBookings({ page: page.value, size: pageSize, status: statusFilter() })
    list.value = r.data.records || []
    total.value = r.data.total || 0
  } catch { /* ignore */ }
  loading.value = false
}

async function doApprove(id: number) {
  try {
    await approveBooking(id, 1)
    ElMessage.success('已通过')
    fetchData()
  } catch { ElMessage.error('操作失败') }
}

function startReject(id: number) {
  rejectId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!rejectReason.value) { ElMessage.warning('请输入驳回原因'); return }
  if (!rejectId.value) return
  try {
    await approveBooking(rejectId.value, 2, rejectReason.value)
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchData()
  } catch { ElMessage.error('操作失败') }
}

onMounted(fetchData)
</script>
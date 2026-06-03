<template>
  <div v-loading="loading" class="min-h-screen">
    <div class="flex items-center gap-3 mb-6">
      <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg shadow-indigo-200 flex items-center justify-center">
        <svg class="w-4.5 h-4.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
          <line x1="3" y1="9" x2="21" y2="9"/>
          <line x1="9" y1="3" x2="9" y2="21"/>
        </svg>
      </div>
      <div>
        <span class="text-lg font-bold text-ink">场地预约</span>
        <span class="text-xs text-mist ml-2">便捷预约校园场地</span>
      </div>
    </div>

    <div class="mb-8">
      <div class="flex items-center gap-2 mb-4">
        <div class="w-1 h-4 rounded-full bg-gradient-to-b from-indigo-500 to-purple-600"></div>
        <span class="text-sm font-semibold text-ink">可用场地</span>
        <span class="text-xs text-mist ml-1">共 {{ venues.length }} 个</span>
      </div>

      <div v-if="venues.length===0" class="flex flex-col items-center justify-center py-16 bg-white rounded-2xl border border-soft">
        <div class="w-14 h-14 rounded-full bg-wash flex items-center justify-center mb-3">
          <svg class="w-6 h-6 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
            <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            <polyline points="9 22 9 12 15 12 15 22"/>
          </svg>
        </div>
        <span class="text-sm text-mist">暂无可用场地</span>
      </div>

      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div v-for="(v, idx) in venues" :key="v.id"
          class="animate__animated animate__fadeInUp"
          :style="{ animationDelay: `${idx * 0.08}s` }">
          <div class="bg-white rounded-2xl border border-soft overflow-hidden group hover:shadow-lg hover:-translate-y-0.5 transition-all duration-300 cursor-pointer" @click="startBooking(v)">
            <div :class="`h-28 bg-gradient-to-br relative overflow-hidden ${iconGradients[idx % iconGradients.length]}`">
              <div class="absolute inset-0 bg-white/20">
                <div class="w-40 h-40 rounded-full bg-white/10 absolute -top-16 -right-12"></div>
                <div class="w-24 h-24 rounded-full bg-white/10 absolute -bottom-8 -left-8"></div>
              </div>
              <div :class="`absolute top-4 left-4 w-11 h-11 rounded-xl bg-white/25 backdrop-blur-sm shadow-lg flex items-center justify-center`">
                <svg class="w-5.5 h-5.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                  <line x1="3" y1="9" x2="21" y2="9"/>
                  <line x1="9" y1="3" x2="9" y2="21"/>
                </svg>
              </div>
              <div class="absolute bottom-3 left-4 flex gap-2">
                <span class="px-2.5 py-0.5 bg-white/25 backdrop-blur-sm rounded-full text-[11px] text-white font-medium">
                  {{ v.capacity || 0 }}人
                </span>
              </div>
            </div>
            <div class="p-4">
              <div class="text-sm font-semibold text-ink mb-1.5 group-hover:text-indigo-600 transition-colors">{{ v.name }}</div>
              <div class="flex items-center gap-1 text-xs text-ash mb-2">
                <svg class="w-3 h-3 shrink-0" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                <span class="truncate">{{ v.location || '未指定位置' }}</span>
              </div>
              <div class="text-xs text-mist mb-3.5 line-clamp-2 leading-relaxed min-h-[2.5rem]">{{ v.description || '暂无描述' }}</div>
              <button @click.stop="startBooking(v)"
                class="w-full py-2 text-xs font-medium rounded-xl border border-soft text-ink bg-white hover:bg-ink hover:text-white hover:border-ink transition-all duration-200 active:scale-[0.97] flex items-center justify-center gap-1.5">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                预约场地
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div>
      <div class="flex items-center justify-between mb-4">
        <div class="flex items-center gap-2">
          <div class="w-1 h-4 rounded-full bg-gradient-to-b from-emerald-500 to-teal-600"></div>
          <span class="text-sm font-semibold text-ink">预约记录</span>
          <span class="text-xs text-mist ml-1">共 {{ total }} 条</span>
        </div>
        <div class="flex items-center gap-1 bg-wash rounded-xl p-1">
          <button v-for="tab in bookingTabs" :key="tab.key" @click="bookingTab=tab.key;page=1;fetchBookings()"
            class="px-4 py-1.5 rounded-lg text-xs font-medium transition-all duration-200"
            :class="bookingTab===tab.key?'bg-white text-ink shadow-sm border border-soft':'text-mist hover:text-ink'">
            {{ tab.label }}
          </button>
        </div>
      </div>

      <div class="space-y-3">
        <div v-if="bookings.length===0"
          class="flex flex-col items-center justify-center py-16 bg-white rounded-2xl border border-soft">
          <div class="w-14 h-14 rounded-full bg-wash flex items-center justify-center mb-3">
            <svg class="w-6 h-6 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
              <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
              <polyline points="14 2 14 8 20 8"/>
              <line x1="12" y1="18" x2="12" y2="12"/>
              <line x1="9" y1="15" x2="15" y2="15"/>
            </svg>
          </div>
          <span class="text-sm text-mist">暂无预约记录</span>
        </div>

        <div v-for="(b, idx) in bookings" :key="b.id"
          class="animate__animated animate__fadeInUp"
          :style="{ animationDelay: `${idx * 0.04}s` }">
          <div class="bg-white rounded-2xl border border-soft p-4 hover:shadow-sm transition-all duration-200">
            <div class="flex items-start justify-between mb-2.5">
              <div class="flex items-center gap-2.5 flex-1 min-w-0">
                <div class="w-2.5 h-2.5 rounded-full shrink-0"
                  :class="b.status===1?'bg-emerald-400 shadow-sm shadow-emerald-200':b.status===2?'bg-red-400 shadow-sm shadow-red-200':'bg-amber-400 shadow-sm shadow-amber-200'">
                </div>
                <span class="text-sm font-medium text-ink truncate">{{ b.title }}</span>
              </div>
              <el-tag size="small" round :type="b.status===1?'success':b.status===2?'danger':'warning'" class="shrink-0 ml-2 !border-0 !font-medium">
                {{ b.status===1?'已通过':b.status===2?'已驳回':'待审批' }}
              </el-tag>
            </div>

            <div class="ml-5 grid grid-cols-2 gap-x-4 gap-y-2 text-xs text-ash mb-3">
              <div class="flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5 shrink-0 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M21 10c0 7-9 13-9 13s-9-6-9-13a9 9 0 0 1 18 0z"/>
                  <circle cx="12" cy="10" r="3"/>
                </svg>
                <span class="truncate">{{ b.venueName || '未知场地' }}</span>
              </div>
              <div class="flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5 shrink-0 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
                  <line x1="16" y1="2" x2="16" y2="6"/>
                  <line x1="8" y1="2" x2="8" y2="6"/>
                  <line x1="3" y1="10" x2="21" y2="10"/>
                </svg>
                <span>{{ b.startTime?.substring(0,10) || '' }}</span>
              </div>
              <div class="flex items-center gap-1.5 col-span-2">
                <svg class="w-3.5 h-3.5 shrink-0 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <polyline points="12 6 12 12 16 14"/>
                </svg>
                <span>{{ b.startTime?.substring(11,16) || '' }} ~ {{ b.endTime?.substring(11,16) || '' }}</span>
              </div>
              <div v-if="b.purpose" class="col-span-2 flex items-start gap-1.5">
                <svg class="w-3.5 h-3.5 shrink-0 mt-0.5 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/>
                  <polyline points="14 2 14 8 20 8"/>
                  <line x1="16" y1="13" x2="8" y2="13"/>
                  <line x1="16" y1="17" x2="8" y2="17"/>
                  <polyline points="10 9 9 9 8 9"/>
                </svg>
                <span>{{ b.purpose }}</span>
              </div>
              <div v-if="bookingTab==='all' && b.userName" class="col-span-2 flex items-center gap-1.5">
                <svg class="w-3.5 h-3.5 shrink-0 text-mist" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
                  <circle cx="12" cy="7" r="4"/>
                </svg>
                <span>申请人: {{ b.userName }}</span>
              </div>
            </div>

            <div v-if="b.status===2 && b.rejectReason"
              class="ml-5 mb-3 px-3.5 py-2.5 bg-red-50/80 rounded-xl border border-red-100">
              <div class="flex items-start gap-2 text-xs text-red-500">
                <svg class="w-3.5 h-3.5 shrink-0 mt-0.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="12" cy="12" r="10"/>
                  <line x1="12" y1="8" x2="12" y2="12"/>
                  <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
                <div>
                  <span class="font-medium">驳回原因：</span>
                  <span>{{ b.rejectReason }}</span>
                </div>
              </div>
            </div>

            <div v-if="(userStore.role==='admin'||userStore.role==='teacher') && b.status===0"
              class="flex justify-end gap-2.5 pt-3 border-t border-wash">
              <button @click="doApprove(b.id,1)"
                class="px-4 py-1.5 text-xs font-medium rounded-lg bg-emerald-500 text-white hover:bg-emerald-600 transition-all duration-200 active:scale-[0.97] shadow-sm shadow-emerald-200 flex items-center gap-1">
                <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round">
                  <polyline points="20 6 9 17 4 12"/>
                </svg>
                通过
              </button>
              <button @click="startReject(b.id)"
                class="px-4 py-1.5 text-xs font-medium rounded-lg border border-soft text-ash hover:bg-wash hover:text-red-500 hover:border-red-200 transition-all duration-200 active:scale-[0.97] flex items-center gap-1">
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

      <div v-if="total > pageSize" class="flex justify-center mt-5">
        <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
          layout="prev,pager,next" small background @current-change="fetchBookings" />
      </div>
    </div>

    <el-dialog v-model="bookingVisible" top="5vh" width="520px" class="booking-dialog" destroy-on-close>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-indigo-500 to-purple-600 shadow-lg shadow-indigo-200 flex items-center justify-center">
            <svg class="w-4.5 h-4.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="4" width="18" height="18" rx="2" ry="2"/>
              <line x1="16" y1="2" x2="16" y2="6"/>
              <line x1="8" y1="2" x2="8" y2="6"/>
              <line x1="3" y1="10" x2="21" y2="10"/>
            </svg>
          </div>
          <div>
            <div class="text-base font-semibold text-ink">预约场地</div>
            <div class="text-xs text-mist mt-0.5">填写以下信息提交场地预约申请</div>
          </div>
        </div>
      </template>
      <el-form :model="bookingForm" label-position="top" class="px-1">
        <div class="mb-4 p-3.5 bg-wash rounded-xl border border-soft">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shrink-0">
              <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <line x1="3" y1="9" x2="21" y2="9"/>
                <line x1="9" y1="3" x2="9" y2="21"/>
              </svg>
            </div>
            <div>
              <div class="text-xs text-mist">预约场地</div>
              <div class="text-sm font-medium text-ink">{{ selectedVenue?.name }}</div>
            </div>
            <div class="ml-auto text-right">
              <div class="text-xs text-mist">容量</div>
              <div class="text-sm font-medium text-ink">{{ selectedVenue?.capacity || 0 }}人</div>
            </div>
          </div>
        </div>
        <el-form-item label="活动名称">
          <el-input v-model="bookingForm.title" placeholder="请输入活动名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="bookingForm.purpose" type="textarea" :rows="3" placeholder="请说明预约用途，如活动性质、参与人数等" class="custom-input" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="开始时间">
            <el-date-picker v-model="bookingForm.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="bookingForm.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="flex items-center justify-between">
          <div class="text-xs text-mist">提交后将等待管理员审批</div>
          <div class="flex gap-3">
            <el-button @click="bookingVisible=false" class="!rounded-xl !border-soft !text-ash !px-5">取消</el-button>
            <el-button type="primary" @click="doBooking" :loading="submitting"
              class="!rounded-xl !bg-gradient-to-r !from-indigo-500 !to-purple-600 !border-none !px-5 !shadow-lg !shadow-indigo-200">
              提交预约
            </el-button>
          </div>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" top="25vh" width="460px" class="reject-dialog" destroy-on-close>
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-9 h-9 rounded-xl bg-gradient-to-br from-red-500 to-rose-600 shadow-lg shadow-red-200 flex items-center justify-center">
            <svg class="w-4.5 h-4.5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <circle cx="12" cy="12" r="10"/>
              <line x1="12" y1="8" x2="12" y2="12"/>
              <line x1="12" y1="16" x2="12.01" y2="16"/>
            </svg>
          </div>
          <div>
            <div class="text-base font-semibold text-ink">驳回预约</div>
            <div class="text-xs text-mist mt-0.5">请输入驳回原因，该原因将通知申请人</div>
          </div>
        </div>
      </template>
      <el-form label-position="top" class="px-1">
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="4" placeholder="请输入驳回原因，以便申请人了解情况并作出调整" class="custom-input" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="rejectVisible=false" class="!rounded-xl !border-soft !text-ash !px-5">取消</el-button>
          <el-button type="primary" @click="doReject" :loading="submitting"
            class="!rounded-xl !bg-gradient-to-r !from-red-500 !to-rose-600 !border-none !px-5 !shadow-lg !shadow-red-200">
            确认驳回
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import 'animate.css'
import { getVenues, getBookings, applyBooking, approveBooking } from '@/api/club'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const venues = ref<any[]>([])
const bookings = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const pageSize = 10
const loading = ref(false)
const submitting = ref(false)

const bookingTabs = [
  { key: 'my' as const, label: '我的预约' },
  { key: 'all' as const, label: '全部预约' },
]
const bookingTab = ref<'my' | 'all'>('my')

const bookingVisible = ref(false)
const selectedVenue = ref<any>(null)
const bookingForm = reactive({ title: '', purpose: '', startTime: '', endTime: '' })

const rejectVisible = ref(false)
const rejectBookingId = ref<number | null>(null)
const rejectReason = ref('')

const iconGradients = [
  'from-indigo-500 to-purple-600',
  'from-emerald-500 to-teal-600',
  'from-orange-500 to-rose-600',
  'from-sky-500 to-blue-600',
  'from-pink-500 to-rose-600',
  'from-amber-500 to-orange-600',
  'from-violet-500 to-fuchsia-600',
  'from-cyan-500 to-blue-600',
  'from-lime-500 to-emerald-600',
  'from-rose-500 to-pink-600',
]

async function fetchVenues() {
  loading.value = true
  try {
    const r = await getVenues()
    venues.value = r.data || []
  } finally {
    loading.value = false
  }
}

async function fetchBookings() {
  loading.value = true
  try {
    if (bookingTab.value === 'my') {
      const r = await getBookings({ page: 1, size: 999 })
      let data = r.data.records || []
      data = data.filter((b: any) => b.userId === userStore.userInfo?.id)
      total.value = data.length
      const start = (page.value - 1) * pageSize
      bookings.value = data.slice(start, start + pageSize)
    } else {
      const r = await getBookings({ page: page.value, size: pageSize })
      bookings.value = r.data.records || []
      total.value = r.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

function startBooking(venue: any) {
  selectedVenue.value = venue
  bookingForm.title = ''
  bookingForm.purpose = ''
  bookingForm.startTime = ''
  bookingForm.endTime = ''
  bookingVisible.value = true
}

async function doBooking() {
  if (!bookingForm.title.trim()) { ElMessage.warning('请输入活动名称'); return }
  if (!bookingForm.startTime) { ElMessage.warning('请选择开始时间'); return }
  if (!bookingForm.endTime) { ElMessage.warning('请选择结束时间'); return }
  submitting.value = true
  try {
    await applyBooking({ ...bookingForm, venueId: selectedVenue.value.id })
    ElMessage.success('预约已提交')
    bookingVisible.value = false
    fetchBookings()
  } catch { ElMessage.error('预约提交失败') }
  finally { submitting.value = false }
}

async function doApprove(id: number, status: number) {
  submitting.value = true
  try {
    await approveBooking(id, status)
    ElMessage.success(status === 1 ? '已通过' : '已驳回')
    fetchBookings()
  } finally {
    submitting.value = false
  }
}

function startReject(id: number) {
  rejectBookingId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!rejectBookingId.value) return
  submitting.value = true
  try {
    await approveBooking(rejectBookingId.value, 2, rejectReason.value)
    ElMessage.success('已驳回')
    rejectVisible.value = false
    fetchBookings()
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  fetchVenues()
  fetchBookings()
})
</script>

<style scoped>
:deep(.el-dialog) {
  --el-dialog-border-radius: 16px;
  --el-dialog-padding-primary: 24px;
}
:deep(.el-dialog__header) {
  padding: 24px 24px 0;
  margin-right: 0;
  border-bottom: none;
}
:deep(.el-dialog__body) {
  padding: 20px 24px;
}
:deep(.el-dialog__footer) {
  padding: 0 24px 24px;
  border-top: none;
}
:deep(.el-dialog__headerbtn) {
  top: 20px;
  right: 20px;
  font-size: 16px;
}
:deep(.el-form-item__label) {
  font-size: 13px;
  font-weight: 500;
  color: #374151;
  padding-bottom: 4px;
}
:deep(.el-form-item) {
  margin-bottom: 18px;
}
:deep(.el-form-item:last-child) {
  margin-bottom: 0;
}
:deep(.custom-input .el-input__wrapper),
:deep(.custom-input .el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #E5E7EB;
  box-shadow: none;
  padding: 8px 12px;
  font-size: 13px;
}
:deep(.custom-input .el-input__wrapper:hover),
:deep(.custom-input .el-textarea__inner:hover) {
  border-color: #9CA3AF;
}
:deep(.custom-input .el-input__wrapper.is-focus),
:deep(.custom-input .el-textarea__inner:focus) {
  border-color: #6366F1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
:deep(.el-date-editor .el-input__wrapper) {
  border-radius: 10px;
  border: 1px solid #E5E7EB;
  box-shadow: none;
}
:deep(.el-date-editor .el-input__wrapper:hover) {
  border-color: #9CA3AF;
}
:deep(.el-date-editor .el-input__wrapper.is-focus) {
  border-color: #6366F1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
:deep(.el-tag) {
  --el-tag-border-color: transparent;
}
:deep(.el-tag--success) {
  background: #ECFDF5;
  color: #059669;
}
:deep(.el-tag--danger) {
  background: #FEF2F2;
  color: #DC2626;
}
:deep(.el-tag--warning) {
  background: #FFFBEB;
  color: #D97706;
}
:deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background-color: #111827;
  border-color: #111827;
}
:deep(.el-pagination.is-background .el-pager li:hover) {
  color: #111827;
}
:deep(.el-pagination .btn-prev),
:deep(.el-pagination .btn-next) {
  border-radius: 8px;
}
:deep(.el-pagination .el-pager li) {
  border-radius: 8px;
}
</style>
<template>
  <div>
    <div class="mb-5">
      <span class="text-lg font-bold text-ink">场地预约</span>
    </div>

    <div class="text-sm font-semibold text-ink mb-3">可用场地</div>
    <div v-if="venues.length===0" class="text-center text-mist py-10 text-sm">暂无可用场地</div>
    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
      <div v-for="v in venues" :key="v.id"
        class="bg-white rounded-xl border border-soft p-5 hover:shadow-sm transition-shadow">
        <div class="text-sm font-semibold text-ink mb-1">{{ v.name }}</div>
        <div class="text-xs text-ash mb-1">{{ v.location || '未指定位置' }} · 容量 {{ v.capacity || 0 }}人</div>
        <div class="text-xs text-mist mb-3 line-clamp-2">{{ v.description || '暂无描述' }}</div>
        <button @click="startBooking(v)"
          class="px-4 py-1.5 text-xs border border-ink text-ink rounded-lg hover:bg-ink hover:text-white transition-colors">
          预约场地
        </button>
      </div>
    </div>

    <div class="flex items-center gap-2 mb-4">
      <button v-for="tab in bookingTabs" :key="tab.key" @click="bookingTab=tab.key;page=1;fetchBookings()"
        class="px-4 py-1.5 rounded-lg text-sm font-medium border transition-all"
        :class="bookingTab===tab.key?'bg-ink text-white border-ink':'bg-white text-steel border-soft hover:border-line'">
        {{ tab.label }}
      </button>
    </div>

    <div class="bg-white rounded-xl border border-soft p-5">
      <div v-if="bookings.length===0" class="text-center text-mist py-10 text-sm">暂无预约记录</div>
      <div v-for="b in bookings" :key="b.id"
        class="flex items-center justify-between py-3 border-b border-wash last:border-0 text-sm">
        <div class="flex-1">
          <div class="flex items-center gap-2">
            <span class="text-ink font-medium">{{ b.title }}</span>
            <el-tag size="small" :type="b.status===1?'success':b.status===2?'danger':'warning'">
              {{ b.status===1?'已通过':b.status===2?'已驳回':'待审批' }}
            </el-tag>
          </div>
          <div class="text-xs text-mist mt-1">
            {{ b.venueName || '未知场地' }} · {{ b.startTime?.substring(0,16) || '' }} ~ {{ b.endTime?.substring(0,16) || '' }}
          </div>
          <div v-if="b.purpose" class="text-xs text-ash mt-0.5">{{ b.purpose }}</div>
          <div v-if="b.userName" class="text-xs text-mist mt-0.5">申请人: {{ b.userName }}</div>
          <div v-if="b.status===2 && b.rejectReason" class="text-xs text-red-400 mt-0.5">驳回原因: {{ b.rejectReason }}</div>
        </div>
        <div v-if="(userStore.role==='admin'||userStore.role==='teacher') && b.status===0" class="flex gap-2 shrink-0 ml-4">
          <button @click="doApprove(b.id,1)" class="px-3 py-1 text-xs bg-ink text-white rounded-lg hover:bg-steel transition-colors">通过</button>
          <button @click="startReject(b.id)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">驳回</button>
        </div>
      </div>
      <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
        layout="prev,pager,next" small @current-change="fetchBookings" />
    </div>

    <el-dialog v-model="bookingVisible" title="预约场地" width="500px">
      <el-form :model="bookingForm">
        <el-form-item label="预约场地">
          <el-input :model-value="selectedVenue?.name" disabled />
        </el-form-item>
        <el-form-item label="活动名称">
          <el-input v-model="bookingForm.title" placeholder="请输入活动名称" />
        </el-form-item>
        <el-form-item label="用途说明">
          <el-input v-model="bookingForm.purpose" type="textarea" :rows="3" placeholder="请说明预约用途" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="bookingForm.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="bookingForm.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookingVisible=false">取消</el-button>
        <el-button type="primary" @click="doBooking">提交预约</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="rejectVisible" title="驳回预约" width="450px">
      <el-form>
        <el-form-item label="驳回原因">
          <el-input v-model="rejectReason" type="textarea" :rows="3" placeholder="请输入驳回原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible=false">取消</el-button>
        <el-button type="primary" @click="doReject">确认驳回</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getVenues, getBookings, applyBooking, approveBooking } from '@/api/club'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const venues = ref<any[]>([])
const bookings = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const pageSize = 10

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

async function fetchVenues() {
  const r = await getVenues()
  venues.value = r.data || []
}

async function fetchBookings() {
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
  try {
    await applyBooking({ ...bookingForm, venueId: selectedVenue.value.id })
    ElMessage.success('预约已提交')
    bookingVisible.value = false
    fetchBookings()
  } catch { ElMessage.error('预约提交失败') }
}

async function doApprove(id: number, status: number) {
  await approveBooking(id, status)
  ElMessage.success(status === 1 ? '已通过' : '已驳回')
  fetchBookings()
}

function startReject(id: number) {
  rejectBookingId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!rejectBookingId.value) return
  await approveBooking(rejectBookingId.value, 2, rejectReason.value)
  ElMessage.success('已驳回')
  rejectVisible.value = false
  fetchBookings()
}

onMounted(() => {
  fetchVenues()
  fetchBookings()
})
</script>
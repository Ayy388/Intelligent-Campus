<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <span class="text-lg font-bold text-ink">签到统计管理</span>
    </div>

    <div v-if="loading && checkins.length === 0" class="bg-white rounded-xl border border-soft p-10 text-center animate__animated animate__fadeInUp">
      <el-icon class="is-loading text-mist" size="24"><Loading /></el-icon>
      <div class="text-mist text-sm mt-2">加载中...</div>
    </div>

    <div v-else-if="checkins.length === 0" class="bg-white rounded-xl border border-soft p-10 text-center animate__animated animate__fadeInUp">
      <div class="text-mist text-sm">暂无签到活动</div>
    </div>

    <div class="space-y-3">
      <div v-for="(item, index) in checkins" :key="item.id"
        class="bg-white rounded-xl border border-soft overflow-hidden transition-all duration-200 hover:shadow-sm animate__animated animate__fadeInUp"
        :class="[expandedId === item.id ? 'shadow-sm' : '']"
        :style="{ animationDelay: index * 0.05 + 's' }">

        <div class="p-4 cursor-pointer select-none" @click="toggleExpand(item)">
          <div class="flex items-start justify-between">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="text-sm font-semibold text-ink truncate">{{ item.title }}</span>
                <el-tag size="small" :type="item.checkinType === 'course' ? 'primary' : 'warning'" effect="plain" class="!border-0">
                  {{ item.checkinType === 'course' ? '课程签到' : '活动签到' }}
                </el-tag>
                <el-tag v-if="item.status === 1" size="small" type="success" effect="plain" class="!border-0">进行中</el-tag>
                <el-tag v-else size="small" type="info" effect="plain" class="!border-0">已结束</el-tag>
              </div>
              <div class="text-xs text-mist mt-1">
                {{ item.teacherName || '未知教师' }}
                <span v-if="item.courseName"> · {{ item.courseName }}</span>
                <span v-if="item.createTime"> · {{ formatTime(item.createTime) }}</span>
              </div>
            </div>
            <div class="ml-4 flex-shrink-0 flex items-center gap-2">
              <div class="text-xs text-mist flex items-center gap-1">
                <span>{{ item._stats ? '已加载' : '点击查看' }}</span>
                <el-icon :class="expandedId === item.id ? 'rotate-180' : ''" class="transition-transform duration-200">
                  <ArrowDown />
                </el-icon>
              </div>
            </div>
          </div>

          <div class="flex items-center gap-6 mt-3 pt-3 border-t border-wash">
            <div class="text-center">
              <div class="text-xs text-mist">总应签</div>
              <div class="text-sm font-semibold text-ink">{{ item._stats?.total ?? '-' }}</div>
            </div>
            <div class="text-center">
              <div class="text-xs text-mist">已签到</div>
              <div class="text-sm font-semibold" :class="item._stats?.checked != null && item._stats.checked > 0 ? 'text-emerald-600' : 'text-mist'">{{ item._stats?.checked ?? '-' }}</div>
            </div>
            <div class="text-center">
              <div class="text-xs text-mist">缺勤</div>
              <div class="text-sm font-semibold" :class="item._stats?.absent != null && item._stats.absent > 0 ? 'text-red-500' : 'text-mist'">{{ item._stats?.absent ?? '-' }}</div>
            </div>
            <div class="flex-1 min-w-0 pl-2">
              <div class="flex items-center justify-between mb-1">
                <span class="text-xs text-mist">签到率</span>
                <span class="text-xs font-semibold" :class="rateColorClass(item._stats?.rate)">{{ item._stats?.rate != null ? item._stats.rate + '%' : '-' }}</span>
              </div>
              <div class="w-full h-2.5 bg-gray-100 rounded-full overflow-hidden">
                <div class="h-full rounded-full transition-all duration-700 ease-out"
                  :style="{ width: (item._stats?.rate ?? 0) + '%', background: rateGradient(item._stats?.rate) }">
                </div>
              </div>
            </div>
          </div>
        </div>

        <transition name="slide">
          <div v-if="expandedId === item.id" class="border-t border-wash bg-gray-50/50">
            <div v-if="item._loadingRecords" class="flex items-center justify-center py-10">
              <el-icon class="is-loading text-mist" size="20"><Loading /></el-icon>
              <span class="text-xs text-mist ml-2">加载签到记录...</span>
            </div>
            <div v-else class="p-4">
              <div class="flex items-center justify-between mb-3">
                <span class="text-xs font-medium text-ink">签到详情（共 {{ item._records?.length || 0 }} 人）</span>
                <div class="flex gap-1.5 flex-wrap">
                  <span class="text-xs px-2 py-0.5 rounded-full bg-emerald-50 text-emerald-600 border border-emerald-100">
                    已签到 {{ recordCountByStatus(item._records, [1]) }}
                  </span>
                  <span class="text-xs px-2 py-0.5 rounded-full bg-amber-50 text-amber-600 border border-amber-100">
                    迟到 {{ recordCountByStatus(item._records, [2]) }}
                  </span>
                  <span class="text-xs px-2 py-0.5 rounded-full bg-blue-50 text-blue-600 border border-blue-100">
                    请假 {{ recordCountByStatus(item._records, [3]) }}
                  </span>
                  <span class="text-xs px-2 py-0.5 rounded-full bg-red-50 text-red-500 border border-red-100">
                    缺勤 {{ recordCountByStatus(item._records, [0]) }}
                  </span>
                </div>
              </div>
              <div v-if="!item._records || item._records.length === 0" class="text-center text-mist py-8 text-sm bg-white rounded-lg border border-dashed border-soft">
                暂无签到记录
              </div>
              <div v-else class="bg-white rounded-lg border border-soft overflow-hidden">
                <table class="w-full text-xs">
                  <thead>
                    <tr class="bg-gray-50">
                      <th class="text-left py-2.5 px-3 text-mist font-medium w-[22%]">姓名</th>
                      <th class="text-left py-2.5 px-3 text-mist font-medium w-[22%]">学号</th>
                      <th class="text-left py-2.5 px-3 text-mist font-medium w-[20%]">状态</th>
                      <th class="text-left py-2.5 px-3 text-mist font-medium w-[36%]">签到时间</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(r, ri) in item._records" :key="r.id"
                      class="border-t border-wash transition-colors hover:bg-gray-50"
                      :class="ri % 2 === 1 ? 'bg-gray-50/30' : ''">
                      <td class="py-2.5 px-3 text-ink font-medium">{{ r.studentName || '未知' }}</td>
                      <td class="py-2.5 px-3 text-mist">{{ r.studentId }}</td>
                      <td class="py-2.5 px-3">
                        <span class="inline-flex items-center gap-1"
                          :class="statusDotClass(r.status)">
                          <span class="w-1.5 h-1.5 rounded-full" :class="statusDotColor(r.status)"></span>
                          {{ statusText(r.status) }}
                        </span>
                      </td>
                      <td class="py-2.5 px-3 text-mist">{{ formatTime(r.checkTime) || '-' }}</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </transition>
      </div>
    </div>

    <el-pagination v-if="total > pageSize"
      v-model:current-page="page" :total="total" :page-size="pageSize"
      layout="prev,pager,next" small @current-change="onPageChange" class="mt-4" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getCheckIns, getCheckInRecords } from '@/api/growth'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const checkins = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10
const expandedId = ref<number | null>(null)

function formatTime(t: any) {
  if (!t) return ''
  const s = typeof t === 'string' ? t : String(t)
  return s.substring(0, 16).replace('T', ' ')
}

function statusText(status: number) {
  const map: Record<number, string> = { 0: '缺勤', 1: '已签到', 2: '迟到', 3: '请假' }
  return map[status] ?? '未知'
}

function statusDotClass(status: number) {
  const map: Record<number, string> = {
    0: 'text-red-500',
    1: 'text-emerald-600',
    2: 'text-amber-600',
    3: 'text-blue-600',
  }
  return map[status] ?? 'text-mist'
}

function statusDotColor(status: number) {
  const map: Record<number, string> = {
    0: 'bg-red-500',
    1: 'bg-emerald-500',
    2: 'bg-amber-500',
    3: 'bg-blue-500',
  }
  return map[status] ?? 'bg-gray-400'
}

function rateColorClass(rate: number | null | undefined) {
  if (rate == null) return 'text-mist'
  if (rate >= 90) return 'text-emerald-600'
  if (rate >= 70) return 'text-orange-500'
  return 'text-red-500'
}

function rateGradient(rate: number | null | undefined) {
  if (rate == null) return '#9CA3AF'
  if (rate >= 90) return 'linear-gradient(90deg, #22C55E, #16A34A)'
  if (rate >= 70) return 'linear-gradient(90deg, #F97316, #EA580C)'
  return 'linear-gradient(90deg, #EF4444, #DC2626)'
}

function recordCountByStatus(records: any[], statuses: number[]) {
  if (!records) return 0
  return records.filter(r => statuses.includes(r.status)).length
}

function computeStats(records: any[]) {
  const total = records.length
  const checked = records.filter(r => r.status === 1 || r.status === 2).length
  const absent = records.filter(r => r.status === 0).length
  const rate = total > 0 ? Math.round((checked / total) * 100) : 0
  return { total, checked, absent, rate }
}

async function fetchRecordsForItem(item: any) {
  if (item._records) return
  item._loadingRecords = true
  try {
    const r = await getCheckInRecords(item.id)
    const records = r.data || []
    item._records = records
    item._stats = computeStats(records)
  } catch {
    item._records = []
    item._stats = { total: 0, checked: 0, absent: 0, rate: 0 }
  } finally {
    item._loadingRecords = false
  }
}

async function toggleExpand(item: any) {
  if (expandedId.value === item.id) {
    expandedId.value = null
    return
  }
  expandedId.value = item.id
  await fetchRecordsForItem(item)
}

async function onPageChange(newPage: number) {
  expandedId.value = null
  page.value = newPage
  await fetchCheckIns()
}

async function fetchCheckIns() {
  loading.value = true
  try {
    const r = await getCheckIns({ page: page.value, size: pageSize })
    const records = r.data.records || []
    checkins.value = records
    total.value = r.data.total || 0
  } catch {
    ElMessage.error('加载签到列表失败')
    checkins.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchCheckIns()
})
</script>

<style scoped>
.slide-enter-active,
.slide-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}

.slide-enter-from,
.slide-leave-to {
  opacity: 0;
  max-height: 0;
  padding-top: 0;
  padding-bottom: 0;
}

.slide-enter-to,
.slide-leave-from {
  opacity: 1;
  max-height: 2000px;
}
</style>
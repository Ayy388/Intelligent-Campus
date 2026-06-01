<template>
  <div>
    <div class="animate__animated animate__fadeInDown">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md mb-5">
        <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-violet-500 to-purple-600 flex items-center justify-center shadow-sm">
              <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-bold text-ink">活动审核</h3>
              <p class="text-xs text-mist mt-0.5">审核社团社长、教师发起的校园活动</p>
            </div>
          </div>
          <div class="flex items-center gap-2">
            <span class="text-xs text-mist bg-wash px-3 py-1.5 rounded-lg">
              待处理 <span class="font-semibold text-ink ml-1">{{ activities.filter(a => a.status === 0).length }}</span>
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

    <div v-else-if="activities.length === 0" class="animate__animated animate__fadeInUp bg-white rounded-xl shadow-sm border border-gray-100 p-10">
      <div class="flex flex-col items-center justify-center py-10">
        <svg class="w-16 h-16 text-gray-200 mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
        </svg>
        <p class="text-sm text-mist">暂无待审核活动</p>
      </div>
    </div>

    <div v-else class="space-y-3">
      <div v-for="(a, i) in activities" :key="a.id"
        class="animate__animated animate__fadeInUp bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md"
        :style="{ animationDelay: `${0.05 * i}s` }">
        <div class="p-5">
          <div class="flex items-start justify-between gap-4">
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2.5 mb-2">
                <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white text-xs font-bold shrink-0 shadow-sm">
                  {{ (a.creatorName || '?').charAt(0) }}
                </div>
                <div>
                  <span class="text-sm font-semibold text-ink">{{ a.creatorName || '用户ID:' + a.creatorId }}</span>
                  <span class="text-xs text-mist ml-1.5">{{ creatorRoleLabel(a.creatorRole) }}</span>
                </div>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium"
                  :class="categoryClass(a.category)">{{ categoryLabel(a.category) }}</span>
                <span class="inline-flex items-center px-3 py-0.5 rounded-full text-xs font-semibold shadow-sm"
                  :class="statusBadgeClass(a.status)">
                  {{ statusLabel(a.status) }}
                </span>
              </div>
              <div class="ml-10">
                <h4 class="text-base font-semibold text-ink mb-1">{{ a.title }}</h4>
                <div class="flex items-center gap-4 text-xs text-mist mb-1">
                  <span class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/></svg>
                    {{ a.startTime?.substring(0,16) }} ~ {{ a.endTime?.substring(0,16) }}
                  </span>
                  <span v-if="a.location" class="flex items-center gap-1">
                    <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                    {{ a.location }}
                  </span>
                </div>
                <p class="text-sm text-steel line-clamp-2">{{ a.description || '暂无描述' }}</p>
                <div v-if="a.status === 2 && a.rejectReason" class="flex items-center gap-1 text-xs text-red-400 mt-1.5">
                  <svg class="w-3.5 h-3.5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
                  驳回原因: {{ a.rejectReason }}
                </div>
              </div>
            </div>
            <div v-if="a.status === 0" class="flex gap-2 shrink-0" @click.stop>
              <button @click="doApprove(a.id, 1)"
                class="px-4 py-2 bg-emerald-500 text-white rounded-lg text-xs font-semibold hover:bg-emerald-600 active:bg-emerald-700 transition-all duration-200 shadow-sm hover:shadow-md">
                通过
              </button>
              <button @click="startReject(a.id)"
                class="px-4 py-2 bg-red-400 text-white rounded-lg text-xs font-semibold hover:bg-red-500 active:bg-red-600 transition-all duration-200 shadow-sm hover:shadow-md">
                驳回
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="mt-4 flex justify-center animate__animated animate__fadeInUp">
      <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
        layout="prev,pager,next" small @current-change="fetchActivities" />
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
import { ref, onMounted } from 'vue'
import { getPendingActivities, approveActivity } from '@/api/activity'
import { ElMessage } from 'element-plus'

const activities = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10

const rejectVisible = ref(false)
const rejectId = ref<number | null>(null)
const rejectReason = ref('')

function categoryLabel(cat: string) {
  const m: Record<string, string> = { academic: '学术科技', sports: '体育竞技', cultural: '文化艺术', volunteer: '志愿服务', other: '其他' }
  return m[cat] || cat || '未分类'
}

function categoryClass(cat: string) {
  const m: Record<string, string> = {
    academic: 'bg-blue-50 text-blue-700 border border-blue-200',
    sports: 'bg-green-50 text-green-700 border border-green-200',
    cultural: 'bg-amber-50 text-amber-700 border border-amber-200',
    volunteer: 'bg-rose-50 text-rose-700 border border-rose-200',
    other: 'bg-gray-50 text-gray-700 border border-gray-200',
  }
  return m[cat] || 'bg-gray-50 text-gray-700 border border-gray-200'
}

function creatorRoleLabel(role: string) {
  if (role === 'admin') return '管理员'
  if (role === 'teacher') return '教师'
  if (role === 'club_president') return '社团社长'
  return role || ''
}

function statusLabel(status: number) {
  if (status === 0) return '审核中'
  if (status === 1) return '报名中'
  if (status === 2) return '已驳回'
  if (status === 3) return '活动中'
  if (status === 4) return '已结束'
  return '未知'
}

function statusBadgeClass(status: number) {
  if (status === 1) return 'bg-gradient-to-r from-emerald-500 to-teal-500 text-white'
  if (status === 2) return 'bg-gradient-to-r from-red-400 to-rose-500 text-white'
  if (status === 3) return 'bg-gradient-to-r from-blue-500 to-cyan-500 text-white'
  if (status === 4) return 'bg-gradient-to-r from-gray-400 to-gray-500 text-white'
  return 'bg-gradient-to-r from-amber-400 to-orange-500 text-white'
}

async function fetchActivities() {
  loading.value = true
  const r = await getPendingActivities({ page: page.value, size: pageSize })
  activities.value = r.data.records || []
  total.value = r.data.total || 0
  loading.value = false
}

async function doApprove(id: number, status: number) {
  await approveActivity(id, status)
  ElMessage.success('操作成功')
  fetchActivities()
}

function startReject(id: number) {
  rejectId.value = id
  rejectReason.value = ''
  rejectVisible.value = true
}

async function doReject() {
  if (!rejectReason.value) { ElMessage.warning('请输入驳回原因'); return }
  await approveActivity(rejectId.value!, 2, rejectReason.value)
  ElMessage.success('已驳回')
  rejectVisible.value = false
  fetchActivities()
}

onMounted(fetchActivities)
</script>

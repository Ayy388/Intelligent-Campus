<template>
  <div>
    <div class="animate__animated animate__fadeInUp flex items-center justify-between mb-6">
      <div>
        <h1 class="text-2xl font-bold text-gray-900 tracking-tight">活动广场</h1>
        <p class="text-sm text-gray-400 mt-1">浏览校园活动，发现精彩生活</p>
      </div>
      <div class="flex items-center gap-3">
        <el-select v-model="filterClubId" placeholder="全部活动" size="small" style="width:160px" clearable @change="fetchActivities">
          <el-option label="全校活动" :value="null" />
          <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
        <button @click="showCreate"
          v-if="canCreate"
          class="px-5 py-2 bg-gray-900 text-white rounded-xl text-sm font-medium hover:bg-gray-700 active:bg-gray-800 transition-all duration-200 shadow-sm hover:shadow-md">
          <span class="flex items-center gap-1.5">
            <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
            发起活动
          </span>
        </button>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20">
      <svg class="animate-spin h-8 w-8 text-gray-300" viewBox="0 0 24 24" fill="none">
        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
      </svg>
    </div>

    <div v-else-if="activities.length===0" class="text-center text-gray-400 py-24 animate__animated animate__fadeInUp">
      <svg class="w-16 h-16 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
      <p class="text-base">暂无开放活动</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      <div v-for="(a, i) in activities" :key="a.id" @click="showDetail(a)"
        :style="{ animationDelay: `${0.08 * (i + 1)}s` }"
        class="animate__animated animate__fadeInUp group bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-lg hover:shadow-gray-200/50 hover:-translate-y-0.5 transition-all duration-300 cursor-pointer overflow-hidden">
        <div class="relative h-32 bg-gradient-to-br from-indigo-500 to-purple-600 overflow-hidden">
          <div class="absolute inset-0 opacity-10" style="background-image: radial-gradient(circle at 20% 50%, white 0%, transparent 50%), radial-gradient(circle at 80% 20%, white 0%, transparent 50%), radial-gradient(circle at 40% 80%, white 0%, transparent 50%);"></div>
          <div class="absolute -top-6 -right-6 w-24 h-24 rounded-full bg-white/10"></div>
          <div class="absolute -bottom-4 -left-4 w-16 h-16 rounded-full bg-white/5"></div>
          <div class="absolute top-3 right-3 flex gap-2">
            <span v-if="a.category" class="px-2.5 py-1 rounded-full text-xs font-medium backdrop-blur-sm bg-white/20 text-white">
              {{ categoryLabel(a.category) }}
            </span>
          </div>
          <div class="absolute bottom-3 left-4">
            <h3 class="text-white font-bold text-lg drop-shadow-sm truncate max-w-[200px]">{{ a.title }}</h3>
          </div>
        </div>
        <div class="p-4">
          <div class="flex items-center gap-2 text-xs text-gray-400 mb-2">
            <span class="flex items-center gap-1">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
              {{ a.creatorName || '未知' }}
            </span>
            <span class="flex items-center gap-1" v-if="a.location">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
              {{ a.location }}
            </span>
            <span v-if="a.clubName" class="inline-flex items-center gap-1 px-2 py-0.5 rounded-full text-xs font-medium bg-indigo-50 text-indigo-600 border border-indigo-200">
              {{ a.clubName }}主办
            </span>
          </div>
          <div class="flex items-center gap-3 text-xs text-gray-400 mb-2">
            <span class="flex items-center gap-1">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
              {{ a.startTime?.substring(0,10) }} ~ {{ a.endTime?.substring(0,10) }}
            </span>
          </div>
          <p class="text-sm text-gray-500 line-clamp-2 mb-3 min-h-[2.5rem]">{{ a.description || '暂无描述' }}</p>
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-1 text-xs text-gray-400">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197"/></svg>
              <span>{{ (a.currentParticipants || 0) + 1 }}/{{ a.maxParticipants || '不限' }}</span>
            </div>
            <span v-if="a.registered"
              class="text-xs font-medium text-emerald-600 bg-emerald-50 px-2.5 py-1 rounded-full border border-emerald-200">
              已报名
            </span>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="mt-6 flex justify-center animate__animated animate__fadeInUp">
      <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
        layout="prev,pager,next" small @current-change="fetchActivities" />
    </div>

    <el-dialog v-model="detailVisible" width="600px" top="5vh" class="activity-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
          </div>
          <span class="text-base font-bold text-ink">{{ detail?.title }}</span>
        </div>
      </template>
      <div v-if="detail" class="px-2">
        <div class="grid grid-cols-2 gap-4 mb-5">
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">活动类别</div>
            <div class="text-sm font-semibold text-ink">{{ categoryLabel(detail.category) }}</div>
          </div>
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">当前状态</div>
            <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold shadow-sm"
              :class="statusBadgeClass(detail.status)">
              {{ statusLabel(detail.status) }}
            </span>
          </div>
        </div>
        <div class="grid grid-cols-2 gap-4 mb-5">
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">开始时间</div>
            <div class="text-sm font-semibold text-ink">{{ detail.startTime?.substring(0,16) || '待定' }}</div>
          </div>
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">结束时间</div>
            <div class="text-sm font-semibold text-ink">{{ detail.endTime?.substring(0,16) || '待定' }}</div>
          </div>
        </div>
        <div class="bg-wash rounded-xl p-4 mb-5">
          <div class="text-xs text-mist mb-1">活动地点</div>
          <div class="text-sm font-semibold text-ink">{{ detail.location || '未指定' }}</div>
        </div>
        <div class="bg-wash rounded-xl p-4 mb-5">
          <div class="text-xs text-mist mb-1">活动描述</div>
          <div class="text-sm text-steel leading-relaxed">{{ detail.description || '暂无描述' }}</div>
        </div>
        <div class="grid grid-cols-2 gap-4 mb-5">
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">发起人</div>
            <div class="text-sm font-semibold text-ink">{{ detail.creatorName || '未知' }}</div>
          </div>
          <div class="bg-wash rounded-xl p-4">
            <div class="text-xs text-mist mb-1">报名人数</div>
            <div class="text-sm font-semibold text-ink">{{ (detail.currentParticipants || 0) + 1 }}/{{ detail.maxParticipants || '不限' }}</div>
          </div>
        </div>
        <div v-if="detail.status === 2 && detail.rejectReason" class="bg-red-50 border border-red-100 rounded-xl p-4 mb-4">
          <div class="flex items-center gap-2 mb-1">
            <svg class="w-4 h-4 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <span class="text-xs font-semibold text-red-500">驳回原因</span>
          </div>
          <div class="text-sm text-red-600 ml-6">{{ detail.rejectReason }}</div>
        </div>

        <div class="bg-wash rounded-xl p-4 mb-5">
          <div class="flex items-center justify-between mb-3">
            <div class="text-xs font-semibold text-mist">报名人员</div>
            <span class="text-xs text-mist">{{ registrations.length }}人参与</span>
          </div>
          <div v-if="registrations.length === 0" class="text-sm text-gray-400 text-center py-3">暂无报名</div>
          <div v-else class="flex flex-wrap gap-2">
            <span v-for="r in registrations" :key="r.id"
              class="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-white border border-gray-100 text-sm text-gray-700">
              <svg class="w-3.5 h-3.5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"/></svg>
              {{ r.userName || '用户' + r.userId }}
              <span v-if="r.isCreator" class="text-xs text-indigo-500 bg-indigo-50 px-1.5 py-0.5 rounded font-medium">发起者</span>
            </span>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="detailVisible = false" class="!rounded-lg">关闭</el-button>
          <el-button v-if="detail?.status === 1 && !detail?.registered && detail?.creatorId !== userStore.userInfo?.id" type="primary" @click="doRegister(detail.id)" class="!rounded-lg !shadow-sm">
            立即报名
          </el-button>
          <el-button v-else-if="detail?.status === 1 && detail?.registered && detail?.creatorId !== userStore.userInfo?.id" type="danger" plain @click="doCancelRegister(detail.id)" class="!rounded-lg">
            取消报名
          </el-button>
          <span v-else-if="detail?.status === 1 && detail?.creatorId === userStore.userInfo?.id" class="text-xs text-gray-400 self-center">你是活动发起者</span>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="createVisible" width="560px" top="5vh" class="activity-dialog" :close-on-click-modal="false">
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-8 h-8 rounded-lg bg-gradient-to-br from-emerald-500 to-teal-500 flex items-center justify-center">
            <svg class="w-4 h-4 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/></svg>
          </div>
          <span class="text-base font-bold text-ink">发起活动</span>
        </div>
      </template>
      <el-form :model="form" label-position="top" class="px-2">
        <el-form-item v-if="userStore.role !== 'admin' && userStore.role !== 'teacher'" label="所属社团">
          <el-select v-model="form.clubId" placeholder="请选择社团（仅限你加入的社团）" class="!w-full">
            <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="活动范围">
          <el-select v-model="form.clubId" placeholder="选择范围" class="!w-full" clearable>
            <el-option :value="null" label="全校活动" />
            <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动标题">
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="活动类别">
            <el-select v-model="form.category" placeholder="请选择类别" class="!w-full">
              <el-option label="学术科技" value="academic" />
              <el-option label="体育竞技" value="sports" />
              <el-option label="文化艺术" value="cultural" />
              <el-option label="志愿服务" value="volunteer" />
              <el-option label="其他" value="other" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动地点">
            <el-input v-model="form.location" placeholder="请输入地点" />
          </el-form-item>
        </div>
        <div class="grid grid-cols-2 gap-4">
          <el-form-item label="开始时间">
            <el-date-picker v-model="form.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择开始时间" class="!w-full" />
          </el-form-item>
          <el-form-item label="结束时间">
            <el-date-picker v-model="form.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" placeholder="选择结束时间" class="!w-full" />
          </el-form-item>
        </div>
        <el-form-item label="人数上限（0=不限）">
          <el-input-number v-model="form.maxParticipants" :min="0" :max="9999" class="!w-full" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请详细描述活动内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="createVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="primary" @click="doCreate" class="!rounded-lg !shadow-sm">提交审核</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getPublicActivities, createActivity, registerActivity, cancelRegistration, getActivity, getClubs, getRegistrations } from '@/api/activity'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { categoryLabel } from '@/utils/labels'

const userStore = useUserStore()
const activities = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10
const filterClubId = ref<number | null>(null)
const clubList = ref<any[]>([])

const detailVisible = ref(false)
const detail = ref<any>(null)
const registrations = ref<any[]>([])

const createVisible = ref(false)
const form = reactive({ clubId: null as number | null, title: '', category: '', location: '', startTime: '', endTime: '', maxParticipants: 0, description: '' })

const canCreate = computed(() => ['admin', 'teacher'].includes(userStore.role) || userStore.role === 'student')

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
  const params: any = { page: page.value, size: pageSize }
  if (filterClubId.value) params.clubId = filterClubId.value
  const r = await getPublicActivities(params)
  activities.value = r.data.records || []
  total.value = r.data.total || 0
  loading.value = false
}

async function showDetail(a: any) {
  try {
    const r = await getActivity(a.id)
    detail.value = r.data
    const regRes = await getRegistrations(a.id, { page: 1, size: 999 })
    const list = regRes.data.records || []
    const hasCreator = list.some((x: any) => x.userId === r.data.creatorId)
    if (!hasCreator) {
      list.unshift({ id: -1, userId: r.data.creatorId, userName: r.data.creatorName || '发起者', isCreator: true })
    } else {
      list.forEach((x: any) => { if (x.userId === r.data.creatorId) x.isCreator = true })
    }
    registrations.value = list
    detailVisible.value = true
  } catch {
    ElMessage.error('获取活动详情失败')
  }
}

function showCreate() {
  form.clubId = null; form.title = ''; form.category = ''; form.location = ''; form.startTime = ''; form.endTime = ''
  form.maxParticipants = 0; form.description = ''
  createVisible.value = true
}

async function doCreate() {
  if (!form.title) { ElMessage.warning('请输入活动标题'); return }
  await createActivity(form)
  ElMessage.success('提交成功，等待管理员审核')
  createVisible.value = false
  fetchActivities()
}

async function doRegister(id: number) {
  await registerActivity(id)
  ElMessage.success('报名成功')
  detailVisible.value = false
  fetchActivities()
}

async function doCancelRegister(id: number) {
  await cancelRegistration(id)
  ElMessage.success('已取消报名')
  detailVisible.value = false
  fetchActivities()
}

onMounted(() => {
  fetchActivities()
  getClubs().then(r => { clubList.value = r.data || [] })
})
</script>

<style scoped>
.activity-dialog :deep(.el-dialog__body) {
  padding-top: 8px;
}
</style>

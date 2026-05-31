<template>
  <div>
    <div class="animate__animated animate__fadeInUp flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold text-gray-900 tracking-tight">社团列表</h1>
      <button @click="showCreate"
        class="px-5 py-2 bg-gray-900 text-white rounded-xl text-sm font-medium hover:bg-gray-700 active:bg-gray-800 transition-all duration-200 shadow-sm hover:shadow-md">
        <span class="flex items-center gap-1.5">
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"/></svg>
          创建社团
        </span>
      </button>
    </div>

    <div class="flex gap-2 mb-6 animate__animated animate__fadeInUp" style="animation-delay:0.06s">
      <button v-for="tab in tabs" :key="tab.key" @click="activeTab=tab.key;page=1;fetchClubs()"
        class="px-5 py-2 rounded-xl text-sm font-medium transition-all duration-200"
        :class="activeTab===tab.key?'bg-gray-900 text-white shadow-md shadow-gray-900/10':'bg-white text-gray-500 border border-gray-200 hover:border-gray-300 hover:text-gray-700'">
        {{ tab.label }}
      </button>
    </div>

    <div v-if="clubs.length===0" class="text-center text-gray-400 py-24 animate__animated animate__fadeInUp">
      <svg class="w-16 h-16 mx-auto mb-4 text-gray-200" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="1" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
      <p class="text-base">暂无社团数据</p>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      <div v-for="(c, i) in clubs" :key="c.id" @click="openDetail(c)"
        :style="{ animationDelay: `${0.08 * (i + 1)}s` }"
        class="animate__animated animate__fadeInUp group bg-white rounded-2xl border border-gray-100 shadow-sm hover:shadow-lg hover:shadow-gray-200/50 hover:-translate-y-0.5 transition-all duration-300 cursor-pointer overflow-hidden">
        <div class="relative h-28 overflow-hidden" :style="{ background: bannerGradients[(c.id || i) % bannerGradients.length] }">
          <div class="absolute inset-0 opacity-20" :style="{ background: overlayPattern }"></div>
          <div class="absolute -top-6 -right-6 w-24 h-24 rounded-full bg-white/10"></div>
          <div class="absolute -bottom-4 -left-4 w-16 h-16 rounded-full bg-white/5"></div>
          <div class="absolute bottom-3 left-4">
            <div class="w-12 h-12 rounded-full flex items-center justify-center text-white text-lg font-bold shadow-lg border-2 border-white/40"
              :style="{ background: avatarGradients[(c.id || i) % avatarGradients.length] }">
              {{ (c.name || '?').charAt(0) }}
            </div>
          </div>
          <div class="absolute top-3 right-3">
            <span class="px-2.5 py-1 rounded-full text-xs font-medium backdrop-blur-sm"
              :class="statusClass(c.status)">
              {{ statusLabel(c.status) }}
            </span>
          </div>
        </div>
        <div class="p-4">
          <h3 class="text-base font-semibold text-gray-900 mb-1 truncate group-hover:text-gray-700 transition-colors">{{ c.name }}</h3>
          <p class="text-sm text-gray-400 line-clamp-2 mb-3 leading-relaxed min-h-[2.5rem]">{{ c.description || '暂无简介' }}</p>
          <div class="flex items-center justify-between text-xs">
            <div class="flex items-center gap-1.5 text-gray-400">
              <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197m13.5-9a2.25 2.25 0 11-4.5 0 2.25 2.25 0 014.5 0z"/></svg>
              <span>{{ c.memberCount || 0 }} 名成员</span>
            </div>
            <div v-if="userStore.role==='admin'" class="flex gap-2 opacity-0 group-hover:opacity-100 transition-opacity duration-200">
              <span @click.stop="showEdit(c)" class="text-gray-400 hover:text-gray-700 transition-colors cursor-pointer">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"/></svg>
              </span>
              <span @click.stop="doDeleteClub(c.id)" class="text-gray-400 hover:text-red-400 transition-colors cursor-pointer">
                <svg class="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/></svg>
              </span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total>pageSize" class="mt-6 flex justify-center animate__animated animate__fadeInUp">
      <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
        layout="prev,pager,next" small @current-change="fetchClubs" />
    </div>

    <el-dialog v-model="detailVisible" width="640px" top="5vh">
      <template #header>
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl flex items-center justify-center text-white text-base font-bold shadow-sm"
            :style="{ background: detailClub ? avatarGradients[detailClub.id % avatarGradients.length] : '#667eea' }">
            {{ detailClub ? (detailClub.name || '?').charAt(0) : '?' }}
          </div>
          <div>
            <div class="text-lg font-bold text-gray-900">{{ detailClub?.name }}</div>
            <span class="px-2 py-0.5 rounded-full text-xs font-medium"
              :class="detailClub ? statusClass(detailClub.status) : ''">
              {{ detailClub ? statusLabel(detailClub.status) : '' }}
            </span>
          </div>
        </div>
      </template>

      <div class="space-y-5">
        <div class="bg-gray-50 rounded-xl p-4">
          <p class="text-sm text-gray-600 leading-relaxed">{{ detailClub?.description || '暂无简介' }}</p>
        </div>

        <div class="grid grid-cols-3 gap-3">
          <div class="bg-gray-50 rounded-xl p-3 text-center">
            <div class="text-lg font-bold text-gray-900">{{ detailClub?.memberCount || 0 }}</div>
            <div class="text-xs text-gray-400 mt-0.5">成员</div>
          </div>
          <div v-if="detailClub?.advisorName" class="bg-gray-50 rounded-xl p-3 text-center">
            <div class="text-sm font-semibold text-gray-900 truncate">{{ detailClub?.advisorName }}</div>
            <div class="text-xs text-gray-400 mt-0.5">指导老师</div>
          </div>
          <div v-if="detailClub?.presidentName" class="bg-gray-50 rounded-xl p-3 text-center">
            <div class="text-sm font-semibold text-gray-900 truncate">{{ detailClub?.presidentName }}</div>
            <div class="text-xs text-gray-400 mt-0.5">社长</div>
          </div>
        </div>

        <div v-if="userStore.role==='admin' && detailClub?.status===0" class="flex gap-2">
          <button @click="doApproveClub(detailClub.id,1)" class="flex-1 px-4 py-2.5 bg-gray-900 text-white rounded-xl text-sm font-medium hover:bg-gray-700 transition-all duration-200 shadow-sm">
            审核通过
          </button>
          <button @click="doApproveClub(detailClub.id,2)" class="flex-1 px-4 py-2.5 border border-gray-200 text-gray-500 rounded-xl text-sm font-medium hover:bg-gray-50 hover:text-gray-700 transition-all duration-200">
            拒绝
          </button>
        </div>

        <div v-if="userStore.role==='admin' && detailClub?.status===3" class="flex gap-2">
          <button @click="doApproveDisband(detailClub.id,1)" class="flex-1 px-4 py-2.5 bg-red-500 text-white rounded-xl text-sm font-medium hover:bg-red-600 transition-all duration-200 shadow-sm">
            确认解散
          </button>
          <button @click="doApproveDisband(detailClub.id,0)" class="flex-1 px-4 py-2.5 border border-gray-200 text-gray-500 rounded-xl text-sm font-medium hover:bg-gray-50 hover:text-gray-700 transition-all duration-200">
            拒绝解散
          </button>
        </div>

        <div v-if="userStore.role==='student' && detailClub?.status===1" class="flex gap-2">
          <button v-if="!myMemberInfo" @click="startApply(detailClub?.id)" class="flex-1 px-4 py-2.5 bg-gray-900 text-white rounded-xl text-sm font-medium hover:bg-gray-700 transition-all duration-200 shadow-sm">
            申请加入
          </button>
          <button v-else-if="myMemberInfo.status===0" disabled class="flex-1 px-4 py-2.5 bg-gray-300 text-white rounded-xl text-sm font-medium cursor-not-allowed">
            申请中
          </button>
          <button v-else disabled class="flex-1 px-4 py-2.5 bg-gray-300 text-white rounded-xl text-sm font-medium cursor-not-allowed">
            已加入
          </button>
        </div>

        <div v-if="detailClub?.status===1 || detailClub?.status===3">
          <button @click="goToSpace" class="w-full px-4 py-2.5 border border-gray-200 text-gray-600 rounded-xl text-sm font-medium hover:bg-gray-50 hover:text-gray-900 transition-all duration-200">
            进入社团空间
          </button>
        </div>

        <div>
          <div class="flex items-center justify-between mb-3">
            <h4 class="text-sm font-semibold text-gray-900">成员列表</h4>
            <span class="text-xs text-gray-400">{{ members.length }} 人</span>
          </div>
          <div v-if="members.length===0" class="text-center text-gray-400 py-8 text-sm bg-gray-50 rounded-xl">
            暂无成员
          </div>
          <div v-for="m in members" :key="m.id" class="flex items-center justify-between py-2.5 border-b border-gray-100 last:border-0">
            <div class="flex items-center gap-3">
              <div class="w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold flex-shrink-0"
                :style="{ background: memberColors[(m.id || 0) % memberColors.length] }">
                {{ (m.userName || '?').charAt(0) }}
              </div>
              <div>
                <span class="text-sm font-medium text-gray-900">{{ m.userName || '未知用户' }}</span>
                <span v-if="m.role==='president'" class="ml-1.5 text-xs bg-red-50 text-red-500 px-1.5 py-0.5 rounded-md">社长</span>
                <span v-else-if="m.role==='vice_president'" class="ml-1.5 text-xs bg-amber-50 text-amber-600 px-1.5 py-0.5 rounded-md">副社长</span>
              </div>
            </div>
            <div v-if="(userStore.role==='admin'||userStore.role==='teacher') && m.status===0" class="flex gap-2">
              <button @click="doApproveMember(m.id,1)" class="px-3 py-1.5 text-xs bg-gray-900 text-white rounded-lg hover:bg-gray-700 transition-colors">通过</button>
              <button @click="doApproveMember(m.id,2)" class="px-3 py-1.5 text-xs border border-gray-200 text-gray-500 rounded-lg hover:bg-gray-50 transition-colors">拒绝</button>
            </div>
          </div>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="createVisible" title="创建社团" width="480px" top="20vh">
      <el-form :model="createForm" label-position="top">
        <el-form-item label="社团名称">
          <el-input v-model="createForm.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团描述">
          <el-input v-model="createForm.description" type="textarea" :rows="4" placeholder="请输入社团描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applyVisible" title="申请加入" width="480px" top="25vh">
      <el-form label-position="top">
        <el-form-item label="申请理由">
          <el-input v-model="applyReason" type="textarea" :rows="4" placeholder="请输入申请理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible=false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑社团" width="480px" top="20vh">
      <el-form :model="editForm" label-position="top">
        <el-form-item label="社团名称"><el-input v-model="editForm.name" placeholder="请输入社团名称" /></el-form-item>
        <el-form-item label="社团描述"><el-input v-model="editForm.description" type="textarea" :rows="4" placeholder="请输入社团描述" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="doEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getClubs, getMyMemberships, createClub, updateClub, deleteClub, applyMember, approveMember, getMembers, approveClub, approveDisband } from '@/api/club'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()

const tabs = [
  { key: 'all', label: '全部社团' },
  { key: 'my', label: '我的社团' },
]
const activeTab = ref('all')
const clubs = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const pageSize = 12

const detailVisible = ref(false)
const detailClub = ref<any>(null)
const members = ref<any[]>([])
const myMemberInfo = ref<any>(null)

const createVisible = ref(false)
const createForm = reactive({ name: '', description: '' })

const applyVisible = ref(false)
const applyClubId = ref<number | null>(null)
const applyReason = ref('')

const editVisible = ref(false)
const editForm = reactive({ name: '', description: '' })
const editClubId = ref<number | null>(null)

const avatarGradients = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
  'linear-gradient(135deg, #fccb90, #d57eeb)',
  'linear-gradient(135deg, #30cfd0, #330867)',
  'linear-gradient(135deg, #a8edea, #fed6e3)',
  'linear-gradient(135deg, #ffecd2, #fcb69f)',
]

const bannerGradients = [
  'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #9f7aea 100%)',
  'linear-gradient(135deg, #f093fb 0%, #f5576c 50%, #ff6f91 100%)',
  'linear-gradient(135deg, #4facfe 0%, #00f2fe 50%, #43e97b 100%)',
  'linear-gradient(135deg, #43e97b 0%, #38f9d7 50%, #a8edea 100%)',
  'linear-gradient(135deg, #fa709a 0%, #fee140 50%, #f6d365 100%)',
  'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 50%, #fccb90 100%)',
  'linear-gradient(135deg, #fccb90 0%, #d57eeb 50%, #667eea 100%)',
  'linear-gradient(135deg, #30cfd0 0%, #330867 50%, #764ba2 100%)',
  'linear-gradient(135deg, #a8edea 0%, #fed6e3 50%, #ffecd2 100%)',
  'linear-gradient(135deg, #ffecd2 0%, #fcb69f 50%, #f093fb 100%)',
]

const overlayPattern = 'radial-gradient(circle at 30% 70%, rgba(255,255,255,0.15) 0%, transparent 50%), radial-gradient(circle at 70% 20%, rgba(255,255,255,0.1) 0%, transparent 50%)'

const memberColors = [
  'linear-gradient(135deg, #667eea, #764ba2)',
  'linear-gradient(135deg, #f093fb, #f5576c)',
  'linear-gradient(135deg, #4facfe, #00f2fe)',
  'linear-gradient(135deg, #43e97b, #38f9d7)',
  'linear-gradient(135deg, #fa709a, #fee140)',
  'linear-gradient(135deg, #a18cd1, #fbc2eb)',
]

function statusClass(status: number) {
  if (status === 1) return 'bg-emerald-50 text-emerald-600 border border-emerald-200'
  if (status === 2) return 'bg-red-50 text-red-500 border border-red-200'
  if (status === 3) return 'bg-orange-50 text-orange-600 border border-orange-200'
  return 'bg-gray-100 text-gray-400 border border-gray-200'
}

function statusLabel(status: number) {
  if (status === 1) return '正常'
  if (status === 2) return '已解散'
  if (status === 3) return '申请解散'
  return '待审核'
}

async function fetchClubs() {
  const r = await getClubs()
  let data = r.data || []
  if (activeTab.value === 'my') {
    try {
      const memberships = await getMyMemberships()
      const myClubIds = new Set((memberships.data || []).map((m: any) => m.clubId))
      data = data.filter((c: any) => myClubIds.has(c.id) && (c.status === 1 || c.status === 3))
    } catch { data = [] }
  }
  total.value = data.length
  const start = (page.value - 1) * pageSize
  clubs.value = data.slice(start, start + pageSize)
}

async function openDetail(club: any) {
  detailClub.value = club
  detailVisible.value = true
  await fetchMembers(club.id)
}

async function fetchMembers(clubId: number) {
  try {
    const r = await getMembers(clubId)
    members.value = r.data || []
    myMemberInfo.value = members.value.find((m: any) => m.userId === userStore.userInfo?.id)
  } catch { members.value = [] }
}

function showCreate() {
  createForm.name = ''
  createForm.description = ''
  createVisible.value = true
}

async function doCreate() {
  if (!createForm.name.trim()) { ElMessage.warning('请输入社团名称'); return }
  try {
    await createClub(createForm)
    ElMessage.success('创建成功，请等待审核')
    createVisible.value = false
    fetchClubs()
  } catch { ElMessage.error('创建失败') }
}

function startApply(clubId: number | undefined) {
  if (!clubId) return
  applyClubId.value = clubId
  applyReason.value = ''
  applyVisible.value = true
}

async function submitApply() {
  if (!applyClubId.value) return
  try {
    await applyMember(applyClubId.value, applyReason.value)
    ElMessage.success('申请已提交')
    applyVisible.value = false
    if (detailClub.value) fetchMembers(detailClub.value.id)
  } catch { ElMessage.error('申请失败') }
}

async function doApproveMember(id: number, status: number) {
  await approveMember(id, status)
  ElMessage.success(status === 1 ? '已通过' : '已拒绝')
  if (detailClub.value) fetchMembers(detailClub.value.id)
}

async function doApproveClub(id: number, status: number) {
  await approveClub(id, status)
  ElMessage.success(status === 1 ? '已通过' : '已拒绝')
  fetchClubs()
  detailVisible.value = false
}

async function doApproveDisband(id: number, status: number) {
  try {
    await approveDisband(id, status)
    ElMessage.success(status === 1 ? '已确认解散' : '已拒绝解散')
    fetchClubs()
    detailVisible.value = false
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function goToSpace() {
  if (detailClub.value) {
    detailVisible.value = false
    router.push(`/club/space/${detailClub.value.id}`)
  }
}

function showEdit(c: any) { editClubId.value = c.id; editForm.name = c.name; editForm.description = c.description || ''; editVisible.value = true }

async function doEdit() {
  if (!editClubId.value) return
  if (!editForm.name.trim()) { ElMessage.warning('请输入社团名称'); return }
  try { await updateClub(editClubId.value, editForm); ElMessage.success('修改成功'); editVisible.value = false; fetchClubs() } catch { ElMessage.error('修改失败') }
}

async function doDeleteClub(id: number) {
  try { await ElMessageBox.confirm('确认删除该社团？', '提示', { type: 'warning' }) } catch { return }
  try { await deleteClub(id); ElMessage.success('已删除'); fetchClubs() } catch { ElMessage.error('删除失败') }
}

onMounted(fetchClubs)
</script>

<style>
.club-card-enter {
  opacity: 0;
  transform: translateY(12px);
}
</style>
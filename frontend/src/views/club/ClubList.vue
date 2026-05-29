<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <span class="text-lg font-bold text-ink">社团列表</span>
      <button @click="showCreate" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">创建社团</button>
    </div>

    <div class="flex gap-2 mb-5">
      <button v-for="tab in tabs" :key="tab.key" @click="activeTab=tab.key;fetchClubs()"
        class="px-4 py-1.5 rounded-lg text-sm font-medium border transition-all"
        :class="activeTab===tab.key?'bg-ink text-white border-ink':'bg-white text-steel border-soft hover:border-line'">
        {{ tab.label }}
      </button>
    </div>

    <div v-if="clubs.length===0" class="text-center text-mist py-20">暂无社团数据</div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
      <div v-for="c in clubs" :key="c.id" @click="openDetail(c)"
        class="bg-white rounded-xl border border-soft p-5 hover:shadow-sm transition-shadow cursor-pointer">
        <div class="flex items-start justify-between mb-2">
          <div class="text-base font-semibold text-ink">{{ c.name }}</div>
          <div class="flex items-center gap-1">
            <button v-if="userStore.role==='admin'" @click.stop="showEdit(c)" class="text-xs text-mist hover:text-ink transition-colors px-1">编辑</button>
            <button v-if="userStore.role==='admin'" @click.stop="doDeleteClub(c.id)" class="text-xs text-mist hover:text-red-400 transition-colors px-1">删除</button>
            <el-tag size="small" :type="c.status===1?'success':c.status===2?'danger':c.status===3?'warning':'info'">
              {{ c.status===1?'正常':c.status===2?'已解散':c.status===3?'申请解散':'待审核' }}
            </el-tag>
          </div>
        </div>
        <div class="text-xs text-ash mb-3 line-clamp-2">{{ c.description || '暂无简介' }}</div>
        <div class="flex items-center justify-between text-xs text-mist">
          <span>{{ c.memberCount || 0 }} 名成员</span>
          <span v-if="c.advisorName">指导老师: {{ c.advisorName }}</span>
        </div>
      </div>
    </div>

    <el-pagination v-if="total>pageSize" v-model:current-page="page" :total="total" :page-size="pageSize"
      layout="prev,pager,next" small @current-change="fetchClubs" />

    <el-dialog v-model="detailVisible" :title="detailClub?.name" width="600px">
      <div class="mb-4">
        <div class="text-sm text-steel mb-2">{{ detailClub?.description || '暂无简介' }}</div>
        <div class="flex gap-4 text-xs text-mist">
          <span>{{ detailClub?.memberCount || 0 }} 名成员</span>
          <span v-if="detailClub?.advisorName">指导老师: {{ detailClub?.advisorName }}</span>
          <span v-if="detailClub?.presidentName">社长: {{ detailClub?.presidentName }}</span>
        </div>
      </div>

      <div v-if="userStore.role==='admin' && detailClub?.status===0" class="mb-4">
        <button @click="doApproveClub(detailClub.id,1)" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors mr-2">
          审核通过
        </button>
        <button @click="doApproveClub(detailClub.id,2)" class="px-4 py-1.5 border border-soft text-ash rounded-lg text-xs font-medium hover:bg-wash transition-colors">
          拒绝
        </button>
      </div>

      <div v-if="userStore.role==='admin' && detailClub?.status===3" class="mb-4">
        <button @click="doApproveDisband(detailClub.id,1)" class="px-4 py-1.5 bg-red-500 text-white rounded-lg text-xs font-medium hover:bg-red-600 transition-colors mr-2">
          确认解散
        </button>
        <button @click="doApproveDisband(detailClub.id,0)" class="px-4 py-1.5 border border-soft text-ash rounded-lg text-xs font-medium hover:bg-wash transition-colors">
          拒绝解散
        </button>
      </div>

      <div v-if="userStore.role==='student' && detailClub?.status===1" class="mb-4">
        <button v-if="!myMemberInfo" @click="startApply(detailClub?.id)" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">
          申请加入
        </button>
        <button v-else-if="myMemberInfo.status===0" disabled class="px-4 py-1.5 bg-ink/50 text-white rounded-lg text-xs font-medium cursor-not-allowed">
          申请中
        </button>
        <button v-else disabled class="px-4 py-1.5 bg-ink/50 text-white rounded-lg text-xs font-medium cursor-not-allowed">
          已加入
        </button>
      </div>

      <div v-if="detailClub?.status===1" class="mb-4">
        <button @click="goToSpace" class="px-4 py-1.5 border border-soft text-steel rounded-lg text-xs font-medium hover:bg-wash transition-colors">
          进入社团空间
        </button>
      </div>

      <div class="text-sm font-semibold text-ink mb-3">成员列表</div>
      <div v-if="members.length===0" class="text-center text-mist py-6 text-sm">暂无成员</div>
      <div v-for="m in members" :key="m.id" class="flex items-center justify-between py-2 border-b border-wash last:border-0">
        <div class="text-sm">
          <span class="text-ink font-medium">{{ m.userName || '未知用户' }}</span>
          <el-tag size="small" class="ml-2" :type="m.role==='president'?'danger':m.role==='vice_president'?'warning':''">
            {{ m.role==='president'?'社长':m.role==='vice_president'?'副社长':'成员' }}
          </el-tag>
          <el-tag size="small" class="ml-1" :type="m.status===1?'success':'warning'">
            {{ m.status===1?'已通过':'待审核' }}
          </el-tag>
        </div>
        <div v-if="(userStore.role==='admin'||userStore.role==='teacher') && m.status===0" class="flex gap-2">
          <button @click="doApproveMember(m.id,1)" class="px-3 py-1 text-xs bg-ink text-white rounded-lg hover:bg-steel transition-colors">通过</button>
          <button @click="doApproveMember(m.id,2)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">拒绝</button>
        </div>
      </div>

      <template #footer>
        <el-button @click="detailVisible=false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createVisible" title="创建社团" width="450px">
      <el-form :model="createForm">
        <el-form-item label="社团名称">
          <el-input v-model="createForm.name" placeholder="请输入社团名称" />
        </el-form-item>
        <el-form-item label="社团描述">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入社团描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="applyVisible" title="申请加入" width="450px">
      <el-form>
        <el-form-item label="申请理由">
          <el-input v-model="applyReason" type="textarea" :rows="3" placeholder="请输入申请理由" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible=false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑社团" width="450px">
      <el-form :model="editForm">
        <el-form-item label="社团名称"><el-input v-model="editForm.name" placeholder="请输入社团名称" /></el-form-item>
        <el-form-item label="社团描述"><el-input v-model="editForm.description" type="textarea" :rows="3" placeholder="请输入社团描述" /></el-form-item>
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
import { getClubs, getMyMemberships, createClub, updateClub, deleteClub, applyMember, approveMember, getMembers, approveClub } from '@/api/club'
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

async function fetchClubs() {
  const r = await getClubs()
  let data = r.data || []
  if (activeTab.value === 'my') {
    try {
      const memberships = await getMyMemberships()
      const myClubIds = new Set((memberships.data || []).map((m: any) => m.clubId))
      data = data.filter((c: any) => myClubIds.has(c.id) && c.status === 1)
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

const editVisible = ref(false)
const editForm = reactive({ name: '', description: '' })
const editClubId = ref<number | null>(null)
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
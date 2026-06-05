<template>
  <div v-loading="loading" class="animate__animated animate__fadeInUp">
    <div class="flex items-center mb-5">
      <button @click="$router.back()" class="mr-3 text-steel hover:text-ink">
        <el-icon size="20"><ArrowLeft /></el-icon>
      </button>
      <h2 class="text-lg font-bold text-ink">{{ club?.name || '社团空间' }}</h2>
    </div>

    <div v-if="club" class="grid grid-cols-1 lg:grid-cols-4 gap-5">
      <div class="lg:col-span-3">
        <el-tabs v-model="activeTab" class="mb-4">
          <el-tab-pane label="社团概况" name="overview">
            <div class="bg-white rounded-xl border border-soft p-5 mb-5">
              <div class="flex items-center mb-4">
                <div v-if="club.logo" class="w-16 h-16 rounded-lg bg-soft flex items-center justify-center mr-4 overflow-hidden">
                  <img :src="club.logo" class="w-full h-full object-cover" />
                </div>
                <div v-else class="w-16 h-16 rounded-lg bg-ink/10 flex items-center justify-center mr-4">
                  <el-icon size="32" class="text-ink/50"><OfficeBuilding /></el-icon>
                </div>
                <div>
                  <h3 class="text-xl font-semibold text-ink">{{ club.name }}</h3>
                  <p class="text-sm text-ash mt-1">{{ club.description }}</p>
                </div>
              </div>
              <div class="flex gap-6 text-sm text-steel">
                <span><el-icon class="mr-1"><User /></el-icon> {{ club.memberCount || 0 }} 名成员</span>
                <span v-if="club.presidentName"><el-icon class="mr-1"><UserFilled /></el-icon> 社长: {{ club.presidentName }}</span>
                <span v-if="club.advisorName"><el-icon class="mr-1"><Document /></el-icon> 指导老师: {{ club.advisorName }}</span>
              </div>
            </div>

            <div class="bg-white rounded-xl border border-soft p-5">
              <div class="flex items-center justify-between mb-4">
                <h4 class="text-sm font-semibold text-ink">社团成员</h4>
                <span class="text-xs text-mist">{{ approvedMembers.length }} 人</span>
              </div>
              <div v-if="approvedMembers.length === 0" class="text-center text-mist py-8 text-sm">暂无成员</div>
              <div v-else class="space-y-3">
                <div v-for="m in approvedMembers" :key="m.id" class="flex items-center justify-between py-2 border-b border-wash last:border-0">
                  <div class="flex items-center">
                    <div class="w-10 h-10 rounded-full bg-ink/10 flex items-center justify-center mr-3">
                      <el-icon class="text-ink/50"><User /></el-icon>
                    </div>
                    <div>
                      <div class="text-sm font-medium text-ink">{{ m.userName || '未知用户' }}</div>
                      <div class="text-xs text-mist">
                        <el-tag size="small" :type="m.role==='president'?'danger':m.role==='vice_president'?'warning':''" class="mr-2">
                          {{ m.role==='president'?'社长':m.role==='vice_president'?'副社长':'成员' }}
                        </el-tag>
                      </div>
                    </div>
                  </div>
                  <div v-if="isPresident && m.userId !== userStore.userInfo?.id" class="flex gap-1">
                    <el-dropdown trigger="click" @command="(cmd:string) => handleMemberAction(cmd, m)">
                      <button class="px-2 py-1 text-sm text-steel hover:text-ink rounded hover:bg-wash transition-colors">
                        <el-icon><MoreFilled /></el-icon>
                      </button>
                      <template #dropdown>
                        <el-dropdown-menu>
                          <el-dropdown-item command="remove" class="text-red-500">移除</el-dropdown-item>
                          <el-dropdown-item v-if="m.role!=='vice_president'" command="setVice">设为副社长</el-dropdown-item>
                          <el-dropdown-item v-if="m.role==='vice_president'" command="unsetVice">取消副社长</el-dropdown-item>
                          <el-dropdown-item command="transfer">转让社长</el-dropdown-item>
                        </el-dropdown-menu>
                      </template>
                    </el-dropdown>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane label="社团活动" name="activities">
            <div class="bg-white rounded-xl border border-soft p-5">
              <div class="flex items-center justify-between mb-4">
                <h4 class="text-sm font-semibold text-ink">活动动态</h4>
                <button v-if="isPresident" @click="showCreateActivity = true"
                  class="px-3 py-1.5 bg-gray-900 text-white rounded-lg text-xs font-medium hover:bg-gray-700 transition-colors">
                  创建活动
                </button>
              </div>
              <div v-if="activities.length === 0" class="text-center text-mist py-8 text-sm">
                <p>暂无活动</p>
              </div>
              <div v-else class="space-y-4">
                <div v-for="a in activities" :key="a.id" class="flex items-start gap-4 p-4 bg-gray-50 rounded-xl">
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center gap-2 mb-1">
                      <span class="text-sm font-semibold text-ink">{{ a.title }}</span>
                      <el-tag size="small" :type="a.status===1?'success':a.status===4?'info':a.status===2?'danger':'warning'">
                        {{ a.status===1?'报名中':a.status===3?'活动中':a.status===4?'已结束':a.status===2?'已驳回':'待审核' }}
                      </el-tag>
                    </div>
                    <div class="text-xs text-mist space-y-0.5">
                      <div v-if="a.category">类别: {{ categoryLabel(a.category) }}</div>
                      <div v-if="a.location">地点: {{ a.location }}</div>
                      <div v-if="a.startTime">{{ a.startTime.substring(0,16) }} ~ {{ a.endTime?.substring(0,16) }}</div>
                      <div v-if="a.creatorName">发起人: {{ a.creatorName }}</div>
                    </div>
                    <div v-if="a.maxParticipants" class="mt-2 flex items-center gap-2">
                      <el-progress :percentage="Math.round(((a.currentParticipants||0)+1)/a.maxParticipants*100)" :stroke-width="6" class="flex-1" />
                      <span class="text-xs text-mist whitespace-nowrap">{{ (a.currentParticipants||0)+1 }}/{{ a.maxParticipants }}</span>
                    </div>
                    <div v-if="a.description" class="mt-1 text-xs text-gray-400 line-clamp-2">{{ a.description }}</div>
                    <div v-if="a.rejectReason && a.status===2" class="mt-1 text-xs text-red-500">驳回原因: {{ a.rejectReason }}</div>
                  </div>
                  <div class="flex flex-col gap-2 shrink-0">
                    <button v-if="a.status===1 && !a.registered && a.creatorId !== userStore.userInfo?.id"
                      @click="doRegister(a.id)" class="px-3 py-1 text-xs bg-gray-900 text-white rounded-lg hover:bg-gray-700">
                      报名
                    </button>
                    <button v-else-if="a.status===1 && a.registered && a.creatorId !== userStore.userInfo?.id"
                      @click="doCancelRegister(a.id)" class="px-3 py-1 text-xs border border-gray-200 text-gray-500 rounded-lg hover:bg-gray-50">
                      取消报名
                    </button>
                    <button v-if="a.status===0 && isPresident"
                      @click="doApproveActivity(a.id,1)" class="px-3 py-1 text-xs bg-emerald-600 text-white rounded-lg hover:bg-emerald-700">
                      通过
                    </button>
                    <button v-if="a.status===0 && isPresident"
                      @click="doApproveActivity(a.id,2)" class="px-3 py-1 text-xs border border-gray-200 text-gray-500 rounded-lg hover:bg-gray-50">
                      驳回
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <el-dialog v-model="showCreateActivity" title="创建活动" width="500px" top="15vh">
              <el-form :model="activityForm" label-position="top">
                <el-form-item label="活动标题"><el-input v-model="activityForm.title" /></el-form-item>
                <el-form-item label="活动类别">
                  <el-select v-model="activityForm.category" class="w-full">
                    <el-option label="学术科技" value="academic" />
                    <el-option label="体育竞技" value="sports" />
                    <el-option label="文化艺术" value="cultural" />
                    <el-option label="志愿服务" value="volunteer" />
                    <el-option label="其他" value="other" />
                  </el-select>
                </el-form-item>
                <el-form-item label="地点"><el-input v-model="activityForm.location" /></el-form-item>
                <el-form-item label="开始时间">
                  <el-date-picker v-model="activityForm.startTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" class="w-full" />
                </el-form-item>
                <el-form-item label="结束时间">
                  <el-date-picker v-model="activityForm.endTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" class="w-full" />
                </el-form-item>
                <el-form-item label="人数上限（0=不限）">
                  <el-input-number v-model="activityForm.maxParticipants" :min="0" class="w-full" />
                </el-form-item>
                <el-form-item label="描述"><el-input v-model="activityForm.description" type="textarea" :rows="3" /></el-form-item>
              </el-form>
              <template #footer>
                <el-button @click="showCreateActivity=false">取消</el-button>
                <el-button type="primary" @click="doCreateActivity" :loading="submitting">提交审核</el-button>
              </template>
            </el-dialog>
          </el-tab-pane>

          <el-tab-pane v-if="isPresident || userStore.role==='admin'" label="成员管理" name="management">
            <div class="bg-white rounded-xl border border-soft p-5">
              <h4 class="text-sm font-semibold text-ink mb-4">待审批成员</h4>
              <div v-if="pendingMembers.length === 0" class="text-center text-mist py-8 text-sm">暂无待审批申请</div>
              <div v-else class="space-y-3">
                <div v-for="m in pendingMembers" :key="m.id" class="flex items-center justify-between py-2 border-b border-wash last:border-0">
                  <div>
                    <div class="text-sm font-medium text-ink">{{ m.userName || '未知用户' }}</div>
                    <div class="text-xs text-mist">{{ m.applyReason || '无申请理由' }}</div>
                  </div>
                  <div class="flex gap-2">
                    <button @click="doApproveMember(m.id,1)" class="px-3 py-1 text-xs bg-gray-900 text-white rounded-lg hover:bg-gray-700">通过</button>
                    <button @click="doApproveMember(m.id,2)" class="px-3 py-1 text-xs border border-gray-200 text-gray-500 rounded-lg hover:bg-gray-50">拒绝</button>
                  </div>
                </div>
              </div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div class="space-y-5">
        <div class="bg-white rounded-xl border border-soft p-5">
          <h4 class="text-sm font-semibold text-ink mb-4">社团信息</h4>
          <div class="space-y-3 text-sm">
            <div class="flex justify-between">
              <span class="text-ash">社团状态</span>
              <el-tag size="small" :type="club.status===1?'success':club.status===2?'danger':club.status===3?'warning':'info'">
                {{ club.status===1?'正常':club.status===2?'已解散':club.status===3?'申请解散':'待审核' }}
              </el-tag>
            </div>
            <div class="flex justify-between">
              <span class="text-ash">创建时间</span>
              <span class="text-steel">{{ club.createTime?.substring(0,10) || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-soft p-5">
          <h4 class="text-sm font-semibold text-ink mb-4">我的身份</h4>
          <div v-if="myMember" class="text-sm">
            <el-tag size="small" :type="myMember.role==='president'?'danger':myMember.role==='vice_president'?'warning':''">
              {{ myMember.role==='president'?'社长':myMember.role==='vice_president'?'副社长':'成员' }}
            </el-tag>
            <div class="text-mist mt-2">
              状态: <span class="text-steel">{{ myMember.status===1?'已通过':'待审核' }}</span>
            </div>
            <div v-if="myMember.status===0 && myMember.role==='president'" class="text-xs text-amber-500 mt-1">
              社团尚未通过审核，社长身份待激活
            </div>
            <button
              v-if="myMember.role === 'president' && club?.status === 1"
              @click="handleDisband"
              class="mt-4 w-full px-4 py-2 bg-red-50 text-red-600 rounded-lg text-sm font-medium hover:bg-red-100 transition-colors"
            >申请解散</button>
            <button
              v-if="myMember.role === 'president' && club?.status === 3"
              @click="handleCancelDisband"
              class="mt-4 w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-200 transition-colors"
            >撤销解散申请</button>
            <button
              v-else-if="myMember.role !== 'president'"
              @click="handleLeave"
              class="mt-4 w-full px-4 py-2 bg-red-50 text-red-600 rounded-lg text-sm font-medium hover:bg-red-100 transition-colors"
            >退出社团</button>
            <div v-else class="text-xs text-mist mt-4">社长不能退出社团</div>
          </div>
          <div v-else class="text-sm text-mist">你还不是该社团成员</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getClub, getMembers, getMyMemberships,
  leaveClub, disbandClub, cancelDisband,
  removeMember, transferPresident, setMemberRole, approveMember } from '@/api/club'
import { getPublicActivities, createActivity as createCenterActivity,
  registerActivity, cancelRegistration, approveActivity as approveCenterActivity } from '@/api/activity'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, OfficeBuilding, User, UserFilled, Document, MoreFilled } from '@element-plus/icons-vue'
import { categoryLabel } from '@/utils/labels'

const userStore = useUserStore()
const route = useRoute()
const router = useRouter()
const club = ref<any>(null)
const members = ref<any[]>([])
const myMember = ref<any>(null)
const loading = ref(false)
const submitting = ref(false)
const activeTab = ref('overview')
const activities = ref<any[]>([])
const showCreateActivity = ref(false)
const activityForm = ref<any>({ title: '', category: '', location: '', startTime: '', endTime: '', maxParticipants: 0, description: '' })

const approvedMembers = computed(() => members.value.filter((m: any) => m.status === 1))
const pendingMembers = computed(() => members.value.filter((m: any) => m.status === 0 && m.role !== 'president'))

const clubId = computed(() => parseInt(route.params.id as string))

const isPresident = computed(() => myMember.value?.role === 'president' && myMember.value?.status === 1)

async function fetchData() {
  loading.value = true
  try {
    const [clubRes, membersRes, myRes] = await Promise.all([
      getClub(clubId.value),
      getMembers(clubId.value),
      getMyMemberships()
    ])
    club.value = clubRes.data
    members.value = membersRes.data || []
    const myMemberships = myRes.data || []
    myMember.value = myMemberships.find((m: any) => m.clubId === clubId.value)
    fetchActivities()
  } catch (e) {
    console.error('获取数据失败', e)
  } finally {
    loading.value = false
  }
}

async function fetchActivities() {
  try {
    const r = await getPublicActivities({ clubId: clubId.value, page: 1, size: 20 })
    activities.value = r.data?.records || []
  } catch { activities.value = [] }
}

async function handleLeave() {
  try {
    await ElMessageBox.confirm('确定要退出该社团吗？', '提示', { type: 'warning' })
    await leaveClub(clubId.value)
    ElMessage.success('退出成功')
    router.push('/club/list')
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('退出失败')
  }
}

async function handleDisband() {
  try {
    await ElMessageBox.confirm('确定要申请解散该社团吗？申请后需要管理员审核。', '提示', { type: 'warning' })
    await disbandClub(clubId.value)
    ElMessage.success('已提交解散申请')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('申请失败')
  }
}

async function handleCancelDisband() {
  try {
    await ElMessageBox.confirm('确定要撤销解散申请吗？', '提示', { type: 'warning' })
    await cancelDisband(clubId.value)
    ElMessage.success('已撤销解散申请')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('撤销失败')
  }
}

async function handleRemoveMember(memberId: number, userName: string) {
  try {
    await ElMessageBox.confirm(`确定要移除成员「${userName}」吗？`, '提示', { type: 'warning' })
    await removeMember(clubId.value, memberId)
    ElMessage.success('已移除')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('移除失败')
  }
}

async function handleTransfer(targetUserId: number, userName: string) {
  try {
    await ElMessageBox.confirm(`确定要将社长职位转让给「${userName}」吗？转让后你将变为普通成员。`, '提示', { type: 'warning' })
    await transferPresident(clubId.value, targetUserId)
    ElMessage.success('转让成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('转让失败')
  }
}

async function handleSetRole(memberId: number, role: string, userName: string) {
  const label = role === 'vice_president' ? '副社长' : '普通成员'
  try {
    await ElMessageBox.confirm(`确定将「${userName}」设为${label}吗？`, '提示', { type: 'info' })
    await setMemberRole(clubId.value, memberId, role)
    ElMessage.success('设置成功')
    await fetchData()
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('设置失败')
  }
}

async function doApproveMember(id: number, status: number) {
  try {
    await approveMember(id, status)
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    await fetchData()
  } catch { ElMessage.error('操作失败') }
}

async function doCreateActivity() {
  if (!activityForm.value.title?.trim()) { ElMessage.warning('请输入活动标题'); return }
  submitting.value = true
  try {
    await createCenterActivity({ ...activityForm.value, clubId: clubId.value })
    ElMessage.success('提交成功，等待审核')
    showCreateActivity.value = false
    activityForm.value = { title: '', category: '', location: '', startTime: '', endTime: '', maxParticipants: 0, description: '' }
    await fetchActivities()
  } catch { ElMessage.error('创建失败') }
  finally { submitting.value = false }
}

async function doRegister(activityId: number) {
  try {
    await registerActivity(activityId)
    ElMessage.success('报名成功')
    await fetchActivities()
  } catch { ElMessage.error('报名失败') }
}

async function doCancelRegister(activityId: number) {
  try {
    await cancelRegistration(activityId)
    ElMessage.success('已取消报名')
    await fetchActivities()
  } catch { ElMessage.error('取消报名失败') }
}

async function doApproveActivity(activityId: number, status: number) {
  try {
    await approveCenterActivity(activityId, status, status === 2 ? '社长驳回' : '')
    ElMessage.success(status === 1 ? '已通过' : '已驳回')
    await fetchActivities()
  } catch { ElMessage.error('操作失败') }
}

function handleMemberAction(cmd: string, m: any) {
  if (cmd === 'remove') return handleRemoveMember(m.id, m.userName)
  if (cmd === 'transfer') return handleTransfer(m.userId, m.userName)
  if (cmd === 'setVice') return handleSetRole(m.id, 'vice_president', m.userName)
  if (cmd === 'unsetVice') return handleSetRole(m.id, 'member', m.userName)
}

onMounted(fetchData)
</script>
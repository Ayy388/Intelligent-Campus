<template>
  <div>
    <div class="animate__animated animate__fadeInDown">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md">
        <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center shadow-sm">
              <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-bold text-ink">我的活动</h3>
              <p class="text-xs text-mist mt-0.5">管理我创建和报名的活动</p>
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
              <path d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
            <p class="text-sm text-mist">{{ activeTab === 'created' ? '暂无创建的活动' : '暂无报名的活动' }}</p>
          </div>

          <el-table v-else :data="list" v-loading="loading" class="!border-0"
            header-row-class-name="activity-table-header" row-class-name="activity-table-row"
            @row-click="handleRowClick">
            <el-table-column label="活动名称" min-width="180">
              <template #default="{row}">
                <span class="text-sm font-medium text-ink">{{ row.title || row.activityTitle }}</span>
              </template>
            </el-table-column>
            <el-table-column label="时间" width="180">
              <template #default="{row}">
                <span class="text-xs text-steel">{{ (row.startTime || '').substring(0,16) }}</span>
              </template>
            </el-table-column>
            <el-table-column label="地点" width="120">
              <template #default="{row}">
                <span class="text-xs text-ash">{{ row.location || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{row}">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold shadow-sm" :class="statusBadgeClass(row.status)">
                  {{ statusLabel(row.status) }}
                </span>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="260" fixed="right">
              <template #default="{row}">
                <div class="flex gap-2">
                  <el-button v-if="activeTab === 'created' && row.status === 0" size="small" type="warning" @click.stop="doWithdraw(row)" plain>
                    撤回申请
                  </el-button>
                  <el-button v-if="activeTab === 'created' && row.status === 1" size="small" type="primary" @click.stop="doConfirm(row.id)" plain>
                    开始活动
                  </el-button>
                  <el-button v-if="activeTab === 'created' && row.status === 3" size="small" type="success" @click.stop="doFinish(row.id)" plain>
                    结束活动
                  </el-button>
                  <el-button v-if="activeTab === 'created' && row.status === 3" size="small" @click.stop="openSummary(row)" plain>
                    活动总结
                  </el-button>
                  <el-button v-if="activeTab === 'registered' && row.status === 1" size="small" type="danger" @click.stop="doCancelReg(row.activityId)" plain>
                    取消报名
                  </el-button>
                  <span v-else-if="activeTab === 'created' && row.status !== 0 && row.status !== 1 && row.status !== 3" class="text-xs text-gray-400">--</span>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <div v-if="total > pageSize" class="mt-4 flex justify-center">
            <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
              layout="prev,pager,next" small @current-change="fetchData" />
          </div>
        </div>
      </div>
    </div>

    <el-dialog v-model="detailVisible" width="600px" top="5vh" :close-on-click-modal="false">
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
            <div class="text-sm font-semibold text-ink">{{ detail.currentParticipants || 0 }}/{{ detail.maxParticipants || '不限' }}</div>
          </div>
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

        <div v-if="detail.status === 2 && detail.rejectReason" class="bg-red-50 border border-red-100 rounded-xl p-4 mb-4">
          <div class="flex items-center gap-2 mb-1">
            <svg class="w-4 h-4 text-red-400" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"/><line x1="15" y1="9" x2="9" y2="15"/><line x1="9" y1="9" x2="15" y2="15"/></svg>
            <span class="text-xs font-semibold text-red-500">驳回原因</span>
          </div>
          <div class="text-sm text-red-600 ml-6">{{ detail.rejectReason }}</div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="detailVisible = false" class="!rounded-lg">关闭</el-button>
          <el-button v-if="detail?.status === 0" type="warning" @click="doWithdraw(detail)" class="!rounded-lg" plain>
            撤回申请
          </el-button>
          <el-button v-if="detail?.status === 1 && activeTab === 'created'" type="primary" @click="doConfirm(detail.id)" class="!rounded-lg">
            开始活动
          </el-button>
          <el-button v-if="detail?.status === 3 && activeTab === 'created'" type="success" @click="doFinish(detail.id)" class="!rounded-lg">
            结束活动
          </el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="summaryVisible" width="480px" top="25vh" :close-on-click-modal="false">
      <template #header>
        <span class="text-base font-bold text-ink">活动总结</span>
      </template>
      <el-input v-model="summaryText" type="textarea" :rows="4" placeholder="请输入活动总结内容" class="mb-4" />
      <el-input v-model="summaryImages" placeholder="图片URL（可选）" />
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="summaryVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="primary" @click="doSaveSummary" class="!rounded-lg">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getMyActivities, getMyRegistrations, confirmActivity, cancelRegistration, updateActivitySummary, getActivity, getRegistrations, withdrawActivity, finishActivity } from '@/api/activity'
import { ElMessage, ElMessageBox } from 'element-plus'
import { categoryLabel } from '@/utils/labels'

const tabs = [{ key: 'created', label: '我创建的' }, { key: 'registered', label: '我报名的' }]
const activeTab = ref('created')
const list = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10

const detailVisible = ref(false)
const detail = ref<any>(null)
const registrations = ref<any[]>([])

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

async function fetchData() {
  loading.value = true
  if (activeTab.value === 'created') {
    const r = await getMyActivities({ page: page.value, size: pageSize })
    list.value = r.data.records || []
    total.value = r.data.total || 0
  } else {
    const r = await getMyRegistrations()
    list.value = r.data || []
    total.value = list.value.length
  }
  loading.value = false
}

async function handleRowClick(row: any) {
  const id = row.id || row.activityId
  if (!id) return
  const r = await getActivity(id)
  detail.value = r.data
  const regRes = await getRegistrations(id, { page: 1, size: 999 })
  const list = regRes.data.records || []
  const hasCreator = list.some((x: any) => x.userId === r.data.creatorId)
  if (!hasCreator) {
    list.unshift({ id: -1, userId: r.data.creatorId, userName: r.data.creatorName || '发起者', isCreator: true })
  } else {
    list.forEach((x: any) => { if (x.userId === r.data.creatorId) x.isCreator = true })
  }
  registrations.value = list
  detailVisible.value = true
}

async function doConfirm(id: number) {
  try {
    await ElMessageBox.confirm('确认开始活动？报名将关闭，活动状态将变更为"活动中"。', '开始活动', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    await confirmActivity(id)
    ElMessage.success('活动已开始')
    detailVisible.value = false
    fetchData()
  } catch { /* cancelled */ }
}

async function doFinish(id: number) {
  try {
    await ElMessageBox.confirm('确认结束活动？此操作不可撤销。', '结束活动', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    await finishActivity(id)
    ElMessage.success('活动已结束')
    detailVisible.value = false
    fetchData()
  } catch { /* cancelled */ }
}

async function doCancelReg(activityId: number) {
  try {
    await ElMessageBox.confirm('确定取消该活动的报名？', '取消报名', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    await cancelRegistration(activityId)
    ElMessage.success('已取消报名')
    detailVisible.value = false
    fetchData()
  } catch { /* cancelled */ }
}

async function doWithdraw(row: any) {
  try {
    await ElMessageBox.confirm('确定撤回该活动的申请吗？', '撤回申请', { confirmButtonText: '确认撤回', cancelButtonText: '取消', type: 'warning' })
    await withdrawActivity(row.id)
    ElMessage.success('已撤回申请')
    detailVisible.value = false
    fetchData()
  } catch { /* cancelled */ }
}

const summaryVisible = ref(false)
const summaryTargetId = ref<number | null>(null)
const summaryText = ref('')
const summaryImages = ref('')

function openSummary(row: any) {
  summaryTargetId.value = row.id
  summaryText.value = row.summary || ''
  summaryImages.value = row.images || ''
  summaryVisible.value = true
}

async function doSaveSummary() {
  if (!summaryTargetId.value) return
  await updateActivitySummary(summaryTargetId.value, summaryText.value, summaryImages.value || undefined)
  ElMessage.success('总结已保存')
  summaryVisible.value = false
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped>
.activity-table-header th {
  background-color: #F9FAFB !important;
  color: #6B7280 !important;
  font-size: 0.75rem !important;
  font-weight: 600 !important;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-top: 12px !important;
  padding-bottom: 12px !important;
}
.activity-table-row td {
  padding-top: 14px !important;
  padding-bottom: 14px !important;
  transition: background-color 0.2s ease;
}
.activity-table-row:hover td {
  background-color: #F3F4F6 !important;
  cursor: pointer;
}
</style>

<template>
  <div v-loading="loading">
    <div class="flex items-center justify-between mb-6">
      <div class="flex items-center gap-4">
        <span class="text-xl font-bold text-ink tracking-tight">活动中心</span>
        <el-select v-model="filterClubId" placeholder="全部社团" size="small" style="width:180px" clearable @change="fetchActivities">
          <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
        </el-select>
      </div>
      <button v-if="userStore.role==='admin'||userStore.role==='teacher'" @click="showCreate"
        class="inline-flex items-center gap-1.5 px-4 py-2 bg-ink text-white rounded-xl text-sm font-medium hover:bg-steel active:scale-[0.97] transition-all duration-200 shadow-sm hover:shadow-md">
        <svg class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M12 4v16m8-8H4"/></svg>
        发布活动
      </button>
    </div>

    <div v-if="activities.length===0" class="flex flex-col items-center justify-center py-24 text-mist">
      <svg class="w-16 h-16 mb-4 opacity-40" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1"><path stroke-linecap="round" stroke-linejoin="round" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
      <span class="text-sm">暂无活动数据</span>
    </div>

    <div class="space-y-4">
      <div v-for="(a, idx) in activities" :key="a.id"
        class="group bg-white rounded-2xl border border-soft/60 hover:border-ink/15 hover:shadow-lg hover:shadow-ink/5 transition-all duration-300 p-5 animate__animated animate__fadeInUp"
        :style="{ animationDelay: idx * 0.06 + 's' }">
        <div class="flex items-start justify-between gap-4">
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2.5 mb-2">
              <span class="text-base font-bold text-ink truncate">{{ a.title }}</span>
              <span v-if="a.status===0" class="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-amber-50 text-amber-600 border border-amber-200/60 shrink-0">审核中</span>
              <span v-else-if="a.status===1" class="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-emerald-50 text-emerald-600 border border-emerald-200/60 shrink-0">报名中</span>
              <span v-else-if="a.status===3" class="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-blue-50 text-blue-600 border border-blue-200/60 shrink-0">活动中</span>
              <span v-else-if="a.status===4" class="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-gray-50 text-gray-400 border border-gray-200/60 shrink-0">已结束</span>
              <span v-else class="inline-flex items-center px-2 py-0.5 rounded-md text-xs font-medium bg-gray-50 text-gray-400 border border-gray-200/60 shrink-0">未知</span>
            </div>
            <div class="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-mist mb-2.5">
              <span class="inline-flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16l14 0"/></svg>
                {{ a.clubName || '未知社团' }}
              </span>
              <span class="inline-flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/></svg>
                {{ a.activityType || '未分类' }}
              </span>
              <span class="inline-flex items-center gap-1">
                <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                {{ a.location || '未指定地点' }}
              </span>
            </div>
            <div class="flex items-center gap-1.5 text-xs text-ash/70 mb-2.5">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
              {{ a.startTime?.substring(0,16) || '' }} ~ {{ a.endTime?.substring(0,16) || '' }}
            </div>
            <p class="text-xs text-ash/60 leading-relaxed line-clamp-2 mb-3">{{ a.description || '暂无描述' }}</p>
            <div class="flex items-center gap-3">
              <div class="flex-1 max-w-xs">
                <div class="flex items-center justify-between text-xs mb-1">
                  <span class="text-mist">报名人数</span>
                  <span class="font-medium" :class="a.maxEnroll > 0 && a.enrolled >= a.maxEnroll ? 'text-red-500' : 'text-ink'">
                    {{ a.enrolled || 0 }}
                    <span v-if="a.maxEnroll > 0" class="text-mist font-normal"> / {{ a.maxEnroll }}</span>
                  </span>
                </div>
                <div v-if="a.maxEnroll > 0" class="w-full h-1.5 bg-gray-100 rounded-full overflow-hidden">
                  <div class="h-full rounded-full transition-all duration-500"
                    :class="a.enrolled >= a.maxEnroll ? 'bg-red-400' : a.enrolled / a.maxEnroll > 0.6 ? 'bg-amber-400' : 'bg-emerald-400'"
                    :style="{ width: Math.min((a.enrolled || 0) / a.maxEnroll * 100, 100) + '%' }">
                  </div>
                </div>
              </div>
              <span v-if="a.maxEnroll > 0 && a.enrolled >= a.maxEnroll"
                class="inline-flex items-center gap-1 text-xs text-red-500 font-medium shrink-0">
                <svg class="w-3.5 h-3.5" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>
                已满
              </span>
            </div>
          </div>
          <div class="flex flex-col gap-2 shrink-0 pt-1">
            <button v-if="userStore.role==='student' && a.status===1"
              :disabled="a.maxEnroll > 0 && a.enrolled >= a.maxEnroll"
              @click="doEnroll(a)"
              class="inline-flex items-center justify-center gap-1.5 px-4 py-2 bg-ink text-white rounded-xl text-xs font-medium hover:bg-steel active:scale-[0.97] transition-all duration-200 disabled:opacity-40 disabled:cursor-not-allowed disabled:active:scale-100 shadow-sm">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"/></svg>
              {{ a.enrolled >= a.maxEnroll && a.maxEnroll > 0 ? '已满' : '报名' }}
            </button>
            <button v-if="(userStore.role==='admin'||userStore.role==='teacher') && (a.status===2||a.status===3)"
              @click="openSummary(a)"
              class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs font-medium border border-ink/20 text-ink rounded-xl hover:bg-ink hover:text-white active:scale-[0.97] transition-all duration-200">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z"/></svg>
              活动总结
            </button>
            <button @click="openEnrollments(a)"
              class="inline-flex items-center justify-center gap-1.5 px-4 py-2 text-xs font-medium text-mist hover:text-ink hover:bg-gray-50 rounded-xl active:scale-[0.97] transition-all duration-200">
              <svg class="w-3.5 h-3.5" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/><path stroke-linecap="round" stroke-linejoin="round" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"/></svg>
              查看报名
            </button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="total > pageSize" class="flex justify-center mt-8">
      <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
        layout="prev,pager,next" small
        class="custom-pagination"
        @current-change="fetchActivities" />
    </div>

    <el-dialog v-model="createVisible" title=" " width="520px" class="custom-dialog" :close-on-click-modal="false" destroy-on-close>
      <div class="px-2">
        <div class="text-lg font-bold text-ink mb-6">发布活动</div>
        <el-form :model="form" label-position="top" class="space-y-1">
          <el-form-item label="所属社团">
            <el-select v-model="form.clubId" placeholder="请选择社团" style="width:100%">
              <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="活动标题">
            <el-input v-model="form.title" placeholder="请输入活动标题" />
          </el-form-item>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="活动类型">
              <el-select v-model="form.activityType" placeholder="请选择类型" style="width:100%">
                <el-option label="讲座" value="讲座" />
                <el-option label="比赛" value="比赛" />
                <el-option label="团建" value="团建" />
                <el-option label="会议" value="会议" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="活动地点">
              <el-input v-model="form.location" placeholder="请输入活动地点" />
            </el-form-item>
          </div>
          <div class="grid grid-cols-2 gap-4">
            <el-form-item label="开始时间">
              <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
            </el-form-item>
            <el-form-item label="结束时间">
              <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
            </el-form-item>
          </div>
          <el-form-item label="人数上限">
            <el-input-number v-model="form.maxEnroll" :min="0" :max="9999" style="width:100%" />
          </el-form-item>
          <el-form-item label="活动描述">
            <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3 px-2 pt-2">
          <el-button @click="createVisible=false" class="!rounded-xl !px-5">取消</el-button>
          <el-button type="primary" @click="doCreate" :loading="submitting" class="!rounded-xl !px-6 !bg-ink !border-ink hover:!bg-steel">发布</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="summaryVisible" title=" " width="520px" class="custom-dialog" :close-on-click-modal="false" destroy-on-close>
      <div class="px-2">
        <div class="text-lg font-bold text-ink mb-6">活动总结</div>
        <el-form label-position="top" class="space-y-1">
          <el-form-item label="总结内容">
            <el-input v-model="summaryForm.summary" type="textarea" :rows="4" placeholder="请输入活动总结" />
          </el-form-item>
          <el-form-item label="活动图片">
            <el-input v-model="summaryForm.images" placeholder="图片URL（可选）">
              <template #prefix>
                <svg class="w-4 h-4 text-mist" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1.5"><path stroke-linecap="round" stroke-linejoin="round" d="M2.25 15.75l5.159-5.159a2.25 2.25 0 013.182 0l5.159 5.159m-1.5-1.5l1.409-1.409a2.25 2.25 0 013.182 0l2.909 2.909M3.75 21h16.5A2.25 2.25 0 0022.5 18.75V5.25A2.25 2.25 0 0020.25 3H3.75A2.25 2.25 0 001.5 5.25v13.5A2.25 2.25 0 003.75 21z"/></svg>
              </template>
            </el-input>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3 px-2 pt-2">
          <el-button @click="summaryVisible=false" class="!rounded-xl !px-5">取消</el-button>
          <el-button type="primary" @click="doSaveSummary" :loading="submitting" class="!rounded-xl !px-6 !bg-ink !border-ink hover:!bg-steel">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="enrollVisible" title=" " width="520px" class="custom-dialog" :close-on-click-modal="false" destroy-on-close>
      <div class="px-2">
        <div class="text-lg font-bold text-ink mb-1">{{ currentEnrollActivity?.title }}</div>
        <div class="text-xs text-mist mb-5">报名列表</div>
        <div v-if="enrollments.length===0" class="flex flex-col items-center py-10 text-mist">
          <svg class="w-12 h-12 mb-3 opacity-40" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="1"><path stroke-linecap="round" stroke-linejoin="round" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
          <span class="text-sm">暂无报名记录</span>
        </div>
        <div v-for="e in enrollments" :key="e.id"
          class="flex items-center justify-between px-4 py-3 rounded-xl bg-gray-50/80 hover:bg-gray-50 transition-colors mb-2">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-full bg-ink/10 flex items-center justify-center text-xs font-bold text-ink shrink-0">
              {{ (e.userName || '?')[0] }}
            </div>
            <div>
              <div class="text-sm font-medium text-ink">{{ e.userName || '未知用户' }}</div>
              <div class="text-xs text-mist">{{ e.enrollTime?.substring(0,16) || '' }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end px-2 pt-2">
          <el-button @click="enrollVisible=false" class="!rounded-xl !px-5">关闭</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getClubs, getActivities, createActivity, enrollActivity, getEnrollments, updateActivitySummary } from '@/api/club'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()

const loading = ref(false)
const enrolling = ref(false)
const submitting = ref(false)
const clubList = ref<any[]>([])
const filterClubId = ref<number | null>(null)
const activities = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const pageSize = 10

const createVisible = ref(false)
const form = reactive({
  clubId: null as number | null,
  title: '',
  activityType: '',
  location: '',
  startTime: '',
  endTime: '',
  maxEnroll: 0,
  description: '',
})

const summaryVisible = ref(false)
const summaryActivityId = ref<number | null>(null)
const summaryForm = reactive({ summary: '', images: '' })

const enrollVisible = ref(false)
const currentEnrollActivity = ref<any>(null)
const enrollments = ref<any[]>([])

async function fetchClubs() {
  loading.value = true
  try {
    const r = await getClubs()
    clubList.value = r.data || []
  } finally {
    loading.value = false
  }
}

async function fetchActivities() {
  loading.value = true
  try {
    const params: any = { page: page.value, size: pageSize }
    if (filterClubId.value) params.clubId = filterClubId.value
    const r = await getActivities(params)
    activities.value = r.data.records || []
    total.value = r.data.total || 0
  } catch {
    ElMessage.error('获取活动列表失败')
  } finally {
    loading.value = false
  }
}

function showCreate() {
  form.clubId = null
  form.title = ''
  form.activityType = ''
  form.location = ''
  form.startTime = ''
  form.endTime = ''
  form.maxEnroll = 0
  form.description = ''
  createVisible.value = true
}

async function doCreate() {
  if (!form.clubId) { ElMessage.warning('请选择所属社团'); return }
  if (!form.title.trim()) { ElMessage.warning('请输入活动标题'); return }
  if (!form.startTime) { ElMessage.warning('请选择开始时间'); return }
  if (!form.endTime) { ElMessage.warning('请选择结束时间'); return }
  submitting.value = true
  try {
    await createActivity(form)
    ElMessage.success('发布成功')
    createVisible.value = false
    fetchActivities()
  } catch { ElMessage.error('发布失败') }
  finally { submitting.value = false }
}

async function doEnroll(activity: any) {
  if (activity.maxEnroll > 0 && activity.enrolled >= activity.maxEnroll) {
    ElMessage.warning('报名人数已满')
    return
  }
  try {
    await ElMessageBox.confirm(`确认报名活动「${activity.title}」?`, '确认报名', { type: 'info' })
  } catch {
    return
  }
  enrolling.value = true
  try {
    await enrollActivity(activity.id)
    ElMessage.success('报名成功')
    fetchActivities()
  } catch (e: any) {
    ElMessage.error(e.message || '报名失败')
  } finally {
    enrolling.value = false
  }
}

function openSummary(activity: any) {
  summaryActivityId.value = activity.id
  summaryForm.summary = activity.summary || ''
  summaryForm.images = activity.images || ''
  summaryVisible.value = true
}

async function doSaveSummary() {
  if (!summaryActivityId.value) return
  submitting.value = true
  try {
    await updateActivitySummary(summaryActivityId.value, summaryForm.summary, summaryForm.images || undefined)
    ElMessage.success('保存成功')
    summaryVisible.value = false
    fetchActivities()
  } catch { ElMessage.error('保存失败') }
  finally { submitting.value = false }
}

async function openEnrollments(activity: any) {
  currentEnrollActivity.value = activity
  enrollVisible.value = true
  enrolling.value = true
  try {
    const r = await getEnrollments(activity.id)
    enrollments.value = r.data || []
  } catch { enrollments.value = [] }
  finally { enrolling.value = false }
}

onMounted(() => {
  fetchClubs()
  fetchActivities()
})
</script>

<style>
.custom-dialog .el-dialog {
  border-radius: 20px !important;
  padding: 12px 0 !important;
  box-shadow: 0 25px 50px -12px rgba(0,0,0,0.25) !important;
}
.custom-dialog .el-dialog__header {
  display: none !important;
}
.custom-dialog .el-dialog__body {
  padding: 8px 24px 4px !important;
}
.custom-dialog .el-dialog__footer {
  padding: 0 24px 16px !important;
  border-top: 1px solid #f3f4f6;
}
.custom-dialog .el-form-item {
  margin-bottom: 18px;
}
.custom-dialog .el-form-item__label {
  font-weight: 600 !important;
  font-size: 13px !important;
  color: #1a1a2e !important;
  padding-bottom: 4px !important;
}
.custom-dialog .el-input__wrapper,
.custom-dialog .el-select .el-input__wrapper,
.custom-dialog .el-date-editor .el-input__wrapper {
  border-radius: 12px !important;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
  transition: box-shadow 0.2s ease !important;
}
.custom-dialog .el-input__wrapper:hover,
.custom-dialog .el-select .el-input__wrapper:hover,
.custom-dialog .el-date-editor .el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #1a1a2e inset !important;
}
.custom-dialog .el-input__wrapper.is-focus,
.custom-dialog .el-select .el-input__wrapper.is-focus,
.custom-dialog .el-date-editor .el-input__wrapper.is-focus {
  box-shadow: 0 0 0 2px #1a1a2e inset !important;
}
.custom-dialog .el-textarea__inner {
  border-radius: 12px !important;
  box-shadow: 0 0 0 1px #e5e7eb inset !important;
  transition: box-shadow 0.2s ease !important;
}
.custom-dialog .el-textarea__inner:focus {
  box-shadow: 0 0 0 2px #1a1a2e inset !important;
}
.custom-dialog .el-input-number {
  width: 100% !important;
}
.custom-dialog .el-input-number .el-input__wrapper {
  border-radius: 12px !important;
}
.custom-dialog .el-input-number .el-input-number__decrease,
.custom-dialog .el-input-number .el-input-number__increase {
  border-radius: 0 12px 12px 0 !important;
}

.custom-pagination .el-pager li {
  border-radius: 10px !important;
  font-weight: 500 !important;
  min-width: 32px !important;
  height: 32px !important;
  line-height: 32px !important;
}
.custom-pagination .el-pager li.is-active {
  background-color: #1a1a2e !important;
  color: white !important;
}
.custom-pagination .el-pagination button {
  border-radius: 10px !important;
  min-width: 32px !important;
  height: 32px !important;
}
</style>
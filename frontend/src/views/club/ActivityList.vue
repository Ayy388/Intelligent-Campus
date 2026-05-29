<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <span class="text-lg font-bold text-ink">活动中心</span>
      <button v-if="userStore.role==='admin'||userStore.role==='teacher'" @click="showCreate"
        class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">发布活动</button>
    </div>

    <div class="flex items-center gap-3 mb-5">
      <span class="text-xs text-steel">筛选社团:</span>
      <el-select v-model="filterClubId" placeholder="全部社团" size="small" style="width:200px" clearable @change="fetchActivities">
        <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
      </el-select>
    </div>

    <div v-if="activities.length===0" class="text-center text-mist py-20">暂无活动数据</div>

    <div class="space-y-3">
      <div v-for="a in activities" :key="a.id"
        class="bg-white rounded-xl border border-soft p-4 hover:shadow-sm transition-shadow">
        <div class="flex items-start justify-between">
          <div class="flex-1 mr-4">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-sm font-semibold text-ink">{{ a.title }}</span>
              <el-tag size="small" :type="a.status===0?'warning':a.status===1?'success':a.status===2?'':a.status===3?'info':'warning'">
                {{ a.status===0?'待审核':a.status===1?'报名中':a.status===2?'进行中':a.status===3?'已结束':'未知' }}
              </el-tag>
            </div>
            <div class="text-xs text-mist mt-1">
              {{ a.clubName || '未知社团' }} · {{ a.activityType || '未分类' }} · {{ a.location || '未指定地点' }}
            </div>
            <div class="text-xs text-mist mt-1">
              {{ a.startTime?.substring(0,16) || '' }} ~ {{ a.endTime?.substring(0,16) || '' }}
            </div>
            <div class="text-xs text-ash mt-1 line-clamp-2">{{ a.description || '暂无描述' }}</div>
            <div class="text-xs text-mist mt-2">
              已报名 {{ a.enrolled || 0 }}{{ a.maxEnroll > 0 ? '/' + a.maxEnroll : '' }}
              <span v-if="a.maxEnroll > 0 && a.enrolled >= a.maxEnroll" class="text-red-400 ml-1">(已满)</span>
            </div>
          </div>
          <div class="flex flex-col gap-1.5 items-end shrink-0">
            <button v-if="userStore.role==='student' && a.status===1"
              :disabled="a.maxEnroll > 0 && a.enrolled >= a.maxEnroll"
              @click="doEnroll(a)"
              class="px-3 py-1 bg-ink text-white rounded-lg text-xs hover:bg-steel transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
              {{ a.enrolled >= a.maxEnroll && a.maxEnroll > 0 ? '已满' : '报名' }}
            </button>
            <button v-if="(userStore.role==='admin'||userStore.role==='teacher') && (a.status===2||a.status===3)"
              @click="openSummary(a)"
              class="px-3 py-1 text-xs border border-ink text-ink rounded-lg hover:bg-ink hover:text-white transition-colors">
              活动总结
            </button>
            <button @click="openEnrollments(a)"
              class="px-3 py-1 text-xs text-mist hover:text-ink transition-colors">
              查看报名
            </button>
          </div>
        </div>
      </div>
    </div>

    <el-pagination v-model:current-page="page" :total="total" :page-size="pageSize"
      layout="prev,pager,next" small @current-change="fetchActivities" />

    <el-dialog v-model="createVisible" title="发布活动" width="500px">
      <el-form :model="form">
        <el-form-item label="所属社团">
          <el-select v-model="form.clubId" placeholder="请选择社团" style="width:100%">
            <el-option v-for="c in clubList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="活动标题">
          <el-input v-model="form.title" placeholder="请输入活动标题" />
        </el-form-item>
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
        <el-form-item label="开始时间">
          <el-date-picker v-model="form.startTime" type="datetime" placeholder="选择开始时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="form.endTime" type="datetime" placeholder="选择结束时间" style="width:100%" />
        </el-form-item>
        <el-form-item label="人数上限">
          <el-input-number v-model="form.maxEnroll" :min="0" :max="9999" style="width:100%" />
        </el-form-item>
        <el-form-item label="活动描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入活动描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="doCreate">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="summaryVisible" title="活动总结" width="500px">
      <el-form>
        <el-form-item label="总结内容">
          <el-input v-model="summaryForm.summary" type="textarea" :rows="4" placeholder="请输入活动总结" />
        </el-form-item>
        <el-form-item label="活动图片">
          <el-input v-model="summaryForm.images" placeholder="图片URL（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="summaryVisible=false">取消</el-button>
        <el-button type="primary" @click="doSaveSummary">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="enrollVisible" :title="currentEnrollActivity?.title + ' - 报名列表'" width="500px">
      <div v-if="enrollments.length===0" class="text-center text-mist py-6 text-sm">暂无报名记录</div>
      <div v-for="e in enrollments" :key="e.id" class="flex items-center justify-between py-2 border-b border-wash last:border-0">
        <div class="text-sm">
          <span class="text-ink font-medium">{{ e.userName || '未知用户' }}</span>
          <span class="text-mist ml-2 text-xs">{{ e.enrollTime?.substring(0,16) || '' }}</span>
        </div>
      </div>
      <template #footer>
        <el-button @click="enrollVisible=false">关闭</el-button>
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
  const r = await getClubs()
  clubList.value = r.data || []
}

async function fetchActivities() {
  const params: any = { page: page.value, size: pageSize }
  if (filterClubId.value) params.clubId = filterClubId.value
  const r = await getActivities(params)
  activities.value = r.data.records || []
  total.value = r.data.total || 0
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
  try {
    await createActivity(form)
    ElMessage.success('发布成功')
    createVisible.value = false
    fetchActivities()
  } catch { ElMessage.error('发布失败') }
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
  try {
    await enrollActivity(activity.id)
    ElMessage.success('报名成功')
    fetchActivities()
  } catch (e: any) {
    ElMessage.error(e.message || '报名失败')
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
  try {
    await updateActivitySummary(summaryActivityId.value, summaryForm.summary, summaryForm.images || undefined)
    ElMessage.success('保存成功')
    summaryVisible.value = false
    fetchActivities()
  } catch { ElMessage.error('保存失败') }
}

async function openEnrollments(activity: any) {
  currentEnrollActivity.value = activity
  enrollVisible.value = true
  try {
    const r = await getEnrollments(activity.id)
    enrollments.value = r.data || []
  } catch { enrollments.value = [] }
}

onMounted(() => {
  fetchClubs()
  fetchActivities()
})
</script>
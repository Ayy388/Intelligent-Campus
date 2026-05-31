<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <span class="text-lg font-bold text-ink">签到打卡</span>
      <button v-if="isTeacher" @click="showCreate" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">发起签到</button>
    </div>

    <div v-if="checkins.length === 0 && !loading" class="bg-white rounded-xl border border-soft p-10 text-center">
      <div class="text-mist text-sm">暂无签到记录</div>
    </div>

    <div class="space-y-3">
      <div v-for="c in checkins" :key="c.id"
        class="bg-white rounded-xl border border-soft p-4 hover:shadow-sm transition-shadow"
        :class="{ 'cursor-pointer': isTeacher }"
        @click="isTeacher && viewRecords(c)">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
              <span class="text-sm font-semibold text-ink">{{ c.title }}</span>
              <el-tag size="small">{{ c.checkinType === 'course' ? '课程签到' : '活动签到' }}</el-tag>
              <el-tag v-if="c.status === 1" size="small" type="success">进行中</el-tag>
              <el-tag v-else size="small" type="info">已结束</el-tag>
            </div>
            <div class="text-xs text-mist mt-1">
              {{ c.teacherName || '' }}
              <span v-if="c.startTime && c.endTime"> · {{ formatTime(c.startTime) }} ~ {{ formatTime(c.endTime) }}</span>
            </div>
            <div class="text-xs text-ash mt-1">
              已签到 {{ c.checkedCount }} / {{ c.totalCount || '-' }}
            </div>
          </div>
          <div v-if="!isTeacher" class="ml-4 flex-shrink-0">
            <el-tag v-if="checkedMap[c.id]" size="small" type="success">已签到</el-tag>
            <button v-else @click.stop="handleCheckIn(c)"
              :disabled="c.status !== 1 || isOutOfTime(c)"
              class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors disabled:opacity-40 disabled:cursor-not-allowed">
              签到
            </button>
          </div>
        </div>
      </div>
    </div>

    <el-pagination v-if="total > pageSize"
      v-model:current-page="page" :total="total" :page-size="pageSize"
      layout="prev,pager,next" small @current-change="fetchCheckIns" />

    <el-dialog v-model="createVisible" title="发起签到" width="450px">
      <el-form :model="createForm">
        <el-form-item label="签到标题">
          <el-input v-model="createForm.title" placeholder="请输入签到标题" />
        </el-form-item>
        <el-form-item label="关联课程">
          <el-select v-model="createForm.courseId" placeholder="选择课程（可选）" class="w-full" clearable>
            <el-option v-for="c in teacherCourses" :key="c.id" :label="`${c.courseName} (${c.semester})`" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="签到类型">
          <el-select v-model="createForm.checkinType" class="w-full">
            <el-option label="课程签到" value="course" />
            <el-option label="活动签到" value="activity" />
          </el-select>
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="createForm.startTime" type="datetime" placeholder="选择开始时间" class="w-full" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="createForm.endTime" type="datetime" placeholder="选择结束时间" class="w-full" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="doCreate">发起</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="recordsVisible" title="签到记录" width="550px">
      <div v-if="records.length === 0" class="text-center text-mist py-8 text-sm">暂无签到记录</div>
      <div v-for="r in records" :key="r.id" class="flex items-center justify-between py-3 border-b border-wash last:border-0">
        <div>
          <div class="text-sm font-medium text-ink">{{ r.studentName || '未知学生' }}</div>
          <div class="text-xs text-mist">学号: {{ r.studentId }}</div>
        </div>
        <div class="text-xs text-mist">{{ formatTime(r.checkinTime) }}</div>
      </div>
      <template #footer>
        <el-button @click="recordsVisible=false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getCheckIns, createCheckIn, doCheckIn, getCheckInRecords, getCheckInStatus } from '@/api/growth'
import { getTeacherCourses } from '@/api/edu'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const isTeacher = computed(() => userStore.role === 'teacher' || userStore.role === 'admin')

const checkins = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const pageSize = 10

const checkedMap = ref<Record<number, boolean>>({})

const createVisible = ref(false)
const createForm = reactive({ title: '', checkinType: 'course', startTime: '', endTime: '', courseId: null as number | null })
const teacherCourses = ref<any[]>([])

const recordsVisible = ref(false)
const records = ref<any[]>([])

function formatTime(t: any) {
  if (!t) return ''
  const s = typeof t === 'string' ? t : String(t)
  return s.substring(0, 16)
}

function isOutOfTime(c: any) {
  if (!c.endTime) return false
  return new Date(c.endTime).getTime() < Date.now()
}

async function fetchCheckIns() {
  loading.value = true
  try {
    const r = await getCheckIns({ page: page.value, size: pageSize })
    checkins.value = r.data.records || []
    total.value = r.data.total || 0
    if (!isTeacher.value) {
      await checkAllStatuses()
    }
  } catch {
    ElMessage.error('加载签到列表失败')
  } finally {
    loading.value = false
  }
}

async function checkAllStatuses() {
  const map: Record<number, boolean> = {}
  await Promise.all(checkins.value.map(async (c) => {
    try {
      const r = await getCheckInStatus(c.id)
      map[c.id] = r.data?.checked || false
    } catch {
      map[c.id] = false
    }
  }))
  checkedMap.value = map
}

function showCreate() {
  createForm.title = ''
  createForm.checkinType = 'course'
  createForm.startTime = ''
  createForm.endTime = ''
  createForm.courseId = null
  createVisible.value = true
}

async function doCreate() {
  if (!createForm.title.trim()) {
    ElMessage.warning('请输入签到标题')
    return
  }
  if (!createForm.startTime) {
    ElMessage.warning('请选择开始时间')
    return
  }
  if (!createForm.endTime) {
    ElMessage.warning('请选择结束时间')
    return
  }
  try {
    await createCheckIn(createForm)
    ElMessage.success('发起成功')
    createVisible.value = false
    fetchCheckIns()
  } catch {
    ElMessage.error('发起失败')
  }
}

async function handleCheckIn(c: any) {
  try {
    await doCheckIn(c.id)
    ElMessage.success('签到成功')
    checkedMap.value[c.id] = true
    fetchCheckIns()
  } catch {
    ElMessage.error('签到失败')
  }
}

async function viewRecords(c: any) {
  try {
    const r = await getCheckInRecords(c.id)
    records.value = r.data || []
    recordsVisible.value = true
  } catch {
    records.value = []
    recordsVisible.value = true
  }
}

onMounted(() => {
  fetchCheckIns()
  if (isTeacher.value) {
    getTeacherCourses().then(r => { teacherCourses.value = r.data || [] }).catch(() => {})
  }
})
</script>
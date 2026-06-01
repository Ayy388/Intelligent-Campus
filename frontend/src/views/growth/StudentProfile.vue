<template>
  <div class="animate__animated animate__fadeInUp">
    <div v-if="isTeacher" class="mb-6 animate__animated animate__fadeInUp" style="animation-delay:0.05s">
      <div class="flex items-center gap-3 bg-white rounded-xl border border-soft px-5 py-3">
        <el-icon size="18" class="text-mist"><Search /></el-icon>
        <el-input
          v-model="searchStudentId"
          placeholder="输入学生学号查询档案"
          clearable
          class="flex-1"
          @keyup.enter="searchProfile"
        />
        <button
          @click="searchProfile"
          class="px-5 py-2 bg-ink text-white rounded-lg text-sm font-medium hover:bg-steel transition-all duration-200 active:scale-[0.97]"
        >
          查询
        </button>
      </div>
    </div>

    <div v-if="!profile && !loading" class="animate__animated animate__fadeInUp" style="animation-delay:0.1s">
      <div class="bg-white rounded-xl border border-soft p-16 text-center">
        <div class="w-16 h-16 mx-auto mb-4 rounded-full bg-wash flex items-center justify-center">
          <el-icon size="28" class="text-mist"><UserFilled /></el-icon>
        </div>
        <div class="text-mist text-sm mb-4">{{ isTeacher ? '请输入学生学号查询档案' : '你还没有创建成长档案' }}</div>
        <button
          v-if="!isTeacher"
          @click="openCreate"
          class="px-6 py-2.5 bg-ink text-white rounded-lg text-sm font-medium hover:bg-steel transition-all duration-200 active:scale-[0.97]"
        >
          创建档案
        </button>
      </div>
    </div>

    <div v-if="loading" class="flex items-center justify-center py-20">
      <div class="flex flex-col items-center gap-3">
        <el-icon class="is-loading text-ink" size="28"><Loading /></el-icon>
        <span class="text-sm text-mist">加载中...</span>
      </div>
    </div>

    <template v-if="profile && !loading">
      <div class="animate__animated animate__fadeInUp" style="animation-delay:0.05s">
        <div class="bg-white rounded-2xl border border-soft p-6 sm:p-8 mb-6 relative overflow-hidden">
          <div class="absolute top-0 right-0 w-64 h-64 bg-gradient-to-bl from-ink/[0.03] to-transparent rounded-full -translate-y-1/2 translate-x-1/2 pointer-events-none" />
          <div class="flex items-center gap-5 relative">
            <div class="w-16 h-16 sm:w-20 sm:h-20 rounded-full bg-gradient-to-br from-ink to-steel flex items-center justify-center text-white text-2xl sm:text-3xl font-bold shrink-0 shadow-sm">
              {{ avatarChar }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-3 flex-wrap">
                <h1 class="text-xl sm:text-2xl font-bold text-ink tracking-tight">{{ profile.studentName || '-' }}</h1>
                <span class="px-2.5 py-0.5 rounded-full text-[11px] font-medium bg-ink/10 text-steel">{{ roleLabel }}</span>
              </div>
              <div class="flex items-center gap-4 mt-1.5 text-sm text-mist">
                <span class="flex items-center gap-1.5">
                  <el-icon size="14"><Document /></el-icon>
                  {{ profile.studentId || '-' }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-5 mb-6">
        <div class="animate__animated animate__fadeInUp" style="animation-delay:0.1s">
          <div class="bg-white rounded-xl border border-soft p-5 h-full hover:shadow-sm transition-shadow duration-200">
            <div class="flex items-center gap-2 mb-5">
              <div class="w-8 h-8 rounded-lg bg-ink/10 flex items-center justify-center">
                <el-icon size="16" class="text-ink"><User /></el-icon>
              </div>
              <span class="text-sm font-semibold text-ink">基本信息</span>
            </div>
            <div class="space-y-4">
              <div>
                <div class="text-xs text-mist mb-1">姓名</div>
                <div class="text-sm font-medium text-ink">{{ profile.studentName || '-' }}</div>
              </div>
              <div>
                <div class="text-xs text-mist mb-1">学号</div>
                <div class="text-sm font-medium text-ink">{{ profile.studentId || '-' }}</div>
              </div>
              <div>
                <div class="text-xs text-mist mb-1">专业</div>
                <div class="text-sm font-medium text-ink">{{ profile.majorName || profile.major || '-' }}</div>
              </div>
              <div>
                <div class="text-xs text-mist mb-1">班级</div>
                <div class="text-sm font-medium text-ink">{{ profile.className || profile.class || '-' }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="space-y-5 animate__animated animate__fadeInUp" style="animation-delay:0.15s">
          <div class="bg-white rounded-xl border border-soft p-5 hover:shadow-sm transition-shadow duration-200">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-8 h-8 rounded-lg bg-ink/10 flex items-center justify-center">
                <el-icon size="16" class="text-ink"><TrendCharts /></el-icon>
              </div>
              <span class="text-sm font-semibold text-ink">GPA</span>
            </div>
            <div class="flex items-end gap-2">
              <span class="text-4xl font-bold text-ink tracking-tight">{{ profile.gpa != null ? profile.gpa : '-' }}</span>
              <span class="text-sm text-mist mb-1">/ 5.0</span>
            </div>
          </div>

          <div class="bg-white rounded-xl border border-soft p-5 hover:shadow-sm transition-shadow duration-200">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-8 h-8 rounded-lg bg-ink/10 flex items-center justify-center">
                <el-icon size="16" class="text-ink"><TrophyBase /></el-icon>
              </div>
              <span class="text-sm font-semibold text-ink">获奖经历</span>
            </div>
            <div v-if="profile.awards" class="text-sm text-steel leading-relaxed whitespace-pre-wrap">
              {{ profile.awards }}
            </div>
            <div v-else class="text-sm text-mist italic">暂无获奖记录</div>
          </div>
        </div>

        <div class="animate__animated animate__fadeInUp" style="animation-delay:0.2s">
          <div class="bg-white rounded-xl border border-soft p-5 h-full hover:shadow-sm transition-shadow duration-200">
            <div class="flex items-center gap-2 mb-4">
              <div class="w-8 h-8 rounded-lg bg-ink/10 flex items-center justify-center">
                <el-icon size="16" class="text-ink"><Briefcase /></el-icon>
              </div>
              <span class="text-sm font-semibold text-ink">实践经历</span>
            </div>
            <div v-if="profile.experiences" class="text-sm text-steel leading-relaxed whitespace-pre-wrap">
              {{ profile.experiences }}
            </div>
            <div v-else class="text-sm text-mist italic">暂无实践经历</div>
          </div>
        </div>
      </div>

      <div class="animate__animated animate__fadeInUp" style="animation-delay:0.25s">
        <div class="bg-white rounded-xl border border-soft p-5 sm:p-6 mb-6 hover:shadow-sm transition-shadow duration-200">
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <div class="w-8 h-8 rounded-lg bg-ink/10 flex items-center justify-center">
                <el-icon size="16" class="text-ink"><ChatDotSquare /></el-icon>
              </div>
              <span class="text-sm font-semibold text-ink">教师评语</span>
            </div>
            <button
              v-if="isTeacher"
              @click="showEvaluation"
              class="px-4 py-2 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-all duration-200 active:scale-[0.97]"
            >
              添加评语
            </button>
          </div>
          <div v-if="profile.evaluation" class="relative pl-5 border-l-[3px] border-ink/20 bg-wash rounded-r-lg p-4">
            <el-icon size="20" class="text-mist/40 absolute top-3 left-3"><ChatLineSquare /></el-icon>
            <div class="text-sm text-steel leading-relaxed whitespace-pre-wrap pl-6">
              {{ profile.evaluation }}
            </div>
          </div>
          <div v-else class="text-sm text-mist italic py-2">暂无评语</div>
        </div>
      </div>

      <div v-if="!isTeacher" class="animate__animated animate__fadeInUp" style="animation-delay:0.3s">
        <button
          @click="showEdit"
          class="px-6 py-2.5 bg-ink text-white rounded-lg text-sm font-medium hover:bg-steel transition-all duration-200 active:scale-[0.97] inline-flex items-center gap-2"
        >
          <el-icon size="15"><Edit /></el-icon>
          编辑档案
        </button>
      </div>
    </template>

    <el-dialog
      v-model="createVisible"
      title="创建档案"
      width="520px"
      :close-on-click-modal="false"
      class="profile-dialog"
    >
      <div class="px-1">
        <el-form :model="createForm" label-position="top">
          <el-form-item label="获奖经历" class="dialog-form-item">
            <el-input
              v-model="createForm.awards"
              type="textarea"
              :rows="4"
              placeholder="请填写你的获奖经历，包含奖项名称、获奖时间等"
            />
          </el-form-item>
          <el-form-item label="实践经历" class="dialog-form-item">
            <el-input
              v-model="createForm.experiences"
              type="textarea"
              :rows="4"
              placeholder="请填写你的实践经历，包含项目、实习、社团活动等"
            />
          </el-form-item>
          <el-form-item label="GPA" class="dialog-form-item">
            <el-input-number
              v-model="createForm.gpa"
              :min="0"
              :max="5"
              :step="0.1"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-3">
          <el-button @click="createVisible=false" class="!px-5">取消</el-button>
          <el-button type="primary" @click="doCreate" class="!px-6">创建</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="editVisible"
      title="编辑档案"
      width="520px"
      :close-on-click-modal="false"
      class="profile-dialog"
    >
      <div class="px-1">
        <el-form :model="editForm" label-position="top">
          <el-form-item label="获奖经历" class="dialog-form-item">
            <el-input
              v-model="editForm.awards"
              type="textarea"
              :rows="4"
              placeholder="请填写你的获奖经历，包含奖项名称、获奖时间等"
            />
          </el-form-item>
          <el-form-item label="实践经历" class="dialog-form-item">
            <el-input
              v-model="editForm.experiences"
              type="textarea"
              :rows="4"
              placeholder="请填写你的实践经历，包含项目、实习、社团活动等"
            />
          </el-form-item>
          <el-form-item label="GPA" class="dialog-form-item">
            <el-input-number
              v-model="editForm.gpa"
              :min="0"
              :max="5"
              :step="0.1"
              :precision="2"
              class="!w-full"
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-3">
          <el-button @click="editVisible=false" class="!px-5">取消</el-button>
          <el-button type="primary" @click="doSave" class="!px-6">保存</el-button>
        </div>
      </template>
    </el-dialog>

    <el-dialog
      v-model="evaluationVisible"
      title="添加评语"
      width="520px"
      :close-on-click-modal="false"
      class="profile-dialog"
    >
      <div class="px-1">
        <el-form label-position="top">
          <el-form-item label="评语内容" class="dialog-form-item">
            <el-input
              v-model="evaluationContent"
              type="textarea"
              :rows="5"
              placeholder="请输入对学生的评语..."
            />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <div class="flex items-center justify-end gap-3">
          <el-button @click="evaluationVisible=false" class="!px-5">取消</el-button>
          <el-button type="primary" @click="submitEvaluation" class="!px-6">提交评语</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getProfile, saveProfile, addEvaluation } from '@/api/growth'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import {
  Search,
  UserFilled,
  Document,
  User,
  TrendCharts,
  TrophyBase,
  Briefcase,
  ChatDotSquare,
  ChatLineSquare,
  Edit,
  Loading,
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const isTeacher = computed(() => userStore.role === 'teacher' || userStore.role === 'admin' || userStore.role === 'counselor')
const roleLabel = computed(() => {
  if (userStore.role === 'teacher') return '教师'
  if (userStore.role === 'admin') return '管理员'
  if (userStore.role === 'counselor') return '辅导员'
  return '学生'
})
const avatarChar = computed(() => {
  const name = profile.value?.studentName || userStore.userInfo?.realName || ''
  return name.charAt(0) || '?'
})

const profile = ref<any>(null)
const loading = ref(false)

const searchStudentId = ref('')

const createVisible = ref(false)
const createForm = reactive({ awards: '', experiences: '', gpa: 0 })

const editVisible = ref(false)
const editForm = reactive({ awards: '', experiences: '', gpa: 0 })

const evaluationVisible = ref(false)
const evaluationContent = ref('')

async function fetchProfile() {
  loading.value = true
  try {
    const r = await getProfile()
    profile.value = r.data
  } catch {
    profile.value = null
  } finally {
    loading.value = false
  }
}

async function searchProfile() {
  if (!searchStudentId.value.trim()) {
    ElMessage.warning('请输入学号')
    return
  }
  loading.value = true
  try {
    const r = await request.get('/growth/profile', { params: { studentId: searchStudentId.value.trim() } })
    profile.value = r.data
  } catch {
    profile.value = null
    ElMessage.warning('未找到该学生档案')
  } finally {
    loading.value = false
  }
}

function openCreate() {
  createForm.awards = ''
  createForm.experiences = ''
  createForm.gpa = 0
  createVisible.value = true
}

async function doCreate() {
  try {
    await saveProfile(createForm)
    ElMessage.success('创建成功')
    createVisible.value = false
    fetchProfile()
  } catch {
    ElMessage.error('创建失败')
  }
}

function showEdit() {
  if (profile.value) {
    editForm.awards = profile.value.awards || ''
    editForm.experiences = profile.value.experiences || ''
    editForm.gpa = profile.value.gpa ?? 0
  }
  editVisible.value = true
}

async function doSave() {
  try {
    await saveProfile(editForm)
    ElMessage.success('保存成功')
    editVisible.value = false
    fetchProfile()
  } catch {
    ElMessage.error('保存失败')
  }
}

function showEvaluation() {
  evaluationContent.value = ''
  evaluationVisible.value = true
}

async function submitEvaluation() {
  if (!evaluationContent.value.trim()) {
    ElMessage.warning('请输入评语内容')
    return
  }
  try {
    await addEvaluation(profile.value.studentId, evaluationContent.value)
    ElMessage.success('评语已提交')
    evaluationVisible.value = false
    fetchProfile()
  } catch {
    ElMessage.error('提交失败')
  }
}

onMounted(() => {
  if (!isTeacher.value) {
    fetchProfile()
  }
})
</script>

<style scoped>
.profile-dialog :deep(.el-dialog__header) {
  padding: 20px 24px 16px;
  font-weight: 600;
  font-size: 16px;
}
.profile-dialog :deep(.el-dialog__body) {
  padding: 8px 24px 16px;
}
.profile-dialog :deep(.el-dialog__footer) {
  padding: 12px 24px 20px;
  border-top: 1px solid #F3F4F6;
}
.profile-dialog :deep(.el-form-item__label) {
  font-weight: 500;
  font-size: 13px;
  color: #374151;
  padding-bottom: 4px;
}
.profile-dialog :deep(.el-input-number .el-input__wrapper) {
  padding-left: 12px;
}
.profile-dialog :deep(.el-textarea__inner) {
  font-size: 13px;
  line-height: 1.6;
}
.dialog-form-item {
  margin-bottom: 18px;
}
</style>
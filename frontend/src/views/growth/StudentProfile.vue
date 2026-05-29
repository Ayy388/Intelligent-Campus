<template>
  <div>
    <div class="flex items-center justify-between mb-5">
      <span class="text-lg font-bold text-ink">成长档案</span>
      <div v-if="isTeacher" class="flex items-center gap-3">
        <div class="flex items-center gap-2">
          <el-input v-model="searchStudentId" placeholder="输入学生学号" size="small" class="!w-44" @keyup.enter="searchProfile" />
          <button @click="searchProfile" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">查询</button>
        </div>
      </div>
    </div>

    <div v-if="!profile && !loading" class="bg-white rounded-xl border border-soft p-10 text-center">
      <div class="text-mist text-sm mb-3">{{ isTeacher ? '请输入学生学号查询档案' : '暂无档案' }}</div>
      <button v-if="!isTeacher" @click="openCreate" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">创建档案</button>
    </div>

    <template v-if="profile">
      <div class="grid grid-cols-1 md:grid-cols-2 gap-5 mb-5">
        <div class="bg-white rounded-xl border border-soft p-5">
          <div class="text-sm font-semibold text-ink mb-4">基本信息</div>
          <div class="space-y-3">
            <div>
              <div class="text-xs text-ash mb-1">姓名</div>
              <div class="text-sm font-medium text-ink">{{ profile.studentName || '-' }}</div>
            </div>
            <div>
              <div class="text-xs text-ash mb-1">学号</div>
              <div class="text-sm font-medium text-ink">{{ profile.studentId || '-' }}</div>
            </div>
            <div>
              <div class="text-xs text-ash mb-1">GPA</div>
              <div class="text-2xl font-bold text-ink">{{ profile.gpa != null ? profile.gpa : '-' }}</div>
            </div>
          </div>
        </div>

        <div class="space-y-5">
          <div class="bg-white rounded-xl border border-soft p-5">
            <div class="text-sm font-semibold text-ink mb-3">获奖经历</div>
            <div class="text-sm text-steel whitespace-pre-wrap">{{ profile.awards || '暂无获奖记录' }}</div>
          </div>
          <div class="bg-white rounded-xl border border-soft p-5">
            <div class="text-sm font-semibold text-ink mb-3">实践经历</div>
            <div class="text-sm text-steel whitespace-pre-wrap">{{ profile.experiences || '暂无实践经历' }}</div>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl border border-soft p-5 mb-5">
        <div class="flex items-center justify-between mb-3">
          <div class="text-sm font-semibold text-ink">教师评语</div>
          <button v-if="isTeacher" @click="showEvaluation" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">添加评语</button>
        </div>
        <div class="text-sm text-steel whitespace-pre-wrap">{{ profile.evaluation || '暂无评语' }}</div>
      </div>

      <div v-if="!isTeacher">
        <button @click="showEdit" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">编辑档案</button>
      </div>
    </template>

    <el-dialog v-model="createVisible" title="创建档案" width="450px">
      <el-form :model="createForm">
        <el-form-item label="获奖经历">
          <el-input v-model="createForm.awards" type="textarea" :rows="3" placeholder="请输入获奖经历" />
        </el-form-item>
        <el-form-item label="实践经历">
          <el-input v-model="createForm.experiences" type="textarea" :rows="3" placeholder="请输入实践经历" />
        </el-form-item>
        <el-form-item label="GPA">
          <el-input-number v-model="createForm.gpa" :min="0" :max="5" :step="0.1" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="doCreate">创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="editVisible" title="编辑档案" width="450px">
      <el-form :model="editForm">
        <el-form-item label="获奖经历">
          <el-input v-model="editForm.awards" type="textarea" :rows="3" placeholder="请输入获奖经历" />
        </el-form-item>
        <el-form-item label="实践经历">
          <el-input v-model="editForm.experiences" type="textarea" :rows="3" placeholder="请输入实践经历" />
        </el-form-item>
        <el-form-item label="GPA">
          <el-input-number v-model="editForm.gpa" :min="0" :max="5" :step="0.1" :precision="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible=false">取消</el-button>
        <el-button type="primary" @click="doSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="evaluationVisible" title="添加评语" width="450px">
      <el-form>
        <el-form-item label="评语内容">
          <el-input v-model="evaluationContent" type="textarea" :rows="4" placeholder="请输入评语内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="evaluationVisible=false">取消</el-button>
        <el-button type="primary" @click="submitEvaluation">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { getProfile, saveProfile, addEvaluation } from '@/api/growth'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const userStore = useUserStore()
const isTeacher = computed(() => userStore.role === 'teacher' || userStore.role === 'admin')

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
    editForm.gpa = profile.value.gpa || 0
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
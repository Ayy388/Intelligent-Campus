<template>
  <div class="max-w-3xl mx-auto space-y-6">
    <!-- 个人信息卡片 -->
    <div class="relative overflow-hidden rounded-2xl bg-gradient-to-br from-gray-900 via-gray-800 to-gray-700 p-6 sm:p-8">
      <div class="absolute top-0 right-0 w-64 h-64 bg-white/5 rounded-full -translate-y-1/2 translate-x-1/2"></div>
      <div class="absolute bottom-0 left-1/3 w-48 h-48 bg-white/[0.03] rounded-full translate-y-1/2"></div>

      <div class="relative flex flex-col sm:flex-row items-center sm:items-start gap-6">
        <div class="relative shrink-0 group">
          <div
            class="w-20 h-20 rounded-full flex items-center justify-center text-white text-2xl font-bold shadow-lg overflow-hidden"
          >
            <img v-if="avatarUrl" :src="avatarUrl" class="w-full h-full object-cover" />
            <span v-else class="bg-gradient-to-br from-indigo-500 to-purple-600 w-full h-full flex items-center justify-center">
              {{ avatarLetter }}
            </span>
          </div>
          <label
            class="absolute inset-0 rounded-full flex items-center justify-center bg-black/40 opacity-0 group-hover:opacity-100 cursor-pointer transition-opacity duration-200"
          >
            <el-icon class="text-white" :size="18"><Camera /></el-icon>
            <input type="file" accept="image/*" class="hidden" @change="handleFileChange" />
          </label>
        </div>
        <div class="flex-1 text-center sm:text-left">
          <h1 class="text-2xl font-bold text-white">{{ userStore.userInfo?.realName || '用户' }}</h1>
          <div class="flex flex-wrap items-center justify-center sm:justify-start gap-2 mt-2">
            <span class="px-3 py-0.5 rounded-full text-xs font-semibold bg-white/10 text-indigo-300">{{ roleLabel }}</span>
            <span class="text-gray-400 text-sm">{{ userStore.userInfo?.username }}</span>
          </div>
          <div class="flex flex-wrap items-center justify-center sm:justify-start gap-x-6 gap-y-1 mt-3 text-sm text-gray-400">
            <span>性别：{{ genderLabel }}</span>
            <span v-if="userStore.userInfo?.phone">手机：{{ userStore.userInfo?.phone }}</span>
            <span v-if="userStore.userInfo?.departmentName">院系：{{ userStore.userInfo?.departmentName }}</span>
            <span v-if="userStore.userInfo?.className">班级：{{ userStore.userInfo?.className }}</span>
            <span v-if="userStore.userInfo?.majorName">专业：{{ userStore.userInfo?.majorName }}</span>
            <span v-if="userStore.userInfo?.gradeName">年级：{{ userStore.userInfo?.gradeName }}</span>
            <span v-if="userStore.userInfo?.counselorName">辅导员：{{ userStore.userInfo?.counselorName }}</span>
            <span>注册时间：{{ formatDate(userStore.userInfo?.createTime) }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 编辑资料 -->
    <div class="bg-white rounded-xl p-6 sm:p-8 shadow-sm border border-gray-100">
      <h2 class="text-base font-semibold text-gray-900 mb-6 flex items-center gap-2">
        <span class="w-1 h-4 bg-indigo-500 rounded-full"></span>
        编辑资料
      </h2>

      <el-form :model="form" label-position="top" class="max-w-xl">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">
            保存修改
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Camera } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/user'
import { updateProfile } from '@/api/auth'
import request from '@/utils/request'

const userStore = useUserStore()

const roleLabel = userStore.role === 'admin' ? '管理员' : userStore.role === 'teacher' ? '教师' : userStore.role === 'counselor' ? '辅导员' : '学生'

const genderLabel = computed(() => userStore.userInfo?.gender === 1 ? '男' : userStore.userInfo?.gender === 0 ? '女' : '未设置')

const avatarLetter = computed(() => (userStore.userInfo?.realName || userStore.userInfo?.username || 'U')[0])

const avatarUrl = computed(() => userStore.userInfo?.avatar || '')

const uploading = ref(false)

const form = reactive({
  email: ''
})

const saving = ref(false)

function formatDate(dateStr: string | undefined | null) {
  if (!dateStr) return '未知'
  return dateStr.substring(0, 10)
}

function initForm() {
  const info = userStore.userInfo
  if (info) {
    form.email = info.email || ''
  }
}

onMounted(initForm)

async function handleFileChange(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (file.size > 2 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 2MB')
    input.value = ''
    return
  }

  uploading.value = true
  try {
    const formData = new FormData()
    formData.append('file', file)
    const res = await request.post('/upload/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    await updateProfile({ avatar: res.data })
    await userStore.fetchUserInfo()
    ElMessage.success('头像更新成功')
  } catch {
    // request.ts 已处理错误提示
  } finally {
    uploading.value = false
    input.value = ''
  }
}

async function handleSave() {
  saving.value = true
  try {
    await updateProfile({ email: form.email })
    await userStore.fetchUserInfo()
    ElMessage.success('保存成功')
  } catch {
    // request.ts 已处理错误提示
  } finally {
    saving.value = false
  }
}
</script>

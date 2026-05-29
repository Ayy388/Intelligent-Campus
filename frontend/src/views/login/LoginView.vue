<template>
  <div class="min-h-screen bg-cloud flex items-center justify-center p-6">
    <div class="animate__animated animate__fadeInUp w-full max-w-[400px] bg-white rounded-xl border border-soft p-10">
      <div class="text-center mb-8">
        <div class="text-2xl font-bold text-ink tracking-tight">智能校园</div>
        <div class="text-mist text-[13px] mt-1 tracking-wide">Intelligent Campus</div>
      </div>
      <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
        <div class="space-y-4">
          <div class="relative">
            <el-input
              v-model="form.username"
              placeholder="学号 / 工号"
              size="large"
              :prefix-icon="User"
              class="login-input"
            />
          </div>
          <div class="relative">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="login-input"
            />
          </div>
          <el-button
            native-type="submit"
            :loading="loading"
            size="large"
            class="w-full !h-11 !rounded-lg !bg-ink !border-ink hover:!bg-steel hover:!border-steel !text-white !font-semibold !text-[15px] !tracking-wide transition-all duration-200"
          >
            登 录
          </el-button>
        </div>
      </el-form>
      <div class="mt-6 pt-5 border-t border-wash text-center text-mist text-xs tracking-wide">
        开发阶段测试账号 &nbsp;admin · t001 · s001 &nbsp;密码 123456
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

async function handleLogin() {
  if (!form.username || !form.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await userStore.login(form.username, form.password)
    router.push('/dashboard')
  } catch {
    ElMessage.error('用户名或密码错误')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
:deep(.login-input .el-input__wrapper) {
  background: #F9FAFB;
  border-radius: 10px;
  height: 48px;
  box-shadow: 0 0 0 1px #E5E7EB inset;
  transition: box-shadow 0.2s ease, background 0.2s ease;
}
:deep(.login-input .el-input__wrapper:hover) {
  background: #F3F4F6;
}
:deep(.login-input.is-focus .el-input__wrapper) {
  background: #FFFFFF;
  box-shadow: 0 0 0 2px #111827 inset;
}
:deep(.login-input .el-input__prefix) {
  color: #9CA3AF;
}
:deep(.login-input.is-focus .el-input__prefix) {
  color: #111827;
}
</style>

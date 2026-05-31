<template>
  <div class="relative min-h-screen flex items-center justify-center overflow-hidden">
    <div class="fixed inset-0 bg-cover bg-center bg-no-repeat"
      style="background-image: url(https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Modern+Chinese+university+campus+aerial+view+sunset+building+library+green+trees+peaceful+atmosphere&image_size=landscape_16_9)">
    </div>
    <div class="fixed inset-0 bg-black/55"></div>
    <div class="relative z-10 w-full px-5 py-10 flex flex-col items-center justify-center min-h-screen">
      <div class="text-center mb-10 animate-fade-in">
        <h1 class="text-5xl sm:text-6xl font-bold text-white tracking-[0.15em] leading-tight">智能校园</h1>
        <p class="text-white/45 text-sm sm:text-base mt-3 tracking-[0.25em] font-light">Intelligent Campus</p>
      </div>
      <div class="w-full max-w-[420px] backdrop-blur-2xl bg-white/10 rounded-2xl border border-white/20 p-8 sm:p-10 shadow-[0_8px_32px_rgba(0,0,0,0.37)] animate-slide-up">
        <el-form :model="form" label-position="top" @submit.prevent="handleLogin">
          <div class="space-y-5">
            <el-input
              v-model="form.username"
              placeholder="学号 / 工号"
              size="large"
              :prefix-icon="User"
              class="login-input"
            />
            <el-input
              v-model="form.password"
              type="password"
              placeholder="密码"
              size="large"
              :prefix-icon="Lock"
              show-password
              class="login-input"
            />
            <el-button
              native-type="submit"
              :loading="loading"
              size="large"
              class="w-full !h-11 !rounded-xl !bg-white/20 !border-white/30 hover:!bg-white/30 hover:!border-white/40 !text-white !text-[15px] !font-semibold !tracking-widest transition-all duration-300"
            >
              登 录
            </el-button>
          </div>
        </el-form>
        <div class="mt-8 pt-6 border-t border-white/10 text-center text-white/30 text-xs tracking-wider">
          开发阶段测试账号 &nbsp;admin · t001 · s001 &nbsp;密码 123456
        </div>
      </div>
      <div class="absolute bottom-6 text-white/25 text-xs tracking-wider">
        &copy; 2026 智能校园 All Rights Reserved
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
@keyframes fade-in {
  from { opacity: 0; transform: translateY(-12px); }
  to { opacity: 1; transform: translateY(0); }
}
@keyframes slide-up {
  from { opacity: 0; transform: translateY(30px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-fade-in {
  animation: fade-in 0.8s ease-out both;
}
.animate-slide-up {
  animation: slide-up 0.8s ease-out 0.2s both;
}
:deep(.login-input .el-input__wrapper) {
  background: rgba(255,255,255,0.12);
  border-radius: 12px;
  height: 48px;
  box-shadow: 0 0 0 1px rgba(255,255,255,0.15) inset;
  transition: box-shadow 0.25s ease, background 0.25s ease;
}
:deep(.login-input .el-input__wrapper:hover) {
  background: rgba(255,255,255,0.18);
}
:deep(.login-input.is-focus .el-input__wrapper) {
  background: rgba(255,255,255,0.22);
  box-shadow: 0 0 0 2px rgba(255,255,255,0.5) inset;
}
:deep(.login-input .el-input__inner) {
  color: #fff;
}
:deep(.login-input .el-input__inner::placeholder) {
  color: rgba(255,255,255,0.45);
}
:deep(.login-input .el-input__prefix) {
  color: rgba(255,255,255,0.45);
}
:deep(.login-input.is-focus .el-input__prefix) {
  color: rgba(255,255,255,0.8);
}
:deep(.login-input .el-input__suffix) {
  color: rgba(255,255,255,0.45);
}
</style>
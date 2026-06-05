<template>
  <div class="relative min-h-screen flex items-center justify-center overflow-hidden bg-[#0a0e1a]">
    <div class="fixed inset-0 bg-cover bg-center bg-no-repeat scale-110"
      style="background-image: url(https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=Modern+Chinese+university+campus+aerial+view+sunset+building+library+green+trees+peaceful+atmosphere&image_size=landscape_16_9)">
    </div>
    <div class="fixed inset-0 bg-gradient-to-b from-black/60 via-black/50 to-black/70"></div>

    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <span
        v-for="i in 50"
        :key="i"
        class="absolute rounded-full bg-white"
        :style="{
          width: `${Math.random() * 3 + 1}px`,
          height: `${Math.random() * 3 + 1}px`,
          left: `${Math.random() * 100}%`,
          top: `${Math.random() * 100}%`,
          opacity: Math.random() * 0.6 + 0.2,
          animation: `twinkle ${Math.random() * 4 + 3}s ease-in-out ${Math.random() * 5}s infinite alternate`,
          boxShadow: `0 0 ${Math.random() * 4 + 2}px rgba(255,255,255,${Math.random() * 0.4 + 0.2})`,
        }"
      />
    </div>

    <div class="fixed inset-0 overflow-hidden pointer-events-none">
      <div
        v-for="i in 3"
        :key="'orb-' + i"
        class="absolute rounded-full"
        :style="{
          width: `${300 + i * 100}px`,
          height: `${300 + i * 100}px`,
          left: `${20 + i * 25}%`,
          top: `${10 + (i % 2) * 40}%`,
          background: `radial-gradient(circle, rgba(99,102,241,${0.06 - i * 0.015}), transparent 70%)`,
          animation: `float-orb ${8 + i * 3}s ease-in-out ${i * 2}s infinite alternate`,
          filter: 'blur(60px)',
        }"
      />
    </div>

    <div class="relative z-10 w-full px-5 py-10 flex flex-col items-center justify-center min-h-screen">
      <div class="text-center mb-10 animate-fade-in">
        <div class="relative inline-block">
          <div class="absolute -inset-8 bg-gradient-to-r from-indigo-500/20 via-purple-500/20 to-indigo-500/20 rounded-full blur-3xl"></div>
          <div class="relative">
            <div class="flex items-center justify-center gap-3 mb-5">
              <div class="h-px w-12 bg-gradient-to-r from-transparent via-indigo-400/60 to-transparent"></div>
              <div class="w-2 h-2 rounded-full bg-indigo-400 shadow-[0_0_12px_rgba(129,140,248,0.6)]"></div>
              <div class="h-px w-12 bg-gradient-to-r from-transparent via-indigo-400/60 to-transparent"></div>
            </div>
            <h1 class="text-5xl sm:text-6xl font-bold leading-tight bg-gradient-to-r from-white via-indigo-200 to-purple-200 bg-clip-text text-transparent tracking-[0.15em]">
              智能校园
            </h1>
          </div>
        </div>
        <p class="text-white/40 text-sm sm:text-base mt-4 tracking-[0.3em] font-light">Intelligent Campus</p>
      </div>

      <div class="w-full max-w-[420px] backdrop-blur-2xl bg-white/10 rounded-2xl border border-white/20 p-8 sm:p-10 shadow-[0_8px_32px_rgba(0,0,0,0.37)] animate-slide-up relative group">
        <div class="absolute inset-0 rounded-2xl bg-gradient-to-br from-white/5 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none"></div>
        <div class="absolute -inset-[1px] rounded-2xl bg-gradient-to-br from-indigo-500/10 via-transparent to-purple-500/10 opacity-0 group-hover:opacity-100 transition-opacity duration-700 pointer-events-none"></div>
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
              class="w-full !h-11 !rounded-xl !border-0 !text-white !text-[15px] !font-semibold !tracking-widest transition-all duration-500 login-btn"
            >
              <span class="relative z-10">登 录</span>
            </el-button>
          </div>
        </el-form>
        <div class="mt-8 pt-6 border-t border-white/10 text-center">
          <span class="text-white/25 text-xs tracking-wider">开发阶段测试账号</span>
          <div class="flex items-center justify-center gap-2 mt-2 flex-wrap">
            <span class="px-2.5 py-1 rounded-md bg-white/5 border border-white/10 text-indigo-300/60 text-xs tracking-wider font-mono">admin</span>
            <span class="px-2.5 py-1 rounded-md bg-white/5 border border-white/10 text-indigo-300/60 text-xs tracking-wider font-mono">t001</span>
            <span class="px-2.5 py-1 rounded-md bg-white/5 border border-white/10 text-indigo-300/60 text-xs tracking-wider font-mono">s001</span>
            <span class="text-white/20 text-xs mx-1">·</span>
            <span class="px-2.5 py-1 rounded-md bg-white/5 border border-white/10 text-amber-300/60 text-xs tracking-wider font-mono">123456</span>
          </div>
        </div>
      </div>

      <div class="absolute bottom-6 flex flex-col items-center gap-1.5">
        <div class="w-8 h-px bg-gradient-to-r from-transparent via-white/20 to-transparent"></div>
        <p class="text-white/20 text-xs tracking-[0.2em] font-light">&copy; 2026 智能校园 All Rights Reserved</p>
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
@keyframes twinkle {
  0% { opacity: 0.2; transform: scale(0.8); }
  100% { opacity: 0.8; transform: scale(1.2); }
}
@keyframes float-orb {
  0% { transform: translate(0, 0) scale(1); }
  100% { transform: translate(30px, -30px) scale(1.1); }
}
.animate-fade-in {
  animation: fade-in 0.8s ease-out both;
}
.animate-slide-up {
  animation: slide-up 0.8s ease-out 0.2s both;
}
:deep(.login-input .el-input__wrapper) {
  background: rgba(255,255,255,0.08);
  border-radius: 12px;
  height: 48px;
  box-shadow: 0 0 0 1px rgba(255,255,255,0.1) inset;
  transition: box-shadow 0.3s ease, background 0.3s ease, transform 0.3s ease;
}
:deep(.login-input .el-input__wrapper:hover) {
  background: rgba(255,255,255,0.14);
  box-shadow: 0 0 0 1px rgba(255,255,255,0.2) inset;
}
:deep(.login-input.is-focus .el-input__wrapper) {
  background: rgba(255,255,255,0.12);
  box-shadow:
    0 0 0 1.5px rgba(129,140,248,0.5) inset,
    0 0 20px rgba(129,140,248,0.15),
    0 0 40px rgba(129,140,248,0.08);
  transform: scale(1.01);
}
:deep(.login-input .el-input__inner) {
  color: #fff;
}
:deep(.login-input .el-input__inner::placeholder) {
  color: rgba(255,255,255,0.35);
}
:deep(.login-input .el-input__prefix) {
  color: rgba(255,255,255,0.35);
  transition: color 0.3s ease;
}
:deep(.login-input.is-focus .el-input__prefix) {
  color: rgba(129,140,248,0.8);
}
:deep(.login-input .el-input__suffix) {
  color: rgba(255,255,255,0.35);
}
.login-btn {
  background: linear-gradient(135deg, rgba(99,102,241,0.8), rgba(139,92,246,0.8));
  position: relative;
  overflow: hidden;
  isolation: isolate;
}
.login-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, rgba(129,140,248,1), rgba(167,139,250,1));
  opacity: 0;
  transition: opacity 0.5s ease;
  border-radius: inherit;
}
.login-btn:hover::before {
  opacity: 1;
}
.login-btn::after {
  content: '';
  position: absolute;
  inset: -2px;
  background: linear-gradient(135deg, rgba(129,140,248,0.4), rgba(167,139,250,0.4));
  border-radius: inherit;
  filter: blur(12px);
  opacity: 0;
  transition: opacity 0.5s ease;
  z-index: -1;
}
.login-btn:hover::after {
  opacity: 1;
}
.login-btn:hover {
  transform: translateY(-1px);
}
.login-btn:active {
  transform: translateY(0);
}
</style>
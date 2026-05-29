<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>智能校园服务系统</h2>
      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item><el-input v-model="form.username" placeholder="学号/工号" prefix-icon="User" /></el-form-item>
        <el-form-item><el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock" show-password /></el-form-item>
        <el-form-item><el-button type="primary" native-type="submit" :loading="loading" style="width:100%">登 录</el-button></el-form-item>
      </el-form>
      <div class="tips">开发阶段默认账号：admin / t001 / s001，密码均为 123456</div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const form = reactive({ username: '', password: '' })
async function handleLogin() {
  loading.value = true
  try { await userStore.login(form.username, form.password); router.push('/dashboard') }
  catch { ElMessage.error('登录失败') }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-container { height: 100vh; display: flex; align-items: center; justify-content: center; background: #f0f2f5; }
.login-card { width: 400px; }
.login-card h2 { text-align: center; margin-bottom: 24px; color: #303133; }
.tips { text-align: center; color: #999; font-size: 12px; margin-top: 8px; }
</style>

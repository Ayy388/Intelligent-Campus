import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'
import type { User, LoginResponse } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<User | null>(null)
  const role = ref(localStorage.getItem('role') || '')

  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    const data = res.data as LoginResponse
    token.value = data.token
    role.value = data.role
    localStorage.setItem('token', data.token)
    localStorage.setItem('role', data.role)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    if (!token.value) return
    const res = await getCurrentUser()
    userInfo.value = res.data as User
    if (!role.value && res.data.roleName) role.value = res.data.roleName
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    role.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('role')
  }

  return { token, userInfo, role, login, fetchUserInfo, logout }
})

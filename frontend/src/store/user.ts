import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, getCurrentUser } from '@/api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<any>(null)
  const role = ref('')

  async function login(username: string, password: string) {
    const res = await loginApi(username, password)
    token.value = res.data.token
    role.value = res.data.role
    localStorage.setItem('token', res.data.token)
    await fetchUserInfo()
  }

  async function fetchUserInfo() {
    if (!token.value) return
    const res = await getCurrentUser()
    userInfo.value = res.data
    role.value = res.data.roleName || userInfo.value.roleName
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    role.value = ''
    localStorage.removeItem('token')
  }

  return { token, userInfo, role, login, fetchUserInfo, logout }
})

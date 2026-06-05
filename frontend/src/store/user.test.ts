import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/store/user'

// Mock the auth API module
vi.mock('@/api/auth', () => ({
  login: vi.fn(),
  getCurrentUser: vi.fn()
}))

import { login as loginApi, getCurrentUser } from '@/api/auth'

describe('userStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('login should set token and role, then fetch user info', async () => {
    const mockLoginRes = { data: { token: 'test-token', role: 'admin' } } as any
    const mockUserRes = { data: { id: 1, username: 'admin', roleName: '管理员' } } as any
    vi.mocked(loginApi).mockResolvedValue(mockLoginRes)
    vi.mocked(getCurrentUser).mockResolvedValue(mockUserRes)

    const store = useUserStore()
    await store.login('admin', '123456')

    expect(store.token).toBe('test-token')
    expect(store.role).toBe('admin')
    expect(localStorage.getItem('token')).toBe('test-token')
    expect(localStorage.getItem('role')).toBe('admin')
    expect(store.userInfo).toEqual(mockUserRes.data)
    expect(loginApi).toHaveBeenCalledWith('admin', '123456')
  })

  it('logout should clear token and userInfo', () => {
    localStorage.setItem('token', 'test-token')
    localStorage.setItem('role', 'admin')

    const store = useUserStore()
    store.token = 'test-token'
    store.role = 'admin'
    store.userInfo = { id: 1 } as any

    store.logout()

    expect(store.token).toBe('')
    expect(store.role).toBe('')
    expect(store.userInfo).toBeNull()
    expect(localStorage.getItem('token')).toBeNull()
    expect(localStorage.getItem('role')).toBeNull()
  })

  it('fetchUserInfo should set userInfo from API', async () => {
    const mockUserRes = { data: { id: 1, username: 'student', roleName: '学生' } } as any
    vi.mocked(getCurrentUser).mockResolvedValue(mockUserRes)

    const store = useUserStore()
    store.token = 'valid-token'
    await store.fetchUserInfo()

    expect(store.userInfo).toEqual(mockUserRes.data)
  })

  it('fetchUserInfo should do nothing if no token', async () => {
    const store = useUserStore()
    store.token = ''
    await store.fetchUserInfo()

    expect(store.userInfo).toBeNull()
    expect(getCurrentUser).not.toHaveBeenCalled()
  })
})
import { defineStore } from 'pinia';
import { ref } from 'vue';
import { login as loginApi, getCurrentUser } from '@/api/auth';
export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '');
    const userInfo = ref(null);
    const role = ref(localStorage.getItem('role') || '');
    async function login(username, password) {
        const res = await loginApi(username, password);
        const data = res.data;
        token.value = data.token;
        role.value = data.role;
        localStorage.setItem('token', data.token);
        localStorage.setItem('role', data.role);
        await fetchUserInfo();
    }
    async function fetchUserInfo() {
        if (!token.value)
            return;
        const res = await getCurrentUser();
        userInfo.value = res.data;
        if (!role.value && res.data.roleName)
            role.value = res.data.roleName;
    }
    function logout() {
        token.value = '';
        userInfo.value = null;
        role.value = '';
        localStorage.removeItem('token');
        localStorage.removeItem('role');
    }
    return { token, userInfo, role, login, fetchUserInfo, logout };
});

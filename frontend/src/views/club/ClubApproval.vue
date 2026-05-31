<template>
  <div>
    <span class="text-lg font-bold text-ink block mb-4">社团审核管理</span>

    <div class="flex gap-2 mb-5">
      <button v-for="tab in tabs" :key="tab.key" @click="activeTab = tab.key; fetchData()"
        class="px-4 py-1.5 rounded-lg text-sm font-medium border transition-all"
        :class="activeTab === tab.key ? 'bg-ink text-white border-ink' : 'bg-white text-steel border-soft hover:border-line'">
        {{ tab.label }}
      </button>
    </div>

    <div v-if="activeTab === 'club'">
      <div v-if="pendingClubs.length === 0" class="bg-white rounded-xl border border-soft p-10 text-center text-mist text-sm">暂无待审核社团</div>
      <div v-for="c in pendingClubs" :key="c.id"
        class="bg-white rounded-xl border border-soft p-4 mb-3 animate__animated animate__fadeIn">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="text-sm font-semibold text-ink mb-1">{{ c.name }}</div>
            <div class="text-xs text-ash mb-2 line-clamp-2">{{ c.description || '暂无简介' }}</div>
            <div class="flex items-center gap-3 text-xs text-mist">
              <span>创建人: {{ c.presidentName || '未知' }}</span>
              <span>{{ c.createTime?.substring(0, 10) || '' }}</span>
            </div>
          </div>
          <div class="flex gap-2 shrink-0 ml-4">
            <button @click="doApproveClub(c.id, 1)" class="px-3 py-1 text-xs bg-ink text-white rounded-lg hover:bg-steel transition-colors">通过</button>
            <button @click="doApproveClub(c.id, 2)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">拒绝</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'member'">
      <div v-if="pendingMembers.length === 0" class="bg-white rounded-xl border border-soft p-10 text-center text-mist text-sm">暂无待处理申请</div>
      <div v-for="m in pendingMembers" :key="m.id"
        class="bg-white rounded-xl border border-soft p-4 mb-3 animate__animated animate__fadeIn">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="text-sm font-semibold text-ink mb-1">{{ m.clubName }}</div>
            <div class="flex items-center gap-2 text-xs text-mist mb-1">
              <span>申请人: {{ m.userName || '未知' }}</span>
            </div>
            <div class="text-xs text-ash">{{ m.reason || '无申请理由' }}</div>
          </div>
          <div class="flex gap-2 shrink-0 ml-4">
            <button @click="doApproveMember(m.id, 1)" class="px-3 py-1 text-xs bg-ink text-white rounded-lg hover:bg-steel transition-colors">通过</button>
            <button @click="doApproveMember(m.id, 2)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">拒绝</button>
          </div>
        </div>
      </div>
    </div>

    <div v-if="activeTab === 'disband'">
      <div v-if="disbandClubs.length === 0" class="bg-white rounded-xl border border-soft p-10 text-center text-mist text-sm">暂无待审核解散</div>
      <div v-for="c in disbandClubs" :key="c.id"
        class="bg-white rounded-xl border border-soft p-4 mb-3 animate__animated animate__fadeIn">
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="text-sm font-semibold text-ink mb-1">{{ c.name }}</div>
            <div class="text-xs text-mist">申请解散</div>
          </div>
          <div class="flex gap-2 shrink-0 ml-4">
            <button @click="doApproveDisband(c.id, 1)" class="px-3 py-1 text-xs bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors">确认解散</button>
            <button @click="doApproveDisband(c.id, 0)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">拒绝解散</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getClubs, approveClub, getMembers, approveMember, approveDisband } from '@/api/club'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import 'animate.css'

const userStore = useUserStore()

const tabs = [
  { key: 'club', label: '待审核社团' },
  { key: 'member', label: '待处理申请' },
  { key: 'disband', label: '待审核解散' },
]
const activeTab = ref('club')

const pendingClubs = ref<any[]>([])
const disbandClubs = ref<any[]>([])
const pendingMembers = ref<any[]>([])

async function fetchData() {
  if (activeTab.value === 'club') {
    await fetchPendingClubs()
  } else if (activeTab.value === 'member') {
    await fetchPendingMembers()
  } else if (activeTab.value === 'disband') {
    await fetchDisbandClubs()
  }
}

async function fetchPendingClubs() {
  try {
    const r = await getClubs()
    pendingClubs.value = (r.data || []).filter((c: any) => c.status === 0)
  } catch {
    pendingClubs.value = []
  }
}

async function fetchDisbandClubs() {
  try {
    const r = await getClubs()
    disbandClubs.value = (r.data || []).filter((c: any) => c.status === 3)
  } catch {
    disbandClubs.value = []
  }
}

async function fetchPendingMembers() {
  try {
    const r = await getClubs()
    const allClubs = r.data || []
    const result: any[] = []
    for (const c of allClubs) {
      try {
        const mr = await getMembers(c.id)
        const members = mr.data || []
        for (const m of members) {
          if (m.status === 0) {
            result.push({
              id: m.id,
              clubName: c.name,
              userName: m.userName || '未知',
              reason: m.reason || '',
            })
          }
        }
      } catch {
        continue
      }
    }
    pendingMembers.value = result
  } catch {
    pendingMembers.value = []
  }
}

async function doApproveClub(id: number, status: number) {
  try {
    await approveClub(id, status)
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    await fetchPendingClubs()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function doApproveMember(id: number, status: number) {
  try {
    await approveMember(id, status)
    ElMessage.success(status === 1 ? '已通过' : '已拒绝')
    await fetchPendingMembers()
  } catch {
    ElMessage.error('操作失败')
  }
}

async function doApproveDisband(id: number, status: number) {
  try {
    await approveDisband(id, status)
    ElMessage.success(status === 1 ? '已确认解散' : '已拒绝解散')
    await fetchDisbandClubs()
  } catch {
    ElMessage.error('操作失败')
  }
}

onMounted(() => {
  if (userStore.role !== 'admin') {
    ElMessage.warning('仅管理员可访问')
    return
  }
  fetchPendingClubs()
})
</script>
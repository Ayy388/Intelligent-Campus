<template>
  <div class="flex h-[calc(100vh-100px)] bg-white rounded-xl border border-soft overflow-hidden">
    <!-- 左侧群列表 -->
    <div class="w-72 border-r border-soft flex flex-col shrink-0">
      <div class="p-4 border-b border-soft">
        <h3 class="text-sm font-bold text-ink">我的群聊</h3>
      </div>
      <div class="flex-1 overflow-y-auto">
        <div
          v-for="g in groups"
          :key="g.id"
          class="flex items-center gap-3 px-4 py-3.5 cursor-pointer hover:bg-gray-50 transition-colors"
          :class="selectedGroup?.id === g.id ? 'bg-blue-50' : ''"
          @click="selectGroup(g)"
        >
          <div class="w-10 h-10 rounded-full bg-gradient-to-br from-blue-500 to-cyan-500 flex items-center justify-center shrink-0 text-white text-sm font-bold">
            {{ g.name?.charAt(0) }}
          </div>
          <div class="min-w-0 flex-1">
            <div class="flex items-center justify-between">
              <span class="text-sm font-medium text-gray-800 truncate">{{ g.name }}</span>
              <span v-if="g.lastTime" class="text-xs text-gray-400 shrink-0 ml-2">{{ formatTime(g.lastTime) }}</span>
            </div>
            <div class="flex items-center justify-between mt-0.5">
              <span class="text-xs text-gray-400 truncate">{{ g.lastMessage || '暂无消息' }}</span>
              <span v-if="g.unreadCount > 0" class="shrink-0 ml-2 min-w-[18px] h-[18px] rounded-full bg-red-500 text-white text-[10px] flex items-center justify-center px-1">{{ g.unreadCount > 99 ? '99+' : g.unreadCount }}</span>
            </div>
          </div>
        </div>
        <div v-if="groups.length === 0" class="flex flex-col items-center justify-center py-16 text-gray-400">
          <svg class="w-12 h-12 mb-3 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/></svg>
          <span class="text-sm">暂无群聊</span>
        </div>
      </div>
    </div>

    <!-- 右侧聊天区 -->
    <div class="flex-1 flex flex-col">
      <template v-if="selectedGroup">
        <div class="p-4 border-b border-soft bg-gray-50">
          <h3 class="text-sm font-bold text-ink">{{ selectedGroup.name }}</h3>
          <p v-if="selectedGroup.className" class="text-xs text-gray-400 mt-0.5">{{ selectedGroup.className }}</p>
        </div>

        <div ref="msgContainer" class="flex-1 overflow-y-auto p-4 space-y-3 bg-gray-50/50">
          <div v-for="m in messages" :key="m.id" class="flex" :class="m.senderId === userId ? 'justify-end' : 'justify-start'">
            <div class="max-w-[70%]">
              <div v-if="m.senderId !== userId" class="text-xs text-gray-400 mb-1 ml-1">{{ m.senderName }}</div>
              <div
                class="px-3.5 py-2.5 rounded-2xl text-sm leading-relaxed break-words"
                :class="m.senderId === userId ? 'bg-blue-500 text-white rounded-br-md' : 'bg-white text-gray-800 shadow-sm border border-soft rounded-bl-md'"
              >
                {{ m.content }}
              </div>
              <div class="text-[10px] text-gray-400 mt-1" :class="m.senderId === userId ? 'text-right mr-1' : 'ml-1'">{{ formatTime(m.createTime) }}</div>
            </div>
          </div>
          <div v-if="messages.length === 0" class="flex flex-col items-center justify-center py-20 text-gray-400">
            <svg class="w-10 h-10 mb-2 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            <span class="text-sm">暂无消息，发送第一条吧</span>
          </div>
        </div>

        <div class="p-4 border-t border-soft bg-white">
          <div class="flex gap-3 items-end">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              placeholder="输入消息..."
              resize="none"
              @keyup.enter.prevent="doSend"
              :disabled="sending"
            />
            <el-button type="primary" @click="doSend" :loading="sending" class="!rounded-lg !min-w-[70px]">发送</el-button>
          </div>
        </div>
      </template>

      <template v-else>
        <div class="flex-1 flex flex-col items-center justify-center text-gray-400">
          <svg class="w-16 h-16 mb-4 text-gray-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
          <span class="text-sm">选择一个群聊开始聊天</span>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, nextTick } from 'vue'
import { getMyGroups, getGroupMessages, sendGroupMessage, markGroupRead } from '@/api/message'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const userId = computed(() => userStore.userInfo?.id || 0)

const groups = ref<any[]>([])
const selectedGroup = ref<any>(null)
const messages = ref<any[]>([])
const inputText = ref('')
const sending = ref(false)
const msgContainer = ref<HTMLElement | null>(null)

async function fetchGroups() {
  try {
    const r = await getMyGroups()
    groups.value = r.data || []
  } catch { groups.value = [] }
}

async function selectGroup(g: any) {
  selectedGroup.value = g
  messages.value = []
  await loadMessages()
  await markGroupRead(g.id)
  g.unreadCount = 0
  scrollToBottom()
}

async function loadMessages() {
  if (!selectedGroup.value) return
  try {
    const r = await getGroupMessages(selectedGroup.value.id)
    messages.value = (r.data.records || []).reverse()
  } catch {}
}

async function doSend() {
  const text = inputText.value.trim()
  if (!text || !selectedGroup.value) return
  sending.value = true
  try {
    const r = await sendGroupMessage(selectedGroup.value.id, text)
    messages.value.push(r.data)
    selectedGroup.value.lastMessage = text
    selectedGroup.value.lastTime = new Date().toISOString()
    inputText.value = ''
    scrollToBottom()
  } catch { ElMessage.error('发送失败') } finally { sending.value = false }
}

function scrollToBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

function formatTime(t: string) {
  if (!t) return ''
  const d = new Date(t)
  const now = new Date()
  const pad = (n: number) => n.toString().padStart(2, '0')
  if (d.toDateString() === now.toDateString()) return `${pad(d.getHours())}:${pad(d.getMinutes())}`
  return `${d.getMonth() + 1}/${d.getDate()} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

onMounted(fetchGroups)
</script>
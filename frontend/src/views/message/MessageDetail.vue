<template>
  <div class="flex gap-4 h-[calc(100vh-140px)]" v-loading="loading">
    <div class="w-[280px] flex-shrink-0 bg-white rounded-xl border border-soft overflow-hidden flex flex-col animate__animated animate__fadeInUp">
      <div class="p-4 border-b border-wash font-semibold text-ink text-sm">消息</div>
      <div class="flex-1 overflow-y-auto scrollbar-thin">
        <div v-for="c in conversations" :key="c.id"
          @click="selectConversation(c)"
          :class="['px-4 py-3.5 cursor-pointer border-b border-wash transition-colors', selectedId===c.id ? 'bg-wash' : 'hover:bg-cloud']">
          <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-ink truncate">{{ c.peerName }}</span>
            <span v-if="c.unread" class="w-2 h-2 rounded-full bg-red-500 flex-shrink-0 ml-2"></span>
          </div>
          <div class="text-xs text-mist mt-1 truncate">{{ c.lastMessage || '暂无消息' }}</div>
        </div>
      </div>
    </div>
    <template v-if="!selectedId">
      <div class="flex-1 bg-white rounded-xl border border-soft flex flex-col items-center justify-center text-mist animate__animated animate__fadeInUp" style="animation-delay:0.08s">
        <svg class="w-16 h-16 mb-4 text-wash" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
        </svg>
        <span class="text-sm">选择一个会话开始聊天</span>
      </div>
    </template>
    <template v-else>
      <div class="flex-1 bg-white rounded-xl border border-soft flex flex-col animate__animated animate__fadeInUp" style="animation-delay:0.08s">
        <div class="px-5 py-3.5 border-b border-wash font-semibold text-ink text-sm flex items-center gap-2">
          <span>{{ peerName }}</span>
        </div>
        <div ref="messageContainer" class="flex-1 overflow-y-auto px-5 py-4 space-y-3 scrollbar-thin">
          <div v-for="m in messages" :key="m.id" :class="['flex', m.senderId===myUserId ? 'justify-end' : 'justify-start']">
            <div :class="['max-w-[70%] px-4 py-2.5 text-sm leading-relaxed break-words', m.senderId===myUserId ? 'bg-blue-500 text-white rounded-2xl rounded-br-md' : 'bg-wash text-ink rounded-2xl rounded-bl-md']">
              {{ m.content }}
            </div>
          </div>
        </div>
        <div class="p-4 border-t border-wash flex gap-3 items-center">
          <el-input v-model="inputText" placeholder="输入消息..." size="default" @keyup.enter="doSend" class="flex-1" :disabled="sending" />
          <button @click="doSend" :disabled="sending || !inputText.trim()" class="px-6 py-[9px] bg-blue-500 text-white rounded-lg text-sm font-medium hover:bg-blue-600 transition-colors flex-shrink-0 disabled:opacity-50 disabled:cursor-not-allowed">
            发送
          </button>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, watch } from 'vue'
import { getConversations, getConversationMessages, sendMessage } from '@/api/message'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const myUserId = ref(userStore.userInfo?.id)
const conversations = ref<any[]>([])
const messages = ref<any[]>([])
const selectedId = ref<number | null>(null)
const peerName = ref('')
const inputText = ref('')
const sending = ref(false)
const loading = ref(false)
const messageContainer = ref<HTMLElement | null>(null)

async function fetchConvs() {
  loading.value = true
  try {
    const r = await getConversations({ page: 1, size: 50 })
    conversations.value = r.data.records
  } finally {
    loading.value = false
  }
}

async function selectConversation(c: any) {
  loading.value = true
  try {
    selectedId.value = c.id
    peerName.value = c.peerName
    const r = await getConversationMessages(c.id)
    messages.value = r.data
  } finally {
    loading.value = false
  }
}

async function doSend() {
  if (!inputText.value.trim() || sending.value) return
  sending.value = true
  try {
    await sendMessage(0, inputText.value, selectedId.value!)
    inputText.value = ''
    const r = await getConversationMessages(selectedId.value!)
    messages.value = r.data
    fetchConvs()
  } catch {
    ElMessage.error('发送失败')
  } finally {
    sending.value = false
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

watch(selectedId, () => {
  scrollToBottom()
})

watch(messages, () => {
  scrollToBottom()
}, { deep: true })

onMounted(fetchConvs)
</script>

<style scoped>
.scrollbar-thin::-webkit-scrollbar {
  width: 4px;
}
.scrollbar-thin::-webkit-scrollbar-track {
  background: transparent;
}
.scrollbar-thin::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 4px;
}
.scrollbar-thin::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>
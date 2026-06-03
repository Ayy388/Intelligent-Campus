<template>
  <div class="flex gap-4 h-[calc(100vh-180px)]">
    <div class="w-72 flex-shrink-0 bg-white rounded-xl border border-soft overflow-hidden flex flex-col" v-loading="loading">
      <div class="p-4 border-b border-wash font-semibold text-ink text-sm">会话列表</div>
      <div class="flex-1 overflow-y-auto">
        <div v-for="c in conversations" :key="c.id" @click="selectConversation(c)"
          :class="['px-4 py-3 cursor-pointer border-b border-wash transition-colors', selectedId===c.id ? 'bg-wash' : 'hover:bg-cloud']">
          <div class="text-sm font-medium text-ink">{{ c.peerName }}</div>
          <div class="text-xs text-mist mt-0.5 truncate">{{ c.lastMessage || '暂无消息' }}</div>
        </div>
      </div>
    </div>
    <div class="flex-1 bg-white rounded-xl border border-soft flex flex-col">
      <div class="p-4 border-b border-wash font-semibold text-ink text-sm" v-if="peerName">{{ peerName }}</div>
      <div class="flex-1 overflow-y-auto p-4 space-y-3" v-if="selectedId">
        <div v-for="m in messages" :key="m.id" :class="['flex', m.senderId===myUserId ? 'justify-end' : 'justify-start']">
          <div :class="['max-w-[60%] px-3 py-2 rounded-xl text-sm', m.senderId===myUserId ? 'bg-ink text-white rounded-br-sm' : 'bg-wash text-ink rounded-bl-sm']">{{ m.content }}</div>
        </div>
      </div>
      <div class="flex-1 flex items-center justify-center text-mist text-sm" v-else>选择一个会话开始聊天</div>
      <div class="p-3 border-t border-wash flex gap-2" v-if="selectedId || newPeerId">
        <el-input v-if="!selectedId" v-model="newPeerId" placeholder="对方ID" class="!w-24" size="small" />
        <el-input v-model="inputText" placeholder="输入消息..." size="small" @keyup.enter="doSend" />
        <button @click="doSend" class="px-4 py-1 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors flex-shrink-0">发送</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
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
const newPeerId = ref('')
const loading = ref(false)
async function fetchConvs() { loading.value = true; try { const r = await getConversations({ page: 1, size: 50 }); conversations.value = r.data.records } finally { loading.value = false } }
async function selectConversation(c: any) { loading.value = true; try { selectedId.value = c.id; peerName.value = c.peerName; const r = await getConversationMessages(c.id); messages.value = r.data } finally { loading.value = false } }
async function doSend() {
  if (!inputText.value.trim()) return
  const peerId = selectedId.value ? null : Number(newPeerId.value)
  const convId = selectedId.value || undefined
  try { await sendMessage(peerId || 0, inputText.value, convId); inputText.value = ''; if (selectedId.value) { const r = await getConversationMessages(selectedId.value); messages.value = r.data } fetchConvs() }
  catch { ElMessage.error('发送失败') }
}
onMounted(fetchConvs)
</script>

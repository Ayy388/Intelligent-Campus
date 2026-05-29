<template>
  <div class="chat-container">
    <el-card class="chat-card">
      <template #header>
        <div class="chat-header">
          <span>🤖 AI 校园助手</span>
          <div>
            <el-button size="small" @click="newChat">新对话</el-button>
            <el-popover placement="bottom" :width="300">
              <template #reference><el-button size="small">历史</el-button></template>
              <div v-for="c in conversations" :key="c.id" style="padding:8px 0;cursor:pointer;border-bottom:1px solid #eee" @click="loadConversation(c)">
                {{ c.title }} <span style="color:#999;font-size:12px">{{ c.updateTime?.substring(0,10) }}</span>
              </div>
            </el-popover>
          </div>
        </div>
      </template>
      <div class="messages" ref="msgContainer">
        <div v-for="(msg,i) in messages" :key="i" :class="['message', msg.role]">
          <div class="msg-content">{{ msg.content }}</div>
        </div>
        <div v-if="streaming" class="message assistant"><div class="msg-content">{{ streamingText }}</div></div>
      </div>
      <div class="quick-questions">
        <el-tag v-for="q in quickQuestions" :key="q" @click="send(q)" style="cursor:pointer;margin:2px">{{ q }}</el-tag>
      </div>
      <div class="input-area">
        <el-input v-model="input" placeholder="输入你的问题..." @keyup.enter="send(input)" /><el-button type="primary" @click="send(input)" :disabled="streaming">发送</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { getConversations, getMessages, deleteConversation } from '@/api/ai'
import { useUserStore } from '@/store/user'
const userStore = useUserStore()
const messages = ref<Array<{role:string,content:string}>>([])
const input = ref('')
const streaming = ref(false)
const streamingText = ref('')
const conversations = ref<any[]>([])
const conversationId = ref<number | null>(null)
const msgContainer = ref<HTMLElement>()
const quickQuestions = ['校历查询', '图书馆开放时间', '校园卡充值流程', '请假流程']

async function send(text: string) {
  if (!text.trim() || streaming.value) return
  messages.value.push({ role: 'user', content: text })
  streaming.value = true
  streamingText.value = ''
  const token = localStorage.getItem('token')
  const params = new URLSearchParams({ question: text })
  if (conversationId.value) params.append('conversationId', String(conversationId.value))
  const response = await fetch(`/api/ai/chat?${params}`, { method: 'POST', headers: { Authorization: `Bearer ${token}` } })
  const reader = response.body?.getReader()
  const decoder = new TextDecoder()
  if (!reader) return
  let buffer = ''
  while (true) {
    const { done, value } = await reader.read()
    if (done) break
    buffer += decoder.decode(value, { stream: true })
    const lines = buffer.split('\n')
    buffer = lines.pop() || ''
    for (const line of lines) {
      if (line.startsWith('data:')) {
        const data = line.substring(5).trim()
        if (data) streamingText.value += data
      } else if (line.startsWith('event:done')) {
        const idLine = lines.find(l => l.startsWith('data:'))
        if (idLine) conversationId.value = Number(idLine.substring(5).trim())
      }
    }
  }
  messages.value.push({ role: 'assistant', content: streamingText.value })
  streaming.value = false
  streamingText.value = ''
  scrollBottom()
  loadConversations()
}

function newChat() { messages.value = []; conversationId.value = null }
async function loadConversation(c: any) { conversationId.value = c.id; const r = await getMessages(c.id); messages.value = r.data }
async function loadConversations() { const r = await getConversations(); conversations.value = r.data }
function scrollBottom() { nextTick(() => { if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight }) }
onMounted(loadConversations)
</script>

<style scoped>
.chat-container { height: calc(100vh - 140px); }
.chat-card { height: 100%; display: flex; flex-direction: column; }
.chat-header { display: flex; justify-content: space-between; align-items: center; }
.messages { flex: 1; overflow-y: auto; padding: 8px 0; }
.message { margin-bottom: 12px; }
.message.user { text-align: right; }
.message.user .msg-content { background: #409eff; color: #fff; display: inline-block; padding: 8px 14px; border-radius: 12px 12px 0 12px; max-width: 70%; }
.message.assistant .msg-content { background: #f0f0f0; display: inline-block; padding: 8px 14px; border-radius: 12px 12px 12px 0; max-width: 70%; }
.quick-questions { padding: 8px 0; }
.input-area { display: flex; gap: 8px; }
</style>

<template>
  <div class="chat-page">
    <div class="chat-header">
      <span class="header-title">AI 校园助手</span>
      <div class="header-actions">
        <button class="header-btn" @click="newChat">新对话</button>
        <div class="history-wrapper">
          <button class="header-btn" @click="showHistory = !showHistory">历史</button>
          <div v-if="showHistory" class="history-dropdown">
            <div v-for="c in conversations" :key="c.id" class="history-item" @click="loadConversation(c); showHistory = false">
              <span class="history-title">{{ c.title }}</span>
              <span class="history-time">{{ c.updateTime?.substring(0, 10) }}</span>
            </div>
            <div v-if="conversations.length === 0" class="history-empty">暂无历史记录</div>
          </div>
        </div>
      </div>
    </div>
    <div class="messages-area" ref="msgContainer">
      <div v-for="(msg, i) in messages" :key="i" class="message-row" :class="msg.role">
        <div class="message-bubble">{{ msg.content }}</div>
      </div>
      <div v-if="streaming" class="message-row assistant">
        <div class="message-bubble streaming">{{ streamingText }}<span class="typing-cursor">|</span></div>
      </div>
      <div v-if="messages.length === 0 && !streaming" class="empty-state">
        <div class="empty-icon">💬</div>
        <p class="empty-text">有什么我可以帮你的吗？</p>
      </div>
    </div>
    <div class="input-section">
      <div class="quick-questions">
        <button v-for="q in quickQuestions" :key="q" class="quick-tag" @click="send(q)">{{ q }}</button>
      </div>
      <div class="input-bar">
        <input v-model="input" class="chat-input" placeholder="输入你的问题..." @keyup.enter="send(input)" :disabled="streaming" />
        <button class="send-btn" @click="send(input)" :disabled="streaming || !input.trim()">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>
        </button>
      </div>
    </div>
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
const showHistory = ref(false)
const msgContainer = ref<HTMLElement>()
const quickQuestions = ['校历查询', '图书馆开放时间', '校园卡充值流程', '请假流程']

async function send(text: string) {
  if (!text.trim() || streaming.value) return
  messages.value.push({ role: 'user', content: text })
  input.value = ''
  streaming.value = true
  streamingText.value = ''
  scrollBottom()
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
    scrollBottom()
  }
  messages.value.push({ role: 'assistant', content: streamingText.value })
  streaming.value = false
  streamingText.value = ''
  scrollBottom()
  loadConversations()
}

function newChat() { messages.value = []; conversationId.value = null; showHistory.value = false }
async function loadConversation(c: any) { conversationId.value = c.id; const r = await getMessages(c.id); messages.value = r.data; scrollBottom() }
async function loadConversations() { const r = await getConversations(); conversations.value = r.data }
function scrollBottom() { nextTick(() => { if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight }) }
onMounted(loadConversations)
</script>

<style scoped>
.chat-page {
  height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 0;
  overflow: hidden;
}

.chat-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
  flex-shrink: 0;
}

.header-title {
  font-size: 17px;
  font-weight: 600;
  color: #1a1a1a;
  letter-spacing: 0.3px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-btn {
  padding: 6px 16px;
  border: 1px solid #e5e5e5;
  border-radius: 8px;
  background: #fff;
  color: #333;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.header-btn:hover {
  background: #f5f5f5;
  border-color: #d9d9d9;
}

.history-wrapper {
  position: relative;
}

.history-dropdown {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  width: 280px;
  max-height: 360px;
  overflow-y: auto;
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
  z-index: 100;
  padding: 6px;
}

.history-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s;
}

.history-item:hover {
  background: #f7f7f8;
}

.history-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
  margin-right: 8px;
}

.history-time {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.history-empty {
  padding: 24px 12px;
  text-align: center;
  color: #bbb;
  font-size: 13px;
}

.messages-area {
  flex: 1;
  overflow-y: auto;
  padding: 24px 0;
  scroll-behavior: smooth;
}

.messages-area::-webkit-scrollbar {
  width: 6px;
}

.messages-area::-webkit-scrollbar-thumb {
  background: #e5e5e5;
  border-radius: 3px;
}

.messages-area::-webkit-scrollbar-track {
  background: transparent;
}

.message-row {
  display: flex;
  padding: 0 80px;
  margin-bottom: 16px;
}

.message-row.user {
  justify-content: flex-end;
}

.message-row.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 65%;
  padding: 12px 18px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-row.user .message-bubble {
  background: #1a73e8;
  color: #fff;
  border-radius: 18px 18px 4px 18px;
  box-shadow: 0 1px 3px rgba(26, 115, 232, 0.15);
}

.message-row.assistant .message-bubble {
  background: #f0f2f5;
  color: #1a1a1a;
  border-radius: 18px 18px 18px 4px;
}

.typing-cursor {
  display: inline-block;
  margin-left: 2px;
  font-weight: 300;
  color: #666;
  animation: blink 0.8s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0; }
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 300px;
  color: #bbb;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
  opacity: 0.6;
}

.empty-text {
  font-size: 15px;
  color: #bbb;
  margin: 0;
}

.input-section {
  flex-shrink: 0;
  padding: 0 80px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fff;
}

.quick-questions {
  display: flex;
  gap: 8px;
  padding: 12px 0;
  overflow-x: auto;
  flex-wrap: nowrap;
}

.quick-questions::-webkit-scrollbar {
  display: none;
}

.quick-tag {
  flex-shrink: 0;
  padding: 6px 14px;
  border: 1px solid #e8e8e8;
  border-radius: 20px;
  background: #fff;
  color: #555;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.quick-tag:hover {
  border-color: #1a73e8;
  color: #1a73e8;
  background: #f5f8ff;
}

.input-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
  background: #fff;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.input-bar:focus-within {
  border-color: #1a73e8;
  box-shadow: 0 0 0 2px rgba(26, 115, 232, 0.08);
}

.chat-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 14px;
  color: #1a1a1a;
  background: transparent;
  line-height: 1.5;
  padding: 4px 0;
}

.chat-input::placeholder {
  color: #bbb;
}

.send-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: #1a73e8;
  color: #fff;
  cursor: pointer;
  transition: all 0.2s;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  background: #1557b0;
}

.send-btn:disabled {
  background: #e5e5e5;
  color: #bbb;
  cursor: not-allowed;
}
</style>
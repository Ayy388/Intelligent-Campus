<template>
  <div>
    <div class="bg-white rounded-xl border border-soft p-5 mb-4">
      <div class="font-semibold text-ink text-sm mb-3">发布推送</div>
      <div class="flex gap-3 items-end flex-wrap">
        <el-input v-model="form.title" placeholder="推送标题" size="small" class="!w-48" />
        <el-input v-model="form.content" placeholder="推送内容" size="small" class="!w-64" />
        <el-select v-model="form.targetType" size="small" class="!w-28"><el-option label="全体" value="all" /><el-option label="学生" value="student" /><el-option label="教师" value="teacher" /><el-option label="辅导员" value="counselor" /></el-select>
        <button @click="doPush" class="px-5 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">发送</button>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div class="font-semibold text-ink text-sm mb-3">推送记录</div>
      <div v-for="a in announcements" :key="a.id" class="flex justify-between py-2 border-b border-wash last:border-0 text-sm">
        <div><span class="font-medium text-ink">{{ a.title }}</span><span class="text-mist ml-2">{{ a.content?.substring(0,40) }}</span></div>
        <div class="text-xs text-mist">{{ a.sendTime?.substring(0,16) }} · {{ a.targetType==='all'?'全体':a.targetType==='student'?'学生':a.targetType==='teacher'?'教师':'辅导员' }}</div>
      </div>
      <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" small />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getAnnouncements, pushAnnouncement } from '@/api/message'
import { ElMessage } from 'element-plus'
const announcements = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const form = reactive({ title: '', content: '', targetType: 'all' })
async function fetch() { const r = await getAnnouncements({ page: page.value, size: 10 }); announcements.value = r.data.records; total.value = r.data.total }
async function doPush() { if (!form.title) { ElMessage.warning('请输入标题'); return }; await pushAnnouncement(form); ElMessage.success('发送成功'); form.title=''; form.content=''; fetch() }
onMounted(fetch)
</script>

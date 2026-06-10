<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>通知公告</span>
      <div class="flex gap-3 items-center">
        <el-select v-model="filterCategory" size="small" placeholder="全部分类" clearable class="!w-28" @change="page=1;fetch()">
          <el-option label="通用" value="general" /><el-option label="教务" value="edu" /><el-option label="行政" value="admin" />
        </el-select>
        <el-button type="primary" @click="dialogVisible=true" v-if="userStore.role!=='student'">发布公告</el-button>
      </div>
    </div></template>
    <el-table :data="tableData" v-loading="loading" @row-click="viewDetail">
      <el-table-column label="标题" min-width="180">
        <template #default="{row}">
          <div class="flex items-center gap-2">
            <span v-if="row.read === false" class="w-2 h-2 rounded-full bg-blue-500 shrink-0"></span>
            <span :class="row.read === false ? 'font-semibold text-gray-800' : 'text-gray-500'">
              <el-tag v-if="row.isTop" size="small" type="danger" style="margin-right:4px">置顶</el-tag>{{ row.title }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="推送范围" width="100" align="center">
        <template #default="{row}">
          <el-tag size="small" :type="row.targetType === 'all' ? '' : row.targetType === 'student' ? 'primary' : row.targetType === 'teacher' ? 'warning' : 'success'">
            {{ row.targetType==='all'?'全体':row.targetType==='student'?'学生':row.targetType==='teacher'?'教师':'辅导员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="80" />
      <el-table-column label="附件" width="70" align="center">
        <template #default="{row}">
          <el-link v-if="row.attachment" :href="row.attachment" target="_blank" :underline="false" @click.stop>
            <el-icon><Paperclip /></el-icon>
          </el-link>
          <span v-else class="text-gray-300 text-xs">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column label="时间" width="170">
        <template #default="{row}">{{ row.createTime?.substring(0,16) || '' }}</template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" title="发布公告" width="600px">
      <el-form :model="form">
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category"><el-option label="通用" value="general" /><el-option label="教务" value="edu" /><el-option label="行政" value="admin" /></el-select></el-form-item>
        <el-form-item label="推送范围">
          <el-select v-model="form.targetType">
            <el-option label="全体" value="all" /><el-option label="学生" value="student" />
            <el-option label="教师" value="teacher" /><el-option label="辅导员" value="counselor" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
        <el-form-item label="附件">
          <div class="flex items-center gap-2">
            <el-upload
              ref="uploadRef"
              action="/api/upload/file"
              :headers="uploadHeaders"
              :show-file-list="false"
              :on-success="handleUploadSuccess"
              :on-error="handleUploadError"
              :before-upload="beforeUpload"
              accept=".pdf,.doc,.docx,.xls,.xlsx,.ppt,.pptx,.txt,.zip,.rar,.jpg,.jpeg,.png,.gif"
            >
              <el-button size="small">选择文件</el-button>
            </el-upload>
            <span v-if="uploadedFile" class="text-xs text-gray-500 flex items-center gap-1">
              <el-icon><Document /></el-icon>{{ uploadedFile.name }}
              <el-button type="danger" size="small" text @click="removeFile">移除</el-button>
            </span>
          </div>
        </el-form-item>
        <el-form-item><el-checkbox v-model="form.isTop">置顶</el-checkbox><el-checkbox v-model="form.isUrgent" class="ml-4">紧急</el-checkbox></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">发布</el-button></template>
    </el-dialog>
    <el-dialog v-model="detailVisible" title="通知详情" width="600px">
      <div v-if="detailItem" class="space-y-4">
        <div class="flex items-center gap-2">
          <h3 class="text-lg font-bold text-gray-800">{{ detailItem.title }}</h3>
          <el-tag v-if="detailItem.isUrgent" size="small" type="danger">紧急</el-tag>
          <el-tag v-if="detailItem.isTop" size="small">置顶</el-tag>
        </div>
        <div class="text-sm text-gray-500 flex items-center gap-4">
          <span>发布人：{{ detailItem.publisherName }}</span>
          <span>分类：{{ detailItem.category }}</span>
          <span>时间：{{ detailItem.createTime }}</span>
          <span v-if="detailItem.attachment">
            <el-link :href="detailItem.attachment" target="_blank" :underline="false"><el-icon><Paperclip /></el-icon>查看附件</el-link>
          </span>
        </div>
        <el-divider />
        <div class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{{ detailItem.content }}</div>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getNotifications, getNotification, addNotification } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Paperclip, Document } from '@element-plus/icons-vue'
import type { Notification } from '@/types'

const userStore = useUserStore()
const tableData = ref<Notification[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const filterCategory = ref('')
const detailVisible = ref(false)
const detailItem = ref<Notification | null>(null)
const form = reactive({ title: '', content: '', category: 'general', targetType: 'all', isTop: false, isUrgent: false, attachment: '' })
const uploadRef = ref()
const uploadedFile = ref<{ name: string; url: string } | null>(null)
const uploadHeaders = computed(() => ({ Authorization: `Bearer ${userStore.token}` }))

async function fetch() {
  loading.value = true
  try {
    const r = await getNotifications({ page: page.value, size: 10, category: filterCategory.value || undefined })
    tableData.value = r.data.records
    total.value = r.data.total
  } finally { loading.value = false }
}

async function viewDetail(row: Notification) {
  try {
    const r = await getNotification(row.id)
    detailItem.value = r.data
    detailVisible.value = true
  } catch { ElMessage.error('获取详情失败') }
}

async function submit() {
  const data: Record<string, any> = {
    ...form,
    isTop: form.isTop ? 1 : 0,
    isUrgent: form.isUrgent ? 1 : 0,
    attachment: uploadedFile.value?.url || ''
  }
  await addNotification(data)
  ElMessage.success('发布成功')
  dialogVisible.value = false
  fetch()
  Object.assign(form, { title: '', content: '', category: 'general', targetType: 'all', isTop: false, isUrgent: false, attachment: '' })
  uploadedFile.value = null
}

function beforeUpload(file: File) {
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) { ElMessage.warning('文件大小不能超过 20MB'); return false }
  return true
}

function handleUploadSuccess(response: any, _file: any, _files: any) {
  if (response.code === 200) {
    uploadedFile.value = { name: _file.name, url: response.data }
    ElMessage.success('文件上传成功')
  } else { ElMessage.error(response.message || '上传失败') }
}

function handleUploadError() { ElMessage.error('文件上传失败，请重试') }

function removeFile() { uploadedFile.value = null }

onMounted(fetch)
</script>
<template>
  <div v-loading="loading">
    <div class="flex items-center justify-between mb-4">
      <div class="flex gap-2">
        <button v-for="t in types" :key="t.value" @click="curType = t.value; page=1; fetch()"
          :class="['px-4 py-1.5 rounded-lg text-sm font-medium border transition-all', curType===t.value ? 'bg-ink text-white border-ink' : 'bg-white text-ash border-soft hover:border-line']">{{ t.label }}</button>
      </div>
      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索..." size="small" clearable class="!w-48" @change="page=1;fetch()" />
        <button @click="showCreateDialog" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel">发布信息</button>
      </div>
    </div>

    <div class="space-y-3">
      <div v-for="item in items" :key="item.id"
        class="bg-white rounded-xl border border-soft p-4 hover:shadow-sm transition-shadow cursor-pointer"
        @click="showDetail(item)">
        <div class="flex gap-4">
          <div v-if="item.images" class="shrink-0">
            <el-image :src="item.images.split(',')[0]" class="w-20 h-20 rounded-lg object-cover" />
          </div>
          <div v-else class="w-20 h-20 rounded-lg bg-wash flex items-center justify-center shrink-0 text-mist text-xs">暂无图片</div>
          <div class="flex-1 min-w-0">
            <div class="flex items-center gap-2 mb-1">
              <el-tag size="small" class="!border-0" :type="item.type===0?'':''" :class="item.type===0?'!bg-blue-50 !text-blue-500':'!bg-orange-50 !text-orange-500'">{{ item.type===0?'寻物':'招领' }}</el-tag>
              <span class="text-sm font-medium text-ink truncate">{{ item.title }}</span>
            </div>
            <div class="text-xs text-ash mt-1 line-clamp-2">{{ item.description }}</div>
            <div class="flex gap-4 mt-2 text-xs text-mist">
              <span>{{ item.location }}</span>
              <span>{{ item.contact }}</span>
              <span v-if="item.userName">{{ item.userName }}</span>
            </div>
          </div>
          <div class="shrink-0 self-start">
            <button v-if="item.status===0" @click.stop="markDone(item.id)" class="text-xs text-ash hover:text-ink transition-colors px-2 py-1">标记已解决</button>
            <el-tag v-else size="small" type="info" class="!bg-wash !text-mist !border-soft">已解决</el-tag>
          </div>
        </div>
      </div>
      <div v-if="!items.length" class="text-center text-mist py-12 text-sm">暂无数据</div>
    </div>

    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" class="mt-4 !justify-center" />

    <el-dialog v-model="createVisible" title="发布信息" width="520px" :close-on-click-modal="false">
      <el-form :model="form" label-position="top">
        <el-form-item label="类型"><el-radio-group v-model="form.type"><el-radio :value="0">寻物</el-radio><el-radio :value="1">招领</el-radio></el-radio-group></el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="请填写标题" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" placeholder="请填写详细描述" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" placeholder="丢失/拾取地点" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" placeholder="手机号 / QQ / 微信" /></el-form-item>
        <el-form-item label="图片">
          <el-upload
            ref="uploadRef"
            action="/api/upload/image"
            :headers="uploadHeaders"
            list-type="picture-card"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :before-upload="beforeUpload"
            multiple
            :limit="6"
          >
            <span class="text-xs text-mist">+ 上传</span>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible=false">取消</el-button>
        <el-button type="primary" @click="submit">发布</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" :title="detailItem?.title" width="600px" top="5vh">
      <div v-if="detailItem">
        <div class="flex items-center gap-2 mb-3">
          <el-tag size="small" class="!border-0" :type="detailItem.type===0?'':''"
            :class="detailItem.type===0?'!bg-blue-50 !text-blue-500':'!bg-orange-50 !text-orange-500'">
            {{ detailItem.type===0?'寻物':'招领' }}
          </el-tag>
          <el-tag v-if="detailItem.status===1" size="small" type="info" class="!bg-wash !text-mist !border-soft">已解决</el-tag>
        </div>
        <div class="text-xs text-mist mb-4 flex gap-3">
          <span v-if="detailItem.userName">发布者：{{ detailItem.userName }}</span>
          <span>{{ detailItem.createTime }}</span>
        </div>

        <div v-if="detailItem.images" class="mb-4 grid gap-2" :class="imageCount > 1 ? 'grid-cols-2' : 'grid-cols-1'">
          <div v-for="(url, i) in detailItem.images.split(',')" :key="i" class="rounded-lg overflow-hidden bg-wash flex items-center justify-center" style="min-height:200px">
            <el-image :src="url" fit="contain" class="w-full"
              :zoom-rate="1.2" :initial-index="i"
              :preview-src-list="detailItem.images.split(',')"
            />
          </div>
        </div>

        <div class="text-sm text-ink mb-4 whitespace-pre-wrap leading-relaxed">{{ detailItem.description }}</div>

        <div class="flex flex-wrap gap-x-6 gap-y-1 text-xs text-mist bg-wash rounded-lg px-4 py-3">
          <span>地点：{{ detailItem.location }}</span>
          <span>联系方式：{{ detailItem.contact }}</span>
        </div>

        <div class="flex gap-2 mt-6 pt-4 border-t border-soft">
          <button v-if="detailItem.status===0" @click="markDone(detailItem.id)"
            class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">标记已解决</button>
          <button v-if="isOwner" @click="handleDelete(detailItem.id)"
            class="px-4 py-1.5 text-red-400 rounded-lg text-xs font-medium hover:bg-red-50 transition-colors">删除</button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { getLostFound, addLostFound, updateLostFoundStatus, deleteLostFound } from '@/api/life'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const uploadHeaders = { Authorization: `Bearer ${userStore.token}` }

const items = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const curType = ref<number | undefined>(undefined)
const keyword = ref('')
const types = [{ label: '全部', value: undefined as number | undefined }, { label: '寻物', value: 0 }, { label: '招领', value: 1 }]

const createVisible = ref(false)
const form = reactive({ type: 0, title: '', description: '', location: '', contact: '' })
const imageUrls = ref<string[]>([])
const uploadRef = ref<any>()

const detailVisible = ref(false)
const detailItem = ref<any>(null)
const isOwner = computed(() => detailItem.value && userStore.userInfo?.id === detailItem.value.userId)
const imageCount = computed(() => detailItem.value?.images ? detailItem.value.images.split(',').length : 0)
const loading = ref(false)
const submitting = ref(false)

async function fetch() {
  loading.value = true
  try {
    const r = await getLostFound({ type: curType.value, keyword: keyword.value, page: page.value, size: 10 })
    items.value = r.data.records
    total.value = r.data.total
  } finally {
    loading.value = false
  }
}

function showCreateDialog() {
  form.type = 0
  form.title = ''
  form.description = ''
  form.location = ''
  form.contact = ''
  imageUrls.value = []
  uploadRef.value?.clearFiles()
  createVisible.value = true
}

function handleUploadSuccess(response: any, file: any) {
  const url = response?.data
  if (url) {
    imageUrls.value.push(url)
    file.url = url
  } else {
    console.warn('上传响应异常:', response)
    ElMessage.warning('图片上传响应异常，请重试')
  }
}

function handleUploadError(err: any) {
  console.error('上传失败:', err)
  ElMessage.error('图片上传失败，请检查网络后重试')
}

function beforeUpload(file: File) {
  const isImg = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImg) { ElMessage.warning('只能上传图片文件'); return false }
  if (!isLt5M) { ElMessage.warning('图片大小不能超过 5MB'); return false }
  return true
}

function handleUploadRemove(file: any) {
  const url = file.url || file.response?.data
  if (url) imageUrls.value = imageUrls.value.filter(u => u !== url)
}

async function submit() {
  submitting.value = true
  try {
    const data = { ...form, images: imageUrls.value.join(',') }
    await addLostFound(data)
    ElMessage.success('发布成功')
    createVisible.value = false
    page.value = 1
    fetch()
  } finally {
    submitting.value = false
  }
}

async function markDone(id: number) {
  submitting.value = true
  try {
    await updateLostFoundStatus(id, 1)
    ElMessage.success('已标记为已解决')
    if (detailVisible.value) detailVisible.value = false
    fetch()
  } finally {
    submitting.value = false
  }
}

function showDetail(item: any) {
  detailItem.value = item
  detailVisible.value = true
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这条记录吗？', '确认删除')
    submitting.value = true
    try {
      await deleteLostFound(id)
      ElMessage.success('删除成功')
      detailVisible.value = false
      fetch()
    } finally {
      submitting.value = false
    }
  } catch { /* cancelled */ }
}

onMounted(fetch)
</script>

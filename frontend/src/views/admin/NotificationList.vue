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
      <el-table-column label="标题" min-width="200">
        <template #default="{row}">
          <div class="flex items-center gap-2">
            <span v-if="row.read === false" class="w-2 h-2 rounded-full bg-blue-500 shrink-0"></span>
            <span :class="row.read === false ? 'font-semibold text-gray-800' : 'text-gray-500'">
              <el-tag v-if="row.isTop" size="small" type="danger" style="margin-right:4px">置顶</el-tag>{{ row.title }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="publisherName" label="发布人" width="100" />
      <el-table-column prop="createTime" label="时间" width="180" />
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" title="发布公告" width="600px">
      <el-form :model="form"><el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="分类"><el-select v-model="form.category"><el-option label="通用" value="general" /><el-option label="教务" value="edu" /><el-option label="行政" value="admin" /></el-select></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="6" /></el-form-item>
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
        </div>
        <el-divider />
        <div class="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">{{ detailItem.content }}</div>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getNotifications, getNotification, addNotification } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
const userStore = useUserStore()
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const filterCategory = ref('')
const detailVisible = ref(false)
const detailItem = ref<any>(null)
const form = reactive({ title: '', content: '', category: 'general', isTop: false, isUrgent: false })
async function fetch() { loading.value = true; const r = await getNotifications({ page: page.value, size: 10, category: filterCategory.value || undefined }); tableData.value = r.data.records; total.value = r.data.total; loading.value = false }
async function viewDetail(row: any) {
  try {
    const r = await getNotification(row.id)
    detailItem.value = r.data
    detailVisible.value = true
  } catch { ElMessage.error('获取详情失败') }
}
async function submit() { await addNotification({...form, isTop: form.isTop ? 1 : 0, isUrgent: form.isUrgent ? 1 : 0}); ElMessage.success('发布成功'); dialogVisible.value = false; fetch(); Object.assign(form, { title: '', content: '', category: 'general', isTop: false, isUrgent: false }) }
onMounted(fetch)
</script>

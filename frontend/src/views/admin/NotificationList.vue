<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>通知公告</span><el-button type="primary" @click="dialogVisible=true" v-if="userStore.role!=='student'">发布公告</el-button></div></template>
    <el-table :data="tableData" v-loading="loading" @row-click="viewDetail">
      <el-table-column prop="title" label="标题">
        <template #default="{row}"><el-tag v-if="row.isTop" size="small" type="danger" style="margin-right:4px">置顶</el-tag>{{ row.title }}</template>
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
        <el-form-item><el-checkbox v-model="form.isTop">置顶</el-checkbox></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">发布</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getNotifications, addNotification } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
const userStore = useUserStore()
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ title: '', content: '', category: 'general', isTop: false })
async function fetch() { loading.value = true; const r = await getNotifications({ page: page.value, size: 10 }); tableData.value = r.data.records; total.value = r.data.total; loading.value = false }
function viewDetail(row: any) { ElMessage.info(row.title) }
async function submit() { await addNotification({...form, isTop: form.isTop ? 1 : 0}); ElMessage.success('发布成功'); dialogVisible.value = false; fetch(); Object.assign(form, { title: '', content: '', category: 'general', isTop: false }) }
onMounted(fetch)
</script>

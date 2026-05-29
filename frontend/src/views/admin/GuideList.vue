<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <span class="text-lg font-bold text-ink">办事指南</span>
      <button v-if="userStore.role==='admin'" @click="showAdd" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">添加指南</button>
    </div>
    <div v-if="guides.length===0" class="text-center text-mist py-20">暂无办事指南</div>
    <div v-for="g in guides" :key="g.id" class="bg-white rounded-xl border border-soft p-4 mb-3 hover:shadow-sm transition-shadow">
      <div class="flex items-start justify-between">
        <div class="flex-1">
          <div class="flex items-center gap-2 mb-1">
            <span class="text-sm font-semibold text-ink">{{ g.title }}</span>
            <el-tag size="small">{{ g.category || '通用' }}</el-tag>
          </div>
          <div class="text-xs text-ash whitespace-pre-wrap">{{ g.content?.substring(0,200) }}</div>
        </div>
        <div v-if="userStore.role==='admin'" class="flex gap-2 shrink-0 ml-4">
          <button @click="showEdit(g)" class="px-3 py-1 text-xs border border-ink text-ink rounded-lg hover:bg-ink hover:text-white transition-colors">编辑</button>
          <button @click="doDelete(g.id)" class="px-3 py-1 text-xs border border-soft text-ash rounded-lg hover:bg-wash transition-colors">删除</button>
        </div>
      </div>
    </div>
    <el-pagination v-if="total>size" v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" small @current-change="fetch" />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑指南' : '添加指南'" width="500px">
      <el-form :model="form">
        <el-form-item label="标题"><el-input v-model="form.title" placeholder="请输入标题" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.category" class="w-full">
            <el-option label="学籍" value="学籍" /><el-option label="教务" value="教务" /><el-option label="后勤" value="后勤" /><el-option label="财务" value="财务" /><el-option label="保卫" value="保卫" /><el-option label="通用" value="通用" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入内容" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="doSave">{{ editingId ? '保存' : '添加' }}</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getGuides, addGuide, updateGuide, deleteGuide } from '@/api/admin'
import { useUserStore } from '@/store/user'
import { ElMessage, ElMessageBox } from 'element-plus'
const userStore = useUserStore()
const guides = ref<any[]>([])
const page = ref(1), total = ref(0), size = 10
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({ title: '', content: '', category: '通用' })
async function fetch() { const r = await getGuides({ page: page.value, size }); guides.value = r.data.records; total.value = r.data.total }
function showAdd() { editingId.value = null; form.title = ''; form.content = ''; form.category = '通用'; dialogVisible.value = true }
function showEdit(g: any) { editingId.value = g.id; form.title = g.title; form.content = g.content; form.category = g.category || '通用'; dialogVisible.value = true }
async function doSave() {
  if (!form.title.trim()) { ElMessage.warning('请输入标题'); return }
  if (editingId.value) { await updateGuide(editingId.value, form); ElMessage.success('修改成功') }
  else { await addGuide(form); ElMessage.success('添加成功') }
  dialogVisible.value = false; fetch()
}
async function doDelete(id: number) {
  try { await ElMessageBox.confirm('确认删除？', '提示', { type: 'warning' }) } catch { return }
  await deleteGuide(id); ElMessage.success('已删除'); fetch()
}
onMounted(fetch)
</script>

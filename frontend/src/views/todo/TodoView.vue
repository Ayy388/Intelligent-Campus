<template>
  <div class="max-w-3xl mx-auto space-y-6">
    <div class="flex items-center justify-between">
      <h1 class="text-xl font-bold text-gray-900">我的待办</h1>
      <el-button type="primary" :icon="Plus" @click="showAdd = true">新建待办</el-button>
    </div>

    <!-- 筛选 tabs -->
    <div class="flex gap-2">
      <button
        v-for="f in filters"
        :key="f.key"
        @click="currentFilter = f.key"
        class="px-4 py-1.5 rounded-lg text-sm font-medium transition-all duration-200"
        :class="currentFilter === f.key ? 'bg-indigo-50 text-indigo-700 border border-indigo-200' : 'bg-white text-gray-500 border border-gray-200 hover:bg-gray-50'"
      >
        {{ f.label }}
        <span v-if="f.count !== undefined" class="ml-1 text-xs opacity-60">({{ f.count }})</span>
      </button>
    </div>

    <!-- 待办列表 -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-100 divide-y divide-gray-50">
      <div v-if="loading" class="flex items-center justify-center py-12 text-gray-400 text-sm">加载中...</div>
      <div v-else-if="displayedTodos.length === 0" class="flex flex-col items-center justify-center py-14 text-gray-400">
        <svg class="w-12 h-12 mb-3 text-gray-200" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round">
          <path d="M9 5H7a2 2 0 0 0-2 2v12a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V7a2 2 0 0 0-2-2h-2"/>
          <rect x="9" y="3" width="6" height="4" rx="1"/>
          <line x1="9" y1="12" x2="15" y2="12"/>
          <line x1="9" y1="16" x2="13" y2="16"/>
        </svg>
        <span class="text-sm">暂无待办事项</span>
        <el-button class="todo-inline-btn" @click="showAdd = true" size="small">+ 新建一条</el-button>
      </div>
      <div
        v-for="t in displayedTodos"
        :key="t.id"
        class="flex items-start gap-3 px-5 py-4 group hover:bg-gray-50/50 transition-colors"
      >
        <el-checkbox
          :model-value="t.completed === 1"
          @change="toggleTodo(t)"
          class="mt-0.5"
        />
        <div class="flex-1 min-w-0">
          <div class="flex items-center gap-2">
            <span
              class="text-sm"
              :class="t.completed === 1 ? 'text-gray-300 line-through' : 'text-gray-800'"
            >{{ t.title }}</span>
            <span
              v-if="t.priority === 2"
              class="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-red-50 text-red-500 border border-red-100"
            >高</span>
            <span
              v-else-if="t.priority === 0"
              class="shrink-0 px-1.5 py-0.5 rounded text-[10px] font-semibold bg-gray-50 text-gray-400 border border-gray-100"
            >低</span>
          </div>
          <div v-if="t.dueDate" class="text-xs text-gray-400 mt-1">
            <span v-if="t.completed !== 1 && isOverdue(t.dueDate)" class="text-red-400">已逾期</span>
            <span v-else>截止 {{ t.dueDate }}</span>
          </div>
        </div>
        <div class="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
          <button @click="editTodo(t)" class="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors">
            <el-icon :size="14"><Edit /></el-icon>
          </button>
          <button @click="handleDelete(t)" class="w-7 h-7 flex items-center justify-center rounded-lg text-gray-400 hover:text-red-500 hover:bg-red-50 transition-colors">
            <el-icon :size="14"><Delete /></el-icon>
          </button>
        </div>
      </div>
    </div>

    <!-- 新建/编辑对话框 -->
    <el-dialog
      v-model="showAdd"
      :title="editing ? '编辑待办' : '新建待办'"
      width="420px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="待办内容">
          <el-input v-model="form.title" placeholder="请输入待办事项" maxlength="200" show-word-limit />
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="form.priority">
            <el-radio :value="0">低</el-radio>
            <el-radio :value="1">中</el-radio>
            <el-radio :value="2">高</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="截止日期（可选）">
          <el-date-picker
            v-model="form.dueDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            class="w-full"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAdd = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getTodos, createTodo, updateTodo, deleteTodo } from '@/api/todo'

const todos = ref<any[]>([])
const loading = ref(false)
const saving = ref(false)
const showAdd = ref(false)
const editing = ref<any>(null)
const currentFilter = ref<'all' | 'pending' | 'done'>('pending')

const form = reactive({
  title: '',
  priority: 1,
  dueDate: ''
})

const displayedTodos = computed(() => {
  if (currentFilter.value === 'pending') return todos.value.filter(t => t.completed !== 1)
  if (currentFilter.value === 'done') return todos.value.filter(t => t.completed === 1)
  return todos.value
})

const filters = computed(() => {
  const total = todos.value.length
  const done = todos.value.filter(t => t.completed === 1).length
  const pending = total - done
  return [
    { key: 'all', label: '全部', count: total },
    { key: 'pending', label: '待处理', count: pending },
    { key: 'done', label: '已完成', count: done },
  ]
})

function isOverdue(dateStr: string) {
  return dateStr < new Date().toISOString().substring(0, 10)
}

function resetForm() {
  form.title = ''
  form.priority = 1
  form.dueDate = ''
  editing.value = null
}

function editTodo(t: any) {
  editing.value = t
  form.title = t.title
  form.priority = t.priority ?? 1
  form.dueDate = t.dueDate || ''
  showAdd.value = true
}

async function fetchTodos() {
  loading.value = true
  try {
    const res = await getTodos({ page: 1, size: 100 })
    todos.value = res.data.records || []
  } catch {} finally {
    loading.value = false
  }
}

async function handleSave() {
  if (!form.title.trim()) {
    ElMessage.warning('请输入待办内容')
    return
  }
  saving.value = true
  try {
    if (editing.value) {
      await updateTodo(editing.value.id, {
        title: form.title,
        priority: form.priority,
        dueDate: form.dueDate || null
      })
      ElMessage.success('已更新')
    } else {
      await createTodo({
        title: form.title,
        priority: form.priority,
        dueDate: form.dueDate || null
      })
      ElMessage.success('已创建')
    }
    showAdd.value = false
    resetForm()
    await fetchTodos()
  } catch {} finally {
    saving.value = false
  }
}

async function toggleTodo(t: any) {
  try {
    await updateTodo(t.id, { completed: t.completed === 1 ? 0 : 1 })
    t.completed = t.completed === 1 ? 0 : 1
  } catch {}
}

async function handleDelete(t: any) {
  try {
    await ElMessageBox.confirm('确定要删除这条待办吗？', '确认删除')
    await deleteTodo(t.id)
    ElMessage.success('已删除')
    await fetchTodos()
  } catch {}
}

onMounted(fetchTodos)
</script>

<style scoped>
.todo-inline-btn {
  height: 28px !important;
  padding: 0 10px !important;
  font-size: 11px !important;
  border-radius: 6px !important;
  border-color: #e5e7eb !important;
  color: #6b7280 !important;
  background: transparent !important;
  font-weight: 500 !important;
  transition: all 0.2s ease !important;
}

.todo-inline-btn:hover {
  color: #6366f1 !important;
  border-color: #c7d2fe !important;
  background: #eef2ff !important;
}
</style>

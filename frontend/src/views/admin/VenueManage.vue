<template>
  <div>
    <div class="animate__animated animate__fadeInDown">
      <div class="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden transition-all duration-300 hover:shadow-md">
        <div class="flex items-center justify-between px-6 py-5 border-b border-gray-100">
          <div class="flex items-center gap-3">
            <div class="w-9 h-9 rounded-lg bg-gradient-to-br from-sky-500 to-blue-600 flex items-center justify-center shadow-sm">
              <svg class="w-5 h-5 text-white" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                <line x1="3" y1="9" x2="21" y2="9"/>
                <line x1="9" y1="3" x2="9" y2="21"/>
              </svg>
            </div>
            <div>
              <h3 class="text-base font-bold text-ink">场地管理</h3>
              <p class="text-xs text-mist mt-0.5">管理校园可用场地</p>
            </div>
          </div>
          <el-button type="primary" @click="openAdd" class="!rounded-lg">
            <svg class="w-4 h-4 mr-1" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <line x1="12" y1="5" x2="12" y2="19"/><line x1="5" y1="12" x2="19" y2="12"/>
            </svg>
            添加场地
          </el-button>
        </div>

        <div class="p-6">
          <div v-if="loading" class="flex items-center justify-center py-16">
            <svg class="animate-spin h-8 w-8 text-gray-300" viewBox="0 0 24 24" fill="none">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
            </svg>
          </div>

          <div v-else-if="venues.length === 0" class="flex flex-col items-center justify-center py-16">
            <svg class="w-16 h-16 text-gray-200 mb-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1" stroke-linecap="round" stroke-linejoin="round">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
              <line x1="3" y1="9" x2="21" y2="9"/>
              <line x1="9" y1="3" x2="9" y2="21"/>
            </svg>
            <p class="text-sm text-mist">暂无场地，请添加</p>
          </div>

          <el-table v-else :data="venues" class="!border-0" header-row-class-name="manage-table-header" row-class-name="manage-table-row">
            <el-table-column label="场地名称" min-width="160">
              <template #default="{row}">
                <span class="text-sm font-medium text-ink">{{ row.name }}</span>
              </template>
            </el-table-column>
            <el-table-column label="位置" min-width="180">
              <template #default="{row}">
                <span class="text-xs text-steel">{{ row.location || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="容量" width="80" align="center">
              <template #default="{row}">
                <span class="text-sm text-ink">{{ row.capacity || 0 }}人</span>
              </template>
            </el-table-column>
            <el-table-column label="描述" min-width="200">
              <template #default="{row}">
                <span class="text-xs text-ash truncate block max-w-[200px]">{{ row.description || '--' }}</span>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="80" align="center">
              <template #default="{row}">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small" round class="!border-0">
                  {{ row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="200" fixed="right">
              <template #default="{row}">
                <div class="flex gap-2">
                  <el-button size="small" @click="openEdit(row)" plain class="!rounded-lg">编辑</el-button>
                  <el-popconfirm title="确定删除该场地？" @confirm="doDelete(row.id)">
                    <template #reference>
                      <el-button size="small" type="danger" plain class="!rounded-lg">删除</el-button>
                    </template>
                  </el-popconfirm>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑场地' : '添加场地'" width="520px" top="15vh" :close-on-click-modal="false">
      <el-form :model="form" label-position="top" class="px-1">
        <el-form-item label="场地名称" required>
          <el-input v-model="form.name" placeholder="请输入场地名称" class="custom-input" />
        </el-form-item>
        <el-form-item label="位置">
          <el-input v-model="form.location" placeholder="请输入场地位置" class="custom-input" />
        </el-form-item>
        <el-form-item label="容量">
          <el-input-number v-model="form.capacity" :min="0" :max="99999" style="width:100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入场地描述" class="custom-input" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-3">
          <el-button @click="dialogVisible = false" class="!rounded-lg">取消</el-button>
          <el-button type="primary" @click="doSave" class="!rounded-lg">保存</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getVenues, addVenue, updateVenue, deleteVenue } from '@/api/club'
import { ElMessage } from 'element-plus'
import type { Venue } from '@/types'

const venues = ref<Venue[]>([])
const loading = ref(false)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ name: '', location: '', capacity: 50, description: '', status: 1 })

async function fetchVenues() {
  loading.value = true
  try {
    const r = await getVenues()
    venues.value = r.data || []
  } catch { /* ignore */ }
  loading.value = false
}

function openAdd() {
  isEdit.value = false
  editId.value = null
  form.name = ''
  form.location = ''
  form.capacity = 50
  form.description = ''
  form.status = 1
  dialogVisible.value = true
}

function openEdit(row: Venue) {
  isEdit.value = true
  editId.value = row.id
  form.name = row.name || ''
  form.location = row.location || ''
  form.capacity = row.capacity ?? 50
  form.description = row.description || ''
  form.status = row.status ?? 1
  dialogVisible.value = true
}

async function doSave() {
  if (!form.name.trim()) { ElMessage.warning('请输入场地名称'); return }
  try {
    if (isEdit.value && editId.value) {
      await updateVenue(editId.value, form)
      ElMessage.success('已更新')
    } else {
      await addVenue(form)
      ElMessage.success('已添加')
    }
    dialogVisible.value = false
    fetchVenues()
  } catch { ElMessage.error('操作失败') }
}

async function doDelete(id: number) {
  try {
    await deleteVenue(id)
    ElMessage.success('已删除')
    fetchVenues()
  } catch { ElMessage.error('删除失败') }
}

onMounted(fetchVenues)
</script>

<style scoped>
.manage-table-header th {
  background-color: #F9FAFB !important;
  color: #6B7280 !important;
  font-size: 0.75rem !important;
  font-weight: 600 !important;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  padding-top: 12px !important;
  padding-bottom: 12px !important;
}
.manage-table-row td {
  padding-top: 14px !important;
  padding-bottom: 14px !important;
}
:deep(.custom-input .el-input__wrapper),
:deep(.custom-input .el-textarea__inner) {
  border-radius: 10px;
  border: 1px solid #E5E7EB;
  box-shadow: none;
  padding: 8px 12px;
  font-size: 13px;
}
:deep(.custom-input .el-input__wrapper:hover),
:deep(.custom-input .el-textarea__inner:hover) {
  border-color: #9CA3AF;
}
:deep(.custom-input .el-input__wrapper.is-focus),
:deep(.custom-input .el-textarea__inner:focus) {
  border-color: #6366F1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.1);
}
</style>
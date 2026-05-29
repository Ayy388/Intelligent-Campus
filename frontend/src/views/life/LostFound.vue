<template>
  <div>
    <div class="flex items-center justify-between mb-4">
      <div class="flex gap-2">
        <button v-for="t in types" :key="t.value" @click="curType = t.value; page=1; fetch()"
          :class="['px-4 py-1.5 rounded-lg text-sm font-medium border transition-all', curType===t.value ? 'bg-ink text-white border-ink' : 'bg-white text-ash border-soft hover:border-line']">{{ t.label }}</button>
      </div>
      <div class="flex items-center gap-3">
        <el-input v-model="keyword" placeholder="搜索..." size="small" clearable class="!w-48" @change="page=1;fetch()" />
        <button @click="showDialog" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel">发布信息</button>
      </div>
    </div>
    <div class="space-y-3">
      <div v-for="item in items" :key="item.id" class="bg-white rounded-xl border border-soft p-4 hover:shadow-sm transition-shadow">
        <div class="flex items-start justify-between">
          <div>
            <div class="flex items-center gap-2 mb-1">
              <el-tag size="small" :type="item.type===0?'':''" class="!bg-wash !text-ash !border-soft">{{ item.type===0?'寻物':'招领' }}</el-tag>
              <span class="text-sm font-medium text-ink">{{ item.title }}</span>
            </div>
            <div class="text-xs text-ash mt-1">{{ item.description }}</div>
            <div class="flex gap-4 mt-2 text-xs text-mist">
              <span>{{ item.location }}</span><span>{{ item.contact }}</span>
            </div>
          </div>
          <button v-if="item.status===0" @click="markDone(item.id)" class="text-xs text-ash hover:text-ink transition-colors px-2 py-1">标记已解决</button>
          <el-tag v-else size="small" type="info" class="!bg-wash !text-mist !border-soft">已解决</el-tag>
        </div>
      </div>
    </div>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" title="发布信息" width="450px">
      <el-form :model="form">
        <el-form-item label="类型"><el-radio-group v-model="form.type"><el-radio :value="0">寻物</el-radio><el-radio :value="1">招领</el-radio></el-radio-group></el-form-item>
        <el-form-item label="标题"><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
        <el-form-item label="地点"><el-input v-model="form.location" /></el-form-item>
        <el-form-item label="联系方式"><el-input v-model="form.contact" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">发布</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getLostFound, addLostFound, updateLostFoundStatus } from '@/api/life'
import { ElMessage } from 'element-plus'
const items = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const curType = ref<number | undefined>(undefined)
const keyword = ref('')
const dialogVisible = ref(false)
const types = [{ label: '全部', value: undefined as number | undefined }, { label: '寻物', value: 0 }, { label: '招领', value: 1 }]
const form = reactive({ type: 0, title: '', description: '', location: '', contact: '' })
async function fetch() { const r = await getLostFound({ type: curType.value, keyword: keyword.value, page: page.value, size: 10 }); items.value = r.data.records; total.value = r.data.total }
function showDialog() { form.type=0; form.title=''; form.description=''; form.location=''; form.contact=''; dialogVisible.value=true }
async function submit() { await addLostFound(form); ElMessage.success('发布成功'); dialogVisible.value=false; fetch() }
async function markDone(id:number) { await updateLostFoundStatus(id, 1); ElMessage.success('已标记'); fetch() }
onMounted(fetch)
</script>

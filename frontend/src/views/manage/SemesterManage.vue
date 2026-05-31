<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between">
        <span>学期管理</span>
        <el-button type="primary" @click="showDialog()">添加学期</el-button>
      </div>
    </template>
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="xn" label="学年" width="140" />
      <el-table-column prop="xqqc" label="学期名称" width="200" />
      <el-table-column label="学期代码" width="100">
        <template #default="{row}">{{ row.xqjc }}</template>
      </el-table-column>
      <el-table-column label="开始日期" width="120">
        <template #default="{row}">{{ row.ksrq?.substring(0,10) }}</template>
      </el-table-column>
      <el-table-column label="结束日期" width="120">
        <template #default="{row}">{{ row.jsrq?.substring(0,10) }}</template>
      </el-table-column>
      <el-table-column prop="zc" label="周数" width="60" />
      <el-table-column label="状态" width="80">
        <template #default="{row}">
          <el-tag :type="row.status===1?'success':'info'">{{ row.status===1?'当前':'历史' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="del(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑学期':'添加学期'" width="500px">
      <el-form :model="form">
        <el-form-item label="学年"><el-input v-model="form.xn" placeholder="如 2025-2026学年" /></el-form-item>
        <el-form-item label="学期代码"><el-input v-model="form.xqjc" placeholder="如 202502" /></el-form-item>
        <el-form-item label="学期名称"><el-input v-model="form.xqqc" placeholder="如 2025-2026学年第二学期" /></el-form-item>
        <el-form-item label="开始日期"><el-date-picker v-model="form.ksrq" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item>
        <el-form-item label="结束日期"><el-date-picker v-model="form.jsrq" type="date" value-format="YYYY-MM-DD" placeholder="选择日期" style="width:100%" /></el-form-item>
        <el-form-item label="周数"><el-input-number v-model="form.zc" :min="1" :max="30" /></el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status">
            <el-option :value="1" label="当前学期" />
            <el-option :value="0" label="历史学期" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible=false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getSemesters, addSemester, updateSemester, deleteSemester } from '@/api/edu'
import { ElMessage, ElMessageBox } from 'element-plus'

const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const form = reactive({
  xn: '',
  xqjc: '',
  xqqc: '',
  ksrq: '',
  jsrq: '',
  zc: 20,
  status: 0
})

async function fetch() {
  loading.value = true
  const r = await getSemesters()
  tableData.value = r.data
  loading.value = false
}

function showDialog(row?: any) {
  editId.value = row?.id || null
  Object.assign(form, row ? { ...row } : {
    xn: '', xqjc: '', xqqc: '', ksrq: '', jsrq: '', zc: 20, status: 0
  })
  dialogVisible.value = true
}

async function save() {
  if (!form.xn.trim() || !form.xqjc.trim() || !form.xqqc.trim()) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (editId.value) {
    await updateSemester(editId.value, form)
  } else {
    await addSemester(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetch()
}

async function del(id: number) {
  await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
  await deleteSemester(id)
  ElMessage.success('删除成功')
  fetch()
}

onMounted(fetch)
</script>
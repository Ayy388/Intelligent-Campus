<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>专业管理</span><el-button type="primary" @click="openCreate">新增专业</el-button></div></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="专业名称" />
      <el-table-column prop="code" label="代码" />
      <el-table-column prop="departmentName" label="所属院系" />
      <el-table-column prop="years" label="学制" width="60" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑专业':'新增专业'" width="500px">
      <el-form :model="form">
        <el-form-item label="专业名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="所属院系">
          <el-select v-model="form.departmentId" placeholder="选择院系" class="w-full">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学制年限"><el-input-number v-model="form.years" :min="2" :max="5" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMajors, getAllDepartments, createMajor, updateMajor, deleteMajor } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref<any[]>([])
const departments = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ name: '', code: '', departmentId: null as number | null, years: 4 })

async function fetch() {
  loading.value = true
  const r = await getMajors({ page: page.value, size: size.value })
  list.value = (r.data.records || []).map((item: any) => ({
    ...item,
    departmentName: departments.value.find((d: any) => d.id === item.departmentId)?.name || ''
  }))
  total.value = r.data.total || 0
  loading.value = false
}

async function fetchDepartments() {
  const r = await getAllDepartments()
  departments.value = r.data || []
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.name = ''; form.code = ''; form.departmentId = null; form.years = 4
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.code = row.code || ''; form.departmentId = row.departmentId; form.years = row.years || 4
  dialogVisible.value = true
}

async function submit() {
  if (isEdit.value && editId.value) {
    await updateMajor(editId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createMajor(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; fetch()
}

async function doDelete(id: number) {
  await ElMessageBox.confirm('确定删除该专业？', '提示', { type: 'warning' })
  await deleteMajor(id); ElMessage.success('删除成功'); fetch()
}

onMounted(() => { fetch(); fetchDepartments() })
</script>
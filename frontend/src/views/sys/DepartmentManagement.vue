<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>院系管理</span><el-button type="primary" @click="openCreate">新增院系</el-button></div></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="院系名称" />
      <el-table-column prop="code" label="代码" />
      <el-table-column prop="sortOrder" label="排序" width="60" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑院系':'新增院系'" width="500px">
      <el-form :model="form">
        <el-form-item label="院系名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { Department } from '@/types'

const list = ref<Department[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ name: '', code: '', sortOrder: 0 })

async function fetch() {
  loading.value = true
  const r = await getDepartments({ page: page.value, size: size.value })
  list.value = r.data.records || []
  total.value = r.data.total || 0
  loading.value = false
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.name = ''; form.code = ''; form.sortOrder = 0
  dialogVisible.value = true
}

function openEdit(row: Department) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.code = row.code || ''; form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function submit() {
  if (isEdit.value && editId.value) {
    await updateDepartment(editId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createDepartment(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; fetch()
}

async function doDelete(id: number) {
  await ElMessageBox.confirm('确定删除该院系？', '提示', { type: 'warning' })
  await deleteDepartment(id); ElMessage.success('删除成功'); fetch()
}

onMounted(fetch)
</script>
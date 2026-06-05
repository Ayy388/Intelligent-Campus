<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>班级管理</span><el-button type="primary" @click="openCreate">新增班级</el-button></div></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="className" label="班级名称" />
      <el-table-column prop="departmentName" label="院系" />
      <el-table-column prop="majorName" label="专业" />
      <el-table-column prop="gradeName" label="年级" width="80" />
      <el-table-column prop="counselorName" label="辅导员" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑班级':'新增班级'" width="500px">
      <el-form :model="form">
        <el-form-item label="班级名称"><el-input v-model="form.className" /></el-form-item>
        <el-form-item label="院系">
          <el-select v-model="form.departmentId" placeholder="选择院系" class="w-full" @change="onDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="form.majorId" placeholder="选择专业" class="w-full" :disabled="!form.departmentId">
            <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="form.gradeId" placeholder="选择年级" class="w-full">
            <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="辅导员">
          <el-select v-model="form.counselorId" placeholder="选择辅导员" class="w-full" clearable>
            <el-option v-for="c in counselors" :key="c.id" :label="c.realName" :value="c.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getClasses, createClass, updateClass, deleteClass, getAllDepartments, getMajorsByDept, getAllGrades, getUsers } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { ClassInfo, Department, Major, SysGrade, User } from '@/types'

const list = ref<ClassInfo[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const departments = ref<Department[]>([])
const majors = ref<Major[]>([])
const grades = ref<SysGrade[]>([])
const counselors = ref<User[]>([])
const form = reactive({ className: '', departmentId: null as number | null, majorId: null as number | null, gradeId: null as number | null, counselorId: null as number | null })

async function fetch() {
  loading.value = true
  const r = await getClasses({ page: page.value, size: size.value })
  list.value = r.data.records || []
  total.value = r.data.total || 0
  loading.value = false
}

async function fetchDepartments() {
  try { const r = await getAllDepartments(); departments.value = r.data || [] } catch {}
}

async function fetchGrades() {
  try { const r = await getAllGrades(); grades.value = r.data || [] } catch {}
}

async function fetchCounselors() {
  try {
    const r = await getUsers({ page: 1, size: 999 })
    const users = r.data.records || []
    counselors.value = users.filter((u: User) => u.roleId === 4)
  } catch { counselors.value = [] }
}

async function onDeptChange(deptId: number) {
  form.majorId = null
  if (deptId) {
    try { const r = await getMajorsByDept(deptId); majors.value = r.data || [] } catch {}
  } else {
    majors.value = []
  }
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.className = ''; form.departmentId = null; form.majorId = null; form.gradeId = null; form.counselorId = null
  majors.value = []
  dialogVisible.value = true
}

function openEdit(row: ClassInfo) {
  isEdit.value = true; editId.value = row.id
  form.className = row.className; form.departmentId = row.departmentId ?? null
  form.majorId = row.majorId ?? null; form.gradeId = row.gradeId ?? null; form.counselorId = row.counselorId ?? null
  if (row.departmentId) onDeptChange(row.departmentId)
  dialogVisible.value = true
}

async function submit() {
  if (isEdit.value && editId.value) {
    await updateClass(editId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createClass(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; fetch()
}

async function doDelete(id: number) {
  await ElMessageBox.confirm('确定删除该班级？', '提示', { type: 'warning' })
  await deleteClass(id); ElMessage.success('删除成功'); fetch()
}

onMounted(() => { fetch(); fetchDepartments(); fetchGrades(); fetchCounselors() })
</script>

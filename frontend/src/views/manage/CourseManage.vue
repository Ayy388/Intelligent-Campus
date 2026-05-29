<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>课程管理</span><el-button type="primary" @click="showDialog()">添加课程</el-button></div></template>
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="courseCode" label="编号" width="100" />
      <el-table-column prop="courseName" label="名称" />
      <el-table-column prop="credit" label="学分" width="60" />
      <el-table-column prop="semester" label="学期" width="100" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status===1?'success':row.status===2?'info':'warning'">{{ row.status===1?'可选':row.status===2?'结束':'未开放' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{row}"><el-button size="small" @click="showDialog(row)">编辑</el-button><el-button size="small" type="danger" @click="del(row.id)">删除</el-button></template>
      </el-table-column>
    </el-table>
    <el-dialog v-model="dialogVisible" :title="editId?'编辑':'添加'" width="500px">
      <el-form :model="form">
        <el-form-item label="编号"><el-input v-model="form.courseCode" /></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.courseName" /></el-form-item>
        <el-form-item label="学分"><el-input-number v-model="form.credit" :min="0" :step="0.5" /></el-form-item>
        <el-form-item label="学期"><el-input v-model="form.semester" placeholder="如 2026-春" /></el-form-item>
        <el-form-item label="教室"><el-input v-model="form.classroom" /></el-form-item>
        <el-form-item label="容量"><el-input-number v-model="form.capacity" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-select v-model="form.status"><el-option :value="0" label="未开放" /><el-option :value="1" label="开放选课" /><el-option :value="2" label="已结束" /></el-select></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCourses, addCourse, updateCourse, deleteCourse } from '@/api/edu'
import { ElMessage, ElMessageBox } from 'element-plus'
const tableData = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ courseCode: '', courseName: '', credit: 0, semester: '', classroom: '', capacity: 0, status: 0 })
async function fetch() { loading.value = true; const r = await getCourses({ page: 1, size: 100 }); tableData.value = r.data.records; loading.value = false }
function showDialog(row?: any) {
  editId.value = row?.id || null
  Object.assign(form, row || { courseCode: '', courseName: '', credit: 0, semester: '', classroom: '', capacity: 0, status: 0 })
  dialogVisible.value = true
}
async function save() {
  if (editId.value) { await updateCourse(editId.value, form) } else { await addCourse(form) }
  ElMessage.success('保存成功'); dialogVisible.value = false; fetch()
}
async function del(id: number) {
  await ElMessageBox.confirm('确认删除?', '提示', { type: 'warning' })
  await deleteCourse(id); ElMessage.success('删除成功'); fetch()
}
onMounted(fetch)
</script>

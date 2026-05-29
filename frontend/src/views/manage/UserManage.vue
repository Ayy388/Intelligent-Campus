<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>用户管理</span><el-button type="primary" @click="showDialog()">添加用户</el-button></div></template>
    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="username" label="用户名" />
      <el-table-column prop="realName" label="姓名" />
      <el-table-column prop="roleName" label="角色" width="80" />
      <el-table-column prop="department" label="院系" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{ row.status?'启用':'禁用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200">
        <template #default="{row}">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status?'danger':'success'" @click="toggleStatus(row)">{{ row.status?'禁用':'启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="editId?'编辑用户':'添加用户'" width="500px">
      <el-form :model="form">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="角色"><el-select v-model="form.roleId"><el-option :value="1" label="学生" /><el-option :value="2" label="教师" /><el-option :value="3" label="管理员" /></el-select></el-form-item>
        <el-form-item label="院系"><el-input v-model="form.department" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUsers, createUser, updateUser, toggleUserStatus } from '@/api/sys'
import { ElMessage } from 'element-plus'
const tableData = ref([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ username: '', realName: '', password: '', roleId: 1, department: '' })
async function fetch() { loading.value = true; const r = await getUsers({ page: page.value, size: 10 }); tableData.value = r.data.records; total.value = r.data.total; loading.value = false }
function showDialog(row?: any) {
  editId.value = row?.id || null
  Object.assign(form, row ? { ...row, password: '' } : { username: '', realName: '', password: '', roleId: 1, department: '' })
  dialogVisible.value = true
}
async function save() {
  if (editId.value) { await updateUser(editId.value, form) } else { await createUser(form) }
  ElMessage.success('保存成功'); dialogVisible.value = false; fetch()
}
async function toggleStatus(row: any) { await toggleUserStatus(row.id, row.status ? 0 : 1); ElMessage.success('操作成功'); fetch() }
onMounted(fetch)
</script>

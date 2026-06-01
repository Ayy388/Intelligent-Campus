<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>{{ tabLabel }}</span>
        <el-button type="primary" @click="showDialog()">添加{{ currentRoleName }}</el-button>
      </div>
    </template>

    <el-tabs v-model="activeTab" @tab-change="onTabChange" style="margin-top:-8px">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane label="学生" name="student" />
      <el-tab-pane label="教师" name="teacher" />
      <el-tab-pane label="辅导员" name="counselor" />
      <el-tab-pane label="管理员" name="admin" />
    </el-tabs>

    <el-table :data="tableData" v-loading="loading">
      <el-table-column prop="username" label="学号/用户名" min-width="120" />
      <el-table-column prop="realName" label="姓名" min-width="100" />
      <el-table-column label="性别" width="60"><template #default="{row}">{{ row.gender === 1 ? '男' : row.gender === 0 ? '女' : '--' }}</template></el-table-column>
      <el-table-column prop="roleName" label="角色" width="80" />
      <el-table-column prop="departmentName" label="院系" min-width="120" />
      <el-table-column v-if="activeTab !== 'counselor'" prop="majorName" label="专业" min-width="120" />
      <el-table-column v-if="activeTab !== 'counselor'" prop="className" label="班级" min-width="120" />
      <el-table-column v-if="activeTab !== 'counselor'" prop="counselorName" label="辅导员" min-width="100" />
      <el-table-column v-if="activeTab === 'counselor'" prop="counselorClasses" label="负责班级" min-width="200" />
      <el-table-column label="状态" width="80"><template #default="{row}"><el-tag :type="row.status?'success':'danger'">{{ row.status?'启用':'禁用' }}</el-tag></template></el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{row}">
          <el-button size="small" @click="showDialog(row)">编辑</el-button>
          <el-button size="small" :type="row.status?'danger':'success'" @click="toggleStatus(row)">{{ row.status?'禁用':'启用' }}</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="(editId?'编辑':'添加')+currentRoleName" width="560px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
        <el-form-item label="姓名"><el-input v-model="form.realName" /></el-form-item>
        <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
        <el-form-item label="性别"><el-select v-model="form.gender" style="width:100%"><el-option :value="1" label="男" /><el-option :value="0" label="女" /></el-select></el-form-item>
        <el-form-item label="角色">
          <el-select v-model="form.roleId" style="width:100%" :disabled="roleLocked">
            <el-option :value="1" label="学生" />
            <el-option :value="2" label="教师" />
            <el-option :value="3" label="管理员" />
            <el-option :value="4" label="辅导员" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="showField('department')" label="院系"><el-select v-model="form.departmentId" placeholder="选择院系" style="width:100%"><el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" /></el-select></el-form-item>
        <el-form-item v-if="showField('major')" label="专业"><el-select v-model="form.majorId" placeholder="选择专业" style="width:100%" clearable><el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" /></el-select></el-form-item>
        <el-form-item v-if="showField('class')" label="班级"><el-select v-model="form.classId" placeholder="选择班级" style="width:100%" clearable><el-option v-for="c in filteredClasses" :key="c.id" :label="c.className" :value="c.id" /></el-select></el-form-item>
        <el-form-item v-if="showField('counselor')" label="辅导员"><el-select v-model="form.counselorId" placeholder="选择辅导员" style="width:100%" clearable><el-option v-for="c in counselors" :key="c.id" :label="c.realName" :value="c.id" /></el-select></el-form-item>
        <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
        <el-form-item label="邮箱"><el-input v-model="form.email" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="save">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getUsers, createUser, updateUser, toggleUserStatus, getAllDepartments, getAllClasses, getClass } from '@/api/sys'
import request from '@/utils/request'
import { ElMessage } from 'element-plus'

const route = useRoute()

const ROLE_MAP: Record<string, number> = { student: 1, teacher: 2, admin: 3, counselor: 4 }
const ROLE_NAME: Record<string, string> = { all: '用户', student: '学生', teacher: '教师', counselor: '辅导员', admin: '管理员' }

const tableData = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const editId = ref<number | null>(null)
const departments = ref<any[]>([])
const classes = ref<any[]>([])
const majors = ref<any[]>([])
const counselors = ref<any[]>([])
const allUsers = ref<any[]>([])
const activeTab = ref('all')

const roleLocked = computed(() => activeTab.value !== 'all')
const currentRoleName = computed(() => ROLE_NAME[activeTab.value] || '用户')
const tabLabel = computed(() => activeTab.value === 'all' ? '用户管理' : `${currentRoleName.value}管理`)
const activeRoleId = computed(() => ROLE_MAP[activeTab.value] ?? null)

const form = reactive({ username: '', realName: '', password: '', gender: null as number | null, roleId: 1, departmentId: null as number | null, majorId: null as number | null, classId: null as number | null, counselorId: null as number | null, phone: '', email: '' })

const filteredClasses = computed(() => {
  if (!form.majorId) return classes.value
  return classes.value.filter(c => c.majorId === form.majorId)
})

async function fetch() {
  loading.value = true
  const params: Record<string, any> = { page: page.value, size: 10 }
  if (activeRoleId.value) params.roleId = activeRoleId.value
  const r = await getUsers(params)
  tableData.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

async function fetchDepartments() { try { const r = await getAllDepartments(); departments.value = r.data || [] } catch {} }
async function fetchMajors(deptId?: number) {
  try {
    const params = deptId ? { deptId } : {}
    const r = await request.get('/sys/majors/all', { params })
    majors.value = r.data || []
  } catch {}
}
async function fetchClasses() { try { const r = await getAllClasses(); classes.value = r.data || [] } catch {} }
async function fetchCounselors() {
  try {
    const r = await getUsers({ page: 1, size: 999 })
    allUsers.value = r.data.records || []
    counselors.value = allUsers.value.filter((u: any) => u.roleId === 4 || u.roleName === '辅导员')
  } catch { counselors.value = [] }
}

function showField(field: string): boolean {
  if (activeTab.value === 'all') return true
  if (field === 'major' || field === 'class' || field === 'counselor') return activeTab.value === 'student'
  if (field === 'department') return activeTab.value !== 'admin'
  return true
}

function onTabChange() {
  page.value = 1
  fetch()
}

function showDialog(row?: any) {
  editId.value = row?.id || null
  if (row) {
    Object.assign(form, { ...row, password: '' })
  } else {
    const defaultRoleId = roleLocked.value ? (activeRoleId.value || 1) : 1
    Object.assign(form, { username: '', realName: '', password: '', gender: null, roleId: defaultRoleId, departmentId: null, majorId: null, classId: null, counselorId: null, phone: '', email: '' })
  }
  dialogVisible.value = true
}

async function save() {
  const data = { ...form }
  if (editId.value) {
    if (!data.password) delete data.password
    await updateUser(editId.value, data)
  } else {
    await createUser(data)
  }
  ElMessage.success('保存成功'); dialogVisible.value = false; fetch()
}

async function toggleStatus(row: any) { await toggleUserStatus(row.id, row.status ? 0 : 1); ElMessage.success('操作成功'); fetch() }

watch(() => form.classId, async (val) => {
  if (val && (activeTab.value === 'student' || activeTab.value === 'all')) {
    try {
      const r = await getClass(val)
      if (r.data?.counselorId) form.counselorId = r.data.counselorId
    } catch {}
  }
})

watch(() => form.departmentId, (val) => {
  form.majorId = null
  form.classId = null
  if (val) fetchMajors(val)
  else fetchMajors()
})

watch(() => route.query.role, (val) => {
  const role = val as string | undefined
  if (role && ROLE_MAP[role] && activeTab.value !== role) {
    activeTab.value = role
    page.value = 1
    fetch()
  }
})

onMounted(() => {
  const role = route.query.role as string | undefined
  if (role && ROLE_MAP[role]) activeTab.value = role
  fetch(); fetchDepartments(); fetchMajors(); fetchClasses(); fetchCounselors()
})
</script>

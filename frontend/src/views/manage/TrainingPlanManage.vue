<template>
  <el-card>
    <template #header>
      <div style="display:flex;justify-content:space-between;align-items:center">
        <span>{{ detailMode ? currentPlan.name : '培养方案管理' }}</span>
        <div style="display:flex;gap:8px">
          <el-button v-if="detailMode" @click="backToList">← 返回列表</el-button>
          <el-button v-else type="primary" @click="showPlanDialog()">新建方案</el-button>
        </div>
      </div>
    </template>

    <!-- 列表模式 -->
    <template v-if="!detailMode">
      <el-table :data="tableData" v-loading="loading">
        <el-table-column prop="name" label="方案名称" min-width="200" />
        <el-table-column label="专业" width="150"><template #default="{row}">{{ row.majorName }}</template></el-table-column>
        <el-table-column label="年级" width="100"><template #default="{row}">{{ row.gradeName }}</template></el-table-column>
        <el-table-column label="学期数" width="80" prop="totalSemesters" />
        <el-table-column label="状态" width="80">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'" size="small">{{ row.status===1?'启用':'草稿' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{row}">
            <el-button size="small" @click="enterDetail(row.id)">编辑方案</el-button>
            <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" />
    </template>

    <!-- 详情模式 -->
    <template v-else>
      <div style="margin-bottom:16px;display:flex;align-items:center;gap:12px;flex-wrap:wrap">
        <el-tag>{{ currentPlan.majorName }}</el-tag>
        <el-tag type="success">{{ currentPlan.gradeName }}</el-tag>
        <el-tag type="info">{{ currentPlan.totalSemesters || 8 }}学期</el-tag>
      </div>

      <el-tabs v-model="activeSemester" @tab-change="onSemesterChange">
        <el-tab-pane v-for="n in (currentPlan.totalSemesters || 8)" :key="n" :label="'第'+n+'学期'" :name="String(n)" />
      </el-tabs>

      <el-table :data="semesterItems" v-loading="itemsLoading" style="margin-bottom:12px">
        <el-table-column prop="courseName" label="课程名称" min-width="160" />
        <el-table-column prop="courseCode" label="编号" width="120" />
        <el-table-column prop="credit" label="学分" width="60" />
        <el-table-column prop="hours" label="学时" width="60" />
        <el-table-column label="类型" width="80">
          <template #default="{row}">
            <el-tag :type="row.isRequired===1?'primary':'warning'" size="small">
              {{ row.isRequired===1?'必修':'选修' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生成状态" width="120">
          <template #default="{row}">
            <el-tag :type="row.status===1?'success':'info'" size="small">
              {{ row.status===1?'已生成':'未生成' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{row}">
            <el-button v-if="row.status!==1" size="small" @click="showItemDialog(row)">编辑</el-button>
            <el-button v-if="row.generatedCourseId" size="small" type="primary" @click="$router.push('/manage/courses')">查看课程</el-button>
            <el-button v-if="row.status!==1" size="small" type="danger" @click="doDeleteItem(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div style="display:flex;gap:8px">
        <el-button @click="showItemDialog(null)">添加课程</el-button>
        <el-button type="primary" :loading="generating" @click="doGenerate">
          一键生成第{{ activeSemester }}学期课程
        </el-button>
      </div>
    </template>

    <!-- 方案对话框 -->
    <el-dialog v-model="planDialogVisible" :title="editPlanId?'编辑方案':'新建方案'" width="500px">
      <el-form :model="planForm" label-width="80px">
        <el-form-item label="方案名称"><el-input v-model="planForm.name" /></el-form-item>
        <el-form-item label="所属院系">
          <el-select v-model="planForm.departmentId" style="width:100%" @change="onDeptChange">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="专业">
          <el-select v-model="planForm.majorId" style="width:100%">
            <el-option v-for="m in filteredMajors" :key="m.id" :label="m.name" :value="m.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="年级">
          <el-select v-model="planForm.gradeId" style="width:100%">
            <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="总学期数"><el-input-number v-model="planForm.totalSemesters" :min="1" :max="12" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="planForm.description" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="savePlan">保存</el-button>
      </template>
    </el-dialog>

    <!-- 课程项对话框 -->
    <el-dialog v-model="itemDialogVisible" :title="editItemId?'编辑课程':'添加课程'" width="500px">
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="课程名称"><el-input v-model="itemForm.courseName" /></el-form-item>
        <el-form-item label="课程编号"><el-input v-model="itemForm.courseCode" placeholder="留空自动生成" /></el-form-item>
        <el-form-item label="学分"><el-input-number v-model="itemForm.credit" :min="0" :step="0.5" /></el-form-item>
        <el-form-item label="学时"><el-input-number v-model="itemForm.hours" :min="0" /></el-form-item>
        <el-form-item label="课程类型">
          <el-radio-group v-model="itemForm.isRequired">
            <el-radio :value="1">必修</el-radio>
            <el-radio :value="0">选修</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序"><el-input-number v-model="itemForm.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="itemDialogVisible=false">取消</el-button>
        <el-button type="primary" @click="saveItem">保存</el-button>
      </template>
    </el-dialog>

    <!-- 生成结果对话框 -->
    <el-dialog v-model="resultVisible" title="生成成功" width="450px">
      <div>
        <p>成功生成 <el-tag type="success">{{ generateResult.generatedCount }}</el-tag> 门课程</p>
        <p v-if="generateResult.classNames">分配班级：<el-tag v-for="cn in generateResult.classNames" :key="cn" style="margin:2px">{{ cn }}</el-tag></p>
        <p>学期：{{ generateResult.semester }}</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="resultVisible=false;refreshItems()">确定</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import {
  getTrainingPlans, getTrainingPlan, createTrainingPlan, updateTrainingPlan, deleteTrainingPlan,
  getPlanItems, addPlanItem, updatePlanItem, deletePlanItem, generateSemester
} from '@/api/edu'
import { getAllDepartments, getAllGrades } from '@/api/sys'
import request from '@/utils/request'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref<any[]>([])
const page = ref(1)
const total = ref(0)

// 详情模式
const detailMode = ref(false)
const currentPlan = ref<any>({})
const activeSemester = ref('1')
const semesterItems = ref<any[]>([])
const itemsLoading = ref(false)

// 方案对话框
const planDialogVisible = ref(false)
const editPlanId = ref<number | null>(null)
const planForm = reactive({
  name: '', departmentId: null as number | null, majorId: null as number | null,
  gradeId: null as number | null, totalSemesters: 8, description: '', status: 1
})

// 课程项对话框
const itemDialogVisible = ref(false)
const editItemId = ref<number | null>(null)
const itemForm = reactive({
  courseName: '', courseCode: '', credit: 0, hours: 0, isRequired: 1, sortOrder: 0
})

// 生成
const generating = ref(false)
const resultVisible = ref(false)
const generateResult = ref<any>({})

// 下拉数据
const departments = ref<any[]>([])
const majors = ref<any[]>([])
const grades = ref<any[]>([])

const filteredMajors = computed(() => {
  if (!planForm.departmentId) return majors.value
  return majors.value.filter((m: any) => m.departmentId === planForm.departmentId)
})

async function fetch() {
  loading.value = true
  const r = await getTrainingPlans({ page: page.value, size: 10 })
  tableData.value = r.data.records
  total.value = r.data.total
  loading.value = false
}

async function fetchDepartments() { try { const r = await getAllDepartments(); departments.value = r.data || [] } catch {} }
async function fetchGrades() { try { const r = await getAllGrades(); grades.value = r.data || [] } catch {} }
async function fetchMajors() { try { const r = await request.get('/sys/majors/all'); majors.value = r.data || [] } catch {} }

function showPlanDialog() {
  editPlanId.value = null
  Object.assign(planForm, { name: '', departmentId: null, majorId: null, gradeId: null, totalSemesters: 8, description: '', status: 1 })
  planDialogVisible.value = true
}

function onDeptChange(val: number) { planForm.majorId = null }

async function savePlan() {
  if (!planForm.name.trim() || !planForm.majorId || !planForm.gradeId) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (editPlanId.value) {
    await updateTrainingPlan(editPlanId.value, planForm)
  } else {
    await createTrainingPlan(planForm)
  }
  ElMessage.success('保存成功')
  planDialogVisible.value = false
  fetch()
}

async function doDelete(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该培养方案？将同时删除所有课程项。', '提示', { type: 'warning' })
    await deleteTrainingPlan(id)
    ElMessage.success('删除成功')
    fetch()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

async function enterDetail(id: number) {
  const r = await getTrainingPlan(id)
  currentPlan.value = r.data
  detailMode.value = true
  activeSemester.value = '1'
  await refreshItems()
}

function backToList() {
  detailMode.value = false
  currentPlan.value = {}
  fetch()
}

async function refreshItems() {
  if (!currentPlan.value.id) return
  itemsLoading.value = true
  try {
    const r = await getPlanItems(currentPlan.value.id, Number(activeSemester.value))
    semesterItems.value = r.data || []
  } catch { semesterItems.value = [] }
  itemsLoading.value = false
}

function onSemesterChange() {
  refreshItems()
}

function showItemDialog(row?: any) {
  editItemId.value = row?.id || null
  if (row) {
    Object.assign(itemForm, { courseName: row.courseName, courseCode: row.courseCode || '', credit: row.credit, hours: row.hours, isRequired: row.isRequired, sortOrder: row.sortOrder || 0 })
  } else {
    Object.assign(itemForm, { courseName: '', courseCode: '', credit: 0, hours: 0, isRequired: 1, sortOrder: 0 })
  }
  itemDialogVisible.value = true
}

async function saveItem() {
  if (!itemForm.courseName.trim()) { ElMessage.warning('课程名称不能为空'); return }
  const data = { ...itemForm }
  if (editItemId.value) {
    await updatePlanItem(editItemId.value, data)
  } else {
    await addPlanItem(currentPlan.value.id, data)
  }
  ElMessage.success('保存成功')
  itemDialogVisible.value = false
  refreshItems()
}

async function doDeleteItem(id: number) {
  try {
    await ElMessageBox.confirm('确认删除该课程项？', '提示', { type: 'warning' })
    await deletePlanItem(id)
    ElMessage.success('删除成功')
    refreshItems()
  } catch (e: any) {
    if (e !== 'cancel' && e?.message) ElMessage.error(e.message)
  }
}

async function doGenerate() {
  generating.value = true
  try {
    const r = await generateSemester(currentPlan.value.id, Number(activeSemester.value))
    generateResult.value = r.data || { generatedCount: 0 }
    resultVisible.value = true
  } catch (e: any) {
    ElMessage.error(e?.message || '生成失败')
  } finally {
    generating.value = false
  }
}

onMounted(() => { fetch(); fetchDepartments(); fetchMajors(); fetchGrades() })
</script>

<template>
  <div>
    <div class="mb-4">
      <span class="text-lg font-bold text-ink">成绩录入</span>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5 max-w-lg">
      <el-form :model="form" label-width="80px">
        <el-form-item label="学生学号">
          <el-input v-model="form.studentId" placeholder="请输入学生ID" type="number" />
        </el-form-item>
        <el-form-item label="课程ID">
          <el-input v-model="form.courseId" placeholder="请输入课程ID" type="number" />
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="form.semester" placeholder="如 2026-春" />
        </el-form-item>
        <el-form-item label="成绩类型">
          <el-select v-model="form.gradeType" class="w-full">
            <el-option label="百分制" value="百分制" /><el-option label="等级制" value="等级制" />
          </el-select>
        </el-form-item>
        <el-form-item label="分数">
          <el-input v-model="form.score" placeholder="请输入分数" type="number" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="备注（可选）" />
        </el-form-item>
      </el-form>
      <div class="mt-4">
        <button @click="doSubmit" class="px-6 py-2 bg-ink text-white rounded-lg text-sm font-medium hover:bg-steel transition-colors">提交成绩</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { inputGrade } from '@/api/edu'
import { ElMessage } from 'element-plus'
const form = reactive({ studentId: '', courseId: '', semester: '2026-春', gradeType: '百分制', score: '', remark: '' })
async function doSubmit() {
  if (!form.studentId || !form.courseId || !form.semester || !form.score) { ElMessage.warning('请填写完整信息'); return }
  try {
    await inputGrade({ studentId: Number(form.studentId), courseId: Number(form.courseId), semester: form.semester, gradeType: form.gradeType, score: Number(form.score), remark: form.remark })
    ElMessage.success('成绩录入成功')
    form.studentId = ''; form.courseId = ''; form.score = ''; form.remark = ''
  } catch { ElMessage.error('录入失败') }
}
</script>

<template>
  <div>
    <div class="mb-4">
      <span class="text-lg font-bold text-ink">成绩查询</span>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div v-if="grades.length===0" class="text-center text-mist py-10 text-sm">暂无成绩数据</div>
      <div v-for="g in grades" :key="g.id" class="flex items-center justify-between py-3 border-b border-wash last:border-0">
        <div>
          <div class="text-sm font-medium text-ink">{{ g.courseName || '未知课程' }}</div>
          <div class="text-xs text-mist mt-0.5">{{ g.semester }} · {{ g.gradeType || '百分制' }}</div>
        </div>
        <div class="flex items-center gap-3">
          <span class="text-lg font-bold text-ink">{{ g.score }}</span>
          <span class="text-xs text-mist">{{ g.remark || '' }}</span>
        </div>
      </div>
      <div v-if="grades.length>0" class="mt-4 pt-4 border-t border-soft">
        <div class="flex items-center justify-between">
          <span class="text-sm text-steel">GPA (4.0制)</span>
          <span class="text-2xl font-bold text-ink">{{ gpa }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { getMyGrades } from '@/api/edu'
const grades = ref<any[]>([])
const loading = ref(false)
const gpa = computed(() => {
  if (grades.value.length === 0) return '0.00'
  const points = grades.value.reduce((s: number, g: any) => {
    const score = g.score || 0
    if (score >= 90) return s + 4.0
    if (score >= 80) return s + 3.0
    if (score >= 70) return s + 2.0
    if (score >= 60) return s + 1.0
    return s
  }, 0)
  return (points / grades.value.length).toFixed(2)
})
onMounted(async () => { loading.value = true; const r = await getMyGrades(); grades.value = r.data || []; loading.value = false })
</script>

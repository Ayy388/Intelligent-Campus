<template>
  <div>
    <div class="mb-6 flex items-center justify-between">
      <div>
        <h2 class="text-2xl font-bold text-ink m-0">我的选课</h2>
        <p class="text-steel text-sm mt-1">查看已选择的选修课程</p>
      </div>
      <el-button type="primary" @click="fetchData" :loading="loading">刷新</el-button>
    </div>

    <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
      <div
        v-for="(item, idx) in mySelections"
        :key="item.id"
        class="bg-white rounded-2xl border border-gray-100 p-5 transition-all duration-300 hover:shadow-lg hover:-translate-y-0.5 animate__animated animate__fadeInUp"
        :style="{ animationDelay: idx * 0.06 + 's' }"
      >
        <div class="flex items-start justify-between mb-4">
          <div class="flex items-center gap-3">
            <span
              class="w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold text-sm flex-shrink-0"
              :class="statusClass(item).bgClass"
            >
              {{ item.courseName?.charAt(0) || '课' }}
            </span>
            <div>
              <h4 class="text-base font-semibold text-ink m-0 leading-tight">{{ item.courseName }}</h4>
              <p v-if="item.teacherName" class="text-xs text-steel mt-0.5">{{ item.teacherName }}</p>
            </div>
          </div>
          <el-tag
            :type="statusClass(item).tagType"
            size="small"
            class="!rounded-full !px-3 !border-none"
          >
            {{ statusClass(item).label }}
          </el-tag>
        </div>

        <div class="grid grid-cols-2 gap-3 text-sm mb-4">
          <div class="bg-wash rounded-lg p-2.5 text-center">
            <span class="block text-xs text-steel mb-0.5">学分</span>
            <span class="font-semibold text-ink">{{ item.credit ?? '-' }}</span>
          </div>
          <div class="bg-wash rounded-lg p-2.5 text-center">
            <span class="block text-xs text-steel mb-0.5">学期</span>
            <span class="font-semibold text-ink">{{ item.semester || '-' }}</span>
          </div>
        </div>

        <el-button
          type="danger"
          size="small"
          plain
          class="!w-full !rounded-xl"
          :disabled="item.courseStatus === 2"
          @click="doDrop(item)"
        >
          退课
        </el-button>
      </div>
    </div>

    <div
      v-if="!loading && mySelections.length === 0"
      class="flex flex-col items-center justify-center py-24 animate__animated animate__fadeIn"
    >
      <svg class="w-24 h-24 mb-5" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <rect x="3" y="3" width="18" height="18" rx="3" stroke="#CBD5E1" stroke-width="1.5"/>
        <path d="M8 8h8M8 12h8M8 16h5" stroke="#CBD5E1" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
      <p class="text-steel text-base font-medium mb-1.5">暂无选课记录</p>
      <p class="text-mist text-sm">您尚未选择任何选修课，前往在线选课页面选择感兴趣的课程</p>
      <el-button type="primary" class="!mt-4 !rounded-xl" @click="$router.push('/edu/selection')">
        前往选课
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getSelections, dropCourse } from '@/api/edu'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { CourseSelection } from '@/types'

const mySelections = ref<CourseSelection[]>([])
const loading = ref(false)

function statusClass(item: CourseSelection) {
  if (item.courseStatus === 2) return { label: '已确认', tagType: 'success' as const, bgClass: 'bg-gradient-to-br from-emerald-400 to-teal-500' }
  if (item.courseStatus === 3) return { label: '已取消', tagType: 'danger' as const, bgClass: 'bg-gradient-to-br from-red-400 to-rose-500' }
  return { label: '选课中', tagType: 'warning' as const, bgClass: 'bg-gradient-to-br from-amber-400 to-orange-500' }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getSelections()
    mySelections.value = res.data || []
  } catch (e: any) {
    ElMessage.error(e.message || '加载失败')
  } finally {
    loading.value = false
  }
}

async function doDrop(item: CourseSelection) {
  try {
    await ElMessageBox.confirm(`确定要退选「${item.courseName}」吗？`, '确认退课', {
      confirmButtonText: '确认退课',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await dropCourse(item.id)
    ElMessage.success('退课成功')
    fetchData()
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e.message || '退课失败')
    }
  }
}

onMounted(fetchData)
</script>

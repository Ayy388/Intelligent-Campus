<template>
  <div>
    <div class="flex gap-3 mb-4 flex-wrap">
      <button v-for="c in canteens" :key="c.id" @click="selectCanteen(c.id)"
        :class="['px-4 py-2 rounded-lg text-sm font-medium transition-all duration-200 border',
          selectedCanteen === c.id ? 'bg-ink text-white border-ink' : 'bg-white text-ash border-soft hover:border-line']">
        {{ c.name }}
      </button>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div class="flex items-center justify-between mb-4">
        <span class="font-semibold text-ink text-sm">食堂点评</span>
        <button @click="showDialog" class="px-4 py-1.5 bg-ink text-white rounded-lg text-xs font-medium hover:bg-steel transition-colors">写点评</button>
      </div>
      <div v-for="r in reviews" :key="r.id" class="py-3 border-b border-wash last:border-0">
        <div class="flex items-center gap-2 mb-1">
          <span class="text-sm font-medium text-ink">{{ r.userName }}</span>
          <span class="text-xs text-mist">{{ r.createTime?.substring(0,16) }}</span>
          <div class="ml-auto flex items-center gap-1">
            <span v-for="s in 5" :key="s" :class="s <= r.rating ? 'text-ink' : 'text-line'">★</span>
          </div>
        </div>
        <div class="text-sm text-steel">{{ r.content }}</div>
        <div class="flex gap-4 mt-1 text-xs text-mist">
          <span>口味 {{ r.tasteRating }}</span><span>价格 {{ r.priceRating }}</span><span>服务 {{ r.serviceRating }}</span>
        </div>
      </div>
      <el-pagination v-model:current-page="page" :total="total" :page-size="5" layout="prev,pager,next" @current-change="fetchReviews" small />
    </div>
    <el-dialog v-model="dialogVisible" title="写点评" width="450px">
      <el-form :model="form">
        <el-form-item label="食堂"><el-select v-model="form.canteenId" class="w-full"><el-option v-for="c in canteens" :key="c.id" :value="c.id" :label="c.name" /></el-select></el-form-item>
        <el-form-item label="综合评分"><el-rate v-model="form.rating" /></el-form-item>
        <el-form-item label="口味"><el-rate v-model="form.tasteRating" /></el-form-item>
        <el-form-item label="价格"><el-rate v-model="form.priceRating" /></el-form-item>
        <el-form-item label="服务"><el-rate v-model="form.serviceRating" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="form.content" type="textarea" :rows="4" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submitReview">提交</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getCanteens, getCanteenReviews, addCanteenReview } from '@/api/life'
import { ElMessage } from 'element-plus'
const canteens = ref<any[]>([])
const reviews = ref<any[]>([])
const selectedCanteen = ref<number | null>(null)
const page = ref(1)
const total = ref(0)
const dialogVisible = ref(false)
const form = reactive({ canteenId: null as number | null, rating: 3, tasteRating: 3, priceRating: 3, serviceRating: 3, content: '' })
async function fetchCanteens() { const r = await getCanteens(); canteens.value = r.data; if (r.data.length) selectCanteen(r.data[0].id) }
async function selectCanteen(id: number) { selectedCanteen.value = id; page.value = 1; fetchReviews() }
async function fetchReviews() { const r = await getCanteenReviews({ canteenId: selectedCanteen.value, page: page.value, size: 5 }); reviews.value = r.data.records; total.value = r.data.total }
function showDialog() { if (!canteens.value.length) { ElMessage.warning('暂无食堂'); return } form.canteenId = selectedCanteen.value; form.rating = 3; form.tasteRating = 3; form.priceRating = 3; form.serviceRating = 3; form.content = ''; dialogVisible.value = true }
async function submitReview() { await addCanteenReview(form); ElMessage.success('点评成功'); dialogVisible.value = false; fetchReviews() }
onMounted(fetchCanteens)
</script>

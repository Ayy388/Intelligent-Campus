<template>
  <div>
    <div class="bg-white rounded-xl border border-soft p-5 mb-4">
      <div class="text-sm text-ash mb-1">当前余额</div>
      <div class="text-3xl font-bold text-ink mb-4">¥{{ balance.toFixed(2) }}</div>
      <div class="flex gap-3">
        <button v-for="a in amounts" :key="a" @click="doRecharge(a)" class="px-6 py-2 bg-ink text-white rounded-lg text-sm font-medium hover:bg-steel transition-colors">¥{{ a }}</button>
      </div>
    </div>
    <div class="bg-white rounded-xl border border-soft p-5">
      <div class="font-semibold text-ink text-sm mb-3">充值记录</div>
      <div v-for="r in records" :key="r.id" class="flex justify-between py-2 border-b border-wash last:border-0 text-sm">
        <span class="text-ash">{{ r.createTime?.substring(0,16) }}</span>
        <span class="text-ink font-medium">+¥{{ r.amount }}</span>
      </div>
      <el-pagination v-model:current-page="page" :total="total" :page-size="10" layout="prev,pager,next" @current-change="fetch" small />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getRecharges, recharge } from '@/api/life'
import { ElMessage } from 'element-plus'
const balance = ref(0)
const records = ref<any[]>([])
const page = ref(1)
const total = ref(0)
const amounts = [10, 50, 100, 200]
async function fetch() { const r = await getRecharges({ page: page.value, size: 10 }); records.value = r.data.records; total.value = r.data.total; if (r.data.records.length && page.value === 1) balance.value = r.data.records[0].balance }
async function doRecharge(amount: number) { await recharge({ amount }); ElMessage.success('充值成功'); page.value = 1; fetch() }
onMounted(fetch)
</script>

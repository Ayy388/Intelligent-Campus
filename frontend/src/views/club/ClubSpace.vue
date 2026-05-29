<template>
  <div class="animate__animated animate__fadeInUp">
    <div class="flex items-center mb-5">
      <button @click="$router.back()" class="mr-3 text-steel hover:text-ink">
        <el-icon size="20"><ArrowLeft /></el-icon>
      </button>
      <h2 class="text-lg font-bold text-ink">{{ club?.name || '社团空间' }}</h2>
    </div>

    <div v-if="club" class="grid grid-cols-1 lg:grid-cols-3 gap-5">
      <div class="lg:col-span-2 space-y-5">
        <div class="bg-white rounded-xl border border-soft p-5">
          <div class="flex items-center mb-4">
            <div v-if="club.logo" class="w-16 h-16 rounded-lg bg-soft flex items-center justify-center mr-4 overflow-hidden">
              <img :src="club.logo" class="w-full h-full object-cover" />
            </div>
            <div v-else class="w-16 h-16 rounded-lg bg-ink/10 flex items-center justify-center mr-4">
              <el-icon size="32" class="text-ink/50"><OfficeBuilding /></el-icon>
            </div>
            <div>
              <h3 class="text-xl font-semibold text-ink">{{ club.name }}</h3>
              <p class="text-sm text-ash mt-1">{{ club.description }}</p>
            </div>
          </div>
          <div class="flex gap-6 text-sm text-steel">
            <span><el-icon class="mr-1"><User /></el-icon> {{ club.memberCount || 0 }} 名成员</span>
            <span v-if="club.presidentName"><el-icon class="mr-1"><UserFilled /></el-icon> 社长: {{ club.presidentName }}</span>
            <span v-if="club.advisorName"><el-icon class="mr-1"><Document /></el-icon> 指导老师: {{ club.advisorName }}</span>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-soft p-5">
          <div class="flex items-center justify-between mb-4">
            <h4 class="text-sm font-semibold text-ink">社团成员</h4>
          </div>
          <div v-if="members.length === 0" class="text-center text-mist py-8 text-sm">
            暂无成员
          </div>
          <div v-else class="space-y-3">
            <div v-for="m in members" :key="m.id" class="flex items-center justify-between py-2 border-b border-wash last:border-0">
              <div class="flex items-center">
                <div class="w-10 h-10 rounded-full bg-ink/10 flex items-center justify-center mr-3">
                  <el-icon class="text-ink/50"><User /></el-icon>
                </div>
                <div>
                  <div class="text-sm font-medium text-ink">{{ m.userName || '未知用户' }}</div>
                  <div class="text-xs text-mist">
                    <el-tag size="small" :type="m.role==='president'?'danger':m.role==='vice_president'?'warning':''" class="mr-2">
                      {{ m.role==='president'?'社长':m.role==='vice_president'?'副社长':'成员' }}
                    </el-tag>
                    <span v-if="m.applyTime">加入时间: {{ m.applyTime.substring(0,10) }}</span>
                  </div>
                </div>
              </div>
              <el-tag size="small" :type="m.status===1?'success':'warning'">
                {{ m.status===1?'已通过':'待审核' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>

      <div class="space-y-5">
        <div class="bg-white rounded-xl border border-soft p-5">
          <h4 class="text-sm font-semibold text-ink mb-4">社团信息</h4>
          <div class="space-y-3 text-sm">
            <div class="flex justify-between">
              <span class="text-ash">社团状态</span>
              <el-tag size="small" :type="club.status===1?'success':club.status===2?'danger':'warning'">
                {{ club.status===1?'正常':club.status===2?'已解散':'待审核' }}
              </el-tag>
            </div>
            <div class="flex justify-between">
              <span class="text-ash">创建时间</span>
              <span class="text-steel">{{ club.createTime?.substring(0,10) || '-' }}</span>
            </div>
          </div>
        </div>

        <div class="bg-white rounded-xl border border-soft p-5">
          <h4 class="text-sm font-semibold text-ink mb-4">我的身份</h4>
          <div v-if="myMember" class="text-sm">
            <el-tag size="small" :type="myMember.role==='president'?'danger':myMember.role==='vice_president'?'warning':''">
              {{ myMember.role==='president'?'社长':myMember.role==='vice_president'?'副社长':'成员' }}
            </el-tag>
            <div class="text-mist mt-2">
              状态: <span class="text-steel">{{ myMember.status===1?'已通过':'待审核' }}</span>
            </div>
          </div>
          <div v-else class="text-sm text-mist">
            你还不是该社团成员
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getClub, getMembers, getMyMemberships } from '@/api/club'
import { useUserStore } from '@/store/user'
import { ArrowLeft, OfficeBuilding, User, UserFilled, Document } from '@element-plus/icons-vue'

const userStore = useUserStore()
const route = useRoute()
const club = ref<any>(null)
const members = ref<any[]>([])
const myMember = ref<any>(null)

async function fetchData() {
  const clubId = route.params.id as string
  try {
    const clubRes = await getClub(parseInt(clubId))
    club.value = clubRes.data
    
    const membersRes = await getMembers(parseInt(clubId))
    members.value = membersRes.data || []
    
    const myRes = await getMyMemberships()
    const myMemberships = myRes.data || []
    myMember.value = myMemberships.find((m: any) => m.clubId === parseInt(clubId))
  } catch (e) {
    console.error('获取数据失败', e)
  }
}

onMounted(fetchData)
</script>

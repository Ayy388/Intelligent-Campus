<template>
  <div class="h-full flex">
    <aside
      :style="{ width: appStore.sidebarCollapsed ? '64px' : '240px' }"
      class="flex-shrink-0 bg-gradient-to-b from-ink to-slate transition-[width] duration-300 overflow-hidden"
    >
      <AppSidebar />
    </aside>
    <div class="flex-1 flex flex-col min-w-0">
      <header class="h-[60px] flex-shrink-0 bg-white/95 backdrop-blur-md border-b border-soft px-5 flex items-center z-10">
        <AppHeader />
      </header>
      <main class="flex-1 overflow-auto bg-cloud p-6">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'
import { useAppStore } from '@/store/app'
const appStore = useAppStore()
</script>

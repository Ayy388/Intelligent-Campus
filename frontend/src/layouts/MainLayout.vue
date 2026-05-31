<template>
  <div class="h-full flex">
    <aside
      :style="{ width: appStore.sidebarCollapsed ? '64px' : '240px' }"
      class="flex-shrink-0 bg-gradient-to-b from-ink to-slate transition-[width] duration-300 overflow-hidden shadow-[2px_0_12px_rgba(0,0,0,0.08)]"
    >
      <AppSidebar />
    </aside>
    <div class="flex-1 flex flex-col min-w-0">
      <header class="h-[60px] flex-shrink-0 bg-white/95 backdrop-blur-md border-b border-soft px-5 flex items-center z-10">
        <AppHeader />
      </header>
      <main class="flex-1 overflow-auto bg-cloud p-6 noise-bg">
        <router-view v-slot="{ Component }">
          <transition name="page-slide" mode="out-in">
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

<style scoped>
.noise-bg {
  background-image: 
    radial-gradient(ellipse at 20% 50%, rgba(59, 130, 246, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(139, 92, 246, 0.03) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, rgba(236, 72, 153, 0.02) 0%, transparent 50%);
}
.page-slide-enter-active,
.page-slide-leave-active {
  transition: opacity 0.15s ease;
}
.page-slide-enter-from,
.page-slide-leave-to {
  opacity: 0;
}
main::-webkit-scrollbar {
  width: 6px;
}
main::-webkit-scrollbar-track {
  background: transparent;
}
main::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 3px;
}
main::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}
</style>

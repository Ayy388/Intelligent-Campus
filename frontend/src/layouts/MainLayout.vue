<template>
  <div class="layout-container">
    <aside
      :style="{ width: appStore.sidebarCollapsed ? '64px' : '240px' }"
      class="layout-sidebar"
    >
      <AppSidebar />
    </aside>
    <div class="layout-main">
      <header class="layout-header">
        <AppHeader />
      </header>
      <main class="layout-content">
        <div class="content-wrapper">
          <router-view />
        </div>
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
.layout-container {
  height: 100%;
  display: flex;
}

.layout-sidebar {
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  background: linear-gradient(180deg, #0f0f23 0%, #1a0a2e 50%, #0d0d1a 100%);
  box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.layout-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.layout-header {
  height: 60px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
  padding: 0 24px;
  display: flex;
  align-items: center;
  z-index: 50;
  position: relative;
}

.layout-header::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(99, 102, 241, 0.08), transparent);
}

.layout-content {
  flex: 1;
  overflow: auto;
  background:
    radial-gradient(ellipse at 20% 50%, rgba(99, 102, 241, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 80% 20%, rgba(139, 92, 246, 0.04) 0%, transparent 50%),
    radial-gradient(ellipse at 50% 80%, rgba(236, 72, 153, 0.03) 0%, transparent 50%),
    #f8f9fc;
  padding: 28px 32px;
}

.content-wrapper {
  max-width: 1400px;
  margin: 0 auto;
  min-height: 100%;
}

.layout-content::-webkit-scrollbar {
  width: 6px;
}

.layout-content::-webkit-scrollbar-track {
  background: transparent;
}

.layout-content::-webkit-scrollbar-thumb {
  background: rgba(209, 213, 219, 0.6);
  border-radius: 3px;
}

.layout-content::-webkit-scrollbar-thumb:hover {
  background: rgba(156, 163, 175, 0.8);
}
</style>
import { describe, it, expect, beforeEach } from 'vitest';
import { setActivePinia, createPinia } from 'pinia';
import { useAppStore } from '@/store/app';
describe('appStore', () => {
    beforeEach(() => {
        setActivePinia(createPinia());
    });
    it('sidebarCollapsed should start as false', () => {
        const store = useAppStore();
        expect(store.sidebarCollapsed).toBe(false);
    });
    it('toggleSidebar should flip collapsed state', () => {
        const store = useAppStore();
        store.toggleSidebar();
        expect(store.sidebarCollapsed).toBe(true);
        store.toggleSidebar();
        expect(store.sidebarCollapsed).toBe(false);
    });
});

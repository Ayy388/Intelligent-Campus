import { describe, it, expect, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import LoginView from '@/views/login/LoginView.vue';
// Mock router
vi.mock('vue-router', () => ({
    useRouter: () => ({ push: vi.fn() }),
    createRouter: vi.fn(() => ({ push: vi.fn(), beforeEach: vi.fn(), currentRoute: { value: {} } })),
    createWebHistory: vi.fn(() => ({}))
}));
// Mock @element-plus/icons-vue
vi.mock('@element-plus/icons-vue', () => ({
    User: {},
    Lock: {}
}));
describe('LoginView', () => {
    function createWrapper() {
        return mount(LoginView, {
            global: {
                plugins: [createPinia(), ElementPlus]
            }
        });
    }
    it('should render username and password inputs', () => {
        const wrapper = createWrapper();
        const inputs = wrapper.findAll('input');
        expect(inputs.length).toBeGreaterThanOrEqual(2);
        // Check for placeholder text
        const formItems = wrapper.findAllComponents({ name: 'ElInput' });
        expect(formItems.length).toBeGreaterThanOrEqual(2);
    });
    it('should have a login button', () => {
        const wrapper = createWrapper();
        const button = wrapper.find('button');
        expect(button.exists()).toBe(true);
        expect(button.text()).toContain('登');
    });
});

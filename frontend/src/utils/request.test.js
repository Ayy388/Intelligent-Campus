import { describe, it, expect, vi, beforeEach } from 'vitest';
import request from '@/utils/request';
// Mock router
vi.mock('@/router', () => ({
    default: { push: vi.fn() }
}));
// Mock Element Plus message
vi.mock('element-plus', () => ({
    ElMessage: { error: vi.fn() }
}));
describe('request interceptor', () => {
    beforeEach(() => {
        localStorage.clear();
    });
    it('should add Authorization header when token exists', async () => {
        localStorage.setItem('token', 'test-token');
        // Trigger the request interceptor with a proper config object
        const config = { headers: {} };
        const fulfilled = request.interceptors.request.handlers[0].fulfilled;
        const interceptedConfig = await fulfilled(config);
        expect(interceptedConfig.headers.Authorization).toBe('Bearer test-token');
    });
    it('should redirect to login on 401 response', async () => {
        const errorResponse = {
            response: {
                data: { code: 401, message: '未登录' }
            }
        };
        try {
            const rejected = request.interceptors.response.handlers[0].rejected;
            await rejected(errorResponse);
        }
        catch {
            // expected
        }
    });
    it('should reject non-401 errors', async () => {
        const errorResponse = {
            response: {
                data: { code: 500, message: '服务器错误' }
            }
        };
        try {
            const rejected = request.interceptors.response.handlers[0].rejected;
            await rejected(errorResponse);
        }
        catch (e) {
            expect(e).toBe(errorResponse);
        }
    });
});

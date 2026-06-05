import { describe, it, expect, vi } from 'vitest';
const mockRequest = vi.hoisted(() => ({
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn()
}));
vi.mock('@/utils/request', () => ({
    default: mockRequest
}));
import { selectCourse, dropCourse, enrollCourse } from '@/api/edu';
describe('edu API', () => {
    it('selectCourse should call POST with correct params', async () => {
        mockRequest.post.mockResolvedValue({ data: null });
        await selectCourse(1, '2025-2026-2');
        expect(mockRequest.post).toHaveBeenCalledWith('/edu/selections', null, {
            params: { courseId: 1, semester: '2025-2026-2' }
        });
    });
    it('dropCourse should call DELETE with selection id', async () => {
        mockRequest.delete.mockResolvedValue({ data: null });
        await dropCourse(42);
        expect(mockRequest.delete).toHaveBeenCalledWith('/edu/selections/42');
    });
    it('enrollCourse should call POST with course id', async () => {
        mockRequest.post.mockResolvedValue({ data: null });
        await enrollCourse(5);
        expect(mockRequest.post).toHaveBeenCalledWith('/edu/courses/5/enroll');
    });
});

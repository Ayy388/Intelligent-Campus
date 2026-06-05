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
import { dropCourse, enrollCourse } from '@/api/edu';
describe('edu API', () => {
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

import request from '@/utils/request'

export function getTodos(params: { page?: number; size?: number; completed?: number }) {
  return request.get('/todos', { params })
}

export function createTodo(data: { title: string; priority?: number; dueDate?: string }) {
  return request.post('/todos', data)
}

export function updateTodo(id: number, data: { title?: string; completed?: number; priority?: number; dueDate?: string; sortOrder?: number }) {
  return request.put(`/todos/${id}`, data)
}

export function deleteTodo(id: number) {
  return request.delete(`/todos/${id}`)
}

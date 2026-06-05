export interface TodoItem {
  id: number
  userId: number
  title: string
  description?: string
  priority: number
  dueDate?: string
  completed: number
  sortOrder?: number
  createTime?: string
  updateTime?: string
}
package com.campus.module.todo.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.todo.entity.Todo;

public interface TodoService {
    Page<Todo> listTodos(Long userId, int page, int size, Integer completed);
    Todo createTodo(Long userId, Todo todo);
    Todo updateTodo(Long id, Long userId, Todo todo);
    void deleteTodo(Long id, Long userId);
}

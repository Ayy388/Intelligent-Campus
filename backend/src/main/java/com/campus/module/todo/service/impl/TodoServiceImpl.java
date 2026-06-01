package com.campus.module.todo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.module.todo.entity.Todo;
import com.campus.module.todo.mapper.TodoMapper;
import com.campus.module.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl extends ServiceImpl<TodoMapper, Todo> implements TodoService {
    private final TodoMapper todoMapper;

    @Override
    public Page<Todo> listTodos(Long userId, int page, int size, Integer completed) {
        LambdaQueryWrapper<Todo> w = new LambdaQueryWrapper<Todo>()
            .eq(Todo::getUserId, userId);
        if (completed != null) {
            w.eq(Todo::getCompleted, completed);
        }
        w.orderByAsc(Todo::getSortOrder).orderByDesc(Todo::getCreateTime);
        return todoMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public Todo createTodo(Long userId, Todo todo) {
        todo.setId(null);
        todo.setUserId(userId);
        if (todo.getCompleted() == null) todo.setCompleted(0);
        if (todo.getPriority() == null) todo.setPriority(1);
        if (todo.getSortOrder() == null) todo.setSortOrder(0);
        todo.setCreateTime(LocalDateTime.now());
        todo.setUpdateTime(LocalDateTime.now());
        todoMapper.insert(todo);
        return todo;
    }

    @Override
    public Todo updateTodo(Long id, Long userId, Todo updated) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId))
            throw new BusinessException("待办事项不存在");
        if (updated.getTitle() != null) todo.setTitle(updated.getTitle());
        if (updated.getCompleted() != null) todo.setCompleted(updated.getCompleted());
        if (updated.getPriority() != null) todo.setPriority(updated.getPriority());
        if (updated.getDueDate() != null) todo.setDueDate(updated.getDueDate());
        if (updated.getSortOrder() != null) todo.setSortOrder(updated.getSortOrder());
        todo.setUpdateTime(LocalDateTime.now());
        todoMapper.updateById(todo);
        return todo;
    }

    @Override
    public void deleteTodo(Long id, Long userId) {
        Todo todo = todoMapper.selectById(id);
        if (todo == null || !todo.getUserId().equals(userId))
            throw new BusinessException("待办事项不存在");
        todoMapper.deleteById(id);
    }
}

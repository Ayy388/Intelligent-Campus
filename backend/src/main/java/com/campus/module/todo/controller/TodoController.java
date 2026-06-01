package com.campus.module.todo.controller;

import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.todo.entity.Todo;
import com.campus.module.todo.service.TodoService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {
    private final TodoService todoService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @GetMapping
    public Result<PageResult<Todo>> list(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) Integer completed) {
        var p = todoService.listTodos(getUserId(auth), page, size, completed);
        PageResult<Todo> pr = new PageResult<>();
        pr.setRecords(p.getRecords());
        pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent());
        pr.setSize(p.getSize());
        pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @PostMapping
    public Result<Todo> create(Authentication auth, @RequestBody Todo todo) {
        return Result.ok(todoService.createTodo(getUserId(auth), todo));
    }

    @PutMapping("/{id}")
    public Result<Todo> update(Authentication auth, @PathVariable Long id, @RequestBody Todo todo) {
        return Result.ok(todoService.updateTodo(id, getUserId(auth), todo));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(Authentication auth, @PathVariable Long id) {
        todoService.deleteTodo(id, getUserId(auth));
        return Result.ok();
    }
}

package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysDepartment;
import com.campus.module.sys.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/departments")
@RequiredArgsConstructor
public class SysDepartmentController {
    private final SysDepartmentService service;

    @GetMapping
    public Result<PageResult<SysDepartment>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Page<SysDepartment> p = service.page(page, size, keyword);
        PageResult<SysDepartment> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysDepartment>> all() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysDepartment> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysDepartment dept) {
        service.save(dept);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDepartment dept) {
        dept.setId(id);
        service.update(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
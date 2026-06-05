package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysDepartment;
import com.campus.module.sys.service.SysDepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/departments")
@RequiredArgsConstructor
@Tag(name = "系统管理")
public class SysDepartmentController {
    private final SysDepartmentService service;

    @Operation(summary = "分页查询院系列表")
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

    @Operation(summary = "获取所有院系列表")
    @GetMapping("/all")
    public Result<List<SysDepartment>> all() {
        return Result.ok(service.listAll());
    }

    @Operation(summary = "获取院系详情")
    @GetMapping("/{id}")
    public Result<SysDepartment> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @Operation(summary = "添加院系")
    @PostMapping
    public Result<Void> save(@RequestBody SysDepartment dept) {
        service.save(dept);
        return Result.ok();
    }

    @Operation(summary = "更新院系信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDepartment dept) {
        dept.setId(id);
        service.update(dept);
        return Result.ok();
    }

    @Operation(summary = "删除院系")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.service.SysMajorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/majors")
@RequiredArgsConstructor
@Tag(name = "系统管理")
public class SysMajorController {
    private final SysMajorService service;

    @Operation(summary = "分页查询专业列表")
    @GetMapping
    public Result<PageResult<SysMajor>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Page<SysMajor> p = service.page(page, size, keyword);
        PageResult<SysMajor> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @Operation(summary = "获取所有专业列表")
    @GetMapping("/all")
    public Result<List<SysMajor>> all(@RequestParam(required = false) Long deptId) {
        return Result.ok(service.listAll(deptId));
    }

    @Operation(summary = "获取专业详情")
    @GetMapping("/{id}")
    public Result<SysMajor> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @Operation(summary = "添加专业")
    @PostMapping
    public Result<Void> save(@RequestBody SysMajor major) {
        service.save(major);
        return Result.ok();
    }

    @Operation(summary = "更新专业信息")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMajor major) {
        major.setId(id);
        service.update(major);
        return Result.ok();
    }

    @Operation(summary = "删除专业")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
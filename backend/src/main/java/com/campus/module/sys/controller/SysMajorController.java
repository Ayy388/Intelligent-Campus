package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/majors")
@RequiredArgsConstructor
public class SysMajorController {
    private final SysMajorService service;

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

    @GetMapping("/all")
    public Result<List<SysMajor>> all(@RequestParam(required = false) Long deptId) {
        return Result.ok(service.listAll(deptId));
    }

    @GetMapping("/{id}")
    public Result<SysMajor> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysMajor major) {
        service.save(major);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMajor major) {
        major.setId(id);
        service.update(major);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
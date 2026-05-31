package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.service.SysGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/grades")
@RequiredArgsConstructor
public class SysGradeController {
    private final SysGradeService service;

    @GetMapping
    public Result<PageResult<SysGrade>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SysGrade> p = service.page(page, size);
        PageResult<SysGrade> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysGrade>> all() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysGrade> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysGrade grade) {
        service.save(grade);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysGrade grade) {
        grade.setId(id);
        service.update(grade);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.service.SysClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sys/classes")
@RequiredArgsConstructor
public class SysClassController {
    private final SysClassService sysClassService;

    @GetMapping
    public Result<PageResult<SysClass>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Page<SysClass> p = sysClassService.page(page, size);
        PageResult<SysClass> pr = new PageResult<>();
        pr.setRecords(p.getRecords());
        pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent());
        pr.setSize(p.getSize());
        pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysClass>> all() {
        return Result.ok(sysClassService.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysClass> get(@PathVariable Long id) {
        return Result.ok(sysClassService.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysClass c) {
        sysClassService.save(c);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysClass c) {
        sysClassService.update(id, c);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysClassService.delete(id);
        return Result.ok();
    }
}
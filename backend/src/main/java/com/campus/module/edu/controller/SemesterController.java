package com.campus.module.edu.controller;

import com.campus.common.Result;
import com.campus.module.edu.entity.Semester;
import com.campus.module.edu.service.SemesterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edu/semesters")
@RequiredArgsConstructor
@Tag(name = "教务管理")
public class SemesterController {
    private final SemesterService semesterService;

    @Operation(summary = "获取学期列表")
    @GetMapping
    public Result<List<Semester>> list(@RequestParam(required = false) String status) {
        if ("active".equals(status)) {
            return Result.ok(semesterService.getActiveList());
        }
        return Result.ok(semesterService.list());
    }

    @Operation(summary = "根据 ID 获取学期详情")
    @GetMapping("/{id}")
    public Result<Semester> getOne(@PathVariable Long id) {
        return Result.ok(semesterService.getById(id));
    }

    @Operation(summary = "添加学期")
    @PostMapping
    public Result<Semester> add(@RequestBody Semester semester) {
        semesterService.save(semester);
        return Result.ok(semester);
    }

    @Operation(summary = "更新学期信息")
    @PutMapping("/{id}")
    public Result<Semester> update(@PathVariable Long id, @RequestBody Semester semester) {
        semester.setId(id);
        semesterService.updateById(semester);
        return Result.ok(semester);
    }

    @Operation(summary = "删除学期")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        semesterService.removeById(id);
        return Result.ok();
    }
}
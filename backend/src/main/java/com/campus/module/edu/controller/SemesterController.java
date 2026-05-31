package com.campus.module.edu.controller;

import com.campus.common.Result;
import com.campus.module.edu.entity.Semester;
import com.campus.module.edu.service.SemesterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edu/semesters")
@RequiredArgsConstructor
public class SemesterController {
    private final SemesterService semesterService;

    @GetMapping
    public Result<List<Semester>> list(@RequestParam(required = false) String status) {
        if ("active".equals(status)) {
            return Result.ok(semesterService.getActiveList());
        }
        return Result.ok(semesterService.list());
    }

    @GetMapping("/{id}")
    public Result<Semester> getOne(@PathVariable Long id) {
        return Result.ok(semesterService.getById(id));
    }

    @PostMapping
    public Result<Semester> add(@RequestBody Semester semester) {
        semesterService.save(semester);
        return Result.ok(semester);
    }

    @PutMapping("/{id}")
    public Result<Semester> update(@PathVariable Long id, @RequestBody Semester semester) {
        semester.setId(id);
        semesterService.updateById(semester);
        return Result.ok(semester);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        semesterService.removeById(id);
        return Result.ok();
    }
}
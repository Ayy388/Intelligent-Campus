package com.campus.module.edu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.service.GradeService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/edu/grades")
@RequiredArgsConstructor
@Tag(name = "教务管理")
public class GradeController {
    private final GradeService gradeService;

    @Operation(summary = "分页查询学生成绩")
    @GetMapping
    public Result<PageResult<Grade>> list(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String semester) {
        Long studentId = getUserId(auth);
        Page<Grade> p = gradeService.getStudentGradesPage(studentId, page, size, semester);
        PageResult<Grade> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @Operation(summary = "查询学生全部成绩")
    @GetMapping("/all")
    public Result<List<Grade>> allGrades(Authentication auth,
            @RequestParam(required = false) String semester) {
        Long studentId = getUserId(auth);
        return Result.ok(gradeService.getStudentAllGrades(studentId, semester));
    }

    @Operation(summary = "录入成绩")
    @PostMapping
    public Result<Void> inputGrade(@RequestBody Grade grade, Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        grade.setTeacherId(Long.parseLong(claims.getSubject()));
        gradeService.inputGrade(grade);
        return Result.ok();
    }

    @Operation(summary = "更新成绩")
    @PutMapping("/{id}")
    public Result<Void> updateGrade(@PathVariable Long id, @RequestBody Grade grade, Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        grade.setTeacherId(Long.parseLong(claims.getSubject()));
        gradeService.updateGrade(id, grade);
        return Result.ok();
    }

    @Operation(summary = "删除成绩")
    @DeleteMapping("/{id}")
    public Result<Void> deleteGrade(@PathVariable Long id) {
        gradeService.deleteGrade(id);
        return Result.ok();
    }

    @Operation(summary = "分页查询某课程学生成绩")
    @GetMapping("/course/{courseId}")
    public Result<PageResult<Grade>> courseGrades(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        Page<Grade> p = gradeService.getCourseGradesPage(courseId, page, size);
        PageResult<Grade> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @Operation(summary = "获取某课程全部学生成绩")
    @GetMapping("/course/{courseId}/all")
    public Result<List<Grade>> courseGradesAll(@PathVariable Long courseId) {
        return Result.ok(gradeService.getCourseGrades(courseId));
    }

    @Operation(summary = "获取课程成绩统计")
    @GetMapping("/course/{courseId}/statistics")
    public Result<Map<String, Object>> courseStatistics(@PathVariable Long courseId) {
        return Result.ok(gradeService.getCourseStatistics(courseId));
    }

    @Operation(summary = "获取学生成绩单")
    @GetMapping("/transcript")
    public Result<Map<String, Object>> transcript(Authentication auth) {
        Long studentId = getUserId(auth);
        return Result.ok(gradeService.getStudentTranscript(studentId));
    }

    @Operation(summary = "获取教师授课成绩状态")
    @GetMapping("/teacher/status")
    public Result<List<Map<String, Object>>> teacherStatus(Authentication auth,
            @RequestParam(required = false) String semester) {
        Claims claims = (Claims) auth.getDetails();
        Long teacherId = Long.parseLong(claims.getSubject());
        return Result.ok(gradeService.getTeacherStatus(teacherId, semester));
    }

    private Long getUserId(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Long.parseLong(claims.getSubject());
    }
}
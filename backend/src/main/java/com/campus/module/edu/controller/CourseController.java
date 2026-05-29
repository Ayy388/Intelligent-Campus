package com.campus.module.edu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.service.CourseService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/edu")
@RequiredArgsConstructor
public class CourseController {
    private final CourseService courseService;

    @GetMapping("/courses")
    public Result<PageResult<Course>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String semester) {
        Page<Course> p = courseService.pageWithTeacher(page, size, keyword, semester);
        PageResult<Course> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/courses/{id}")
    public Result<Course> getOne(@PathVariable Long id) { return Result.ok(courseService.getById(id)); }

    @PostMapping("/courses")
    public Result<Course> add(@RequestBody Course c) { courseService.save(c); return Result.ok(c); }

    @PutMapping("/courses/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course c) {
        c.setId(id); courseService.updateById(c); return Result.ok(c);
    }

    @DeleteMapping("/courses/{id}")
    public Result<Void> delete(@PathVariable Long id) { courseService.removeById(id); return Result.ok(); }

    @GetMapping("/selections")
    public Result<List<CourseSelection>> mySelections(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Result.ok(courseService.getMySelections(Long.parseLong(claims.getSubject())));
    }

    @PostMapping("/selections")
    public Result<CourseSelection> select(Authentication auth,
            @RequestParam Long courseId, @RequestParam String semester) {
        Claims claims = (Claims) auth.getDetails();
        return Result.ok(courseService.selectCourse(Long.parseLong(claims.getSubject()), courseId, semester));
    }

    @DeleteMapping("/selections/{id}")
    public Result<Void> drop(Authentication auth, @PathVariable Long id) {
        Claims claims = (Claims) auth.getDetails();
        courseService.dropCourse(id, Long.parseLong(claims.getSubject()));
        return Result.ok();
    }

    @GetMapping("/grades")
    public Result<List<Grade>> grades(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Result.ok(courseService.getStudentGrades(Long.parseLong(claims.getSubject())));
    }

    @PostMapping("/grades")
    public Result<Void> inputGrade(@RequestBody Grade grade, Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        grade.setTeacherId(Long.parseLong(claims.getSubject()));
        courseService.inputGrade(grade);
        return Result.ok();
    }

    @GetMapping("/grades/course/{courseId}")
    public Result<List<Grade>> courseGrades(@PathVariable Long courseId) {
        return Result.ok(courseService.getCourseGrades(courseId));
    }

    @GetMapping("/schedule")
    public Result<List<Course>> schedule(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        return Result.ok(courseService.getMySchedule(userId, role));
    }
}

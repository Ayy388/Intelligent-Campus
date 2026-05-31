package com.campus.module.edu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseClass;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.service.CourseService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

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

    @GetMapping("/courses/available")
    public Result<List<Course>> availableCourses(Authentication auth) {
        return Result.ok(courseService.getAvailableCourses(getUserId(auth)));
    }

    @GetMapping("/courses/{id}")
    public Result<Course> get(@PathVariable Long id) { return Result.ok(courseService.getById(id)); }

    @PostMapping("/courses")
    public Result<Course> add(@RequestBody Course c) {
        Course existing = courseService.lambdaQuery()
            .eq(Course::getCourseCode, c.getCourseCode()).one();
        if (existing != null) {
            return Result.error(400, "课程编号 '" + c.getCourseCode() + "' 已存在");
        }
        courseService.save(c);
        return Result.ok(c);
    }

    @PutMapping("/courses/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course c) {
        c.setId(id); courseService.updateById(c); return Result.ok(c);
    }

    @DeleteMapping("/courses/{id}")
    public Result<Void> delete(@PathVariable Long id) { courseService.deleteCourse(id); return Result.ok(); }

    @PostMapping("/courses/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        courseService.confirmCourse(id, Long.parseLong(claims.getSubject()));
        return Result.ok();
    }

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

    @GetMapping("/selections/course/{courseId}")
    public Result<List<CourseSelection>> courseStudents(@PathVariable Long courseId) {
        return Result.ok(courseService.getCourseStudents(courseId));
    }

    @GetMapping("/courses/teacher")
    public Result<List<Course>> teacherCourses(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Result.ok(courseService.getTeacherCourses(Long.parseLong(claims.getSubject())));
    }

    @GetMapping("/schedule")
    public Result<List<Course>> schedule(Authentication auth,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer week) {
        Claims claims = (Claims) auth.getDetails();
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        return Result.ok(courseService.getMySchedule(userId, role, semester, week));
    }
    
    @PostMapping("/courses/import")
    public Result<String> importCourses(@RequestParam("file") MultipartFile file) {
        try {
            // 简单的CSV解析逻辑
            String content = new String(file.getBytes());
            String[] lines = content.split("\n");
            List<Course> courses = new ArrayList<>();
            
            // 跳过表头
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i].trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(",");
                if (parts.length >= 8) {
                    Course course = new Course();
                    course.setCourseCode(parts[0].trim());
                    course.setCourseName(parts[1].trim());
                    course.setCredit(BigDecimal.valueOf(Double.parseDouble(parts[2].trim())));
                    course.setHours(Integer.parseInt(parts[3].trim()));
                    course.setSemester(parts[4].trim());
                    course.setClassroom(parts[5].trim());
                    course.setStartWeek(Integer.parseInt(parts[6].trim()));
                    course.setEndWeek(Integer.parseInt(parts[7].trim()));
                    if (parts.length > 8) {
                        course.setCapacity(Integer.parseInt(parts[8].trim()));
                    }
                    if (parts.length > 9) {
                        course.setDescription(parts[9].trim());
                    }
                    course.setStatus(0);
                    courses.add(course);
                }
            }
            
            // 保存课程
            for (Course course : courses) {
                courseService.save(course);
            }
            
            return Result.ok("成功导入 " + courses.size() + " 门课程");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/assign-classes")
    public Result<Void> assignClasses(@PathVariable Long id, @RequestBody List<Long> classIds) {
        courseService.assignRequiredCourse(id, classIds);
        return Result.ok();
    }

    @GetMapping("/{id}/classes")
    public Result<List<CourseClass>> getClasses(@PathVariable Long id) {
        return Result.ok(courseService.getCourseClasses(id));
    }

    @PutMapping("/{id}/classes")
    public Result<Void> setClasses(@PathVariable Long id, @RequestBody List<CourseClass> classes) {
        courseService.setCourseClasses(id, classes);
        return Result.ok();
    }

    @PostMapping("/{id}/enroll")
    public Result<Void> enroll(@PathVariable Long id, Authentication auth) {
        courseService.enrollElective(id, getUserId(auth));
        return Result.ok();
    }

    @PostMapping("/{id}/confirm-opening")
    public Result<Void> confirmOpening(@PathVariable Long id) {
        courseService.confirmCourse(id);
        return Result.ok();
    }

    @PostMapping("/{id}/cancel-opening")
    public Result<Void> cancelOpening(@PathVariable Long id) {
        courseService.cancelCourse(id);
        return Result.ok();
    }

    private Long getUserId(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Long.parseLong(claims.getSubject());
    }
}

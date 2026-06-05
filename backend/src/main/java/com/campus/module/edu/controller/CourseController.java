package com.campus.module.edu.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseClass;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.service.CourseService;
import com.campus.module.edu.service.ScheduleConflictDetector;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "教务管理", description = "课程、选课、排课等教务功能")
public class CourseController {
    private final CourseService courseService;

    @Operation(summary = "分页查询课程列表")
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

    @Operation(summary = "获取可选的课程列表")
    @GetMapping("/courses/available")
    public Result<List<Course>> availableCourses(Authentication auth) {
        return Result.ok(courseService.getAvailableCourses(getUserId(auth)));
    }

    @Operation(summary = "根据 ID 获取课程详情")
    @GetMapping("/courses/{id}")
    public Result<Course> get(@PathVariable Long id) { return Result.ok(courseService.getById(id)); }

    @Operation(summary = "添加新课程")
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

    @Operation(summary = "更新课程信息")
    @PutMapping("/courses/{id}")
    public Result<Course> update(@PathVariable Long id, @RequestBody Course c) {
        c.setId(id); courseService.updateById(c); return Result.ok(c);
    }

    @Operation(summary = "删除课程")
    @DeleteMapping("/courses/{id}")
    public Result<Void> delete(@PathVariable Long id) { courseService.deleteCourse(id); return Result.ok(); }

    @Operation(summary = "确认课程")
    @PostMapping("/courses/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        courseService.confirmCourse(id, Long.parseLong(claims.getSubject()));
        return Result.ok();
    }

    @Operation(summary = "查询我的选课列表")
    @GetMapping("/selections")
    public Result<List<CourseSelection>> mySelections(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Result.ok(courseService.getMySelections(Long.parseLong(claims.getSubject())));
    }

    @Operation(summary = "退选课程")
    @DeleteMapping("/selections/{id}")
    public Result<Void> drop(Authentication auth, @PathVariable Long id) {
        Claims claims = (Claims) auth.getDetails();
        courseService.dropCourse(id, Long.parseLong(claims.getSubject()));
        return Result.ok();
    }

    @Operation(summary = "查询课程下的学生名单")
    @GetMapping("/selections/course/{courseId}")
    public Result<List<CourseSelection>> courseStudents(@PathVariable Long courseId) {
        return Result.ok(courseService.getCourseStudents(courseId));
    }

    @Operation(summary = "查询教师授课列表")
    @GetMapping("/courses/teacher")
    public Result<List<Course>> teacherCourses(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Result.ok(courseService.getTeacherCourses(Long.parseLong(claims.getSubject())));
    }

    @Operation(summary = "查询课表")
    @GetMapping("/schedule")
    public Result<List<Course>> schedule(Authentication auth,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) Integer week) {
        Claims claims = (Claims) auth.getDetails();
        Long userId = Long.parseLong(claims.getSubject());
        String role = claims.get("role", String.class);
        return Result.ok(courseService.getMySchedule(userId, role, semester, week));
    }
    
    @Operation(summary = "批量导入课程（CSV）")
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

    @Operation(summary = "分配必修课程班级")
    @PostMapping("/courses/{id}/assign-classes")
    public Result<Void> assignClasses(@PathVariable Long id, @RequestBody List<Long> classIds) {
        courseService.assignRequiredCourse(id, classIds);
        return Result.ok();
    }

    @Operation(summary = "获取课程班级列表")
    @GetMapping("/courses/{id}/classes")
    public Result<List<CourseClass>> getClasses(@PathVariable Long id) {
        return Result.ok(courseService.getCourseClasses(id));
    }

    @Operation(summary = "设置课程班级")
    @PutMapping("/courses/{id}/classes")
    public Result<Void> setClasses(@PathVariable Long id, @RequestBody List<CourseClass> classes) {
        courseService.setCourseClasses(id, classes);
        return Result.ok();
    }

    @Operation(summary = "选修课选课（学生自主选课）")
    @PostMapping("/courses/{id}/enroll")
    public Result<Void> enroll(@PathVariable Long id, Authentication auth) {
        courseService.enrollElective(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "确认开课")
    @PostMapping("/courses/{id}/confirm-opening")
    public Result<Void> confirmOpening(@PathVariable Long id) {
        courseService.confirmCourse(id);
        return Result.ok();
    }

    @Operation(summary = "取消开课")
    @PostMapping("/courses/{id}/cancel-opening")
    public Result<Void> cancelOpening(@PathVariable Long id) {
        courseService.cancelCourse(id);
        return Result.ok();
    }

    // ========== Schedule Management Endpoints ==========

    @Operation(summary = "新增排课（含冲突检测）")
    @PostMapping("/courses/{id}/schedule")
    public Result<Void> addSchedule(@PathVariable Long id, @RequestBody ScheduleConflictDetector.ScheduleItem item) {
        courseService.addSchedule(id, item);
        return Result.ok();
    }

    @Operation(summary = "修改排课")
    @PutMapping("/courses/{id}/schedule/{index}")
    public Result<Void> updateSchedule(@PathVariable Long id, @PathVariable int index,
            @RequestBody ScheduleConflictDetector.ScheduleItem item) {
        courseService.updateSchedule(id, index, item);
        return Result.ok();
    }

    @Operation(summary = "删除排课")
    @DeleteMapping("/courses/{id}/schedule/{index}")
    public Result<Void> removeSchedule(@PathVariable Long id, @PathVariable int index) {
        courseService.removeSchedule(id, index);
        return Result.ok();
    }

    @Operation(summary = "按班级查询课表")
    @GetMapping("/schedule/class/{classId}")
    public Result<List<Course>> getScheduleByClass(@PathVariable Long classId) {
        return Result.ok(courseService.getScheduleByClass(classId));
    }

    @Operation(summary = "按教师查询课表")
    @GetMapping("/schedule/teacher/{teacherId}")
    public Result<List<Course>> getScheduleByTeacher(@PathVariable Long teacherId) {
        return Result.ok(courseService.getScheduleByTeacher(teacherId));
    }

    @Operation(summary = "按教室查询课表")
    @GetMapping("/schedule/room/{classroom}")
    public Result<List<Course>> getScheduleByRoom(@PathVariable String classroom) {
        return Result.ok(courseService.getScheduleByRoom(classroom));
    }

    private Long getUserId(Authentication auth) {
        Claims claims = (Claims) auth.getDetails();
        return Long.parseLong(claims.getSubject());
    }
}

package com.campus.module.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseClass;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.mapper.CourseClassMapper;
import com.campus.module.edu.mapper.CourseMapper;
import com.campus.module.edu.mapper.CourseSelectionMapper;
import com.campus.module.edu.mapper.GradeMapper;
import com.campus.module.edu.service.CourseService;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.mapper.SysDepartmentMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper selMapper;
    private final GradeMapper gradeMapper;
    private final com.campus.module.sys.mapper.SysUserMapper userMapper;
    private final JdbcTemplate jdbcTemplate;
    private final CourseClassMapper courseClassMapper;
    private final SysClassMapper sysClassMapper;
    private final SysDepartmentMapper sysDepartmentMapper;

    @Override
    @Transactional
    public void confirmCourse(Long courseId, Long teacherId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() == 2) throw new BusinessException("该课程已确认，无需重复确认");
        if (course.getStatus() != 1) throw new BusinessException("该课程未开放选课，无法确认");
        if (!course.getTeacherId().equals(teacherId)) throw new BusinessException("只能确认自己授课的课程");
        course.setStatus(2);
        courseMapper.updateById(course);
    }

    @Override
    public Page<Course> pageWithTeacher(int page, int size, String keyword, String semester) {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty())
            w.like(Course::getCourseName, keyword).or().like(Course::getCourseCode, keyword);
        if (semester != null && !semester.isEmpty())
            w.eq(Course::getSemester, semester);
        w.orderByDesc(Course::getCreateTime);
        Page<Course> result = courseMapper.selectPage(new Page<>(page, size), w);
        for (Course c : result.getRecords()) {
            if (c.getTeacherId() != null) {
                com.campus.module.sys.entity.SysUser teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
        }
        return result;
    }

    @Override
    @Transactional
    public CourseSelection selectCourse(Long studentId, Long courseId, String semester) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() != 1) throw new BusinessException("该课程不开放选课");
        int updated = courseMapper.updateEnrolled(courseId);
        if (updated == 0) throw new BusinessException("已满员");
        CourseSelection exist = selMapper.selectOne(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getSemester, semester));
        if (exist != null) {
            if (exist.getStatus() == 1) throw new BusinessException("已选该课程");
            exist.setStatus(1);
            selMapper.updateById(exist);
            return exist;
        }
        CourseSelection sel = new CourseSelection();
        sel.setStudentId(studentId); sel.setCourseId(courseId);
        sel.setSemester(semester); sel.setStatus(1);
        selMapper.insert(sel);
        return sel;
    }

    @Override
    @Transactional
    public void dropCourse(Long selId, Long studentId) {
        CourseSelection sel = selMapper.selectById(selId);
        if (sel == null || !sel.getStudentId().equals(studentId))
            throw new BusinessException("选课记录不存在");
        if (sel.getStatus() == 0) throw new BusinessException("已退课");
        Course course = courseMapper.selectById(sel.getCourseId());
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() == 2) throw new BusinessException("课程已确认，无法退课");
        sel.setStatus(0); selMapper.updateById(sel);
        course.setEnrolled(Math.max(0, course.getEnrolled() - 1));
        courseMapper.updateById(course);
    }

    @Override
    public List<CourseSelection> getMySelections(Long studentId) {
        List<CourseSelection> list = selMapper.selectList(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1));
        for (CourseSelection s : list) {
            Course course = courseMapper.selectById(s.getCourseId());
            if (course != null) {
                s.setCourseName(course.getCourseName());
                s.setCourseStatus(course.getStatus());
            }
        }
        return list;
    }

    @Override
    public List<CourseSelection> getCourseStudents(Long courseId) {
        List<CourseSelection> list = selMapper.selectList(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1));
        for (CourseSelection s : list) {
            Course course = courseMapper.selectById(s.getCourseId());
            if (course != null) {
                s.setCourseName(course.getCourseName());
            }
            com.campus.module.sys.entity.SysUser student = userMapper.selectById(s.getStudentId());
            if (student != null) {
                s.setStudentName(student.getRealName());
                s.setStudentClassName(student.getClassName());
                if (student.getDepartmentId() != null) {
                    com.campus.module.sys.entity.SysDepartment dept = sysDepartmentMapper.selectById(student.getDepartmentId());
                    s.setStudentDepartment(dept != null ? dept.getName() : "");
                }
                s.setStudentPhone(student.getPhone());
            }
        }
        return list;
    }

    @Override
    public List<Course> getTeacherCourses(Long teacherId) {
        return courseMapper.selectList(new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId));
    }

    @Override
    @Transactional
    public void deleteCourse(Long courseId) {
        Long activeCount = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1));
        if (activeCount > 0) {
            throw new BusinessException("该课程还有学生正在选课，无法删除");
        }
        Long gradeCount = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getCourseId, courseId));
        if (gradeCount > 0) {
            throw new BusinessException("该课程已有成绩记录，无法删除");
        }
        jdbcTemplate.update("DELETE FROM edu_course_selection WHERE course_id = ?", courseId);
        jdbcTemplate.update("DELETE FROM edu_grade WHERE course_id = ?", courseId);
        jdbcTemplate.update("DELETE FROM growth_checkin WHERE course_id = ?", courseId);
        jdbcTemplate.update("DELETE FROM edu_course WHERE id = ?", courseId);
    }

    @Override
    public void inputGrade(Grade grade) {
        if (grade.getScore() == null) throw new BusinessException("成绩不能为空");
        Course course = courseMapper.selectById(grade.getCourseId());
        if (course == null) throw new BusinessException("课程不存在");
        Long cnt = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, grade.getStudentId())
                .eq(CourseSelection::getCourseId, grade.getCourseId())
                .eq(CourseSelection::getStatus, 1));
        if (cnt == 0) throw new BusinessException("该学生未选修此课程");
        Long existCount = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, grade.getStudentId())
                .eq(Grade::getCourseId, grade.getCourseId())
                .eq(Grade::getSemester, grade.getSemester()));
        if (existCount > 0) throw new BusinessException("该学生该学期成绩已录入");
        gradeMapper.insert(grade);
    }

    @Override
    public List<Grade> getStudentGrades(Long studentId) {
        List<Grade> grades = gradeMapper.selectList(new LambdaQueryWrapper<Grade>().eq(Grade::getStudentId, studentId));
        for (Grade g : grades) {
            Course course = courseMapper.selectById(g.getCourseId());
            if (course != null) g.setCourseName(course.getCourseName());
        }
        return grades;
    }

    @Override
    public List<Grade> getCourseGrades(Long courseId) {
        return gradeMapper.selectList(new LambdaQueryWrapper<Grade>().eq(Grade::getCourseId, courseId));
    }

    @Override
    public List<Course> getMySchedule(Long userId, String role, String semester, Integer week) {
        List<Course> courses;
        if ("student".equals(role)) {
            LambdaQueryWrapper<CourseSelection> selWrapper = new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, userId)
                .eq(CourseSelection::getStatus, 1);
            if (semester != null && !semester.isEmpty()) {
                selWrapper.eq(CourseSelection::getSemester, semester);
            }
            List<CourseSelection> selections = selMapper.selectList(selWrapper);
            List<Long> courseIds = selections.stream()
                .map(CourseSelection::getCourseId)
                .collect(java.util.stream.Collectors.toList());
            if (courseIds.isEmpty()) return List.of();
            
            LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<Course>()
                .in(Course::getId, courseIds);
            if (semester != null && !semester.isEmpty()) {
                courseWrapper.eq(Course::getSemester, semester);
            }
            courses = courseMapper.selectList(courseWrapper);
        } else {
            LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, userId);
            if (semester != null && !semester.isEmpty()) {
                courseWrapper.eq(Course::getSemester, semester);
            }
            courses = courseMapper.selectList(courseWrapper);
        }
        
        // 按周数筛选
        if (week != null) {
            courses = courses.stream()
                .filter(c -> {
                    Integer startWeek = c.getStartWeek() != null ? c.getStartWeek() : 1;
                    Integer endWeek = c.getEndWeek() != null ? c.getEndWeek() : 20;
                    return week >= startWeek && week <= endWeek;
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 填充教师姓名
        for (Course c : courses) {
            if (c.getTeacherId() != null) {
                var teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
        }
        return courses;
    }

    @Override
    @Transactional
    public void assignRequiredCourse(Long courseId, List<Long> classIds) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");

        for (Long classId : classIds) {
            CourseClass cc = new CourseClass();
            cc.setCourseId(courseId);
            cc.setClassId(classId);
            cc.setIsRequired(1);
            courseClassMapper.insert(cc);

            List<SysUser> students = userMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getClassId, classId));
            for (SysUser s : students) {
                Long cnt = selMapper.selectCount(
                    new LambdaQueryWrapper<CourseSelection>()
                        .eq(CourseSelection::getCourseId, courseId)
                        .eq(CourseSelection::getStudentId, s.getId())
                        .eq(CourseSelection::getStatus, 1));
                if (cnt == 0) {
                    CourseSelection cs = new CourseSelection();
                    cs.setStudentId(s.getId());
                    cs.setCourseId(courseId);
                    cs.setSemester(course.getSemester());
                    cs.setSelectTime(LocalDateTime.now());
                    cs.setStatus(1);
                    cs.setSelectType("auto");
                    selMapper.insert(cs);
                    course.setEnrolled((course.getEnrolled() != null ? course.getEnrolled() : 0) + 1);
                }
            }
        }
        course.setStatus(2);
        course.setCourseType("required");
        courseMapper.updateById(course);
    }

    @Override
    public List<Course> getAvailableCourses(Long studentId) {
        SysUser student = userMapper.selectById(studentId);
        if (student == null || student.getClassId() == null) return List.of();

        List<Course> all = courseMapper.selectList(
            new LambdaQueryWrapper<Course>()
                .eq(Course::getStatus, 1)
                .eq(Course::getCourseType, "elective"));

        List<Course> available = new ArrayList<>();
        for (Course course : all) {
            List<CourseClass> constraints = courseClassMapper.selectList(
                new LambdaQueryWrapper<CourseClass>()
                    .eq(CourseClass::getCourseId, course.getId())
                    .eq(CourseClass::getIsRequired, 0));

            if (constraints.isEmpty()) {
                available.add(course);
                continue;
            }

            boolean accessible = constraints.stream().anyMatch(cc -> {
                if (cc.getClassId() == null) return true;
                return cc.getClassId().equals(student.getClassId());
            });

            if (accessible) available.add(course);
        }
        return available;
    }

    @Override
    public void enrollElective(Long courseId, Long studentId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() != 1) throw new BusinessException("该课程不在选课期");
        if (!"elective".equals(course.getCourseType())) throw new BusinessException("该课程不是选修课");

        Long cnt = selMapper.selectCount(
            new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1));
        if (cnt > 0) throw new BusinessException("已选该课程");

        if (course.getEnrolled() != null && course.getCapacity() != null
            && course.getEnrolled() >= course.getCapacity())
            throw new BusinessException("该课程已满员");

        CourseSelection cs = new CourseSelection();
        cs.setStudentId(studentId);
        cs.setCourseId(courseId);
        cs.setSemester(course.getSemester());
        cs.setSelectTime(LocalDateTime.now());
        cs.setStatus(1);
        cs.setSelectType("manual");
        selMapper.insert(cs);

        course.setEnrolled((course.getEnrolled() != null ? course.getEnrolled() : 0) + 1);
        courseMapper.updateById(course);
    }

    @Override
    public void confirmCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() != 1) throw new BusinessException("课程状态不正确");
        course.setStatus(2);
        courseMapper.updateById(course);
    }

    @Override
    public void cancelCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        course.setStatus(3);
        courseMapper.updateById(course);
    }

    @Override
    public List<CourseClass> getCourseClasses(Long courseId) {
        return courseClassMapper.selectList(
            new LambdaQueryWrapper<CourseClass>()
                .eq(CourseClass::getCourseId, courseId));
    }

    @Override
    public void setCourseClasses(Long courseId, List<CourseClass> classes) {
        courseClassMapper.delete(new LambdaQueryWrapper<CourseClass>()
            .eq(CourseClass::getCourseId, courseId));
        for (CourseClass cc : classes) {
            cc.setCourseId(courseId);
            cc.setId(null);
            courseClassMapper.insert(cc);
        }
    }
}

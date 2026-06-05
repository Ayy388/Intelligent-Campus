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
import com.campus.module.edu.service.ScheduleConflictDetector;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.mapper.SysDepartmentMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    @CacheEvict(value = "courses", allEntries = true)
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
    @Cacheable(value = "courses", key = "#page + '-' + #size + '-' + (#keyword ?: '') + '-' + (#semester ?: '')")
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
    @CacheEvict(value = "courses", allEntries = true)
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
    @CacheEvict(value = "courses", allEntries = true)
    public void dropCourse(Long selId, Long studentId) {
        CourseSelection sel = selMapper.selectById(selId);
        if (sel == null || !sel.getStudentId().equals(studentId))
            throw new BusinessException("选课记录不存在");
        if (sel.getStatus() == 0) throw new BusinessException("已退课");
        if ("auto".equals(sel.getSelectType()))
            throw new BusinessException("该课程为系统分配的必修课，无法退课");
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
                .eq(CourseSelection::getStatus, 1)
                .eq(CourseSelection::getSelectType, "manual"));
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
                s.setStudentUsername(student.getUsername());
                if (student.getClassId() != null) {
                    com.campus.module.sys.entity.SysClass c = sysClassMapper.selectById(student.getClassId());
                    s.setStudentClassName(c != null ? c.getClassName() : "");
                }
                if (student.getDepartmentId() != null) {
                    com.campus.module.sys.entity.SysDepartment dept = sysDepartmentMapper.selectById(student.getDepartmentId());
                    s.setStudentDepartment(dept != null ? dept.getName() : "");
                }
                s.setStudentPhone(student.getPhone());
            }
            Long gradeCnt = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                    .eq(Grade::getStudentId, s.getStudentId())
                    .eq(Grade::getCourseId, s.getCourseId())
                    .eq(Grade::getSemester, s.getSemester()));
            s.setGraded(gradeCnt > 0);
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
    @CacheEvict(value = "courses", allEntries = true)
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
        jdbcTemplate.update("DELETE FROM edu_course WHERE id = ?", courseId);
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
    @CacheEvict(value = "courses", allEntries = true)
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
    @CacheEvict(value = "courses", allEntries = true)
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
    @CacheEvict(value = "courses", allEntries = true)
    public void confirmCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() != 1) throw new BusinessException("课程状态不正确");
        course.setStatus(2);
        courseMapper.updateById(course);
    }

    @Override
    @CacheEvict(value = "courses", allEntries = true)
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
    @CacheEvict(value = "courses", allEntries = true)
    public void setCourseClasses(Long courseId, List<CourseClass> classes) {
        courseClassMapper.delete(new LambdaQueryWrapper<CourseClass>()
            .eq(CourseClass::getCourseId, courseId));
        for (CourseClass cc : classes) {
            cc.setCourseId(courseId);
            cc.setId(null);
            courseClassMapper.insert(cc);
        }
    }

    // ========== Schedule Management ==========

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void addSchedule(Long courseId, ScheduleConflictDetector.ScheduleItem item) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() == 2) throw new BusinessException("已确认开课的课程不可修改排课");

        // 1. 查询所有课程（用于教室/教师冲突检测）
        List<Course> allCourses = courseMapper.selectList(null);

        // 2. 教室冲突
        String roomConflict = ScheduleConflictDetector.findRoomConflict(allCourses, item);
        if (roomConflict != null) throw new BusinessException(roomConflict);

        // 3. 教师冲突（相同教师的所有课程）
        List<Course> teacherCourses = allCourses.stream()
            .filter(c -> c.getTeacherId() != null && c.getTeacherId().equals(course.getTeacherId()))
            .collect(Collectors.toList());
        String teacherConflict = ScheduleConflictDetector.findTeacherConflict(teacherCourses, item);
        if (teacherConflict != null) throw new BusinessException(teacherConflict);

        // 4. 班级冲突（该课程关联班级的其他课程）
        List<CourseClass> classLinks = courseClassMapper.selectList(
            new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, courseId));
        for (CourseClass link : classLinks) {
            List<Course> classCourses = courseClassMapper.selectList(
                new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getClassId, link.getClassId()))
                .stream()
                .map(cl -> courseMapper.selectById(cl.getCourseId()))
                .filter(cl -> cl != null && !cl.getId().equals(courseId))
                .collect(Collectors.toList());
            String classConflict = ScheduleConflictDetector.findClassConflict(classCourses, item);
            if (classConflict != null) throw new BusinessException(classConflict);
        }

        // 5. 无冲突 → 更新 schedule
        List<ScheduleConflictDetector.ScheduleItem> items = ScheduleConflictDetector.parseSchedule(course.getSchedule());
        items.add(item);
        course.setSchedule(ScheduleConflictDetector.toScheduleJson(items));
        courseMapper.updateById(course);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void removeSchedule(Long courseId, int scheduleIndex) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() == 2) throw new BusinessException("已确认开课的课程不可修改排课");

        List<ScheduleConflictDetector.ScheduleItem> items = ScheduleConflictDetector.parseSchedule(course.getSchedule());
        if (scheduleIndex < 0 || scheduleIndex >= items.size()) throw new BusinessException("排课索引无效");

        items.remove(scheduleIndex);
        course.setSchedule(ScheduleConflictDetector.toScheduleJson(items));
        courseMapper.updateById(course);
    }

    @Override
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public void updateSchedule(Long courseId, int scheduleIndex, ScheduleConflictDetector.ScheduleItem newItem) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() == 2) throw new BusinessException("已确认开课的课程不可修改排课");

        List<ScheduleConflictDetector.ScheduleItem> items = ScheduleConflictDetector.parseSchedule(course.getSchedule());
        if (scheduleIndex < 0 || scheduleIndex >= items.size()) throw new BusinessException("排课索引无效");

        // 检查课程内部排课冲突（新增项与同课程其他排课项的冲突）
        for (int i = 0; i < items.size(); i++) {
            if (i == scheduleIndex) continue;
            if (ScheduleConflictDetector.isTimeConflict(newItem, items.get(i))) {
                throw new BusinessException("课程内部排课时间冲突");
            }
        }

        // 排除自身后做冲突检测
        List<Course> allCourses = courseMapper.selectList(null);
        List<Course> otherCourses = allCourses.stream()
            .filter(c -> !c.getId().equals(courseId))
            .collect(Collectors.toList());

        // 教室冲突
        String roomConflict = ScheduleConflictDetector.findRoomConflict(otherCourses, newItem);
        if (roomConflict != null) throw new BusinessException(roomConflict);

        // 教师冲突
        List<Course> teacherCourses = otherCourses.stream()
            .filter(c -> c.getTeacherId() != null && c.getTeacherId().equals(course.getTeacherId()))
            .collect(Collectors.toList());
        String teacherConflict = ScheduleConflictDetector.findTeacherConflict(teacherCourses, newItem);
        if (teacherConflict != null) throw new BusinessException(teacherConflict);

        // 班级冲突
        List<CourseClass> classLinks = courseClassMapper.selectList(
            new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getCourseId, courseId));
        for (CourseClass link : classLinks) {
            List<Course> classCourses = courseClassMapper.selectList(
                new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getClassId, link.getClassId()))
                .stream()
                .map(cl -> courseMapper.selectById(cl.getCourseId()))
                .filter(cl -> cl != null && !cl.getId().equals(courseId))
                .collect(Collectors.toList());
            String classConflict = ScheduleConflictDetector.findClassConflict(classCourses, newItem);
            if (classConflict != null) throw new BusinessException(classConflict);
        }

        // 全部通过 → 替换并保存
        items.set(scheduleIndex, newItem);
        course.setSchedule(ScheduleConflictDetector.toScheduleJson(items));
        courseMapper.updateById(course);
    }

    @Override
    public List<Course> getScheduleByClass(Long classId) {
        List<CourseClass> links = courseClassMapper.selectList(
            new LambdaQueryWrapper<CourseClass>().eq(CourseClass::getClassId, classId));
        List<Long> courseIds = links.stream()
            .map(CourseClass::getCourseId)
            .collect(Collectors.toList());
        if (courseIds.isEmpty()) return List.of();
        List<Course> courses = courseMapper.selectBatchIds(courseIds);
        for (Course c : courses) {
            if (c.getTeacherId() != null) {
                SysUser teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
        }
        return courses;
    }

    @Override
    public List<Course> getScheduleByTeacher(Long teacherId) {
        List<Course> courses = courseMapper.selectList(
            new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, teacherId));
        for (Course c : courses) {
            if (c.getTeacherId() != null) {
                SysUser teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
        }
        return courses;
    }

    @Override
    public List<Course> getScheduleByRoom(String classroom) {
        List<Course> allCourses = courseMapper.selectList(null);
        List<Course> result = new ArrayList<>();
        for (Course course : allCourses) {
            String scheduleJson = course.getSchedule();
            if (scheduleJson != null && !scheduleJson.isEmpty()) {
                List<ScheduleConflictDetector.ScheduleItem> items = ScheduleConflictDetector.parseSchedule(scheduleJson);
                boolean match = items.stream().anyMatch(item -> classroom.equals(item.classroom));
                if (match) {
                    result.add(course);
                    continue;
                }
            }
            // 兼容旧的 classroom 字段
            if (course.getClassroom() != null && classroom.equals(course.getClassroom())) {
                result.add(course);
            }
        }
        for (Course c : result) {
            if (c.getTeacherId() != null) {
                SysUser teacher = userMapper.selectById(c.getTeacherId());
                if (teacher != null) c.setTeacherName(teacher.getRealName());
            }
        }
        return result;
    }
}

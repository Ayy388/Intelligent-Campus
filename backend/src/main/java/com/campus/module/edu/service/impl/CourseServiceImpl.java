package com.campus.module.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.mapper.CourseMapper;
import com.campus.module.edu.mapper.CourseSelectionMapper;
import com.campus.module.edu.mapper.GradeMapper;
import com.campus.module.edu.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper selMapper;
    private final GradeMapper gradeMapper;
    private final com.campus.module.sys.mapper.SysUserMapper userMapper;

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
        Long cnt = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getSemester, semester)
                .eq(CourseSelection::getStatus, 1));
        if (cnt > 0) throw new BusinessException("已选该课程");
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
        sel.setStatus(0); selMapper.updateById(sel);
        Course course = courseMapper.selectById(sel.getCourseId());
        if (course != null) {
            course.setEnrolled(Math.max(0, course.getEnrolled() - 1));
            courseMapper.updateById(course);
        }
    }

    @Override
    public List<CourseSelection> getMySelections(Long studentId) {
        List<CourseSelection> list = selMapper.selectList(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1));
        for (CourseSelection s : list) {
            Course course = courseMapper.selectById(s.getCourseId());
            if (course != null) s.setCourseName(course.getCourseName());
        }
        return list;
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
}

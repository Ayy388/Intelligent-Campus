package com.campus.module.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.mapper.CourseMapper;
import com.campus.module.edu.mapper.CourseSelectionMapper;
import com.campus.module.edu.mapper.GradeMapper;
import com.campus.module.edu.service.GradeService;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {
    private final GradeMapper gradeMapper;
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper selMapper;
    private final SysUserMapper userMapper;

    @Override
    @Transactional
    public void inputGrade(Grade grade) {
        if (grade.getScore() == null) throw new BusinessException("成绩不能为空");
        if (grade.getScore().compareTo(BigDecimal.ZERO) < 0 || grade.getScore().compareTo(new BigDecimal("100")) > 0)
            throw new BusinessException("分数必须在 0-100 之间");
        if (grade.getTeacherId() == null) throw new BusinessException("教师信息缺失");
        Course course = courseMapper.selectById(grade.getCourseId());
        if (course == null) throw new BusinessException("课程不存在");
        if (course.getStatus() != 2) throw new BusinessException("课程尚未确认开课，无法录入成绩");
        if (!course.getTeacherId().equals(grade.getTeacherId()))
            throw new BusinessException("您不是该课程的授课教师");
        Long selCnt = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, grade.getStudentId())
                .eq(CourseSelection::getCourseId, grade.getCourseId())
                .eq(CourseSelection::getStatus, 1));
        if (selCnt == 0) throw new BusinessException("该学生未选修此课程");
        Long existCount = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, grade.getStudentId())
                .eq(Grade::getCourseId, grade.getCourseId())
                .eq(Grade::getSemester, grade.getSemester()));
        if (existCount > 0) throw new BusinessException("该学生该学期成绩已录入");
        grade.setGradeLevel(calcGradeLevel(grade.getScore()));
        grade.setCreateTime(LocalDateTime.now());
        gradeMapper.insert(grade);
    }

    @Override
    @Transactional
    public void updateGrade(Long id, Grade grade) {
        Grade existing = gradeMapper.selectById(id);
        if (existing == null) throw new BusinessException("成绩记录不存在");
        Course course = courseMapper.selectById(existing.getCourseId());
        if (course == null) throw new BusinessException("课程不存在");
        if (grade.getTeacherId() != null && !course.getTeacherId().equals(grade.getTeacherId()))
            throw new BusinessException("您不是该课程的授课教师");
        // 校验学生当前是否仍选修该课程
        Long selCnt = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getStudentId, existing.getStudentId())
                .eq(CourseSelection::getCourseId, existing.getCourseId())
                .eq(CourseSelection::getStatus, 1));
        if (selCnt == 0) throw new BusinessException("该学生未选修此课程，无法更新成绩");
        if (grade.getScore() != null) {
            if (grade.getScore().compareTo(BigDecimal.ZERO) < 0 || grade.getScore().compareTo(new BigDecimal("100")) > 0)
                throw new BusinessException("分数必须在 0-100 之间");
            existing.setScore(grade.getScore());
            existing.setGradeLevel(calcGradeLevel(grade.getScore()));
        }
        if (grade.getGradeType() != null) existing.setGradeType(grade.getGradeType());
        if (grade.getGradeLevel() != null && grade.getScore() == null) existing.setGradeLevel(grade.getGradeLevel());
        if (grade.getRemark() != null) existing.setRemark(grade.getRemark());
        gradeMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void deleteGrade(Long id) {
        Grade existing = gradeMapper.selectById(id);
        if (existing == null) throw new BusinessException("成绩记录不存在");
        gradeMapper.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Grade> getStudentGradesPage(Long studentId, int page, int size, String semester) {
        LambdaQueryWrapper<Grade> w = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId);
        if (semester != null && !semester.isEmpty()) {
            w.eq(Grade::getSemester, semester);
        }
        w.orderByDesc(Grade::getCreateTime);
        Page<Grade> result = gradeMapper.selectPage(new Page<>(page, size), w);
        for (Grade g : result.getRecords()) {
            enrichGrade(g);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grade> getStudentAllGrades(Long studentId, String semester) {
        LambdaQueryWrapper<Grade> w = new LambdaQueryWrapper<Grade>()
                .eq(Grade::getStudentId, studentId);
        if (semester != null && !semester.isEmpty()) {
            w.eq(Grade::getSemester, semester);
        }
        List<Grade> grades = gradeMapper.selectList(w);
        for (Grade g : grades) {
            enrichGrade(g);
        }
        return grades;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Grade> getCourseGradesPage(Long courseId, int page, int size) {
        Page<Grade> result = gradeMapper.selectPage(new Page<>(page, size),
                new LambdaQueryWrapper<Grade>().eq(Grade::getCourseId, courseId));
        for (Grade g : result.getRecords()) {
            enrichGrade(g);
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Grade> getCourseGrades(Long courseId) {
        List<Grade> list = gradeMapper.selectList(new LambdaQueryWrapper<Grade>().eq(Grade::getCourseId, courseId));
        for (Grade g : list) {
            enrichGrade(g);
        }
        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getCourseStatistics(Long courseId) {
        List<Grade> grades = gradeMapper.selectList(new LambdaQueryWrapper<Grade>().eq(Grade::getCourseId, courseId));
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("total", grades.size());
        if (grades.isEmpty()) {
            stats.put("average", 0);
            stats.put("max", 0);
            stats.put("min", 0);
            stats.put("passRate", 0);
            stats.put("distribution", Map.of("优秀", 0, "良好", 0, "中等", 0, "及格", 0, "不及格", 0));
            return stats;
        }
        double sum = 0, max = -1, min = 101;
        int passCount = 0;
        Map<String, Integer> dist = new LinkedHashMap<>();
        dist.put("优秀", 0); dist.put("良好", 0); dist.put("中等", 0);
        dist.put("及格", 0); dist.put("不及格", 0);
        for (Grade g : grades) {
            if (g.getScore() == null) continue;
            double s = g.getScore().doubleValue();
            sum += s;
            max = Math.max(max, s);
            min = Math.min(min, s);
            if (s >= 60) passCount++;
            String level = calcGradeLevel(g.getScore());
            dist.merge(level, 1, Integer::sum);
        }
        stats.put("average", BigDecimal.valueOf(sum / grades.size()).setScale(2, RoundingMode.HALF_UP).doubleValue());
        stats.put("max", max);
        stats.put("min", min);
        stats.put("passRate", BigDecimal.valueOf(passCount * 100.0 / grades.size()).setScale(1, RoundingMode.HALF_UP).doubleValue());
        stats.put("distribution", dist);
        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStudentTranscript(Long studentId) {
        List<Grade> grades = gradeMapper.selectList(new LambdaQueryWrapper<Grade>().eq(Grade::getStudentId, studentId));
        for (Grade g : grades) {
            enrichGrade(g);
        }
        Map<String, List<Grade>> bySemester = new LinkedHashMap<>();
        for (Grade g : grades) {
            bySemester.computeIfAbsent(g.getSemester(), k -> new ArrayList<>()).add(g);
        }
        double totalWeighted = 0, totalCredits = 0;
        List<Map<String, Object>> semesters = new ArrayList<>();
        for (Map.Entry<String, List<Grade>> entry : bySemester.entrySet()) {
            double semWeighted = 0, semCredits = 0;
            for (Grade g : entry.getValue()) {
                if (g.getScore() == null) continue;
                Course c = courseMapper.selectById(g.getCourseId());
                BigDecimal credit = c != null && c.getCredit() != null ? c.getCredit() : BigDecimal.ZERO;
                double s = g.getScore().doubleValue();
                double gp = calcGradePoint(s);
                semWeighted += gp * credit.doubleValue();
                semCredits += credit.doubleValue();
            }
            Map<String, Object> sem = new LinkedHashMap<>();
            sem.put("semester", entry.getKey());
            sem.put("grades", entry.getValue());
            sem.put("totalCredits", semCredits);
            sem.put("gpa", semCredits > 0 ? BigDecimal.valueOf(semWeighted / semCredits).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0);
            semesters.add(sem);
            totalWeighted += semWeighted;
            totalCredits += semCredits;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("semesters", semesters);
        result.put("totalCredits", totalCredits);
        result.put("overallGpa", totalCredits > 0 ? BigDecimal.valueOf(totalWeighted / totalCredits).setScale(2, RoundingMode.HALF_UP).doubleValue() : 0);
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getTeacherStatus(Long teacherId, String semester) {
        LambdaQueryWrapper<Course> cw = new LambdaQueryWrapper<Course>()
                .eq(Course::getTeacherId, teacherId);
        if (semester != null && !semester.isEmpty()) {
            cw.eq(Course::getSemester, semester);
        }
        List<Course> courses = courseMapper.selectList(cw);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Course c : courses) {
            long total = selMapper.selectCount(new LambdaQueryWrapper<CourseSelection>()
                    .eq(CourseSelection::getCourseId, c.getId())
                    .eq(CourseSelection::getStatus, 1));
            long graded = gradeMapper.selectCount(new LambdaQueryWrapper<Grade>()
                    .eq(Grade::getCourseId, c.getId()));
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("courseId", c.getId());
            item.put("courseName", c.getCourseName());
            item.put("courseCode", c.getCourseCode());
            item.put("semester", c.getSemester());
            item.put("totalStudents", total);
            item.put("gradedCount", graded);
            item.put("progress", total > 0 ? BigDecimal.valueOf(graded * 100.0 / total).setScale(1, RoundingMode.HALF_UP).doubleValue() : 0);
            result.add(item);
        }
        return result;
    }

    private String calcGradeLevel(BigDecimal score) {
        if (score == null) return "";
        double s = score.doubleValue();
        if (s >= 90) return "优秀";
        if (s >= 80) return "良好";
        if (s >= 70) return "中等";
        if (s >= 60) return "及格";
        return "不及格";
    }

    private double calcGradePoint(double score) {
        if (score >= 90) return 4.0;
        if (score >= 85) return 3.7;
        if (score >= 82) return 3.3;
        if (score >= 78) return 3.0;
        if (score >= 75) return 2.7;
        if (score >= 72) return 2.3;
        if (score >= 68) return 2.0;
        if (score >= 64) return 1.5;
        if (score >= 60) return 1.0;
        return 0;
    }

    private void enrichGrade(Grade g) {
        if (g.getStudentId() != null) {
            SysUser student = userMapper.selectById(g.getStudentId());
            if (student != null) {
                g.setStudentName(student.getRealName());
                g.setStudentUsername(student.getUsername());
            }
        }
        if (g.getCourseId() != null) {
            Course course = courseMapper.selectById(g.getCourseId());
            if (course != null) g.setCourseName(course.getCourseName());
        }
    }
}
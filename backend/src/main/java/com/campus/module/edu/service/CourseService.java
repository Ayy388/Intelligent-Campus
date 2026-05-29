package com.campus.module.edu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import java.util.List;

public interface CourseService extends IService<Course> {
    Page<Course> pageWithTeacher(int page, int size, String keyword, String semester);
    CourseSelection selectCourse(Long studentId, Long courseId, String semester);
    void dropCourse(Long selectionId, Long studentId);
    List<CourseSelection> getMySelections(Long studentId);
    void inputGrade(Grade grade);
    List<Grade> getStudentGrades(Long studentId);
    List<Grade> getCourseGrades(Long courseId);
}

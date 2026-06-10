package com.campus.module.edu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.edu.entity.Grade;
import java.util.List;
import java.util.Map;

public interface GradeService {
    void inputGrade(Grade grade);
    Map<String, Object> batchInputGrade(List<Grade> grades, Long teacherId);
    void updateGrade(Long id, Grade grade);
    void deleteGrade(Long id);
    Page<Grade> getStudentGradesPage(Long studentId, int page, int size, String semester);
    List<Grade> getStudentAllGrades(Long studentId, String semester);
    Page<Grade> getCourseGradesPage(Long courseId, int page, int size);
    List<Grade> getCourseGrades(Long courseId);
    Map<String, Object> getCourseStatistics(Long courseId);
    Map<String, Object> getStudentTranscript(Long studentId);
    List<Map<String, Object>> getTeacherStatus(Long teacherId, String semester);
}
package com.campus.module.edu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseClass;
import com.campus.module.edu.entity.CourseSelection;
import java.util.List;

public interface CourseService extends IService<Course> {
    Page<Course> pageWithTeacher(int page, int size, String keyword, String semester);
    CourseSelection selectCourse(Long studentId, Long courseId, String semester);
    void dropCourse(Long selectionId, Long studentId);
    List<CourseSelection> getMySelections(Long studentId);
    List<CourseSelection> getCourseStudents(Long courseId);
    List<Course> getTeacherCourses(Long teacherId);
    void confirmCourse(Long courseId, Long teacherId);
    void deleteCourse(Long courseId);
    List<Course> getMySchedule(Long userId, String role, String semester, Integer week);
    void assignRequiredCourse(Long courseId, List<Long> classIds);
    List<Course> getAvailableCourses(Long studentId);
    void enrollElective(Long courseId, Long studentId);
    void confirmCourse(Long courseId);
    void cancelCourse(Long courseId);
    List<CourseClass> getCourseClasses(Long courseId);
    void setCourseClasses(Long courseId, List<CourseClass> classes);

    // Schedule management
    void addSchedule(Long courseId, ScheduleConflictDetector.ScheduleItem item);
    void removeSchedule(Long courseId, int scheduleIndex);
    void updateSchedule(Long courseId, int scheduleIndex, ScheduleConflictDetector.ScheduleItem item);
    List<Course> getScheduleByClass(Long classId);
    List<Course> getScheduleByTeacher(Long teacherId);
    List<Course> getScheduleByRoom(String classroom);
}

package com.campus.module.edu.service;

import com.campus.module.edu.entity.Course;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

/**
 * 排课冲突检测器
 * 纯工具类，不依赖 Spring 容器
 */
public class ScheduleConflictDetector {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String[] DAY_NAMES = {"", "周一", "周二", "周三", "周四", "周五"};

    // 排课项数据结构
    public static class ScheduleItem {
        public int day;
        public int timeSlot;
        public String weeks;       // "all" | "odd" | "even"
        public int startWeek = 1;
        public int endWeek = 20;
        public String classroom;
    }

    /**
     * 检测两条排课是否时间冲突
     * 同时满足以下条件才视为冲突：
     *   1. 相同 day
     *   2. 相同 timeSlot
     *   3. 周次模式有重叠（weekPatternOverlap）
     *   4. 周范围有重叠（startWeek/endWeek）
     */
    public static boolean isTimeConflict(ScheduleItem a, ScheduleItem b) {
        if (a.day != b.day) return false;
        if (a.timeSlot != b.timeSlot) return false;
        if (!weekPatternOverlap(a.weeks, b.weeks)) return false;
        return weekRangeOverlap(a.startWeek, a.endWeek, b.startWeek, b.endWeek);
    }

    /**
     * 判断周次模式是否重叠
     * "all" + "odd" = true, "all" + "even" = true,
     * "odd" + "even" = false, "odd" + "odd" = true
     */
    public static boolean weekPatternOverlap(String wa, String wb) {
        if (wa == null || wb == null) return false;
        if (wa.equals(wb)) return true;
        if (wa.equals("all") || wb.equals("all")) return true;
        return false; // odd vs even
    }

    /**
     * 判断周范围是否重叠
     * [1,16] 和 [12,20] 重叠 → true
     * [1,8] 和 [12,20] 不重叠 → false
     */
    public static boolean weekRangeOverlap(int sa, int ea, int sb, int eb) {
        return sa <= eb && sb <= ea;
    }

    /**
     * 检测教室冲突
     * 在给定课程列表中，查找与目标排课项冲突的课程
     * 同一教室 + 时间冲突 = 教室冲突
     */
    public static String findRoomConflict(List<Course> courses, ScheduleItem target) {
        for (Course course : courses) {
            String scheduleJson = course.getSchedule();
            if (scheduleJson == null || scheduleJson.isEmpty()) continue;
            List<ScheduleItem> items = parseSchedule(scheduleJson);
            for (ScheduleItem item : items) {
                if (item.classroom != null && item.classroom.equals(target.classroom)
                        && isTimeConflict(item, target)) {
                    return String.format("教室%s 在%s第%d大节已被《%s》占用",
                            target.classroom,
                            DAY_NAMES[target.day],
                            target.timeSlot,
                            course.getCourseName());
                }
            }
        }
        return null;
    }

    /**
     * 检测教师冲突
     * 同一教师 + 时间冲突 = 教师冲突
     * courseList 是同一教师的所有课程
     */
    public static String findTeacherConflict(List<Course> teacherCourses, ScheduleItem target) {
        for (Course course : teacherCourses) {
            String scheduleJson = course.getSchedule();
            if (scheduleJson == null || scheduleJson.isEmpty()) continue;
            List<ScheduleItem> items = parseSchedule(scheduleJson);
            for (ScheduleItem item : items) {
                if (isTimeConflict(item, target)) {
                    return String.format("教师在%s第%d大节已被《%s》占用",
                            DAY_NAMES[target.day],
                            target.timeSlot,
                            course.getCourseName());
                }
            }
        }
        return null;
    }

    /**
     * 检测班级冲突
     * 通过课程-班级关联表查询：某班级已选的课程中，
     * 是否有与目标排课时间冲突的
     * 需要接收：该班级关联的所有课程列表
     */
    public static String findClassConflict(List<Course> classCourses, ScheduleItem target) {
        for (Course course : classCourses) {
            String scheduleJson = course.getSchedule();
            if (scheduleJson == null || scheduleJson.isEmpty()) continue;
            List<ScheduleItem> items = parseSchedule(scheduleJson);
            for (ScheduleItem item : items) {
                if (isTimeConflict(item, target)) {
                    return String.format("班级在%s第%d大节已被《%s》占用",
                            DAY_NAMES[target.day],
                            target.timeSlot,
                            course.getCourseName());
                }
            }
        }
        return null;
    }

    /**
     * JSON 解析工具：将 course.schedule 字符串解析为 ScheduleItem 列表
     */
    public static List<ScheduleItem> parseSchedule(String scheduleJson) {
        if (scheduleJson == null || scheduleJson.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return OBJECT_MAPPER.readValue(scheduleJson, new TypeReference<List<ScheduleItem>>() {});
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    /**
     * 将 ScheduleItem 列表序列化为 JSON 字符串
     */
    public static String toScheduleJson(List<ScheduleItem> items) {
        try {
            return OBJECT_MAPPER.writeValueAsString(items);
        } catch (Exception e) {
            return "[]";
        }
    }
}
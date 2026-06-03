package com.campus.module.edu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.*;
import com.campus.module.edu.mapper.*;
import com.campus.module.edu.service.TrainingPlanService;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.mapper.SysGradeMapper;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TrainingPlanServiceImpl
        extends ServiceImpl<TrainingPlanMapper, TrainingPlan>
        implements TrainingPlanService {

    private final TrainingPlanMapper planMapper;
    private final TrainingPlanItemMapper itemMapper;
    private final CourseMapper courseMapper;
    private final CourseClassMapper courseClassMapper;
    private final CourseSelectionMapper selMapper;
    private final GradeMapper gradeMapper;
    private final SysClassMapper sysClassMapper;
    private final SysMajorMapper majorMapper;
    private final SysGradeMapper gradeMapper2;
    private final SysUserMapper userMapper;
    private final SemesterMapper semesterMapper;

    @Override
    public Page<TrainingPlan> pageWithDetail(int page, int size, String keyword) {
        LambdaQueryWrapper<TrainingPlan> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(TrainingPlan::getName, keyword);
        }
        w.orderByDesc(TrainingPlan::getCreateTime);
        Page<TrainingPlan> p = planMapper.selectPage(new Page<>(page, size), w);
        for (TrainingPlan plan : p.getRecords()) {
            enrichPlan(plan);
        }
        return p;
    }

    @Override
    public TrainingPlan getWithDetail(Long id) {
        TrainingPlan plan = planMapper.selectById(id);
        if (plan == null) throw new BusinessException(404, "培养方案不存在");
        enrichPlan(plan);
        return plan;
    }

    private void enrichPlan(TrainingPlan plan) {
        if (plan.getMajorId() != null) {
            SysMajor major = majorMapper.selectById(plan.getMajorId());
            if (major != null) plan.setMajorName(major.getName());
        }
        if (plan.getGradeId() != null) {
            SysGrade grade = gradeMapper2.selectById(plan.getGradeId());
            if (grade != null) plan.setGradeName(grade.getName());
        }
    }

    @Override
    public List<TrainingPlanItem> getItems(Long planId, Integer semesterNumber) {
        List<TrainingPlanItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<TrainingPlanItem>()
                        .eq(TrainingPlanItem::getPlanId, planId)
                        .eq(TrainingPlanItem::getSemesterNumber, semesterNumber)
                        .orderByAsc(TrainingPlanItem::getSortOrder));
        enrichItems(items);
        return items;
    }

    private void enrichItems(List<TrainingPlanItem> items) {
        for (TrainingPlanItem item : items) {
            if (item.getGeneratedCourseId() != null) {
                Course c = courseMapper.selectById(item.getGeneratedCourseId());
                if (c != null) {
                    item.setGeneratedCourseName(c.getCourseName());
                    item.setGeneratedCourseStatus(c.getStatus());
                }
            }
        }
    }

    @Override
    @Transactional
    public TrainingPlanItem addItem(TrainingPlanItem item) {
        item.setId(null);
        item.setStatus(0);
        item.setCreateTime(LocalDateTime.now());
        itemMapper.insert(item);
        return item;
    }

    @Override
    @Transactional
    public TrainingPlanItem updateItem(TrainingPlanItem item) {
        TrainingPlanItem existing = itemMapper.selectById(item.getId());
        if (existing == null) throw new BusinessException(404, "方案项不存在");
        if (existing.getGeneratedCourseId() != null) {
            throw new BusinessException(400, "该课程已生成，无法修改，请删除重新添加");
        }
        itemMapper.updateById(item);
        return itemMapper.selectById(item.getId());
    }

    @Override
    @Transactional
    public void deleteItem(Long itemId) {
        TrainingPlanItem item = itemMapper.selectById(itemId);
        if (item == null) return;
        if (item.getGeneratedCourseId() != null) {
            throw new BusinessException(400, "该课程已生成，请先删除生成的课程记录");
        }
        itemMapper.deleteById(itemId);
    }

    @Override
    @Transactional
    public void deletePlan(Long id) {
        itemMapper.delete(new LambdaQueryWrapper<TrainingPlanItem>()
                .eq(TrainingPlanItem::getPlanId, id));
        planMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Map<String, Object> generateSemester(Long planId, Integer semesterNumber) {
        TrainingPlan plan = planMapper.selectById(planId);
        if (plan == null) throw new BusinessException(404, "培养方案不存在");

        List<TrainingPlanItem> items = itemMapper.selectList(
                new LambdaQueryWrapper<TrainingPlanItem>()
                        .eq(TrainingPlanItem::getPlanId, planId)
                        .eq(TrainingPlanItem::getSemesterNumber, semesterNumber)
                        .isNull(TrainingPlanItem::getGeneratedCourseId));
        if (items.isEmpty()) {
            throw new BusinessException(400, "该学期没有未生成的课程项");
        }

        List<SysClass> targetClasses = sysClassMapper.selectList(
                new LambdaQueryWrapper<SysClass>()
                        .eq(SysClass::getMajorId, plan.getMajorId())
                        .eq(SysClass::getGradeId, plan.getGradeId()));
        if (targetClasses.isEmpty()) {
            throw new BusinessException(400, "没有找到匹配的班级，请确认专业和年级下已有班级");
        }

        String semesterCode = resolveSemesterCode(plan, semesterNumber);

        List<Long> generatedCourseIds = new ArrayList<>();
        int generatedCount = 0;

        // 自动排课：记录已占用的 (day, timeSlot)，避免同一班级冲突
        Set<String> usedSlots = new HashSet<>();
        com.fasterxml.jackson.databind.ObjectMapper jsonMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        int altCounter = 0;

        for (TrainingPlanItem item : items) {
            // 始终使用自动生成编码避免与已有课程冲突
            String courseCode = "TP" + planId + "S" + semesterNumber + "I" + item.getId();

            Course course = new Course();
            course.setCourseCode(courseCode);
            course.setCourseName(item.getCourseName());
            course.setTeacherId(null);
            course.setCredit(item.getCredit());
            course.setHours(item.getHours());
            course.setSemester(semesterCode);
            course.setStartWeek(1);
            course.setCapacity(100);
            course.setStatus(0);
            course.setCourseType(item.getIsRequired() == 1 ? "required" : "elective");
            course.setEnrolled(0);

            // 自动排课：根据学时分配时段，高学时课程每周2时段，支持单双周
            int endWeek = 16;
            if (item.getHours() != null && item.getHours() > 0) {
                // 确定时段数和交替模式
                int h = item.getHours();
                int slotCount;
                boolean useAlternating = false;
                if (h >= 64) {
                    slotCount = 2;        // 64h+ 双时段全周
                } else if (h >= 48) {
                    slotCount = 2;        // 48-63h 双时段，第2个时段交替
                    useAlternating = true;
                } else {
                    slotCount = 1;        // <48h 单时段
                }

                String basePattern = "all";
                String altPattern = "all";
                if ("形势与政策".equals(item.getCourseName())) {
                    altPattern = "odd";
                    useAlternating = true;
                } else if (useAlternating) {
                    altPattern = (altCounter++ % 2 == 0) ? "odd" : "even";
                }

                // 计算周数：交替课程平均 3h/周，双时段全周 4h/周，单时段 2h/周
                double avgHoursPerWeek = slotCount == 2 ? (useAlternating ? 3.0 : 4.0) : 2.0;
                int weeks = Math.min(20, Math.max(1, (int) Math.ceil(h / avgHoursPerWeek)));
                endWeek = course.getStartWeek() + weeks - 1;
                if (endWeek < 16) endWeek = 16;

                List<Map<String, Object>> slotList = new ArrayList<>();
                if (slotCount == 2) {
                    outer:
                    for (int d1 = 1; d1 <= 5; d1++) {
                        for (int s1 = 1; s1 <= 4; s1++) {
                            String k1 = d1 + "-" + s1;
                            if (usedSlots.contains(k1)) continue;
                            for (int d2 = d1; d2 <= 5; d2++) {
                                for (int s2 = (d2 == d1 ? s1 + 1 : 1); s2 <= 4; s2++) {
                                    String k2 = d2 + "-" + s2;
                                    if (usedSlots.contains(k2)) continue;
                                    if (d1 == d2 && Math.abs(s1 - s2) < 2) continue;
                                    usedSlots.add(k1); usedSlots.add(k2);
                                    Map<String, Object> m1 = new HashMap<>();
                                    m1.put("day", d1); m1.put("timeSlot", s1); m1.put("weeks", basePattern);
                                    Map<String, Object> m2 = new HashMap<>();
                                    m2.put("day", d2); m2.put("timeSlot", s2); m2.put("weeks", altPattern);
                                    slotList.add(m1); slotList.add(m2);
                                    break outer;
                                }
                            }
                        }
                    }
                }
                if (slotList.isEmpty()) {
                    String singlePattern = useAlternating ? altPattern : basePattern;
                    for (int d = 1; d <= 5 && slotList.isEmpty(); d++) {
                        for (int s = 1; s <= 4 && slotList.isEmpty(); s++) {
                            String key = d + "-" + s;
                            if (!usedSlots.contains(key)) {
                                usedSlots.add(key);
                                Map<String, Object> m = new HashMap<>();
                                m.put("day", d); m.put("timeSlot", s); m.put("weeks", singlePattern);
                                slotList.add(m);
                            }
                        }
                    }
                }
                if (!slotList.isEmpty()) {
                    try {
                        course.setSchedule(jsonMapper.writeValueAsString(slotList));
                    } catch (Exception e) {
                        throw new BusinessException(500, "排课JSON序列化失败: " + e.getMessage());
                    }
                }
            }
            course.setEndWeek(endWeek);
            courseMapper.insert(course);
            generatedCourseIds.add(course.getId());

            for (SysClass clazz : targetClasses) {
                CourseClass cc = new CourseClass();
                cc.setCourseId(course.getId());
                cc.setClassId(clazz.getId());
                cc.setIsRequired(item.getIsRequired());
                courseClassMapper.insert(cc);
            }

            if (item.getIsRequired() == 1) {
                for (SysClass clazz : targetClasses) {
                    List<SysUser> students = userMapper.selectList(
                            new LambdaQueryWrapper<SysUser>()
                                    .eq(SysUser::getClassId, clazz.getId()));
                    for (SysUser student : students) {
                        Long cnt = selMapper.selectCount(
                                new LambdaQueryWrapper<CourseSelection>()
                                        .eq(CourseSelection::getCourseId, course.getId())
                                        .eq(CourseSelection::getStudentId, student.getId())
                                        .eq(CourseSelection::getStatus, 1));
                        if (cnt == 0) {
                            CourseSelection cs = new CourseSelection();
                            cs.setStudentId(student.getId());
                            cs.setCourseId(course.getId());
                            cs.setSemester(semesterCode);
                            cs.setSelectTime(LocalDateTime.now());
                            cs.setStatus(1);
                            cs.setSelectType("auto");
                            selMapper.insert(cs);
                            course.setEnrolled((course.getEnrolled() != null ? course.getEnrolled() : 0) + 1);
                        }
                    }
                }
                courseMapper.updateById(course);
            }

            item.setGeneratedCourseId(course.getId());
            item.setStatus(1);
            itemMapper.updateById(item);
            generatedCount++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("generatedCount", generatedCount);
        result.put("totalItems", items.size());
        result.put("courseIds", generatedCourseIds);
        result.put("semester", semesterCode);
        result.put("classNames", targetClasses.stream().map(SysClass::getClassName).collect(Collectors.toList()));
        return result;
    }

    private String resolveSemesterCode(TrainingPlan plan, Integer semesterNumber) {
        SysGrade grade = gradeMapper2.selectById(plan.getGradeId());
        if (grade == null || grade.getYear() == null) {
            throw new BusinessException(400, "年级信息不完整，无法推算学期");
        }
        int baseYear = grade.getYear();
        int yearOffset = (semesterNumber - 1) / 2;
        int academicStart = baseYear + yearOffset;
        int academicEnd = academicStart + 1;
        String xn = academicStart + "-" + academicEnd + "学年";
        String xqjc = (semesterNumber % 2 == 1) ? (academicStart + "01") : (academicStart + "02");

        List<Semester> match = semesterMapper.selectList(
                new LambdaQueryWrapper<Semester>()
                        .eq(Semester::getXn, xn)
                        .eq(Semester::getXqjc, xqjc)
                        .last("LIMIT 1"));
        if (!match.isEmpty()) {
            return match.get(0).getXqjc();
        }
        return xqjc;
    }

    @Override
    public Map<String, Object> getMyPlan(Long studentId) {
        SysUser student = userMapper.selectById(studentId);
        if (student == null) throw new BusinessException(404, "学生不存在");
        if (student.getClassId() == null) {
            throw new BusinessException(400, "当前学生未分配班级");
        }

        SysClass clazz = sysClassMapper.selectById(student.getClassId());
        if (clazz == null) throw new BusinessException(400, "班级信息不完整");

        // 从班级获取专业和年级（学生表可能未直接关联）
        Long majorId = student.getMajorId() != null ? student.getMajorId() : clazz.getMajorId();
        if (majorId == null) throw new BusinessException(400, "班级未关联专业信息");
        Long gradeId = clazz.getGradeId();
        if (gradeId == null) throw new BusinessException(400, "班级未关联年级信息");

        TrainingPlan plan = planMapper.selectOne(
                new LambdaQueryWrapper<TrainingPlan>()
                        .eq(TrainingPlan::getMajorId, majorId)
                        .eq(TrainingPlan::getGradeId, gradeId));
        if (plan == null) {
            throw new BusinessException(404, "未找到匹配的培养方案");
        }
        enrichPlan(plan);

        List<Map<String, Object>> semesters = new ArrayList<>();
        for (int sem = 1; sem <= plan.getTotalSemesters(); sem++) {
            List<TrainingPlanItem> items = itemMapper.selectList(
                    new LambdaQueryWrapper<TrainingPlanItem>()
                            .eq(TrainingPlanItem::getPlanId, plan.getId())
                            .eq(TrainingPlanItem::getSemesterNumber, sem)
                            .orderByAsc(TrainingPlanItem::getSortOrder));

            List<Map<String, Object>> courseInfos = new ArrayList<>();
            for (TrainingPlanItem item : items) {
                Map<String, Object> info = new HashMap<>();
                info.put("courseName", item.getCourseName());
                info.put("courseCode", item.getCourseCode());
                info.put("credit", item.getCredit());
                info.put("hours", item.getHours());
                info.put("isRequired", item.getIsRequired());
                info.put("generatedCourseId", item.getGeneratedCourseId());

                String studentStatus = "unregistered";
                if (item.getGeneratedCourseId() != null) {
                    CourseSelection sel = selMapper.selectOne(
                            new LambdaQueryWrapper<CourseSelection>()
                                    .eq(CourseSelection::getCourseId, item.getGeneratedCourseId())
                                    .eq(CourseSelection::getStudentId, studentId)
                                    .eq(CourseSelection::getStatus, 1)
                                    .last("LIMIT 1"));
                    if (sel != null) {
                        Long gradeCount = gradeMapper.selectCount(
                                new LambdaQueryWrapper<com.campus.module.edu.entity.Grade>()
                                        .eq(com.campus.module.edu.entity.Grade::getCourseId, item.getGeneratedCourseId())
                                        .eq(com.campus.module.edu.entity.Grade::getStudentId, studentId));
                        if (gradeCount > 0) {
                            studentStatus = "completed";
                        } else {
                            studentStatus = "in_progress";
                        }
                    }
                }
                info.put("studentStatus", studentStatus);
                courseInfos.add(info);
            }

            Map<String, Object> semData = new HashMap<>();
            semData.put("semesterNumber", sem);
            semData.put("courses", courseInfos);
            semesters.add(semData);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("plan", plan);
        result.put("semesters", semesters);
        return result;
    }
}

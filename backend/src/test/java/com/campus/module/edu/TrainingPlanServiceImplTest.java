package com.campus.module.edu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.*;
import com.campus.module.edu.mapper.*;
import com.campus.module.edu.service.impl.TrainingPlanServiceImpl;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceImplTest {

    @Mock
    private TrainingPlanMapper planMapper;

    @Mock
    private TrainingPlanItemMapper itemMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseClassMapper courseClassMapper;

    @Mock
    private CourseSelectionMapper selMapper;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private SysClassMapper sysClassMapper;

    @Mock
    private SysMajorMapper majorMapper;

    @Mock
    private SysGradeMapper gradeMapper2;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SemesterMapper semesterMapper;

    @InjectMocks
    private TrainingPlanServiceImpl trainingPlanService;

    private TrainingPlan plan;
    private TrainingPlanItem item1;
    private SysClass targetClass;
    private SysGrade grade;
    private Semester semester;

    @BeforeEach
    void setUp() {
        plan = new TrainingPlan();
        plan.setId(1L);
        plan.setName("2024计算机培养方案");
        plan.setMajorId(1L);
        plan.setGradeId(1L);
        plan.setTotalSemesters(8);

        item1 = new TrainingPlanItem();
        item1.setId(1L);
        item1.setPlanId(1L);
        item1.setSemesterNumber(1);
        item1.setCourseName("高等数学");
        item1.setCourseCode("MATH101");
        item1.setCredit(new BigDecimal("4"));
        item1.setHours(64);
        item1.setIsRequired(1);

        targetClass = new SysClass();
        targetClass.setId(1L);
        targetClass.setClassName("计科2401班");
        targetClass.setMajorId(1L);
        targetClass.setGradeId(1L);

        grade = new SysGrade();
        grade.setId(1L);
        grade.setName("2024级");
        grade.setYear(2024);

        semester = new Semester();
        semester.setId(1L);
        semester.setXn("2024-2025学年");
        semester.setXqjc("202401");
        semester.setXqqc("2024-2025学年第一学期");
    }

    @Test
    @DisplayName("生成学期课程成功 - 培养方案有效、班级存在、课程项未生成")
    void testGenerateSemester_whenValid_shouldGenerateCourses() {
        // Mock plan
        when(planMapper.selectById(1L)).thenReturn(plan);
        // Mock ungenerated items
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(item1));
        // Mock classes
        when(sysClassMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(targetClass));
        // Mock grade for semester code resolution
        when(gradeMapper2.selectById(1L)).thenReturn(grade);
        // Mock semester search (returns empty so it uses auto-generated code)
        when(semesterMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        // Mock course insert - stub the insert to set the course ID
        doAnswer(invocation -> {
            Course c = invocation.getArgument(0);
            c.setId(100L);
            return 1;
        }).when(courseMapper).insert(any(Course.class));
        // Mock courseClassMapper insert - returns 1
        when(courseClassMapper.insert(any(CourseClass.class))).thenReturn(1);
        // Mock student list for required course auto-enrollment (empty list)
        when(userMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        // Mock courseMapper updateById - returns 1
        when(courseMapper.updateById(any(Course.class))).thenReturn(1);
        // Mock itemMapper updateById - returns 1
        when(itemMapper.updateById(any(TrainingPlanItem.class))).thenReturn(1);

        Map<String, Object> result = trainingPlanService.generateSemester(1L, 1);

        assertNotNull(result);
        assertEquals(1, result.get("generatedCount"));
        assertEquals(1, result.get("totalItems"));
        assertNotNull(result.get("courseIds"));
        assertEquals("202401", result.get("semester"));

        // verify course was created
        verify(courseMapper, times(1)).insert(any(Course.class));
        // verify courseClass was created
        verify(courseClassMapper, times(1)).insert(any(CourseClass.class));
        // verify item was backfilled with generated course id
        verify(itemMapper).updateById(any(TrainingPlanItem.class));
    }

    @Test
    @DisplayName("重复生成学期 - 无未生成的课程项，抛出异常")
    void testGenerateSemester_whenNoUngeneratedItems_shouldThrow() {
        when(planMapper.selectById(1L)).thenReturn(plan);
        // Return empty list — all items already generated
        when(itemMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> trainingPlanService.generateSemester(1L, 1));
        assertEquals("该学期没有未生成的课程项", ex.getMessage());
    }

    @Test
    @DisplayName("生成学期课程失败 - 培养方案不存在")
    void testGenerateSemester_whenPlanNotFound_shouldThrow() {
        when(planMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> trainingPlanService.generateSemester(999L, 1));
        assertEquals(404, ex.getCode());
    }
}
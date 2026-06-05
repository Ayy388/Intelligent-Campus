package com.campus.module.edu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.entity.Grade;
import com.campus.module.edu.mapper.CourseMapper;
import com.campus.module.edu.mapper.CourseSelectionMapper;
import com.campus.module.edu.mapper.GradeMapper;
import com.campus.module.edu.service.impl.GradeServiceImpl;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class GradeServiceImplTest {

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseSelectionMapper selMapper;

    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private GradeServiceImpl gradeService;

    private Course course;
    private Grade validGrade;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setCourseName("测试课程");
        course.setTeacherId(10L);
        course.setStatus(2); // 已确认

        validGrade = new Grade();
        validGrade.setStudentId(1L);
        validGrade.setCourseId(1L);
        validGrade.setTeacherId(10L);
        validGrade.setScore(new BigDecimal("85.5"));
        validGrade.setSemester("2025-2026学年第二学期");
        validGrade.setGradeType("百分制");
    }

    // ========== inputGrade 测试 ==========

    @Test
    @DisplayName("录入成绩成功 - 有效数据")
    void testInputGrade_whenValid_shouldSucceed() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(selMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(gradeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        gradeService.inputGrade(validGrade);

        assertEquals("良好", validGrade.getGradeLevel());
        assertNotNull(validGrade.getCreateTime());
        verify(gradeMapper).insert(validGrade);
    }

    @Test
    @DisplayName("录入成绩失败 - 分数超出 0-100 范围")
    void testInputGrade_whenScoreOutOfRange_shouldThrow() {
        validGrade.setScore(new BigDecimal("150"));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gradeService.inputGrade(validGrade));
        assertEquals("分数必须在 0-100 之间", ex.getMessage());

        // 负数测试
        validGrade.setScore(new BigDecimal("-10"));
        BusinessException ex2 = assertThrows(BusinessException.class,
                () -> gradeService.inputGrade(validGrade));
        assertEquals("分数必须在 0-100 之间", ex2.getMessage());
    }

    @Test
    @DisplayName("录入成绩失败 - 学生未选修此课程")
    void testInputGrade_whenStudentNotEnrolled_shouldThrow() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(selMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gradeService.inputGrade(validGrade));
        assertEquals("该学生未选修此课程", ex.getMessage());
    }

    @Test
    @DisplayName("录入成绩失败 - 该学期成绩已存在")
    void testInputGrade_whenGradeAlreadyExists_shouldThrow() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(selMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        when(gradeMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> gradeService.inputGrade(validGrade));
        assertEquals("该学生该学期成绩已录入", ex.getMessage());
    }

    // ========== getCourseStatistics 测试 ==========

    @Test
    @DisplayName("成绩统计正常 - 有成绩数据时计算正确")
    void testGetStatistics_whenHasData_shouldReturnCorrectStats() {
        Grade g1 = new Grade(); g1.setScore(new BigDecimal("95"));
        Grade g2 = new Grade(); g2.setScore(new BigDecimal("85"));
        Grade g3 = new Grade(); g3.setScore(new BigDecimal("60"));
        Grade g4 = new Grade(); g4.setScore(new BigDecimal("45"));
        when(gradeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(g1, g2, g3, g4));

        Map<String, Object> stats = gradeService.getCourseStatistics(1L);

        assertEquals(4, stats.get("total"));
        assertEquals(95.0, (Double) stats.get("max"), 0.01);
        assertEquals(45.0, (Double) stats.get("min"), 0.01);
        assertEquals(71.25, (Double) stats.get("average"), 0.01);
        assertEquals(75.0, (Double) stats.get("passRate"), 0.1); // 3/4 通过
    }

    @Test
    @DisplayName("成绩统计正常 - 无数据时返回零值")
    void testGetStatistics_whenNoData_shouldReturnZeroValues() {
        when(gradeMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());

        Map<String, Object> stats = gradeService.getCourseStatistics(1L);

        assertEquals(0, stats.get("total"));
        assertEquals(0, stats.get("average"));
        assertEquals(0, stats.get("max"));
        assertEquals(0, stats.get("min"));
        assertEquals(0, stats.get("passRate"));
        assertNotNull(stats.get("distribution"));
    }
}
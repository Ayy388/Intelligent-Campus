package com.campus.module.edu;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.module.edu.entity.Course;
import com.campus.module.edu.entity.CourseSelection;
import com.campus.module.edu.mapper.*;
import com.campus.module.edu.service.impl.CourseServiceImpl;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private CourseSelectionMapper selMapper;

    @Mock
    private GradeMapper gradeMapper;

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private CourseClassMapper courseClassMapper;

    @Mock
    private SysClassMapper sysClassMapper;

    @Mock
    private SysDepartmentMapper sysDepartmentMapper;

    @InjectMocks
    private CourseServiceImpl courseService;

    private Course course;
    private CourseSelection selection;

    @BeforeEach
    void setUp() {
        course = new Course();
        course.setId(1L);
        course.setCourseName("测试课程");
        course.setCourseCode("TEST001");
        course.setCapacity(50);
        course.setEnrolled(10);
        course.setStatus(1); // 选课中
        course.setSemester("2025-2026学年第二学期");

        selection = new CourseSelection();
        selection.setId(1L);
        selection.setStudentId(1L);
        selection.setCourseId(1L);
        selection.setSemester("2025-2026学年第二学期");
        selection.setStatus(1);
        selection.setSelectType("manual");
    }

    // ========== dropCourse 测试 ==========

    @Test
    @DisplayName("退选成功 - 手动选课且课程未确认")
    void testDropCourse_whenValidSelection_shouldSucceed() {
        when(selMapper.selectById(1L)).thenReturn(selection);
        Course courseWithEnrolled = new Course();
        courseWithEnrolled.setId(1L);
        courseWithEnrolled.setStatus(1); // 选课中，未确认
        courseWithEnrolled.setEnrolled(10);
        when(courseMapper.selectById(1L)).thenReturn(courseWithEnrolled);

        courseService.dropCourse(1L, 1L);

        assertEquals(0, selection.getStatus());
        verify(selMapper).updateById(selection);
        assertEquals(9, courseWithEnrolled.getEnrolled());
        verify(courseMapper).updateById(courseWithEnrolled);
    }

    @Test
    @DisplayName("退选失败 - 选课记录不存在")
    void testDropCourse_whenNotSelected_shouldThrow() {
        when(selMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.dropCourse(999L, 1L));
        assertEquals("选课记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("退选失败 - 系统分配的必修课无法退选")
    void testDropCourse_whenAutoSelect_shouldThrow() {
        selection.setSelectType("auto");
        when(selMapper.selectById(1L)).thenReturn(selection);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.dropCourse(1L, 1L));
        assertEquals("该课程为系统分配的必修课，无法退课", ex.getMessage());
    }

    @Test
    @DisplayName("退选失败 - 课程已确认开课无法退选")
    void testDropCourse_whenCourseConfirmed_shouldThrow() {
        when(selMapper.selectById(1L)).thenReturn(selection);
        Course confirmedCourse = new Course();
        confirmedCourse.setId(1L);
        confirmedCourse.setStatus(2); // 已确认
        when(courseMapper.selectById(1L)).thenReturn(confirmedCourse);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.dropCourse(1L, 1L));
        assertEquals("课程已确认，无法退课", ex.getMessage());
    }
}
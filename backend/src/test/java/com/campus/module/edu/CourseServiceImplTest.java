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

    // ========== selectCourse 测试 ==========

    @Test
    @DisplayName("选课成功 - 课程存在、未满员、未选过")
    void testSelectCourse_whenCourseExistsAndOpen_shouldSucceed() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(courseMapper.updateEnrolled(1L)).thenReturn(1);
        when(selMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        CourseSelection result = courseService.selectCourse(1L, 1L, "2025-2026学年第二学期");

        assertNotNull(result);
        assertEquals(1L, result.getStudentId());
        assertEquals(1L, result.getCourseId());
        assertEquals(1, result.getStatus());
        assertEquals("2025-2026学年第二学期", result.getSemester());
        verify(selMapper).insert(any(CourseSelection.class));
    }

    @Test
    @DisplayName("选课失败 - 课程不存在")
    void testSelectCourse_whenCourseNotFound_shouldThrow() {
        when(courseMapper.selectById(999L)).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.selectCourse(1L, 999L, "2025-2026学年第二学期"));
        assertEquals("课程不存在", ex.getMessage());
    }

    @Test
    @DisplayName("选课失败 - 课程不开放选课")
    void testSelectCourse_whenCourseNotOpen_shouldThrow() {
        course.setStatus(0); // 未发布
        when(courseMapper.selectById(1L)).thenReturn(course);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.selectCourse(1L, 1L, "2025-2026学年第二学期"));
        assertEquals("该课程不开放选课", ex.getMessage());
    }

    @Test
    @DisplayName("选课失败 - 课程已满员(updateEnrolled返回0)")
    void testSelectCourse_whenCourseFull_shouldThrow() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(courseMapper.updateEnrolled(1L)).thenReturn(0);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.selectCourse(1L, 1L, "2025-2026学年第二学期"));
        assertEquals("已满员", ex.getMessage());
    }

    @Test
    @DisplayName("选课失败 - 已选过该课程(status=1)")
    void testSelectCourse_whenAlreadySelected_shouldThrow() {
        when(courseMapper.selectById(1L)).thenReturn(course);
        when(courseMapper.updateEnrolled(1L)).thenReturn(1);
        CourseSelection existing = new CourseSelection();
        existing.setStatus(1);
        when(selMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(existing);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> courseService.selectCourse(1L, 1L, "2025-2026学年第二学期"));
        assertEquals("已选该课程", ex.getMessage());
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
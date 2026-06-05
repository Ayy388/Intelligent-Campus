package com.campus.module.admin.service.impl;

import com.campus.common.BusinessException;
import com.campus.module.admin.entity.LeaveApplication;
import com.campus.module.admin.mapper.LeaveApplicationMapper;
import com.campus.module.admin.mapper.NotificationMapper;
import com.campus.module.admin.mapper.NotificationReadMapper;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private NotificationMapper notiMapper;

    @Mock
    private NotificationReadMapper notiReadMapper;

    @Mock
    private LeaveApplicationMapper leaveMapper;

    @Mock
    private SysUserMapper userMapper;

    @InjectMocks
    private AdminServiceImpl adminService;

    @Captor
    private ArgumentCaptor<LeaveApplication> leaveCaptor;

    private LeaveApplication sampleLeave;

    @BeforeEach
    void setUp() {
        sampleLeave = new LeaveApplication();
        sampleLeave.setStudentId(1L);
        sampleLeave.setLeaveType("sick");
        sampleLeave.setStartTime(LocalDateTime.now());
        sampleLeave.setEndTime(LocalDateTime.now().plusDays(1));
        sampleLeave.setReason("身体不适");
    }

    // ===== applyLeave =====

    @Test
    @DisplayName("请假申请成功 - 应设置状态为待审批并插入记录")
    void testApplyLeave_whenValidRequest_shouldInsertLeave() {
        // Act
        adminService.applyLeave(sampleLeave);

        // Assert
        verify(leaveMapper, times(1)).insert(leaveCaptor.capture());
        LeaveApplication captured = leaveCaptor.getValue();
        assertEquals(Integer.valueOf(0), captured.getStatus());
        assertNotNull(captured.getApplyTime());
        assertEquals("sick", captured.getLeaveType());
        assertEquals("身体不适", captured.getReason());
    }

    @Test
    @DisplayName("请假申请理由为空 - 应仍能成功创建")
    void testApplyLeave_whenReasonEmpty_shouldStillInsert() {
        // Arrange
        sampleLeave.setReason(null);

        // Act
        adminService.applyLeave(sampleLeave);

        // Assert
        verify(leaveMapper, times(1)).insert(leaveCaptor.capture());
        assertNull(leaveCaptor.getValue().getReason());
        assertEquals(Integer.valueOf(0), leaveCaptor.getValue().getStatus());
    }

    @Test
    @DisplayName("重复请假申请 - 应插入两条记录（无去重逻辑）")
    void testApplyLeave_whenDuplicateRequest_shouldInsertTwice() {
        // Act
        adminService.applyLeave(sampleLeave);
        adminService.applyLeave(sampleLeave);

        // Assert
        verify(leaveMapper, times(2)).insert(any(LeaveApplication.class));
    }

    // ===== approveLeave =====

    @Test
    @DisplayName("审批通过 - 应更新状态为已通过并记录审批时间")
    void testApproveLeave_whenApprove_shouldUpdateStatusToApproved() {
        // Arrange
        Long leaveId = 1L;
        Long teacherId = 2L;
        Integer status = 1; // 通过
        String reason = "";

        LeaveApplication existing = new LeaveApplication();
        existing.setId(leaveId);
        existing.setStudentId(1L);
        existing.setStatus(0); // 待审批
        when(leaveMapper.selectById(leaveId)).thenReturn(existing);

        // Act
        adminService.approveLeave(leaveId, teacherId, status, reason);

        // Assert
        verify(leaveMapper).updateById(leaveCaptor.capture());
        LeaveApplication updated = leaveCaptor.getValue();
        assertEquals(Integer.valueOf(1), updated.getStatus());
        assertEquals(teacherId, updated.getTeacherId());
        assertNotNull(updated.getApproveTime());
    }

    @Test
    @DisplayName("审批拒绝 - 应更新状态为已拒绝并记录原因")
    void testApproveLeave_whenReject_shouldUpdateStatusToRejected() {
        // Arrange
        Long leaveId = 1L;
        Long teacherId = 2L;
        Integer status = 2; // 拒绝
        String reason = "理由不充分";

        LeaveApplication existing = new LeaveApplication();
        existing.setId(leaveId);
        existing.setStudentId(1L);
        existing.setStatus(0);
        when(leaveMapper.selectById(leaveId)).thenReturn(existing);

        // Act
        adminService.approveLeave(leaveId, teacherId, status, reason);

        // Assert
        verify(leaveMapper).updateById(leaveCaptor.capture());
        LeaveApplication updated = leaveCaptor.getValue();
        assertEquals(Integer.valueOf(2), updated.getStatus());
        assertEquals(reason, updated.getRejectReason());
        assertEquals(teacherId, updated.getTeacherId());
    }

    @Test
    @DisplayName("审批不存在的请假记录 - 应抛出 BusinessException")
    void testApproveLeave_whenLeaveNotFound_shouldThrowException() {
        // Arrange
        when(leaveMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.approveLeave(999L, 2L, 1, ""));
        assertEquals("请假记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("审批已处理的请假 - 应抛出 BusinessException")
    void testApproveLeave_whenAlreadyProcessed_shouldThrowException() {
        // Arrange
        Long leaveId = 1L;
        LeaveApplication existing = new LeaveApplication();
        existing.setId(leaveId);
        existing.setStatus(1); // 已审批
        when(leaveMapper.selectById(leaveId)).thenReturn(existing);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> adminService.approveLeave(leaveId, 2L, 2, ""));
        assertEquals("该请假申请已被处理", ex.getMessage());
    }

    // ===== getLeaveById =====

    @Test
    @DisplayName("获取不存在请假记录 - 应抛出 BusinessException")
    void testGetLeaveById_whenNotFound_shouldThrowException() {
        when(leaveMapper.selectById(999L)).thenReturn(null);
        assertThrows(BusinessException.class, () -> adminService.getLeaveById(999L));
    }

    @Test
    @DisplayName("获取请假记录 - 应填充学生姓名")
    void testGetLeaveById_whenFound_shouldFillStudentName() {
        // Arrange
        LeaveApplication leave = new LeaveApplication();
        leave.setId(1L);
        leave.setStudentId(1L);
        when(leaveMapper.selectById(1L)).thenReturn(leave);

        SysUser student = new SysUser();
        student.setId(1L);
        student.setRealName("张三");
        when(userMapper.selectById(1L)).thenReturn(student);

        // Act
        LeaveApplication result = adminService.getLeaveById(1L);

        // Assert
        assertNotNull(result);
        assertEquals("张三", result.getStudentName());
    }
}
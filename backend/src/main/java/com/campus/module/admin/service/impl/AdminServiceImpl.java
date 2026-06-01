package com.campus.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.admin.entity.*;
import com.campus.module.admin.mapper.*;
import com.campus.module.admin.service.AdminService;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final NotificationMapper notiMapper;
    private final NotificationReadMapper notiReadMapper;
    private final LeaveApplicationMapper leaveMapper;
    private final SysUserMapper userMapper;

    @Override
    public Page<Notification> pageNotifications(int page, int size, String category, Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) w.eq(Notification::getCategory, category);
        w.orderByDesc(Notification::getIsTop).orderByDesc(Notification::getCreateTime);
        Page<Notification> result = notiMapper.selectPage(new Page<>(page, size), w);

        if (userId != null && !result.getRecords().isEmpty()) {
            Set<Long> readIds = notiReadMapper.selectList(
                    new LambdaQueryWrapper<NotificationRead>()
                            .eq(NotificationRead::getUserId, userId)
                            .in(NotificationRead::getNotificationId,
                                result.getRecords().stream().map(Notification::getId).collect(Collectors.toList())))
                    .stream().map(NotificationRead::getNotificationId).collect(Collectors.toSet());
            for (Notification n : result.getRecords()) {
                n.setRead(readIds.contains(n.getId()));
            }
        }
        return result;
    }

    @Override
    public Notification getNotificationById(Long id) {
        Notification n = notiMapper.selectById(id);
        if (n != null) { n.setViewCount(n.getViewCount() + 1); notiMapper.updateById(n); }
        return n;
    }

    @Override
    public void saveNotification(Notification n) { notiMapper.insert(n); }

    @Override
    public void updateNotification(Long id, Notification n) { n.setId(id); notiMapper.updateById(n); }

    @Override
    public void deleteNotification(Long id) { notiMapper.deleteById(id); }

    @Override
    public Page<LeaveApplication> pageLeaves(Long userId, String role, int page, int size) {
        LambdaQueryWrapper<LeaveApplication> w = new LambdaQueryWrapper<>();
        if ("student".equals(role)) w.eq(LeaveApplication::getStudentId, userId);
        else if ("teacher".equals(role) || "admin".equals(role) || "counselor".equals(role))
            w.and(w2 -> w2.eq(LeaveApplication::getTeacherId, userId).or().isNull(LeaveApplication::getTeacherId));
        w.orderByDesc(LeaveApplication::getApplyTime);
        Page<LeaveApplication> result = leaveMapper.selectPage(new Page<>(page, size), w);
        for (LeaveApplication l : result.getRecords()) {
            if (l.getStudentId() != null) {
                SysUser student = userMapper.selectById(l.getStudentId());
                if (student != null) l.setStudentName(student.getRealName());
            }
        }
        return result;
    }

    @Override
    public void applyLeave(LeaveApplication leave) {
        leave.setStatus(0);
        leave.setApplyTime(LocalDateTime.now());
        leaveMapper.insert(leave);
    }

    @Override
    public void approveLeave(Long id, Long teacherId, Integer status, String reason) {
        LeaveApplication leave = leaveMapper.selectById(id);
        if (leave == null) throw new BusinessException("请假记录不存在");
        if (leave.getStatus() != 0) throw new BusinessException("该请假申请已被处理");
        leave.setTeacherId(teacherId);
        leave.setStatus(status);
        leave.setRejectReason(reason);
        leave.setApproveTime(LocalDateTime.now());
        leaveMapper.updateById(leave);
    }

    @Override
    public LeaveApplication getLeaveById(Long id) {
        LeaveApplication leave = leaveMapper.selectById(id);
        if (leave == null) throw new BusinessException("请假记录不存在");
        if (leave.getStudentId() != null) {
            SysUser student = userMapper.selectById(leave.getStudentId());
            if (student != null) leave.setStudentName(student.getRealName());
        }
        return leave;
    }

    @Override
    public void cancelLeave(Long id, Long userId) {
        LeaveApplication leave = leaveMapper.selectById(id);
        if (leave == null || !leave.getStudentId().equals(userId))
            throw new BusinessException("请假记录不存在或无权操作");
        if (leave.getStatus() != 0) throw new BusinessException("已审批的申请无法取消");
        leaveMapper.deleteById(id);
    }

    public long countUnreadNotifications(Long userId) {
        long total = notiMapper.selectCount(new LambdaQueryWrapper<>());
        long read = notiReadMapper.selectCount(new LambdaQueryWrapper<NotificationRead>()
            .eq(NotificationRead::getUserId, userId));
        return total - read;
    }

    @Override
    public void markNotificationRead(Long notificationId, Long userId) {
        long count = notiReadMapper.selectCount(new LambdaQueryWrapper<NotificationRead>()
            .eq(NotificationRead::getNotificationId, notificationId)
            .eq(NotificationRead::getUserId, userId));
        if (count == 0) {
            NotificationRead nr = new NotificationRead();
            nr.setNotificationId(notificationId);
            nr.setUserId(userId);
            nr.setReadTime(java.time.LocalDateTime.now());
            notiReadMapper.insert(nr);
        }
    }
}

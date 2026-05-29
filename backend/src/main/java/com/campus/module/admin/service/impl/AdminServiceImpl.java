package com.campus.module.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.BusinessException;
import com.campus.module.admin.entity.*;
import com.campus.module.admin.mapper.*;
import com.campus.module.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final NotificationMapper notiMapper;
    private final LeaveApplicationMapper leaveMapper;
    private final GuideMapper guideMapper;

    @Override
    public Page<Notification> pageNotifications(int page, int size, String category) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) w.eq(Notification::getCategory, category);
        w.orderByDesc(Notification::getIsTop).orderByDesc(Notification::getCreateTime);
        return notiMapper.selectPage(new Page<>(page, size), w);
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
        else if ("teacher".equals(role) || "admin".equals(role))
            w.and(w2 -> w2.eq(LeaveApplication::getTeacherId, userId).or().isNull(LeaveApplication::getTeacherId));
        w.orderByDesc(LeaveApplication::getApplyTime);
        return leaveMapper.selectPage(new Page<>(page, size), w);
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

    @Override
    public Page<Guide> pageGuides(int page, int size, String category) {
        LambdaQueryWrapper<Guide> w = new LambdaQueryWrapper<>();
        if (category != null && !category.isEmpty()) w.eq(Guide::getCategory, category);
        w.orderByDesc(Guide::getCreateTime);
        return guideMapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public Guide getGuideById(Long id) {
        Guide g = guideMapper.selectById(id);
        if (g != null) { g.setViewCount(g.getViewCount() + 1); guideMapper.updateById(g); }
        return g;
    }

    @Override public void saveGuide(Guide g) { guideMapper.insert(g); }
    @Override public void updateGuide(Long id, Guide g) { g.setId(id); guideMapper.updateById(g); }
    @Override public void deleteGuide(Long id) { guideMapper.deleteById(id); }
}

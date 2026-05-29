package com.campus.module.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.admin.entity.*;

public interface AdminService {
    Page<Notification> pageNotifications(int page, int size);
    Notification getNotificationById(Long id);
    void saveNotification(Notification n);
    void updateNotification(Long id, Notification n);
    void deleteNotification(Long id);

    Page<LeaveApplication> pageLeaves(Long userId, String role, int page, int size);
    void applyLeave(LeaveApplication leave);
    void approveLeave(Long id, Long teacherId, Integer status, String reason);

    Page<Guide> pageGuides(int page, int size, String category);
    Guide getGuideById(Long id);
    void saveGuide(Guide g);
    void updateGuide(Long id, Guide g);
    void deleteGuide(Long id);
}

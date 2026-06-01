package com.campus.module.activity.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.activity.entity.ActivityCenter;
import com.campus.module.activity.entity.ActivityRegistration;

import java.util.List;

public interface ActivityCenterService {
    Page<ActivityCenter> pagePublic(int page, int size, Long currentUserId, Long clubId);
    Page<ActivityCenter> pageMyCreated(Long userId, int page, int size);
    Page<ActivityCenter> pagePending(int page, int size);
    ActivityCenter create(ActivityCenter activity, Long userId, String role);
    ActivityCenter getById(Long id, Long currentUserId);
    void approve(Long id, Long approverId, Integer status, String rejectReason);
    void confirm(Long id, Long userId);
    void register(Long activityId, Long userId);
    void cancelRegistration(Long activityId, Long userId);
    Page<ActivityRegistration> pageRegistrations(Long activityId, int page, int size);
    List<ActivityRegistration> getMyRegistrations(Long userId);
    void updateSummary(Long id, Long userId, String summary, String images);
    void withdraw(Long id, Long userId);
    void finish(Long id, Long userId);
}

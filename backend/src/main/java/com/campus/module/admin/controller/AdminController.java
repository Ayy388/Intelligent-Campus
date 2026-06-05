package com.campus.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.admin.entity.*;
import com.campus.module.admin.service.AdminService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "行政管理", description = "通知公告、请假审批等行政管理功能")
public class AdminController {
    private final AdminService adminService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    private String getRole(Authentication auth) {
        return ((Claims) auth.getDetails()).get("role", String.class);
    }

    @Operation(summary = "分页查询通知公告")
    @GetMapping("/notifications")
    public Result<PageResult<Notification>> listNotis(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        return toPageResult(adminService.pageNotifications(page, size, category,
                auth != null ? getUserId(auth) : null));
    }

    @Operation(summary = "获取通知详情")
    @GetMapping("/notifications/{id}")
    public Result<Notification> getNoti(@PathVariable Long id, Authentication auth) {
        Notification n = adminService.getNotificationById(id);
        if (n != null) adminService.markNotificationRead(id, getUserId(auth));
        return Result.ok(n);
    }

    @Operation(summary = "获取未读通知数量")
    @GetMapping("/notifications/unread-count")
    public Result<Long> unreadCount(Authentication auth) {
        return Result.ok(adminService.countUnreadNotifications(getUserId(auth)));
    }

    @Operation(summary = "标记通知为已读")
    @PostMapping("/notifications/{id}/read")
    public Result<Void> markRead(@PathVariable Long id, Authentication auth) {
        adminService.markNotificationRead(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "添加通知")
    @PostMapping("/notifications")
    public Result<Void> addNoti(@RequestBody Notification n, Authentication auth) {
        n.setPublisherId(getUserId(auth));
        adminService.saveNotification(n);
        return Result.ok();
    }

    @Operation(summary = "更新通知")
    @PutMapping("/notifications/{id}")
    public Result<Void> updateNoti(@PathVariable Long id, @RequestBody Notification n) {
        adminService.updateNotification(id, n);
        return Result.ok();
    }

    @Operation(summary = "删除通知")
    @DeleteMapping("/notifications/{id}")
    public Result<Void> delNoti(@PathVariable Long id) {
        adminService.deleteNotification(id);
        return Result.ok();
    }

    @Operation(summary = "分页查询请假记录")
    @GetMapping("/leaves")
    public Result<PageResult<LeaveApplication>> listLeaves(Authentication auth,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return toPageResult(adminService.pageLeaves(getUserId(auth), getRole(auth), page, size));
    }

    @Operation(summary = "提交请假申请")
    @PostMapping("/leaves")
    public Result<Void> applyLeave(@RequestBody LeaveApplication leave, Authentication auth) {
        leave.setStudentId(getUserId(auth));
        adminService.applyLeave(leave);
        return Result.ok();
    }

    @Operation(summary = "审批请假申请")
    @PutMapping("/leaves/{id}/approve")
    public Result<Void> approveLeave(@PathVariable Long id, Authentication auth,
            @RequestParam Integer status, @RequestParam(required = false) String reason) {
        adminService.approveLeave(id, getUserId(auth), status,
                reason != null ? reason : "");
        return Result.ok();
    }

    @Operation(summary = "获取请假详情")
    @GetMapping("/leaves/{id}")
    public Result<LeaveApplication> getLeave(@PathVariable Long id) {
        return Result.ok(adminService.getLeaveById(id));
    }

    @Operation(summary = "取消请假申请")
    @DeleteMapping("/leaves/{id}")
    public Result<Void> cancelLeave(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        adminService.cancelLeave(id, userId);
        return Result.ok();
    }

    private <T> Result<PageResult<T>> toPageResult(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

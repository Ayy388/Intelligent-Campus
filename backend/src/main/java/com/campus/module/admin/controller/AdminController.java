package com.campus.module.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.admin.entity.*;
import com.campus.module.admin.service.AdminService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    private String getRole(Authentication auth) {
        return ((Claims) auth.getDetails()).get("role", String.class);
    }

    @GetMapping("/notifications")
    public Result<PageResult<Notification>> listNotis(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(adminService.pageNotifications(page, size));
    }

    @GetMapping("/notifications/{id}")
    public Result<Notification> getNoti(@PathVariable Long id) {
        return Result.ok(adminService.getNotificationById(id));
    }

    @PostMapping("/notifications")
    public Result<Void> addNoti(@RequestBody Notification n, Authentication auth) {
        n.setPublisherId(getUserId(auth));
        adminService.saveNotification(n);
        return Result.ok();
    }

    @PutMapping("/notifications/{id}")
    public Result<Void> updateNoti(@PathVariable Long id, @RequestBody Notification n) {
        adminService.updateNotification(id, n);
        return Result.ok();
    }

    @DeleteMapping("/notifications/{id}")
    public Result<Void> delNoti(@PathVariable Long id) {
        adminService.deleteNotification(id);
        return Result.ok();
    }

    @GetMapping("/leaves")
    public Result<PageResult<LeaveApplication>> listLeaves(Authentication auth,
            @RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) {
        return toPageResult(adminService.pageLeaves(getUserId(auth), getRole(auth), page, size));
    }

    @PostMapping("/leaves")
    public Result<Void> applyLeave(@RequestBody LeaveApplication leave, Authentication auth) {
        leave.setStudentId(getUserId(auth));
        adminService.applyLeave(leave);
        return Result.ok();
    }

    @PutMapping("/leaves/{id}/approve")
    public Result<Void> approveLeave(@PathVariable Long id, Authentication auth,
            @RequestParam Integer status, @RequestParam(required = false) String reason) {
        adminService.approveLeave(id, getUserId(auth), status,
                reason != null ? reason : "");
        return Result.ok();
    }

    @GetMapping("/guides")
    public Result<PageResult<Guide>> listGuides(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category) {
        return toPageResult(adminService.pageGuides(page, size, category));
    }

    @GetMapping("/guides/{id}")
    public Result<Guide> getGuide(@PathVariable Long id) {
        return Result.ok(adminService.getGuideById(id));
    }

    @PostMapping("/guides")
    public Result<Void> addGuide(@RequestBody Guide g, Authentication auth) {
        g.setPublisherId(getUserId(auth));
        adminService.saveGuide(g);
        return Result.ok();
    }

    @PutMapping("/guides/{id}")
    public Result<Void> updateGuide(@PathVariable Long id, @RequestBody Guide g) {
        adminService.updateGuide(id, g);
        return Result.ok();
    }

    @DeleteMapping("/guides/{id}")
    public Result<Void> delGuide(@PathVariable Long id) {
        adminService.deleteGuide(id);
        return Result.ok();
    }

    @GetMapping("/leaves/{id}")
    public Result<LeaveApplication> getLeave(@PathVariable Long id) {
        return Result.ok(adminService.getLeaveById(id));
    }

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

package com.campus.module.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.activity.entity.ActivityCenter;
import com.campus.module.activity.entity.ActivityRegistration;
import com.campus.module.activity.service.ActivityCenterService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
@Tag(name = "活动管理", description = "活动中心、报名、审批等校园活动功能")
public class ActivityCenterController {

    private final ActivityCenterService activityCenterService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    private String getRole(Authentication auth) {
        return ((Claims) auth.getDetails()).get("role", String.class);
    }

    @Operation(summary = "获取公开活动列表")
    @GetMapping("/public")
    public Result<PageResult<ActivityCenter>> publicList(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long clubId) {
        Long userId = auth != null ? getUserId(auth) : null;
        return toPageResult(activityCenterService.pagePublic(page, size, userId, clubId));
    }

    @Operation(summary = "创建活动")
    @PostMapping
    public Result<ActivityCenter> create(@RequestBody ActivityCenter activity, Authentication auth) {
        ActivityCenter result = activityCenterService.create(activity, getUserId(auth), getRole(auth));
        return Result.ok(result);
    }

    @Operation(summary = "查看我创建的活动")
    @GetMapping("/my")
    public Result<PageResult<ActivityCenter>> myCreated(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(activityCenterService.pageMyCreated(getUserId(auth), page, size));
    }

    @Operation(summary = "查看待审批活动列表")
    @GetMapping("/pending")
    public Result<PageResult<ActivityCenter>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(activityCenterService.pagePending(page, size));
    }

    @Operation(summary = "获取活动详情")
    @GetMapping("/{id}")
    public Result<ActivityCenter> detail(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? getUserId(auth) : null;
        return Result.ok(activityCenterService.getById(id, userId));
    }

    @Operation(summary = "审批活动")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, Authentication auth,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason) {
        activityCenterService.approve(id, getUserId(auth), status, rejectReason != null ? rejectReason : "");
        return Result.ok();
    }

    @Operation(summary = "确认活动开展")
    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, Authentication auth) {
        activityCenterService.confirm(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "撤回活动")
    @DeleteMapping("/{id}")
    public Result<Void> withdraw(@PathVariable Long id, Authentication auth) {
        activityCenterService.withdraw(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "结束活动")
    @PutMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id, Authentication auth) {
        activityCenterService.finish(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "报名活动")
    @PostMapping("/{id}/register")
    public Result<Void> register(@PathVariable Long id, Authentication auth) {
        activityCenterService.register(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "取消报名")
    @DeleteMapping("/{id}/register")
    public Result<Void> cancelRegister(@PathVariable Long id, Authentication auth) {
        activityCenterService.cancelRegistration(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "获取活动报名列表")
    @GetMapping("/{id}/registrations")
    public Result<PageResult<ActivityRegistration>> registrations(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(activityCenterService.pageRegistrations(id, page, size));
    }

    @Operation(summary = "查看我的报名列表")
    @GetMapping("/my-registrations")
    public Result<List<ActivityRegistration>> myRegistrations(Authentication auth) {
        return Result.ok(activityCenterService.getMyRegistrations(getUserId(auth)));
    }

    @Operation(summary = "更新活动总结")
    @PutMapping("/{id}/summary")
    public Result<Void> updateSummary(@PathVariable Long id, Authentication auth,
            @RequestParam String summary,
            @RequestParam(required = false) String images) {
        activityCenterService.updateSummary(id, getUserId(auth), summary, images);
        return Result.ok();
    }

    private <T> Result<PageResult<T>> toPageResult(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords());
        pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent());
        pr.setSize(p.getSize());
        pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

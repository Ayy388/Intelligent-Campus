package com.campus.module.activity.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.activity.entity.ActivityCenter;
import com.campus.module.activity.entity.ActivityRegistration;
import com.campus.module.activity.service.ActivityCenterService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityCenterController {

    private final ActivityCenterService activityCenterService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    private String getRole(Authentication auth) {
        return ((Claims) auth.getDetails()).get("role", String.class);
    }

    @GetMapping("/public")
    public Result<PageResult<ActivityCenter>> publicList(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long clubId) {
        Long userId = auth != null ? getUserId(auth) : null;
        return toPageResult(activityCenterService.pagePublic(page, size, userId, clubId));
    }

    @PostMapping
    public Result<ActivityCenter> create(@RequestBody ActivityCenter activity, Authentication auth) {
        ActivityCenter result = activityCenterService.create(activity, getUserId(auth), getRole(auth));
        return Result.ok(result);
    }

    @GetMapping("/my")
    public Result<PageResult<ActivityCenter>> myCreated(
            Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(activityCenterService.pageMyCreated(getUserId(auth), page, size));
    }

    @GetMapping("/pending")
    public Result<PageResult<ActivityCenter>> pending(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(activityCenterService.pagePending(page, size));
    }

    @GetMapping("/{id}")
    public Result<ActivityCenter> detail(@PathVariable Long id, Authentication auth) {
        Long userId = auth != null ? getUserId(auth) : null;
        return Result.ok(activityCenterService.getById(id, userId));
    }

    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, Authentication auth,
            @RequestParam Integer status,
            @RequestParam(required = false) String rejectReason) {
        activityCenterService.approve(id, getUserId(auth), status, rejectReason != null ? rejectReason : "");
        return Result.ok();
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirm(@PathVariable Long id, Authentication auth) {
        activityCenterService.confirm(id, getUserId(auth));
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> withdraw(@PathVariable Long id, Authentication auth) {
        activityCenterService.withdraw(id, getUserId(auth));
        return Result.ok();
    }

    @PutMapping("/{id}/finish")
    public Result<Void> finish(@PathVariable Long id, Authentication auth) {
        activityCenterService.finish(id, getUserId(auth));
        return Result.ok();
    }

    @PostMapping("/{id}/register")
    public Result<Void> register(@PathVariable Long id, Authentication auth) {
        activityCenterService.register(id, getUserId(auth));
        return Result.ok();
    }

    @DeleteMapping("/{id}/register")
    public Result<Void> cancelRegister(@PathVariable Long id, Authentication auth) {
        activityCenterService.cancelRegistration(id, getUserId(auth));
        return Result.ok();
    }

    @GetMapping("/{id}/registrations")
    public Result<PageResult<ActivityRegistration>> registrations(@PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPageResult(activityCenterService.pageRegistrations(id, page, size));
    }

    @GetMapping("/my-registrations")
    public Result<List<ActivityRegistration>> myRegistrations(Authentication auth) {
        return Result.ok(activityCenterService.getMyRegistrations(getUserId(auth)));
    }

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

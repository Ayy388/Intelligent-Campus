package com.campus.module.growth.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.growth.entity.*;
import com.campus.module.growth.service.GrowthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/growth")
@RequiredArgsConstructor
public class GrowthController {
    private final GrowthService growthService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @GetMapping("/profile")
    public Result<StudentProfile> profile(Authentication auth) {
        return Result.ok(growthService.getProfile(getUserId(auth)));
    }

    @PutMapping("/profile")
    public Result<Void> saveProfile(@RequestBody StudentProfile p, Authentication auth) {
        p.setStudentId(getUserId(auth));
        growthService.saveOrUpdateProfile(p);
        return Result.ok();
    }

    @PostMapping("/evaluation")
    public Result<Void> evaluation(@RequestParam Long studentId, @RequestParam String content, Authentication auth) {
        growthService.addEvaluation(studentId, content, getUserId(auth));
        return Result.ok();
    }

    @GetMapping("/checkin")
    public Result<PageResult<CheckIn>> checkins(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Claims claims = (Claims) auth.getDetails();
        String role = claims.get("role", String.class);
        Long teacherId = "teacher".equals(role) ? Long.parseLong(claims.getSubject()) : null;
        return toPage(growthService.pageCheckIns(teacherId, page, size));
    }

    @PostMapping("/checkin")
    public Result<CheckIn> createCheckIn(@RequestBody CheckIn c, Authentication auth) {
        c.setTeacherId(getUserId(auth));
        return Result.ok(growthService.createCheckIn(c));
    }

    @PostMapping("/checkin/do")
    public Result<CheckInRecord> doCheckIn(@RequestParam Long checkinId, Authentication auth) {
        return Result.ok(growthService.doCheckIn(checkinId, getUserId(auth)));
    }

    @GetMapping("/checkin/{id}/records")
    public Result<List<CheckInRecord>> records(@PathVariable Long id) {
        return Result.ok(growthService.getCheckInRecords(id));
    }

    @GetMapping("/checkin/{id}/status")
    public Result<Map<String, Boolean>> checkinStatus(@PathVariable Long id, Authentication auth) {
        return Result.ok(Map.of("checked", growthService.getCheckInStatus(id, getUserId(auth))));
    }

    private <T> Result<PageResult<T>> toPage(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

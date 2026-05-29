package com.campus.module.club.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.club.entity.*;
import com.campus.module.club.service.ClubService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club")
@RequiredArgsConstructor
public class ClubController {
    private final ClubService clubService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @GetMapping("/list")
    public Result<List<Club>> list() { return Result.ok(clubService.getClubs()); }

    @GetMapping("/{id}")
    public Result<Club> detail(@PathVariable Long id) { return Result.ok(clubService.getClubById(id)); }

    @PostMapping
    public Result<Void> create(@RequestBody Club c, Authentication auth) { 
        c.setPresidentId(getUserId(auth));
        clubService.saveClub(c); 
        return Result.ok(); 
    }
    
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status) {
        clubService.approveClub(id, status);
        return Result.ok();
    }

    @DeleteMapping("/member/{clubId}")
    public Result<Void> leaveClub(@PathVariable Long clubId, Authentication auth) {
        Long userId = getUserId(auth);
        clubService.leaveClub(clubId, userId);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody Club c) { clubService.updateClub(id, c); return Result.ok(); }

    @PostMapping("/member/apply")
    public Result<ClubMember> apply(@RequestParam Long clubId, @RequestParam String reason, Authentication auth) {
        return Result.ok(clubService.applyMember(clubId, getUserId(auth), reason));
    }

    @PutMapping("/member/{id}")
    public Result<Void> approveMember(@PathVariable Long id, @RequestParam Integer status) {
        clubService.approveMember(id, status); return Result.ok();
    }

    @GetMapping("/member/my")
    public Result<List<ClubMember>> myMembers(Authentication auth) {
        return Result.ok(clubService.getMyMemberships(getUserId(auth)));
    }

    @GetMapping("/{clubId}/members")
    public Result<List<ClubMember>> members(@PathVariable Long clubId) {
        return Result.ok(clubService.getMembers(clubId));
    }

    @GetMapping("/activity")
    public Result<PageResult<Activity>> activities(
            @RequestParam(required = false) Long clubId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(clubService.pageActivities(clubId, page, size));
    }

    @PostMapping("/activity")
    public Result<Void> createActivity(@RequestBody Activity a) { clubService.saveActivity(a); return Result.ok(); }

    @PostMapping("/activity/enroll")
    public Result<ActivityEnrollment> enroll(@RequestParam Long activityId, Authentication auth) {
        return Result.ok(clubService.enroll(activityId, getUserId(auth)));
    }

    @GetMapping("/activity/{id}/enrollments")
    public Result<List<ActivityEnrollment>> enrollments(@PathVariable Long id) {
        return Result.ok(clubService.getEnrollments(id));
    }

    @PutMapping("/activity/{id}/summary")
    public Result<Void> summary(@PathVariable Long id, @RequestParam String summary, @RequestParam(required = false) String images) {
        clubService.updateActivitySummary(id, summary, images != null ? images : ""); return Result.ok();
    }

    @GetMapping("/venue")
    public Result<List<Venue>> venues() { return Result.ok(clubService.getVenues()); }

    @GetMapping("/venue/booking")
    public Result<PageResult<VenueBooking>> bookings(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Claims claims = (Claims) auth.getDetails();
        return toPage(clubService.pageBookings(Long.parseLong(claims.getSubject()),
                claims.get("role", String.class), page, size));
    }

    @PostMapping("/venue/booking")
    public Result<Void> applyBooking(@RequestBody VenueBooking b, Authentication auth) {
        b.setUserId(getUserId(auth)); clubService.applyBooking(b); return Result.ok();
    }

    @PutMapping("/venue/booking/{id}")
    public Result<Void> approveBooking(@PathVariable Long id, Authentication auth,
            @RequestParam Integer status, @RequestParam(required = false) String reason) {
        clubService.approveBooking(id, getUserId(auth), status, reason != null ? reason : "");
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteClub(@PathVariable Long id) { clubService.deleteClub(id); return Result.ok(); }

    @DeleteMapping("/activity/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) { clubService.deleteActivity(id); return Result.ok(); }

    @DeleteMapping("/activity/enroll/{id}")
    public Result<Void> cancelEnroll(@PathVariable Long id, Authentication auth) {
        clubService.cancelEnroll(id, getUserId(auth)); return Result.ok();
    }

    @PostMapping("/venue/add")
    public Result<Void> addVenue(@RequestBody Venue v) { clubService.saveVenue(v); return Result.ok(); }

    @PutMapping("/venue/{id}")
    public Result<Void> updateVenue(@PathVariable Long id, @RequestBody Venue v) { clubService.updateVenue(id, v); return Result.ok(); }

    @DeleteMapping("/venue/{id}")
    public Result<Void> deleteVenue(@PathVariable Long id) { clubService.deleteVenue(id); return Result.ok(); }

    private <T> Result<PageResult<T>> toPage(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

package com.campus.module.club.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.club.entity.*;
import com.campus.module.club.service.ClubService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/club")
@RequiredArgsConstructor
@Tag(name = "社团管理", description = "社团创建、审批、成员管理等社团功能")
public class ClubController {
    private final ClubService clubService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @Operation(summary = "获取社团列表")
    @GetMapping("/list")
    @PreAuthorize("permitAll()")
    public Result<List<Club>> list() { return Result.ok(clubService.getClubs()); }

    @Operation(summary = "获取社团详情")
    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public Result<Club> detail(@PathVariable Long id) { return Result.ok(clubService.getClubById(id)); }

    @Operation(summary = "创建社团")
    @PostMapping
    @PreAuthorize("hasRole('student')")
    public Result<Void> create(@RequestBody Club c, Authentication auth) { 
        c.setPresidentId(getUserId(auth));
        clubService.saveClub(c); 
        return Result.ok(); 
    }
    
    @Operation(summary = "审批社团")
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> approve(@PathVariable Long id, @RequestParam Integer status) {
        clubService.approveClub(id, status);
        return Result.ok();
    }

    @Operation(summary = "退出社团")
    @DeleteMapping("/member/{clubId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> leaveClub(@PathVariable Long clubId, Authentication auth) {
        Long userId = getUserId(auth);
        clubService.leaveClub(clubId, userId);
        return Result.ok();
    }

    @Operation(summary = "申请解散社团")
    @PostMapping("/{id}/disband")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> disband(@PathVariable Long id, Authentication auth) {
        clubService.disbandClub(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "审批解散社团")
    @PostMapping("/{id}/approve-disband")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> approveDisband(@PathVariable Long id, @RequestParam Integer status) {
        clubService.approveDisband(id, status);
        return Result.ok();
    }

    @Operation(summary = "撤销解散申请")
    @PostMapping("/{id}/cancel-disband")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> cancelDisband(@PathVariable Long id, Authentication auth) {
        clubService.cancelDisband(id, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "移除社团成员")
    @DeleteMapping("/{clubId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> removeMember(@PathVariable Long clubId, @PathVariable Long memberId, Authentication auth) {
        clubService.removeMember(clubId, memberId, getUserId(auth));
        return Result.ok();
    }

    @Operation(summary = "转让社长")
    @PostMapping("/{clubId}/transfer/{targetUserId}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> transferPresident(@PathVariable Long clubId, @PathVariable Long targetUserId, Authentication auth) {
        clubService.transferPresidency(clubId, getUserId(auth), targetUserId);
        return Result.ok();
    }

    @Operation(summary = "设置成员角色")
    @PutMapping("/{clubId}/members/{memberId}/role")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> setMemberRole(@PathVariable Long clubId, @PathVariable Long memberId,
            @RequestParam String role, Authentication auth) {
        clubService.setMemberRole(clubId, memberId, getUserId(auth), role);
        return Result.ok();
    }

    @Operation(summary = "更新社团信息")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> update(@PathVariable Long id, @RequestBody Club c) { clubService.updateClub(id, c); return Result.ok(); }

    @Operation(summary = "申请加入社团")
    @PostMapping("/member/apply")
    @PreAuthorize("isAuthenticated()")
    public Result<ClubMember> apply(@RequestParam Long clubId, @RequestParam String reason, Authentication auth) {
        return Result.ok(clubService.applyMember(clubId, getUserId(auth), reason));
    }

    @Operation(summary = "审批成员申请")
    @PutMapping("/member/{id}")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> approveMember(@PathVariable Long id, @RequestParam Integer status, Authentication auth) {
        clubService.approveMember(id, status, getUserId(auth)); return Result.ok();
    }

    @Operation(summary = "查询我的社团列表")
    @GetMapping("/member/my")
    @PreAuthorize("isAuthenticated()")
    public Result<List<ClubMember>> myMembers(Authentication auth) {
        return Result.ok(clubService.getMyMemberships(getUserId(auth)));
    }

    @Operation(summary = "获取社团成员列表")
    @GetMapping("/{clubId}/members")
    @PreAuthorize("permitAll()")
    public Result<List<ClubMember>> members(@PathVariable Long clubId) {
        return Result.ok(clubService.getMembers(clubId));
    }

    @Operation(summary = "获取场地列表")
    @GetMapping("/venue")
    @PreAuthorize("isAuthenticated()")
    public Result<List<Venue>> venues() { return Result.ok(clubService.getVenues()); }

    @Operation(summary = "查询场地预约列表")
    @GetMapping("/venue/booking")
    @PreAuthorize("isAuthenticated()")
    public Result<PageResult<VenueBooking>> bookings(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Claims claims = (Claims) auth.getDetails();
        return toPage(clubService.pageBookings(Long.parseLong(claims.getSubject()),
                claims.get("role", String.class), page, size, status));
    }

    @Operation(summary = "预约场地")
    @PostMapping("/venue/booking")
    @PreAuthorize("isAuthenticated()")
    public Result<Void> applyBooking(@RequestBody VenueBooking b, Authentication auth) {
        b.setUserId(getUserId(auth)); clubService.applyBooking(b); return Result.ok();
    }

    @Operation(summary = "审批场地预约")
    @PutMapping("/venue/booking/{id}")
    @PreAuthorize("hasAnyRole('admin', 'teacher')")
    public Result<Void> approveBooking(@PathVariable Long id, Authentication auth,
            @RequestParam Integer status, @RequestParam(required = false) String reason) {
        clubService.approveBooking(id, getUserId(auth), status, reason != null ? reason : "");
        return Result.ok();
    }

    @Operation(summary = "删除社团")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deleteClub(@PathVariable Long id) { clubService.deleteClub(id); return Result.ok(); }

    @Operation(summary = "添加场地")
    @PostMapping("/venue/add")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> addVenue(@RequestBody Venue v) { clubService.saveVenue(v); return Result.ok(); }

    @Operation(summary = "更新场地信息")
    @PutMapping("/venue/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> updateVenue(@PathVariable Long id, @RequestBody Venue v) { clubService.updateVenue(id, v); return Result.ok(); }

    @Operation(summary = "删除场地")
    @DeleteMapping("/venue/{id}")
    @PreAuthorize("hasRole('admin')")
    public Result<Void> deleteVenue(@PathVariable Long id) { clubService.deleteVenue(id); return Result.ok(); }

    private <T> Result<PageResult<T>> toPage(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

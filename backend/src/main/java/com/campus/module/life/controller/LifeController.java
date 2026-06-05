package com.campus.module.life.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.life.entity.*;
import com.campus.module.life.service.LifeService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/life")
@RequiredArgsConstructor
@Tag(name = "校园生活", description = "一卡通充值、失物招领等校园生活服务")
public class LifeController {
    private final LifeService lifeService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @Operation(summary = "查询充值记录")
    @GetMapping("/card-recharge")
    public Result<PageResult<CardRecharge>> recharges(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(lifeService.pageRecharges(getUserId(auth), page, size));
    }

    @Operation(summary = "充值")
    @PostMapping("/card-recharge")
    public Result<Void> recharge(@RequestBody CardRecharge recharge, Authentication auth) {
        recharge.setUserId(getUserId(auth));
        lifeService.recharge(recharge);
        return Result.ok();
    }

    @Operation(summary = "分页查询失物招领")
    @GetMapping("/lost-found")
    public Result<PageResult<LostFound>> lostFound(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(lifeService.pageLostFound(keyword, type, page, size));
    }

    @Operation(summary = "获取失物招领详情")
    @GetMapping("/lost-found/{id}")
    public Result<LostFound> getLostFound(@PathVariable Long id) {
        return Result.ok(lifeService.getLostFoundById(id));
    }

    @Operation(summary = "发布失物招领")
    @PostMapping("/lost-found")
    public Result<Void> addLostFound(@RequestBody LostFound lf, Authentication auth) {
        lf.setUserId(getUserId(auth));
        lifeService.addLostFound(lf);
        return Result.ok();
    }

    @Operation(summary = "更新失物招领状态")
    @PutMapping("/lost-found/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        lifeService.updateLostFoundStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除失物招领")
    @DeleteMapping("/lost-found/{id}")
    public Result<Void> deleteLostFound(@PathVariable Long id, Authentication auth) {
        lifeService.deleteLostFound(id, getUserId(auth));
        return Result.ok();
    }

    private <T> Result<PageResult<T>> toPage(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

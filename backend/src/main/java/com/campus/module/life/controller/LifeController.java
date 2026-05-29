package com.campus.module.life.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.life.entity.*;
import com.campus.module.life.service.LifeService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/life")
@RequiredArgsConstructor
public class LifeController {
    private final LifeService lifeService;

    private Long getUserId(Authentication auth) {
        return Long.parseLong(((Claims) auth.getDetails()).getSubject());
    }

    @GetMapping("/canteens")
    public Result<List<Canteen>> canteens() {
        return Result.ok(lifeService.getCanteens());
    }

    @GetMapping("/canteen-reviews")
    public Result<PageResult<CanteenReview>> reviews(
            @RequestParam(required = false) Long canteenId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(lifeService.pageReviews(canteenId, page, size));
    }

    @PostMapping("/canteen-reviews")
    public Result<Void> addReview(@RequestBody CanteenReview review, Authentication auth) {
        review.setUserId(getUserId(auth));
        lifeService.addReview(review);
        return Result.ok();
    }

    @GetMapping("/card-recharge")
    public Result<PageResult<CardRecharge>> recharges(Authentication auth,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(lifeService.pageRecharges(getUserId(auth), page, size));
    }

    @PostMapping("/card-recharge")
    public Result<Void> recharge(@RequestBody CardRecharge recharge, Authentication auth) {
        recharge.setUserId(getUserId(auth));
        lifeService.recharge(recharge);
        return Result.ok();
    }

    @GetMapping("/lost-found")
    public Result<PageResult<LostFound>> lostFound(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return toPage(lifeService.pageLostFound(keyword, type, page, size));
    }

    @PostMapping("/lost-found")
    public Result<Void> addLostFound(@RequestBody LostFound lf, Authentication auth) {
        lf.setUserId(getUserId(auth));
        lifeService.addLostFound(lf);
        return Result.ok();
    }

    @PutMapping("/lost-found/{id}")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        lifeService.updateLostFoundStatus(id, status);
        return Result.ok();
    }

    private <T> Result<PageResult<T>> toPage(Page<T> p) {
        PageResult<T> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }
}

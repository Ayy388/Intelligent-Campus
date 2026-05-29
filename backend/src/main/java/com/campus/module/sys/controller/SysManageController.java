package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys")
@RequiredArgsConstructor
public class SysManageController {
    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        Page<SysUser> p = userService.pageUsers(page, size, keyword);
        PageResult<SysUser> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @PostMapping("/users")
    public Result<SysUser> create(@RequestBody SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/users/{id}")
    public Result<SysUser> update(@PathVariable Long id, @RequestBody SysUser user) {
        user.setId(id);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        userService.updateById(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser u = new SysUser(); u.setId(id); u.setStatus(status);
        userService.updateById(u);
        return Result.ok();
    }
}

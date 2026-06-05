package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sys")
@RequiredArgsConstructor
@Tag(name = "系统管理", description = "用户管理、角色权限等系统功能")
public class SysManageController {
    private final SysUserService userService;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/users")
    public Result<PageResult<SysUser>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId) {
        Page<SysUser> p = userService.pageUsers(page, size, keyword, roleId);
        PageResult<SysUser> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @Operation(summary = "创建用户")
    @PostMapping("/users")
    public Result<SysUser> create(@RequestBody SysUser user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userService.save(user);
        user.setPassword(null);
        return Result.ok(user);
    }

    @Operation(summary = "更新用户信息")
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

    @Operation(summary = "切换用户状态（启用/禁用）")
    @PutMapping("/users/{id}/status")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        SysUser u = new SysUser(); u.setId(id); u.setStatus(status);
        userService.updateById(u);
        return Result.ok();
    }

    @Operation(summary = "获取用户详情")
    @GetMapping("/users/{id}")
    public Result<SysUser> getUser(@PathVariable Long id) {
        SysUser user = userService.getById(id);
        if (user != null) userService.resolveUserExtra(user);
        return Result.ok(user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.removeById(id);
        return Result.ok();
    }
}

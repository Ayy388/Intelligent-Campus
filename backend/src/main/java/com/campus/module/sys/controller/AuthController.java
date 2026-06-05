package com.campus.module.sys.controller;

import com.campus.common.Result;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.service.SysUserService;
import com.campus.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理", description = "用户登录、个人信息管理")
public class AuthController {
    private final SysUserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<SysUser> me(@RequestHeader("Authorization") String auth) {
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        Long userId = jwtTokenProvider.getUserId(token);
        SysUser user = userService.getById(userId);
        user.setPassword(null);
        userService.resolveUserExtra(user);
        return Result.ok(user);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("Authorization") String auth,
                                      @RequestBody SysUser updated) {
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        Long userId = jwtTokenProvider.getUserId(token);
        userService.updateProfile(userId, updated);
        return Result.ok();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("Authorization") String auth,
                                       @RequestBody Map<String, String> body) {
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        Long userId = jwtTokenProvider.getUserId(token);
        userService.changePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.ok();
    }
}

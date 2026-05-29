package com.campus.module.sys.controller;

import com.campus.common.Result;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.service.SysUserService;
import com.campus.security.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final SysUserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(userService.login(req));
    }

    @GetMapping("/me")
    public Result<SysUser> me(@RequestHeader("Authorization") String auth) {
        String token = auth.startsWith("Bearer ") ? auth.substring(7) : auth;
        Long userId = jwtTokenProvider.getUserId(token);
        SysUser user = userService.getById(userId);
        user.setPassword(null);
        return Result.ok(user);
    }
}

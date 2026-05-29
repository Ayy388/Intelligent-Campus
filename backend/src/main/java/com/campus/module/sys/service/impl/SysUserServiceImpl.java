package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import com.campus.module.sys.entity.SysRole;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.SysRoleMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import com.campus.module.sys.service.SysUserService;
import com.campus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public LoginResponse login(LoginRequest request) {
        SysUser user = getByUsername(request.getUsername());
        if (user == null) throw new BusinessException(401, "用户名或密码错误");
        if (user.getStatus() == 0) throw new BusinessException(403, "账号已被禁用");
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new BusinessException(401, "用户名或密码错误");
        SysRole role = roleMapper.selectById(user.getRoleId());
        String roleCode = role != null ? role.getRoleCode() : "student";
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername(), roleCode);
        return new LoginResponse(token, user.getId(), user.getUsername(),
                user.getRealName(), roleCode, user.getAvatar());
    }

    @Override
    public SysUser getByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
    }

    @Override
    public Page<SysUser> pageUsers(int page, int size, String keyword) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(SysUser::getRealName, keyword).or().like(SysUser::getUsername, keyword);
        }
        return userMapper.selectPage(new Page<>(page, size), w);
    }
}

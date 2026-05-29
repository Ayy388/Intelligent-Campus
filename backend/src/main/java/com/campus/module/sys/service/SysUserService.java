package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import com.campus.module.sys.entity.SysUser;

public interface SysUserService extends IService<SysUser> {
    LoginResponse login(LoginRequest request);
    SysUser getByUsername(String username);
    Page<SysUser> pageUsers(int page, int size, String keyword);
}

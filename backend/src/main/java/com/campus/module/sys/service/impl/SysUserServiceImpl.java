package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.BusinessException;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import com.campus.module.sys.entity.SysRole;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.mapper.SysRoleMapper;
import com.campus.module.sys.mapper.SysUserMapper;
import com.campus.module.sys.mapper.SysDepartmentMapper;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.mapper.SysGradeMapper;
import com.campus.module.sys.service.SysUserService;
import com.campus.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {
    private final SysUserMapper userMapper;
    private final SysRoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SysDepartmentMapper departmentMapper;
    private final SysClassMapper classMapper;
    private final SysMajorMapper majorMapper;
    private final SysGradeMapper gradeMapper;

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
    @Transactional
    public void updateProfile(Long userId, SysUser updated) {
        SysUser user = getById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getAvatar() != null) user.setAvatar(updated.getAvatar());
        updateById(user);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getById(userId);
        if (user == null) throw new BusinessException(404, "用户不存在");
        if (!passwordEncoder.matches(oldPassword, user.getPassword()))
            throw new BusinessException(400, "原密码错误");
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
    }

    @Override
    public Page<SysUser> pageUsers(int page, int size, String keyword, Long roleId) {
        LambdaQueryWrapper<SysUser> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            w.like(SysUser::getRealName, keyword).or().like(SysUser::getUsername, keyword);
        }
        if (roleId != null) {
            w.eq(SysUser::getRoleId, roleId);
        }
        Page<SysUser> result = userMapper.selectPage(new Page<>(page, size), w);
        for (SysUser u : result.getRecords()) {
            resolveUserExtra(u);
        }
        return result;
    }

    public void resolveUserExtra(SysUser u) {
        if (u.getRoleId() != null) {
            SysRole role = roleMapper.selectById(u.getRoleId());
            if (role != null) u.setRoleName(role.getRoleName());
        }
        if (u.getDepartmentId() != null) {
            var dept = departmentMapper.selectById(u.getDepartmentId());
            if (dept != null) u.setDepartmentName(dept.getName());
        }
        if (u.getMajorId() != null) {
            SysMajor m = majorMapper.selectById(u.getMajorId());
            if (m != null) u.setMajorName(m.getName());
        }
        if (u.getClassId() != null) {
            SysClass c = classMapper.selectById(u.getClassId());
            if (c != null) {
                u.setClassName(c.getClassName());
                if (u.getMajorId() == null && c.getMajorId() != null) {
                    SysMajor m = majorMapper.selectById(c.getMajorId());
                    if (m != null) u.setMajorName(m.getName());
                }
                if (c.getGradeId() != null) {
                    SysGrade g = gradeMapper.selectById(c.getGradeId());
                    if (g != null) u.setGradeName(g.getName());
                }
            }
        }
        if (u.getCounselorId() != null) {
            SysUser counselor = userMapper.selectById(u.getCounselorId());
            if (counselor != null) u.setCounselorName(counselor.getRealName());
        } else if (u.getClassId() != null) {
            SysClass c = classMapper.selectById(u.getClassId());
            if (c != null && c.getCounselorId() != null) {
                SysUser counselor = userMapper.selectById(c.getCounselorId());
                if (counselor != null) u.setCounselorName(counselor.getRealName());
            }
        }
        // 辅导员查询其负责的班级
        if (u.getRoleId() != null && u.getRoleId() == 4L) {
            StringBuilder sb = new StringBuilder();
            for (SysClass sc : classMapper.selectList(
                    new LambdaQueryWrapper<SysClass>().eq(SysClass::getCounselorId, u.getId()))) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(sc.getClassName());
            }
            u.setCounselorClasses(sb.toString());
        }
    }
}

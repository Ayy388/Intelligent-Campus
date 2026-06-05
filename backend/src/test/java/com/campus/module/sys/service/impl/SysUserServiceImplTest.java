package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.common.BusinessException;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import com.campus.module.sys.entity.SysRole;
import com.campus.module.sys.entity.SysUser;
import com.campus.module.sys.mapper.*;
import com.campus.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private SysDepartmentMapper departmentMapper;

    @Mock
    private SysClassMapper classMapper;

    @Mock
    private SysMajorMapper majorMapper;

    @Mock
    private SysGradeMapper gradeMapper;

    @InjectMocks
    private SysUserServiceImpl sysUserService;

    private SysUser sampleUser;
    private SysRole sampleRole;

    @BeforeEach
    void setUp() {
        sampleUser = new SysUser();
        sampleUser.setId(1L);
        sampleUser.setUsername("student1");
        sampleUser.setPassword("$2a$10$encodedPassword");
        sampleUser.setRealName("张三");
        sampleUser.setRoleId(1L);
        sampleUser.setStatus(1);

        sampleRole = new SysRole();
        sampleRole.setId(1L);
        sampleRole.setRoleCode("student");
        sampleRole.setRoleName("学生");

        // Set baseMapper to the userMapper mock (for ServiceImpl.getById/updateById)
        ReflectionTestUtils.setField(sysUserService, "baseMapper", userMapper);
    }

    // ===== login =====

    @Test
    @DisplayName("登录成功 - 应返回有效的 LoginResponse")
    void testLogin_whenValidCredentials_shouldReturnLoginResponse() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleUser);
        when(passwordEncoder.matches("123456", sampleUser.getPassword())).thenReturn(true);
        when(roleMapper.selectById(1L)).thenReturn(sampleRole);
        when(jwtTokenProvider.generateToken(1L, "student1", "student")).thenReturn("mock-token");

        // Act
        LoginResponse response = sysUserService.login(request);

        // Assert
        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals(1L, response.getUserId());
        assertEquals("student1", response.getUsername());
        assertEquals("张三", response.getRealName());
        assertEquals("student", response.getRole());
    }

    @Test
    @DisplayName("登录密码错误 - 应抛出 BusinessException")
    void testLogin_whenWrongPassword_shouldThrowException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("student1");
        request.setPassword("wrong-password");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(sampleUser);
        when(passwordEncoder.matches("wrong-password", sampleUser.getPassword())).thenReturn(false);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login(request));
        assertEquals(401, ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    @Test
    @DisplayName("登录用户不存在 - 应抛出 BusinessException")
    void testLogin_whenUserNotFound_shouldThrowException() {
        // Arrange
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistent");
        request.setPassword("123456");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.login(request));
        assertEquals(401, ex.getCode());
        assertEquals("用户名或密码错误", ex.getMessage());
    }

    // ===== updateProfile =====

    @Test
    @DisplayName("更新个人信息成功 - 应更新邮箱和头像")
    void testUpdateProfile_whenValidRequest_shouldUpdateEmailAndAvatar() {
        // Arrange
        Long userId = 1L;
        SysUser updated = new SysUser();
        updated.setEmail("newemail@test.com");
        updated.setAvatar("https://avatar.new.jpg");

        when(userMapper.selectById(userId)).thenReturn(sampleUser);

        // Act
        sysUserService.updateProfile(userId, updated);

        // Assert
        verify(userMapper).updateById(argThat(user ->
                "newemail@test.com".equals(user.getEmail())
                && "https://avatar.new.jpg".equals(user.getAvatar())
                && "张三".equals(user.getRealName()) // original values preserved
        ));
    }

    @Test
    @DisplayName("更新不存在的用户 - 应抛出 BusinessException")
    void testUpdateProfile_whenUserNotFound_shouldThrowException() {
        // Arrange
        when(userMapper.selectById(999L)).thenReturn(null);

        // Act & Assert
        BusinessException ex = assertThrows(BusinessException.class,
                () -> sysUserService.updateProfile(999L, new SysUser()));
        assertEquals(404, ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
    }

    @Test
    @DisplayName("更新个人信息只传部分字段 - 应只更新非空字段")
    void testUpdateProfile_whenPartialUpdate_shouldOnlyUpdateNonNullFields() {
        // Arrange
        Long userId = 1L;
        SysUser updated = new SysUser();
        updated.setEmail("newemail@test.com");
        // avatar is null - should not overwrite

        when(userMapper.selectById(userId)).thenReturn(sampleUser);

        // Act
        sysUserService.updateProfile(userId, updated);

        // Assert
        verify(userMapper).updateById(argThat(user ->
                "newemail@test.com".equals(user.getEmail())
                && sampleUser.getAvatar() == null // original was null
        ));
    }
}
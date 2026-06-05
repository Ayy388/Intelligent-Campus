package com.campus.module.edu;

import com.campus.common.Result;
import com.campus.module.sys.dto.LoginRequest;
import com.campus.module.sys.dto.LoginResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Create tables
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_role (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                role_code VARCHAR(20) NOT NULL UNIQUE,
                role_name VARCHAR(50) NOT NULL,
                description VARCHAR(200),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_user (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                real_name VARCHAR(50) NOT NULL,
                gender TINYINT DEFAULT 0,
                phone VARCHAR(20),
                email VARCHAR(100),
                avatar VARCHAR(255),
                role_id BIGINT NOT NULL,
                department_id BIGINT,
                major_id BIGINT,
                class_id BIGINT,
                counselor_id BIGINT,
                status TINYINT DEFAULT 1,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_course (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                course_code VARCHAR(20) NOT NULL UNIQUE,
                course_name VARCHAR(100) NOT NULL,
                teacher_id BIGINT,
                credit DECIMAL(3,1) DEFAULT 0,
                hours INT DEFAULT 0,
                semester VARCHAR(20) NOT NULL,
                classroom VARCHAR(100),
                schedule VARCHAR(200),
                start_week INT DEFAULT 1,
                end_week INT DEFAULT 20,
                capacity INT DEFAULT 0,
                course_type VARCHAR(20) DEFAULT 'required',
                min_students INT DEFAULT 0,
                enroll_end TIMESTAMP NULL,
                enrolled INT DEFAULT 0,
                description TEXT,
                status TINYINT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_course_selection (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                student_id BIGINT NOT NULL,
                course_id BIGINT NOT NULL,
                semester VARCHAR(20) NOT NULL,
                select_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status TINYINT DEFAULT 1,
                select_type VARCHAR(20) DEFAULT 'manual'
            )
        """);

        // Insert roles if not exist
        Long roleCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Long.class);
        if (roleCount == 0) {
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('student','学生')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('teacher','教师')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('admin','管理员')");
        }

        // Insert test users if not exist
        Long userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Long.class);
        if (userCount == 0) {
            String encodedPassword = passwordEncoder.encode("123456");
            jdbcTemplate.update("INSERT INTO sys_user (username, password, real_name, role_id, status) VALUES (?,?,?,?,?)",
                    "admin", encodedPassword, "系统管理员", 3L, 1);
            jdbcTemplate.update("INSERT INTO sys_user (username, password, real_name, role_id, status) VALUES (?,?,?,?,?)",
                    "s001", encodedPassword, "测试学生", 1L, 1);
        }
    }

    @Test
    @DisplayName("登录成功 - 正确用户名密码返回 JWT Token")
    void testLogin_whenValidCredentials_shouldReturnToken() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("123456");

        ResponseEntity<Result> resp = restTemplate.postForEntity(
                "/api/auth/login", req, Result.class);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        assertNotNull(resp.getBody());
        assertEquals(200, resp.getBody().getCode());
        assertNotNull(resp.getBody().getData());
    }

    @Test
    @DisplayName("登录失败 - 错误密码返回 401")
    void testLogin_whenInvalidPassword_shouldReturn401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrongpassword");

        ResponseEntity<Result> resp = restTemplate.postForEntity(
                "/api/auth/login", req, Result.class);

        // BusinessException with code 401 returns Result with code 401
        assertNotNull(resp.getBody());
        assertEquals(401, resp.getBody().getCode());
    }

    @Test
    @DisplayName("登录失败 - 不存在的用户名返回 401")
    void testLogin_whenUserNotFound_shouldReturn401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("nonexistent");
        req.setPassword("123456");

        ResponseEntity<Result> resp = restTemplate.postForEntity(
                "/api/auth/login", req, Result.class);

        assertNotNull(resp.getBody());
        assertEquals(401, resp.getBody().getCode());
    }

    @Test
    @DisplayName("无 Token 访问受保护接口 - 返回 403 Forbidden")
    void testAccessWithoutToken_shouldReturn403() {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                "/api/edu/courses", String.class);

        assertEquals(HttpStatus.FORBIDDEN, resp.getStatusCode());
    }
}
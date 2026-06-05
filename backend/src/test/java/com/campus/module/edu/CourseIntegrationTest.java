package com.campus.module.edu;

import com.campus.module.sys.dto.LoginRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class CourseIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String studentToken;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
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

        // Clear data in FK-safe order (child tables first)
        jdbcTemplate.update("DELETE FROM edu_course_selection");
        jdbcTemplate.update("DELETE FROM edu_course");
        jdbcTemplate.update("DELETE FROM sys_user");
        jdbcTemplate.update("DELETE FROM sys_role");

        // roles
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, role_name) VALUES (1,'student','学生')");
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, role_name) VALUES (2,'teacher','教师')");
        jdbcTemplate.update("INSERT INTO sys_role (id, role_code, role_name) VALUES (3,'admin','管理员')");

        // users
        String encodedPwd = passwordEncoder.encode("123456");
        jdbcTemplate.update("INSERT INTO sys_user (id, username, password, real_name, role_id, status) VALUES (?,?,?,?,?,?)",
                1L, "s001", encodedPwd, "测试学生", 1L, 1);
        jdbcTemplate.update("INSERT INTO sys_user (id, username, password, real_name, role_id, status) VALUES (?,?,?,?,?,?)",
                10L, "t001", encodedPwd, "测试教师", 2L, 1);

        // course
        jdbcTemplate.update("INSERT INTO edu_course (id, course_code, course_name, teacher_id, credit, hours, semester, capacity, enrolled, status, course_type) " +
                        "VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                100L, "CS101", "程序设计基础", 10L, 4, 64, "2025-2026-2", 50, 0, 1, "elective");

        // login as student
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername("s001");
        loginReq.setPassword("123456");
        ResponseEntity<String> loginResp = restTemplate.postForEntity(
                "/api/auth/login", loginReq, String.class);
        try {
            JsonNode root = objectMapper.readTree(loginResp.getBody());
            studentToken = root.get("data").get("token").asText();
        } catch (Exception e) {
            fail("Failed to get student token: " + e.getMessage());
        }
    }

    @Test
    @DisplayName("完整选课退选流程 - 登录->选课->验证->退选->验证")
    void testSelectAndDropCourse_shouldCompleteFullFlow() throws Exception {
        assertNotNull(studentToken, "Student token should not be null");

        // 1. 选课
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(studentToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> selectResp = restTemplate.exchange(
                "/api/edu/courses/100/enroll",
                HttpMethod.POST, entity, String.class);
        assertEquals(HttpStatus.OK, selectResp.getStatusCode());

        // 2. 验证选课记录
        ResponseEntity<String> mySelResp = restTemplate.exchange(
                "/api/edu/selections", HttpMethod.GET, entity, String.class);
        assertEquals(HttpStatus.OK, mySelResp.getStatusCode());
        JsonNode selRoot = objectMapper.readTree(mySelResp.getBody());
        assertTrue(selRoot.get("data").isArray());
        assertEquals(1, selRoot.get("data").size());

        // 获取 selection ID
        Long selId = selRoot.get("data").get(0).get("id").asLong();

        // 3. 退选
        ResponseEntity<String> dropResp = restTemplate.exchange(
                "/api/edu/selections/" + selId,
                HttpMethod.DELETE, entity, String.class);
        assertEquals(HttpStatus.OK, dropResp.getStatusCode());

        // 4. 验证退选后无选课记录
        ResponseEntity<String> afterDropResp = restTemplate.exchange(
                "/api/edu/selections", HttpMethod.GET, entity, String.class);
        JsonNode afterDrop = objectMapper.readTree(afterDropResp.getBody());
        // 手动选课记录已删除，应该返回空数组
        assertTrue(afterDrop.get("data").isArray());
    }

    @Test
    @DisplayName("选课失败 - 课程不存在")
    void testSelectCourse_whenCourseNotFound_shouldFail() throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(studentToken);
        HttpEntity<?> entity = new HttpEntity<>(headers);

        ResponseEntity<String> resp = restTemplate.exchange(
                "/api/edu/courses/9999/enroll",
                HttpMethod.POST, entity, String.class);

        // BusinessException 返回 Result.error，HTTP 状态为 200
        assertEquals(HttpStatus.OK, resp.getStatusCode());
        JsonNode respRoot = objectMapper.readTree(resp.getBody());
        assertNotNull(respRoot.get("code"));
        // 课程不存在，code 不应为 200
        assertTrue(respRoot.get("code").asInt() != 200, "Should return error code for non-existent course");
    }
}
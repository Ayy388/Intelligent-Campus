package com.campus.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
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
                department VARCHAR(100),
                class_name VARCHAR(100),
                status TINYINT DEFAULT 1,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (role_id) REFERENCES sys_role(id)
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
                capacity INT DEFAULT 0,
                enrolled INT DEFAULT 0,
                description TEXT,
                status TINYINT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
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
                UNIQUE (student_id, course_id, semester),
                FOREIGN KEY (student_id) REFERENCES sys_user(id),
                FOREIGN KEY (course_id) REFERENCES edu_course(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_grade (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                student_id BIGINT NOT NULL,
                course_id BIGINT NOT NULL,
                teacher_id BIGINT NOT NULL,
                score DECIMAL(5,2),
                grade_type VARCHAR(20) DEFAULT '百分制',
                semester VARCHAR(20) NOT NULL,
                remark VARCHAR(255),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES sys_user(id),
                FOREIGN KEY (course_id) REFERENCES edu_course(id),
                FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS admin_notification (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                category VARCHAR(20) DEFAULT 'general',
                publisher_id BIGINT NOT NULL,
                is_top TINYINT DEFAULT 0,
                is_urgent TINYINT DEFAULT 0,
                view_count INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS admin_leave (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                student_id BIGINT NOT NULL,
                leave_type VARCHAR(20) NOT NULL,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                reason TEXT NOT NULL,
                attachment VARCHAR(500),
                teacher_id BIGINT,
                status TINYINT DEFAULT 0,
                reject_reason VARCHAR(500),
                apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                approve_time TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES sys_user(id),
                FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS admin_guide (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                category VARCHAR(20) DEFAULT 'general',
                publisher_id BIGINT NOT NULL,
                view_count INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS ai_conversation (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(200),
                message_count INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS ai_message (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                conversation_id BIGINT NOT NULL,
                role VARCHAR(20) NOT NULL,
                content TEXT NOT NULL,
                token_count INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (conversation_id) REFERENCES ai_conversation(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS life_canteen (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                location VARCHAR(200),
                description VARCHAR(500),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS life_canteen_review (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                canteen_id BIGINT NOT NULL,
                rating TINYINT DEFAULT 5,
                taste_rating TINYINT DEFAULT 5,
                price_rating TINYINT DEFAULT 5,
                service_rating TINYINT DEFAULT 5,
                content TEXT,
                images VARCHAR(500),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES sys_user(id),
                FOREIGN KEY (canteen_id) REFERENCES life_canteen(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS life_card_recharge (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                amount DECIMAL(10,2) NOT NULL,
                balance DECIMAL(10,2) DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS life_lost_found (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                type TINYINT DEFAULT 0,
                title VARCHAR(200) NOT NULL,
                description TEXT,
                images VARCHAR(500),
                location VARCHAR(200),
                contact VARCHAR(100),
                status TINYINT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS message_conversation (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user1_id BIGINT NOT NULL,
                user2_id BIGINT NOT NULL,
                last_message VARCHAR(500),
                last_time TIMESTAMP,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user1_id) REFERENCES sys_user(id),
                FOREIGN KEY (user2_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS message_detail (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                conversation_id BIGINT NOT NULL,
                sender_id BIGINT NOT NULL,
                content TEXT NOT NULL,
                is_read TINYINT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (conversation_id) REFERENCES message_conversation(id),
                FOREIGN KEY (sender_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS message_announcement_push (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                content TEXT NOT NULL,
                target_type VARCHAR(20) DEFAULT 'all',
                target_value VARCHAR(100),
                publisher_id BIGINT NOT NULL,
                send_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                status TINYINT DEFAULT 1,
                FOREIGN KEY (publisher_id) REFERENCES sys_user(id)
            )
        """);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('student','学生')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('teacher','教师')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('admin','管理员')");
        }

        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
        if (userCount != null && userCount == 0) {
            String encodedPassword = passwordEncoder.encode("123456");
            jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                "admin", encodedPassword, "系统管理员", 3L, "信息中心", 1);
            jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                "t001", encodedPassword, "张教授", 2L, "计算机学院", 1);
            jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                "s001", encodedPassword, "李同学", 1L, "计算机学院", 1);
        }

        Integer canteenCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM life_canteen", Integer.class);
        if (canteenCount != null && canteenCount == 0) {
            jdbcTemplate.update("INSERT INTO life_canteen (name, location, description) VALUES (?,?,?)",
                "第一食堂", "东区一楼", "主营中式快餐、面食、小炒");
            jdbcTemplate.update("INSERT INTO life_canteen (name, location, description) VALUES (?,?,?)",
                "第二食堂", "西区二楼", "主营西式简餐、烘焙、咖啡");
            jdbcTemplate.update("INSERT INTO life_canteen (name, location, description) VALUES (?,?,?)",
                "教工餐厅", "行政楼负一层", "教工自助餐、小火锅");
        }
    }
}

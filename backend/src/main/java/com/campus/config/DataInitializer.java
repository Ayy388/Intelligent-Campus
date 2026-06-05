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
                class_id BIGINT,
                department_id BIGINT,
                major_id BIGINT,
                counselor_id BIGINT,
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
                start_week INT DEFAULT 1,
                end_week INT DEFAULT 20,
                capacity INT DEFAULT 0,
                enrolled INT DEFAULT 0,
                description TEXT,
                status TINYINT DEFAULT 0,
                course_type VARCHAR(20) DEFAULT 'required',
                min_students INT DEFAULT 1,
                enroll_end DATETIME,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_semester (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                xn VARCHAR(20) NOT NULL,
                xqjc VARCHAR(10) NOT NULL,
                xqqc VARCHAR(50) NOT NULL,
                ksrq DATE NOT NULL,
                jsrq DATE NOT NULL,
                zc INT DEFAULT 20,
                status TINYINT DEFAULT 1,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (xn, xqjc)
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
                select_type VARCHAR(20) DEFAULT 'manual',
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
                grade_level VARCHAR(10),
                semester VARCHAR(20) NOT NULL,
                remark VARCHAR(255),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES sys_user(id),
                FOREIGN KEY (course_id) REFERENCES edu_course(id),
                FOREIGN KEY (teacher_id) REFERENCES sys_user(id),
                UNIQUE KEY uk_student_course_semester (student_id, course_id, semester)
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

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS club_info (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                description TEXT,
                logo VARCHAR(255),
                advisor_id BIGINT,
                president_id BIGINT,
                member_count INT DEFAULT 0,
                status TINYINT DEFAULT 1,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (advisor_id) REFERENCES sys_user(id),
                FOREIGN KEY (president_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS club_member (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                club_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                role VARCHAR(20) DEFAULT 'member',
                status TINYINT DEFAULT 0,
                apply_reason VARCHAR(500),
                apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                approve_time TIMESTAMP,
                FOREIGN KEY (club_id) REFERENCES club_info(id),
                FOREIGN KEY (user_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS club_activity (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                club_id BIGINT NOT NULL,
                title VARCHAR(200) NOT NULL,
                description TEXT,
                activity_type VARCHAR(50),
                location VARCHAR(200),
                start_time TIMESTAMP,
                end_time TIMESTAMP,
                max_enroll INT DEFAULT 0,
                enrolled INT DEFAULT 0,
                summary TEXT,
                images VARCHAR(500),
                status TINYINT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (club_id) REFERENCES club_info(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS club_activity_enrollment (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                activity_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                status TINYINT DEFAULT 1,
                enroll_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (activity_id) REFERENCES club_activity(id),
                FOREIGN KEY (user_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS club_venue (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                location VARCHAR(200),
                capacity INT DEFAULT 0,
                description VARCHAR(500),
                status TINYINT DEFAULT 1,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS club_venue_booking (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                venue_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                title VARCHAR(200) NOT NULL,
                purpose TEXT,
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                approver_id BIGINT,
                status TINYINT DEFAULT 0,
                reject_reason VARCHAR(500),
                apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                approve_time TIMESTAMP,
                FOREIGN KEY (venue_id) REFERENCES club_venue(id),
                FOREIGN KEY (user_id) REFERENCES sys_user(id),
                FOREIGN KEY (approver_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS user_todo (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                title VARCHAR(300) NOT NULL,
                completed TINYINT DEFAULT 0,
                priority TINYINT DEFAULT 1,
                due_date VARCHAR(20),
                sort_order INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (user_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS admin_notification_read (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                notification_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                read_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE (notification_id, user_id)
            )
        """);

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('student','学生')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('teacher','教师')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('admin','管理员')");
        }
        // 增量添加辅导员角色（兼容已有数据的情况）
        try {
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('counselor','辅导员')");
        } catch (Exception ignored) {}

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_class (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                class_name VARCHAR(100) NOT NULL,
                department VARCHAR(100),
                major VARCHAR(100),
                grade VARCHAR(20),
                advisor VARCHAR(50),
                department_id BIGINT,
                major_id BIGINT,
                grade_id BIGINT,
                counselor_id BIGINT,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_course_class (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                course_id BIGINT NOT NULL,
                class_id BIGINT,
                is_required TINYINT(1) DEFAULT 0
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_training_plan (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                major_id BIGINT NOT NULL,
                grade_id BIGINT NOT NULL,
                total_semesters INT DEFAULT 8,
                status TINYINT DEFAULT 0,
                description VARCHAR(500),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                UNIQUE KEY uk_plan_major_grade (major_id, grade_id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS edu_training_plan_item (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                plan_id BIGINT NOT NULL,
                semester_number INT NOT NULL,
                course_name VARCHAR(100) NOT NULL,
                course_code VARCHAR(20),
                credit DECIMAL(3,1) DEFAULT 0,
                hours INT DEFAULT 0,
                is_required TINYINT(1) DEFAULT 1,
                status TINYINT DEFAULT 0,
                generated_course_id BIGINT,
                sort_order INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // 迁移数据：将 sys_user.class_name 中的班级名称转为 sys_class 记录，并更新 class_id
        try {
            Integer needMigrate = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE class_name IS NOT NULL AND class_name != '' AND class_id IS NULL",
                Integer.class);
            if (needMigrate != null && needMigrate > 0) {
                jdbcTemplate.update("INSERT IGNORE INTO sys_class (class_name) SELECT DISTINCT class_name FROM sys_user WHERE class_name IS NOT NULL AND class_name != ''");
                jdbcTemplate.update(
                    "UPDATE sys_user u JOIN sys_class c ON u.class_name = c.class_name SET u.class_id = c.id WHERE u.class_id IS NULL");
            }
        } catch (Exception ignored) {}

        // 创建院系/专业/年级字典表
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_department (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                code VARCHAR(20),
                sort_order INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_major (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                code VARCHAR(20),
                department_id BIGINT,
                years INT DEFAULT 4,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_grade (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                year INT NOT NULL UNIQUE,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        // 迁移院系数据
        try {
            Integer cnt = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_department", Integer.class);
            if (cnt != null && cnt == 0) {
                jdbcTemplate.update("INSERT INTO sys_department (name) SELECT DISTINCT department FROM sys_user WHERE department IS NOT NULL AND department != ''");
                jdbcTemplate.update("UPDATE sys_user u JOIN sys_department d ON u.department = d.name SET u.department_id = d.id WHERE u.department_id IS NULL");
            }
        } catch (Exception ignored) {}

        // 迁移班级院系/专业/年级数据
        try {
            jdbcTemplate.update("INSERT IGNORE INTO sys_department (name) SELECT DISTINCT department FROM sys_class WHERE department IS NOT NULL AND department != ''");
            jdbcTemplate.update("UPDATE sys_class c JOIN sys_department d ON c.department = d.name SET c.department_id = d.id WHERE c.department_id IS NULL");

            jdbcTemplate.update("INSERT IGNORE INTO sys_major (name, department_id) SELECT DISTINCT s.major, s.department_id FROM sys_class s WHERE s.major IS NOT NULL AND s.major != ''");
            jdbcTemplate.update("UPDATE sys_class c JOIN sys_major m ON c.major = m.name AND c.department_id = m.department_id SET c.major_id = m.id WHERE c.major_id IS NULL");

            jdbcTemplate.update("INSERT IGNORE INTO sys_grade (name, year) SELECT DISTINCT grade, CAST(grade AS UNSIGNED) FROM sys_class WHERE grade IS NOT NULL AND grade != ''");
            jdbcTemplate.update("UPDATE sys_class c JOIN sys_grade g ON c.grade = g.name SET c.grade_id = g.id WHERE c.grade_id IS NULL");
        } catch (Exception ignored) {}

        Integer userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user", Integer.class);
        if (userCount != null && userCount == 0) {
            String encodedPassword = passwordEncoder.encode("123456");
            jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                "admin", encodedPassword, "系统管理员", 3L, "信息中心", 1);
            jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                "t001", encodedPassword, "张教授", 2L, "计算机科学与技术学院", 1);
            jdbcTemplate.update(
                "INSERT INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                "s001", encodedPassword, "李同学", 1L, "计算机科学与技术学院", 1);
        }
        // 增量添加辅导员账号
        try {
            String pwd = passwordEncoder.encode("123456");
            Long counselorRoleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE role_code = 'counselor'", Long.class);
            if (counselorRoleId != null) {
                jdbcTemplate.update(
                    "INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department, status) VALUES (?,?,?,?,?,?)",
                    "c001", pwd, "王辅导员", counselorRoleId, "计算机科学与技术学院", 1);
            }
        } catch (Exception ignored) {}

        // --- 活动中心 ---
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS activity_center (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                title VARCHAR(200) NOT NULL,
                description TEXT,
                location VARCHAR(200),
                start_time TIMESTAMP,
                end_time TIMESTAMP,
                max_participants INT DEFAULT 0,
                current_participants INT DEFAULT 0,
                cover_image VARCHAR(500),
                category VARCHAR(50),
                club_id BIGINT,
                summary TEXT,
                images VARCHAR(500),
                activity_type VARCHAR(50),
                creator_id BIGINT NOT NULL,
                creator_role VARCHAR(20) NOT NULL,
                status TINYINT DEFAULT 0,
                approver_id BIGINT,
                reject_reason VARCHAR(500),
                approve_time TIMESTAMP,
                confirm_time TIMESTAMP,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (creator_id) REFERENCES sys_user(id),
                FOREIGN KEY (approver_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS activity_registration (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                activity_id BIGINT NOT NULL,
                user_id BIGINT NOT NULL,
                register_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (activity_id) REFERENCES activity_center(id),
                FOREIGN KEY (user_id) REFERENCES sys_user(id),
                UNIQUE (activity_id, user_id)
            )
        """);

        // 填充完整测试数据
        try {
            seedComprehensiveData(passwordEncoder.encode("123456"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 兜底：确保辅导员分配到班级（即使 seedComprehensiveData 部分失败）
        try {
            String[] deptNames = {"计算机科学与技术学院", "经济管理学院"};
            for (String dName : deptNames) {
                Long deptId = jdbcTemplate.queryForObject("SELECT id FROM sys_department WHERE name=?", Long.class, dName);
                Long counselorId = jdbcTemplate.queryForObject(
                    "SELECT u.id FROM sys_user u JOIN sys_role r ON u.role_id=r.id WHERE r.role_code='counselor' AND u.department_id=? LIMIT 1",
                    Long.class, deptId);
                jdbcTemplate.update("UPDATE sys_class SET counselor_id=? WHERE department_id=? AND (counselor_id IS NULL OR counselor_id=0)", counselorId, deptId);
            }
        } catch (Exception ignored) {}
    }

    private void seedComprehensiveData(String pwd) {
        // 清理旧种子数据（保留 run() 方法创建的初始用户：s001, t001, c001 等）
        jdbcTemplate.update("DELETE FROM edu_grade");
        jdbcTemplate.update("DELETE FROM edu_course_selection");
        jdbcTemplate.update("DELETE FROM edu_course_class");
        jdbcTemplate.update("DELETE FROM edu_course WHERE course_code IN ('CS401','CS402','EC201','EC202','EE301','EE302','FL101','CS101','CS102','CS103','CS104','CS105','CS201','ENG101','MATH201','CS301','EC101','EC102','EC103','EC104','EC105','GED102','GED202','GED302','GED403','GED402','ELEC001','ELEC002','ELEC003')");
        // 清理引用 sys_user 的外键表（按 FK 链顺序，必须在 sys_user DELETE 之前）
        jdbcTemplate.update("DELETE FROM user_todo");
        jdbcTemplate.update("DELETE FROM life_lost_found");
        jdbcTemplate.update("DELETE FROM life_card_recharge");
        jdbcTemplate.update("DELETE FROM admin_leave");
        jdbcTemplate.update("DELETE FROM admin_notification");
        jdbcTemplate.update("DELETE FROM activity_registration");
        jdbcTemplate.update("DELETE FROM activity_center");
        jdbcTemplate.update("DELETE FROM club_activity_enrollment");
        jdbcTemplate.update("DELETE FROM club_activity");
        jdbcTemplate.update("DELETE FROM club_venue_booking");
        jdbcTemplate.update("DELETE FROM club_member");
        jdbcTemplate.update("DELETE FROM club_info");
        jdbcTemplate.update("DELETE FROM ai_message");
        jdbcTemplate.update("DELETE FROM ai_conversation");
        jdbcTemplate.update("DELETE FROM message_detail");
        jdbcTemplate.update("DELETE FROM message_conversation");

        jdbcTemplate.update("DELETE FROM sys_class");
        jdbcTemplate.update("DELETE FROM sys_grade");
        jdbcTemplate.update("DELETE FROM sys_major");
        jdbcTemplate.update("DELETE FROM sys_department");
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE 's%' AND username != 's001'");
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE 'c%' AND username != 'c001'");
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE 't%' AND username != 't001'");
        jdbcTemplate.update("DELETE FROM edu_training_plan_item");
        jdbcTemplate.update("DELETE FROM edu_training_plan");
        jdbcTemplate.update("DELETE FROM edu_semester");

        // === 学期 ===
        jdbcTemplate.update("INSERT IGNORE INTO edu_semester (xn, xqjc, xqqc, ksrq, jsrq, zc, status) VALUES (?,?,?,?,?,?,?)",
            "2025-2026学年", "202502", "2025-2026学年第二学期", java.sql.Date.valueOf("2026-03-02"), java.sql.Date.valueOf("2026-09-04"), 29, 1);
        jdbcTemplate.update("INSERT IGNORE INTO edu_semester (xn, xqjc, xqqc, ksrq, jsrq, zc, status) VALUES (?,?,?,?,?,?,?)",
            "2025-2026学年", "202501", "2025-2026学年第一学期", java.sql.Date.valueOf("2025-09-08"), java.sql.Date.valueOf("2026-02-15"), 23, 0);

        // === 1. 院系（2个） ===
        jdbcTemplate.update("INSERT IGNORE INTO sys_department (name) VALUES ('计算机科学与技术学院')");
        jdbcTemplate.update("INSERT IGNORE INTO sys_department (name) VALUES ('经济管理学院')");
        long d1 = jdbcTemplate.queryForObject("SELECT id FROM sys_department WHERE name='计算机科学与技术学院'", Long.class);
        long d2 = jdbcTemplate.queryForObject("SELECT id FROM sys_department WHERE name='经济管理学院'", Long.class);

        // === 2. 专业（各1个） ===
        jdbcTemplate.update("INSERT IGNORE INTO sys_major (name, department_id) VALUES ('计算机科学与技术',?)", d1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_major (name, department_id) VALUES ('经济学',?)", d2);
        long m1 = jdbcTemplate.queryForObject("SELECT id FROM sys_major WHERE name='计算机科学与技术'", Long.class);
        long m2 = jdbcTemplate.queryForObject("SELECT id FROM sys_major WHERE name='经济学'", Long.class);

        // === 3. 年级 ===
        jdbcTemplate.update("INSERT IGNORE INTO sys_grade (name, year) VALUES ('2025级',2025)");
        long g25 = jdbcTemplate.queryForObject("SELECT id FROM sys_grade WHERE year=2025", Long.class);

        // === 4. 班级（各2个，共4个） ===
        Object[][] classes = {
            {"计算机科学与技术2025级1班", d1, m1, g25},
            {"计算机科学与技术2025级2班", d1, m1, g25},
            {"经济学2025级1班", d2, m2, g25},
            {"经济学2025级2班", d2, m2, g25},
        };
        for (Object[] c : classes) {
            jdbcTemplate.update("INSERT IGNORE INTO sys_class (class_name, department_id, major_id, grade_id) VALUES (?,?,?,?)", c);
        }

        Long teacherRoleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE role_code='teacher'", Long.class);
        Long counselorRoleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE role_code='counselor'", Long.class);
        Long studentRoleId = jdbcTemplate.queryForObject("SELECT id FROM sys_role WHERE role_code='student'", Long.class);

        // 更新 run() 创建的初始用户的院系/班级指向
        jdbcTemplate.update("UPDATE sys_user SET department_id=? WHERE username='t001'", d1);
        jdbcTemplate.update("UPDATE sys_user SET department_id=? WHERE username='c001'", d1);
        jdbcTemplate.update("UPDATE sys_user SET department_id=?, class_id=(SELECT id FROM sys_class WHERE class_name='计算机科学与技术2025级1班'), major_id=(SELECT major_id FROM sys_class WHERE class_name='计算机科学与技术2025级1班') WHERE username='s001'", d1);

        // === 5. 教师（10位，各教一门课） ===
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t001", pwd, "张教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t002", pwd, "李教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t003", pwd, "陈教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t004", pwd, "王教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t005", pwd, "刘教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t006", pwd, "赵教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t007", pwd, "周教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t008", pwd, "吴教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t009", pwd, "郑教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t010", pwd, "冯教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t011", pwd, "孙教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t012", pwd, "马教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t013", pwd, "钱教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t014", pwd, "许教授", teacherRoleId, d2, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t015", pwd, "夏教授", teacherRoleId, d1, 1);
        long t001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t001'", Long.class);
        long t002Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t002'", Long.class);
        long t003Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t003'", Long.class);
        long t004Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t004'", Long.class);
        long t005Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t005'", Long.class);
        long t006Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t006'", Long.class);
        long t007Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t007'", Long.class);
        long t008Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t008'", Long.class);
        long t009Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t009'", Long.class);
        long t010Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t010'", Long.class);
        long t011Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t011'", Long.class);
        long t012Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t012'", Long.class);
        long t013Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t013'", Long.class);
        long t014Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t014'", Long.class);
        long t015Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t015'", Long.class);

        // === 6. 辅导员（2位，各管一个院系） ===
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "c001", pwd, "王辅导员", counselorRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "c002", pwd, "张辅导员", counselorRoleId, d2, 1);
        long c001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='c001'", Long.class);
        long c002Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='c002'", Long.class);

        // === 7. 辅导员分配班级（按院系批量分配） ===
        jdbcTemplate.update("UPDATE sys_class SET counselor_id=? WHERE department_id=?", c001Id, d1);
        jdbcTemplate.update("UPDATE sys_class SET counselor_id=? WHERE department_id=?", c002Id, d2);

        // === 8. 学生（每班70人，共280人） ===
        String[] classNames = {
            "计算机科学与技术2025级1班", "计算机科学与技术2025级2班",
            "经济学2025级1班", "经济学2025级2班"
        };
        String[] studentNames = {
            "陈明","李华","王芳","张伟","刘洋","杨静","赵磊","黄丽","周强","吴敏",
            "徐浩","孙悦","马超","朱婷","胡杰","郭雪","何平","高峰","林娜","罗鑫",
            "梁宇","宋佳","唐飞","韩冰","曹阳","邓琴","许涛","冯蕾","彭波","蒋芸",
            "蔡文","贾亮","余莉","潘伟","戴琳","夏勇","钟婷","汪凯","田雪","范强",
            "石静","姚远","谭敏","汪峰","廖杰","邹丽","熊伟","金娜","陆毅","白杨"
        };

        long studentCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_user WHERE role_id=?", Long.class, studentRoleId);
        if (studentCount < 280) {
            for (int i = 0; i < 280; i++) {
                String sid = String.format("s%03d", i + 1);
                String clazz = classNames[i / 70]; // 每70人一个班
                Long classId = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name=?", Long.class, clazz);
                jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, class_id, major_id, gender, status) VALUES (?,?,?,?,?,?,?,?,?)",
                    sid, pwd, studentNames[i % 50], studentRoleId,
                    jdbcTemplate.queryForObject("SELECT department_id FROM sys_class WHERE id=?", Long.class, classId),
                    classId,
                    jdbcTemplate.queryForObject("SELECT major_id FROM sys_class WHERE id=?", Long.class, classId),
                    (long)(i % 2), 1);

                Long userId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username=?", Long.class, sid);
            }
        }

        // === 9. 课程（每个专业5门必修课，共10门，教师已分配） ===
        long courseCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM edu_course", Long.class);
        if (courseCount < 23) {
            // 计算机科学与技术学院 — 5门专业课（每位老师一门课，每周2时段）
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "CS101", "程序设计基础", t001Id, 4.0, 64, "202502", "教3-201", "[{\"day\":1,\"timeSlot\":1,\"weeks\":\"all\"},{\"day\":3,\"timeSlot\":2,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "CS102", "数据结构与算法", t003Id, 4.0, 64, "202502", "教3-202", "[{\"day\":2,\"timeSlot\":1,\"weeks\":\"all\"},{\"day\":4,\"timeSlot\":2,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "CS103", "计算机网络", t004Id, 3.0, 48, "202502", "教3-301", "[{\"day\":3,\"timeSlot\":1,\"weeks\":\"all\"},{\"day\":5,\"timeSlot\":2,\"weeks\":\"even\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "CS104", "操作系统", t005Id, 4.0, 64, "202502", "教3-302", "[{\"day\":1,\"timeSlot\":3,\"weeks\":\"all\"},{\"day\":4,\"timeSlot\":1,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "CS105", "数据库原理", t006Id, 3.0, 48, "202502", "教3-401", "[{\"day\":2,\"timeSlot\":3,\"weeks\":\"all\"},{\"day\":5,\"timeSlot\":3,\"weeks\":\"odd\"}]", 16);

            // 经济管理学院 — 5门专业课（每位老师一门课，每周2时段）
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "EC101", "微观经济学", t002Id, 3.0, 48, "202502", "教5-101", "[{\"day\":2,\"timeSlot\":1,\"weeks\":\"all\"},{\"day\":4,\"timeSlot\":2,\"weeks\":\"odd\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "EC102", "宏观经济学", t007Id, 3.0, 48, "202502", "教5-102", "[{\"day\":3,\"timeSlot\":1,\"weeks\":\"all\"},{\"day\":5,\"timeSlot\":2,\"weeks\":\"even\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "EC103", "计量经济学", t008Id, 4.0, 64, "202502", "教5-201", "[{\"day\":1,\"timeSlot\":3,\"weeks\":\"all\"},{\"day\":4,\"timeSlot\":1,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "EC104", "国际经济学", t009Id, 3.0, 48, "202502", "教5-202", "[{\"day\":2,\"timeSlot\":3,\"weeks\":\"all\"},{\"day\":5,\"timeSlot\":3,\"weeks\":\"odd\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,140,0,2,'required')",
                "EC105", "财政学", t010Id, 3.0, 48, "202502", "教5-301", "[{\"day\":1,\"timeSlot\":1,\"weeks\":\"all\"},{\"day\":3,\"timeSlot\":2,\"weeks\":\"even\"}]", 16);

            // 公共课（跨专业共享，容量300容纳所有学生）
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,300,0,2,'required')",
                "GED102", "大学英语(二)", t011Id, 4.0, 64, "202502", "教1-101", "[{\"day\":3,\"timeSlot\":3,\"weeks\":\"all\"},{\"day\":5,\"timeSlot\":1,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,300,0,2,'required')",
                "GED202", "线性代数", t012Id, 4.0, 64, "202502", "教1-102", "[{\"day\":1,\"timeSlot\":2,\"weeks\":\"all\"},{\"day\":4,\"timeSlot\":3,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,300,0,2,'required')",
                "GED302", "中国近现代史纲要", t013Id, 3.0, 48, "202502", "教1-201", "[{\"day\":2,\"timeSlot\":4,\"weeks\":\"all\"}]", 20);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,300,0,2,'required')",
                "GED403", "形势与政策", t014Id, 2.0, 16, "202502", "教1-202", "[{\"day\":4,\"timeSlot\":4,\"weeks\":\"odd\"}]", 20);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,300,0,2,'required')",
                "GED402", "体育(二)", t015Id, 1.0, 32, "202502", "体育馆", "[{\"day\":1,\"timeSlot\":4,\"weeks\":\"all\"}]", 16);

            // 9b. 课程-班级关联（CourseClass）
            long csClass1Id = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name='计算机科学与技术2025级1班'", Long.class);
            long csClass2Id = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name='计算机科学与技术2025级2班'", Long.class);
            long ecClass1Id = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name='经济学2025级1班'", Long.class);
            long ecClass2Id = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name='经济学2025级2班'", Long.class);
            for (String code : new String[]{"CS101","CS102","CS103","CS104","CS105"}) {
                long cid = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code=?", Long.class, code);
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_class (course_id, class_id, is_required) VALUES (?,?,1)", cid, csClass1Id);
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_class (course_id, class_id, is_required) VALUES (?,?,1)", cid, csClass2Id);
            }
            for (String code : new String[]{"EC101","EC102","EC103","EC104","EC105"}) {
                long cid = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code=?", Long.class, code);
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_class (course_id, class_id, is_required) VALUES (?,?,1)", cid, ecClass1Id);
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_class (course_id, class_id, is_required) VALUES (?,?,1)", cid, ecClass2Id);
            }
            // 公共课关联到所有班级
            for (String code : new String[]{"GED102","GED202","GED302","GED403","GED402"}) {
                long cid = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code=?", Long.class, code);
                for (long clsId : new long[]{csClass1Id, csClass2Id, ecClass1Id, ecClass2Id}) {
                    jdbcTemplate.update("INSERT IGNORE INTO edu_course_class (course_id, class_id, is_required) VALUES (?,?,1)", cid, clsId);
                }
            }

            // 9c. 选修课（3门，无班级限制，开放选课）
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,50,0,1,'elective')",
                "ELEC001", "Python程序设计", t011Id, 3.0, 48, "202502", "教3-501", "[{\"day\":4,\"timeSlot\":1,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,50,0,1,'elective')",
                "ELEC002", "Web前端开发", t012Id, 3.0, 48, "202502", "教3-502", "[{\"day\":2,\"timeSlot\":2,\"weeks\":\"all\"}]", 16);
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,?,50,0,1,'elective')",
                "ELEC003", "人工智能导论", t015Id, 3.0, 48, "202502", "教3-503", "[{\"day\":5,\"timeSlot\":4,\"weeks\":\"all\"}]", 16);
        }

        // === 10. 选课数据（所有学生自动分配到本专业5门必修课） ===
        long selectionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM edu_course_selection", Long.class);
        if (selectionCount == 0) {
            String[] csCourses = {"CS101", "CS102", "CS103", "CS104", "CS105", "GED102", "GED202", "GED302", "GED403", "GED402"};
            String[] ecCourses = {"EC101", "EC102", "EC103", "EC104", "EC105", "GED102", "GED202", "GED302", "GED403", "GED402"};
            Long[] csIds = new Long[csCourses.length];
            for (int j = 0; j < csCourses.length; j++) {
                csIds[j] = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code=?", Long.class, csCourses[j]);
            }
            Long[] ecIds = new Long[ecCourses.length];
            for (int j = 0; j < ecCourses.length; j++) {
                ecIds[j] = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code=?", Long.class, ecCourses[j]);
            }
            Long csClass1 = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name='计算机科学与技术2025级1班'", Long.class);
            Long csClass2 = jdbcTemplate.queryForObject("SELECT id FROM sys_class WHERE class_name='计算机科学与技术2025级2班'", Long.class);
            for (int i = 1; i <= 280; i++) {
                Long sid = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username=?", Long.class, String.format("s%03d", i));
                Long classId = jdbcTemplate.queryForObject("SELECT class_id FROM sys_user WHERE id=?", Long.class, sid);
                Long[] targetIds = (classId.equals(csClass1) || classId.equals(csClass2)) ? csIds : ecIds;
                for (Long cid : targetIds) {
                    jdbcTemplate.update("INSERT IGNORE INTO edu_course_selection (student_id, course_id, semester, select_type) VALUES (?,?,'202502','auto')", sid, cid);
                }
            }
        }

        // 必修课状态修正：已自动分配学生的课程应显示为"已确认"而非"选课中"
        try {
            jdbcTemplate.update("UPDATE edu_course SET status=2 WHERE course_type='required' AND status=1 AND enrolled>0");
        } catch (Exception ignored) {}

        // === 11. 培养方案示例数据（两个专业各一套） ===
        long planCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM edu_training_plan", Long.class);
        if (planCount == 0) {
            Long csMajorId = jdbcTemplate.queryForObject("SELECT id FROM sys_major WHERE name='计算机科学与技术'", Long.class);
            Long ecMajorId = jdbcTemplate.queryForObject("SELECT id FROM sys_major WHERE name='经济学'", Long.class);
            Long grade2025 = jdbcTemplate.queryForObject("SELECT id FROM sys_grade WHERE year=2025", Long.class);

            // 计算机科学与技术培养方案
            jdbcTemplate.update("INSERT INTO edu_training_plan (name, major_id, grade_id, total_semesters, status) VALUES (?,?,?,8,1)",
                "2025级计算机科学与技术培养方案", csMajorId, grade2025);
            Long csPlanId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            String[][] csSem1 = {
                {"大学英语(一)", "GED101", "4.0", "64", "1"},
                {"高等数学(一)", "GED201", "5.0", "80", "1"},
                {"程序设计基础", "CS101", "4.0", "64", "1"},
                {"思想道德修养与法律基础", "GED301", "3.0", "48", "1"},
                {"体育(一)", "GED401", "1.0", "32", "1"}
            };
            for (int i = 0; i < csSem1.length; i++) {
                jdbcTemplate.update("INSERT INTO edu_training_plan_item (plan_id, semester_number, course_name, course_code, credit, hours, is_required, sort_order) VALUES (?,1,?,?,?,?,1,?)",
                    csPlanId, csSem1[i][0], csSem1[i][1], new java.math.BigDecimal(csSem1[i][2]), Integer.parseInt(csSem1[i][3]), i + 1);
            }
            String[][] csSem2 = {
                {"数据结构与算法", "CS102", "4.0", "64", "1"},
                {"计算机网络", "CS103", "3.0", "48", "1"},
                {"操作系统", "CS104", "4.0", "64", "1"},
                {"数据库原理", "CS105", "3.0", "48", "1"},
                {"大学英语(二)", "GED102", "4.0", "64", "1"},
                {"线性代数", "GED202", "4.0", "64", "1"},
                {"中国近现代史纲要", "GED302", "3.0", "48", "1"},
                {"形势与政策", "GED403", "2.0", "16", "1"},
                {"体育(二)", "GED402", "1.0", "32", "1"}
            };
            for (int i = 0; i < csSem2.length; i++) {
                jdbcTemplate.update("INSERT INTO edu_training_plan_item (plan_id, semester_number, course_name, course_code, credit, hours, is_required, sort_order) VALUES (?,2,?,?,?,?,1,?)",
                    csPlanId, csSem2[i][0], csSem2[i][1], new java.math.BigDecimal(csSem2[i][2]), Integer.parseInt(csSem2[i][3]), i + 1);
            }

            // 经济学培养方案
            jdbcTemplate.update("INSERT INTO edu_training_plan (name, major_id, grade_id, total_semesters, status) VALUES (?,?,?,8,1)",
                "2025级经济学培养方案", ecMajorId, grade2025);
            Long ecPlanId = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);

            String[][] ecSem1 = {
                {"大学英语(一)", "GED101", "4.0", "64", "1"},
                {"高等数学(一)", "GED201", "5.0", "80", "1"},
                {"微观经济学", "EC101", "3.0", "48", "1"},
                {"思想道德修养与法律基础", "GED301", "3.0", "48", "1"},
                {"体育(一)", "GED401", "1.0", "32", "1"}
            };
            for (int i = 0; i < ecSem1.length; i++) {
                jdbcTemplate.update("INSERT INTO edu_training_plan_item (plan_id, semester_number, course_name, course_code, credit, hours, is_required, sort_order) VALUES (?,1,?,?,?,?,1,?)",
                    ecPlanId, ecSem1[i][0], ecSem1[i][1], new java.math.BigDecimal(ecSem1[i][2]), Integer.parseInt(ecSem1[i][3]), i + 1);
            }
            String[][] ecSem2 = {
                {"宏观经济学", "EC102", "3.0", "48", "1"},
                {"计量经济学", "EC103", "4.0", "64", "1"},
                {"国际经济学", "EC104", "3.0", "48", "1"},
                {"财政学", "EC105", "3.0", "48", "1"},
                {"大学英语(二)", "GED102", "4.0", "64", "1"},
                {"线性代数", "GED202", "4.0", "64", "1"},
                {"中国近现代史纲要", "GED302", "3.0", "48", "1"},
                {"形势与政策", "GED403", "2.0", "16", "1"},
                {"体育(二)", "GED402", "1.0", "32", "1"}
            };
            for (int i = 0; i < ecSem2.length; i++) {
                jdbcTemplate.update("INSERT INTO edu_training_plan_item (plan_id, semester_number, course_name, course_code, credit, hours, is_required, sort_order) VALUES (?,2,?,?,?,?,1,?)",
                    ecPlanId, ecSem2[i][0], ecSem2[i][1], new java.math.BigDecimal(ecSem2[i][2]), Integer.parseInt(ecSem2[i][3]), i + 1);
            }

            // 回填培养方案项的 generatedCourseId，关联到已存在的课程记录
            try {
                for (String code : new String[]{"CS101","CS102","CS103","CS104","CS105","EC101","EC102","EC103","EC104","EC105","GED102","GED202","GED302","GED403","GED402"}) {
                    Long existingId = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code=? AND semester='202502'", Long.class, code);
                    if (existingId != null) {
                        jdbcTemplate.update("UPDATE edu_training_plan_item SET generated_course_id=?, status=1 WHERE course_code=? AND generated_course_id IS NULL", existingId, code);
                    }
                }
            } catch (Exception ignored) {}
        }

        // 回填课程实际选课人数（全表自动覆盖所有课程）
        try {
            jdbcTemplate.update("UPDATE edu_course e SET e.enrolled = (SELECT COUNT(*) FROM edu_course_selection s WHERE s.course_id = e.id AND s.status = 1)");
        } catch (Exception ignored) {}

        // === 社团种子数据 ===
        try {
            Long s001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 's001'", Long.class);
            if (s001Id != null) {
                jdbcTemplate.update("INSERT IGNORE INTO club_info (name, description, president_id, member_count, status) VALUES (?,?,?,?,?)",
                    "计算机协会", "编程、算法、人工智能兴趣社团", s001Id, 1, 1);
                jdbcTemplate.update("INSERT IGNORE INTO club_info (name, description, president_id, member_count, status) VALUES (?,?,?,?,?)",
                    "羽毛球社", "羽毛球运动交流与训练", s001Id, 1, 1);
                jdbcTemplate.update("INSERT IGNORE INTO club_info (name, description, president_id, member_count, status) VALUES (?,?,?,?,?)",
                    "摄影社", "摄影技巧分享与外拍活动", s001Id, 1, 1);

                Long club1Id = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '计算机协会'", Long.class);
                Long club2Id = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '羽毛球社'", Long.class);
                Long club3Id = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '摄影社'", Long.class);

                if (club1Id != null)
                    jdbcTemplate.update("INSERT IGNORE INTO club_member (club_id, user_id, role, status) VALUES (?,?,?,?)", club1Id, s001Id, "president", 1);
                if (club2Id != null)
                    jdbcTemplate.update("INSERT IGNORE INTO club_member (club_id, user_id, role, status) VALUES (?,?,?,?)", club2Id, s001Id, "president", 1);
                if (club3Id != null)
                    jdbcTemplate.update("INSERT IGNORE INTO club_member (club_id, user_id, role, status) VALUES (?,?,?,?)", club3Id, s001Id, "president", 1);
            }
        } catch (Exception ignored) {}

        // === 场地种子数据 ===
        try {
            jdbcTemplate.update("INSERT IGNORE INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "301 报告厅", "教学楼A座3楼", 200, "大型报告厅，配备投影音响");
            jdbcTemplate.update("INSERT IGNORE INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "篮球场", "东区体育场", 50, "标准室外篮球场");
            jdbcTemplate.update("INSERT IGNORE INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "B101 活动室", "教学楼B座1楼", 30, "小型多功能活动室");
            jdbcTemplate.update("INSERT IGNORE INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "阶梯教室", "教学楼C座2楼", 150, "可容纳150人的阶梯教室");
        } catch (Exception ignored) {}

        // === 活动中心种子数据 ===
        try {
            Long adminId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 'admin'", Long.class);
            Long teacherId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 't001'", Long.class);
            Long s001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 's001'", Long.class);
            if (adminId != null && teacherId != null && s001Id != null) {
                jdbcTemplate.update(
                    "INSERT IGNORE INTO activity_center (title, description, location, start_time, end_time, max_participants, category, creator_id, creator_role, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    "校园编程马拉松", "24小时编程挑战赛，欢迎各路高手参加！组队或个人参赛均可。", "体育馆",
                    java.sql.Timestamp.valueOf("2026-06-15 09:00:00"), java.sql.Timestamp.valueOf("2026-06-16 09:00:00"),
                    50, "academic", s001Id, "club_president", 0);
                jdbcTemplate.update(
                    "INSERT IGNORE INTO activity_center (title, description, location, start_time, end_time, max_participants, current_participants, category, creator_id, creator_role, status, approver_id, approve_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "摄影作品展", "全校摄影爱好者作品展，欢迎参观投稿。", "图书馆一楼展厅",
                    java.sql.Timestamp.valueOf("2026-06-20 10:00:00"), java.sql.Timestamp.valueOf("2026-06-22 17:00:00"),
                    200, 0, "cultural", teacherId, "teacher", 1, adminId,
                    java.sql.Timestamp.valueOf("2026-05-30 10:00:00"));
                jdbcTemplate.update(
                    "INSERT IGNORE INTO activity_center (title, description, location, start_time, end_time, category, creator_id, creator_role, status, approver_id, reject_reason, approve_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    "校外聚餐活动", "组织同学到校外餐厅聚餐联谊。", "校外美食街",
                    java.sql.Timestamp.valueOf("2026-07-01 18:00:00"), java.sql.Timestamp.valueOf("2026-07-01 21:00:00"),
                    "other", s001Id, "club_president", 2, adminId, "活动内容不符合校园安全管理规定",
                    java.sql.Timestamp.valueOf("2026-05-29 14:00:00"));
                jdbcTemplate.update(
                    "INSERT IGNORE INTO activity_center (title, description, location, start_time, end_time, max_participants, current_participants, category, creator_id, creator_role, status, approver_id, approve_time, confirm_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "羽毛球友谊赛", "社团羽毛球友谊赛，欢迎同学们报名参加。", "东区体育馆",
                    java.sql.Timestamp.valueOf("2026-05-10 14:00:00"), java.sql.Timestamp.valueOf("2026-05-10 18:00:00"),
                    30, 28, "sports", adminId, "admin", 3, adminId,
                    java.sql.Timestamp.valueOf("2026-05-05 10:00:00"),
                    java.sql.Timestamp.valueOf("2026-05-10 19:00:00"));

                Long confirmedActId = jdbcTemplate.queryForObject(
                    "SELECT id FROM activity_center WHERE title = '羽毛球友谊赛'", Long.class);
                if (confirmedActId != null) {
                    jdbcTemplate.update("INSERT IGNORE INTO activity_registration (activity_id, user_id) VALUES (?,?)",
                        confirmedActId, s001Id);
                }

                Long clubId = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '计算机协会'", Long.class);
                if (clubId != null) {
                    jdbcTemplate.update(
                        "INSERT IGNORE INTO activity_center (title, description, location, start_time, end_time, max_participants, category, club_id, creator_id, creator_role, status) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        "编程技术分享会", "计算机协会内部技术分享，主题：Spring Boot 实战经验", "理学院303教室",
                        java.sql.Timestamp.valueOf("2026-06-25 14:00:00"), java.sql.Timestamp.valueOf("2026-06-25 17:00:00"),
                        30, "academic", clubId, s001Id, "club_president", 0);
                }
            }
        } catch (Exception ignored) {}
    }
}

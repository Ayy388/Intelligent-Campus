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
                start_week INT DEFAULT 1,
                end_week INT DEFAULT 20,
                capacity INT DEFAULT 0,
                enrolled INT DEFAULT 0,
                description TEXT,
                status TINYINT DEFAULT 0,
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
            CREATE TABLE IF NOT EXISTS growth_profile (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                student_id BIGINT NOT NULL UNIQUE,
                awards TEXT,
                experiences TEXT,
                evaluation TEXT,
                gpa DECIMAL(4,2),
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (student_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS growth_checkin (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                teacher_id BIGINT NOT NULL,
                title VARCHAR(200) NOT NULL,
                checkin_type VARCHAR(50) DEFAULT 'course',
                start_time TIMESTAMP NOT NULL,
                end_time TIMESTAMP NOT NULL,
                total_count INT DEFAULT 0,
                checked_count INT DEFAULT 0,
                status TINYINT DEFAULT 1,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (teacher_id) REFERENCES sys_user(id)
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS growth_checkin_record (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                checkin_id BIGINT NOT NULL,
                student_id BIGINT NOT NULL,
                checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (checkin_id) REFERENCES growth_checkin(id),
                FOREIGN KEY (student_id) REFERENCES sys_user(id)
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

        // 数据库迁移：为已有表添加新列（如果不存在）
        try {
            jdbcTemplate.execute("ALTER TABLE edu_course ADD COLUMN start_week INT DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE edu_course ADD COLUMN end_week INT DEFAULT 20");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE growth_checkin ADD COLUMN course_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE growth_checkin ADD COLUMN class_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE edu_course ADD COLUMN course_type VARCHAR(20) DEFAULT 'required'");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE edu_course ADD COLUMN min_students INT DEFAULT 1");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE edu_course ADD COLUMN enroll_end DATETIME");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE edu_course_selection ADD COLUMN select_type VARCHAR(20) DEFAULT 'manual'");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN class_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN counselor_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN major_id BIGINT");
        } catch (Exception ignored) {}

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_class (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                class_name VARCHAR(100) NOT NULL,
                department VARCHAR(100),
                major VARCHAR(100),
                grade VARCHAR(20),
                advisor VARCHAR(50),
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

        // 添加外键字段
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN department_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN department_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN major_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN grade_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN counselor_id BIGINT");
        } catch (Exception ignored) {}

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

        Integer clubCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM club_info", Integer.class);
        if (clubCount != null && clubCount == 0) {
            // 获取s001用户的ID
            Long s001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 's001'", Long.class);
            if (s001Id != null) {
                jdbcTemplate.update("INSERT INTO club_info (name, description, president_id, member_count, status) VALUES (?,?,?,?,?)",
                    "计算机协会", "编程、算法、人工智能兴趣社团", s001Id, 1, 1);
                jdbcTemplate.update("INSERT INTO club_info (name, description, president_id, member_count, status) VALUES (?,?,?,?,?)",
                    "羽毛球社", "羽毛球运动交流与训练", s001Id, 1, 1);
                jdbcTemplate.update("INSERT INTO club_info (name, description, president_id, member_count, status) VALUES (?,?,?,?,?)",
                    "摄影社", "摄影技巧分享与外拍活动", s001Id, 1, 1);
                
                // 获取刚插入的社团ID并添加社长成员
                Long club1Id = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '计算机协会'", Long.class);
                Long club2Id = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '羽毛球社'", Long.class);
                Long club3Id = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '摄影社'", Long.class);
                
                if (club1Id != null) {
                    jdbcTemplate.update("INSERT INTO club_member (club_id, user_id, role, status) VALUES (?,?,?,?)",
                        club1Id, s001Id, "president", 1);
                }
                if (club2Id != null) {
                    jdbcTemplate.update("INSERT INTO club_member (club_id, user_id, role, status) VALUES (?,?,?,?)",
                        club2Id, s001Id, "president", 1);
                }
                if (club3Id != null) {
                    jdbcTemplate.update("INSERT INTO club_member (club_id, user_id, role, status) VALUES (?,?,?,?)",
                        club3Id, s001Id, "president", 1);
                }
            } else {
                // 如果找不到s001用户，使用原来的逻辑
                jdbcTemplate.update("INSERT INTO club_info (name, description, member_count, status) VALUES (?,?,?,?)",
                    "计算机协会", "编程、算法、人工智能兴趣社团", 0, 1);
                jdbcTemplate.update("INSERT INTO club_info (name, description, member_count, status) VALUES (?,?,?,?)",
                    "羽毛球社", "羽毛球运动交流与训练", 0, 1);
                jdbcTemplate.update("INSERT INTO club_info (name, description, member_count, status) VALUES (?,?,?,?)",
                    "摄影社", "摄影技巧分享与外拍活动", 0, 1);
            }
        }

        Integer venueCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM club_venue", Integer.class);
        if (venueCount != null && venueCount == 0) {
            jdbcTemplate.update("INSERT INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "301 报告厅", "教学楼A座3楼", 200, "大型报告厅，配备投影音响");
            jdbcTemplate.update("INSERT INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "篮球场", "东区体育场", 50, "标准室外篮球场");
            jdbcTemplate.update("INSERT INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "B101 活动室", "教学楼B座1楼", 30, "小型多功能活动室");
            jdbcTemplate.update("INSERT INTO club_venue (name, location, capacity, description) VALUES (?,?,?,?)",
                "阶梯教室", "教学楼C座2楼", 150, "可容纳150人的阶梯教室");
        }

        Integer courseCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM edu_course", Integer.class);
        if (courseCount != null && courseCount == 0) {
            // 获取教师ID
            Long teacherId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 't001'", Long.class);
            if (teacherId != null) {
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS101", "Java程序设计", teacherId, 4.0, 64, "2026-春", "教3-201", 
                    "{\"day\":1,\"timeSlot\":1}", 1, 20, 50, 0, "Java基础编程入门课程", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS102", "数据结构与算法", teacherId, 3.5, 56, "2026-春", "教3-302", 
                    "{\"day\":2,\"timeSlot\":2}", 1, 20, 45, 0, "算法与数据结构课程", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS201", "操作系统", teacherId, 3.0, 48, "2026-春", "教4-105", 
                    "{\"day\":3,\"timeSlot\":3}", 1, 20, 40, 0, "操作系统原理课程", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "ENG101", "大学英语", teacherId, 3.0, 48, "2026-春", "外语楼-202", 
                    "{\"day\":4,\"timeSlot\":1}", 1, 20, 50, 0, "大学英语读写译", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "MATH201", "高等数学", teacherId, 5.0, 80, "2026-春", "教1-101", 
                    "{\"day\":5,\"timeSlot\":2}", 1, 20, 100, 0, "高等数学微积分", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS301", "计算机网络", teacherId, 3.0, 48, "2026-春", "教3-401", 
                    "{\"day\":2,\"timeSlot\":4}", 1, 20, 35, 0, "计算机网络基础知识", 1, "required");
            }
        }

        Integer semesterCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM edu_semester", Integer.class);
        if (semesterCount != null && semesterCount == 0) {
            jdbcTemplate.update("INSERT INTO edu_semester (xn, xqjc, xqqc, ksrq, jsrq, zc, status) VALUES (?,?,?,?,?,?,?)",
                "2025-2026学年", "202502", "2025-2026学年第二学期", java.sql.Date.valueOf("2026-03-02"), java.sql.Date.valueOf("2026-09-04"), 29, 1);
            jdbcTemplate.update("INSERT INTO edu_semester (xn, xqjc, xqqc, ksrq, jsrq, zc, status) VALUES (?,?,?,?,?,?,?)",
                "2025-2026学年", "202501", "2025-2026学年第一学期", java.sql.Date.valueOf("2025-09-08"), java.sql.Date.valueOf("2026-02-15"), 23, 0);
            jdbcTemplate.update("INSERT INTO edu_semester (xn, xqjc, xqqc, ksrq, jsrq, zc, status) VALUES (?,?,?,?,?,?,?)",
                "2024-2025学年", "202402", "2024-2025学年第二学期", java.sql.Date.valueOf("2025-02-17"), java.sql.Date.valueOf("2025-09-03"), 29, 0);
            jdbcTemplate.update("INSERT INTO edu_semester (xn, xqjc, xqqc, ksrq, jsrq, zc, status) VALUES (?,?,?,?,?,?,?)",
                "2024-2025学年", "202401", "2024-2025学年第一学期", java.sql.Date.valueOf("2024-09-02"), java.sql.Date.valueOf("2025-01-17"), 20, 0);
        }

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

        // 兼容旧表：补充可能缺失的字段（旧版表缺少部分列）
        String[][] missingColumns = {
            {"club_id", "BIGINT", "category"},
            {"summary", "TEXT", "club_id"},
            {"images", "VARCHAR(500)", "summary"},
            {"activity_type", "VARCHAR(50)", "images"},
            {"confirm_time", "TIMESTAMP", "approve_time"},
        };
        for (String[] col : missingColumns) {
            try {
                jdbcTemplate.update("ALTER TABLE activity_center ADD COLUMN " + col[0] + " " + col[1] + " AFTER " + col[2]);
            } catch (Exception ignored) {
                // 列已存在则忽略
            }
        }

        Integer activityCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM activity_center", Integer.class);
        if (activityCount != null && activityCount == 0) {
            Long adminId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 'admin'", Long.class);
            Long teacherId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 't001'", Long.class);
            Long s001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username = 's001'", Long.class);

            if (adminId != null && teacherId != null && s001Id != null) {
                jdbcTemplate.update(
                    "INSERT INTO activity_center (title, description, location, start_time, end_time, max_participants, category, creator_id, creator_role, status) VALUES (?,?,?,?,?,?,?,?,?,?)",
                    "校园编程马拉松", "24小时编程挑战赛，欢迎各路高手参加！组队或个人参赛均可。", "体育馆",
                    java.sql.Timestamp.valueOf("2026-06-15 09:00:00"), java.sql.Timestamp.valueOf("2026-06-16 09:00:00"),
                    50, "academic", s001Id, "club_president", 0);

                jdbcTemplate.update(
                    "INSERT INTO activity_center (title, description, location, start_time, end_time, max_participants, current_participants, category, creator_id, creator_role, status, approver_id, approve_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "摄影作品展", "全校摄影爱好者作品展，欢迎参观投稿。", "图书馆一楼展厅",
                    java.sql.Timestamp.valueOf("2026-06-20 10:00:00"), java.sql.Timestamp.valueOf("2026-06-22 17:00:00"),
                    200, 0, "cultural", teacherId, "teacher", 1, adminId,
                    java.sql.Timestamp.valueOf("2026-05-30 10:00:00"));

                jdbcTemplate.update(
                    "INSERT INTO activity_center (title, description, location, start_time, end_time, category, creator_id, creator_role, status, approver_id, reject_reason, approve_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)",
                    "校外聚餐活动", "组织同学到校外餐厅聚餐联谊。", "校外美食街",
                    java.sql.Timestamp.valueOf("2026-07-01 18:00:00"), java.sql.Timestamp.valueOf("2026-07-01 21:00:00"),
                    "other", s001Id, "club_president", 2, adminId, "活动内容不符合校园安全管理规定",
                    java.sql.Timestamp.valueOf("2026-05-29 14:00:00"));

                jdbcTemplate.update(
                    "INSERT INTO activity_center (title, description, location, start_time, end_time, max_participants, current_participants, category, creator_id, creator_role, status, approver_id, approve_time, confirm_time) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
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

                // 添加一个社团关联的活动
                Long clubId = jdbcTemplate.queryForObject("SELECT id FROM club_info WHERE name = '计算机协会'", Long.class);
                if (clubId != null) {
                    jdbcTemplate.update(
                        "INSERT INTO activity_center (title, description, location, start_time, end_time, max_participants, category, club_id, creator_id, creator_role, status) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                        "编程技术分享会", "计算机协会内部技术分享，主题：Spring Boot 实战经验", "理学院303教室",
                        java.sql.Timestamp.valueOf("2026-06-25 14:00:00"), java.sql.Timestamp.valueOf("2026-06-25 17:00:00"),
                        30, "academic", clubId, s001Id, "club_president", 0);
            }
        }
    }

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
        jdbcTemplate.update("DELETE FROM growth_profile");
        jdbcTemplate.update("DELETE FROM edu_grade");
        jdbcTemplate.update("DELETE FROM edu_course_selection");
        jdbcTemplate.update("DELETE FROM edu_course WHERE course_code IN ('CS401','CS402','EC201','EC202','EE301','EE302','FL101')");
        jdbcTemplate.update("DELETE FROM sys_class");
        jdbcTemplate.update("DELETE FROM sys_grade");
        jdbcTemplate.update("DELETE FROM sys_major");
        jdbcTemplate.update("DELETE FROM sys_department");
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE 's%' AND username != 's001'");
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE 'c%' AND username != 'c001'");
        jdbcTemplate.update("DELETE FROM sys_user WHERE username LIKE 't%' AND username != 't001'");

        try { jdbcTemplate.execute("ALTER TABLE sys_department ADD CONSTRAINT uk_department_name UNIQUE (name)"); } catch (Exception ignored) {}
        try { jdbcTemplate.execute("ALTER TABLE sys_major ADD CONSTRAINT uk_major_name UNIQUE (name, department_id)"); } catch (Exception ignored) {}

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
        jdbcTemplate.update("INSERT IGNORE INTO sys_grade (name, year) VALUES ('2024级',2024)");
        long g24 = jdbcTemplate.queryForObject("SELECT id FROM sys_grade WHERE year=2024", Long.class);

        // === 4. 班级（各2个，共4个） ===
        Object[][] classes = {
            {"计算机科学与技术2024级1班", d1, m1, g24},
            {"计算机科学与技术2024级2班", d1, m1, g24},
            {"经济学2024级1班", d2, m2, g24},
            {"经济学2024级2班", d2, m2, g24},
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
        jdbcTemplate.update("UPDATE sys_user SET department_id=?, class_id=(SELECT id FROM sys_class WHERE class_name='计算机科学与技术2024级1班') WHERE username='s001'", d1);

        // === 5. 教师（2位） ===
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t001", pwd, "张教授", teacherRoleId, d1, 1);
        jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, status) VALUES (?,?,?,?,?,?)",
            "t002", pwd, "李教授", teacherRoleId, d2, 1);
        long t001Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t001'", Long.class);
        long t002Id = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username='t002'", Long.class);

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
            "计算机科学与技术2024级1班", "计算机科学与技术2024级2班",
            "经济学2024级1班", "经济学2024级2班"
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
                jdbcTemplate.update("INSERT IGNORE INTO sys_user (username, password, real_name, role_id, department_id, class_id, gender, status) VALUES (?,?,?,?,?,?,?,?)",
                    sid, pwd, studentNames[i % 50], studentRoleId,
                    jdbcTemplate.queryForObject("SELECT department_id FROM sys_class WHERE id=?", Long.class, classId),
                    classId, (long)(i % 2), 1);

                Long userId = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username=?", Long.class, sid);
                jdbcTemplate.update("INSERT IGNORE INTO growth_profile (student_id, gpa) VALUES (?,?)",
                    userId, 2.0 + (Math.random() * 2.5));
            }
        }

        // === 9. 课程（每个老师2门，共4门） ===
        long courseCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM edu_course", Long.class);
        if (courseCount < 10) {
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,20,70,0,1,'required')",
                "CS401", "数据库原理", t001Id, 3.5, 56, "2026-春", "教2-201", "{\"day\":1,\"timeSlot\":3}");
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,20,70,0,1,'required')",
                "CS402", "数据结构与算法", t001Id, 4.0, 64, "2026-春", "教2-202", "{\"day\":3,\"timeSlot\":2}");
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,20,70,0,1,'required')",
                "EC201", "微观经济学", t002Id, 3.0, 48, "2026-春", "教5-101", "{\"day\":1,\"timeSlot\":4}");
            jdbcTemplate.update("INSERT IGNORE INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, status, course_type) VALUES (?,?,?,?,?,?,?,?,1,20,70,0,1,'required')",
                "EC202", "宏观经济学", t002Id, 3.0, 48, "2026-春", "教5-102", "{\"day\":3,\"timeSlot\":4}");
        }

        // === 10. 选课数据（前20名学生选课） ===
        long selectionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM edu_course_selection", Long.class);
        if (selectionCount == 0) {
            Long cs401Id = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code='CS401'", Long.class);
            Long cs402Id = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code='CS402'", Long.class);
            Long ec201Id = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code='EC201'", Long.class);
            Long ec202Id = jdbcTemplate.queryForObject("SELECT id FROM edu_course WHERE course_code='EC202'", Long.class);
            for (int i = 1; i <= 20; i++) {
                Long sid = jdbcTemplate.queryForObject("SELECT id FROM sys_user WHERE username=?", Long.class, String.format("s%03d", i));
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_selection (student_id, course_id, semester, select_type) VALUES (?,?,'2026-春','auto')", sid, cs401Id);
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_selection (student_id, course_id, semester, select_type) VALUES (?,?,'2026-春','auto')", sid, cs402Id);
                jdbcTemplate.update("INSERT IGNORE INTO edu_course_selection (student_id, course_id, semester, select_type) VALUES (?,?,'2026-春','auto')", sid, ec201Id);
                if (i <= 10) {
                    jdbcTemplate.update("INSERT IGNORE INTO edu_course_selection (student_id, course_id, semester, select_type) VALUES (?,?,'2026-春','auto')", sid, ec202Id);
                }
                if (i >= 11 && i <= 15) {
                    jdbcTemplate.update("INSERT IGNORE INTO edu_grade (student_id, course_id, teacher_id, score, semester) VALUES (?,?,?,?,'2026-春')",
                        sid, cs401Id, t001Id, 60 + (int)(Math.random() * 40));
                }
            }
        }
    }
}

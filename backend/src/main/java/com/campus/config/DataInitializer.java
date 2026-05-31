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

        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sys_role", Integer.class);
        if (count != null && count == 0) {
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('student','学生')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('teacher','教师')");
            jdbcTemplate.update("INSERT INTO sys_role (role_code, role_name) VALUES ('admin','管理员')");
        }

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
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS101", "Java程序设计", teacherId, 4.0, 64, "2026-春", "教3-201", 
                    "{\"day\":1,\"timeSlot\":1}", 1, 20, 50, 0, "Java基础编程入门课程", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS102", "数据结构与算法", teacherId, 3.5, 56, "2026-春", "教3-302", 
                    "{\"day\":2,\"timeSlot\":2}", 1, 20, 45, 0, "算法与数据结构课程", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "CS201", "操作系统", teacherId, 3.0, 48, "2026-春", "教4-105", 
                    "{\"day\":3,\"timeSlot\":3}", 1, 20, 40, 0, "操作系统原理课程", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "ENG101", "大学英语", teacherId, 3.0, 48, "2026-春", "外语楼-202", 
                    "{\"day\":4,\"timeSlot\":1}", 1, 20, 50, 0, "大学英语读写译", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                    "MATH201", "高等数学", teacherId, 5.0, 80, "2026-春", "教1-101", 
                    "{\"day\":5,\"timeSlot\":2}", 1, 20, 100, 0, "高等数学微积分", 1, "required");
                
                jdbcTemplate.update(
                    "INSERT INTO edu_course (course_code, course_name, teacher_id, credit, hours, semester, classroom, schedule, start_week, end_week, capacity, enrolled, description, status, course_type) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
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
    }
}

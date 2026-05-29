# 智能校园服务系统 — 第三期实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** 实现社团活动模块（社团管理、活动发布报名、场地预约）和学生成长档案模块（个人档案、签到打卡），共 8 张新表 + 17 个后端文件 + 7 个前端文件。

**Architecture:** 延续一期模块化单体架构，新增 `club/` 和 `growth/` 两个后端模块目录，Entity → Mapper → Service → Controller 标准分层；前端新增对应 views 和 API 文件。

**Tech Stack:** Java 17, Spring Boot 3.2, MyBatis-Plus, H2, Tailwind CSS, Vue 3, Element Plus

**Spec Reference:** `docs/superpowers/specs/2026-05-29-intelligent-campus-design.md` §4.4, §4.6

---

## 改动总览

```
backend/src/main/java/com/campus/
├── config/DataInitializer.java          # 修改 — 8 张新表
└── module/
    ├── club/                            # 新建目录
    │   ├── entity/
    │   │   ├── Club.java
    │   │   ├── ClubMember.java
    │   │   ├── Activity.java
    │   │   ├── ActivityEnrollment.java
    │   │   ├── Venue.java
    │   │   └── VenueBooking.java
    │   ├── mapper/ (6 files)
    │   ├── service/ClubService.java + impl
    │   └── controller/ClubController.java
    └── growth/                          # 新建目录
        ├── entity/
        │   ├── StudentProfile.java
        │   └── CheckIn.java
        ├── mapper/ (2 files)
        ├── service/GrowthService.java + impl
        └── controller/GrowthController.java

frontend/src/
├── api/club.ts                          # 新建
├── api/growth.ts                        # 新建
├── router/index.ts                      # 修改 — 新增 7 条路由
├── components/AppSidebar.vue            # 修改 — 新增 2 组菜单
└── views/
    ├── club/                            # 新建目录
    │   ├── ClubList.vue
    │   ├── ActivityList.vue
    │   └── VenueBooking.vue
    └── growth/                          # 新建目录
        ├── StudentProfile.vue
        └── CheckIn.vue
```

---

## 模块概览

| 子模块 | 功能 | 新表 | 后端文件 | 前端页面 |
|--------|------|:---:|:---:|:---:|
| 🎭 社团管理 | 社团CRUD、成员申请/审批 | 2 | - | ClubList |
| 📅 活动管理 | 活动发布、报名、统计 | 2 | - | ActivityList |
| 🏟️ 场地预约 | 场地管理、预约申请/审批 | 2 | - | VenueBooking |
| 📊 成长档案 | 学生档案、教师评语 | 1 | - | StudentProfile |
| ✅ 签到打卡 | 教师发起签到、学生打卡 | 1 | - | CheckIn |

**后端文件统计**: 实体×8, Mapper×8, Service接口×2, ServiceImpl×2, Controller×2 = 22 个
**前端文件统计**: API文件×2, 页面×5

---

## Task 1: Database Tables (DataInitializer 扩展)

**Files:**
- Modify: `backend/src/main/java/com/campus/config/DataInitializer.java`

在 `life_canteen` 种子数据块之后、`run()` 方法闭合 `}` 之前，插入以下 8 张表和 `club` 种子数据：

```java
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

        Integer clubCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM club_info", Integer.class);
        if (clubCount != null && clubCount == 0) {
            jdbcTemplate.update("INSERT INTO club_info (name, description, member_count, status) VALUES (?,?,?,?)",
                "计算机协会", "编程、算法、人工智能兴趣社团", 0, 1);
            jdbcTemplate.update("INSERT INTO club_info (name, description, member_count, status) VALUES (?,?,?,?)",
                "羽毛球社", "羽毛球运动交流与训练", 0, 1);
            jdbcTemplate.update("INSERT INTO club_info (name, description, member_count, status) VALUES (?,?,?,?)",
                "摄影社", "摄影技巧分享与外拍活动", 0, 1);
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
```

验证:
```bash
cd backend && mvn compile
```

---

## Task 2: Club Module — Entities + Mappers

**Files (12):**

6 个实体:
- `backend/src/main/java/com/campus/module/club/entity/Club.java`
- `backend/src/main/java/com/campus/module/club/entity/ClubMember.java`
- `backend/src/main/java/com/campus/module/club/entity/Activity.java`
- `backend/src/main/java/com/campus/module/club/entity/ActivityEnrollment.java`
- `backend/src/main/java/com/campus/module/club/entity/Venue.java`
- `backend/src/main/java/com/campus/module/club/entity/VenueBooking.java`

6 个 Mapper:
- `backend/src/main/java/com/campus/module/club/mapper/ClubMapper.java`
- `backend/src/main/java/com/campus/module/club/mapper/ClubMemberMapper.java`
- `backend/src/main/java/com/campus/module/club/mapper/ActivityMapper.java`
- `backend/src/main/java/com/campus/module/club/mapper/ActivityEnrollmentMapper.java`
- `backend/src/main/java/com/campus/module/club/mapper/VenueMapper.java`
- `backend/src/main/java/com/campus/module/club/mapper/VenueBookingMapper.java`

实体代码与环境已有文件风格一致，均使用 `@Data @TableName @TableId(IdType.AUTO)`，含 `@TableField(exist=false)` 的展示用关联字段。Mapper 均为 `@Mapper public interface XxxMapper extends BaseMapper<Xxx> {}` 一行式。

验证: `cd backend && mvn compile`

---

## Task 3: Club Module — Service + Controller

**Files (3):**
- `backend/src/main/java/com/campus/module/club/service/ClubService.java`
- `backend/src/main/java/com/campus/module/club/service/impl/ClubServiceImpl.java`
- `backend/src/main/java/com/campus/module/club/controller/ClubController.java`

Service 接口定义:
- 社团: `getClubs()`、`getClubById(id)`、`saveClub(c)`、`updateClub(id,c)`、`applyMember(clubId,userId,reason)`、`approveMember(memberId,status)`、`getMembers(clubId)`
- 活动: `pageActivities(clubId,page,size)`、`saveActivity(a)`、`enroll(activityId,userId)`、`getEnrollments(activityId)`、`updateActivitySummary(id,summary,images)`
- 场地: `getVenues()`、`pageBookings(userId,role,page,size)`、`applyBooking(b)`、`approveBooking(id,approverId,status,reason)`

Controller 挂载在 `@RequestMapping("/api/club")`，18 个端点。Service 实现使用 MyBatis-Plus LambdaQueryWrapper + @Transactional + BusinessException。

验证: `cd backend && mvn compile`

---

## Task 4: Growth Module — Entities + Mappers

**Files (4):**

2 个实体:
- `backend/src/main/java/com/campus/module/growth/entity/StudentProfile.java`
- `backend/src/main/java/com/campus/module/growth/entity/CheckIn.java`

2 个 Mapper:
- `backend/src/main/java/com/campus/module/growth/mapper/StudentProfileMapper.java`
- `backend/src/main/java/com/campus/module/growth/mapper/CheckInMapper.java`

CheckIn 实体还需要一个 record 实体 `CheckInRecord.java` 和对应 mapper — 共 4 个文件。

验证: `cd backend && mvn compile`

---

## Task 5: Growth Module — Service + Controller

**Files (3):**
- `backend/src/main/java/com/campus/module/growth/service/GrowthService.java`
- `backend/src/main/java/com/campus/module/growth/service/impl/GrowthServiceImpl.java`
- `backend/src/main/java/com/campus/module/growth/controller/GrowthController.java`

Service 接口定义:
- 档案: `getProfile(studentId)`、`saveOrUpdateProfile(p)`、`addEvaluation(studentId,content,teacherId)`
- 签到: `pageCheckIns(teacherId,page,size)`、`createCheckIn(c)`、`doCheckIn(checkinId,studentId)`、`getCheckInRecords(checkinId)`、`getCheckInStatus(checkinId,studentId)`

Controller 挂载在 `@RequestMapping("/api/growth")`，9 个端点。

验证: `cd backend && mvn compile`

---

## Task 6: Frontend — API + Router + Sidebar

**Files:**
- Create: `frontend/src/api/club.ts`
- Create: `frontend/src/api/growth.ts`
- Modify: `frontend/src/router/index.ts` — 新增 7 条路由
- Modify: `frontend/src/components/AppSidebar.vue` — 新增 2 组菜单

路由:
```typescript
{ path: 'club/list', name: 'ClubList', component: () => import('@/views/club/ClubList.vue') },
{ path: 'club/activity', name: 'ActivityList', component: () => import('@/views/club/ActivityList.vue') },
{ path: 'club/venue', name: 'VenueBooking', component: () => import('@/views/club/VenueBooking.vue') },
{ path: 'growth/profile', name: 'StudentProfile', component: () => import('@/views/growth/StudentProfile.vue') },
{ path: 'growth/checkin', name: 'CheckIn', component: () => import('@/views/growth/CheckIn.vue') },
```

侧边栏菜单:
```vue
<el-sub-menu index="club">
  <template #title><el-icon><TrophyBase /></el-icon><span>社团活动</span></template>
  <el-menu-item index="/club/list">社团列表</el-menu-item>
  <el-menu-item index="/club/activity">活动中心</el-menu-item>
  <el-menu-item index="/club/venue">场地预约</el-menu-item>
</el-sub-menu>
<el-sub-menu index="growth">
  <template #title><el-icon><TrendCharts /></el-icon><span>成长档案</span></template>
  <el-menu-item v-if="userStore.role==='student'" index="/growth/profile">我的档案</el-menu-item>
  <el-menu-item index="/growth/checkin">签到打卡</el-menu-item>
</el-sub-menu>
```

API 文件 cover 所有后端端点。

验证: 前端无 TypeScript 编译错误。

---

## Task 7: Frontend — Club Pages

**Files (3):**

- `frontend/src/views/club/ClubList.vue` — 社团卡片网格 + 申请加入/审批弹窗
- `frontend/src/views/club/ActivityList.vue` — 活动列表 + 发布/报名/总结
- `frontend/src/views/club/VenueBooking.vue` — 场地卡片 + 预约表单 + 时间线审批

每个页面与一期风格一致：白卡片、细边框、Tailwind 灰度色板、淡入动画。

---

## Task 8: Frontend — Growth Pages

**Files (2):**

- `frontend/src/views/growth/StudentProfile.vue` — 档案卡片（基本信息+获奖经历+教师评语+GPA）+ 编辑
- `frontend/src/views/growth/CheckIn.vue` — 教师发起签到 / 学生扫码打卡 / 签到统计

---

## Self-Review

1. **Spec coverage**: T1 covers all 9 tables (club_info, club_member, club_activity, club_activity_enrollment, club_venue, club_venue_booking, growth_profile, growth_checkin, growth_checkin_record) + seed data. T2-T3 cover club backend. T4-T5 cover growth backend. T6-T8 cover frontend. All §4.4 and §4.6 requirements mapped.
2. **No placeholders**: All code to be generated in Tasks 2-5 follows established patterns from existing codebase.
3. **Type consistency**: Table names match `@TableName`, field names match column names via `map-underscore-to-camel-case`.

---

## Execution Handoff

Plan complete. Two execution options:

**1. Subagent-Driven (recommended)** — I dispatch a fresh subagent per task, review between tasks, fast iteration

**2. Inline Execution** — Execute tasks in this session using executing-plans, batch execution with checkpoints

Which approach?

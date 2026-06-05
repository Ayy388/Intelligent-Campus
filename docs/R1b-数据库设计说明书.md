# 智能校园管理系统 — 数据库设计说明书

| 文档编号 | R1b | 版本 | 1.0 |
|---------|-----|------|-----|
| 编写日期 | 2026-06-05 | 状态 | 初稿 |
| 基于需求 | R0-需求规格说明书 v2.0 |  |  |

---

## 修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| 1.0 | 2026-06-05 | 初稿 — 覆盖全部 34 张表的字段定义、索引、ER 关系、核心 SQL | Claude |

---

## 目录

1. [数据库总览](#1-数据库总览)
2. [ER 关系描述](#2-er-关系描述)
3. [表结构详述](#3-表结构详述)
4. [索引设计](#4-索引设计)
5. [核心 SQL 示例](#5-核心-sql-示例)

---

## 1. 数据库总览

### 1.1 基本信息

| 项目 | 值 |
|------|-----|
| 数据库类型 | MySQL 8.0+ |
| 数据库名称 | `intelligent_campus` |
| 字符集 | `utf8mb4` |
| 排序规则 | `utf8mb4_unicode_ci` |
| 引擎 | InnoDB（所有表） |
| 表总数 | 34 |
| 初始化方式 | `DataInitializer.java` + `CREATE TABLE IF NOT EXISTS` |

### 1.2 表清单（按模块分组）

| 模块 | 表名 | 说明 | 行数 |
|------|------|------|:----:|
| **系统核心** | | | |
| | `sys_role` | 角色定义表 | 4 |
| | `sys_user` | 用户表 | ~300 |
| | `sys_department` | 部门（学院）表 | 2 |
| | `sys_major` | 专业表 | 2 |
| | `sys_grade` | 年级表 | 1 |
| | `sys_class` | 班级表 | 4 |
| **教务管理** | | | |
| | `edu_course` | 课程表 | ~15 |
| | `edu_semester` | 学期表 | 1 |
| | `edu_course_selection` | 选课记录表 | ~280 |
| | `edu_grade` | 成绩表 | 0 |
| | `edu_course_class` | 课程-班级关联表 | ~30 |
| | `edu_training_plan` | 培养方案表 | 2 |
| | `edu_training_plan_item` | 培养方案课程项表 | 0 |
| **行政服务** | | | |
| | `admin_notification` | 通知公告表 | 0 |
| | `admin_notification_read` | 通知已读表 | 0 |
| | `admin_leave` | 请假表 | 0 |
| **社团管理** | | | |
| | `club_info` | 社团信息表 | 3 |
| | `club_member` | 社团成员表 | 3 |
| | `club_activity` | 社团活动表 | 0 |
| | `club_activity_enrollment` | 社团活动报名表 | 0 |
| | `club_venue` | 场地表 | 4 |
| | `club_venue_booking` | 场地预约表 | 0 |
| **校园活动** | | | |
| | `activity_center` | 活动中心表 | 5 |
| | `activity_registration` | 活动报名表 | 1 |
| **校园生活** | | | |
| | `life_card_recharge` | 校园卡充值表 | 0 |
| | `life_lost_found` | 失物招领表 | 0 |
| **消息通信** | | | |
| | `message_conversation` | 私信会话表 | 0 |
| | `message_detail` | 私信消息表 | 0 |
| | `message_announcement_push` | 公告推送表 | 0 |
| **AI 助手** | | | |
| | `ai_conversation` | AI 对话会话表 | 0 |
| | `ai_message` | AI 对话消息表 | 0 |
| **待办事项** | | | |
| | `user_todo` | 用户待办表 | 0 |
| **（废弃）** | | | |
| | `growth_profile` | 学生档案表（待清理） | — |
| | `growth_checkin` | 签到表（待清理） | — |
| | `growth_checkin_record` | 签到记录表（待清理） | — |

---

## 2. ER 关系描述

### 2.1 核心实体关系

```
sys_department(1) ────→ (N) sys_major
sys_major(1) ─────────→ (N) sys_class
sys_grade(1) ─────────→ (N) sys_class
sys_class(1) ─────────→ (N) sys_user（学生）
sys_user(1) ─────────→ (N) sys_user（辅导员）  [counselor_id 自引用]
sys_role(1) ─────────→ (N) sys_user

sys_user/教师(1) ───→ (N) edu_course
edu_course(M) ──────→ (N) edu_course_class
sys_class(1) ───────→ (N) edu_course_class
sys_user/学生(1) ───→ (N) edu_course_selection
edu_course(1) ──────→ (N) edu_course_selection
edu_course(1) ──────→ (N) edu_grade
sys_user/学生(1) ───→ (N) edu_grade
edu_training_plan(1) → (N) edu_training_plan_item
sys_major(1) ───────→ (N) edu_training_plan
sys_grade(1) ───────→ (N) edu_training_plan
```

### 2.2 业务关系说明

| 关系 | 说明 |
|------|------|
| 部门 → 专业 | 一个部门（学院）下有多个专业 |
| 专业 → 班级 | 一个专业下有多个班级 |
| 年级 → 班级 | 一个年级下可设多个班级 |
| 班级 → 学生 | 一个班级有多个学生 |
| 班级 → 辅导员 | 一个班级有一个辅导员（sys_class.counselor_id → sys_user.id）|
| 教师 → 课程 | 一个教师可授多门课程，一门课程仅一个教师 |
| 课程 → 班级 | 多对多，通过 edu_course_class 关联 |
| 课程 → 选课 | 一门课程对应多条选课记录，一个学生对一个课程仅一条 |
| 学生 → 成绩 | 一个学生在每门课程中一条成绩 |
| 培养方案 → 课程项 | 一个方案包含多个学期的课程项 |
| 学生 → 请假 | 一个学生可有多条请假记录 |
| 用户 → 社团 | 多对多，通过 club_member 关联 |
| 用户 → 活动 | 多对多，通过 activity_registration 关联 |
| 用户 → 消息 | 一对一对话，通过 conversation → message_detail 追溯 |

---

## 3. 表结构详述

### 3.1 sys_role — 角色定义

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| role_code | VARCHAR | 32 | | ✅ | — | 角色编码（student/teacher/admin/counselor） |
| role_name | VARCHAR | 32 | | ✅ | — | 角色名称（学生/教师/管理员/辅导员） |
| description | VARCHAR | 255 | | ❌ | — | 角色描述 |

**初始化数据：**

| id | role_code | role_name | description |
|:--:|:---------|:---------|:------------|
| 1 | student | 学生 | 在校学生 |
| 2 | teacher | 教师 | 授课教师 |
| 3 | admin | 管理员 | 系统管理员 |
| 4 | counselor | 辅导员 | 学生辅导员 |

---

### 3.2 sys_user — 用户表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 用户 ID |
| username | VARCHAR | 64 | | ✅ | — | 登录用户名（学号/工号） |
| password | VARCHAR | 255 | | ✅ | — | BCrypt 加密密码 |
| real_name | VARCHAR | 64 | | ✅ | — | 真实姓名 |
| gender | VARCHAR | 8 | | ❌ | — | 性别 |
| phone | VARCHAR | 20 | | ❌ | — | 手机号 |
| email | VARCHAR | 128 | | ❌ | — | 邮箱 |
| avatar | VARCHAR | 512 | | ❌ | — | 头像 URL |
| role_id | BIGINT | — | | ✅ | — | FK → sys_role.id |
| department_id | BIGINT | — | | ❌ | — | FK → sys_department.id |
| major_id | BIGINT | — | | ❌ | — | FK → sys_major.id |
| class_id | BIGINT | — | | ❌ | — | FK → sys_class.id |
| grade_id | BIGINT | — | | ❌ | — | FK → sys_grade.id |
| counselor_id | BIGINT | — | | ❌ | — | FK → sys_user.id（辅导员自引用） |
| status | TINYINT | — | | ✅ | 1 | 状态：0=禁用, 1=启用 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段（@TableField(exist=false)）：** role_name, department_name, major_name, grade_name, class_name, counselor_name, counselor_classes（不持久化，通过 resolveUserExtra() 运行时填充）

**索引：** UK(username)

---

### 3.3 sys_department — 部门（学院）表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| name | VARCHAR | 64 | | ✅ | — | 部门名称 |
| code | VARCHAR | 32 | | ❌ | — | 部门编码 |
| sort_order | INT | — | | ❌ | 0 | 排序序号 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

---

### 3.4 sys_major — 专业表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| name | VARCHAR | 64 | | ✅ | — | 专业名称 |
| code | VARCHAR | 32 | | ❌ | — | 专业编码 |
| department_id | BIGINT | — | | ✅ | — | FK → sys_department.id |
| years | INT | — | | ❌ | 4 | 学制（年） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**索引：** IDX(department_id)

---

### 3.5 sys_grade — 年级表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| name | VARCHAR | 64 | | ✅ | — | 年级名称（如 2025级） |
| year | VARCHAR | 16 | | ❌ | — | 年份（如 2025） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

---

### 3.6 sys_class — 班级表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| class_name | VARCHAR | 64 | | ✅ | — | 班级名称 |
| department_id | BIGINT | — | | ✅ | — | FK → sys_department.id |
| major_id | BIGINT | — | | ✅ | — | FK → sys_major.id |
| grade_id | BIGINT | — | | ✅ | — | FK → sys_grade.id |
| counselor_id | BIGINT | — | | ❌ | — | FK → sys_user.id（辅导员） |
| advisor | VARCHAR | 64 | | ❌ | — | 班主任姓名（文本字段） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** department_name, major_name, grade_name, counselor_name

**索引：** IDX(department_id, major_id), IDX(counselor_id)

**业务约束：**
- 一个辅导员管理的多个班级必须属于**同一专业（major_id）+ 同一年级（grade_id）**
- 一个辅导员管理的班级数量建议不超过 3 个
- 在校验层面，当为班级选择辅导员时，系统需检查该辅导员已有班级的 major_id + grade_id 是否与当前班级一致

---

### 3.7 edu_semester — 学期表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| xn | VARCHAR | 32 | | ✅ | — | 学年代码（如 2025-2026） |
| xqjc | VARCHAR | 8 | | ✅ | — | 学期代码（如 1, 2） |
| xqqc | VARCHAR | 64 | | ❌ | — | 学期全称（如 2025-2026学年第二学期） |
| ksrq | DATE | — | | ❌ | — | 开始日期 |
| jsrq | DATE | — | | ❌ | — | 结束日期 |
| zc | INT | — | | ❌ | 20 | 总周数 |
| status | TINYINT | — | | ✅ | 0 | 是否当前学期（0=否, 1=是） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**业务规则：** 仅允许一条记录的 status=1

---

### 3.8 edu_course — 课程表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| course_code | VARCHAR | 64 | | ✅ | — | 课程代码 |
| course_name | VARCHAR | 128 | | ✅ | — | 课程名称 |
| teacher_id | BIGINT | — | | ✅ | — | FK → sys_user.id（授课教师） |
| credit | DECIMAL(3,1) | — | | ✅ | — | 学分 |
| hours | INT | — | | ✅ | — | 总学时 |
| semester | VARCHAR | 32 | | ✅ | — | 开课学期（对应 edu_semester.xn + xqjc）|
| classroom | VARCHAR | 64 | | ❌ | — | 上课教室 |
| schedule | JSON | — | | ❌ | — | 排课信息（JSON 数组） |
| start_week | INT | — | | ❌ | 1 | 起始周 |
| end_week | INT | — | | ❌ | 20 | 结束周 |
| capacity | INT | — | | ✅ | 60 | 课程容量 |
| enrolled | INT | — | | ✅ | 0 | 已选人数 |
| course_type | VARCHAR | 16 | | ✅ | — | 课程类型：required(必修)/elective(选修) |
| min_students | INT | — | | ❌ | 1 | 最低开课人数 |
| enroll_end | DATETIME | — | | ❌ | — | 选课截止时间 |
| description | TEXT | — | | ❌ | — | 课程描述 |
| status | TINYINT | — | | ✅ | 0 | 课程状态：0=未发布, 1=选课中, 2=已确认, 3=已取消 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** teacher_name（运行时通过 teacher_id 查询填充）

**索引：** UK(course_code), IDX(teacher_id), IDX(status)

**`schedule` JSON 格式：**

```json
[
  {
    "day": 1,
    "timeSlot": 1,
    "weeks": "all",
    "startWeek": 1,
    "endWeek": 16
  },
  {
    "day": 3,
    "timeSlot": 2,
    "weeks": "even",
    "startWeek": 1,
    "endWeek": 16
  }
]
```

**schedule 字段说明：**

| 字段 | 类型 | 取值范围 | 说明 |
|------|------|---------|------|
| day | int | 1-5 | 1=周一, 2=周二, 3=周三, 4=周四, 5=周五 |
| timeSlot | int | 1-4 | 1=1-2节, 2=3-4节, 3=5-6节, 4=7-8节 |
| weeks | string | "all"/"odd"/"even" | 全周/单周/双周 |
| startWeek | int | 1-20 | 开始周（默认 1） |
| endWeek | int | 1-20 | 结束周 |

---

### 3.9 edu_course_class — 课程-班级关联表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| course_id | BIGINT | — | | ✅ | — | FK → edu_course.id |
| class_id | BIGINT | — | | ✅ | — | FK → sys_class.id |
| is_required | TINYINT | — | | ❌ | 1 | 是否必修（1=必修, 0=选修） |

**业务规则：** 必修课(class_id → is_required=1)的班级学生自动生成选课记录。选修课不限制班级。
**索引：** UK(course_id, class_id)

---

### 3.10 edu_course_selection — 选课记录表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| student_id | BIGINT | — | | ✅ | — | FK → sys_user.id（学生） |
| course_id | BIGINT | — | | ✅ | — | FK → edu_course.id |
| semester | VARCHAR | 32 | | ❌ | — | 学期标识 |
| select_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 选课时间 |
| status | TINYINT | — | | ❌ | 0 | 选课状态（保留字段） |
| select_type | VARCHAR | 16 | | ❌ | "manual" | 选课类型：auto(自动分配必修)/manual(自主选课) |

**虚拟字段：** course_name, student_name, course_status, student_class_name, student_department, student_phone, student_username, graded

**索引：** UK(student_id, course_id), IDX(course_id), IDX(student_id)

---

### 3.11 edu_grade — 成绩表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| student_id | BIGINT | — | | ✅ | — | FK → sys_user.id（学生） |
| course_id | BIGINT | — | | ✅ | — | FK → edu_course.id |
| teacher_id | BIGINT | — | | ✅ | — | FK → sys_user.id（授课教师） |
| score | DECIMAL(5,1) | — | | ❌ | — | 数值成绩（0-100） |
| grade_type | VARCHAR | 16 | | ❌ | — | 成绩类型（数值制/等级制） |
| grade_level | VARCHAR | 16 | | ❌ | — | 等级成绩（优秀/良好/中等/及格/不及格） |
| semester | VARCHAR | 32 | | ❌ | — | 学期 |
| remark | VARCHAR | 255 | | ❌ | — | 备注 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** course_name, student_name, student_username

**索引：** UK(student_id, course_id), IDX(course_id), IDX(student_id)

---

### 3.12 edu_training_plan — 培养方案表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| name | VARCHAR | 128 | | ✅ | — | 方案名称 |
| major_id | BIGINT | — | | ✅ | — | FK → sys_major.id |
| grade_id | BIGINT | — | | ✅ | — | FK → sys_grade.id |
| total_semesters | INT | — | | ✅ | 8 | 总学期数 |
| status | TINYINT | — | | ❌ | 0 | 状态（0=未启用, 1=启用） |
| description | TEXT | — | | ❌ | — | 方案描述 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** major_name, grade_name

**索引：** UK(major_id, grade_id)

---

### 3.13 edu_training_plan_item — 培养方案课程项表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| plan_id | BIGINT | — | | ✅ | — | FK → edu_training_plan.id |
| semester_number | INT | — | | ✅ | — | 所属学期号（1, 2, 3...） |
| course_name | VARCHAR | 128 | | ✅ | — | 课程名称 |
| course_code | VARCHAR | 64 | | ❌ | — | 课程代码 |
| credit | DECIMAL(3,1) | — | | ✅ | — | 学分 |
| hours | INT | — | | ✅ | — | 学时 |
| is_required | TINYINT | — | | ❌ | 1 | 是否必修 |
| status | TINYINT | — | | ❌ | 0 | 状态 |
| generated_course_id | BIGINT | — | | ❌ | — | 自动开课时生成的 FK → edu_course.id |
| sort_order | INT | — | | ❌ | 0 | 排序序号 |

**虚拟字段：** generated_course_name, generated_course_status

**索引：** IDX(plan_id, semester_number)

---

### 3.14 admin_notification — 通知公告表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| title | VARCHAR | 255 | | ✅ | — | 通知标题 |
| content | TEXT | — | | ✅ | — | 通知内容 |
| category | VARCHAR | 32 | | ❌ | "general" | 分类（general/edu/admin） |
| publisher_id | BIGINT | — | | ✅ | — | FK → sys_user.id（发布人） |
| is_top | TINYINT | — | | ❌ | 0 | 是否置顶 |
| is_urgent | TINYINT | — | | ❌ | 0 | 是否紧急 |
| view_count | INT | — | | ❌ | 0 | 浏览次数 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 发布时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** publisher_name, read（当前用户是否已读）

**索引：** IDX(publisher_id), IDX(category)

---

### 3.15 admin_notification_read — 通知已读记录表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| notification_id | BIGINT | — | | ✅ | — | FK → admin_notification.id |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| read_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 阅读时间 |

**索引：** UK(notification_id, user_id)

---

### 3.16 admin_leave — 请假表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| student_id | BIGINT | — | | ✅ | — | FK → sys_user.id（请假学生） |
| leave_type | VARCHAR | 16 | | ✅ | — | 请假类型（sick/personal/official） |
| start_time | DATETIME | — | | ✅ | — | 开始时间 |
| end_time | DATETIME | — | | ✅ | — | 结束时间 |
| reason | TEXT | — | | ✅ | — | 请假原因 |
| attachment | VARCHAR | 512 | | ❌ | — | 附件图片 URL |
| teacher_id | BIGINT | — | | ❌ | — | FK → sys_user.id（审批人） |
| status | TINYINT | — | | ✅ | 0 | 状态（0=待审批, 1=已通过, 2=已驳回） |
| reject_reason | VARCHAR | 255 | | ❌ | — | 驳回原因 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** student_name

**索引：** IDX(student_id), IDX(status)

---

### 3.17 club_info — 社团信息表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| name | VARCHAR | 64 | | ✅ | — | 社团名称 |
| description | TEXT | — | | ❌ | — | 社团描述 |
| logo | VARCHAR | 512 | | ❌ | — | 社团 Logo URL |
| advisor_id | BIGINT | — | | ❌ | — | FK → sys_user.id（指导教师） |
| president_id | BIGINT | — | | ❌ | — | FK → sys_user.id（社长） |
| member_count | INT | — | | ❌ | 0 | 成员人数 |
| status | TINYINT | — | | ✅ | 0 | 状态（0=待审批, 1=已激活, 2=已解散, 3=申请解散） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** advisor_name, president_name

**索引：** IDX(president_id)

---

### 3.18 club_member — 社团成员表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| club_id | BIGINT | — | | ✅ | — | FK → club_info.id |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| role | VARCHAR | 32 | | ❌ | "member" | 角色（president/vice_president/member） |
| status | VARCHAR | 16 | | ❌ | — | 状态（pending/approved/rejected） |
| apply_reason | VARCHAR | 255 | | ❌ | — | 申请理由 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 加入时间 |

**虚拟字段：** user_name, club_name

**索引：** UK(club_id, user_id)

---

### 3.19 club_activity — 社团活动表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| club_id | BIGINT | — | | ✅ | — | FK → club_info.id |
| title | VARCHAR | 128 | | ✅ | — | 活动标题 |
| description | TEXT | — | | ❌ | — | 活动描述 |
| activity_type | VARCHAR | 32 | | ❌ | — | 活动类型 |
| location | VARCHAR | 128 | | ❌ | — | 活动地点 |
| start_time | DATETIME | — | | ❌ | — | 开始时间 |
| end_time | DATETIME | — | | ❌ | — | 结束时间 |
| max_enroll | INT | — | | ❌ | 0 | 最大报名人数 |
| enrolled | INT | — | | ❌ | 0 | 已报名人数 |
| summary | TEXT | — | | ❌ | — | 活动总结 |
| images | VARCHAR | 1024 | | ❌ | — | 活动图片（多个 URL） |
| status | TINYINT | — | | ✅ | 0 | 状态（0=pending, 1=approved, 2=rejected） |

**虚拟字段：** club_name

**索引：** IDX(club_id)

---

### 3.20 club_activity_enrollment — 社团活动报名表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| activity_id | BIGINT | — | | ✅ | — | FK → club_activity.id |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| status | VARCHAR | 16 | | ❌ | — | 报名状态 |

**虚拟字段：** user_name, activity_title

**索引：** UK(activity_id, user_id)

---

### 3.21 club_venue — 场地表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| name | VARCHAR | 64 | | ✅ | — | 场地名称 |
| location | VARCHAR | 128 | | ❌ | — | 场地位置 |
| capacity | INT | — | | ❌ | 0 | 容纳人数 |
| description | TEXT | — | | ❌ | — | 场地描述 |
| status | TINYINT | — | | ❌ | 1 | 状态（0=不可用, 1=可用） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

---

### 3.22 club_venue_booking — 场地预约表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| venue_id | BIGINT | — | | ✅ | — | FK → club_venue.id |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id（预约人） |
| title | VARCHAR | 128 | | ✅ | — | 预约标题 |
| purpose | VARCHAR | 255 | | ❌ | — | 预约用途 |
| start_time | DATETIME | — | | ✅ | — | 开始时间 |
| end_time | DATETIME | — | | ✅ | — | 结束时间 |
| approver_id | BIGINT | — | | ❌ | — | FK → sys_user.id（审批人） |
| status | TINYINT | — | | ❌ | 0 | 状态（0=待审批, 1=已通过, 2=已驳回） |
| reject_reason | VARCHAR | 255 | | ❌ | — | 驳回原因 |

**虚拟字段：** venue_name, user_name

**索引：** IDX(venue_id), IDX(status)

---

### 3.23 activity_center — 活动中心表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| title | VARCHAR | 128 | | ✅ | — | 活动标题 |
| description | TEXT | — | | ❌ | — | 活动描述 |
| location | VARCHAR | 128 | | ❌ | — | 活动地点 |
| start_time | DATETIME | — | | ❌ | — | 开始时间 |
| end_time | DATETIME | — | | ❌ | — | 结束时间 |
| max_participants | INT | — | | ❌ | 0 | 最大参与人数 |
| current_participants | INT | — | | ❌ | 0 | 当前参与人数 |
| cover_image | VARCHAR | 512 | | ❌ | — | 封面图片 URL |
| category | VARCHAR | 32 | | ❌ | — | 活动类别 |
| club_id | BIGINT | — | | ❌ | — | FK → club_info.id（关联社团） |
| summary | TEXT | — | | ❌ | — | 活动总结 |
| images | VARCHAR | 1024 | | ❌ | — | 活动图片 |
| activity_type | VARCHAR | 32 | | ❌ | — | 活动类型 |
| creator_id | BIGINT | — | | ✅ | — | FK → sys_user.id（创建人） |
| creator_role | VARCHAR | 16 | | ❌ | — | 创建人角色 |
| status | TINYINT | — | | ✅ | 0 | 状态（0=待审批, 1=已审批, 2=已驳回, 3=已确认, 4=已结束） |
| approver_id | BIGINT | — | | ❌ | — | FK → sys_user.id（审批人） |
| reject_reason | VARCHAR | 255 | | ❌ | — | 驳回原因 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** creator_name, registered（当前用户是否已报名）, club_name

**索引：** IDX(creator_id), IDX(status), IDX(club_id)

---

### 3.24 activity_registration — 活动报名表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| activity_id | BIGINT | — | | ✅ | — | FK → activity_center.id |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 报名时间 |

**虚拟字段：** user_name, activity_title

**索引：** UK(activity_id, user_id)

---

### 3.25 life_card_recharge — 校园卡充值表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| amount | DECIMAL(10,2) | — | | ✅ | — | 充值金额 |
| balance | DECIMAL(10,2) | — | | ❌ | — | 充值后余额 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 充值时间 |

**索引：** IDX(user_id)

---

### 3.26 life_lost_found — 失物招领表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id（发布人） |
| type | TINYINT | — | | ✅ | — | 类型（0=丢失, 1=拾到） |
| title | VARCHAR | 128 | | ✅ | — | 标题 |
| description | TEXT | — | | ❌ | — | 详细描述 |
| images | VARCHAR | 1024 | | ❌ | — | 图片 URL |
| location | VARCHAR | 128 | | ❌ | — | 丢失/拾到地点 |
| contact | VARCHAR | 128 | | ❌ | — | 联系方式 |
| status | TINYINT | — | | ❌ | 0 | 状态（0=待解决, 1=已解决） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 发布时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**虚拟字段：** user_name

**索引：** IDX(user_id), IDX(type), IDX(status)

---

### 3.27 message_conversation — 私信会话表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| user1_id | BIGINT | — | | ✅ | — | FK → sys_user.id（会话参与者 1） |
| user2_id | BIGINT | — | | ✅ | — | FK → sys_user.id（会话参与者 2） |
| last_message | VARCHAR | 255 | | ❌ | — | 最后一条消息摘要 |
| last_time | DATETIME | — | | ❌ | — | 最后消息时间 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |

**虚拟字段：** peer_name（对方姓名）

**索引：** UK(user1_id, user2_id), IDX(last_time)

**业务规则：** user1_id < user2_id，保证同一对用户只有一条会话记录

---

### 3.28 message_detail — 私信消息表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| conversation_id | BIGINT | — | | ✅ | — | FK → message_conversation.id |
| sender_id | BIGINT | — | | ✅ | — | FK → sys_user.id（发送人） |
| content | TEXT | — | | ✅ | — | 消息内容 |
| is_read | TINYINT | — | | ❌ | 0 | 是否已读（0=未读, 1=已读） |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 发送时间 |

**虚拟字段：** sender_name

**索引：** IDX(conversation_id, create_time)

---

### 3.29 message_announcement_push — 公告推送表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| title | VARCHAR | 128 | | ✅ | — | 推送标题 |
| content | TEXT | — | | ✅ | — | 推送内容 |
| target_type | VARCHAR | 32 | | ✅ | — | 目标类型（all/student/teacher/counselor） |
| target_value | VARCHAR | 64 | | ❌ | — | 目标值 |
| publisher_id | BIGINT | — | | ✅ | — | FK → sys_user.id（发布人） |
| send_time | DATETIME | — | | ❌ | — | 发送时间 |
| status | TINYINT | — | | ❌ | — | 状态 |

**虚拟字段：** publisher_name

---

### 3.30 ai_conversation — AI 对话会话表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| title | VARCHAR | 128 | | ❌ | — | 会话标题 |
| message_count | INT | — | | ❌ | 0 | 消息数量 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**索引：** IDX(user_id)

---

### 3.31 ai_message — AI 对话消息表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| conversation_id | BIGINT | — | | ✅ | — | FK → ai_conversation.id |
| role | VARCHAR | 16 | | ✅ | — | 角色（user/assistant） |
| content | TEXT | — | | ✅ | — | 消息内容 |
| token_count | INT | — | | ❌ | — | Token 数量 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 发送时间 |

**索引：** IDX(conversation_id, create_time)

---

### 3.32 user_todo — 用户待办表

| 字段名 | 类型 | 长度 | 主键 | 必填 | 默认值 | 说明 |
|-------|------|:----:|:----:|:----:|:-----:|------|
| id | BIGINT | — | ✅ | ✅ | 自增 | 主键 ID |
| user_id | BIGINT | — | | ✅ | — | FK → sys_user.id |
| title | VARCHAR | 255 | | ✅ | — | 待办标题 |
| completed | TINYINT | — | | ❌ | 0 | 是否完成（0=未完成, 1=已完成） |
| priority | VARCHAR | 16 | | ❌ | "medium" | 优先级（low/medium/high） |
| due_date | DATE | — | | ❌ | — | 截止日期 |
| sort_order | INT | — | | ❌ | 0 | 排序序号 |
| create_time | DATETIME | — | | ❌ | CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | — | | ❌ | ON UPDATE | 更新时间 |

**索引：** IDX(user_id, completed), IDX(user_id, priority)

---

### 3.33 growth_profile — 学生档案表（废弃，待清理）

> ⚠️ **此表已废弃**，因需求变更移除了学生成长模块。保留用于参考，后续可删除。

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|:----:|------|
| id | BIGINT | — | 主键 ID |
| student_id | BIGINT | — | FK → sys_user.id |
| awards | TEXT | — | 获奖经历 |
| experiences | TEXT | — | 实践经历 |
| evaluation | TEXT | — | 教师评价 |
| gpa | DECIMAL(3,1) | — | GPA |

---

### 3.34 growth_checkin / growth_checkin_record（废弃，待清理）

> ⚠️ 此两表已废弃，因需求变更移除了签到功能。后续可清理。

**growth_checkin：** teacher_id, course_id, class_id, title, checkin_type, start_time, end_time, total_count, checked_count, status
**growth_checkin_record：** checkin_id, student_id, checkin_time, status

---

## 4. 索引设计

### 4.1 主键索引

所有表均以 `id BIGINT AUTO_INCREMENT` 作为主键，使用 InnoDB 聚集索引。

### 4.2 唯一索引（UK）

| 表 | 字段 | 说明 |
|----|------|------|
| sys_user | username | 用户名唯一 |
| edu_course | course_code | 课程代码唯一 |
| edu_course_class | (course_id, class_id) | 一门课程对一个班级只出现一次 |
| edu_course_selection | (student_id, course_id) | 一个学生一门课程只能选一次 |
| edu_grade | (student_id, course_id) | 一个学生一门课程只有一条成绩 |
| edu_training_plan | (major_id, grade_id) | 一个专业+年级只有一个培养方案 |
| admin_notification_read | (notification_id, user_id) | 一条通知一个用户只能标记一次已读 |
| club_member | (club_id, user_id) | 一个用户在一个社团只有一个角色 |
| club_activity_enrollment | (activity_id, user_id) | 一个活动一个用户只能报名一次 |
| activity_registration | (activity_id, user_id) | 同上 |
| message_conversation | (user1_id, user2_id) | 一对用户只有一条会话 |

### 4.3 外键索引（IDX）

| 表 | 索引字段 | 说明 |
|----|---------|------|
| sys_user | role_id, department_id, major_id, class_id, counselor_id | 按角色/部门/班级/辅导员查询 |
| sys_major | department_id | 按部门查询专业 |
| sys_class | (department_id, major_id), counselor_id | 按专业/辅导员查询班级 |
| edu_course | teacher_id, status | 按教师/状态查询课程 |
| edu_course_selection | student_id, course_id | 按学生/课程查询选课记录 |
| edu_grade | student_id, course_id | 按学生/课程查询成绩 |
| edu_training_plan_item | (plan_id, semester_number) | 按方案+学期查询课程项 |
| admin_notification | publisher_id, category | 按发布人/分类查询 |
| admin_leave | student_id, status | 按学生/状态查询请假 |
| club_info | president_id | 按社长查询 |
| club_activity | club_id | 按社团查询活动 |
| club_venue_booking | venue_id, status | 按场地/状态查询预约 |
| activity_center | creator_id, status, club_id | 按创建人/状态/社团查询 |
| life_card_recharge | user_id | 按用户查询充值记录 |
| life_lost_found | user_id, type, status | 按用户/类型/状态查询 |
| message_conversation | last_time | 按最后时间排序会话 |
| message_detail | (conversation_id, create_time) | 按会话+时间查询消息 |
| ai_conversation | user_id | 按用户查询 AI 会话 |
| ai_message | (conversation_id, create_time) | 按会话+时间查询 AI 消息 |
| user_todo | (user_id, completed), (user_id, priority) | 按用户+状态/优先级查询待办 |

---

## 5. 核心 SQL 示例

### 5.1 选课 — 容量安全更新（CAS）

```sql
-- 更新已选人数，仅在未满时 +1
UPDATE edu_course
SET enrolled = enrolled + 1
WHERE id = #{courseId}
  AND enrolled < capacity;
-- 影响行数 = 1 → 更新成功
-- 影响行数 = 0 → 课程已满
```

### 5.2 选课 — 创建选课记录

```sql
INSERT INTO edu_course_selection (student_id, course_id, semester, select_time, select_type)
VALUES (#{studentId}, #{courseId}, #{semester}, NOW(), #{selectType});
```

### 5.3 学生课表查询

```sql
SELECT
  c.id, c.course_name, c.classroom, c.schedule,
  c.start_week, c.end_week, t.real_name AS teacher_name
FROM edu_course c
JOIN sys_user t ON c.teacher_id = t.id
JOIN edu_course_selection cs ON cs.course_id = c.id
WHERE cs.student_id = #{studentId}
  AND cs.semester = #{semester}
  AND c.status IN (1, 2);  -- 选课中或已确认
```

### 5.4 排课冲突检测 — 教室冲突

```sql
SELECT COUNT(*)
FROM edu_course
WHERE classroom = #{classroom}
  AND id != #{courseId}
  AND JSON_CONTAINS(schedule, JSON_OBJECT(
    'day', CAST(#{day} AS JSON),
    'timeSlot', CAST(#{timeSlot} AS JSON)
  ));
-- 此查询需要额外在应用层校验周模式是否有交集
```

### 5.5 课程成绩统计

```sql
SELECT
  c.id AS course_id,
  c.course_name,
  COUNT(g.id) AS student_count,
  ROUND(AVG(g.score), 1) AS avg_score,
  MAX(g.score) AS max_score,
  MIN(g.score) AS min_score,
  ROUND(SUM(CASE WHEN g.score >= 60 THEN 1 ELSE 0 END) / COUNT(g.id) * 100, 1) AS pass_rate
FROM edu_course c
LEFT JOIN edu_grade g ON g.course_id = c.id
WHERE c.id = #{courseId}
GROUP BY c.id, c.course_name;
```

### 5.6 学生 GPA 计算

```sql
SELECT
  c.course_name,
  g.score,
  c.credit,
  CASE
    WHEN g.score >= 90 THEN 4.0
    WHEN g.score >= 80 THEN 3.0
    WHEN g.score >= 70 THEN 2.0
    WHEN g.score >= 60 THEN 1.0
    ELSE 0
  END AS grade_point
FROM edu_grade g
JOIN edu_course c ON g.course_id = c.id
WHERE g.student_id = #{studentId}
  AND g.semester = #{semester};

-- GPA = SUM(grade_point * credit) / SUM(credit)
```

### 5.7 未读通知计数

```sql
SELECT COUNT(*)
FROM admin_notification n
WHERE n.id NOT IN (
  SELECT notification_id
  FROM admin_notification_read
  WHERE user_id = #{userId}
);
```

### 5.8 教师当天课表

```sql
SELECT c.course_name, c.classroom, c.schedule
FROM edu_course c
WHERE c.teacher_id = #{teacherId}
  AND c.status IN (1, 2)
  AND JSON_CONTAINS(c.schedule, JSON_OBJECT('day', #{currentDay}));
```

> **说明：** 本文档为数据库设计说明书（R1b），定义了全部 34 张表的字段结构、索引和核心 SQL。
>
> **配套文档：**
> - R1a-系统架构设计说明书.md — 系统架构、技术选型
> - R1c-接口设计说明书.md — 全量 API 端点定义
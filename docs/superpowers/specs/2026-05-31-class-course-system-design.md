# 班级-课程体系设计

## 1. 概述

重构现有课程体系，引入行政班管理、必修/选修分离、基于班级的签到机制，使系统更贴合真实高校教务流程。

## 2. 数据模型

### 2.1 班级表 `sys_class`（新建）

```sql
CREATE TABLE sys_class (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name   VARCHAR(100) NOT NULL,  -- "2023级计算机科学与技术1班"
    department   VARCHAR(100),           -- "计算机科学与技术学院"
    major        VARCHAR(100),           -- "计算机科学与技术"
    grade        VARCHAR(20),            -- "2023"
    advisor      VARCHAR(50),            -- 辅导员姓名
    create_time  DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### 2.2 SysUser 表调整

```
SysUser 现有 className(VARCHAR) → 改为 classId(BIGINT)，指向 sys_class.id
```

### 2.3 `edu_course_class` 课程-班级关联表（新建）

```sql
CREATE TABLE edu_course_class (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT NOT NULL,           -- 关联 edu_course.id
    class_id    BIGINT,                    -- 关联 sys_class.id, null=不限班级
    is_required TINYINT(1) DEFAULT 0       -- 1=必修 0=选修
);
```

- 必修课：is_required=1，class_id 必须指定具体班级
- 选修课：is_required=0，class_id 可为 null（不限班级）或指定限制班级

### 2.4 Course 表调整

```java
// 新增字段
private String courseType;         // required(必修) / elective(选修)
private Integer minStudents;       // 最低开课人数（选修课用）
private LocalDateTime enrollEnd;   // 选课截止时间（选修课用）
```

#### 课程状态流转

```
必修课：
  创建 → status=0(未发布) → 管理员下达教学任务 → status=2(已确认)

选修课：
  创建 → status=0(未发布) → 教师提交 → status=1(选课中)
  → 管理员确认开课(人数达标) → status=2(已确认)
  → 管理员取消开课(人数不足) → status=3(已取消)
```

### 2.5 CourseSelection 表调整

```java
// 新增字段
private String selectType;         // auto(系统分配/必修) / manual(自主选课/选修)
```

### 2.6 CheckIn 表调整

```java
// 新增字段
private Long classId;              // 签到面向的班级
```

## 3. 业务流程

### 3.1 必修课流程

```
1. 管理员创建课程 → 设为必修
2. 管理员分配授课教师
3. 管理员关联目标班级（edu_course_class，is_required=1）
4. 管理员点击"下达教学任务"
5. 系统自动：
   a. 课程状态 → status=2(已确认)
   b. 为目标班级所有学生创建选课记录（selectType=auto）
   c. 课程出现在教师课表和学生课表中
6. 教师在我的教学页面查看学生 → 按班级发起签到 → 录入成绩
```

### 3.2 选修课流程

```
1. 教师创建课程 → 设为选修
2. 教师设置：
   a. 报名限制（不限/仅某专业/仅某年级/仅某些班级）
   b. 最低开课人数
   c. 容量上限
   d. 选课截止时间
3. 教师提交 → 课程进入 status=1(选课中)
4. 学生在选课页面查看可选课程（根据限制条件过滤）
5. 学生自主报名 → 选课记录创建（selectType=manual）
6. 到达截止时间后：
   a. 管理员审核选课人数
   b. 人数 ≥ 最低开课人数 → 确认开课 → status=2
   c. 人数 < 最低开课人数 → 取消开课 → status=3，通知学生
```

### 3.3 签到流程

```
教师发起签到：
  1. 选择课程
  2. 选择该课程下的班级（基于 edu_course_class）
  3. 系统通过 CourseSelection 统计应到人数（选了该课+属于该班级）
  4. 签到创建，关联 classId

学生签到：
  1. 只显示自己班级相关的签到
  2. 签到前校验学生是否属于目标班级
```

### 3.4 选课限制校验

学生查看可选课程时，按以下规则过滤：

| 限制类型 | 过滤逻辑 |
|---------|---------|
| 不限制 | 所有学生可见 |
| 仅限某专业 | 学生 major 匹配 |
| 仅限某年级 | 学生 grade 匹配 |
| 仅限指定班级 | student.classId 在 edu_course_class.classId 列表中 |

## 4. 涉及改动

| 模块 | 改动 |
|------|------|
| **新建** sys_class 表 | 班级管理基础数据 |
| **新建** sys_class_mapper/service/controller | 班级 CRUD 接口 |
| **新建** edu_course_class 表 | 课程-班级关联 |
| **新建** edu_course_class_mapper/service | 关联操作接口 |
| **修改** SysUser | className→classId，数据迁移 |
| **修改** Course | 新增 courseType/minStudents/enrollEnd |
| **修改** CourseSelection | 新增 selectType |
| **修改** CheckIn | 新增 classId |
| **修改** CourseServiceImpl | 必修分配、选修限制校验、管理员确认开课 |
| **修改** GrowthServiceImpl | 签到按班级过滤 |
| **修改** CheckIn.vue | 签到创建增加班级选择 |
| **修改** TeacherTeaching.vue | 按班级查看学生 |
| **修改** CourseStudents.vue | 按班级分组展示 |
| **新增** 班级管理页面 | 管理员 CRUD 班级 |
| **修改** 课程创建页面 | 区分必修/选修，设置班级关联 |
| **新增** 下达教学任务功能 | 管理员操作 |
| **新增** 确认开课/取消开课功能 | 管理员操作 |
| **修改** 选课页面 | 按限制条件过滤课程 |
| **修改** 签到页面 | 学生只看本班签到 |
| **修改** 签到发起弹窗 | 增加班级选择 |

## 5. 数据迁移

1. 创建 `sys_class` 表
2. 从现有 `SysUser.className` 中提取去重，生成班级记录
3. 将 `SysUser.className(字符串)` 替换为 `classId(外键)`
4. 将已有课程的关联信息通过管理员操作补充到 `edu_course_class`
# 智能校园管理系统 — 接口设计说明书

| 文档编号 | R1c | 版本 | 1.0 |
|---------|-----|------|-----|
| 编写日期 | 2026-06-05 | 状态 | 初稿 |
| 基于需求 | R0-需求规格说明书 v2.0 |  |  |

---

## 修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| 1.0 | 2026-06-05 | 初稿 — 覆盖全部模块的 API 端点定义 | Claude |

---

## 目录

1. [接口规范](#1-接口规范)
2. [认证模块 — auth](#2-认证模块--auth)
3. [系统管理模块 — sys](#3-系统管理模块--sys)
4. [教务管理模块 — edu](#4-教务管理模块--edu)
5. [行政服务模块 — admin](#5-行政服务模块--admin)
6. [社团管理模块 — club](#6-社团管理模块--club)
7. [校园活动模块 — activity](#7-校园活动模块--activity)
8. [校园生活模块 — life](#8-校园生活模块--life)
9. [消息通信模块 — message](#9-消息通信模块--message)
10. [AI 助手模块 — ai](#10-ai-助手模块--ai)
11. [待办事项模块 — todo](#11-待办事项模块--todo)
12. [关键业务序列图](#12-关键业务序列图)

---

## 1. 接口规范

### 1.1 基础信息

| 项目 | 说明 |
|------|------|
| 基础路径 | `/api` |
| 协议 | HTTP/1.1 |
| 请求格式 | `application/json`（文件上传使用 `multipart/form-data`） |
| 响应格式 | `application/json;charset=utf-8` |
| 字符编码 | UTF-8 |
| 日期格式 | `yyyy-MM-dd HH:mm:ss`（时区 Asia/Shanghai） |

### 1.2 全局响应结构

**成功响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": { /* 业务数据 */ },
  "timestamp": 1717545600000
}
```

**分页响应：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ /* 当前页数据 */ ],
    "total": 280,
    "page": 1,
    "size": 10,
    "pages": 28
  },
  "timestamp": 1717545600000
}
```

**错误响应：**

```json
{
  "code": 400,
  "message": "参数错误：课程容量不能为空",
  "data": null,
  "timestamp": 1717545600000
}
```

### 1.3 HTTP 状态码 vs 业务状态码

| 场景 | HTTP 状态码 | 业务 code |
|------|:----------:|:---------:|
| 成功 | 200 | 200 |
| 参数校验失败 | 400 | 400 |
| 未登录/Token 过期 | 401 | 401 |
| 无权限 | 403 | 403 |
| 资源不存在 | 404 | 404 |
| 业务异常 | 200 | 自定义（500/400等） |
| 服务器错误 | 500 | 500 |

### 1.4 认证方式

```http
Authorization: Bearer <jwt_token>
Header: Content-Type: application/json
```

### 1.5 分页参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
|--------|:----:|:----:|:------:|------|
| page | Integer | ❌ | 1 | 页码，从 1 开始 |
| size | Integer | ❌ | 10 | 每页条数 |

---

## 2. 认证模块 — auth

### POST /api/auth/login — 用户登录

**请求体：**

```json
{
  "username": "s001",
  "password": "123456"
}
```

**响应体：**

```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "userId": 1,
    "username": "s001",
    "realName": "张三",
    "role": "student",
    "avatar": null
  }
}
```

**权限：** 公开

---

### GET /api/auth/me — 获取当前用户信息

**请求头：** `Authorization: Bearer <token>`

**响应体：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "s001",
    "realName": "张三",
    "gender": "男",
    "phone": "13800138001",
    "email": "s001@campus.edu",
    "avatar": null,
    "roleId": 1,
    "roleName": "学生",
    "departmentName": "计算机科学与技术学院",
    "majorName": "计算机科学与技术",
    "gradeName": "2025级",
    "className": "计科2501班",
    "counselorName": "王辅导员"
  }
}
```

**权限：** authenticated

---

### PUT /api/auth/profile — 修改个人资料

**请求体：**

```json
{
  "realName": "张三",
  "email": "newemail@campus.edu",
  "phone": "13800138001"
}
```

**响应体：** `{ "code": 200, "data": null }`

**权限：** authenticated

---

### PUT /api/auth/password — 修改密码

**请求体：**

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

**响应体：** `{ "code": 200, "data": null }`

**权限：** authenticated

---

### POST /api/upload/avatar — 上传头像

**请求格式：** `multipart/form-data`（字段名：`file`）

**响应体：**

```json
{
  "code": 200,
  "data": "/uploads/avatars/xxx.jpg"
}
```

**权限：** authenticated | 限制：2MB, jpg/png

---

### POST /api/upload/image — 上传图片

**请求格式：** `multipart/form-data`（字段名：`file`）

**响应体：**

```json
{
  "code": 200,
  "data": "/uploads/images/xxx.jpg"
}
```

**权限：** authenticated | 限制：5MB, jpg/png

---

## 3. 系统管理模块 — sys

### 3.1 用户管理

#### GET /api/sys/users — 分页查询用户列表

| 参数 | 类型 | 必填 | 说明 |
|------|:----:|:----:|------|
| page | Integer | ❌ | 页码 |
| size | Integer | ❌ | 每页条数 |
| roleId | Integer | ❌ | 角色筛选 |
| username | String | ❌ | 用户名模糊搜索 |
| realName | String | ❌ | 姓名模糊搜索 |
| classId | Integer | ❌ | 班级筛选 |

**响应体：** `PageResult<SysUser>`（记录中不含 password 字段）

**权限：** admin

---

#### POST /api/sys/users — 新增用户

**请求体：**

```json
{
  "username": "s281",
  "password": "123456",
  "realName": "李四",
  "gender": "男",
  "roleId": 1,
  "departmentId": 1,
  "majorId": 1,
  "gradeId": 1,
  "classId": 1,
  "phone": "13800138000",
  "email": "s281@campus.edu"
}
```

**权限：** admin

---

#### GET /api/sys/users/{id} — 获取用户详情

**权限：** admin

---

#### PUT /api/sys/users/{id} — 修改用户

**权限：** admin

---

#### DELETE /api/sys/users/{id} — 删除用户

**权限：** admin

---

#### PUT /api/sys/users/{id}/status — 启禁用用户

**请求体：**

```json
{ "status": 0 }
```

**权限：** admin

---

### 3.2 部门管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/sys/departments | 部门列表（分页） | admin |
| GET | /api/sys/departments/all | 所有部门（下拉框用） | authenticated |
| GET | /api/sys/departments/{id} | 部门详情 | admin |
| POST | /api/sys/departments | 新增部门 | admin |
| PUT | /api/sys/departments/{id} | 修改部门 | admin |
| DELETE | /api/sys/departments/{id} | 删除部门（有专业引用时禁止） | admin |

### 3.3 专业管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/sys/majors | 专业列表（分页） | admin |
| GET | /api/sys/majors/all | 所有专业（支持 ?deptId= 过滤） | authenticated |
| GET | /api/sys/majors/{id} | 专业详情 | admin |
| POST | /api/sys/majors | 新增专业 | admin |
| PUT | /api/sys/majors/{id} | 修改专业 | admin |
| DELETE | /api/sys/majors/{id} | 删除专业 | admin |

### 3.4 年级管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/sys/grades | 年级列表（分页） | admin |
| GET | /api/sys/grades/all | 所有年级 | authenticated |
| GET | /api/sys/grades/{id} | 年级详情 | admin |
| POST | /api/sys/grades | 新增年级 | admin |
| PUT | /api/sys/grades/{id} | 修改年级 | admin |
| DELETE | /api/sys/grades/{id} | 删除年级 | admin |

### 3.5 班级管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/sys/classes | 班级列表（分页，支持 ?departmentId=&majorId= 过滤） | admin/counselor |
| GET | /api/sys/classes/all | 所有班级（下拉框用） | authenticated |
| GET | /api/sys/classes/{id} | 班级详情 | admin/counselor |
| POST | /api/sys/classes | 新增班级 | admin/counselor |
| PUT | /api/sys/classes/{id} | 修改班级 | admin/counselor |
| DELETE | /api/sys/classes/{id} | 删除班级 | admin/counselor |

---

## 4. 教务管理模块 — edu

### 4.1 课程管理

#### GET /api/edu/courses — 课程列表（分页）

| 参数 | 类型 | 必填 | 说明 |
|------|:----:|:----:|------|
| page | Integer | ❌ | 页码 |
| size | Integer | ❌ | 每页条数 |
| courseName | String | ❌ | 课程名模糊搜索 |
| status | Integer | ❌ | 课程状态过滤 |
| teacherId | Long | ❌ | 教师 ID 过滤 |
| semester | String | ❌ | 学期过滤 |

**响应体：** `PageResult<{ id, courseCode, courseName, teacherName, credit, hours, classroom, semester, capacity, enrolled, status, courseType }>`

**权限：** authenticated

---

#### GET /api/edu/courses/available — 可选课程列表（学生选课用）

| 参数 | 说明 |
|------|------|
| page / size | 分页 |
| courseName | 可选，模糊搜索 |

**响应体：** 过滤后的可选课程列表（排除已选/班级不匹配的课程）

**权限：** student（需从 Token 解析 studentId）

---

#### GET /api/edu/courses/teacher — 教师所授课程

| 参数 | 说明 |
|------|------|
| semester | 学期，可选 |

**权限：** authenticated（需从 Token 解析 teacherId）

---

#### GET /api/edu/courses/{id} — 课程详情

**权限：** authenticated

---

#### POST /api/edu/courses — 新增课程

```json
{
  "courseCode": "CS101",
  "courseName": "数据结构",
  "teacherId": 10,
  "credit": 4,
  "hours": 64,
  "courseType": "required",
  "capacity": 70,
  "minStudents": 10,
  "description": "计算机专业核心课",
  "classroom": "教学楼301",
  "schedule": [{"day": 1, "timeSlot": 1, "weeks": "all"}],
  "startWeek": 1,
  "endWeek": 16
}
```

**权限：** admin/teacher

---

#### PUT /api/edu/courses/{id} — 编辑课程

**权限：** admin/teacher

---

#### DELETE /api/edu/courses/{id} — 删除课程

**权限：** admin

---

### 4.2 排课管理

#### POST /api/edu/courses/{id}/schedule — 新增排课

**请求体：**

```json
{
  "day": 1,
  "timeSlot": 1,
  "weeks": "all",
  "startWeek": 1,
  "endWeek": 16,
  "classroom": "教学楼301"
}
```

**冲突检测：** 服务端自动检测教室/教师/班级冲突

**权限：** admin

---

#### PUT /api/edu/courses/{id}/schedule/{scheduleIndex} — 修改排课

| 参数 | 说明 |
|------|------|
| scheduleIndex | schedule JSON 数组中的索引（从 0 开始） |

**权限：** admin

---

#### DELETE /api/edu/courses/{id}/schedule/{scheduleIndex} — 删除排课

**权限：** admin

---

#### GET /api/edu/schedule — 个人课表

| 参数 | 说明 |
|------|------|
| week | 周次（可选，默认当前周） |

**权限：** authenticated

---

#### GET /api/edu/schedule/class/{classId} — 按班级查看课表

**权限：** authenticated

---

#### GET /api/edu/schedule/teacher/{teacherId} — 按教师查看课表

**权限：** authenticated

---

#### GET /api/edu/schedule/room/{classroom} — 按教室查看课表

**权限：** authenticated

---

### 4.3 选课管理

#### GET /api/edu/selections — 我的选课列表

| 参数 | 说明 |
|------|------|
| semester | 可选，学期过滤 |

**权限：** student

---

#### POST /api/edu/selections — 选课

```json
{ "courseId": 1 }
```

**权限：** student | **冲突检测：** 已选/容量满/班级不匹配

---

#### DELETE /api/edu/selections/{id} — 退选

**权限：** student（仅退自己的选修课）

---

#### GET /api/edu/selections/course/{courseId} — 查看课程选课学生

**权限：** admin/teacher

---

#### GET /api/edu/courses/available — 可选课程列表

**权限：** student

---

#### POST /api/edu/courses/{id}/confirm-opening — 确认开课

**权限：** admin

---

#### POST /api/edu/courses/{id}/cancel-opening — 取消开课

**请求体：** `{ "reason": "选课人数不足" }`

**权限：** admin

---

#### POST /api/edu/courses/{id}/assign-classes — 分配教学班级

```json
{
  "classIds": [1, 2, 3],
  "isRequired": true
}
```

**权限：** admin

---

#### GET /api/edu/courses/{id}/classes — 查看课程分配的班级

**权限：** admin/teacher

---

#### PUT /api/edu/courses/{id}/classes — 更新课程班级分配

**权限：** admin

---

#### POST /api/edu/courses/import — CSV 导入课程

**请求格式：** `multipart/form-data`（字段名：`file`）

**CSV 模板：** `courseCode, courseName, teacherUsername, credit, hours, courseType`

**权限：** admin

---

### 4.4 成绩管理

#### GET /api/edu/grades — 成绩列表（学生个人）

| 参数 | 说明 |
|------|------|
| semester | 可选，学期过滤 |

**权限：** student

---

#### GET /api/edu/grades/transcript — 成绩单（含 GPA）

**权限：** student

---

#### GET /api/edu/grades/course/{courseId} — 课程成绩列表

**权限：** admin/teacher

---

#### GET /api/edu/grades/course/{courseId}/all — 课程全部学生成绩（含未录入占位）

**权限：** teacher（成绩录入页用）

---

#### GET /api/edu/grades/course/{courseId}/statistics — 课程成绩统计

**响应体：**

```json
{
  "data": {
    "avgScore": 82.5,
    "maxScore": 98,
    "minScore": 45,
    "medianScore": 84,
    "passRate": 92.3,
    "distribution": {
      "excellent": 15,
      "good": 30,
      "medium": 20,
      "pass": 10,
      "fail": 5
    }
  }
}
```

**权限：** admin/teacher

---

#### GET /api/edu/grades/teacher/status — 教师成绩提交状态

**响应体：**

```json
{
  "data": [
    { "courseId": 1, "courseName": "数据结构", "totalStudents": 70, "gradedStudents": 50 }
  ]
}
```

**权限：** teacher

---

#### POST /api/edu/grades — 录入成绩

```json
{
  "courseId": 1,
  "grades": [
    { "studentId": 3, "score": 85, "gradeType": "numeric" },
    { "studentId": 4, "score": 92, "gradeType": "numeric" }
  ]
}
```

**权限：** teacher

---

#### PUT /api/edu/grades/{id} — 修改成绩

```json
{
  "score": 88,
  "gradeType": "numeric"
}
```

**权限：** teacher

---

#### DELETE /api/edu/grades/{id} — 删除成绩

**权限：** teacher

---

### 4.5 学期管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/edu/semesters | 学期列表（支持 ?status=active 过滤） | authenticated |
| GET | /api/edu/semesters/{id} | 学期详情 | authenticated |
| POST | /api/edu/semesters | 新增学期 | admin |
| PUT | /api/edu/semesters/{id} | 修改学期 | admin |
| DELETE | /api/edu/semesters/{id} | 删除学期 | admin |

### 4.6 培养方案管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/edu/training-plans | 培养方案列表 | admin |
| GET | /api/edu/training-plans/my-plan | 学生查看自己的培养方案 | student |
| GET | /api/edu/training-plans/{id} | 方案详情（含课程项） | admin |
| POST | /api/edu/training-plans | 新增方案 | admin |
| PUT | /api/edu/training-plans/{id} | 修改方案 | admin |
| DELETE | /api/edu/training-plans/{id} | 删除方案 | admin |
| GET | /api/edu/training-plans/{id}/items | 获取方案课程项（支持 ?semesterNumber= 过滤） | admin |
| POST | /api/edu/training-plans/{id}/items | 新增课程项 | admin |
| PUT | /api/edu/training-plans/items/{itemId} | 修改课程项 | admin |
| DELETE | /api/edu/training-plans/items/{itemId} | 删除课程项 | admin |
| POST | /api/edu/training-plans/{id}/generate/{semesterNumber} | 自动开课 | admin |

---

## 5. 行政服务模块 — admin

### 5.1 通知管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/admin/notifications | 通知列表（分页，支持 ?category= 过滤） | authenticated |
| GET | /api/admin/notifications/unread-count | 未读通知计数 | authenticated |
| GET | /api/admin/notifications/{id} | 通知详情（自动标记已读） | authenticated |
| POST | /api/admin/notifications | 发布通知 | admin/teacher/counselor |
| PUT | /api/admin/notifications/{id} | 修改通知 | admin/teacher/counselor |
| DELETE | /api/admin/notifications/{id} | 删除通知 | admin/teacher/counselor |
| POST | /api/admin/notifications/{id}/read | 手动标记已读 | authenticated |

### 5.2 请假管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/admin/leaves | 请假列表（学生看自己的；审批人看待审批） | authenticated |
| GET | /api/admin/leaves/{id} | 请假详情 | authenticated |
| POST | /api/admin/leaves | 提交请假申请 | student |
| PUT | /api/admin/leaves/{id}/approve | 审批请假 | counselor/admin |
| DELETE | /api/admin/leaves/{id} | 删除请假 | authenticated（仅自己的待审批记录） |

**POST /api/admin/leaves — 提交请假申请**

```json
{
  "leaveType": "sick",
  "startTime": "2026-06-10 08:00:00",
  "endTime": "2026-06-10 17:00:00",
  "reason": "感冒发烧，需要休息一天",
  "attachment": "/uploads/images/病历.jpg"
}
```

**PUT /api/admin/leaves/{id}/approve — 审批请假**

```json
// 通过
{ "approved": true }

// 驳回
{ "approved": false, "rejectReason": "理由不充分，需要辅导员面谈" }
```

---

## 6. 社团管理模块 — club

### 6.1 社团基本管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/club/list | 社团列表（已激活） | 公开 |
| GET | /api/club/{id} | 社团详情 | 公开 |
| GET | /api/club/my | 我的社团 | authenticated |
| POST | /api/club | 创建社团 | authenticated |
| PUT | /api/club/{id} | 修改社团信息 | president |
| DELETE | /api/club/{id} | 删除社团 | admin |
| PUT | /api/club/{id}/approve | 审批社团 | admin |
| POST | /api/club/{id}/disband | 申请解散社团 | president |
| POST | /api/club/{id}/approve-disband | 确认解散 | admin |
| POST | /api/club/{id}/cancel-disband | 取消解散申请 | president |

**POST /api/club — 创建社团**

```json
{
  "name": "羽毛球社",
  "description": "欢迎热爱羽毛球的同学加入",
  "logo": "/uploads/images/logo.jpg"
}
```

### 6.2 成员管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/club/{clubId}/members | 成员列表 | 公开 |
| POST | /api/club/member/apply | 申请加入 | authenticated |
| PUT | /api/club/member/{id} | 审核入社申请（通过/拒绝） | president/admin |
| DELETE | /api/club/member/{clubId} | 退出社团 | authenticated |
| DELETE | /api/club/{clubId}/members/{memberId} | 移除成员 | president |
| PUT | /api/club/{clubId}/members/{memberId}/role | 设置角色（vice_president） | president |
| POST | /api/club/{clubId}/transfer/{targetUserId} | 转让社长 | president |

### 6.3 社团活动

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/club/activity | 社团活动列表 | authenticated |
| POST | /api/club/activity | 发布社团活动 | president |

### 6.4 场地管理

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/club/venue | 场地列表 | authenticated |
| POST | /api/club/venue/add | 新增场地 | admin |
| PUT | /api/club/venue/{id} | 修改场地 | admin |
| DELETE | /api/club/venue/{id} | 删除场地 | admin |

### 6.5 场地预约

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/club/venue/booking | 预约列表（支持 ?status= 过滤） | authenticated |
| POST | /api/club/venue/booking | 提交预约申请 | authenticated |
| PUT | /api/club/venue/booking/{id} | 审批预约 | admin |

**POST /api/club/venue/booking**

```json
{
  "venueId": 1,
  "title": "羽毛球社招新宣讲",
  "purpose": "社团招新宣讲会",
  "startTime": "2026-06-15 14:00:00",
  "endTime": "2026-06-15 17:00:00"
}
```

---

## 7. 校园活动模块 — activity

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/activity/public | 活动广场列表（已审批的活动） | authenticated |
| GET | /api/activity/{id} | 活动详情 | authenticated |
| POST | /api/activity | 发布活动 | authenticated |
| PUT | /api/activity/{id} | 修改活动 | creator |
| DELETE | /api/activity/{id} | 撤回活动（仅待审批状态） | creator |
| PUT | /api/activity/{id}/approve | 审批活动 | admin |
| PUT | /api/activity/{id}/confirm | 确认活动开始 | creator |
| PUT | /api/activity/{id}/finish | 结束活动 | creator |
| PUT | /api/activity/{id}/summary | 提交总结 | creator |
| GET | /api/activity/my | 我创建的活动 | authenticated |
| GET | /api/activity/my-registrations | 我报名的活动 | authenticated |
| GET | /api/activity/pending | 待审批活动列表 | admin |
| GET | /api/activity/{id}/registrations | 活动报名列表 | creator/admin |
| POST | /api/activity/{id}/register | 报名活动 | authenticated |
| DELETE | /api/activity/{id}/register | 取消报名 | authenticated |

**POST /api/activity — 发布活动**

```json
{
  "title": "校园编程马拉松",
  "description": "24小时编程挑战赛",
  "location": "计算机学院楼301",
  "startTime": "2026-06-20 09:00:00",
  "endTime": "2026-06-21 09:00:00",
  "maxParticipants": 50,
  "category": "technology",
  "coverImage": "/uploads/images/cover.jpg"
}
```

**PUT /api/activity/{id}/approve — 审批活动**

```json
{ "approved": true }
// 或
{ "approved": false, "rejectReason": "活动时间与考试周冲突" }
```

---

## 8. 校园生活模块 — life

### 8.1 校园卡充值

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/life/card-recharge | 充值记录（分页） | student |
| GET | /api/life/card-recharge/balance | 当前余额 | student |
| POST | /api/life/card-recharge | 充值 | student |

**POST /api/life/card-recharge**

```json
{ "amount": 100 }
```

**响应体：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "amount": 100,
    "balance": 650.00,
    "createTime": "2026-06-05 10:30:00"
  }
}
```

### 8.2 失物招领

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/life/lost-found | 列表（支持 ?type=0/1&keyword= 过滤） | authenticated |
| GET | /api/life/lost-found/{id} | 详情 | authenticated |
| POST | /api/life/lost-found | 发布 | authenticated |
| PUT | /api/life/lost-found/{id} | 修改 | authenticated（本人） |
| PUT | /api/life/lost-found/{id}/resolve | 标记已解决 | authenticated（本人） |
| DELETE | /api/life/lost-found/{id} | 删除 | authenticated（本人） |

**POST /api/life/lost-found**

```json
{
  "type": 0,
  "title": "黑色钱包",
  "description": "在图书馆三楼丢失黑色钱包一个",
  "location": "图书馆三楼",
  "contact": "138xxxxxxx",
  "images": "/uploads/images/wallet.jpg"
}
```

---

## 9. 消息通信模块 — message

### 9.1 私信

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/message/conversations | 会话列表 | authenticated |
| GET | /api/message/conversations/{id}/detail | 会话消息（分页） | authenticated |
| POST | /api/message/send | 发送消息 | authenticated |
| PUT | /api/message/read/{id} | 标记消息已读 | authenticated |

**POST /api/message/send**

```json
{
  "receiverId": 10,
  "content": "老师，我明天请假"
}
```

**响应体：** 若无会话则自动创建

### 9.2 公告推送

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/message/announcement | 公告推送历史 | admin/teacher/counselor |
| POST | /api/message/announcement | 发布公告推送 | admin/teacher/counselor |

**POST /api/message/announcement**

```json
{
  "title": "期末考试安排通知",
  "content": "本学期期末考试将于第18周开始...",
  "targetType": "student"
}
```

---

## 10. AI 助手模块 — ai

### 10.1 AI 对话

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| POST | /api/ai/chat | AI 对话（SSE 流式） | authenticated |
| GET | /api/ai/conversations | 会话列表 | authenticated |
| GET | /api/ai/conversations/{id} | 会话消息历史 | authenticated |
| DELETE | /api/ai/conversations/{id} | 删除会话 | authenticated |

**POST /api/ai/chat — AI 对话（SSE）**

**请求体：**

```json
{
  "conversationId": 1,
  "message": "这学期有哪些课程？"
}
```

**响应：** `text/event-stream` 格式的流式响应，每块包含部分回复内容。

**实现说明：** 前端使用 `fetch` 而非 Axios 接收 SSE 流，使用 `ReadableStream` 解析数据块。

---

## 11. 待办事项模块 — todo

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|:----:|
| GET | /api/todos | 待办列表（支持 ?status=all/pending/done&priority= 过滤） | authenticated |
| POST | /api/todos | 新增待办 | authenticated |
| PUT | /api/todos/{id} | 修改待办 | authenticated（本人） |
| DELETE | /api/todos/{id} | 删除待办 | authenticated（本人） |

**POST /api/todos**

```json
{
  "title": "完成数据结构作业",
  "priority": "high",
  "dueDate": "2026-06-10"
}
```

**PUT /api/todos/{id} — 切换完成状态**

```json
{
  "completed": true
}
```

---

## 12. 关键业务序列图

### 12.1 选课流程序列

```
学生                   前端                   后端                  数据库
  │                     │                     │                     │
  │  登录               │                     │                     │
  │────────────────────→│  POST /auth/login   │                     │
  │                     │────────────────────→│                     │
  │                     │  token + userInfo   │  校验密码生成 Token  │
  │←────────────────────│←────────────────────│                     │
  │                     │                     │                     │
  │  查看可选课程        │                     │                     │
  │────────────────────→│  GET /courses/      │                     │
  │                     │  available          │                     │
  │                     │────────────────────→│  SELECT * FROM      │
  │                     │                     │  edu_course WHERE   │
  │                     │                     │  status=1           │
  │                     │                     │────────────────────→│
  │                     │  课程列表            │←────────────────────│
  │←────────────────────│←────────────────────│                     │
  │                     │                     │                     │
  │  选课               │                     │                     │
  │────────────────────→│  POST /selections   │                     │
  │                     │────────────────────→│  ① 校验课程状态     │
  │                     │                     │  ② 校验容量         │
  │                     │                     │  ③ 校验是否已选     │
  │                     │                     │  ④ UPDATE enrolled  │
  │                     │                     │  ⑤ INSERT selection │
  │                     │                     │────────────────────→│
  │  成功/失败           │                     │  @Transactional      │
  │←────────────────────│←────────────────────│                     │
```

### 12.2 请假审批序列

```
学生             辅导员/管理员           后端                数据库
  │                   │                   │                   │
  │  提交请假          │                   │                   │
  │───────────────────│                   │                   │
  │                   │  POST /leaves     │                   │
  │                   │──────────────────→│  INSERT INTO      │
  │                   │                   │  admin_leave      │
  │                   │                   │──────────────────→│
  │←──────────────────│←──────────────────│                   │
  │                   │                   │                   │
  │                   │  审批             │                   │
  │                   │──────────────────→│  UPDATE status    │
  │                   │  PUT /leaves/     │  SET status=1/2  │
  │                   │  {id}/approve     │──────────────────→│
  │                   │←──────────────────│                   │
  │  查看结果           │                   │                   │
  │──────────────────→│  GET /leaves      │                   │
  │                   │──────────────────→│  SELECT * FROM    │
  │                   │                   │  admin_leave      │
  │                   │                   │  WHERE id=xxx     │
  │←──────────────────│←──────────────────│──────────────────→│
```

### 12.3 排课冲突检测序列

```
管理员             前端                  后端（排课 Service）     数据库
  │                   │                     │                     │
  │  填写排课信息       │                     │                     │
  │  (星期/节次/教室)  │                     │                     │
  │──────────────────→│                     │                     │
  │                   │  POST /courses/     │                     │
  │                   │  {id}/schedule      │                     │
  │                   │────────────────────→│                     │
  │                   │                     │  [阶段1] 教室冲突检测│
  │                   │                     │  SELECT COUNT(*)    │
  │                   │                     │  FROM edu_course    │
  │                   │                     │  WHERE classroom=   │
  │                   │                     │  #{classroom}       │
  │                   │                     │  AND day=#{day}     │
  │                   │                     │  AND timeSlot=...   │
  │                   │                     │────────────────────→│
  │                   │                     │←────────────────────│
  │                   │                     │                     │
  │                   │                     │  [阶段2] 教师冲突检测│
  │                   │                     │  SELECT COUNT(*)    │
  │                   │                     │  FROM edu_course    │
  │                   │                     │  WHERE teacher_id=  │
  │                   │                     │  #{teacherId}       │
  │                   │                     │  AND day=...        │
  │                   │                     │────────────────────→│
  │                   │                     │←────────────────────│
  │                   │                     │                     │
  │                   │                     │  [阶段3] 班级冲突检测│
  │                   │                     │  SELECT ... JOIN    │
  │                   │                     │  edu_course_class   │
  │                   │                     │────────────────────→│
  │                   │                     │←────────────────────│
  │                   │                     │                     │
  │                   │                     │  [决策] 无冲突？    │
  │                   │                     │  → 更新 schedule    │
  │                   │                     │  → 返回成功         │
  │                   │                     │  [有冲突]           │
  │                   │                     │  → 抛出 Business    │
  │                   │                     │    Exception        │
  │                   │                     │                     │
  │  排课成功/冲突提示  │                     │                     │
  │←──────────────────│←────────────────────│                     │
```

> **说明：** 本文档为接口设计说明书（R1c），定义了系统全部 API 端点的路径、方法、请求体、响应体和权限要求。
>
> **注意：** 本版本为"设计级"文档，标注了每个接口的目的和契约。swagger.json / OpenAPI 3.0 规格文件将在后续阶段生成，包含更完整的 schema 定义和请求/响应示例。
>
> **配套文档：**
> - R1a-系统架构设计说明书.md — 系统架构、技术选型
> - R1b-数据库设计说明书.md — 数据库表结构、索引
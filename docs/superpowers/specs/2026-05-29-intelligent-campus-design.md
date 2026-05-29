# 智能校园服务系统 — 系统需求规格说明书

> **版本**: v1.0  
> **日期**: 2026-05-29  
> **作者**: AI 辅助设计

---

## 1. 项目概述

### 1.1 项目背景

构建一个覆盖校园核心场景的综合服务系统，整合教务管理、行政服务、校园生活、社团活动、师生沟通、学生成长档案等功能，并接入 DeepSeek v4 flash 大模型提供 AI 校园咨询助手能力。系统面向学生、教师、管理员三类用户，提供一站式校园服务体验。

### 1.2 项目目标

- 提供统一的校园服务入口，覆盖教务、行政、生活、社团、沟通、成长档案六大业务域及AI咨询助手
- 接入 DeepSeek v4 flash 大模型，提供智能问答、校园导航等 AI 能力
- 支持学生、教师、管理员三种角色，实现精细化权限管控
- 采用前后端分离架构，保证系统的可维护性和可扩展性

### 1.3 项目范围

本规格说明书涵盖完整的 7 大功能模块。实现时按优先级分三期交付：

| 阶段 | 模块 | 优先级 |
|------|------|--------|
| 一期 | 教务学习 + 行政服务 + AI 校园咨询助手 | P0 核心 |
| 二期 | 校园生活 + 师生沟通 | P1 重要 |
| 三期 | 社团活动 + 学生成长档案 | P2 增强 |

---

## 2. 系统架构

### 2.1 架构模式

模块化单体架构 — 单个 Spring Boot 应用，内部按业务模块清晰分层。前端为 Vue3 SPA，按模块路由组织。

```
浏览器 (Vue3 + Element Plus SPA)
    ↓ HTTPS
Spring Security + JWT 认证层
    ↓
┌──────────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
│  教务模块  │  行政模块  │  生活模块  │  社团模块  │  沟通模块  │  成长档案  │  AI 模块  │
│ Controller│ Controller│ Controller│ Controller│ Controller│ Controller│ Controller│
│  Service  │  Service  │  Service  │  Service  │  Service  │  Service  │  Service  │
│  Mapper   │  Mapper   │  Mapper   │  Mapper   │  Mapper   │  Mapper   │ LLM API  │
└──────────┴──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘
    ↓
MyBatis-Plus + MySQL 8.0
```

### 2.2 技术栈

| 层级 | 技术 | 说明 |
|------|------|------|
| 前端框架 | Vue 3 + TypeScript + Vite | 组合式 API，响应式开发 |
| UI 组件库 | Element Plus | 中文生态完善，组件丰富 |
| 状态管理 | Pinia | Vue 3 官方推荐 |
| 路由 | Vue Router 4 | 路由守卫 + 角色权限控制 |
| HTTP 客户端 | Axios | 请求/响应拦截器封装 |
| 后端框架 | Spring Boot 3.x + Java 17 | 长期支持版本 |
| ORM | MyBatis-Plus | 简化 CRUD 操作 |
| 数据库 | MySQL 8.0 | 关系型数据库 |
| 认证 | Spring Security + JWT | 模拟 SSO + Token 鉴权 |
| API 文档 | SpringDoc OpenAPI 3 | 自动生成 Swagger UI |
| AI 模型 | DeepSeek v4 flash (deepseek-chat) | 兼容 OpenAI 协议 |
| 构建工具 | Maven (后端) / Vite (前端) | 标准构建流程 |

### 2.3 项目目录结构

```
intelligent-campus/
├── frontend/                          # Vue3 前端
│   ├── src/
│   │   ├── api/                       # 接口封装
│   │   │   ├── auth.ts
│   │   │   ├── edu.ts
│   │   │   ├── admin.ts
│   │   │   ├── life.ts
│   │   │   ├── club.ts
│   │   │   ├── message.ts
│   │   │   ├── growth.ts
│   │   │   └── ai.ts
│   │   ├── router/
│   │   │   └── index.ts              # 路由配置 + 角色守卫
│   │   ├── store/
│   │   │   ├── user.ts                # 用户状态（登录信息、角色、Token）
│   │   │   └── app.ts                 # 应用状态（侧边栏、主题等）
│   │   ├── views/
│   │   │   ├── login/                 # 登录页
│   │   │   ├── dashboard/             # 仪表盘首页
│   │   │   ├── edu/                   # 教务模块
│   │   │   │   ├── CourseList.vue
│   │   │   │   ├── CourseSelection.vue
│   │   │   │   └── GradeQuery.vue
│   │   │   ├── admin/                 # 行政模块
│   │   │   │   ├── NotificationList.vue
│   │   │   │   ├── LeaveApply.vue
│   │   │   │   └── GuideList.vue
│   │   │   ├── life/                  # 校园生活
│   │   │   │   ├── CanteenReview.vue
│   │   │   │   ├── CardRecharge.vue
│   │   │   │   └── LostFound.vue
│   │   │   ├── club/                  # 社团活动
│   │   │   │   ├── ClubList.vue
│   │   │   │   ├── ActivityList.vue
│   │   │   │   └── VenueBooking.vue
│   │   │   ├── message/               # 师生沟通
│   │   │   │   ├── ConversationList.vue
│   │   │   │   └── AnnouncementPush.vue
│   │   │   ├── growth/                # 成长档案
│   │   │   │   ├── Profile.vue
│   │   │   │   └── Attendance.vue
│   │   │   ├── ai/                    # AI 助手
│   │   │   │   └── AiChat.vue
│   │   │   └── manage/                # 管理后台
│   │   │       ├── UserManage.vue
│   │   │       ├── RoleManage.vue
│   │   │       ├── CourseManage.vue
│   │   │       └── SystemConfig.vue
│   │   ├── components/                # 公共组件
│   │   │   ├── AppHeader.vue
│   │   │   ├── AppSidebar.vue
│   │   │   └── ...
│   │   ├── layouts/
│   │   │   └── MainLayout.vue         # 主布局（侧边栏+顶栏+内容）
│   │   ├── utils/
│   │   │   └── request.ts             # Axios 封装 + Token 拦截
│   │   ├── assets/
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/
│   ├── index.html
│   ├── package.json
│   ├── vite.config.ts
│   └── tsconfig.json
├── backend/                            # Spring Boot 后端
│   ├── src/main/java/com/campus/
│   │   ├── CampusApplication.java     # 启动类
│   │   ├── config/                     # 配置类
│   │   │   ├── SecurityConfig.java
│   │   │   ├── JwtConfig.java
│   │   │   ├── WebMvcConfig.java
│   │   │   └── DeepSeekConfig.java
│   │   ├── common/                     # 公共模块
│   │   │   ├── Result.java             # 统一响应体
│   │   │   ├── PageResult.java         # 分页响应
│   │   │   ├── BusinessException.java  # 业务异常
│   │   │   └── GlobalExceptionHandler.java
│   │   ├── security/                   # 安全模块
│   │   │   ├── JwtTokenProvider.java
│   │   │   ├── JwtAuthenticationFilter.java
│   │   │   ├── UserDetailsServiceImpl.java
│   │   │   └── SsoSimulateController.java
│   │   ├── module/
│   │   │   ├── sys/                    # 系统公共模块
│   │   │   │   ├── entity/
│   │   │   │   │   ├── SysUser.java
│   │   │   │   │   └── SysRole.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   ├── edu/                    # 教务模块
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Course.java
│   │   │   │   │   ├── CourseSelection.java
│   │   │   │   │   └── Grade.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   ├── admin/                  # 行政模块
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Notification.java
│   │   │   │   │   ├── LeaveApplication.java
│   │   │   │   │   └── Guide.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   ├── life/                   # 校园生活
│   │   │   │   ├── entity/
│   │   │   │   │   ├── CanteenReview.java
│   │   │   │   │   ├── CardRecharge.java
│   │   │   │   │   └── LostFound.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   ├── club/                   # 社团活动
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Club.java
│   │   │   │   │   ├── Activity.java
│   │   │   │   │   └── VenueBooking.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   ├── message/                # 师生沟通
│   │   │   │   ├── entity/
│   │   │   │   │   ├── Conversation.java
│   │   │   │   │   └── Announcement.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   ├── growth/                 # 成长档案
│   │   │   │   ├── entity/
│   │   │   │   │   ├── StudentProfile.java
│   │   │   │   │   └── Attendance.java
│   │   │   │   ├── mapper/
│   │   │   │   ├── service/
│   │   │   │   └── controller/
│   │   │   └── ai/                     # AI 助手
│   │   │       ├── entity/
│   │   │       │   ├── AiConversation.java
│   │   │       │   └── AiMessage.java
│   │   │       ├── mapper/
│   │   │       ├── service/
│   │   │       │   └── DeepSeekService.java
│   │   │       └── controller/
│   │   │           └── AiChatController.java
│   │   └── util/
│   │       └── FileUploadUtil.java
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   ├── application-dev.yml
│   │   └── application-prod.yml
│   ├── pom.xml
│   └── sql/
│       └── init.sql                    # 数据库初始化脚本
├── docs/                               # 文档
│   └── superpowers/
│       ├── specs/
│       └── plans/
├── .gitignore
└── README.md
```

---

## 3. 用户角色与权限

### 3.1 角色定义

| 角色 | 标识 | 说明 |
|------|------|------|
| 学生 | `student` | 系统主要使用者，拥有大部分读权限和业务提交权限 |
| 教师 | `teacher` | 拥有教务管理、审批、公告发布等权限 |
| 管理员 | `admin` | 系统管理员，拥有用户管理、系统配置、数据管理等全部权限 |

### 3.2 认证流程

```
用户访问前端 → 检查 Token
  ↓ 无 Token
跳转 SSO 登录页（模拟）
  ↓ 输入学号/工号 + 密码
后端 /auth/login 验证
  ↓ 通过
返回 JWT Token（含 userId + role + 过期时间）
  ↓
前端存储 Token → 路由守卫按 role 加载菜单
```

- JWT 有效期：access_token 2小时，refresh_token 7天
- Token 存储于 localStorage，每次请求通过 Axios 拦截器自动附加
- 开发阶段 SSO 以模拟方式实现，前端展示统一登录页，后端进行模拟校验
- 生产环境可通过配置切换接入真实 SSO 平台

### 3.3 权限矩阵

| 功能 | 学生 | 教师 | 管理员 |
|------|:---:|:---:|:---:|
| 个人信息管理 | ✅ | ✅ | ✅ |
| 课程表查看 | ✅ | ✅ | ✅ |
| 选课/退课 | ✅ | — | — |
| 成绩查询 | ✅（仅自己） | 录入/查看所教班级 | 全部查看 |
| 通知公告 | 查看 | 发布/查看 | 审核/置顶/删除 |
| 请假申请 | 提交 | 审批 | 归档 |
| 办事指南 | 查看 | 查看 | 编辑发布 |
| 食堂点评 | 查看/发表 | 查看/发表 | 管理 |
| 校园卡充值 | ✅ | — | 管理 |
| 失物招领 | 发布/查看 | 发布/查看 | 管理 |
| 社团管理 | 加入/查看 | 指导 | 审核/管理 |
| 活动报名 | ✅ | 发起 | 审核 |
| 场地预约 | 申请 | 审批 | 管理 |
| 师生沟通 | ✅ | ✅ | 监督 |
| 公告推送 | 接收 | 推送 | 审核推送 |
| 成长档案 | 仅自己 | 查看所教学生 | 全部查看 |
| 签到打卡 | ✅ | 发起 | 统计 |
| AI 咨询 | ✅ | ✅ | ✅ |
| 用户管理 | — | — | ✅ |
| 系统配置 | — | — | ✅ |

---

## 4. 功能模块详述

### 4.1 教务学习模块

#### 4.1.1 课程表管理

- 学生查看个人学期课表，按周展示
- 教师查看授课班级课表
- 支持课程表导出为图片/PDF
- 课表数据由管理员导入或通过教学计划自动生成

#### 4.1.2 选课管理

- 管理员配置选课时间段、可选课程、容量上限
- 学生在选课期内浏览可选课程列表，进行选课/退课操作
- 支持先到先得模式，满员后不可再选
- 选课结果实时更新

#### 4.1.3 成绩管理

- 教师录入所教课程成绩（百分制/等级制）
- 学生查看本人各学期成绩及绩点
- 管理员查看成绩统计分析（平均分、分布图、挂科率等）
- 支持成绩单导出

### 4.2 行政服务模块

#### 4.2.1 通知公告

- 教师和管理员发布通知公告（支持富文本、附件）
- 管理员可设置置顶、紧急标记
- 学生/教师在首页 Dashboard 查看最新公告
- 支持按类型筛选（教务通知、行政通知、活动通知等）

#### 4.2.2 请假审批

- 学生提交请假申请（选择请假类型：病假/事假/公假）
- 填写请假时间范围、原因、证明材料（图片上传）
- 教师（辅导员/班主任）在线审批（同意/驳回/要求补充材料）
- 审批结果实时通知学生
- 管理员可查看归档数据

#### 4.2.3 办事指南

- 管理员编辑发布校园办事指南
- 分类管理（学籍、教务、后勤、财务、保卫等）
- 支持富文本内容、流程图、附件下载
- 学生和教师按分类浏览查看

### 4.3 校园生活模块

#### 4.3.1 食堂点评

- 展示校内各食堂/档口列表
- 学生和教师可对菜品评分（1-5星）、发表点评
- 支持按口味、价格、服务等维度评分
- 热门菜品排行展示
- 管理员管理食堂信息和点评内容

#### 4.3.2 校园卡充值

- 学生在线给校园卡充值（模拟支付流程）
- 查看充值记录和余额
- 管理员管理充值记录

#### 4.3.3 失物招领

- 用户发布失物招领信息（分类：寻物/招领）
- 包含物品描述、图片、地点、联系方式
- 按时间/分类浏览和搜索
- 标记已找到/已归还状态

### 4.4 社团活动模块

#### 4.4.1 社团管理

- 管理员创建和管理社团信息
- 学生浏览社团列表、查看社团详情
- 学生申请加入社团
- 教师可申请成为社团指导老师

#### 4.4.2 活动发布与报名

- 社团管理员或教师发布活动（时间、地点、类型、详情）
- 学生浏览活动列表、查看详情、在线报名
- 活动报名人数统计
- 活动结束后上传活动总结/照片

#### 4.4.3 场地预约

- 学生或社团申请预约校内场地（教室、报告厅、操场等）
- 选择时间、场地、用途
- 管理员或教师审批
- 场地占用情况可视化（日历视图）

### 4.5 师生沟通模块

#### 4.5.1 即时消息

- 学生与教师之间一对一私信
- 支持文本消息，可扩展图片/文件
- 消息列表和未读计数
- 管理员可监督查看（需合规声明）

#### 4.5.2 公告推送

- 管理员或教师编辑推送消息
- 选择推送范围（全体/按班级/按角色）
- 推送后在通知中心展示
- 可选邮件/短信通知（预留接口）

### 4.6 学生成长档案模块

#### 4.6.1 个人档案

- 学生基本信息、学籍信息
- 学习成绩曲线（GPA 变化图）
- 社团经历、获奖记录
- 教师评语和综合评价

#### 4.6.2 签到打卡

- 教师发起签到（课程签到、活动签到）
- 学生扫码或在线打卡
- 签到统计和缺勤记录
- 管理员查看全校签到数据

### 4.7 AI 校园咨询助手

#### 4.7.1 智能问答

- 学生、教师、管理员均可使用 AI 助手
- 支持自然语言提问：校历查询、办事流程、地点导航、政策解答等
- 基于 DeepSeek v4 flash (deepseek-chat) 大模型
- System prompt 注入学校专属知识（FAQ、办事流程、校规校纪）
- 支持流式输出（SSE），前端展示打字机效果

#### 4.7.2 多轮对话

- 单次会话内保留上下文，支持追问和澄清
- 保留最近 10 轮对话记录作为上下文
- 对话历史持久化存储

#### 4.7.3 快捷提问

- 预设高频问题入口（"今天食堂吃什么"、"校历"、"图书馆开放时间"等）
- 点击直接发送，降低使用门槛

#### 4.7.4 AI 技术方案

```
用户输入问题
    ↓
前端 POST /api/ai/chat  (Accept: text/event-stream)
    ↓
后端 DeepSeekService 构建请求：
  POST https://api.deepseek.com/chat/completions
  {
    model: "deepseek-chat",
    messages: [
      { role: "system", content: "你是XX大学校园AI助手，请根据以下知识回答问题..." },
      { role: "user", content: "用户的问题" }
    ],
    stream: true
  }
    ↓
通过 Spring SSE 逐 token 回流前端
    ↓
前端实时渲染 → 对话结束保存到数据库
```

- DeepSeek API 完全兼容 OpenAI SDK，可直接使用 openai-java 或自定义 HTTP 客户端
- 敏感问题（如涉及隐私、不当内容）通过 system prompt 约束和关键词过滤双保险
- API Key 存储于后端配置文件，不暴露给前端

---

## 5. 数据库设计

### 5.1 实体关系概要

```
sys_user (用户)
  ├── 1:N → edu_course_selection (选课记录)
  ├── 1:N → edu_grade (成绩，教师为录入方)
  ├── 1:N → admin_leave (请假申请)
  ├── 1:N → life_canteen_review (食堂点评)
  ├── 1:N → life_lost_found (失物招领)
  ├── 1:N → club_member (社团成员)
  ├── 1:N → club_activity_signup (活动报名)
  ├── 1:N → venue_booking (场地预约)
  ├── 1:N → message (消息)
  ├── 1:N → growth_attendance (签到记录)
  └── 1:N → ai_conversation (AI 对话)

sys_role (角色) ←→ N:M → sys_user (用户角色关联)

edu_course (课程) — 1:N → edu_course_selection (选课)
edu_course (课程) — 1:N → edu_grade (成绩)

club (社团) → 1:N → club_member (成员)
club (社团) → 1:N → club_activity (活动)
club_activity (活动) → 1:N → club_activity_signup (报名)

ai_conversation (对话) → 1:N → ai_message (消息明细)
```

### 5.2 核心表结构

#### sys_user (系统用户表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 用户ID |
| username | VARCHAR(50) | 学号/工号，唯一 |
| password | VARCHAR(255) | 加密密码 |
| real_name | VARCHAR(50) | 真实姓名 |
| gender | TINYINT | 性别 0:女 1:男 |
| phone | VARCHAR(20) | 手机号 |
| email | VARCHAR(100) | 邮箱 |
| avatar | VARCHAR(255) | 头像URL |
| role_id | BIGINT FK | 角色ID |
| department | VARCHAR(100) | 院系/部门 |
| class_name | VARCHAR(100) | 班级（学生） |
| status | TINYINT | 0:禁用 1:启用 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

#### sys_role (角色表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 角色ID |
| role_code | VARCHAR(20) | 角色编码 student/teacher/admin |
| role_name | VARCHAR(50) | 角色名称 |
| description | VARCHAR(200) | 角色描述 |
| create_time | DATETIME | 创建时间 |

#### edu_course (课程表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 课程ID |
| course_code | VARCHAR(20) | 课程编号，唯一 |
| course_name | VARCHAR(100) | 课程名称 |
| teacher_id | BIGINT FK | 授课教师ID |
| credit | DECIMAL(3,1) | 学分 |
| hours | INT | 学时 |
| semester | VARCHAR(20) | 学期 (如 2026-春) |
| classroom | VARCHAR(100) | 上课地点 |
| schedule | VARCHAR(200) | 上课时间 (JSON) |
| capacity | INT | 课程容量 |
| enrolled | INT | 已选人数 |
| description | TEXT | 课程简介 |
| status | TINYINT | 0:未开放 1:开放选课 2:已结束 |
| create_time | DATETIME | 创建时间 |

#### edu_course_selection (选课记录表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录ID |
| student_id | BIGINT FK | 学生ID |
| course_id | BIGINT FK | 课程ID |
| semester | VARCHAR(20) | 学期 |
| select_time | DATETIME | 选课时间 |
| status | TINYINT | 0:已退课 1:已选 |

#### edu_grade (成绩表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 成绩ID |
| student_id | BIGINT FK | 学生ID |
| course_id | BIGINT FK | 课程ID |
| teacher_id | BIGINT FK | 录入教师ID |
| score | DECIMAL(5,2) | 分数 |
| grade_type | VARCHAR(20) | 百分制/等级制 |
| semester | VARCHAR(20) | 学期 |
| remark | VARCHAR(255) | 备注 |
| create_time | DATETIME | 录入时间 |

#### admin_notification (通知公告表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 公告ID |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容（富文本） |
| category | VARCHAR(20) | 分类 |
| publisher_id | BIGINT FK | 发布人ID |
| is_top | TINYINT | 是否置顶 0:否 1:是 |
| is_urgent | TINYINT | 是否紧急 0:否 1:是 |
| view_count | INT | 浏览次数 |
| create_time | DATETIME | 发布时间 |
| update_time | DATETIME | 更新时间 |

#### admin_leave (请假申请表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 请假ID |
| student_id | BIGINT FK | 学生ID |
| leave_type | VARCHAR(20) | 病假/事假/公假 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| reason | TEXT | 请假原因 |
| attachment | VARCHAR(500) | 附件URL（JSON数组） |
| teacher_id | BIGINT FK | 审批教师ID |
| status | TINYINT | 0:待审批 1:已通过 2:已驳回 |
| reject_reason | VARCHAR(500) | 驳回原因 |
| apply_time | DATETIME | 申请时间 |
| approve_time | DATETIME | 审批时间 |

#### admin_guide (办事指南表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 指南ID |
| title | VARCHAR(200) | 标题 |
| content | TEXT | 内容（富文本） |
| category | VARCHAR(20) | 分类 |
| publisher_id | BIGINT FK | 发布人ID |
| view_count | INT | 浏览次数 |
| create_time | DATETIME | 发布时间 |
| update_time | DATETIME | 更新时间 |

#### life_canteen_review (食堂点评表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 点评ID |
| user_id | BIGINT FK | 用户ID |
| canteen_id | BIGINT FK | 食堂/档口ID |
| rating | TINYINT | 评分 1-5 |
| taste_rating | TINYINT | 口味评分 |
| price_rating | TINYINT | 价格评分 |
| service_rating | TINYINT | 服务评分 |
| content | TEXT | 点评内容 |
| images | VARCHAR(500) | 图片URL（JSON数组） |
| create_time | DATETIME | 发布时间 |

#### life_lost_found (失物招领表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录ID |
| user_id | BIGINT FK | 发布人ID |
| type | TINYINT | 0:寻物 1:招领 |
| title | VARCHAR(200) | 标题 |
| description | TEXT | 描述 |
| images | VARCHAR(500) | 图片URL（JSON数组） |
| location | VARCHAR(200) | 地点 |
| contact | VARCHAR(100) | 联系方式 |
| status | TINYINT | 0:进行中 1:已解决 |
| create_time | DATETIME | 发布时间 |

#### club (社团表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 社团ID |
| name | VARCHAR(100) | 社团名称 |
| description | TEXT | 社团简介 |
| logo | VARCHAR(255) | 社团Logo URL |
| advisor_id | BIGINT FK | 指导老师ID |
| president_id | BIGINT FK | 社长ID（学生） |
| member_count | INT | 成员数量 |
| status | TINYINT | 0:待审核 1:正常 2:已解散 |
| create_time | DATETIME | 创建时间 |

#### club_member (社团成员表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录ID |
| club_id | BIGINT FK | 社团ID |
| user_id | BIGINT FK | 用户ID |
| role | VARCHAR(20) | 成员角色：社长/副社长/干事/普通成员 |
| join_time | DATETIME | 加入时间 |
| status | TINYINT | 0:待审核 1:已通过 |

#### club_activity (社团活动表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 活动ID |
| club_id | BIGINT FK | 所属社团ID |
| title | VARCHAR(200) | 活动标题 |
| description | TEXT | 活动描述 |
| cover_image | VARCHAR(255) | 封面图 |
| location | VARCHAR(200) | 活动地点 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| signup_limit | INT | 报名上限 |
| signed_count | INT | 已报名人数 |
| publisher_id | BIGINT FK | 发布人ID |
| status | TINYINT | 0:待审核 1:报名中 2:进行中 3:已结束 |
| create_time | DATETIME | 发布时间 |

#### club_activity_signup (活动报名表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录ID |
| activity_id | BIGINT FK | 活动ID |
| user_id | BIGINT FK | 报名用户ID |
| signup_time | DATETIME | 报名时间 |
| status | TINYINT | 0:已取消 1:已报名 2:已签到 |

#### venue_booking (场地预约表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 预约ID |
| user_id | BIGINT FK | 申请人ID |
| venue_name | VARCHAR(100) | 场地名称 |
| purpose | VARCHAR(200) | 用途 |
| start_time | DATETIME | 开始时间 |
| end_time | DATETIME | 结束时间 |
| approver_id | BIGINT FK | 审批人ID |
| status | TINYINT | 0:待审批 1:已通过 2:已驳回 |
| create_time | DATETIME | 申请时间 |

#### message_conversation (对话表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 对话ID |
| user1_id | BIGINT FK | 用户1 |
| user2_id | BIGINT FK | 用户2 |
| last_message | VARCHAR(500) | 最后一条消息摘要 |
| last_time | DATETIME | 最后消息时间 |
| create_time | DATETIME | 创建时间 |

#### message_detail (消息明细表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 消息ID |
| conversation_id | BIGINT FK | 对话ID |
| sender_id | BIGINT FK | 发送人ID |
| content | TEXT | 消息内容 |
| is_read | TINYINT | 是否已读 |
| create_time | DATETIME | 发送时间 |

#### message_announcement_push (公告推送表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 推送ID |
| title | VARCHAR(200) | 推送标题 |
| content | TEXT | 推送内容 |
| target_type | VARCHAR(20) | 推送范围 all/role/class |
| target_value | VARCHAR(100) | 目标值 |
| publisher_id | BIGINT FK | 发布人ID |
| send_time | DATETIME | 推送时间 |
| status | TINYINT | 0:待发送 1:已发送 2:已取消 |

#### growth_profile (成长档案表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 档案ID |
| student_id | BIGINT FK | 学生ID，唯一 |
| enrollment_year | VARCHAR(10) | 入学年份 |
| major | VARCHAR(100) | 专业 |
| awards | TEXT | 获奖记录（JSON数组） |
| club_experience | TEXT | 社团经历（JSON数组） |
| gpa_history | TEXT | GPA历史（JSON数组） |
| teacher_comment | TEXT | 教师评语 |
| update_time | DATETIME | 更新时间 |

#### growth_attendance (签到记录表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 记录ID |
| teacher_id | BIGINT FK | 发起教师ID |
| course_id | BIGINT FK | 关联课程ID |
| student_id | BIGINT FK | 签到学生ID |
| check_type | VARCHAR(20) | 课程签到/活动签到 |
| check_time | DATETIME | 签到时间 |
| status | TINYINT | 0:缺勤 1:已签到 2:迟到 3:请假 |
| remark | VARCHAR(255) | 备注 |

#### ai_conversation (AI 对话表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 对话ID |
| user_id | BIGINT FK | 用户ID |
| title | VARCHAR(200) | 对话标题（取第一条问题） |
| message_count | INT | 消息总数 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 最后消息时间 |

#### ai_message (AI 消息明细表)

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT PK | 消息ID |
| conversation_id | BIGINT FK | 对话ID |
| role | VARCHAR(20) | user/assistant |
| content | TEXT | 消息内容 |
| token_count | INT | Token 消耗数 |
| create_time | DATETIME | 消息时间 |

---

## 6. API 接口设计

### 6.1 通用规范

- 基础路径：`/api`
- 请求格式：JSON (Content-Type: application/json)
- 响应格式：统一 `Result<T>` 封装
- 分页查询参数：`page`、`size`、`sort`
- 认证方式：Header `Authorization: Bearer <token>`
- API 文档：`/swagger-ui.html`

### 6.2 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": 1717000000000
}
```

分页响应：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "page": 1,
    "size": 20,
    "pages": 5
  }
}
```

### 6.3 接口清单

#### 认证模块 `/api/auth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/auth/login` | 模拟SSO登录 | 公开 |
| POST | `/api/auth/logout` | 退出登录 | 登录用户 |
| GET | `/api/auth/me` | 获取当前用户信息 | 登录用户 |
| PUT | `/api/auth/password` | 修改密码 | 登录用户 |

#### 教务模块 `/api/edu`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/edu/courses` | 课程列表（分页） | 登录用户 |
| GET | `/api/edu/courses/{id}` | 课程详情 | 登录用户 |
| POST | `/api/edu/courses` | 添加课程 | 管理员 |
| PUT | `/api/edu/courses/{id}` | 修改课程 | 管理员 |
| DELETE | `/api/edu/courses/{id}` | 删除课程 | 管理员 |
| GET | `/api/edu/schedule` | 课表查询 | 登录用户 |
| GET | `/api/edu/selections` | 我的选课列表 | 学生 |
| POST | `/api/edu/selections` | 选课 | 学生 |
| DELETE | `/api/edu/selections/{id}` | 退课 | 学生 |
| GET | `/api/edu/grades` | 成绩列表 | 登录用户 |
| POST | `/api/edu/grades` | 录入成绩 | 教师 |
| PUT | `/api/edu/grades/{id}` | 修改成绩 | 教师 |

#### 行政模块 `/api/admin`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/admin/notifications` | 公告列表 | 登录用户 |
| GET | `/api/admin/notifications/{id}` | 公告详情 | 登录用户 |
| POST | `/api/admin/notifications` | 发布公告 | 教师/管理员 |
| PUT | `/api/admin/notifications/{id}` | 修改公告 | 发布者/管理员 |
| DELETE | `/api/admin/notifications/{id}` | 删除公告 | 管理员 |
| GET | `/api/admin/leaves` | 请假列表 | 登录用户 |
| GET | `/api/admin/leaves/{id}` | 请假详情 | 登录用户 |
| POST | `/api/admin/leaves` | 提交请假 | 学生 |
| PUT | `/api/admin/leaves/{id}/approve` | 审批请假 | 教师 |
| GET | `/api/admin/guides` | 办事指南列表 | 登录用户 |
| GET | `/api/admin/guides/{id}` | 办事指南详情 | 登录用户 |
| POST | `/api/admin/guides` | 添加指南 | 管理员 |
| PUT | `/api/admin/guides/{id}` | 修改指南 | 管理员 |
| DELETE | `/api/admin/guides/{id}` | 删除指南 | 管理员 |

#### 校园生活 `/api/life`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/life/canteen-reviews` | 食堂点评列表 | 登录用户 |
| POST | `/api/life/canteen-reviews` | 发表点评 | 登录用户 |
| GET | `/api/life/card-recharge` | 充值记录 | 学生 |
| POST | `/api/life/card-recharge` | 校园卡充值 | 学生 |
| GET | `/api/life/lost-found` | 失物招领列表 | 登录用户 |
| POST | `/api/life/lost-found` | 发布失物招领 | 登录用户 |
| PUT | `/api/life/lost-found/{id}` | 标记状态 | 发布者 |

#### 社团活动 `/api/club`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/club/list` | 社团列表 | 登录用户 |
| POST | `/api/club/list` | 创建社团 | 管理员 |
| PUT | `/api/club/list/{id}` | 修改社团信息 | 管理员/社长 |
| POST | `/api/club/members` | 申请加入社团 | 学生 |
| GET | `/api/club/activities` | 活动列表 | 登录用户 |
| POST | `/api/club/activities` | 发布活动 | 社团管理员 |
| POST | `/api/club/activities/{id}/signup` | 报名活动 | 学生 |
| GET | `/api/club/venues` | 场地列表 | 登录用户 |
| POST | `/api/club/venues/booking` | 预约场地 | 登录用户 |
| PUT | `/api/club/venues/booking/{id}` | 审批场地 | 教师/管理员 |

#### 师生沟通 `/api/message`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/message/conversations` | 对话列表 | 登录用户 |
| GET | `/api/message/conversations/{id}/detail` | 消息记录 | 对话成员 |
| POST | `/api/message/send` | 发送消息 | 登录用户 |
| PUT | `/api/message/read/{id}` | 标记已读 | 接收者 |
| POST | `/api/message/announcement` | 推送公告 | 教师/管理员 |
| GET | `/api/message/announcement` | 推送记录 | 登录用户 |

#### 成长档案 `/api/growth`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/growth/profile` | 我的档案 | 学生（自己）/教师/管理员 |
| PUT | `/api/growth/profile/{id}` | 更新档案 | 管理员 |
| POST | `/api/growth/attendance` | 发起签到 | 教师 |
| POST | `/api/growth/attendance/check` | 签到打卡 | 学生 |
| GET | `/api/growth/attendance/records` | 签到记录 | 登录用户 |
| GET | `/api/growth/attendance/stats` | 签到统计 | 教师/管理员 |

#### AI 助手 `/api/ai`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| POST | `/api/ai/chat` | 发送消息（SSE流式返回） | 登录用户 |
| GET | `/api/ai/conversations` | 历史对话列表 | 登录用户 |
| GET | `/api/ai/conversations/{id}` | 对话详情 | 对话所有者 |
| DELETE | `/api/ai/conversations/{id}` | 删除对话 | 对话所有者 |

#### 系统管理 `/api/sys`

| 方法 | 路径 | 说明 | 权限 |
|------|------|------|------|
| GET | `/api/sys/users` | 用户列表 | 管理员 |
| POST | `/api/sys/users` | 创建用户 | 管理员 |
| PUT | `/api/sys/users/{id}` | 修改用户 | 管理员 |
| PUT | `/api/sys/users/{id}/status` | 启用/禁用用户 | 管理员 |
| GET | `/api/sys/roles` | 角色列表 | 管理员 |
| PUT | `/api/sys/users/{id}/role` | 分配角色 | 管理员 |

---

## 7. 前端设计

### 7.1 布局结构

采用经典的后台管理布局：左侧可折叠侧边栏 + 顶部导航栏 + 主内容区。

```
┌──────────────────────────────────────────┐
│  顶部栏：Logo | 通知 | 用户头像(下拉菜单)    │
├──────────┬───────────────────────────────┤
│ 侧边栏    │                               │
│          │                               │
│  📊 首页   │       主内容区                 │
│  📚 教务   │     <router-view>            │
│  🏛️ 行政   │                               │
│  🍽️ 生活   │                               │
│  🎭 社团   │                               │
│  💬 消息   │                               │
│  📋 档案   │                               │
│  🤖 AI    │                               │
│          │                               │
│  ──────   │                               │
│  ⚙️ 管理   │  (仅管理员可见)                │
└──────────┴───────────────────────────────┘
```

- 侧边栏根据用户角色动态渲染菜单项
- 顶部栏显示未读消息和通知数
- AI 助手入口在侧边栏常驻，也可在右下角悬浮球快速呼出

### 7.2 路由设计

| 路由路径 | 页面 | 角色 |
|----------|------|------|
| `/login` | 登录页 | 公开 |
| `/` | 首页仪表盘 | 登录用户 |
| `/edu/courses` | 课程列表 | 登录用户 |
| `/edu/selection` | 选课页 | 学生 |
| `/edu/grades` | 成绩页 | 登录用户 |
| `/admin/notifications` | 公告列表 | 登录用户 |
| `/admin/leave` | 请假页 | 学生 |
| `/admin/leave-approval` | 请假审批 | 教师 |
| `/admin/guides` | 办事指南 | 登录用户 |
| `/life/canteen` | 食堂点评 | 登录用户 |
| `/life/card` | 校园卡 | 学生 |
| `/life/lost-found` | 失物招领 | 登录用户 |
| `/club/list` | 社团列表 | 登录用户 |
| `/club/activities` | 活动列表 | 登录用户 |
| `/club/venue` | 场地预约 | 登录用户 |
| `/message/chat` | 消息 | 登录用户 |
| `/message/announcement` | 推送 | 登录用户 |
| `/growth/profile` | 成长档案 | 学生/教师 |
| `/growth/attendance` | 签到打卡 | 登录用户 |
| `/ai/chat` | AI 助手 | 登录用户 |
| `/manage/users` | 用户管理 | 管理员 |
| `/manage/courses` | 课程管理 | 管理员 |
| `/manage/config` | 系统配置 | 管理员 |

### 7.3 状态管理

**userStore (Pinia)**：

- `token` — JWT Token
- `userInfo` — 当前用户信息（ID、姓名、角色、院系等）
- `permissions` — 权限列表
- `login()` / `logout()` / `fetchUserInfo()`

**appStore (Pinia)**：

- `sidebarCollapsed` — 侧边栏折叠状态
- `unreadCount` — 未读消息数
- `theme` — 主题设置

### 7.4 AI 助手页面设计

AI 助手页面采用类 ChatGPT 的对话式界面：

```
┌─────────────────────────────────────────┐
│  🤖 AI 校园助手          [新对话] [历史]   │
├─────────────────────────────────────────┤
│                                         │
│  ┌──────────────────────────────┐      │
│  │ 你好！我是校园AI小助手 🤖       │      │
│  │ 有什么可以帮你的吗？            │      │
│  │ 例如：                        │      │
│  │ · 校历查询                    │      │
│  │ · 图书馆开放时间               │      │
│  │ · 校园卡充值流程               │      │
│  └──────────────────────────────┘      │
│                                         │
│                ┌────────────────────┐    │
│                │ 请问明天的校历？    │    │
│                └────────────────────┘    │
│  ┌──────────────────────────────┐      │
│  │ 明天（2026年5月30日）是第15周  │      │
│  │ 星期五，正常上课。              │      │
│  │ 下周一（6月1日）为校运动会...   │      │
│  └──────────────────────────────┘      │
│                                         │
├─────────────────────────────────────────┤
│  💡 今天有什么菜  🗺️ 图书馆在哪           │
│  ┌─────────────────────────────────┐    │
│  │ 输入你的问题...              [发送]│    │
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

---

## 8. 非功能性需求

### 8.1 性能

- 页面首次加载（FCP）< 2秒
- API 响应时间 P95 < 500ms（不含大模型调用）
- AI 对话首 Token 延迟 < 3秒
- 支持 1000 并发用户

### 8.2 安全

- 所有 API 使用 HTTPS
- 密码 BCrypt 加密存储
- JWT Token 有过期和刷新机制
- SQL 注入防护（MyBatis 参数化查询）
- XSS 防护（前端输出转义）
- DeepSeek API Key 仅后端持有，不暴露前端
- AI 对话内容不包含用户敏感身份信息

### 8.3 可用性

- 全功能浏览器兼容：Chrome、Edge、Firefox 最新两个版本
- 移动端响应式适配（主要页面）
- 关键操作（删除、审批驳回）有二次确认

### 8.4 可维护性

- 代码按模块目录组织，单一职责
- 关键业务逻辑有单元测试覆盖
- Swagger API 文档自动生成
- 配置文件分离（dev / prod）

---

## 9. 开发与部署

### 9.1 开发环境

| 工具 | 版本 |
|------|------|
| JDK | 17+ |
| Node.js | 18+ |
| MySQL | 8.0+ |
| Maven | 3.8+ |
| IDE | IntelliJ IDEA / VS Code |

### 9.2 本地启动

**后端：**
```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
# 启动在 http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

**前端：**
```bash
cd frontend
npm install
npm run dev
# 启动在 http://localhost:5173
# 开发代理将 /api 请求转发到 localhost:8080
```

### 9.3 部署架构（生产环境）

```
用户 → Nginx (静态资源 + 反向代理)
         ├── / → frontend 静态文件
         └── /api → Spring Boot (localhost:8080)
                      └── MySQL
```

---

## 10. 风险与约束

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| DeepSeek API 不稳定或限流 | AI 助手不可用 | 添加超时和重试机制，降级为提示"AI 暂时不可用" |
| SSO 模拟与真实环境差异 | 上线需大量适配 | 预留 SSO 标准接口（OAuth2/OIDC），开发阶段模拟 |
| 模块过多导致首期开发周期过长 | 进度压力 | 严格按三期分阶段交付，每期独立可上线 |
| 数据库表设计不完善 | 后期重构成本高 | 核心表先行（一期模块），扩展表在二期三期补充 |

---

## 附录 A：术语说明

| 术语 | 说明 |
|------|------|
| SPA | Single Page Application，单页面应用 |
| JWT | JSON Web Token，无状态身份认证令牌 |
| SSE | Server-Sent Events，服务器推送事件（用于流式输出） |
| SSO | Single Sign-On，统一身份认证 |
| ORM | Object-Relational Mapping，对象关系映射 |
| MyBatis-Plus | MyBatis 增强工具包 |

---

## 附录 B：修订记录

| 版本 | 日期 | 修订内容 |
|------|------|----------|
| v1.0 | 2026-05-29 | 初始版本，覆盖7大模块完整需求 |

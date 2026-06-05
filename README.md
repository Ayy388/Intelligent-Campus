<div align="center">
  <h1>🏫 智能校园 Intelligent Campus</h1>
  <p>全栈校园综合管理平台 · 模块化单体后端 + Vue 3 SPA 前端</p>

  <p>
    <img src="https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot"/>
    <img src="https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white" alt="Java"/>
    <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?logo=mysql&logoColor=white" alt="MySQL"/>
    <img src="https://img.shields.io/badge/Redis-7-FF4438?logo=redis&logoColor=white" alt="Redis"/>
    <img src="https://img.shields.io/badge/Vue_3-4DD6B9?logo=vue.js&logoColor=white" alt="Vue 3"/>
    <img src="https://img.shields.io/badge/TypeScript-5-3178C6?logo=typescript&logoColor=white" alt="TypeScript"/>
    <img src="https://img.shields.io/badge/Element_Plus-409EFF?logo=element&logoColor=white" alt="Element Plus"/>
    <img src="https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white" alt="Docker"/>
  </p>
</div>

---

## 📋 功能速览

| 模块 | 核心功能 | 角色 |
|:----|---------|:----:|
| **用户认证** | JWT 登录/注册、个人信息管理 | 全部用户 |
| **教务管理** | 课程 CRUD、学期管理、培养方案生成 | 管理员、教师 |
| **选课系统** | 在线选课/退课、必修分配、冲突检测 | 学生、管理员 |
| **成绩管理** | 成绩录入/查看/统计、GPA 计算、绩点转换 | 教师、学生 |
| **排课管理** | 课表网格排课、三重冲突检测、多维课表查询 | 管理员 |
| **班级管理** | 院系/专业/班级/年级 CRUD | 管理员 |
| **用户管理** | 学生/教师/辅导员/管理员 CRUD、角色管理 | 管理员 |
| **社团管理** | 社团创建/审批、成员管理、活动组织 | 学生、管理员 |
| **活动中心** | 活动发布/报名/审批 | 教师、学生 |
| **通知公告** | 通知发布、审批管理 | 管理员、教师 |
| **请假管理** | 请假申请/审批 | 学生、辅导员 |
| **失物招领** | 失物发布/招领 | 全部用户 |
| **一卡通充值** | 线上充值 | 学生 |
| **AI 助手** | 对话式 AI 答疑 | 全部用户 |

## 🏗️ 系统架构

```
┌─────────────────────────────────────────────────────┐
│                    前端层 (Vue 3 SPA)                │
│  Vue Router  →  Pinia Store  →  Element Plus UI     │
│       ↑                          ↑                   │
│       └──────── Axios JWT ───────┘                   │
├─────────────────────────────────────────────────────┤
│                    API 网关 (Nginx)                   │
├─────────────────────────────────────────────────────┤
│                   后端层 (Spring Boot)                │
│  ┌──────┐  ┌──────┐  ┌──────┐  ┌──────┐             │
│  │ sys  │  │ edu  │  │ club │  │admin │  ... 模块    │
│  └──┬───┘  └──┬───┘  └──┬───┘  └──┬───┘             │
│     └─────────┴────┬────┴─────────┘                  │
│                     ▼                                 │
│          MyBatis-Plus ORM 层                          │
├─────────────────────────────────────────────────────┤
│               数据层 (MySQL 8 + Redis)                │
└─────────────────────────────────────────────────────┘
```

### 技术栈

| 层次 | 技术 | 版本 |
|:----|:----|:----:|
| 前端框架 | Vue 3 (Composition API + `<script setup>`) | 3.4+ |
| 类型系统 | TypeScript | 5.x |
| 构建工具 | Vite | 5.x |
| 状态管理 | Pinia | 2.x |
| 路由 | Vue Router | 4.x |
| UI 组件 | Element Plus | 2.7+ |
| CSS 框架 | Tailwind CSS | 3.x |
| HTTP 客户端 | Axios | 1.7+ |
| 后端框架 | Spring Boot | 3.2.5 |
| 语言 | Java | 17 |
| ORM | MyBatis-Plus | 3.5.6 |
| 安全 | Spring Security + JWT (jjwt 0.12.5) | — |
| 数据库 | MySQL | 8.0 |
| 缓存 | Redis（可选，有内存回退） | 7 |
| 容器化 | Docker + docker-compose | — |

### 模块依赖

```
edu ───→ sys  (课程关联教师/班级/专业)
admin ─→ sys  (通知目标角色)
club ───→ sys (社团关联创建者)
activity ─→ sys (活动关联组织者)
message ─→ sys (消息关联用户)
life ───→ sys (充值/失物关联用户)
todo ───→ sys (待办关联用户)
```

**核心约定：** 所有模块只依赖 `sys`（用户/角色/组织架构），不跨模块直接依赖。

## 🚀 快速开始

### 环境要求

- JDK 17+
- Node.js 18+
- MySQL 8.0
- Maven 3.8+

### 1. 克隆项目

```bash
git clone https://github.com/your-username/intelligent-campus.git
cd intelligent-campus
```

### 2. 创建数据库

```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS intelligent_campus DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 3. 启动后端

```bash
cd backend
# 设置 JWT 密钥（可选，dev 配置有默认值）
export JWT_SECRET=your-secret-key
# 启动（自动建表 + 种子数据）
mvn spring-boot:run
```

后端启动在 `http://localhost:8080`。首次启动自动执行建表、迁移、种子数据。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端启动在 `http://localhost:5173`，自动代理 `/api` 到后端。

### 5. Docker 一键部署

```bash
docker-compose up -d
```

启动 MySQL + Redis + 后端 + 前端。前端在 `http://localhost:80`。

### 演示账号

| 角色 | 账号 | 密码 |
|:----|:----|:----:|
| 管理员 | `admin` | `123456` |
| 教师 | `t001` ~ `t015` | `123456` |
| 学生 | `s001` ~ `s280` | `123456` |
| 辅导员 | `c001` ~ `c002` | `123456` |

## 🧪 测试

```bash
# 后端测试
cd backend
mvn test -Dspring.profiles.active=test

# 前端类型检查 + 构建
cd frontend
npm run build       # vue-tsc type-check + vite build

# 前端测试
npm run test
```

### 测试覆盖

- **后端：** 57 个单元/集成测试（JUnit 5 + Mockito + H2）
- **前端：** 14 个 Vitest 测试（Store/API/Utils/View）
- **API 文档：** Swagger UI `http://localhost:8080/swagger-ui.html`

## 📚 文档体系

| 文档 | 内容 |
|:----|:----|
| `docs/R0-需求规格说明书.md` | 功能定义、角色定义、业务流程 |
| `docs/R1a-系统架构设计说明书.md` | 架构决策、技术选型、模块依赖 |
| `docs/R1b-数据库设计说明书.md` | 34 张表定义、索引设计、SQL |
| `docs/R1c-接口设计说明书.md` | 120+ API 端点、请求/响应格式 |
| `docs/R2a-测试计划.md` | 测试策略、覆盖范围 |
| `docs/R3-实施计划.md` | 实施路线图、风险评估 |

## 🔒 安全设计

- **认证：** JWT 无状态认证
- **授权：** Spring Security RBAC（4 种角色）
- **密码：** BCrypt 加密存储
- **防超卖：** CAS 更新（`UPDATE ... WHERE enrolled < capacity`）

## 📊 主要业务流程

### 学生选课

```
学生登录 → 查看可选课程 → 选课 → CAS 容量检查 → 生成记录 → 成功
```

### 排课冲突检测

```
管理员填排课 → 教室冲突检测 → 教师冲突检测 → 班级冲突检测
                                       ↓
                                  有冲突？→ 返回冲突信息
                                       ↓
                                  无冲突 → 更新排课
```

### 请假审批

```
学生填写 → 提交 → 辅导员查看 → 审批通过/拒绝 → 通知学生
```

## 🗺️ 项目路线图

| 阶段 | 内容 | 状态 |
|:----|:----|:----:|
| Phase A | CI 配置 + 安全加固 | ✅ |
| Phase B | 测试建设 + Swagger 文档 | ✅ |
| Phase C | 技术债务修复（类型系统、异常处理） | ✅ |
| Phase D | 排课功能（冲突检测、API、课表） | ✅ |
| Phase E | 架构增强（Redis 缓存、Docker） | ✅ |
| Phase F | 交付（README、清理、验收） | ✅ |

---

<div align="center">
  <p>Built with ❤️ for campus digitalization</p>
</div>
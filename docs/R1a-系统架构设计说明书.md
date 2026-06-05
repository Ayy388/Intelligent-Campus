# 智能校园管理系统 — 系统架构设计说明书

| 文档编号 | R1a | 版本 | 1.0 |
|---------|-----|------|-----|
| 编写日期 | 2026-06-05 | 状态 | 初稿 |
| 基于需求 | R0-需求规格说明书 v2.0 |  |  |

---

## 修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| 1.0 | 2026-06-05 | 初稿 — 覆盖架构总览、技术选型、模块架构、安全设计、数据流、部署方案 | Claude |

---

## 目录

1. [系统架构总览](#1-系统架构总览)
2. [技术选型与决策](#2-技术选型与决策)
3. [前端架构](#3-前端架构)
4. [后端架构](#4-后端架构)
5. [模块划分与依赖关系](#5-模块划分与依赖关系)
6. [安全架构](#6-安全架构)
7. [关键数据流设计](#7-关键数据流设计)
8. [部署架构](#8-部署架构)

---

## 1. 系统架构总览

### 1.1 整体架构

本系统采用**前后端分离的模块化单体架构**，分为三个层次：

```
┌─────────────────────────────────────────────────────────┐
│                    表 现 层（前端）                        │
│  ┌───────────┐  ┌───────────┐  ┌───────────────────┐   │
│  │ Vue 3 SPA │  │ Element   │  │ Pinia 状态管理     │   │
│  │ 组件化页面 │  │ Plus UI库 │  │ (user + app store) │   │
│  └─────┬─────┘  └─────┬─────┘  └─────────┬─────────┘   │
│        └──────────────┴──────────────────┘              │
│                        │  Axios HTTP                     │
│                   (localhost:5173)                        │
├────────────────────────┼─────────────────────────────────┤
│                        ▼  /api/*                         │
│                   代 理 层 (Vite proxy)                   │
│              localhost:5173 → localhost:8080              │
├────────────────────────┼─────────────────────────────────┤
│                    业 务 层（后端）                        │
│  ┌─────────────────────────────────────────────────┐    │
│  │            Security 安全网关                       │    │
│  │  JwtAuthenticationFilter → SecurityConfig        │    │
│  └─────────────────────┬───────────────────────────┘    │
│                        ▼                                 │
│  ┌─────────────────────────────────────────────────┐    │
│  │           Controller 层（REST API）               │    │
│  │  sys / edu / admin / club / activity / life     │    │
│  │  message / ai / todo                            │    │
│  └─────────────────────┬───────────────────────────┘    │
│                        ▼                                 │
│  ┌─────────────────────────────────────────────────┐    │
│  │           Service 层（业务逻辑）                   │    │
│  │  接口 + 实现分离，@Service + @Transactional       │    │
│  └─────────────────────┬───────────────────────────┘    │
│                        ▼                                 │
│  ┌─────────────────────────────────────────────────┐    │
│  │           Mapper 层（数据访问）                    │    │
│  │  MyBatis-Plus BaseMapper + Lambda QueryWrapper  │    │
│  └─────────────────────┬───────────────────────────┘    │
├────────────────────────┼─────────────────────────────────┤
│                        ▼                                 │
│                  数 据 层                                │
│  ┌──────────────┐  ┌────────────────────────────────┐   │
│  │   MySQL 8     │  │  文件系统（uploads/）           │   │
│  │   intelligent_campus  │  头像/图片/附件              │   │
│  └──────────────┘  └────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

### 1.2 架构原则

| 原则 | 说明 |
|------|------|
| **前后端分离** | 前端 SPA 通过 HTTP JSON 与后端通信，互不耦合 |
| **模块化单体** | 后端按业务拆分为模块包，共享同一进程，不引入微服务 |
| **接口契约优先** | 前后端基于 JSON 接口约定开发，不依赖视图模板 |
| **安全内建于每一层** | JWT 鉴权 → Controller 路径规则 → Service 业务校验，三重防护 |
| **数据库为最终一致性保证** | 唯一约束 + 事务 + 乐观锁保障数据一致 |

---

## 2. 技术选型与决策

### 2.1 技术栈总表

| 层级 | 技术 | 版本 | 选型理由 |
|------|------|:----:|---------|
| 前端框架 | Vue 3 | ^3.4 | 组合式 API + `<script setup>` 开发效率高；响应式系统成熟 |
| 前端语言 | TypeScript | ^5.4 | 类型安全，大型项目维护性优于 JS |
| 构建工具 | Vite | ^5.2 | 开发服务器秒启，HMR 热更新，比 Webpack 快 10x+ |
| UI 组件库 | Element Plus | ^2.7 | 国内最成熟 Vue 3 组件库，表格/表单/对话框等开箱即用 |
| CSS 框架 | Tailwind CSS | ^3.4 | 原子化 CSS，无需写自定义样式文件 |
| 状态管理 | Pinia | ^2.1 | Vue 3 官方推荐，TypeScript 友好，Composition API 风格 |
| 路由 | Vue Router 4 | ^4.3 | Vue 3 官方路由，懒加载支持 |
| HTTP 客户端 | Axios | ^1.7 | 拦截器机制成熟（JWT 注入 + 错误统一处理） |
| 后端框架 | Spring Boot 3 | 3.2.5 | 成熟的 Java 企业级框架，自动配置 + 强类型 |
| ORM | MyBatis-Plus | 3.5.6 | 单表零 SQL（BaseMapper），复杂查询手写 XML |
| 安全 | Spring Security | 6.x | 与 Spring Boot 深度集成，RBAC 支持 |
| JWT | jjwt | 0.12.5 | Java JWT 标准实现，解析/验证一站式 |
| 数据库 | MySQL 8 | 8.0+ | 成熟稳定，高校场景常规数据量 |
| 缓存（待引入） | Redis | 7.x | 选课热点缓存 + 分布式锁 |
| API 文档（待引入） | Springdoc | 2.x | Spring Boot 3 原生兼容的 Swagger 实现 |

### 2.2 关键决策记录

#### D1：为什么选择模块化单体而非微服务？

| 对比维度 | 模块化单体（选择） | 微服务 |
|---------|-----------------|--------|
| 复杂度 | 低 — 单一进程，本地方法调用 | 高 — 服务发现、分布式事务、RPC |
| 运维成本 | 低 — 一个 JAR 包部署 | 高 — 多服务独立部署、监控 |
| 开发效率 | 高 — 跨模块调用零网络开销 | 低 — 接口联调、版本兼容 |
| 团队规模匹配 | 1-2 人开发，单体更高效 | 适合 5+ 人团队 |
| 扩展性 | 水平扩展整体实例 | 按服务独立扩缩 |

**结论：** 1-2 人开发的高校管理系统，微服务引入的复杂度远大于收益。保持模块化单体，但包结构按业务拆分，便于未来抽取。

#### D2：为什么选 MyBatis-Plus 而非 JPA？

| 对比维度 | MyBatis-Plus（选择） | JPA/Hibernate |
|---------|-------------------|---------------|
| SQL 控制力 | 完全可控，可优化每一条 SQL | 自动生成，复杂查询难优化 |
| 学习曲线 | 低 — 写 SQL 即可 | 高 — 需要理解 JPQL/级联/懒加载 |
| 复杂查询 | 手写 XML/注解，灵活 | JPQL/Criteria API，繁琐 |
| 代码量 | BaseMapper 覆盖单表 CRUD | 类似，仓库接口 |
| 多表关联 | 手写 SQL + ResultMap | @OneToMany 自动关联 |

**结论：** 高校管理系统有大量复杂统计查询（成绩分布、课表聚合），手写 SQL 更可控。MyBatis-Plus 同时提供单表零 SQL 能力。

#### D3：为什么选择 JWT 而非 Session？

| 对比维度 | JWT（选择） | Session |
|---------|-----------|---------|
| 无状态 | ✅ 服务器不保存会话状态 | ❌ 依赖服务端 Session 存储 |
| 扩展性 | ✅ 任意实例验证 Token | ❌ 需要 Session 共享（Redis） |
| 跨域 | ✅ 天然支持 | ❌ 需额外配置 CORS + 凭证 |
| Token 吊销 | ❌ 需黑名单 | ✅ 直接删除 Session |
| 简单性 | ✅ 前后端只需处理 Token 字符串 | ❌ Cookie + Session 配置 |

**结论：** 单体 + 前端 SPA 场景，JWT 更简洁。Token 吊销问题通过短期有效（2h）+ 前端退出清除 Token 缓解。

#### D4：为什么选 Element Plus 而非 Ant Design Vue？

| 对比维度 | Element Plus（选择） | Ant Design Vue |
|---------|-------------------|---------------|
| 文档中文 | ✅ 完善的中文文档 | ✅ 完善的中文文档 |
| 组件覆盖 | ✅ el-table/el-form/el-menu 覆盖需求 | ✅ 类似 |
| 生态成熟度 | Vue 2 时代积累，Vue 3 版稳定 | 版本迭代较慢 |
| 自定义样式 | CSS 变量体系灵活 | 样式覆盖较复杂 |

**结论：** 两者均可，Element Plus 在国内 Vue 生态中使用更广泛，社区资源更多。

---

## 3. 前端架构

### 3.1 目录结构

```
frontend/src/
├── api/              # API 请求层 — 每个模块一个文件
│   ├── auth.ts       # 登录/注册相关
│   ├── edu.ts        # 教务模块（课程/选课/成绩/学期/培养方案）
│   ├── admin.ts      # 行政模块（通知/请假）
│   ├── club.ts       # 社团模块（社团/成员/场地）
│   ├── activity.ts   # 活动模块
│   ├── life.ts       # 校园生活（充值/失物招领）
│   ├── message.ts    # 消息通信
│   ├── sys.ts        # 系统管理（用户/部门/专业/年级/班级）
│   ├── todo.ts       # 待办事项
│   └── ai.ts         # AI 助手
├── components/       # 共享组件
│   ├── AppSidebar.vue  # 侧边栏导航（角色权限控制）
│   └── AppHeader.vue   # 顶部导航（用户信息/退出）
├── layouts/
│   └── MainLayout.vue  # 主布局（侧边栏 + 头部 + 内容区）
├── router/
│   └── index.ts      # 路由配置（42+ 路由，含守卫）
├── store/
│   ├── user.ts       # 用户状态（Token/角色/信息）
│   └── app.ts        # 应用状态（侧边栏折叠）
├── utils/
│   ├── request.ts    # Axios 实例（拦截器）
│   └── labels.ts     # 枚举/状态码中文映射
├── views/            # 页面组件（按模块分目录）
│   ├── login/        # 登录页
│   ├── dashboard/    # 仪表盘
│   ├── edu/          # 教务页面（10 个）
│   ├── admin/        # 行政页面（5 个）
│   ├── sys/          # 系统数据页面（4 个）
│   ├── manage/       # 管理页面（4 个）
│   ├── club/         # 社团页面（5 个）
│   ├── activity/     # 活动页面（3 个）
│   ├── life/         # 生活页面（2 个）
│   ├── growth/       # 成长页面（已废弃，待清理）
│   ├── message/      # 消息页面（3 个）
│   ├── ai/           # AI 页面
│   ├── todo/         # 待办页面
│   └── profile/      # 个人中心
├── types/            # TypeScript 类型定义（待新建）
├── main.ts           # 入口文件
├── App.vue           # 根组件
└── style.css         # 全局样式
```

### 3.2 组件层级

```
App.vue
  └─ <router-view>（带过渡动画）
      ├─ LoginView.vue           [/login]       — 公开
      └─ MainLayout.vue          [/*]           — 需认证
           ├─ AppSidebar.vue     侧边栏导航
           ├─ AppHeader.vue      顶部栏
           └─ <router-view>      内容区（子路由）
               ├─ DashboardView.vue
               ├─ edu/CourseSelection.vue
               ├─ ...
```

### 3.3 路由守卫逻辑

```
用户访问 /xxx
    │
    ├─ 路径 = /login？
    │   ├─ 是 → 渲染登录页
    │   └─ 否 → 检查 Token
    │       ├─ Token 存在 → 渲染页面
    │       └─ Token 不存在 → 重定向到 /login?redirect=xxx
    │
    └─ 页面渲染后，侧边栏根据 userStore.role 控制菜单可见性
       （服务端同时有 SecurityConfig 做二次鉴权）
```

### 3.4 Axios 请求/响应拦截器

```
请求拦截器:
  config.headers.Authorization ← localStorage('token')

响应拦截器:
  response.data.code === 200 → 返回 response.data
  response.data.code === 401 → 清除 Token, 跳转 /login
  其他 → ElMessage.error(message)
  网络错误 → ElMessage.error('网络异常，请稍后重试')
```

---

## 4. 后端架构

### 4.1 目录结构

```
backend/src/main/java/com/campus/
├── CampusApplication.java       # 启动类
├── common/                      # 公共基础设施
│   ├── Result.java              # 统一响应封装
│   ├── PageResult.java          # 分页响应封装
│   ├── BusinessException.java   # 业务异常
│   └── GlobalExceptionHandler.java  # 全局异常处理
├── config/                      # 配置
│   ├── SecurityConfig.java      # Spring Security 规则
│   ├── CorsConfig.java          # 跨域配置
│   ├── WebMvcConfig.java        # 静态资源映射
│   ├── MybatisPlusConfig.java   # 分页插件
│   └── DataInitializer.java     # 开发环境数据初始化
├── security/                    # JWT 认证
│   ├── JwtTokenProvider.java    # Token 生成/解析/验证
│   └── JwtAuthenticationFilter.java  # Token 鉴权过滤器
└── module/                      # 业务模块
    ├── sys/                     # 系统管理
    ├── edu/                     # 教务管理
    ├── admin/                   # 行政服务
    ├── club/                    # 社团管理
    ├── activity/                # 活动中心
    ├── life/                    # 校园生活
    ├── message/                 # 消息通信
    ├── ai/                      # AI 助手
    └── todo/                    # 待办事项
```

### 4.2 分层架构（模块内部）

```
controller/     REST 接口层
    │           接收 HTTP 请求，参数校验，调用 Service
    │           返回 Result<T> / PageResult<T>
    ▼
service/       业务逻辑层
    │           接口 + impl 分离，定义业务方法
    │           @Service + @Transactional
    ▼
mapper/        数据访问层
               继承 MyBatis-Plus BaseMapper
               手写复杂 SQL 使用 XML 或 @Select/@Update
```

### 4.3 通用返回格式

```json
// 成功
{
  "code": 200,
  "message": "success",
  "data": { /* 任意类型 */ },
  "timestamp": 1717545600000
}

// 分页
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [ /* 数据行 */ ],
    "total": 280,
    "page": 1,
    "size": 10,
    "pages": 28
  },
  "timestamp": 1717545600000
}

// 错误
{
  "code": 500,
  "message": "课程容量已满",
  "data": null,
  "timestamp": 1717545600000
}
```

### 4.4 异常处理体系

```
BusinessException(code, message)  ← 业务层手动抛出
    │
    ├── 400 — 参数错误
    ├── 401 — 未授权/Token 过期
    ├── 403 — 无权限
    ├── 404 — 资源不存在
    └── 500 — 系统内部错误

GlobalExceptionHandler
    ├── BusinessException     → Result.error(code, msg)
    ├── MethodArgumentNotValidException → Result.error(400, 校验信息)
    ├── AccessDeniedException → Result.error(403, "无权限访问")
    ├── AuthenticationException → Result.error(401, "未登录或Token已过期")
    ├── MissingServletRequestParameterException → Result.error(400, "缺少参数")
    ├── HttpMessageNotReadableException → Result.error(400, "请求参数格式错误")
    └── Exception             → Result.error(500, "服务器内部错误")
```

### 4.5 配置管理

```yaml
# application.yml — 公共配置（含占位符，可被 profile 覆盖）
server.port: 8080
jwt:
  secret: ${JWT_SECRET:intelligent-campus-default-secret}
  expiration: 7200000

# application-dev.yml — 开发配置（MySQL + SQL 日志）
spring.datasource:
  url: jdbc:mysql://localhost:3306/intelligent_campus

# application-local.yml — 本地密钥覆盖（已 gitignore）
deepseek.api-key: sk-xxxx
```

---

## 5. 模块划分与依赖关系

### 5.1 模块依赖图

```
                           ┌──────────┐
                           │  common  │ ← Result/BusinessException 等
                           └─────┬────┘
                                 │ 被所有模块引用
         ┌───────┬───────┬──────┼──────┬───────┬──────┐
         ▼       ▼       ▼      ▼      ▼       ▼      ▼
       ┌───┐  ┌───┐  ┌────┐ ┌────┐ ┌────┐  ┌────┐  ┌────┐
       │sys│  │edu│  │admin│ │club│ │life│  │msg │  │todo│
       └───┘  └───┘  └────┘ └────┘ └────┘  └────┘  └────┘
         │       │       │       │
         │       │       │  ┌────┴────┐
         │       │       │  │activity │
         │       │       │  └─────────┘
         │       │       │
         │       │       └──────┐
         │       │              ▼
         │       │         ┌────────┐
         │       │         │growth  │ ← 已废弃
         │       │         └────────┘
         │       │
         │       └─────────────┐
         │                     ▼
         │               ┌─────────┐
         │               │   ai    │
         │               └─────────┘
         │
         └── 其他模块通过 SysUserService 调用用户信息
```

### 5.2 模块职责边界

| 模块 | 包名 | 核心职责 | 是否独立 | 备注 |
|------|------|---------|:--------:|------|
| common | .common | 响应封装、异常处理 | 独立 | 无业务依赖 |
| sys | .module.sys | 用户/角色/部门/专业/年级/班级 | 独立 | 被所有模块引用（查用户信息） |
| edu | .module.edu | 课程/选课/成绩/学期/培养方案 | 独立 | 引用 sys（教师/班级） |
| admin | .module.admin | 通知/请假 | 独立 | 引用 sys（用户/审批人） |
| club | .module.club | 社团/成员/场地/场地预约 | 独立 | 引用 sys（用户） |
| activity | .module.activity | 活动/报名 | 独立 | 引用 sys（用户） |
| life | .module.life | 充值/失物招领 | 独立 | 引用 sys（用户） |
| message | .module.message | 会话/消息/公告 | 独立 | 引用 sys（用户） |
| ai | .module.ai | AI 对话 | 独立 | 引用 sys（用户） |
| todo | .module.todo | 待办事项 | 独立 | 引用 sys（用户） |
| ~~growth~~ | ~~.module.growth~~ | ~~档案/签到/评价~~ | — | 已废弃 |

### 5.3 模块间调用规则

```
规则 1: 模块间只能通过 Service 接口调用，不能直接访问 Mapper
规则 2: 不允许循环依赖（A→B→A）
规则 3: sys 模块是基础，其他模块可依赖 sys，反之不可
规则 4: 跨模块数据查询通过 Service 方法返回，不落地到本地表
```

**当前跨模块查询示例：**
- `edu` 模块查询教师姓名 → 调用 `SysUserService.getById(teacherId).getRealName()`
- `admin` 模块查询学生姓名 → 调用 `SysUserService.getById(studentId).getRealName()`

---

## 6. 安全架构

### 6.1 认证流程（JWT）

```
客户端                         服务端
  │                              │
  │  1. POST /api/auth/login     │
  │     { username, password }   │
  │ ──────────────────────────→  │
  │                              │  2. 查询用户，校验密码
  │                              │  3. BCrypt.matches(password, hash)
  │                              │  4. 生成 JWT Token
  │                              │     subject=userId
  │                              │     claims={ username, role }
  │  5. { token, userId, role }  │     exp=now+2h
  │ ←──────────────────────────  │
  │                              │
  │  6. 存储 Token 到 localStorage  │
  │                              │
  │  7. GET /api/edu/courses     │
  │     Authorization: Bearer xxx│
  │ ──────────────────────────→  │
  │                              │  8. JwtAuthenticationFilter
  │                              │     解析 Token → 用户信息
  │                              │     设置 SecurityContext
  │                              │  9. SecurityConfig 检查角色
  │                              │  10. Controller 处理请求
  │  11. 200 { data: [...] }     │
  │ ←──────────────────────────  │
```

### 6.2 JWT Token 结构

```json
// Header
{ "alg": "HS256", "typ": "JWT" }

// Payload
{
  "sub": "1",               // userId
  "username": "s001",
  "role": "student",        // 角色编码
  "iat": 1717545600,        // 签发时间
  "exp": 1717552800         // 过期时间（2 小时后）
}

// 签名
HMAC-SHA256(base64UrlEncode(header) + "." + base64UrlEncode(payload), secret)
```

### 6.3 授权体系（RBAC）

```
用户 ──→ 角色 ──→ 权限

实现方式：
  1. SecurityConfig 定义路径→角色映射
  2. JwtAuthenticationFilter 将 Token 中的 role 转为 Spring Security 权限
  3. 访问路径时触发 FilterSecurityInterceptor 校验

路径规则匹配顺序（SecurityConfig）：
  permitAll → 特定角色 → 通用 authenticated → .anyRequest().authenticated()
```

**核心路径权限矩阵（简略）：**

| 路径前缀 | 角色要求 |
|---------|---------|
| /api/auth/login | 公开 |
| /api/sys/users/** | ADMIN |
| /api/sys/departments, majors, grades, classes | AUTHENTICATED（读） |
| /api/edu/courses (POST/PUT/DELETE) | ADMIN / TEACHER |
| /api/edu/selections (POST/DELETE) | AUTHENTICATED（学生自校验） |
| /api/edu/grades (POST/PUT) | TEACHER / ADMIN |
| /api/edu/semesters (POST/PUT/DELETE) | ADMIN |
| /api/admin/leaves/{id}/approve | COUNSELOR / ADMIN |
| /api/admin/notifications (POST) | ADMIN / TEACHER / COUNSELOR |
| /api/club/list (GET) | 公开 |
| /api/club/** | AUTHENTICATED |
| /api/activity/** | AUTHENTICATED |
| /api/life/card-recharge/** | STUDENT |
| /api/todos/** | AUTHENTICATED |
| /api/ai/** | AUTHENTICATED |
| /api/message/** | AUTHENTICATED |

### 6.4 安全防护措施

| 威胁 | 防护措施 |
|------|---------|
| 未授权访问 | JWT 认证 + SecurityConfig 路径保护 |
| 越权操作 | 业务层自校验（如学生只能退自己的课） |
| CSRF | 禁用 CSRF（SPA + Token 认证方式天然免疫） |
| XSS | Vue 模板默认转义，不信任用户 HTML 输入 |
| SQL 注入 | MyBatis-Plus 参数绑定（#{}），不使用拼接 SQL |
| 密码泄露 | BCrypt 加盐哈希，不存明文 |
| Token 劫持 | HTTPS 传输（生产环境），Token 有效期 2h |
| 文件上传攻击 | 限制类型为图片，限制大小，存储于应用目录外 |

---

## 7. 关键数据流设计

### 7.1 登录数据流

```
LoginView.vue
    │  调用 userStore.login(username, password)
    ▼
userStore.login()
    │  axios.post('/api/auth/login', { username, password })
    ▼
AuthController.login()
    │  @PostMapping("/api/auth/login")
    │  接收 LoginRequest，调用 SysUserService.login()
    ▼
SysUserServiceImpl.login()
    │  1. query().eq("username", username).one()
    │  2. BCryptPasswordEncoder.matches(password, user.password)
    │  3. JwtTokenProvider.generateToken(user)
    │  4. 返回 LoginResponse { token, userId, username, realName, role }
    ▼
AuthController
    │  返回 Result.ok(loginResponse)
    ▼
userStore
    │  1. 存储 token 到 localStorage
    │  2. 存储 role 到 localStorage
    │  3. 调用 fetchUserInfo()
    ▼
userStore.fetchUserInfo()
    │  axios.get('/api/auth/me')
    ▼
router.beforeEach
    │  检测到 token 存在 → 允许访问仪表盘
    ▼
DashboardView.vue 渲染
```

### 7.2 选课数据流

```
CourseSelection.vue
    │  页面加载 → 调用 getAvailableCourses()
    ▼
api/edu.ts getAvailableCourses()
    │  request.get('/edu/courses/available')
    ▼
CourseController.getAvailableCourses()
    │  获取当前登录学生的 roleId/classId
    │  调用 courseService.getAvailableCourses(studentId, classId)
    ▼
CourseServiceImpl.getAvailableCourses()
    │  1. 获取当前活动学期
    │  2. 查询所有 status=1（选课中）的课程
    │  3. 过滤：排除已选课程
    │  4. 填充 teacherName 等展示字段
    │  5. 返回课程列表
    ▼
CourseSelection.vue 渲染课程卡片
    │
    │  用户点击「选课」
    ▼
enroll(courseId)
    │  request.post('/edu/selections', { courseId })
    ▼
CourseController.enroll()
    │  调用 courseService.selectCourse(studentId, courseId)
    ▼
CourseServiceImpl.selectCourse()
    │  1. 检查课程是否存在且状态=选课中
    │  2. 检查是否已选 → 抛 BusinessException("已选过该课程")
    │  3. 检查课程容量 → UPDATE edu_course SET enrolled=enrolled+1
    │     WHERE id=#{id} AND enrolled < capacity
    │     影响行数=0 → 抛 BusinessException("课程已满")
    │  4. 检查是否向学生班级开放（如果是必修课）
    │  5. INSERT INTO edu_course_selection (studentId, courseId, ...)
    │  6. @Transactional 包装以上操作
    │  7. 返回成功
    ▼
CourseSelection.vue
    │  成功 → ElMessage.success("选课成功")
    │  更新课程卡片（enrolled+1，进度条更新）
```

### 7.3 排课冲突检测数据流

```
排课表单提交
    │  request.post('/edu/courses/{id}/schedule', scheduleData)
    ▼
CourseController.addSchedule()
    │  调用 courseService.addSchedule(courseId, scheduleData)
    ▼
CourseServiceImpl.addSchedule()
    │  1. 解析 scheduleData：{ day, timeSlot, weeks, classroom }
    │
    │  === 冲突检测阶段 ===
    │
    │  2. 教室冲突检测：
    │     SELECT COUNT(*) FROM edu_course
    │     WHERE classroom = #{classroom}
    │       AND JSON_CONTAINS(schedule, '{"day":${day},"timeSlot":${timeSlot}}')
    │       AND weeks_overlap(weeks, #{weeks}) = true
    │       AND id != #{courseId}
    │     若 > 0 → 抛 BusinessException("教室已被占用")
    │
    │  3. 教师时间冲突检测：
    │     SELECT COUNT(*) FROM edu_course
    │     WHERE teacher_id = #{teacherId}
    │       AND JSON_CONTAINS(schedule, ...) // 同上
    │       AND id != #{courseId}
    │     若 > 0 → 抛 BusinessException("教师时间冲突")
    │
    │  4. 班级时间冲突检测：
    │     SELECT c.id FROM edu_course c
    │     JOIN edu_course_class cc ON cc.course_id = c.id
    │     WHERE cc.class_id IN (该课程分配的班级列表)
    │       AND JSON_CONTAINS(c.schedule, ...)
    │       AND c.id != #{courseId}
    │     若 > 0 → 抛 BusinessException("班级时间冲突")
    │
    │  === 冲突检测通过 ===
    │
    │  5. 更新课程 schedule JSON 字段
    │     若首次排课 → 直接设置 schedule
    │     若追加排课 → 追加到已有 schedule 数组
    │
    │  6. 返回成功
    ▼
    |  前端更新课表展示
```

### 7.4 请假审批数据流

```
LeaveApply.vue
    │  学生填写表单 → 提交
    │  request.post('/admin/leaves', formData)
    ▼
AdminController.applyLeave()
    │  @PostMapping("/api/admin/leaves")
    │  调用 adminService.applyLeave(studentId, leaveData)
    ▼
AdminServiceImpl.applyLeave()
    │  1. 校验起止时间合法性
    │  2. 校验同一时段无重复待审批请假
    │  3. INSERT INTO admin_leave (studentId, type, startTime, endTime,
    │     reason, attachment, status=0)
    │  4. 返回 leaveId
    ▼
LeaveApply.vue
    │  成功 → 提示并跳转请假历史
    │
    │  ── 审批端 ──
    │
LeaveApproval.vue
    │  加载 → request.get('/admin/leaves', { status: 0 })
    ▼
AdminController.getPendingLeaves()
    │  adminService.getLeavesByStatus(0)  // 待审批
    ▼
LeaveApproval.vue 展示待审批列表
    │
    │  审批人点击「通过」
    │  request.put('/admin/leaves/{id}/approve', { approved: true })
    ▼
AdminController.approveLeave()
    │  adminService.approveLeave(id, approverId, true, null)
    │  UPDATE admin_leave SET status=1, approver_id=#{approverId}
    │  WHERE id=#{id}
    │
    │  或点击「驳回」
    │  request.put('/admin/leaves/{id}/approve', { approved: false, reason: "..." })
    │  adminService.approveLeave(id, approverId, false, reason)
    │  UPDATE admin_leave SET status=2, reject_reason=#{reason}
    │  WHERE id=#{id}
    ▼
    返回成功 → 列表更新
```

---

## 8. 部署架构

### 8.1 开发环境（当前）

```
┌─────────────────────────────────────────┐
│              开发者机器                     │
│                                           │
│  ┌──────────┐   ┌──────────────────┐      │
│  │ Node 18+ │   │  JDK 17+          │      │
│  │ Vite 5173│   │  Spring Boot 8080 │      │
│  │ 前端 SPA │   │  后端 API         │      │
│  └────┬─────┘   └────────┬─────────┘      │
│       │  /api → 8080      │               │
│       └───────────────────┘               │
│                                           │
│  ┌────────────────────────────────────┐   │
│  │  MySQL 8.0 @ localhost:3306        │   │
│  │  Database: intelligent_campus      │   │
│  └────────────────────────────────────┘   │
│                                           │
│  ┌────────────────────────────────────┐   │
│  │  uploads/ 文件存储                   │   │
│  │  ├── avatars/                       │   │
│  │  └── images/                        │   │
│  └────────────────────────────────────┘   │
└─────────────────────────────────────────┘
```

### 8.2 生产部署方案（待实现）

```
                            ┌─────────────┐
                            │  Nginx 反向代理 │
                            │  :80/:443     │
                            └──────┬───────┘
                                   │
                    ┌──────────────┴──────────────┐
                    │                              │
              ┌─────▼─────┐                 ┌─────▼─────┐
              │ 静态文件    │                 │  API 代理  │
              │ / → index  │                 │ /api/*     │
              │ .html      │                 │ → :8080    │
              └───────────┘                 └─────┬──────┘
                                                   │
                                          ┌────────▼────────┐
                                          │ Spring Boot JAR  │
                                          │ 应用实例 :8080    │
                                          └────────┬────────┘
                                                   │
                                          ┌────────▼────────┐
                                          │  MySQL 8         │
                                          │  (独立实例/容器)   │
                                          └─────────────────┘
```

### 8.3 Docker 化方案（待实现）

```yaml
# docker-compose.yml 结构
services:
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: intelligent_campus
      MYSQL_ROOT_PASSWORD: ${DB_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    ports:
      - "3306:3306"

  backend:
    build: ./backend
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/intelligent_campus
      JWT_SECRET: ${JWT_SECRET}
    depends_on:
      - mysql
    ports:
      - "8080:8080"

  frontend:
    build: ./frontend
    environment:
      VITE_API_BASE_URL: /api
    depends_on:
      - backend
    ports:
      - "80:80"
```

### 8.4 环境配置对照

| 配置项 | 开发环境 | 生产环境 |
|--------|---------|---------|
| 数据库 | localhost:3306 MySQL | 独立 MySQL 实例 |
| JWT 密钥 | 默认占位符 | 环境变量注入 |
| 文件存储 | 本地文件系统 `uploads/` | 本地文件系统或对象存储 |
| 日志级别 | DEBUG | INFO |
| MyBatis SQL 日志 | StdOutImpl（开发用） | Slf4jImpl |
| API 文档 | Swagger UI 可用 | 关闭或限制访问 |

---

> **说明：** 本文档为系统架构设计说明书（R1a），定义了系统的整体架构、技术选型、模块划分、安全设计和部署方案。
>
> **配套文档：**
> - R1b-数据库设计说明书.md — 数据库表结构、索引、数据字典
> - R1c-接口设计说明书.md — 全量 API 端点定义
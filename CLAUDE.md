# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 文档至上 — 开发前必读

> 本项目有完整的规格文档体系（`docs/` 目录），**所有开发必须以文档为准**，不得随意偏离。

### 文档层级

| 文档 | 作用 | 何时查阅 |
|------|------|---------|
| `R0-需求规格说明书.md` | 功能定义、角色定义、业务流程 | 任何新增/修改功能前，确认需求是否覆盖 |
| `R1a-系统架构设计说明书.md` | 架构决策、技术选型、模块划分 | 涉及跨模块调用、新增依赖、安全配置前 |
| `R1b-数据库设计说明书.md` | 表结构、字段定义、索引、约束 | 任何数据库相关操作（建表、改字段、写 SQL）前 |
| `R1c-接口设计说明书.md` | API 端点、请求/响应格式、权限 | 前后端联调、新增/修改接口前 |
| `R2a-测试计划.md` | 测试策略、覆盖范围、测试设计 | 编写测试前，确认测试范围和标准 |
| `R3-实施计划.md` | 实施路线图、风险应对、自测流程 | 每次开始新 Phase 前，确认当前在哪个阶段 |

### 开发前三问

```
1. 这个改动对应 R0 中的哪个功能？
   → 找不到对应功能 → 要么是 Bug，要么需求没写 → 先确认再动手
2. 这个改动的接口/数据库/架构是否符合 R1 系列的设计？
   → 不符合 → 要么改设计文档，要么调整实现 → 保持一致
3. 这个改动是否需要更新测试（R2a）？
   → 需要 → 先补测试再改代码
```

## Project Overview

智能校园 (Intelligent Campus) — a full-stack campus management platform with modular monolith backend + Vue 3 SPA frontend. Roles: admin, student, teacher, counselor.

## Tech Stack

- **Frontend**: Vue 3 (Composition API + `<script setup>`), TypeScript, Vite 5, Pinia, Vue Router 4, Element Plus, Tailwind CSS, Axios
- **Backend**: Spring Boot 3.2, Java 17, MyBatis-Plus 3.5.6, Spring Security, JWT (jjwt 0.12.5), MySQL 8 + H2
- **Build**: Maven (backend), Vite + vue-tsc (frontend)

## Workflow
-用户发出指令之后，先确认用户的需求
- 需求思路清晰之后，向用户确认开始写代码，写完之后如果用户没有后续要求，那么写完代码即可结束
## Commands

### Frontend (frontend/)

```bash
npm install       # install dependencies
npm run dev       # start dev server on :5173 (proxies /api to :8080)
npm run build     # vue-tsc type-check + vite production build
npm run preview   # preview production build
```

### Backend (backend/)

```bash
mvn spring-boot:run              # run with dev profile (MySQL on localhost:3306, port 8080)
mvn clean install -DskipTests    # package
```

## Architecture

### Backend — layered modules under `com.campus`

```
CampusApplication.java          # @SpringBootApplication, @MapperScan("com.campus.module.**.mapper")
common/                         # Result<T>, PageResult<T>, BusinessException, GlobalExceptionHandler
config/                         # SecurityConfig, CorsConfig, WebMvcConfig, MybatisPlusConfig, DataInitializer
security/                       # JwtTokenProvider, JwtAuthenticationFilter
module/
  sys/        # Users, roles, departments, majors, classes, grades — system admin CRUD
  edu/        # Courses, semesters, course selection, grades, schedule — educational management
  admin/      # Notifications, leave applications, venue booking — daily administration
  club/       # Clubs, activities, members, venues — club management
  activity/   # Activity center, approvals — campus activity hub
  life/       # Card recharge, lost & found — campus life services
  growth/     # Student profiles, check-in, evaluation — student development
  message/    # Conversations, messages, announcements — messaging system
  ai/         # AI chat (DeepSeek API integration)
  todo/       # Personal to-do items
```

Module convention: `controller/` → `service/` → `service/impl/` → `mapper/` → `entity/`.

### Frontend — Vue 3 SPA under `frontend/src/`

```
main.ts                    # App entry: createApp + Pinia + Router + Element Plus + global icons
App.vue                    # Root: router-view + token-based auth check on mount
style.css                  # Global: Tailwind directives + CSS variables
api/                       # One file per feature module (auth.ts, edu.ts, admin.ts, club.ts, sys.ts, etc.)
store/                     # Pinia stores (Composition API style)
  user.ts                  # Token, userInfo, role, login/logout/fetchUserInfo
  app.ts                   # Sidebar collapse state
router/index.ts            # Lazy-loaded routes + beforeEach auth guard (45+ routes)
layouts/MainLayout.vue     # Sidebar + Header + content area (all authenticated routes)
components/AppSidebar.vue  # Navigation menu with role-based visibility (el-menu)
components/AppHeader.vue   # Top bar with user menu
utils/request.ts           # Axios: JWT interceptor + error handling + 401 redirect
views/
  login/, dashboard/       # Login + dashboard
  edu/                     # ScheduleView, TeacherTeaching, CourseSelection, MySelectCourse, etc.
  admin/                   # NotificationList, LeaveApply, LeaveApproval, VenueBooking, etc.
  sys/                     # ClassManagement, DepartmentManagement, MajorManagement, GradeManagement
  manage/                  # UserManage, CourseManage, SemesterManage, TrainingPlanManage
  club/                    # ClubList, ClubSpace, ActivityList, ClubApproval
  activity/                # ActivityCenter, MyActivities, ActivityApproval
  growth/                  # StudentProfile, CheckIn, CheckinStatistics
  profile/                 # ProfileView
  todo/                    # TodoView
  message/                 # MessageDetail, AnnouncementPush
  ai/                      # AiChat
  life/                    # CardRecharge, LostFound
  dashboard/               # DashboardView
```

## Role System

Roles stored in `sys_role` table, referenced by `sys_user.role_id`:

| role_code | role_name | role_id |
|-----------|-----------|---------|
| student   | 学生      | 1       |
| teacher   | 教师      | 2       |
| admin     | 管理员    | 3       |
| counselor | 辅导员    | 4       |

Role-based access: Spring Security `hasRole()` in SecurityConfig + frontend `v-if="userStore.role === 'xxx'"` in AppSidebar.vue.

## API Conventions

- Base URL: `/api` (proxied from frontend :5173 to backend :8080)
- Response wrapper: `Result<T>` — `{ code: 200, message: "success", data: T, timestamp }`
- Pagination: `PageResult<T>` — `{ records: T[], total, page, size, pages }`
- Error handling: `BusinessException(code, message)` → `GlobalExceptionHandler` returns `Result.error()`
- Auth errors: code 401 redirects to `/login` in Axios interceptor
- Frontend Axios interceptor unwraps response.data, so callers get `{ code, data, message }` directly

## SecurityConfig Path Convention

All `/api/edu/courses/{id}/xxx` paths in SecurityConfig must match the Controller endpoint exactly. CourseController endpoints MUST use the `/courses/{id}/xxx` pattern (not `/{id}/xxx`), otherwise the request passes security but hits a 404. Currently fixed paths:
- `GET /courses/{id}/classes`, `PUT /courses/{id}/classes`
- `POST /courses/{id}/enroll`, `POST /courses/{id}/confirm-opening`, `POST /courses/{id}/cancel-opening`
- `POST /courses/{id}/assign-classes`

## Key Backend Patterns

### `@TableField(exist = false)` — Virtual fields

Entity fields resolved at runtime via `resolveUserExtra()`. Used for display-only fields (roleName, departmentName, className, majorName, gradeName, counselorName, counselorClasses). These are NOT persisted but serialized to JSON.

### `resolveUserExtra(SysUser u)` — Post-query enrichment

Called after fetching users to populate virtual fields by looking up related entities. Must be called explicitly after `selectPage`, `selectById`, etc. Stores in `SysUserServiceImpl` — not automatic.

### DataInitializer pattern

Class: `DataInitializer implements CommandLineRunner`, annotated with `@Profile("dev")`. Structure:
1. `CREATE TABLE IF NOT EXISTS ...` for all tables (idempotent)
2. `ALTER TABLE ... ADD COLUMN ...` migrations (each in try-catch for idempotency)
3. Seed initial users (admin, t001, s001, c001)
4. Call `seedComprehensiveData()` for test data (protected by try-catch, prints errors)

### MyBatis-Plus Pagination

Requires `MybatisPlusConfig` bean with `PaginationInnerInterceptor(DbType.MYSQL)`. Without it, `selectPage` returns ALL records without LIMIT clause. Location: `com.campus.config.MybatisPlusConfig`.

### Spring Security

Stateless JWT auth: `JwtAuthenticationFilter` extends `OncePerRequestFilter`, runs before `UsernamePasswordAuthenticationFilter`. Role codes used as Spring Security roles (prefix `ROLE_`). API paths gated in `SecurityConfig.filterChain()`.

### MySQL Schema Evolution

Tables auto-created by DataInitializer via `CREATE TABLE IF NOT EXISTS`. Column additions via `ALTER TABLE ... ADD COLUMN ...` wrapped in try-catch. FK constraints defined on CREATE TABLE. No Flyway/Liquibase.

### Course `schedule` Field Format

Stored as a JSON string in `edu_course.schedule`:

```json
[{"day":1,"timeSlot":1,"weeks":"all"},{"day":3,"timeSlot":2,"weeks":"even"}]
```
- `day`: 1-5 (周一至周五)
- `timeSlot`: 1-4 (第1-4大节)
- `weeks`: `"all"` | `"odd"` | `"even"` — 全周/单周/双周

Frontend parsing: `ScheduleView.vue` has `getCourseForSlot()` + `matchesWeek()`. Backward-compatible with old single-object format.

### `CourseSelection.selectType` — Auto vs Manual

- `"auto"` — 系统分配（培养方案生成的必修课）. Protected by `dropCourse()` which rejects dropping auto-enrolled courses.
- `"manual"` — 自主选课（在线选课页面选择的选修课）. `getMySelections()` filters by `selectType="manual"` so "我的选课" only shows manually enrolled courses.

### `edu_course_class` — Course-Class Association

Bridge table linking courses to classes. Each course can serve multiple classes:
```
edu_course_class (id, course_id, class_id, is_required)
```
- `is_required=1` for required courses (both professional and GE)
- GE/public courses link to ALL classes across majors

### Training Plan System

Tables: `edu_training_plan` (per major+grade), `edu_training_plan_item` (courses per semester).
- `generateSemester()` auto-creates `Course` + `CourseClass` + `CourseSelection` records from plan items
- High-hour courses (>=48h) get 2 time slots; low-hour (<48h) get 1
- `generated_course_id` backfilled after course generation links plan items to actual courses
- TrainingPlanServiceImpl is NOT in git (untracked file)

### Course Status & Type

**Status** (`edu_course.status`): 0=未发布, 1=选课中, 2=已确认, 3=已取消
**Type** (`edu_course.course_type`): `"required"` | `"elective"`

## Key Frontend Patterns

- All API calls in `api/*.ts` using `request.get/post/put/delete` from `@/utils/request`
- Stores use Pinia Composition API (defineStore with setup function)
- Views use `<script setup lang="ts">` with Composition API
- Element Plus icons registered globally in main.ts
- Role-based UI visibility in AppSidebar.vue via `v-if="userStore.role === 'xxx'"`
- Router lazy-loads all views: `component: () => import('@/views/xxx.vue')`

## Database

- Dev: MySQL `intelligent_campus` on localhost:3306 (root/123456)
- DataInitializer seeds test accounts with password `123456`
- Test data (when `seedComprehensiveData()` runs): 2 departments (计算机/经管), 2 majors, 4 classes (每专业2个班), 280 students, 15 teachers (t001-t015), 2 counselors, 2 admins, 15 courses (5 CS专业课 + 5 EC专业课 + 5 公共课 GE), 1 active semester (2025-2026学年第二学期)

## Config Reference

| File | Purpose |
|------|---------|
| `frontend/vite.config.ts` | Dev server port, proxy, `@` alias |
| `frontend/tsconfig.json` | `@/*` → `./src/*` path alias |
| `backend/src/main/resources/application.yml` | JWT secret/expiration, DeepSeek API key |
| `backend/src/main/resources/application-dev.yml` | MySQL datasource, MyBatis-Plus config |

---

## 自我复盘：效率陷阱与改进清单

> 这不是项目笔记，是我（Claude Code）对自己工作方式的复盘。每一条都是真实犯过的错，对应一个具体改进措施。

### 陷阱 1：Edit 工具「一步到位」的错觉

**表现：** 一次性向 Edit 传递大段 old_string，结果因为制表符/空格/注释内容差异连续失败 2-3 次才命中。

**根因：** 没有先精确确认改动的行内容。Edit 不是 diff apply，它是精确字符串匹配。

**改进措施：**
- 改文件前，先用 `Read` 精确复制目标行（含缩进）
- 大段替换拆成多次小段 Edit，每次只改 3-5 行
- 如果第一次 Edit 失败，立刻用 Read 重新读取目标区域再试，不要凭记忆拼写 old_string

### 陷阱 2：一条消息塞太多工具调用

**表现：** 一条消息里同时 Read 5 个文件 + Edit 3 个文件 + Write 2 个文件。如果中间任何一个失败，整个批次的结果难以追踪。

**根因：** 想「一次搞定」，但实际上增加了调试复杂度。

**改进措施：**
- 读文件可以批量（只读不写，没有副作用）
- **写/改操作必须分批：** 每批最多 2-3 个 Edit 或 Write
- 每批操作后，如果可能，执行验证（编译/测试）再继续下一批

### 陷阱 3：Windows 环境下默认用 Bash

**表现：** Phase A 到 Phase C，反复用 `cd backend` 然后用 Bash 运行命令，结果报 `No such file or directory`。然后改用 PowerShell 重跑，浪费时间。

**根因：** Bash 的 `cd` 在当前会话里不持久（每次是独立进程），且 Windows 路径在 Bash 下需要用 `/` 而非 `\`。

**改进措施：**
- 默认用 PowerShell（系统是 Windows 11）
- 所有命令使用绝对路径 `D:\Intelligent-Campus\backend`
- Bash 只用于 grep/find/sed 等纯文本处理，不用于运行项目命令

### 陷阱 4：测试修复的「单循环」模式

**表现：** Phase B 中跑测试 → 发现 7 个失败 → 修 1 个 → 跑全部 → 发现还有 6 个失败 → 修 1 个 → ... —— 跑了 5 轮才修完。

**根因：** 没有对所有失败做模式分析就开始了逐条修复。

**改进措施：**
- 失败分析先行：查看**所有**失败的模式归类（列缺失？类型不匹配？断言值不对？）
- 按「根因」批量修复，而不是按「文件名」逐个修
- 例如 Phase B 的 7 个失败实际只有 3 个根因（缺列、FK 顺序、Mockito 用法）—— 修复 3 处就解决了 7 个问题

### 陷阱 5：背景 Agent 的「放养」心态

**表现：** Phase B 中启动 3 个背景 agent 后，没有检查它们的依赖环境（前端 agent 需要 npm install），结果等了 15 秒发现 agent 空手退出。

**根因：** 假设 agent 能自己处理环境问题。

**改进措施：**
- 在启动 agent 之前，确保所有外部依赖已就位（npm 包已装、目录已创建）
- Agent 的 prompt 中写入验证命令，让它完成后自动验证
- 关键 agent（写代码的）不用背景模式，用前台模式可以看到实时进展

### 陷阱 6：Agent 边界重叠

**表现：** Phase B 中两个后端 agent 都修改了测试配置或冲突的文件，导致合并时出现重复或冲突。

**根因：** 没有给每个 agent 划定精确的文件边界。

**改进措施：**
- 每个 agent 的 prompt 第一行就写清文件边界：`只改 src/test/java/com/campus/module/edu/ 目录下的文件`
- 如果两个 agent 必须改同一个文件（如 pom.xml），不要并行，串行执行
- 文件边界用 `Glob` 结果确认后再分配

### 总结：一次工作的正确流程

```
1. 理解需求 → 定位文档（R0/R1c/R1b）
2. 拆任务 → 判断哪些能并行、哪些必须串行
3. 准备工作环境 → 安装依赖、建目录
4. 派发 agent → 每 agent 精确划界、写验证命令
5. 每步验证 → 编译/测试通过后再下一步
6. 出问题 → 先分析模式再批量修，不要单循环
7. 复盘 → 更新本清单
```

## Agent 工作方法论（跨阶段经验）

> 以下是从 Phase A~C 实践中总结的 agent 使用原则，**比具体技术点更重要**，直接影响交付质量和迭代速度。

### 1. 颗粒度原则：一个 agent 只做一件小事

| 粒度 | 改动范围 | 实际结果 |
|------|---------|---------|
| ❌ 粗（Phase C 类型系统） | 1 agent 改 50 个文件 | 30+ 编译错误，两轮修复 |
| ✅ 细（推荐） | 1 agent 改 5-10 个文件 | 出错范围可控，可局部回滚 |

**正确拆分方式：** 按依赖链拆成串行步骤，每步一个 agent，每步完成后验证：

```
types/ 定义  →  api/ 替换  →  store 替换  →  第 1 组 View  →  第 2 组 View  →  ...
  agent 1        agent 2        agent 3          agent 4           agent 5
      验证            验证            验证              验证              验证
```

每一步的 agent 如果出错，只回滚当前批次，不影响已完成的。链式执行总耗时 = 各步之和，但修复成本远低于一个大 agent。

### 2. 验证前置：先读源码，再写代码

每个 agent 的 prompt 必须显式要求 "先读源码再写代码"，这是**唯一能避免类型/字段不匹配的方法**：

| 场景 | 没读源码的结果 | 读了源码的结果 |
|------|---------------|---------------|
| 写测试 | Column not found（H2 表缺列） | 表结构匹配 |
| 写类型 | 前端用 `s.name` 后端字段叫 `xqqc` | 类型包含 `xqqc` + `name?` 别名 |
| 写 API 调用 | 参数名/路径错误 | 与 Controller 一致 |

### 3. 并行 vs 串行的选择标准

**并行（3+ agent 同时跑）适用的场景：**
- 独立模块的测试（edu / 其他模块 / 前端测试可同时进行）
- 无相互依赖的新功能（写不同 Controller）
- 前提：每个 agent 知道自己改哪几个文件，不重叠

**串行适用的场景（不要强行并行）：**
- 有依赖链的工作（类型定义 → API 层 → Store → View）
- 改动会引发连锁编译错误的工作
- 同一个文件的多次修改

**判断标准：** 问自己"如果 agent A 的代码编译出错，agent B 是否还能正常跑？"——不能，就别并行。

### 4. 环境依赖前置

Agent 依赖安装包、工具链时，**必须在启动 agent 前手动准备好**，否则 agent 会卡住：

- Phase B 教训：前端 agent 需要 `npm install vitest`，没权限就卡了 15 秒后空手退出
- 修复模式：`# 在 launch agent 前手动跑：npm install --save-dev vitest`

### 5. Prompt 要给出精确的「验收标准」

好 prompt 和差 prompt 的区别：

```
差：为 edu 模块写测试
好：为 CourseServiceImpl 写 10 个测试（selectCourse 4 个、dropCourse 2 个、addSchedule 4 个）
    → 测试文件放 src/test/java/.../CourseServiceImplTest.java
    → 使用 Mockito，集成测试用 @SpringBootTest + H2
    → 完成后运行 mvn test -Dtest=CourseServiceImplTest 验证
```

验收标准越具体，agent 输出的准确率越高，后续修复成本越低。

---

## 阶段经验记录

### Phase A（CI + 安全加固）
- **JWT 密钥环境变量化**：`application.yml` 使用 `${JWT_SECRET}`，同时在 `application-dev.yml` 中使用 `${JWT_SECRET:intelligent-campus-dev-secret}` 提供开发期默认值。这样 CI/生产环境通过 env var 注入，本地开发不设置也不会报错。
- **H2 测试库配置**：使用 `MODE=MySQL` 参数尽可能兼容 MySQL 方言。测试 Profile 为 `test`，后端测试时通过 `-Dspring.profiles.active=test` 激活。
- **CI 双 Job 分离**：backend（Java 17 + Maven）和 frontend（Node 20 + npm ci）独立运行，互不阻塞，各自缓存依赖加速。
- **未解决的问题**：`spring.profiles.active=dev,local` 中缺少 `application-local.yml` 文件，启动时有警告但不影响运行（预存）。

### Phase B（测试建设 + Swagger）
- **H2 兼容性陷阱**：`DataInitializer` 通过 ALTER TABLE 添加的列在 H2 测试数据库中不存在，导致集成测试报 `Column not found`。修复：在测试的 CREATE TABLE 中直接包含全量列（`min_students`, `enroll_end`, `department_id`, `major_id`, `class_id`, `counselor_id`）。
- **Service 测试常见问题**：Mockito `doNothing()` 只能用于 void 方法，MyBatis-Plus Mapper 的 `insert()` 返回 int，需用 `when().thenReturn()` 或 `doAnswer()`。Mock 不必要的调用会导致 `UnnecessaryStubbingException`，应移除或用 `lenient`。
- **集成测试 FK 删除顺序**：H2 环境中外键约束强制生效。删除数据的顺序必须从子表到父表（先删 `edu_course_selection` → `edu_course` → `sys_user` → `sys_role`），否则报 FK 冲突。
- **Swagger 集成**：使用 `springdoc-openapi-starter-webmvc-ui:2.5.0`，SecurityConfig 放行 `/swagger-ui/**`、`/v3/api-docs/**`。19 个 Controller 添加了 `@Tag` + `@Operation` 注解。
- **前端测试框架**：Vitest 4.x 配合 `@vue/test-utils` 和 `happy-dom`。`vi.mock` 工厂函数中引用外部变量需用 `vi.hoisted()` 包裹（因 mock 声明被提升到文件顶部）。
- **前端测试覆盖**：14 个测试用例覆盖 5 个文件（2 个 Store、1 个 Utils、1 个 API、1 个 View），全部通过。

### Phase C（技术债修复）
- **前端类型系统**：创建 13 个 types 文件覆盖所有模块，替换 12 个 API 文件、1 个 Store、25 个 View 中的 `any`。关键模式：`Record<string, any>` 替代裸 `any` 用于 API 参数、`??` 空值合并处理 `undefined` 到 `null` 的转换、`as any` 仅用于测试 mock。
- **Semester 类型兼容**：后端 Semester 实体使用中文拼音字段（`xn`/`xqjc`/`xqqc`），但前端多处代码用 `s.name` / `s.semester` 访问。修复：在 TypeScript 类型中添加 `name?` / `semester?` 兼容字段，而非修改 10+ 个 View。
- **动态赋值字段**：排课 `schedule` JSON 解析后 `timeSlot`/`weeks` 动态赋值给 Course 对象。修复：在类型中声明为可选字段 `timeSlot?: number` + `weeks?: string`。
- **GlobalExceptionHandler 增强**：补充 4 种异常处理器（MethodArgumentNotValidException → 400 带字段错误、AccessDeniedException → 403、MissingServletRequestParameterException → 400、HttpMessageNotReadableException → 400）。
- **`@Transactional` 补充**：`SysUserServiceImpl.updateProfile()` 和 `changePassword()` 添加 `@Transactional` 确保写操作事务安全。
- **日志配置优化**：`application-dev.yml` 中 `log-impl` 从 `StdOutImpl` 改为 `Slf4jImpl`，统一日志框架输出。
- **vue-tsc 严格模式教训**：类型系统从无到有后，发现 30+ 类型错误。典型模式：undefined 不可赋值给 number（用 `??` 兜底）、delete 操作符只可作用于 optional 属性（用 `= undefined` 替代）、catch 回调隐式 any（显式 `: any` 标注）。

### Phase D（排课功能）
- **冲突检测独立工具类**：`ScheduleConflictDetector` 设计为纯静态工具类（无 Spring 依赖），方便单元测试和复用。三重检测（教室/教师/班级）各自独立方法。
- **周次模式交叉算法**：`weekPatternOverlap("odd","even")=false` 是核心算法。`all` 与一切重叠，相同模式重叠，`odd` vs `even` 不重叠。配合 `weekRangeOverlap`（区间交集）实现完整的时间冲突检测。
- **排课 API 路径约定**：严格遵守 SecurityConfig 的 `/courses/{id}/xxx` 模式。新增路径必须在 `courses/**` 通用规则之前声明，否则被更宽泛的规则覆盖。
- **排课页面设计**：5天×4大节网格 + 课程列表双栏布局。点击网格空闲格弹出排课对话框，点击已有排课弹出编辑/删除。使用 `JSON.parse` 解析 `course.schedule` 字段。
- **Agent 粒度控制**：Phase D 拆为 5 个小 agent（每个 1-4 文件），每个完成后验证编译/类型检查再继续。从 Phase C 的 50 文件/agent 降到 1-4 文件/agent，大幅减少排查成本。

### Phase E（架构增强）
- **Redis 缓存双层降级设计**：`@Profile("redis")` 启用 Redis CacheManager，`@Profile("!redis")` 回退到 ConcurrentHashMap 内存缓存。不依赖 Redis 的 profile 也无需修改任何配置。
- **@CacheEvict 覆盖面**：课程列表 `@Cacheable("courses")` 后，需要识别所有修改课程数据的方法并添加 `@CacheEvict`。漏掉任何一个都会导致缓存数据不一致。共添加了 12 个方法的失效注解。
- **Docker 多阶段构建**：backend 分 builder/runtime 两阶段，builder 先 `dependency:go-offline` 缓存 Maven 依赖层，再 `mvn package` 编译。后续构建仅重新编译源码，大幅提速。
- **docker-compose 健康检查链**：MySQL + Redis 设 healthcheck，backend 的 `depends_on.condition: service_healthy` 确保数据库和缓存就绪后才启动后端。frontend 简单依赖 backend（纯 Nginx 反向代理）。

### Phase F（交付）
- **Growth 模块全面清理**：DataInitializer 移除 3 张 growth 表创建、2 条 ALTER TABLE、3 条 DELETE FROM、1 条 INSERT。前端删除 growth 目录（3 个 View）、types/growth.ts、路由 3 条、侧边栏子菜单。SecurityConfig 清除 3 条 growth 路径规则。
- **Agent 错误模式复盘**：记录到 CLAUDE.md 的「自我复盘」章节。最重要的一条改进：每次写操作后立即验证编译（`mvn compile -q` / `vue-tsc --noEmit`），而不是写完 50 个文件再一次性排查。
- **README 设计原则**：徽章使用 shields.io 构建（可复用）。架构图使用纯文本 ASCII 而非图片（便于版本控制 diff）。功能清单、快速开始、账号表格、测试命令均从实际项目配置提取而非臆造。

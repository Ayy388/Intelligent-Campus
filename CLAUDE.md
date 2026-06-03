# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

智能校园 (Intelligent Campus) — a full-stack campus management platform with modular monolith backend + Vue 3 SPA frontend. Roles: admin, student, teacher, counselor.

## Tech Stack

- **Frontend**: Vue 3 (Composition API + `<script setup>`), TypeScript, Vite 5, Pinia, Vue Router 4, Element Plus, Tailwind CSS, Axios
- **Backend**: Spring Boot 3.2, Java 17, MyBatis-Plus 3.5.6, Spring Security, JWT (jjwt 0.12.5), MySQL 8 + H2
- **Build**: Maven (backend), Vite + vue-tsc (frontend)

## Workflow

- 我只需写代码，不需要运行项目或执行命令。运行、构建、测试等操作由用户自行完成并反馈结果。

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

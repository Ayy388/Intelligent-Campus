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
router/index.ts            # Lazy-loaded routes + beforeEach auth guard
layouts/MainLayout.vue     # Sidebar + Header + content area (all authenticated routes)
components/AppSidebar.vue  # Navigation menu with role-based visibility (el-menu)
components/AppHeader.vue   # Top bar with user menu
utils/request.ts           # Axios: JWT interceptor + error handling + 401 redirect
views/                     # Pages by module folder: login/, dashboard/, edu/, admin/, sys/, manage/, etc.
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
- Test data (when seedComprehensiveData runs): 2 departments, 2 majors, 4 classes, 280 students, 2 teachers, 2 counselors, 4 courses

## Config Reference

| File | Purpose |
|------|---------|
| `frontend/vite.config.ts` | Dev server port, proxy, `@` alias |
| `frontend/tsconfig.json` | `@/*` → `./src/*` path alias |
| `backend/src/main/resources/application.yml` | JWT secret/expiration, DeepSeek API key |
| `backend/src/main/resources/application-dev.yml` | MySQL datasource, MyBatis-Plus config |

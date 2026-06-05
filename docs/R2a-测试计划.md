# 智能校园管理系统 — 测试计划

| 文档编号 | R2a | 版本 | 1.0 |
|---------|-----|------|-----|
| 编写日期 | 2026-06-05 | 状态 | 初稿 |
| 基于需求 | R0-需求规格说明书 v2.0 |  |  |

---

## 修订记录

| 版本 | 日期 | 修订内容 | 修订人 |
|------|------|---------|--------|
| 1.0 | 2026-06-05 | 初稿 — 测试策略、覆盖范围、测试场景、数据准备、CI 集成 | Claude |

---

## 目录

1. [测试策略](#1-测试策略)
2. [测试范围](#2-测试范围)
3. [后端测试设计](#3-后端测试设计)
4. [前端测试设计](#4-前端测试设计)
5. [测试数据准备](#5-测试数据准备)
6. [CI 集成方案](#6-ci-集成方案)
7. [验收标准](#7-验收标准)

---

## 1. 测试策略

### 1.1 测试金字塔

```
         ╱╲
        ╱  ╲          E2E 测试（手工验证为主）
       ╱    ╲         ─────────────────────
      ╱      ╲        集成测试（核心业务流程）
     ╱        ╲       ─────────────────────
    ╱──────────╲      单元测试（Service + Store）
   ╱            ╲    ─────────────────────
  ╱──────────────╲    UI 测试（关键组件 + 页面）
```

### 1.2 测试层级分配

| 层级 | 覆盖目标 | 工具 | 数量目标 | 执行频率 |
|------|---------|------|:--------:|---------|
| **单元测试（后端）** | Service 层核心业务逻辑 | JUnit 5 + Mockito | 20-30 用例 | 每次 CI |
| **单元测试（前端）** | Store、Utils、API 层 | vitest | 10-15 用例 | 每次 CI |
| **集成测试（后端）** | Controller + Service + Mapper 串联 | SpringBootTest + H2 | 10-15 用例 | 每次 CI |
| **组件测试（前端）** | 关键页面组件渲染与交互 | @vue/test-utils | 5-10 用例 | 每次 CI |
| **E2E 测试** | 手工验收为主 | 手工 | — | 每次版本发布 |

### 1.3 测试原则

| 原则 | 说明 |
|------|------|
| **核心优先** | 优先覆盖选课、排课、成绩、请假等核心业务路径，边缘功能其次 |
| **正向 + 反向** | 每个方法同时测试成功路径和失败路径（异常、边界、权限不足） |
| **数据隔离** | 测试使用独立数据库（H2 内存库），不污染开发/生产数据 |
| **可重复** | 每个测试独立，测试间无状态依赖，可任意顺序执行 |
| **快速执行** | 后端测试 < 60s，前端测试 < 30s，CI 总耗时 < 3min |

---

## 2. 测试范围

### 2.1 后端测试范围

#### 必须测试的类（P0）

| 模块 | 类 | 测试重点 |
|------|-----|---------|
| **sys** | `AuthController` | 登录成功/失败、Token 生成、密码错误 |
| | `SysUserService` | 用户创建、密码加密、用户状态管理 |
| | `JwtTokenProvider` | Token 生成/解析/过期/签名验证 |
| **edu** | `CourseServiceImpl.selectCourse()` | 选课成功、容量满、重复选课、班级不匹配 |
| | `CourseServiceImpl.dropCourse()` | 退选成功、不能退必修课、不能退已确认课程 |
| | `CourseServiceImpl.addSchedule()` | 排课成功、三项冲突检测 |
| | `GradeServiceImpl.inputGrade()` | 成绩录入、批量提交、校验分数范围 |
| | `GradeServiceImpl.getCourseStatistics()` | 统计计算正确性 |
| | `TrainingPlanServiceImpl.generateSemester()` | 自动开课逻辑 |
| **admin** | `AdminServiceImpl.applyLeave()` | 请假提交、时间校验、重复请假 |
| | `AdminServiceImpl.approveLeave()` | 审批通过、驳回 |
| **club** | `ClubServiceImpl.createClub()` | 创建社团、重复创建限制 |
| | `ClubServiceImpl.approveClub()` | 社团审批流转 |
| **message** | `MessageServiceImpl.sendMessage()` | 发送消息、自动创建会话 |
| **tommon** | `GlobalExceptionHandler` | 各类异常的响应格式 |

#### 可选测试的类（P1）

| 模块 | 类 | 说明 |
|------|-----|------|
| activity | ActivityCenterService | 活动审批流转 |
| life | LifeService | 充值余额累加、失物招领 CRUD |
| ai | DeepSeekService | 仅测消息持久化（不调外部 API） |

#### 不测试的（明确排除）

| 类 | 理由 |
|----|------|
| Controller 层简单 CRUD（除 Auth/SysUser/Club 外） | 逻辑代理给 Service，测试 Service 即可 |
| Mapper 层（MyBatis-Plus 自动实现） | 框架自带测试，且集成测试会覆盖 |
| 配置类（Config/*.java） | 纯配置，无业务逻辑 |
| DataInitializer | 仅在 dev 运行，非业务逻辑 |

### 2.2 前端测试范围

#### 必须测试的模块（P0）

| 文件 | 测试重点 |
|------|---------|
| `store/user.ts` | login/logout/fetchUserInfo、Token 持久化、角色切换 |
| `store/app.ts` | toggleSidebar |
| `utils/request.ts` | JWT 拦截器注入、401 重定向 |
| `api/edu.ts` | 选课/退选 API 调用参数验证 |
| `api/auth.ts` | 登录 API 调用格式 |

#### 可选测试（P1）

| 文件 | 说明 |
|------|------|
| `views/login/LoginView.vue` | 表单验证、登录按钮交互 |
| `views/todo/TodoView.vue` | 增删改操作、状态切换 |
| `views/edu/CourseSelection.vue` | 选课按钮交互、容量进度条 |

---

## 3. 后端测试设计

### 3.1 测试技术栈

| 工具 | 用途 |
|------|------|
| JUnit 5 | 测试框架 |
| Mockito | Mock 依赖（Service 层注入） |
| Spring Boot Test | 集成测试（加载 ApplicationContext） |
| H2 Database | 内存数据库（模拟 MySQL 方言） |
| MockMvc | Controller 层 HTTP 模拟 |
| @BeforeEach | 每次测试前初始化数据 |

### 3.2 单元测试设计（Service 层）

#### Test Case 1: CourseServiceImpl.selectCourse()

```java
@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest {

    @Mock private CourseMapper courseMapper;
    @Mock private CourseSelectionMapper selectionMapper;
    @InjectMocks private CourseServiceImpl courseService;

    @Test
    void selectCourse_ShouldSucceed_WhenValid() {
        // given: 课程状态=选课中, 容量未满, 未选过
        when(courseMapper.selectById(1L)).thenReturn(validCourse());
        when(selectionMapper.selectCount(any())).thenReturn(0L);
        when(courseMapper.updateEnrolled(any())).thenReturn(1);

        // when
        Result result = courseService.selectCourse(3L, 1L);

        // then
        assertTrue(result.isSuccess());
    }

    @Test
    void selectCourse_ShouldThrow_WhenCourseFull() {
        // given: 课程已满
        Course fullCourse = validCourse();
        fullCourse.setEnrolled(fullCourse.getCapacity());
        when(courseMapper.selectById(1L)).thenReturn(fullCourse);

        // then
        assertThrows(BusinessException.class,
            () -> courseService.selectCourse(3L, 1L));
    }

    @Test
    void selectCourse_ShouldThrow_WhenAlreadySelected() {
        // given: 已选过
        when(courseMapper.selectById(1L)).thenReturn(validCourse());
        when(selectionMapper.selectCount(any())).thenReturn(1L);

        // then
        assertThrows(BusinessException.class,
            () -> courseService.selectCourse(3L, 1L));
    }

    @Test
    void selectCourse_ShouldThrow_WhenCourseNotOpen() {
        // given: 课程未发布
        Course draftCourse = validCourse();
        draftCourse.setStatus(0);
        when(courseMapper.selectById(1L)).thenReturn(draftCourse);

        // then
        assertThrows(BusinessException.class,
            () -> courseService.selectCourse(3L, 1L));
    }
}
```

#### Test Case 2: CourseServiceImpl.dropCourse()

```java
@Test
void dropCourse_ShouldSucceed_WhenManualSelection() {
    // given: 手动选修课
    CourseSelection cs = new CourseSelection();
    cs.setSelectType("manual");
    cs.setCourseId(1L);
    when(selectionMapper.selectById(1L)).thenReturn(cs);

    // when
    courseService.dropCourse(3L, 1L);

    // then: 删除选课记录
    verify(selectionMapper).deleteById(1L);
}

@Test
void dropCourse_ShouldThrow_WhenAutoSelection() {
    // given: 系统自动分配的必修课
    CourseSelection cs = new CourseSelection();
    cs.setSelectType("auto");
    when(selectionMapper.selectById(1L)).thenReturn(cs);

    // then: 抛出异常
    assertThrows(BusinessException.class,
        () -> courseService.dropCourse(3L, 1L));
}
```

#### Test Case 3: GradeServiceImpl.inputGrade()

```java
@Test
void inputGrade_ShouldSucceed_WhenValidScore() {
    // when
    gradeService.inputGrade(1L, 3L, 1L, 85.0, "numeric", null);
    // then: 验证保存
    verify(gradeMapper).insert(any(Grade.class));
}

@Test
void inputGrade_ShouldThrow_WhenScoreOutOfRange() {
    // then: 分数 > 100 抛异常
    assertThrows(BusinessException.class,
        () -> gradeService.inputGrade(1L, 3L, 1L, 150.0, "numeric", null));
}
```

#### Test Case 4: TrainingPlanServiceImpl.generateSemester()

```java
@Test
void generateSemester_ShouldCreateCourses() {
    // given: 培养方案有 3 个课程项
    when(planItemMapper.selectList(any())).thenReturn(testPlanItems());

    // when
    trainingPlanService.generateSemester(1L, 1);

    // then: 创建了 3 门课程
    verify(courseMapper, times(3)).insert(any(Course.class));
}
```

### 3.3 集成测试设计（Controller + Service + DB）

使用 `@SpringBootTest` + H2 内存数据库，加载完整应用上下文。

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "admin", "password": "123456"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(200))
            .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    void login_ShouldReturn401_WhenInvalidPassword() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"username": "admin", "password": "wrong"}
                    """))
            .andExpect(status().isUnauthorized());
    }
}
```

**集成测试场景清单：**

| # | 测试场景 | 涉及接口 | 验证点 |
|:--:|---------|---------|--------|
| IT-1 | 正常登录流程 | POST /api/auth/login | 返回 Token、用户信息 |
| IT-2 | 密码错误拒绝 | POST /api/auth/login | 401 状态码 |
| IT-3 | 无 Token 访问受保护接口 | GET /api/edu/courses | 401 状态码 |
| IT-4 | 学生成功选课 | POST /api/edu/selections | 201/200 + 选课记录创建 |
| IT-5 | 选课容量满拒绝 | POST /api/edu/selections | 业务异常 400 |
| IT-6 | 退选选修课 | DELETE /api/edu/selections/{id} | 200 + 记录删除 |
| IT-7 | 退选必修课拒绝 | DELETE /api/edu/selections/{id} | 业务异常 400 |
| IT-8 | 学生提交请假 | POST /api/admin/leaves | 200 + 记录创建 |
| IT-9 | 辅导员审批请假 | PUT /api/admin/leaves/{id}/approve | 200 + 状态更新 |
| IT-10 | 排课冲突检测 | POST /api/edu/courses/{id}/schedule | 冲突时抛异常 |
| IT-11 | 非管理员访问管理接口 | DELETE /api/sys/users/{id} | 403 限制 |

---

## 4. 前端测试设计

### 4.1 测试技术栈

| 工具 | 用途 |
|------|------|
| vitest | 测试运行器 |
| @vue/test-utils | Vue 组件测试 |
| @vueuse/core 测试工具 | Pinia Store 测试 |
| jsdom | 浏览器环境模拟 |

### 4.2 Store 测试

#### userStore 测试

```typescript
import { setActivePinia, createPinia } from 'pinia';
import { useUserStore } from '@/store/user';

describe('userStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
    localStorage.clear();
  });

  it('should login and store token', async () => {
    const store = useUserStore();
    await store.login('admin', '123456');

    expect(store.token).toBeTruthy();
    expect(localStorage.getItem('token')).toBeTruthy();
    expect(store.role).toBe('admin');
  });

  it('should logout and clear state', () => {
    const store = useUserStore();
    store.token = 'fake-token';
    store.role = 'student';

    store.logout();

    expect(store.token).toBeNull();
    expect(store.role).toBeNull();
    expect(localStorage.getItem('token')).toBeNull();
  });

  it('should throw on invalid credentials', async () => {
    const store = useUserStore();
    await expect(store.login('admin', 'wrong'))
      .rejects.toThrow();
  });
});
```

#### appStore 测试

```typescript
import { useAppStore } from '@/store/app';

describe('appStore', () => {
  it('should start with sidebar expanded', () => {
    const store = useAppStore();
    expect(store.sidebarCollapsed).toBe(false);
  });

  it('should toggle sidebar', () => {
    const store = useAppStore();
    store.toggleSidebar();
    expect(store.sidebarCollapsed).toBe(true);
    store.toggleSidebar();
    expect(store.sidebarCollapsed).toBe(false);
  });
});
```

### 4.3 组件测试

#### LoginView 测试

```typescript
import { mount } from '@vue/test-utils';
import LoginView from '@/views/login/LoginView.vue';

describe('LoginView', () => {
  it('should render login form', () => {
    const wrapper = mount(LoginView);
    expect(wrapper.find('input[type="text"]').exists()).toBe(true);
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
  });

  it('should disable submit when fields empty', () => {
    const wrapper = mount(LoginView);
    const btn = wrapper.find('button[type="submit"]');
    expect(btn.attributes('disabled')).toBeDefined();
  });
});
```

---

## 5. 测试数据准备

### 5.1 后端测试数据

使用 `application-test.yml` + `schema.sql` + `data.sql` 的方式：

```yaml
# application-test.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1
    driver-class-name: org.h2.Driver
  sql:
    init:
      schema-locations: classpath:schema.sql
      data-locations: classpath:data.sql
```

**预置测试数据（最小集）：**

| 表 | 记录数 | 说明 |
|----|:-----:|------|
| sys_role | 4 | student/teacher/admin/counselor |
| sys_user | 4 | admin(admin), t001(teacher), s001(student), c001(counselor) |
| sys_department | 1 | 计算机学院 |
| sys_major | 1 | 计算机科学与技术 |
| sys_grade | 1 | 2025级 |
| sys_class | 1 | 计科2501班 |
| edu_semester | 1 | 2025-2026学年第二学期（活动） |
| edu_course | 2 | 1门必修(已发布), 1门选修(选课中) |
| edu_course_class | 2 | 必修课→班级关联, 选修课→班级关联 |

**原则：** 测试数据仅包含测试所需的最小记录数，不多余插入无关数据。

### 5.2 前端测试数据

使用 `vi.mock()` 模拟 API 层：

```typescript
// 模拟 api/edu.ts
vi.mock('@/api/edu', () => ({
  getAvailableCourses: vi.fn().mockResolvedValue({
    code: 200,
    data: mockCourses
  }),
  selectCourse: vi.fn().mockResolvedValue({ code: 200 }),
}));
```

---

## 6. CI 集成方案

### 6.1 GitHub Actions 工作流

```yaml
# .github/workflows/ci.yml
name: CI

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main]

jobs:
  backend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build and Test
        run: |
          cd backend
          mvn clean test -Dspring.profiles.active=test
      - name: Upload coverage report
        uses: actions/upload-artifact@v4
        with:
          name: backend-coverage
          path: backend/target/site/jacoco/

  frontend:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Use Node.js 18
        uses: actions/setup-node@v4
        with:
          node-version: '18'
      - name: Install and Test
        run: |
          cd frontend
          npm ci
          npm run test:unit
          npm run build
```

### 6.2 CI 校验内容

| 阶段 | 命令 | 用途 |
|------|------|------|
| 后端编译 | `mvn compile` | 检查 Java 编译 |
| 后端测试 | `mvn test` | 运行全部单元/集成测试 |
| 前端类型检查 | `npx vue-tsc --noEmit` | TypeScript 类型校验 |
| 前端测试 | `npm run test:unit` | 运行 vitest |
| 前端构建 | `npm run build` | 验证生产构建通过 |

---

## 7. 验收标准

### 7.1 测试覆盖率目标

| 模块 | 行覆盖率目标 | 分支覆盖率目标 |
|------|:-----------:|:-------------:|
| edu — CourseService | ≥ 80% | ≥ 70% |
| edu — GradeService | ≥ 75% | ≥ 60% |
| edu — TrainingPlanService | ≥ 70% | ≥ 60% |
| sys — SysUserService | ≥ 75% | ≥ 60% |
| admin — AdminService | ≥ 70% | ≥ 60% |
| club — ClubService | ≥ 60% | ≥ 50% |
| 后端整体 | ≥ 60% | ≥ 50% |
| 前端 Store | ≥ 80% | — |
| 前端组件 | ≥ 60% | — |

### 7.2 CI 通过标准

- ✅ 所有测试用例通过（0 失败）
- ✅ 前端类型检查无错误
- ✅ 构建产物正常生成
- ✅ 测试耗时 < 3 分钟

### 7.3 质量门禁

| 门禁 | 阻断条件 | 行动 |
|------|---------|------|
| 编译错误 | 任何编译失败 | ❌ 阻断 CI |
| 测试失败 | 任一测试用例失败 | ❌ 阻断 CI |
| 类型错误 | vue-tsc 报错 | ❌ 阻断 CI |
| 覆盖率 | 低于目标值 | ⚠️ 警告不阻断 |

---

> **说明：** 本文档为测试计划说明书（R2a），定义了系统的测试策略、覆盖范围、测试设计、数据准备和 CI 集成方案。
>
> **下一阶段：** 审核通过后，进行测试代码实现 + CI 配置 + Swagger 集成。
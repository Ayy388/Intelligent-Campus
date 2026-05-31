# 院系-专业-年级统一管理设计

## 1. 概述

当前系统中，`sys_user.department`（用户院系）、`sys_class.department/major/grade`（班级院系/专业/年级）均为自由文本字段，用户在输入时缺乏约束，导致数据不一致（如同一院系出现"计算机学院"、"计科院"、"计算机科学与技术学院"等多种写法）。

本次改造将**院系（Department）**、**专业（Major）**、**年级（Grade）** 提取为独立字典表，由管理员统一管理，所有引用处改为下拉选择，从根本上杜绝数据混乱。

## 2. 数据模型

### 2.1 院系表 `sys_department`（新建）

```sql
CREATE TABLE sys_department (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '院系名称，如"计算机学院"',
    code        VARCHAR(20) COMMENT '院系代码，唯一，如 CS',
    sort_order  INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';
```

### 2.2 专业表 `sys_major`（新建）

```sql
CREATE TABLE sys_major (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL COMMENT '专业名称，如"计算机科学与技术"',
    code          VARCHAR(20) COMMENT '专业代码，如 080901',
    department_id BIGINT COMMENT '所属院系 FK → sys_department.id',
    years         INT DEFAULT 4 COMMENT '学制年限',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';
```

### 2.3 年级表 `sys_grade`（新建）

```sql
CREATE TABLE sys_grade (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '年级名称，如"2023级"',
    year        INT NOT NULL UNIQUE COMMENT '年份，如 2023',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';
```

### 2.4 Entity 变更

**SysUser**（[SysUser.java](file:///d:/Intelligent-Campus/backend/src/main/java/com/campus/module/sys/entity/SysUser.java)）

```
- private String department;                    // 删除
+ private Long departmentId;                    // FK → sys_department.id
+ @TableField(exist = false) private String departmentName;  // 虚拟字段，展示用
```

**SysClass**（[SysClass.java](file:///d:/Intelligent-Campus/backend/src/main/java/com/campus/module/sys/entity/SysClass.java)）

```
- private String department;                    // 删除
- private String major;                         // 删除
- private String grade;                         // 删除
+ private Long departmentId;                    // FK → sys_department.id
+ private Long majorId;                         // FK → sys_major.id
+ private Long gradeId;                         // FK → sys_grade.id
+ @TableField(exist = false) private String departmentName;  // 虚拟字段
+ @TableField(exist = false) private String majorName;       // 虚拟字段
+ @TableField(exist = false) private String gradeName;       // 虚拟字段
```

## 3. API 设计

### 3.1 新增控制器

所有控制器放在 `com.campus.module.sys.controller` 包下。

**DepartmentController** — `/api/sys/departments`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/sys/departments` | admin | 分页列表，支持 keyword 搜索 name |
| GET | `/api/sys/departments/all` | authenticated | 全部院系（下拉用） |
| GET | `/api/sys/departments/{id}` | admin | 详情 |
| POST | `/api/sys/departments` | admin | 新增 |
| PUT | `/api/sys/departments/{id}` | admin | 编辑 |
| DELETE | `/api/sys/departments/{id}` | admin | 删除（有专业关联时禁止） |

**MajorController** — `/api/sys/majors`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/sys/majors` | admin | 分页列表 |
| GET | `/api/sys/majors/all` | authenticated | 全部专业，支持 ?deptId= 按院系过滤 |
| GET | `/api/sys/majors/{id}` | admin | 详情 |
| POST | `/api/sys/majors` | admin | 新增 |
| PUT | `/api/sys/majors/{id}` | admin | 编辑 |
| DELETE | `/api/sys/majors/{id}` | admin | 删除 |

**GradeController** — `/api/sys/grades`

| 方法 | 路径 | 权限 | 说明 |
|------|------|------|------|
| GET | `/api/sys/grades` | admin | 分页列表 |
| GET | `/api/sys/grades/all` | authenticated | 全部年级（下拉用） |
| GET | `/api/sys/grades/{id}` | admin | 详情 |
| POST | `/api/sys/grades` | admin | 新增 |
| PUT | `/api/sys/grades/{id}` | admin | 编辑 |
| DELETE | `/api/sys/grades/{id}` | admin | 删除 |

### 3.2 SecurityConfig 新增规则

在 [SecurityConfig.java](file:///d:/Intelligent-Campus/backend/src/main/java/com/campus/config/SecurityConfig.java) 中，在 `/api/sys/**` → hasRole("admin") 之前插入：

```java
.requestMatchers(HttpMethod.GET, "/api/sys/departments/all").authenticated()
.requestMatchers(HttpMethod.GET, "/api/sys/majors/all").authenticated()
.requestMatchers(HttpMethod.GET, "/api/sys/grades/all").authenticated()
```

### 3.3 受影响的服务改造

**SysUserServiceImpl.getUsers()**：查询用户列表时，通过 `departmentId` 关联 `sys_department` 填充 `departmentName`

**SysClassServiceImpl**：查询班级列表时，JOIN 三张字典表填充 `departmentName`/`majorName`/`gradeName`

**CourseServiceImpl.getCourseStudents()**：学生信息中的 `studentDepartment` 改为通过 `departmentId` 获取院系名称

## 4. 前端设计

### 4.1 新增管理页面

三个页面均放在 `frontend/src/views/sys/` 目录下，风格与 [ClassManagement.vue](file:///d:/Intelligent-Campus/frontend/src/views/sys/ClassManagement.vue) 一致（el-card + el-table + el-dialog）。

**DepartmentManagement.vue** — `/admin/departments`
- 列表列：ID、名称、代码、排序、操作
- 新增/编辑弹窗：名称（必填）、代码、排序号

**MajorManagement.vue** — `/admin/majors`
- 列表列：ID、名称、代码、所属院系、学制、操作
- 新增/编辑弹窗：名称（必填）、代码、所属院系（下拉选择）、学制年限
- 院系下拉选项来自 getAllDepartments()

**GradeManagement.vue** — `/admin/grades`
- 列表列：ID、年级名称、年份、操作
- 新增/编辑弹窗：名称（必填，如"2023级"）、年份（必填）

**侧边栏**：在"行政服务"菜单下新增三个子菜单项

### 4.2 用户管理改造

[UserManage.vue](file:///d:/Intelligent-Campus/frontend/src/views/manage/UserManage.vue)

- 院系字段改为 el-select 从 getAllDepartments() 获取选项
- 用户列表表格中展示 `departmentName`
- submit 时提交 `departmentId`

### 4.3 班级管理改造

[ClassManagement.vue](file:///d:/Intelligent-Campus/frontend/src/views/sys/ClassManagement.vue)

- **院系**：el-select，选项来自 getAllDepartments()
- **专业**：el-select 级联，选择院系后调用 `getMajorsByDept(deptId)` 获取该院系下的专业，未选院系时专业下拉禁用
- **年级**：el-select，选项来自 getAllGrades()
- 列表表格展示 `departmentName`/`majorName`/`gradeName`

### 4.4 API 层

在 [sys.ts](file:///d:/Intelligent-Campus/frontend/src/api/sys.ts) 中新增：

```typescript
// 院系
export function getDepartments(params: any)
export function getAllDepartments()
export function createDepartment(data: any)
export function updateDepartment(id: number, data: any)
export function deleteDepartment(id: number)

// 专业
export function getMajors(params: any)
export function getMajorsByDept(deptId: number)
export function createMajor(data: any)
export function updateMajor(id: number, data: any)
export function deleteMajor(id: number)

// 年级
export function getGrades(params: any)
export function getAllGrades()
export function createGrade(data: any)
export function updateGrade(id: number, data: any)
export function deleteGrade(id: number)
```

## 5. 数据迁移与边界处理

### 5.1 DataInitializer 自动迁移

在 [DataInitializer.java](file:///d:/Intelligent-Campus/backend/src/main/java/com/campus/config/DataInitializer.java) 中新增：

1. **CREATE TABLE IF NOT EXISTS** 创建三张新表
2. **ALTER TABLE** 尝试添加新字段（department_id/major_id/grade_id），失败则忽略（字段已存在）
3. **数据填充**：从现有 `sys_user.department` 和 `sys_class.department/major/grade` 去重导入到字典表
4. **外键更新**：UPDATE 语句将 `sys_user.department` → `sys_user.department_id`，`sys_class.*` 同理

### 5.2 删除约束

| 场景 | 行为 |
|------|------|
| 删除院系，下有专业 | 后端拦截，返回 "该院系下有 N 个专业，请先删除专业" |
| 删除院系，下有用户/班级 | 不强制阻止，允许保留引用（展示时显示"已删除"） |
| 删除专业，下有班级 | 同上 |
| 删除年级，下有班级 | 同上 |

## 6. 涉及文件清单

### 后端（14 个文件）

| # | 文件 | 操作 |
|---|------|------|
| 1 | `entity/SysDepartment.java` | 新建 |
| 2 | `entity/SysMajor.java` | 新建 |
| 3 | `entity/SysGrade.java` | 新建 |
| 4 | `mapper/SysDepartmentMapper.java` | 新建 |
| 5 | `mapper/SysMajorMapper.java` | 新建 |
| 6 | `mapper/SysGradeMapper.java` | 新建 |
| 7 | `service/SysDepartmentService.java` | 新建 |
| 8 | `service/SysMajorService.java` | 新建 |
| 9 | `service/SysGradeService.java` | 新建 |
| 10 | `service/impl/SysDepartmentServiceImpl.java` | 新建 |
| 11 | `service/impl/SysMajorServiceImpl.java` | 新建 |
| 12 | `service/impl/SysGradeServiceImpl.java` | 新建 |
| 13 | `controller/SysDepartmentController.java` | 新建 |
| 14 | `controller/SysMajorController.java` | 新建 |
| 15 | `controller/SysGradeController.java` | 新建 |
| 16 | `entity/SysUser.java` | 修改：department → departmentId |
| 17 | `entity/SysClass.java` | 修改：department/major/grade → 外键 |
| 18 | `service/impl/SysUserServiceImpl.java` | 修改：查询时填充 departmentName |
| 19 | `service/impl/SysClassServiceImpl.java` | 修改：查询时填充三字段名称 |
| 20 | `config/SecurityConfig.java` | 修改：新增 all 端点权限 |
| 21 | `config/DataInitializer.java` | 修改：创建表 + 迁移数据 |
| 22 | `sql/init.sql` | 修改：添加三张建表语句 |

### 前端（8 个文件）

| # | 文件 | 操作 |
|---|------|------|
| 1 | `views/sys/DepartmentManagement.vue` | 新建 |
| 2 | `views/sys/MajorManagement.vue` | 新建 |
| 3 | `views/sys/GradeManagement.vue` | 新建 |
| 4 | `views/manage/UserManage.vue` | 修改：department → 下拉 |
| 5 | `views/sys/ClassManagement.vue` | 修改：三字段 → 级联下拉 |
| 6 | `api/sys.ts` | 修改：新增 12 个 API 函数 |
| 7 | `components/AppSidebar.vue` | 修改：新增三个菜单项 |
| 8 | `router/index.ts` | 修改：新增三个路由 |
# 院系-专业-年级统一管理 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 sys_user.department、sys_class.department/major/grade 从自由文本改为外键关联字典表，由管理员统一管理院系/专业/年级，前端改为下拉选择。

**Architecture:** 新增三张字典表（sys_department/sys_major/sys_grade），每个表一套 CRUD（Mapper + Service + Controller）。SysUser 和 SysClass 实体改 String 字段为 Long 外键 + 虚拟展示字段。DataInitializer 自动建表+迁移旧数据。

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus, Vue 3 + TypeScript + Element Plus

---

## 文件结构

### 后端新建（15 个文件）

| 文件 | 职责 |
|------|------|
| `entity/SysDepartment.java` | 院系实体 |
| `entity/SysMajor.java` | 专业实体 |
| `entity/SysGrade.java` | 年级实体 |
| `mapper/SysDepartmentMapper.java` | 院系 Mapper |
| `mapper/SysMajorMapper.java` | 专业 Mapper |
| `mapper/SysGradeMapper.java` | 年级 Mapper |
| `service/SysDepartmentService.java` | 院系服务接口 |
| `service/SysMajorService.java` | 专业服务接口 |
| `service/SysGradeService.java` | 年级服务接口 |
| `service/impl/SysDepartmentServiceImpl.java` | 院系服务实现 |
| `service/impl/SysMajorServiceImpl.java` | 专业服务实现 |
| `service/impl/SysGradeServiceImpl.java` | 年级服务实现 |
| `controller/SysDepartmentController.java` | 院系控制器 |
| `controller/SysMajorController.java` | 专业控制器 |
| `controller/SysGradeController.java` | 年级控制器 |

### 后端修改（7 个文件）

| 文件 | 改动 |
|------|------|
| `entity/SysUser.java` | department(String) → departmentId(Long) + departmentName(虚拟) |
| `entity/SysClass.java` | department/major/grade(String) → XXXId(Long) + XXXName(虚拟) |
| `service/impl/SysUserServiceImpl.java` | 查询时填充 departmentName |
| `service/impl/SysClassServiceImpl.java` | 查询时填充三字段名称 |
| `config/SecurityConfig.java` | /api/sys/majors/all 等端点放行 authenticated |
| `config/DataInitializer.java` | 创建三张表 + 迁移旧数据 |
| `sql/init.sql` | 添加三张建表语句 |

### 前端新建（3 个文件）

| 文件 | 职责 |
|------|------|
| `views/sys/DepartmentManagement.vue` | 院系管理页面 |
| `views/sys/MajorManagement.vue` | 专业管理页面 |
| `views/sys/GradeManagement.vue` | 年级管理页面 |

### 前端修改（5 个文件）

| 文件 | 改动 |
|------|------|
| `api/sys.ts` | 新增 12 个 API 函数 |
| `views/manage/UserManage.vue` | department → departmentId 下拉 |
| `views/sys/ClassManagement.vue` | 三字段 → 级联下拉 |
| `components/AppSidebar.vue` | 新增三个菜单项 |
| `router/index.ts` | 新增三个路由 |

---

### Task 1: 新建院系/专业/年级实体

**Files:**
- Create: `backend/src/main/java/com/campus/module/sys/entity/SysDepartment.java`
- Create: `backend/src/main/java/com/campus/module/sys/entity/SysMajor.java`
- Create: `backend/src/main/java/com/campus/module/sys/entity/SysGrade.java`
- Create: `backend/src/main/java/com/campus/module/sys/mapper/SysDepartmentMapper.java`
- Create: `backend/src/main/java/com/campus/module/sys/mapper/SysMajorMapper.java`
- Create: `backend/src/main/java/com/campus/module/sys/mapper/SysGradeMapper.java`

- [ ] **Step 1: 创建 SysDepartment.java**

```java
package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_department")
public class SysDepartment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
```

- [ ] **Step 2: 创建 SysMajor.java**

```java
package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_major")
public class SysMajor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String code;
    private Long departmentId;
    private Integer years;
    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 创建 SysGrade.java**

```java
package com.campus.module.sys.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_grade")
public class SysGrade {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer year;
    private LocalDateTime createTime;
}
```

- [ ] **Step 4: 创建三个 Mapper**

SysDepartmentMapper.java:
```java
package com.campus.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.sys.entity.SysDepartment;

public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {}
```

SysMajorMapper.java:
```java
package com.campus.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.sys.entity.SysMajor;

public interface SysMajorMapper extends BaseMapper<SysMajor> {}
```

SysGradeMapper.java:
```java
package com.campus.module.sys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.sys.entity.SysGrade;

public interface SysGradeMapper extends BaseMapper<SysGrade> {}
```

---

### Task 2: 新建服务层（接口 + 实现）

**Files:**
- Create: `backend/src/main/java/com/campus/module/sys/service/SysDepartmentService.java`
- Create: `backend/src/main/java/com/campus/module/sys/service/SysMajorService.java`
- Create: `backend/src/main/java/com/campus/module/sys/service/SysGradeService.java`
- Create: `backend/src/main/java/com/campus/module/sys/service/impl/SysDepartmentServiceImpl.java`
- Create: `backend/src/main/java/com/campus/module/sys/service/impl/SysMajorServiceImpl.java`
- Create: `backend/src/main/java/com/campus/module/sys/service/impl/SysGradeServiceImpl.java`
- Modify: (参照) `backend/src/main/java/com/campus/module/sys/service/impl/SysClassServiceImpl.java`

- [ ] **Step 1: 创建三个服务接口**

SysDepartmentService.java:
```java
package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysDepartment;
import java.util.List;

public interface SysDepartmentService {
    Page<SysDepartment> page(int page, int size, String keyword);
    List<SysDepartment> listAll();
    SysDepartment getById(Long id);
    void save(SysDepartment dept);
    void update(SysDepartment dept);
    void delete(Long id);
}
```

SysMajorService.java:
```java
package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysMajor;
import java.util.List;

public interface SysMajorService {
    Page<SysMajor> page(int page, int size, String keyword);
    List<SysMajor> listAll(Long deptId);
    SysMajor getById(Long id);
    void save(SysMajor major);
    void update(SysMajor major);
    void delete(Long id);
}
```

SysGradeService.java:
```java
package com.campus.module.sys.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysGrade;
import java.util.List;

public interface SysGradeService {
    Page<SysGrade> page(int page, int size);
    List<SysGrade> listAll();
    SysGrade getById(Long id);
    void save(SysGrade grade);
    void update(SysGrade grade);
    void delete(Long id);
}
```

- [ ] **Step 2: 创建 SysDepartmentServiceImpl**

```java
package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysDepartment;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.mapper.SysDepartmentMapper;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysDepartmentServiceImpl implements SysDepartmentService {
    private final SysDepartmentMapper mapper;
    private final SysMajorMapper majorMapper;

    @Override
    public Page<SysDepartment> page(int page, int size, String keyword) {
        LambdaQueryWrapper<SysDepartment> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty())
            w.like(SysDepartment::getName, keyword);
        w.orderByAsc(SysDepartment::getSortOrder);
        return mapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public List<SysDepartment> listAll() {
        return mapper.selectList(new LambdaQueryWrapper<SysDepartment>()
            .orderByAsc(SysDepartment::getSortOrder));
    }

    @Override
    public SysDepartment getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void save(SysDepartment dept) {
        mapper.insert(dept);
    }

    @Override
    public void update(SysDepartment dept) {
        mapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        Long count = majorMapper.selectCount(
            new LambdaQueryWrapper<SysMajor>()
                .eq(SysMajor::getDepartmentId, id));
        if (count > 0) {
            throw new com.campus.common.BusinessException("该院系下有 " + count + " 个专业，请先删除专业");
        }
        mapper.deleteById(id);
    }
}
```

- [ ] **Step 3: 创建 SysMajorServiceImpl**

```java
package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.mapper.SysMajorMapper;
import com.campus.module.sys.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysMajorServiceImpl implements SysMajorService {
    private final SysMajorMapper mapper;

    @Override
    public Page<SysMajor> page(int page, int size, String keyword) {
        LambdaQueryWrapper<SysMajor> w = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isEmpty())
            w.like(SysMajor::getName, keyword);
        w.orderByAsc(SysMajor::getId);
        return mapper.selectPage(new Page<>(page, size), w);
    }

    @Override
    public List<SysMajor> listAll(Long deptId) {
        LambdaQueryWrapper<SysMajor> w = new LambdaQueryWrapper<>();
        if (deptId != null) w.eq(SysMajor::getDepartmentId, deptId);
        w.orderByAsc(SysMajor::getId);
        return mapper.selectList(w);
    }

    @Override
    public SysMajor getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void save(SysMajor major) {
        mapper.insert(major);
    }

    @Override
    public void update(SysMajor major) {
        mapper.updateById(major);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
```

- [ ] **Step 4: 创建 SysGradeServiceImpl**

```java
package com.campus.module.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.mapper.SysGradeMapper;
import com.campus.module.sys.service.SysGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysGradeServiceImpl implements SysGradeService {
    private final SysGradeMapper mapper;

    @Override
    public Page<SysGrade> page(int page, int size) {
        return mapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<SysGrade>().orderByDesc(SysGrade::getYear));
    }

    @Override
    public List<SysGrade> listAll() {
        return mapper.selectList(
            new LambdaQueryWrapper<SysGrade>().orderByDesc(SysGrade::getYear));
    }

    @Override
    public SysGrade getById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public void save(SysGrade grade) {
        mapper.insert(grade);
    }

    @Override
    public void update(SysGrade grade) {
        mapper.updateById(grade);
    }

    @Override
    public void delete(Long id) {
        mapper.deleteById(id);
    }
}
```

---

### Task 3: 新建控制器（DepartmentController + MajorController + GradeController）

**Files:**
- Create: `backend/src/main/java/com/campus/module/sys/controller/SysDepartmentController.java`
- Create: `backend/src/main/java/com/campus/module/sys/controller/SysMajorController.java`
- Create: `backend/src/main/java/com/campus/module/sys/controller/SysGradeController.java`

- [ ] **Step 1: 创建 SysDepartmentController**

```java
package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysDepartment;
import com.campus.module.sys.service.SysDepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/departments")
@RequiredArgsConstructor
public class SysDepartmentController {
    private final SysDepartmentService service;

    @GetMapping
    public Result<PageResult<SysDepartment>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Page<SysDepartment> p = service.page(page, size, keyword);
        PageResult<SysDepartment> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysDepartment>> all() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysDepartment> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysDepartment dept) {
        service.save(dept);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysDepartment dept) {
        dept.setId(id);
        service.update(dept);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
```

- [ ] **Step 2: 创建 SysMajorController**

```java
package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysMajor;
import com.campus.module.sys.service.SysMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/majors")
@RequiredArgsConstructor
public class SysMajorController {
    private final SysMajorService service;

    @GetMapping
    public Result<PageResult<SysMajor>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {
        Page<SysMajor> p = service.page(page, size, keyword);
        PageResult<SysMajor> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysMajor>> all(@RequestParam(required = false) Long deptId) {
        return Result.ok(service.listAll(deptId));
    }

    @GetMapping("/{id}")
    public Result<SysMajor> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysMajor major) {
        service.save(major);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysMajor major) {
        major.setId(id);
        service.update(major);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
```

- [ ] **Step 3: 创建 SysGradeController**

```java
package com.campus.module.sys.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysGrade;
import com.campus.module.sys.service.SysGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/grades")
@RequiredArgsConstructor
public class SysGradeController {
    private final SysGradeService service;

    @GetMapping
    public Result<PageResult<SysGrade>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<SysGrade> p = service.page(page, size);
        PageResult<SysGrade> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysGrade>> all() {
        return Result.ok(service.listAll());
    }

    @GetMapping("/{id}")
    public Result<SysGrade> get(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody SysGrade grade) {
        service.save(grade);
        return Result.ok();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysGrade grade) {
        grade.setId(id);
        service.update(grade);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok();
    }
}
```

---

### Task 4: 修改 SysUser 和 SysClass 实体

**Files:**
- Modify: `backend/src/main/java/com/campus/module/sys/entity/SysUser.java`
- Modify: `backend/src/main/java/com/campus/module/sys/entity/SysClass.java`

- [ ] **Step 1: 修改 SysUser.java**

在现有文件基础上，将 `private String department;` 替换为：

```java
    private Long departmentId;
    @TableField(exist = false)
    private String departmentName;
```

保持其他字段不变。

- [ ] **Step 2: 修改 SysClass.java**

在现有文件基础上，将：

```java
    private String department;
    private String major;
    private String grade;
```

替换为：

```java
    private Long departmentId;
    private Long majorId;
    private Long gradeId;
    @TableField(exist = false)
    private String departmentName;
    @TableField(exist = false)
    private String majorName;
    @TableField(exist = false)
    private String gradeName;
```

---

### Task 5: 修改 SysUserServiceImpl 和 SysClassServiceImpl 填充展示字段

**Files:**
- Modify: `backend/src/main/java/com/campus/module/sys/service/impl/SysUserServiceImpl.java`
- Modify: `backend/src/main/java/com/campus/module/sys/service/impl/SysClassServiceImpl.java`

- [ ] **Step 1: 查看 SysUserServiceImpl 当前代码**

Read the file first to understand current implementation.

- [ ] **Step 2: 修改 SysUserServiceImpl 查询方法填充 departmentName**

在查询用户列表的方法中，查询后遍历填充：

```java
// 在已有查询代码后添加：
SysDepartmentMapper deptMapper; // 注入该字段
for (SysUser u : records) {
    if (u.getDepartmentId() != null) {
        SysDepartment dept = deptMapper.selectById(u.getDepartmentId());
        if (dept != null) u.setDepartmentName(dept.getName());
    }
}
```

需要注入 `SysDepartmentMapper`。

- [ ] **Step 3: 查看 SysClassServiceImpl 当前代码**

Read the file first to understand current implementation.

- [ ] **Step 4: 修改 SysClassServiceImpl 填充三字段名称**

在查询班级列表的方法中，查询后遍历填充：

```java
// 需要注入 SysDepartmentMapper, SysMajorMapper, SysGradeMapper
for (SysClass c : records) {
    if (c.getDepartmentId() != null) {
        SysDepartment d = deptMapper.selectById(c.getDepartmentId());
        if (d != null) c.setDepartmentName(d.getName());
    }
    if (c.getMajorId() != null) {
        SysMajor m = majorMapper.selectById(c.getMajorId());
        if (m != null) c.setMajorName(m.getName());
    }
    if (c.getGradeId() != null) {
        SysGrade g = gradeMapper.selectById(c.getGradeId());
        if (g != null) c.setGradeName(g.getName());
    }
}
```

---

### Task 6: 修改 SecurityConfig

**Files:**
- Modify: `backend/src/main/java/com/campus/config/SecurityConfig.java`

- [ ] **Step 1: 在 `/api/sys/**` → hasRole("admin") 之前插入 all 端点放行规则

找到 `.requestMatchers("/api/sys/**").hasRole("admin")` 这一行，在其前面插入：

```java
                .requestMatchers(HttpMethod.GET, "/api/sys/departments/all").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/sys/majors/all").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/sys/grades/all").authenticated()
```

---

### Task 7: 修改 DataInitializer 和 init.sql

**Files:**
- Modify: `backend/src/main/java/com/campus/config/DataInitializer.java`
- Modify: `backend/sql/init.sql`

- [ ] **Step 1: 在 DataInitializer 中添加三张表的 CREATE TABLE**

在 DataInitializer.java 的 run() 方法中，在已有建表语句后添加：

```java
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_department (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                code VARCHAR(20),
                sort_order INT DEFAULT 0,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_major (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(100) NOT NULL,
                code VARCHAR(20),
                department_id BIGINT,
                years INT DEFAULT 4,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);

        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS sys_grade (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(50) NOT NULL,
                year INT NOT NULL UNIQUE,
                create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """);
```

- [ ] **Step 2: 在 DataInitializer 中添加 ALTER TABLE 迁移**

```java
        try {
            jdbcTemplate.execute("ALTER TABLE sys_user ADD COLUMN department_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN department_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN major_id BIGINT");
        } catch (Exception ignored) {}
        try {
            jdbcTemplate.execute("ALTER TABLE sys_class ADD COLUMN grade_id BIGINT");
        } catch (Exception ignored) {}
```

- [ ] **Step 3: 在 DataInitializer 中添加旧数据迁移**

```java
        // 迁移院系数据
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_department", Integer.class);
            if (cnt != null && cnt == 0) {
                jdbcTemplate.update(
                    "INSERT INTO sys_department (name) SELECT DISTINCT department FROM sys_user WHERE department IS NOT NULL AND department != ''");
                jdbcTemplate.update(
                    "UPDATE sys_user u JOIN sys_department d ON u.department = d.name SET u.department_id = d.id WHERE u.department_id IS NULL");
            }
        } catch (Exception ignored) {}

        // 迁移班级院系/专业/年级数据
        try {
            // 院系
            jdbcTemplate.update(
                "INSERT IGNORE INTO sys_department (name) SELECT DISTINCT department FROM sys_class WHERE department IS NOT NULL AND department != ''");
            jdbcTemplate.update(
                "UPDATE sys_class c JOIN sys_department d ON c.department = d.name SET c.department_id = d.id WHERE c.department_id IS NULL");

            // 专业
            jdbcTemplate.update(
                "INSERT IGNORE INTO sys_major (name, department_id) SELECT DISTINCT s.major, s.department_id FROM sys_class s WHERE s.major IS NOT NULL AND s.major != ''");
            jdbcTemplate.update(
                "UPDATE sys_class c JOIN sys_major m ON c.major = m.name AND c.department_id = m.department_id SET c.major_id = m.id WHERE c.major_id IS NULL");

            // 年级
            jdbcTemplate.update(
                "INSERT IGNORE INTO sys_grade (name, year) SELECT DISTINCT grade, CAST(grade AS UNSIGNED) FROM sys_class WHERE grade IS NOT NULL AND grade != ''");
            jdbcTemplate.update(
                "UPDATE sys_class c JOIN sys_grade g ON c.grade = g.name SET c.grade_id = g.id WHERE c.grade_id IS NULL");
        } catch (Exception ignored) {}
```

- [ ] **Step 4: 更新 init.sql 追加建表语句**

在 init.sql 末尾追加：

```sql
CREATE TABLE IF NOT EXISTS sys_department (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '院系名称',
    code        VARCHAR(20) COMMENT '院系代码',
    sort_order  INT DEFAULT 0 COMMENT '排序号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='院系表';

CREATE TABLE IF NOT EXISTS sys_major (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(100) NOT NULL COMMENT '专业名称',
    code          VARCHAR(20) COMMENT '专业代码',
    department_id BIGINT COMMENT '所属院系',
    years         INT DEFAULT 4 COMMENT '学制年限',
    create_time   DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

CREATE TABLE IF NOT EXISTS sys_grade (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50) NOT NULL COMMENT '年级名称',
    year        INT NOT NULL UNIQUE COMMENT '年份',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='年级表';
```

---

### Task 8: 前端 API 层

**Files:**
- Modify: `frontend/src/api/sys.ts`

- [ ] **Step 1: 在 sys.ts 末尾追加新 API 函数**

```typescript
// === 院系管理 ===
export function getDepartments(params: any) {
  return request.get('/sys/departments', { params })
}
export function getAllDepartments() {
  return request.get('/sys/departments/all')
}
export function createDepartment(data: any) {
  return request.post('/sys/departments', data)
}
export function updateDepartment(id: number, data: any) {
  return request.put(`/sys/departments/${id}`, data)
}
export function deleteDepartment(id: number) {
  return request.delete(`/sys/departments/${id}`)
}

// === 专业管理 ===
export function getMajors(params: any) {
  return request.get('/sys/majors', { params })
}
export function getMajorsByDept(deptId: number) {
  return request.get('/sys/majors/all', { params: { deptId } })
}
export function createMajor(data: any) {
  return request.post('/sys/majors', data)
}
export function updateMajor(id: number, data: any) {
  return request.put(`/sys/majors/${id}`, data)
}
export function deleteMajor(id: number) {
  return request.delete(`/sys/majors/${id}`)
}

// === 年级管理 ===
export function getGrades(params: any) {
  return request.get('/sys/grades', { params })
}
export function getAllGrades() {
  return request.get('/sys/grades/all')
}
export function createGrade(data: any) {
  return request.post('/sys/grades', data)
}
export function updateGrade(id: number, data: any) {
  return request.put(`/sys/grades/${id}`, data)
}
export function deleteGrade(id: number) {
  return request.delete(`/sys/grades/${id}`)
}
```

---

### Task 9: 前端院系/专业/年级管理页面

**Files:**
- Create: `frontend/src/views/sys/DepartmentManagement.vue`
- Create: `frontend/src/views/sys/MajorManagement.vue`
- Create: `frontend/src/views/sys/GradeManagement.vue`

- [ ] **Step 1: 创建 DepartmentManagement.vue**

```vue
<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>院系管理</span><el-button type="primary" @click="openCreate">新增院系</el-button></div></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="院系名称" />
      <el-table-column prop="code" label="代码" />
      <el-table-column prop="sortOrder" label="排序" width="60" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑院系':'新增院系'" width="500px">
      <el-form :model="form">
        <el-form-item label="院系名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="排序号"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getDepartments, createDepartment, updateDepartment, deleteDepartment } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ name: '', code: '', sortOrder: 0 })

async function fetch() {
  loading.value = true
  const r = await getDepartments({ page: page.value, size: size.value })
  list.value = r.data.records || []
  total.value = r.data.total || 0
  loading.value = false
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.name = ''; form.code = ''; form.sortOrder = 0
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.code = row.code || ''; form.sortOrder = row.sortOrder || 0
  dialogVisible.value = true
}

async function submit() {
  if (isEdit.value && editId.value) {
    await updateDepartment(editId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createDepartment(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; fetch()
}

async function doDelete(id: number) {
  await ElMessageBox.confirm('确定删除该院系？', '提示', { type: 'warning' })
  await deleteDepartment(id); ElMessage.success('删除成功'); fetch()
}

onMounted(fetch)
</script>
```

- [ ] **Step 2: 创建 MajorManagement.vue**

```vue
<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>专业管理</span><el-button type="primary" @click="openCreate">新增专业</el-button></div></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="专业名称" />
      <el-table-column prop="code" label="代码" />
      <el-table-column prop="departmentName" label="所属院系" />
      <el-table-column prop="years" label="学制" width="60" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑专业':'新增专业'" width="500px">
      <el-form :model="form">
        <el-form-item label="专业名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="代码"><el-input v-model="form.code" /></el-form-item>
        <el-form-item label="所属院系">
          <el-select v-model="form.departmentId" placeholder="选择院系" class="w-full">
            <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学制年限"><el-input-number v-model="form.years" :min="2" :max="5" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getMajors, getAllDepartments, createMajor, updateMajor, deleteMajor } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref<any[]>([])
const departments = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ name: '', code: '', departmentId: null as number | null, years: 4 })

async function fetch() {
  loading.value = true
  const r = await getMajors({ page: page.value, size: size.value })
  list.value = (r.data.records || []).map((item: any) => ({
    ...item,
    departmentName: departments.value.find((d: any) => d.id === item.departmentId)?.name || ''
  }))
  total.value = r.data.total || 0
  loading.value = false
}

async function fetchDepartments() {
  const r = await getAllDepartments()
  departments.value = r.data || []
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.name = ''; form.code = ''; form.departmentId = null; form.years = 4
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.code = row.code || ''; form.departmentId = row.departmentId; form.years = row.years || 4
  dialogVisible.value = true
}

async function submit() {
  if (isEdit.value && editId.value) {
    await updateMajor(editId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createMajor(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; fetch()
}

async function doDelete(id: number) {
  await ElMessageBox.confirm('确定删除该专业？', '提示', { type: 'warning' })
  await deleteMajor(id); ElMessage.success('删除成功'); fetch()
}

onMounted(() => { fetch(); fetchDepartments() })
</script>
```

- [ ] **Step 3: 创建 GradeManagement.vue**

```vue
<template>
  <el-card>
    <template #header><div style="display:flex;justify-content:space-between"><span>年级管理</span><el-button type="primary" @click="openCreate">新增年级</el-button></div></template>
    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="年级名称" />
      <el-table-column prop="year" label="年份" width="80" />
      <el-table-column label="操作" width="150">
        <template #default="{row}">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="doDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination v-model:current-page="page" :total="total" :page-size="size" layout="prev,pager,next" @current-change="fetch" />
    <el-dialog v-model="dialogVisible" :title="isEdit?'编辑年级':'新增年级'" width="500px">
      <el-form :model="form">
        <el-form-item label="年级名称"><el-input v-model="form.name" placeholder="如 2023级" /></el-form-item>
        <el-form-item label="年份"><el-input-number v-model="form.year" :min="2000" :max="2099" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialogVisible=false">取消</el-button><el-button type="primary" @click="submit">保存</el-button></template>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getGrades, createGrade, updateGrade, deleteGrade } from '@/api/sys'
import { ElMessage, ElMessageBox } from 'element-plus'

const list = ref<any[]>([])
const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref<number | null>(null)
const form = reactive({ name: '', year: new Date().getFullYear() })

async function fetch() {
  loading.value = true
  const r = await getGrades({ page: page.value, size: size.value })
  list.value = r.data.records || []
  total.value = r.data.total || 0
  loading.value = false
}

function openCreate() {
  isEdit.value = false; editId.value = null
  form.name = ''; form.year = new Date().getFullYear()
  dialogVisible.value = true
}

function openEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  form.name = row.name; form.year = row.year
  dialogVisible.value = true
}

async function submit() {
  if (isEdit.value && editId.value) {
    await updateGrade(editId.value, form)
    ElMessage.success('修改成功')
  } else {
    await createGrade(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false; fetch()
}

async function doDelete(id: number) {
  await ElMessageBox.confirm('确定删除该年级？', '提示', { type: 'warning' })
  await deleteGrade(id); ElMessage.success('删除成功'); fetch()
}

onMounted(fetch)
</script>
```

---

### Task 10: 修改用户管理和班级管理页面

**Files:**
- Modify: `frontend/src/views/manage/UserManage.vue`
- Modify: `frontend/src/views/sys/ClassManagement.vue`

- [ ] **Step 1: 修改 UserManage.vue 的院系字段**

将模板中的：
```vue
<el-form-item label="院系"><el-input v-model="form.department" /></el-form-item>
```
改为：
```vue
<el-form-item label="院系">
  <el-select v-model="form.departmentId" placeholder="选择院系" class="w-full">
    <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
  </el-select>
</el-form-item>
```

在 script 中：
- 添加 `import { getAllDepartments } from '@/api/sys'`
- 添加 `const departments = ref<any[]>([])`
- 在脚本中新增 `fetchDepartments` 函数：
```typescript
async function fetchDepartments() {
  try { const r = await getAllDepartments(); departments.value = r.data || [] } catch {}
}
```
- 在 `onMounted` 中调用 `fetchDepartments()`
- 修改 `form` 初始值：`department: ''` → `departmentId: null`
- 修改 `showDialog` 中的 Object.assign：`department: ''` → `departmentId: null`
- 表格列 `prop="department"` 改为 `prop="departmentName"`（需要后端返回 `departmentName`）

- [ ] **Step 2: 修改 ClassManagement.vue 的三个字段**

将三个 `<el-input>` 替换为级联下拉：

```vue
<el-form-item label="院系">
  <el-select v-model="form.departmentId" placeholder="选择院系" class="w-full" @change="onDeptChange">
    <el-option v-for="d in departments" :key="d.id" :label="d.name" :value="d.id" />
  </el-select>
</el-form-item>
<el-form-item label="专业">
  <el-select v-model="form.majorId" placeholder="选择专业" class="w-full" :disabled="!form.departmentId">
    <el-option v-for="m in majors" :key="m.id" :label="m.name" :value="m.id" />
  </el-select>
</el-form-item>
<el-form-item label="年级">
  <el-select v-model="form.gradeId" placeholder="选择年级" class="w-full">
    <el-option v-for="g in grades" :key="g.id" :label="g.name" :value="g.id" />
  </el-select>
</el-form-item>
```

在 script 中改造：

```typescript
import { getClasses, createClass, updateClass, deleteClass } from '@/api/sys'
import { getAllDepartments, getMajorsByDept, getAllGrades } from '@/api/sys'

const departments = ref<any[]>([])
const majors = ref<any[]>([])
const grades = ref<any[]>([])
const form = reactive({ className: '', departmentId: null as number | null, majorId: null as number | null, gradeId: null as number | null, advisor: '' })

// 新增函数
async function fetchDepartments() {
  try { const r = await getAllDepartments(); departments.value = r.data || [] } catch {}
}
async function fetchGrades() {
  try { const r = await getAllGrades(); grades.value = r.data || [] } catch {}
}
async function onDeptChange(deptId: number) {
  form.majorId = null
  if (deptId) {
    try { const r = await getMajorsByDept(deptId); majors.value = r.data || [] } catch {}
  } else {
    majors.value = []
  }
}

// 修改 openCreate
function openCreate() {
  isEdit.value = false; editId.value = null
  form.className = ''; form.departmentId = null; form.majorId = null; form.gradeId = null; form.advisor = ''
  dialogVisible.value = true
}

// 修改 openEdit
function openEdit(row: any) {
  isEdit.value = true; editId.value = row.id
  form.className = row.className; form.departmentId = row.departmentId
  form.majorId = row.majorId; form.gradeId = row.gradeId; form.advisor = row.advisor
  if (row.departmentId) onDeptChange(row.departmentId)
  dialogVisible.value = true
}

// 修改表格列
// department → departmentName
// major → majorName
// grade → gradeName

// 修改 onMounted
onMounted(() => { fetch(); fetchDepartments(); fetchGrades() })
```

---

### Task 11: 路由和侧边栏

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/components/AppSidebar.vue`

- [ ] **Step 1: 在 router/index.ts 添加三个路由**

在 `/admin/classes` 路由后添加：

```typescript
  {
    path: '/admin/departments',
    name: 'DepartmentManagement',
    component: () => import('@/views/sys/DepartmentManagement.vue'),
    meta: { title: '院系管理', role: 'admin' }
  },
  {
    path: '/admin/majors',
    name: 'MajorManagement',
    component: () => import('@/views/sys/MajorManagement.vue'),
    meta: { title: '专业管理', role: 'admin' }
  },
  {
    path: '/admin/grades',
    name: 'GradeManagement',
    component: () => import('@/views/sys/GradeManagement.vue'),
    meta: { title: '年级管理', role: 'admin' }
  },
```

- [ ] **Step 2: 在侧边栏添加菜单项**

在 AppSidebar.vue 的"班级管理"菜单项后添加：

```vue
<el-menu-item index="/admin/departments">院系管理</el-menu-item>
<el-menu-item index="/admin/majors">专业管理</el-menu-item>
<el-menu-item index="/admin/grades">年级管理</el-menu-item>
```

---

### Task 12: 编译验证

- [ ] **Step 1: 后端编译**

```bash
cd backend && mvn compile -q
```

预期：BUILD SUCCESS

- [ ] **Step 2: 前端 TypeScript 检查**

```bash
cd frontend && npx vue-tsc --noEmit
```

预期：仅 ClubList.vue 已有错误，其他无新增错误

- [ ] **Step 3: 启动应用验证**

启动后端和前端，登录管理，访问：
1. `/admin/departments` — 能正常展示院系列表
2. `/admin/majors` — 能正常展示专业列表
3. `/admin/grades` — 能正常展示年级列表
4. 用户管理 — 院系字段为下拉选择
5. 班级管理 — 院系/专业/年级为级联下拉
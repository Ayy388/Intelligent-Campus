# 班级-课程体系 实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 引入行政班管理、必修/选修分离、基于班级的签到机制

**Architecture:** 新增 sys_class 班级表和 edu_course_class 课程-班级关联表；修改 SysUser/Course/CourseSelection/CheckIn 等现有实体；重构 CourseService 支持必修分配和选修限制；GrowthService 签到按班级过滤；前端新增班级管理页面，改造课程创建/选课/签到/教学页面

**Tech Stack:** Spring Boot 3.x + MyBatis-Plus + Vue 3 + Element Plus

---

### Task 1: 新建 sys_class 表 + Java Entity + Mapper

**Files:**
- Create: `backend/sql/init.sql`（追加建表语句）
- Create: `backend/src/main/java/com/campus/module/sys/entity/SysClass.java`
- Create: `backend/src/main/java/com/campus/module/sys/mapper/SysClassMapper.java`

- [ ] **Step 1: 追加建表语句到 init.sql**

在文件末尾追加：
```sql
CREATE TABLE IF NOT EXISTS sys_class (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    class_name  VARCHAR(100) NOT NULL COMMENT '班级名称，如2023级计算机科学与技术1班',
    department  VARCHAR(100) DEFAULT NULL COMMENT '所属院系',
    major       VARCHAR(100) DEFAULT NULL COMMENT '专业',
    grade       VARCHAR(20) DEFAULT NULL COMMENT '年级，如2023',
    advisor     VARCHAR(50) DEFAULT NULL COMMENT '辅导员',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 创建 SysClass 实体**

`backend/src/main/java/com/campus/module/sys/entity/SysClass.java`:
```java
package com.campus.module.sys.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_class")
public class SysClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String className;
    private String department;
    private String major;
    private String grade;
    private String advisor;
    private LocalDateTime createTime;
}
```

- [ ] **Step 3: 创建 SysClassMapper**

`backend/src/main/java/com/campus/module/sys/mapper/SysClassMapper.java`:
```java
package com.campus.module.sys.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.sys.entity.SysClass;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface SysClassMapper extends BaseMapper<SysClass> {}
```

---

### Task 2: 新建 edu_course_class 表 + Entity + Mapper

**Files:**
- Create: `backend/src/main/java/com/campus/module/edu/entity/CourseClass.java`
- Create: `backend/src/main/java/com/campus/module/edu/mapper/CourseClassMapper.java`

- [ ] **Step 1: 追加建表语句到 init.sql**

```sql
CREATE TABLE IF NOT EXISTS edu_course_class (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT NOT NULL COMMENT '关联edu_course.id',
    class_id    BIGINT DEFAULT NULL COMMENT '关联sys_class.id，null=不限班级',
    is_required TINYINT(1) DEFAULT 0 COMMENT '1=必修 0=选修'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

- [ ] **Step 2: 创建 CourseClass 实体**

`backend/src/main/java/com/campus/module/edu/entity/CourseClass.java`:
```java
package com.campus.module.edu.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("edu_course_class")
public class CourseClass {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long courseId;
    private Long classId;
    private Integer isRequired;
}
```

- [ ] **Step 3: 创建 CourseClassMapper**

`backend/src/main/java/com/campus/module/edu/mapper/CourseClassMapper.java`:
```java
package com.campus.module.edu.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.module.edu.entity.CourseClass;
import org.apache.ibatis.annotations.Mapper;
@Mapper
public interface CourseClassMapper extends BaseMapper<CourseClass> {}
```

---

### Task 3: 修改 SysUser 实体（className → classId）

**Files:**
- Modify: `backend/src/main/java/com/campus/module/sys/entity/SysUser.java`

- [ ] **Step 1: 替换 SysUser 中的 className 字段**

将 `private String className;` 替换为 `private Long classId;`，并添加虚拟字段：
```java
// 删除原有
// private String className;

// 替换为
private Long classId;

@TableField(exist = false)
private String className;   // 保留用于前端展示（从 sys_class 表 JOIN 获取）
```

---

### Task 4: 修改 Course 实体（新增 courseType/minStudents/enrollEnd）

**Files:**
- Modify: `backend/src/main/java/com/campus/module/edu/entity/Course.java`

- [ ] **Step 1: 在 Course 实体中增加三个字段**

```java
// 在现有字段之后添加
private String courseType;       // required / elective
private Integer minStudents;     // 选修课最低开课人数
private LocalDateTime enrollEnd; // 选修课选课截止时间
```

确保 import `java.time.LocalDateTime`（已存在）。

---

### Task 5: 修改 CourseSelection 实体（新增 selectType）

**Files:**
- Modify: `backend/src/main/java/com/campus/module/edu/entity/CourseSelection.java`

- [ ] **Step 1: 增加 selectType 字段**

```java
// 在现有字段之后添加
private String selectType;       // auto / manual
```

---

### Task 6: 修改 CheckIn 实体（新增 classId）

**Files:**
- Modify: `backend/src/main/java/com/campus/module/growth/entity/CheckIn.java`

- [ ] **Step 1: 增加 classId 字段**

```java
// 在 courseId 之后添加
private Long classId;
```

---

### Task 7: 后端 - SysClass CRUD Service + Controller

**Files:**
- Create: `backend/src/main/java/com/campus/module/sys/service/SysClassService.java`
- Create: `backend/src/main/java/com/campus/module/sys/service/impl/SysClassServiceImpl.java`
- Create: `backend/src/main/java/com/campus/module/sys/controller/SysClassController.java`
- Modify: `backend/src/main/java/com/campus/module/sys/service/SysUserService.java`（新增按班级查询用户方法）

- [ ] **Step 1: 创建 SysClassService 接口**

`SysClassService.java`:
```java
package com.campus.module.sys.service;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysClass;
import java.util.List;

public interface SysClassService {
    Page<SysClass> page(int page, int size);
    SysClass getById(Long id);
    List<SysClass> listAll();
    void save(SysClass c);
    void update(Long id, SysClass c);
    void delete(Long id);
}
```

- [ ] **Step 2: 创建 SysClassServiceImpl**

`SysClassServiceImpl.java`:
```java
package com.campus.module.sys.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.mapper.SysClassMapper;
import com.campus.module.sys.service.SysClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SysClassServiceImpl implements SysClassService {
    private final SysClassMapper sysClassMapper;

    @Override
    public Page<SysClass> page(int page, int size) {
        return sysClassMapper.selectPage(new Page<>(page, size),
            new LambdaQueryWrapper<SysClass>().orderByDesc(SysClass::getId));
    }

    @Override
    public SysClass getById(Long id) { return sysClassMapper.selectById(id); }

    @Override
    public List<SysClass> listAll() { return sysClassMapper.selectList(null); }

    @Override
    public void save(SysClass c) { sysClassMapper.insert(c); }

    @Override
    public void update(Long id, SysClass c) { c.setId(id); sysClassMapper.updateById(c); }

    @Override
    public void delete(Long id) { sysClassMapper.deleteById(id); }
}
```

- [ ] **Step 3: 创建 SysClassController**

`SysClassController.java`:
```java
package com.campus.module.sys.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.common.PageResult;
import com.campus.common.Result;
import com.campus.module.sys.entity.SysClass;
import com.campus.module.sys.service.SysClassService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sys/classes")
@RequiredArgsConstructor
public class SysClassController {
    private final SysClassService sysClassService;

    @GetMapping
    public Result<PageResult<SysClass>> list(@RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "20") int size) {
        Page<SysClass> p = sysClassService.page(page, size);
        PageResult<SysClass> pr = new PageResult<>();
        pr.setRecords(p.getRecords()); pr.setTotal(p.getTotal());
        pr.setPage(p.getCurrent()); pr.setSize(p.getSize()); pr.setPages(p.getPages());
        return Result.ok(pr);
    }

    @GetMapping("/all")
    public Result<List<SysClass>> all() { return Result.ok(sysClassService.listAll()); }

    @GetMapping("/{id}")
    public Result<SysClass> get(@PathVariable Long id) { return Result.ok(sysClassService.getById(id)); }

    @PostMapping
    public Result<Void> save(@RequestBody SysClass c) { sysClassService.save(c); return Result.ok(); }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody SysClass c) { sysClassService.update(id, c); return Result.ok(); }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { sysClassService.delete(id); return Result.ok(); }
}
```

---

### Task 8: 后端 - 重写 CourseService（必修分配、选修限制、课程确认）

**Files:**
- Modify: `backend/src/main/java/com/campus/module/edu/service/CourseService.java`
- Modify: `backend/src/main/java/com/campus/module/edu/service/impl/CourseServiceImpl.java`
- Create: `backend/src/main/java/com/campus/module/edu/mapper/CourseClassMapper.java`（Task2 已创建）

- [ ] **Step 1: CourseService 接口新增方法**

```java
// 新增
void assignRequiredCourse(Long courseId, List<Long> classIds);  // 下达必修教学任务
List<Course> getAvailableCourses(Long studentId);                // 学生查看可选选修课
void enrollElective(Long courseId, Long studentId);              // 学生选选修课
boolean checkEnoughStudents(Long courseId);                      // 检查是否达到开课人数
void confirmCourse(Long courseId);                               // 管理员确认开课
void cancelCourse(Long courseId);                                // 管理员取消开课
List<CourseClass> getCourseClasses(Long courseId);               // 获取课程的班级关联
void setCourseClasses(Long courseId, List<CourseClass> classes); // 设置课程的班级关联
```

- [ ] **Step 2: CourseServiceImpl 实现**

注入 `CourseClassMapper`, `SysUserMapper`, `SysClassMapper`：
```java
private final CourseClassMapper courseClassMapper;
private final SysUserMapper userMapper;
private final SysClassMapper sysClassMapper;
```

**assignRequiredCourse 实现：**
```java
@Override
@Transactional
public void assignRequiredCourse(Long courseId, List<Long> classIds) {
    Course course = courseMapper.selectById(courseId);
    if (course == null) throw new BusinessException("课程不存在");

    // 创建课程-班级关联
    for (Long classId : classIds) {
        CourseClass cc = new CourseClass();
        cc.setCourseId(courseId);
        cc.setClassId(classId);
        cc.setIsRequired(1);
        courseClassMapper.insert(cc);

        // 为该班所有学生创建选课记录
        List<SysUser> students = userMapper.selectList(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getClassId, classId)
                .eq(SysUser::getRoleId, /* 学生角色ID，需要先查出来 */));
        
        // 查询学生角色ID
        // 假设从数据库或配置中获取学生角色ID
    }

    // 课程状态设为已确认
    course.setStatus(2);
    course.setCourseType("required");
    courseMapper.updateById(course);
}
```

> 注意：学生角色ID需要从 `sys_role` 表查出。可以注入 `SysRoleMapper` 或直接用一个常量。查询方式：`roleMapper.selectOne(new LambdaQueryWrapper<SysRole>().eq(SysRole::getRoleName, "student")).getId()`

**getAvailableCourses 实现（按限制过滤）：**
```java
@Override
public List<Course> getAvailableCourses(Long studentId) {
    SysUser student = userMapper.selectById(studentId);
    if (student == null || student.getClassId() == null) return List.of();

    SysClass studentClass = sysClassMapper.selectById(student.getClassId());

    // 查询所有选课中的选修课
    List<Course> all = courseMapper.selectList(
        new LambdaQueryWrapper<Course>()
            .eq(Course::getStatus, 1)
            .eq(Course::getCourseType, "elective"));

    // 按限制条件过滤
    List<Course> available = new ArrayList<>();
    for (Course course : all) {
        List<CourseClass> constraints = courseClassMapper.selectList(
            new LambdaQueryWrapper<CourseClass>()
                .eq(CourseClass::getCourseId, course.getId())
                .eq(CourseClass::getIsRequired, 0));

        if (constraints.isEmpty()) {
            // 无限制条件 → 全校可选
            available.add(course);
            continue;
        }

        boolean accessible = constraints.stream().anyMatch(cc -> {
            if (cc.getClassId() == null) return true; // 不限
            return cc.getClassId().equals(student.getClassId());
        });

        // 如果不全按班级，还可能有专业/年级限制，这里可以通过 SysClass 的字段判断
        // 简化：先实现按班级限制，专业/年级限制后续扩展
        // ...

        if (accessible) available.add(course);
    }

    return available;
}
```

**enrollElective 实现：**
```java
@Override
public void enrollElective(Long courseId, Long studentId) {
    Course course = courseMapper.selectById(courseId);
    if (course == null) throw new BusinessException("课程不存在");
    if (course.getStatus() != 1) throw new BusinessException("该课程不在选课期");
    if (!"elective".equals(course.getCourseType())) throw new BusinessException("该课程不是选修课");

    // 检查是否已选
    Long cnt = selectionMapper.selectCount(
        new LambdaQueryWrapper<CourseSelection>()
            .eq(CourseSelection::getCourseId, courseId)
            .eq(CourseSelection::getStudentId, studentId)
            .eq(CourseSelection::getStatus, 1));
    if (cnt > 0) throw new BusinessException("已选该课程");

    // 检查容量
    if (course.getEnrolled() != null && course.getCapacity() != null
        && course.getEnrolled() >= course.getCapacity())
        throw new BusinessException("该课程已满员");

    // 创建选课记录
    CourseSelection cs = new CourseSelection();
    cs.setStudentId(studentId);
    cs.setCourseId(courseId);
    cs.setSemester(course.getSemester());
    cs.setSelectTime(LocalDateTime.now());
    cs.setStatus(1);
    cs.setSelectType("manual");
    selectionMapper.insert(cs);

    // 更新选课人数
    course.setEnrolled((course.getEnrolled() != null ? course.getEnrolled() : 0) + 1);
    courseMapper.updateById(course);
}
```

**confirmCourse / cancelCourse 实现：**
```java
@Override
public void confirmCourse(Long courseId) {
    Course course = courseMapper.selectById(courseId);
    if (course == null) throw new BusinessException("课程不存在");
    if (course.getStatus() != 1) throw new BusinessException("课程状态不正确");
    if (course.getMinStudents() != null && course.getEnrolled() < course.getMinStudents())
        throw new BusinessException("选课人数未达最低开课要求");
    course.setStatus(2);
    courseMapper.updateById(course);
}

@Override
public void cancelCourse(Long courseId) {
    Course course = courseMapper.selectById(courseId);
    if (course == null) throw new BusinessException("课程不存在");
    course.setStatus(3);
    courseMapper.updateById(course);
}
```

**getCourseClasses / setCourseClasses 实现：**
```java
@Override
public List<CourseClass> getCourseClasses(Long courseId) {
    return courseClassMapper.selectList(
        new LambdaQueryWrapper<CourseClass>()
            .eq(CourseClass::getCourseId, courseId));
}

@Override
public void setCourseClasses(Long courseId, List<CourseClass> classes) {
    courseClassMapper.delete(new LambdaQueryWrapper<CourseClass>()
        .eq(CourseClass::getCourseId, courseId));
    for (CourseClass cc : classes) {
        cc.setCourseId(courseId);
        cc.setId(null);
        courseClassMapper.insert(cc);
    }
}
```

---

### Task 9: 后端 - CourseController 新增端点

**Files:**
- Modify: `backend/src/main/java/com/campus/module/edu/controller/CourseController.java`

- [ ] **Step 1: 注入 CourseClassMapper（如果需要）并新增端点**

```java
// 必修分配
@PostMapping("/{id}/assign-classes")
public Result<Void> assignClasses(@PathVariable Long id, @RequestBody List<Long> classIds) {
    courseService.assignRequiredCourse(id, classIds);
    return Result.ok();
}

// 获取课程班级关联
@GetMapping("/{id}/classes")
public Result<List<CourseClass>> getClasses(@PathVariable Long id) {
    return Result.ok(courseService.getCourseClasses(id));
}

// 设置课程班级关联
@PutMapping("/{id}/classes")
public Result<Void> setClasses(@PathVariable Long id, @RequestBody List<CourseClass> classes) {
    courseService.setCourseClasses(id, classes);
    return Result.ok();
}

// 学生查看可选课程
@GetMapping("/available")
public Result<List<Course>> availableCourses(Authentication auth) {
    return Result.ok(courseService.getAvailableCourses(getUserId(auth)));
}

// 学生选选修课
@PostMapping("/{id}/enroll")
public Result<Void> enroll(@PathVariable Long id, Authentication auth) {
    courseService.enrollElective(id, getUserId(auth));
    return Result.ok();
}

// 管理员确认开课
@PostMapping("/{id}/confirm-opening")
public Result<Void> confirmOpening(@PathVariable Long id) {
    courseService.confirmCourse(id);
    return Result.ok();
}

// 管理员取消开课
@PostMapping("/{id}/cancel-opening")
public Result<Void> cancelOpening(@PathVariable Long id) {
    courseService.cancelCourse(id);
    return Result.ok();
}
```

---

### Task 10: 后端 - GrowthService 签到按班级过滤

**Files:**
- Modify: `backend/src/main/java/com/campus/module/growth/service/GrowthService.java`
- Modify: `backend/src/main/java/com/campus/module/growth/service/impl/GrowthServiceImpl.java`

- [ ] **Step 1: GrowthService 接口新增方法**

```java
// 新增
List<CheckIn> getCheckInsByClass(Long classId, int page, int size);
```

- [ ] **Step 2: 修改 GrowthServiceImpl.createCheckIn**

在创建签到的方法中增加对 classId 的支持，同时传入 classId 时按课程+班级统计人数：

```java
@Override
public CheckIn createCheckIn(CheckIn c) {
    c.setStatus(1);
    if (c.getTotalCount() == null) c.setTotalCount(0);
    if (c.getCheckedCount() == null) c.setCheckedCount(0);
    if (c.getCourseId() != null) {
        Course course = courseMapper.selectById(c.getCourseId());
        if (course != null) {
            c.setCourseName(course.getCourseName());
            LambdaQueryWrapper<CourseSelection> qw = new LambdaQueryWrapper<CourseSelection>()
                .eq(CourseSelection::getCourseId, c.getCourseId())
                .eq(CourseSelection::getStatus, 1);
            if (c.getClassId() != null) {
                // 按班级过滤：需要查出该班级的学生列表
                List<SysUser> students = userMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getClassId, c.getClassId()));
                if (!students.isEmpty()) {
                    qw.in(CourseSelection::getStudentId,
                        students.stream().map(SysUser::getId).collect(Collectors.toList()));
                }
            }
            Long enrolled = selMapper.selectCount(qw);
            c.setTotalCount(enrolled.intValue());
        }
    }
    checkinMapper.insert(c);
    return c;
}
```

需要添加 import：
```java
import com.campus.module.sys.entity.SysUser;
import java.util.stream.Collectors;
```

- [ ] **Step 3: 修改 pageCheckIns 支持学生按班级查看**

```java
@Override
public Page<CheckIn> pageCheckIns(Long teacherId, int page, int size) {
    LambdaQueryWrapper<CheckIn> w = new LambdaQueryWrapper<>();
    if (teacherId != null) w.eq(CheckIn::getTeacherId, teacherId);
    w.orderByDesc(CheckIn::getCreateTime);
    // 注意：学生端需要另外的查询方式，这里保持原样
    Page<CheckIn> result = checkinMapper.selectPage(new Page<>(page, size), w);
    for (CheckIn c : result.getRecords()) {
        if (c.getTeacherId() != null) {
            SysUser teacher = userMapper.selectById(c.getTeacherId());
            if (teacher != null) c.setTeacherName(teacher.getRealName());
        }
        if (c.getCourseId() != null) {
            Course course = courseMapper.selectById(c.getCourseId());
            if (course != null) c.setCourseName(course.getCourseName());
        }
    }
    return result;
}
```

> 注意：后续可以添加一个 `getCheckInsForStudent(Long studentId)` 方法，只返回学生所在班级相关的签到。

---

### Task 11: 前端 - 新增班级管理 API

**Files:**
- Modify: `frontend/src/api/sys.ts`（如果不存在则创建该文件）

- [ ] **Step 1: 创建/修改 sys.ts**

`frontend/src/api/sys.ts`：
```typescript
import request from '@/utils/request'

export function getClasses(params: any) { return request.get('/sys/classes', { params }) }
export function getAllClasses() { return request.get('/sys/classes/all') }
export function getClass(id: number) { return request.get(`/sys/classes/${id}`) }
export function createClass(data: any) { return request.post('/sys/classes', data) }
export function updateClass(id: number, data: any) { return request.put(`/sys/classes/${id}`, data) }
export function deleteClass(id: number) { return request.delete(`/sys/classes/${id}`) }
```

---

### Task 12: 前端 - edu API 新增接口

**Files:**
- Modify: `frontend/src/api/edu.ts`

- [ ] **Step 1: 添加新 API 函数**

```typescript
// 必修分配
export function assignCourseClasses(courseId: number, classIds: number[]) {
  return request.post(`/edu/courses/${courseId}/assign-classes`, classIds)
}

// 课程班级关联
export function getCourseClasses(courseId: number) {
  return request.get(`/edu/courses/${courseId}/classes`)
}
export function setCourseClasses(courseId: number, classes: any[]) {
  return request.put(`/edu/courses/${courseId}/classes`, classes)
}

// 学生可选课程
export function getAvailableCourses() {
  return request.get('/edu/courses/available')
}

// 学生选课
export function enrollCourse(courseId: number) {
  return request.post(`/edu/courses/${courseId}/enroll`)
}

// 管理员确认/取消开课
export function confirmOpening(courseId: number) {
  return request.post(`/edu/courses/${courseId}/confirm-opening`)
}
export function cancelOpening(courseId: number) {
  return request.post(`/edu/courses/${courseId}/cancel-opening`)
}
```

---

### Task 13: 前端 - 班级管理页面

**Files:**
- Create: `frontend/src/views/sys/ClassManagement.vue`
- Modify: `frontend/src/components/AppSidebar.vue`（添加菜单项）
- Modify: `frontend/src/router/index.ts`（添加路由）

- [ ] **Step 1: 创建 班级管理页面**

`ClassManagement.vue`：包含班级的 CRUD 表格 + 新增/编辑对话框，字段包括 className, department, major, grade, advisor。

- [ ] **Step 2: 添加路由**

`/admin/classes` → `ClassManagement.vue`

- [ ] **Step 3: 添加侧边栏菜单**

管理员菜单下添加"班级管理"项

---

### Task 14: 前端 - 改造课程创建/编辑页面（区分必修/选修）

**Files:**
- Find 并修改课程创建/编辑页面（`frontend/src/views/edu/CourseForm.vue` 或类似）

- [ ] **Step 1: 找到并修改课程创建页面**

在课程表单中增加：
- `courseType` 单选：必修 / 选修
- 如果选选修，显示：最低开课人数、选课截止时间
- 如果选必修或选修，显示班级选择器（多选，从 sys_class 表获取所有班级）
- 班级选择器对应的数据保存到 `edu_course_class` 表

---

### Task 15: 前端 - 改造选课页面（学生端）

**Files:**
- Find 并修改选课页面（`frontend/src/views/edu/CourseSelect.vue` 或类似）

- [ ] **Step 1: 获取可选课程列表（调 available API）**
- [ ] **Step 2: 显示课程时标注必修/选修标签**
- [ ] **Step 3: 选修课显示"报名"按钮，点击调 enroll API**
- [ ] **Step 4: 显示最低开课人数和当前报名人数**

---

### Task 16: 前端 - 改造签到页面（按班级签到）

**Files:**
- Modify: `frontend/src/views/growth/CheckIn.vue`

- [ ] **Step 1: 发起签到弹窗增加班级选择**

教师选择课程后，自动加载该课程的班级列表（调 getCourseClasses API），然后选择班级。

- [ ] **Step 2: 学生签到列表只显示自己班级的签到**

在 `fetchCheckIns` 中增加班级过滤逻辑。

---

### Task 17: 前端 - 改造我的教学页面（按班级查看）

**Files:**
- Modify: `frontend/src/views/edu/TeacherTeaching.vue`

- [ ] **Step 1: 课程卡片点击后显示该课程下的班级列表**
- [ ] **Step 2: 点击班级显示该班的学生名单**
- [ ] **Step 3: 在班级面板中增加"发起签到"快捷按钮（跳转到签到页面并预选课程+班级）**

---

### Task 18: 数据迁移 + SecurityConfig 更新

**Files:**
- Modify: `backend/src/main/java/com/campus/config/SecurityConfig.java`
- Modify: `backend/src/main/resources/application.yml`（如果需要）

- [ ] **Step 1: 更新 SecurityConfig**

确保所有新增的 API 端点有正确的权限配置：
```java
// 新增：班级管理需要 admin 权限
.requestMatchers("/api/sys/classes/**").hasAuthority("admin")
```

- [ ] **Step 2: 执行数据库迁移**

手动在数据库中执行建表语句，并从现有 `SysUser.className` 中生成 `sys_class` 记录。

---

### Task 19: 添加学生查看自己班级签到的 API

**Files:**
- Modify: `backend/src/main/java/com/campus/module/growth/controller/GrowthController.java`
- Modify: `backend/src/main/java/com/campus/module/growth/service/GrowthService.java`
- Modify: `backend/src/main/java/com/campus/module/growth/service/impl/GrowthServiceImpl.java`

- [ ] **Step 1: GrowthService 新增方法**

```java
Page<CheckIn> getCheckInsForStudent(Long studentId, int page, int size);
```

实现：
```java
@Override
public Page<CheckIn> getCheckInsForStudent(Long studentId, int page, int size) {
    SysUser student = userMapper.selectById(studentId);
    if (student == null || student.getClassId() == null)
        return new Page<>(page, size);

    LambdaQueryWrapper<CheckIn> w = new LambdaQueryWrapper<CheckIn>()
        .eq(CheckIn::getClassId, student.getClassId())
        .orderByDesc(CheckIn::getCreateTime);
    Page<CheckIn> result = checkinMapper.selectPage(new Page<>(page, size), w);
    // 填充 teacherName, courseName...
    return result;
}
```

- [ ] **Step 2: 添加 Controller 端点**

在 GrowthController 中增加（或修改现有 checkin 列表接口按角色区分）：
```java
// 已有 /checkin 接口，修改为根据角色区分
// 如果是 student，调 getCheckInsForStudent
```

---

### Task 20: 管理员课程管理页面改造（下达教学任务、确认/取消开课）

**Files:**
- 查找并修改管理员课程管理页面（可能是 `frontend/src/views/edu/CourseManagement.vue`）

- [ ] **Step 1: 必修课操作按钮**：增加"下达教学任务"按钮
- [ ] **Step 2: 选修课操作按钮**：增加"确认开课"/"取消开课"按钮（在选课截止后）
- [ ] **Step 3: 课程列表增加"类型"列（必修/选修）**
- [ ] **Step 4: 课程创建/编辑页面关联班级选择**
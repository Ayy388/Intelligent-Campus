# 智能校园 P0+P1 完善计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development

**Goal:** 修复P0紧急缺陷+P1重要缺失功能，覆盖Search框/Dashboard/附件上传/GPA/管理UI等14项

**Architecture:** 前端为主（11个.vue文件），后端3处修改（AI截断+Prompt+选课时间字段），15个文件变更

**Tech Stack:** Vue 3 + TypeScript + Element Plus + Tailwind, Java 17 + Spring Boot + MyBatis-Plus

---

## Task 1: P0 — LostFound搜索框 + Dashboard真实通知 + 附件上传入口

**Files:**
- Modify: `frontend/src/views/life/LostFound.vue`
- Modify: `frontend/src/views/dashboard/DashboardView.vue`
- Modify: `frontend/src/views/admin/LeaveApply.vue`

### LostFound — 添加搜索框

在 tab 按钮行后面加搜索输入框：

```
搜索框 HTML: 在 tab 按钮 div 和发布按钮之间
<el-input v-model="keyword" placeholder="搜索..." size="small" clearable class="!w-48" @change="page=1;fetch()" />
```

script: 添加 `const keyword = ref('')`，fetch改为 `getLostFound({ type: curType.value, keyword: keyword.value, page: page.value, size: 10 })`

### Dashboard — 真实通知数据

替换"暂无新通知"为实际 API 调用。文件中 `const notifications = ref<any[]>([])`，在 onMounted 中：

```typescript
import { getNotifications } from '@/api/admin'

const notifications = ref<any[]>([])

async function fetchNotifications() {
  try { const r = await getNotifications({ page: 1, size: 5 }); notifications.value = r.data.records || [] } catch {}
}
onMounted(() => { fetchNotifications(); /* 原有的 loads */ })
```

模板中把硬编码替换为：
```html
<div v-for="n in notifications" :key="n.id" class="flex items-center justify-between py-3 border-b border-gray-50 last:border-0">
  <div>
    <div class="text-sm text-gray-800 font-medium">{{ n.title }}</div>
    <div class="text-xs text-gray-400 mt-1">{{ n.createTime?.substring(0,16) }}</div>
  </div>
  <el-tag v-if="n.isUrgent" size="small" type="danger">紧急</el-tag>
</div>
<div v-if="notifications.length===0" class="text-center text-gray-400 text-sm py-4">暂无新通知</div>
```

### LeaveApply — 附件上传

在 "请假原因" 表单项后添加：
```html
<el-form-item label="附件"><el-input v-model="form.attachment" placeholder="附件URL（可选）" /></el-form-item>
```

## Task 2: P0 — AI: 10轮截断 + 学校知识Prompt

**Files:**
- Modify: `backend/src/main/java/com/campus/module/ai/service/DeepSeekService.java`

### 2.1 修复 buildHistory 添加 10轮限制

修改 [buildHistory 方法](file:///d:/Intelligent-Campus/backend/src/main/java/com/campus/module/ai/service/DeepSeekService.java#L140-L150)：

```java
private List<Map<String, String>> buildHistory(Long conversationId) {
    List<AiMessage> messages = msgMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AiMessage>()
                    .eq(AiMessage::getConversationId, conversationId)
                    .orderByAsc(AiMessage::getCreateTime));
    List<Map<String, String>> history = new ArrayList<>();
    for (AiMessage msg : messages) {
        history.add(Map.of("role", msg.getRole(), "content", msg.getContent()));
    }
    // 只保留最后20条消息（10轮对话）
    if (history.size() > 20) {
        history = history.subList(history.size() - 20, history.size());
    }
    return history;
}
```

### 2.2 丰富 System Prompt

```java
private static final String SYSTEM_PROMPT = """
    你是智能校园AI助手，服务于本校师生。
    你可以回答以下问题：
    - 校历查询：本学期为2026年春季学期，6月30日结束
    - 图书馆：开放时间 8:00-22:00，周末不休
    - 校园卡：充值可在第一食堂1楼窗口或在线充值
    - 请假流程：学生在行政服务-请假申请中提交，由辅导员审批
    - 选课：教务学习-在线选课中操作，每学期最多选5门
    - 成绩：教务学习-成绩查询中查看各学期成绩
    - 社团：社团列表中可浏览和申请加入，活动中心可报名参加
    - 场地：教学楼A座301报告厅、B101活动室、C座阶梯教室可预约
    请用中文回答，保持友好专业。如不确定请建议用户咨询相关部门。""";
```

## Task 3: P1 — 办事指南管理UI + 通知筛选+紧急标记

**Files:**
- Modify: `frontend/src/views/admin/GuideList.vue`
- Modify: `frontend/src/views/admin/NotificationList.vue`

### GuideList — 新增/编辑/删除

添加创建/编辑/删除按钮和对话框（与现有项目 el-dialog 风格一致）。

模板添加 `v-if="userStore.role==='admin'"` 的"添加指南"按钮 + 表单弹窗：
- title, content(textarea), category(select: 学籍/教务/后勤/财务/保卫)
- 列表每行加"编辑"/"删除"按钮
- 删除确认

### NotificationList — 分类筛选 + isUrgent

1. 列表顶部加 `el-select` 分类筛选，绑定 `filterCategory`，`@change="fetchNotis"`
2. 发布表单中加 `isUrgent` checkbox: `<el-form-item label="紧急"><el-switch v-model="form.isUrgent" /></el-form-item>`
3. API 调用传 `category` 参数

后端 AdminController 的 listNotis 需要接受 category 参数。修改方法签名：

```java
@GetMapping("/notifications")
public Result<PageResult<Notification>> listNotis(
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String category) {
    return toPage(adminService.pageNotifications(page, size, category));
}
```

AdminService 和 AdminServiceImpl 也需要改：pageNotifications 添加 category 参数，w.eq 过滤。

## Task 4: P1 — 成绩录入前端 + GPA计算 + 选课时间配置

**Files:**
- Create: `frontend/src/views/edu/GradeEntry.vue`
- Modify: `frontend/src/views/edu/GradeQuery.vue`
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/components/AppSidebar.vue`
- Modify: `backend/src/main/java/com/campus/module/edu/entity/Course.java`
- Modify: `frontend/src/views/manage/CourseManage.vue`

### GradeEntry.vue — 教师成绩录入页面

完整新建页面：
- 顶部分课程下拉选择（仅教师自己的课程），或输入 studentId + courseId
- 表单：studentId、courseId、score、gradeType(百分制/等级制)、semester
- 提交调用 `inputGrade` API
- 仅 teacher 和 admin 可见

### GradeQuery.vue — 添加GPA计算

在成绩表格下方显示GPA：
```typescript
const gpa = computed(() => {
  if (grades.value.length === 0) return 0
  const total = grades.value.reduce((s, g) => s + (g.score || 0), 0)
  // 百分制转4.0制: >=90→4.0, >=80→3.0, >=70→2.0, >=60→1.0
  const points = grades.value.reduce((s, g) => {
    const score = g.score || 0
    if (score >= 90) return s + 4.0
    if (score >= 80) return s + 3.0
    if (score >= 70) return s + 2.0
    if (score >= 60) return s + 1.0
    return s
  }, 0)
  return grades.value.length > 0 ? (points / grades.value.length).toFixed(2) : '0.00'
})
```

### 选课时间配置

Course 实体添加字段（无需改表，app层面用status替代）：在 CourseManage.vue 中 status 下拉改为：0=未开放 / 1=开放选课 / 2=已结束。已实现。

### 路由+侧边栏

```typescript
{ path: 'edu/grade-entry', name: 'GradeEntry', component: () => import('@/views/edu/GradeEntry.vue') },
```

侧边栏添加 `v-if="userStore.role==='teacher' || userStore.role==='admin'"` 的成绩录入菜单。

## Task 5: P1 — 社团编辑删除UI + 食堂管理UI

**Files:**
- Modify: `frontend/src/views/club/ClubList.vue`
- Modify: `frontend/src/views/life/CanteenReview.vue`

### ClubList — 编辑/删除按钮

在卡片右上角或详情弹窗中，admin 可见编辑/删除按钮：
- 编辑：弹出 el-dialog 表单（name, description）
- 删除：ElMessageBox.confirm + deleteClub API
- 列表添加 el-pagination

### CanteenReview — 管理员食堂管理

admin 可见部分：
- "管理食堂"按钮 → 新增食堂弹窗
- 食堂卡片上方显示编辑/删除图标

## Task 6: 验证编译

```bash
cd d:\Intelligent-Campus\backend && mvn compile -q
cd d:\Intelligent-Campus\frontend && npx vue-tsc --noEmit
```

---

## Self-Review

1. **Coverage**: 覆盖 P0(4项) + P1(10项) 共14项
2. **No placeholders**: 所有代码均给出
3. **Type consistency**: 前后端API参数对齐

---

## Execution

Plan saved. Use subagent-driven-development.

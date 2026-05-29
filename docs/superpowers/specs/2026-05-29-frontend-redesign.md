# 前端视觉升级 — 黑白灰极简设计

> **版本**: v1.0  
> **日期**: 2026-05-29  
> **依赖 Spec**: `docs/superpowers/specs/2026-05-29-intelligent-campus-design.md`

---

## 1. 概述

在不改变 Element Plus 组件、路由、业务逻辑的前提下，通过 CSS 变量覆写 + Tailwind CSS 原子类 + animate.css 轻量动画，将前端整体风格升级为黑白灰极简风格。

### 设计原则

- **克制** — 只保留必要的视觉层次，去掉多余的颜色和装饰
- **清晰** — 信息层级由灰度深浅决定，而非多种颜色
- **一致** — 所有页面遵循同一套灰度色板 + 间距体系
- **微交互** — 悬浮阴影、过渡动画，让交互有「呼吸感」

---

## 2. 色板

| 色号 | 用途 |
|------|------|
| `#111827` | 最深 — 主按钮、侧边栏底色、标题文字 |
| `#1F2937` | 深灰 — 侧边栏渐变底、次级文字 |
| `#374151` | 中灰 — hover 状态、菜单活跃文字 |
| `#6B7280` | 灰 — 占位文案、禁用态 |
| `#9CA3AF` | 浅灰 — 图标、辅助文字 |
| `#D1D5DB` | 边框灰 — 分割线、disabled 边框 |
| `#E5E7EB` | 边框 — 卡片边框 |
| `#F3F4F6` | 浅底 — 斑马条纹、hover 行 |
| `#F9FAFB` | 页面背景 |
| `#FFFFFF` | 纯白 — 卡片、输入框底色 |

---

## 3. 技术栈

| 层 | 技术 | 用途 |
|----|------|------|
| 组件库 | Element Plus 2.x（保留） | 表格、表单、弹窗 |
| 原子化 CSS | Tailwind CSS 3.x（新增） | 间距、布局、颜色 |
| 动画 | animate.css 4.x（新增） | 淡入、悬浮、页面切换 |
| 主题 | CSS 变量覆写 | Element Plus 暗色变量重定义 |

---

## 4. 文件改动清单

| # | 文件 | 操作 | 说明 |
|---|------|------|------|
| 1 | `package.json` | 修改 | 新增 tailwindcss、autoprefixer、animate.css 依赖 |
| 2 | `tailwind.config.js` | 新建 | Tailwind 灰度色阶配置 |
| 3 | `postcss.config.js` | 新建 | Tailwind + Autoprefixer 插件 |
| 4 | `src/style.css` | 修改 | Tailwind 指令、Element Plus 变量覆写、animate.css 引入 |
| 5 | `src/views/login/LoginView.vue` | 修改 | 纯白极简登录页 |
| 6 | `src/layouts/MainLayout.vue` | 修改 | 背景、侧边栏容器 |
| 7 | `src/components/AppSidebar.vue` | 修改 | 深灰渐变菜单、左侧高亮 |
| 8 | `src/components/AppHeader.vue` | 修改 | 半透明白底顶栏 |
| 9 | `src/views/dashboard/DashboardView.vue` | 修改 | 白卡统计 + 留白 |
| 10 | `src/App.vue` | 修改 | 页面 fade 过渡 |

---

## 5. 各组件设计

### 5.1 登录页

```
┌───────────────────────────────────────┐
│          背景色 #F9FAFB               │
│                                       │
│    ┌─────────────────────────┐       │
│    │   background: #FFFFFF    │       │
│    │   border: #E5E7EB        │       │
│    │   border-radius: 12px    │       │
│    │                         │       │
│    │      智能校园             │       │
│    │      Intelligent Campus  │       │
│    │                         │       │
│    │   ┌───────────────────┐ │       │
│    │   │ 学号/工号          │ │       │
│    │   └───────────────────┘ │       │
│    │   ┌───────────────────┐ │       │
│    │   │ ········          │ │       │
│    │   └───────────────────┘ │       │
│    │   ┌───────────────────┐ │       │
│    │   │     登 录          │ │       │
│    │   │  bg: #111827 white │ │       │
│    │   └───────────────────┘ │       │
│    └─────────────────────────┘       │
└───────────────────────────────────────┘
```

- 卡片 `animate__animated animate__fadeInUp`
- 输入框：`border: 1px solid #E5E7EB`，focus 时 border 变 `#111827`
- 按钮：`bg-[#111827] hover:bg-[#374151]`，白字

### 5.2 主布局

```
┌──────┬────────────────────────────────┐
│ 侧栏  │  顶栏 rgba(255,255,255,0.92)   │
│      │  border-bottom: 1px #E5E7EB   │
│ #111 │────────────────────────────────│
│  827 │                                │
│      │  主内容区 bg: #F9FAFB          │
│      │  padding: 24px                 │
│      │                                │
│      │  ┌──────────────────────┐     │
│      │  │  卡片 bg: #FFFFFF    │     │
│      │  │  border: #E5E7EB     │     │
│      │  │  rounded-xl shadow-sm│     │
│      │  └──────────────────────┘     │
│      │                                │
└──────┴────────────────────────────────┘
```

- 侧边栏宽 240px，收缩后 64px
- 侧边栏背景 `linear-gradient(180deg, #111827 0%, #1F2937 100%)`
- 顶栏 `backdrop-filter: blur(8px)` 毛玻璃效果

### 5.3 侧边栏菜单

```
🏫 智能校园           ← 白色 Logo 区，底部细线分割
──────────────────
  首页              ← 图标 #9CA3AF，hover 变白
▎ 教务学习          ← 活跃态：左侧 3px 白色边线
   课程列表
   在线选课
   成绩查询
  行政服务
  AI助手
  系统管理（管理员）
```

- 菜单文字色 `#9CA3AF`，hover 变 `#FFFFFF`
- 活跃菜单项：`bg-[#1F2937]`，左侧 `border-l-2 border-white`
- 菜单展开/收起过渡动画

### 5.4 仪表盘

```
欢迎回来，[用户名]                     ← text-[#111827] text-2xl font-bold
──────────────────────────────────────

┌─────────┐ ┌─────────┐ ┌─────────┐
│ 我的课程  │ │ 未读通知  │ │ 待审批   │
│   8      │ │   3      │ │   2      │
│          │ │          │ │          │
└─────────┘ └─────────┘ └─────────┘

  每个卡片：bg-white rounded-xl border border-[#E5E7EB] shadow-sm
  数字：text-3xl font-bold text-[#111827]
  标签：text-sm text-[#6B7280]
```

- 统计卡片三列排列
- hover 时 `shadow-md` 微提升
- 卡片有 `animate__fadeInUp` 依次入场

---

## 6. 动画方案

| 场景 | 动画 |
|------|------|
| 页面路由切换 | `<router-view v-slot>` + `<Transition name="fade">` |
| 登录卡片 | `animate__fadeInUp` |
| 仪表盘卡片 | `animate__fadeInUp` 逐个延迟 |
| 表格行 hover | `transition-colors duration-150` |
| 侧边栏折叠 | `transition-width duration-300` |
| 按钮 hover | `transition-all duration-200` |

---

## 7. 自审

- ✅ 无占位符
- ✅ 色板完整定义，无矛盾
- ✅ 10 个文件范围清晰
- ✅ 不改业务逻辑、路由、API
- ✅ Element Plus + Tailwind 共存方案明确

---

## 8. 修订记录

| 版本 | 日期 | 内容 |
|------|------|------|
| v1.0 | 2026-05-29 | 初始版本，黑白灰极简设计 |

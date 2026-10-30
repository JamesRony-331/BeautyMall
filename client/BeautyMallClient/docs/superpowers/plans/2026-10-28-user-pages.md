# 用户端页面实施计划

> 使用 executing-plans 在当前会话完成实现。

**Goal:** 实现已确认的黑白灰美妆杂志风主页、登录注册和个人中心。
**Architecture:** Vue Router 页面、共用布局、会话状态、HTTP 封装和设计变量分离。只操作当前前端目录。
**Tech Stack:** Vue 3、Element Plus、Vite、Node test runner。
**Spec:** ../../design/2026-10-28-user-pages.md

## 约束与审查重点

不修改后端。无用户资料读取接口，不伪造资料；只发送修改字段。处理登录过期、非 JSON 响应、网络失败、密码不一致和不安全回跳地址。所有页面适配手机与键盘。

## Task 1: API 与会话

- [x] tests/account.test.js：先测试表单编码、JSON 更新、错误转换、未知资料保留、站内回跳。
- [x] src/api/account.js、src/lib/account.js、src/stores/session.js：请求和会话封装。
- [x] node --test：验证请求参数、错误和资料差量。

## Task 2: 页面与样式

- [x] 安装 Element Plus，配置 main.js、router/index.js 与 vite.config.js 开发代理。
- [x] App.vue、components/SiteHeader.vue、components/SiteFooter.vue：统一布局。
- [x] views/HomeView.vue、AuthView.vue、ProfileView.vue：主页、认证和个人中心。
- [x] style.css：统一黑白灰变量、响应式和减少动效。
- [x] npm run build：验证编译。

## Task 3: 验证与交付

- [x] 浏览器检查四个页面、移动端溢出和键盘可访问性。
- [x] 验证模拟登录/注册/资料更新/修改密码以及错误路径，真实后端可用时另行验证。
- [x] 写明运行方式、环境变量、接口限制及实际测试结果。

## 实施记录

当前目录不是 Git 仓库，直接在授权前端目录实现，不创建或修改后端工作树。

验证记录：Node 接口测试 7 项通过；生产构建通过。浏览器 5 项端到端场景全部通过，覆盖认证、差量保存、密码修改、过期会话、四个断点、旧头像、键盘、菜单和退出确认；格式检查通过。独立审查发现未修改的历史头像阻止保存，已建立失败回归并修正。后端最新代码已提供 userVO，未修改后端；只读 5050 请求返回 401。

# 美妆灵感管理端

Vue 3 + Vite + Element Plus。统一设计规范见 `docs/admin-design.md`。

## 启动

运行 `npm install` 和 `npm run dev`，默认端口 5174。也可使用项目根目录的一键启动脚本，同时启动客户端和管理端。
`/api` 代理到 `http://localhost:5050`，可通过 `API_PROXY_TARGET` 覆盖。

## 页面范围

- `/products`：静态商品列表，支持搜索、分类/状态筛选、分页和详情。
- `/categories`：静态分类列表，支持搜索、状态筛选、分页和详情。
- `/users`：登录、服务端分页、编辑姓名/邮箱/电话、确认后禁用用户。凭证存储在当前标签页 sessionStorage。

静态数据仅用于展示，不保存到服务器。没有修改后端，登录使用现有账号接口，管理员权限仍由后端决定。

## 验证

运行 `npm test`、`npm run build`、`npm run format:check`。

浏览器测试使用本机 Chrome。先启动开发服务，再运行 `ADMIN_TEST_URL=http://localhost:5174 npm run test:e2e`。
覆盖筛选、分页、详情、移动菜单，以及 375 / 768 / 1440 像素下的页面溢出与运行时错误检查。截图保存于 `docs/previews`。

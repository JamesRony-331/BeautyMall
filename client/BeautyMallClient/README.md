# 美妆灵感 · 用户端

Vue 3 + Vite + Vue Router + Element Plus。黑白灰美妆杂志风，包含主页、登录、注册与个人中心。所有代码变更均在本用户端目录中，未修改后端。

## 运行

```sh
npm install
npm run dev
```

本地访问终端输出的地址（默认 http://localhost:5173）。复制 `.env.example` 为 `.env.local` 可以调整后端地址，默认代理 `http://localhost:5050`。开发请求 `/api/sys-user/*` 会去掉 `/api` 后发送到后端。

```sh
npm run build
npm test
# 先启动 npm run dev；本地需要安装 Google Chrome
npm run test:e2e
```

生产部署：将 `dist/` 发布到静态服务，配置 SPA 路由回退到 `index.html`；将 `/api` 代理到后端且去掉此前缀，或构建时设置 `VITE_API_BASE_URL` 并配置后端允许的跨域策略。Vite 开发代理不会随 dist 发布。

## 页面

- `/`：主页、功能介绍及账号入口；未实现的导购和穿搭功能明确标注“即将开放”。
- `/login`、`/register`：邮箱密码认证，字段校验，错误和加载反馈。
- `/profile`：资料编辑；`/profile?tab=security`：修改密码。

## 已适配的接口

| 方法 | 地址                 | 请求体                                       |
| ---- | -------------------- | -------------------------------------------- |
| POST | `/sys-user/reg`      | 表单编码 email/password                      |
| POST | `/sys-user/login`    | 表单编码 email/password                      |
| PUT  | `/sys-user/info`     | JSON，仅修改的 realName/phone/avatar         |
| PUT  | `/sys-user/password` | JSON oldPassword/newPassword/confirmPassword |

更新请求携带 `Authorization: Bearer <token>`。按 `code === 200` 判断业务成功，优先显示 `info` 错误信息。Token 和当前会话资料保存于 sessionStorage，不保存密码。修改密码后清除前端会话并重新登录；退出只清除本地会话，不宣称撤销服务端 Token。

## 后端现状与边界

最新磁盘后端登录代码已填充 `data.userVO`，前端会回显姓名、电话、邮箱和头像地址。仍无独立查询当前用户资料的 GET 接口，刷新使用本次登录会话缓存；再次登录时获取最新登录返回资料。前端同时兼容旧版 `userVO` 为空的情况：显示实际输入邮箱，并明确提示只能编辑、缓存本次修改的字段，不把缓存冒充服务端查询结果。

个人资料已移除头像地址输入及编辑区域，侧栏仍显示已有安全头像或文字头像，不提交头像字段。新增个人地址管理、我的收藏、我的订单菜单；订单支持全部订单、待付款、待发货、待评价、退款、售后分类，选择状态保存于 URL，刷新后恢复。目前缺少相应业务接口，三个新页面显示服务准备中，不提供假数据或本地假保存。

## 设计与素材

规范：`docs/design/2026-10-28-user-pages.md`。
计划及验证记录：`docs/superpowers/plans/2026-10-28-user-pages.md`。
预览截图：`docs/previews/`（个人中心截图使用测试账号和模拟接口）。
`public/images/beauty-editorial.jpg` 为本项目生成的无品牌美妆静物图，非真实商品目录，不复用 LV、Apple、Chanel 的商标或产品图。原始 PNG 同目录保留。

## 验证范围

Node 测试覆盖请求编码、Bearer Token、差量资料、错误处理和安全回跳。Playwright 使用模拟接口覆盖认证、资料保存、修改密码、过期处理和 375 / 768 / 1024 / 1440px 布局。本地后端 5050 端口可连接，未登录资料请求返回 401；没有使用真实账号进行读写，认证和资料写入流程验证使用模拟接口，未完成真实数据库联调。

## 商品列表（新增）

顶部及手机导航的“全部商品”进入 `/products`。支持名称/分类搜索、每页 8/12/24 件、翻页、空结果及 URL 状态恢复。数据源为 `src/data/catalog.js` 中明确标注的 24 件示例商品；当前没有商品后端接口，图片为统一示意图，不包含价格、库存、购买或伪造的服务端请求。后续接入真实 API 时替换本地查询层 `src/lib/catalog.js`。

# BeautyMall 美妆商城（未完成）

Vue 3 + Element Plus 客户端与管理端，Spring Boot 服务端。

## 目录

- `client/BeautyMallClient`：客户端，默认 5173。
- `admin/BeautyMall`：管理端，默认 5174。
- `service/BeautyMall`：服务端，默认 5050。
- `scripts/start-frontends.mjs`：一键启动两端前端。

## 启动

前端运行项目根目录的 `一键启动.command`，或分别进入前端目录执行 `npm ci`、`npm run dev`。

服务端使用 Java 21 和 MySQL。在本机创建 BeautyMall 数据库及对应表，并设置环境变量 `DB_PASSWORD`、`JWT_SECRET`，可选 `DB_USERNAME`（默认 root）、`DB_URL`。JWT_SECRET 应为足够长的随机 Base64 密钥（例如使用 `openssl rand -base64 48` 生成）。或复制 `.env.example` 为 `.env` 填写配置后执行 `./start-service.sh`，脚本自动载入环境变量。IDE 启动时也需配置这些环境变量。

本仓库不包含数据库备份、真实账号或密码。本机敏感配置已移入被忽略的 `.env`；仓库只包含 `.env.example`。数据库建表请按实体和 Mapper 准备，本次未上传真实数据库数据。

商品与分类管理目前为静态展示；用户管理接入现有服务接口。权限实现状态以服务端代码为准。

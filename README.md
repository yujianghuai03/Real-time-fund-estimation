# 基金实时预估系统

这是一个基于 Spring Boot / Spring Cloud + Vue3 的基金实时预估系统。后端支持默认 Boot 聚合启动，也可以通过 Maven profile 切换 Cloud 微服务模式；前端使用 Vue3 + Vite + Element Plus 构建。

## 后端模块

- `yujianghuai-common`：统一返回 `R`、全局异常、多租户 `TENANT-ID`、MyBatis-Plus、RedisTemplate、Feign、MQ 基础封装。
- `yujianghuai-fund`：基金业务模块，包含 `yujianghuai-fund-api / yujianghuai-fund-biz` 分层。
- `yujianghuai-auth`：认证模块，参考 `cfm-auth` 风格实现 OAuth2 Authorization Server、JWT、JWK、密码模式扩展、token 管理端点。
- `yujianghuai-admin`：权限后台模块，提供用户、角色、菜单、角色授权等 RBAC 增删改查接口。
- `yujianghuai-boot`：默认 Boot 聚合启动模块，一个进程整合业务和认证能力。
- `yujianghuai-nacos`：Cloud 模式下的 Nacos 管理模块。
- `yujianghuai-gateway`：Cloud 模式下的 Spring Cloud Gateway，使用 JWKS 校验 JWT。
- `yujianghuai-ui`：Vue3 + Vite + Element Plus 前端模块。

## 数据库

业务库脚本：

```text
db/yujianghuai-sys.sql
```

脚本会创建 `yujianghuai` 数据库，并初始化 RBAC 权限模型、租户、部门、岗位、用户、角色、菜单、按钮、API 权限、OAuth2 客户端、字典、系统参数、登录日志、操作日志和演示业务表。

本地导入：

```powershell
mysql --default-character-set=utf8mb4 -uroot -p123456 -e "source F:/yujianghuai/yujianghuai/db/yujianghuai-sys.sql"
```

默认账号：

- 用户：`admin / 123456`
- 租户：`TENANT-ID=1`（默认租户编码为 `demo`）
- 角色：`ADMIN`
- OAuth2 客户端：`yujianghuai-client / yujianghuai-secret`

## 后端启动

默认 Boot 模式测试：

```powershell
mvn -s .mvn\local-settings.xml test
```

默认 Boot 单体启动：

```powershell
mvn -s .mvn\local-settings.xml -pl yujianghuai-boot spring-boot:run
```

Cloud 模式测试：

```powershell
mvn -s .mvn\local-settings.xml -Pcloud test
```

Cloud 启动顺序：

```powershell
mvn -s .mvn\local-settings.xml -Pcloud -pl yujianghuai-nacos spring-boot:run
mvn -s .mvn\local-settings.xml -Pcloud -pl yujianghuai-auth spring-boot:run
mvn -s .mvn\local-settings.xml -Pcloud -pl yujianghuai-admin spring-boot:run
mvn -s .mvn\local-settings.xml -Pcloud -pl yujianghuai-fund/yujianghuai-fund-biz spring-boot:run -Dspring-boot.run.profiles=cloud
mvn -s .mvn\local-settings.xml -Pcloud -pl yujianghuai-gateway spring-boot:run
```

OAuth2 密码模式：

```http
POST http://127.0.0.1:8080/oauth2/token
Authorization: Basic base64(yujianghuai-client:yujianghuai-secret)
Content-Type: application/x-www-form-urlencoded

grant_type=password&username=admin&password=123456&scope=openid profile api.read api.write&TENANT-ID=1
```

业务接口：

```http
GET http://127.0.0.1:8080/api/demo/ping
Authorization: Bearer <accessToken>
TENANT-ID: 1
```

权限后台接口：

```http
GET http://127.0.0.1:8080/admin-api/users
GET http://127.0.0.1:8080/admin-api/roles
GET http://127.0.0.1:8080/admin-api/menus
Authorization: Bearer <accessToken>
TENANT-ID: 1
```

## 前端模块

前端根目录：

```text
yujianghuai-ui
```

结构：

- 当前为单应用 Vue3 + Vite + Element Plus 前端工程。
- 后续可按业务需要拆分为前台门户、后台管理和共享包。

启动：

```powershell
cd yujianghuai-ui
npm install
npm run dev
```

默认地址：

- 前端页面：`http://127.0.0.1:5173`

构建：

```powershell
cd yujianghuai-ui
npm run build
```

# yujianghuai-ui

Vue3 + Vite + Element Plus 前端模块，按应用拆分：

- `apps/portal`：前台页面，面向普通用户和访客。
- `apps/admin`：后台管理，面向管理员，内置 RBAC 权限菜单管理页面。
- `packages/shared`：共享类型、菜单权限数据和 HTTP 客户端。

## 启动

```powershell
npm install
npm run dev:portal
npm run dev:admin
```

默认端口：

- 前台：`http://127.0.0.1:5173`
- 后台：`http://127.0.0.1:5174`

## 构建

```powershell
npm run build
```

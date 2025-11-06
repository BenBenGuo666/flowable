# 前端反向代理配置说明

## 配置概述

为了解决开发环境的跨域问题，项目使用 Vite 的反向代理功能，将前端请求转发到后端服务。

---

## 配置文件

### 1. vite.config.js

```javascript
server: {
  port: 5173,
  host: '0.0.0.0',
  open: true,
  proxy: {
    // 所有以这些路径开头的请求都会被代理到 http://localhost:8080
    '/auth': { target: 'http://localhost:8080', changeOrigin: true },
    '/user': { target: 'http://localhost:8080', changeOrigin: true },
    '/role': { target: 'http://localhost:8080', changeOrigin: true },
    '/permission': { target: 'http://localhost:8080', changeOrigin: true },
    // ... 更多路径
  }
}
```

### 2. request.js

```javascript
const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '',
  timeout: 30000
})
```

### 3. 环境变量

**开发环境** (`.env.development`):
```bash
VITE_API_BASE_URL=
```
- 使用相对路径，请求会被 Vite 代理转发

**生产环境** (`.env.production`):
```bash
VITE_API_BASE_URL=/api
```
- 直接访问后端 API

---

## 工作原理

### 开发环境

1. 前端运行在: `http://localhost:5173`
2. 后端运行在: `http://localhost:8080`
3. 前端发起请求: `/auth/login`
4. Vite 代理拦截并转发到: `http://localhost:8080/auth/login`
5. 响应返回给前端

```
浏览器 → http://localhost:5173/auth/login
       ↓ (Vite 代理)
       → http://localhost:8080/auth/login
       ↓
       ← 响应返回
```

### 生产环境

生产环境需要在 Nginx 或其他 Web 服务器配置反向代理：

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # 前端静态资源
    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API 代理到后端
    location /api/ {
        proxy_pass http://localhost:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

---

## 代理路径列表

| 前端路径 | 后端地址 | 说明 |
|---------|---------|------|
| `/auth/*` | `http://localhost:8080/auth/*` | 认证接口 |
| `/user/*` | `http://localhost:8080/user/*` | 用户管理 |
| `/role/*` | `http://localhost:8080/role/*` | 角色管理 |
| `/permission/*` | `http://localhost:8080/permission/*` | 权限管理 |
| `/process-definition/*` | `http://localhost:8080/process-definition/*` | 流程定义 |
| `/process-instance/*` | `http://localhost:8080/process-instance/*` | 流程实例 |
| `/task/*` | `http://localhost:8080/task/*` | 任务管理 |
| `/leave/*` | `http://localhost:8080/leave/*` | 请假流程 |
| `/init/*` | `http://localhost:8080/init/*` | 初始化接口 |

---

## 使用方法

### 启动开发服务器

```bash
cd frontend
npm run dev
```

服务器将在 `http://localhost:5173` 启动，所有 API 请求会自动代理到 `http://localhost:8080`。

### 构建生产版本

```bash
npm run build
```

构建后的文件在 `dist` 目录，部署时需要配置 Nginx 反向代理。

---

## 常见问题

### 1. 代理不生效？

**检查项**:
- 后端服务是否启动在 `http://localhost:8080`
- Vite 开发服务器是否重启
- 浏览器控制台是否有错误

**解决方法**:
```bash
# 重启前端服务
npm run dev
```

### 2. 请求返回 404？

**原因**: 后端接口路径不匹配

**解决方法**:
- 检查后端 Controller 的 `@RequestMapping` 路径
- 检查前端 API 调用的路径是否正确

### 3. CORS 错误？

**原因**: 后端未配置 CORS

**解决方法**:
后端已在 `SecurityConfig.java` 中配置 CORS，允许以下域名：
- `http://localhost:5173`
- `http://localhost:3000`

如需添加其他域名，修改 `SecurityConfig.java`:
```java
configuration.setAllowedOrigins(Arrays.asList(
    "http://localhost:5173",
    "http://localhost:3000",
    "http://your-domain.com"
));
```

### 4. 超时错误？

**原因**: 请求超时（默认 30 秒）

**解决方法**:
修改 `request.js` 中的 `timeout`:
```javascript
const request = axios.create({
  timeout: 60000  // 增加到 60 秒
})
```

---

## 测试代理

### 1. 测试登录接口

```javascript
// 前端发起请求
fetch('/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ username: 'admin', password: '123456' })
})
```

### 2. 查看代理日志

Vite 会在控制台显示代理日志：
```
vite v7.1.7 dev server running at:

➜  Local:   http://localhost:5173/
➜  Network: http://192.168.1.100:5173/

/auth/login → http://localhost:8080/auth/login
```

---

## 高级配置

### 1. 修改后端地址

编辑 `vite.config.js`:
```javascript
proxy: {
  '/auth': {
    target: 'http://your-backend-server:8080',  // 修改这里
    changeOrigin: true
  }
}
```

### 2. 添加请求日志

编辑 `vite.config.js`:
```javascript
proxy: {
  '/auth': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    configure: (proxy, options) => {
      proxy.on('proxyReq', (proxyReq, req, res) => {
        console.log('Proxying:', req.method, req.url)
      })
    }
  }
}
```

### 3. 修改请求路径

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

这样 `/api/auth/login` 会被转发到 `http://localhost:8080/auth/login`

---

## 总结

✅ **开发环境**: Vite 代理自动处理跨域，无需额外配置
✅ **生产环境**: 需要 Nginx 配置反向代理
✅ **安全**: 后端已配置 CORS 和 CSRF 保护
✅ **灵活**: 支持环境变量配置不同后端地址

---

**最后更新**: 2025-11-06

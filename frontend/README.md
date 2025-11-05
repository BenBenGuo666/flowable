# Flowable 工作流前端项目

基于 Vue 3 + Vite + Naive UI 构建的轻量化工作流管理系统前端，采用苹果风格设计。

## 技术栈

- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **UI 组件库**: Naive UI
- **路由**: Vue Router
- **HTTP 客户端**: Axios
- **图表**: Chart.js
- **流程图**: bpmn-js

## 项目特色

### 苹果风格设计
- 苹果官方色系（主色 #007AFF）
- 12px/8px 圆角设计
- 毛玻璃效果
- 系统字体 (-apple-system)
- 自动适配深色模式

### 功能模块
- 请假申请
- 我的请假列表
- 待审批管理
- 数据可视化
- 流程图展示

## 项目结构

```
frontend/
├── src/
│   ├── api/              # API 服务层
│   ├── components/       # 公共组件
│   ├── config/           # 配置文件
│   ├── router/           # 路由配置
│   ├── styles/           # 全局样式
│   ├── views/            # 页面组件
│   ├── App.vue           # 主应用
│   └── main.js           # 入口文件
├── .env                  # 开发环境配置
├── vite.config.js        # Vite 配置
└── package.json
```

## 快速开始

### 安装依赖

```bash
npm install
```

### 开发模式

```bash
npm run dev
```

项目将在 http://localhost:3000 启动

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 环境配置

### 开发环境
```
VITE_API_BASE_URL=http://localhost:8080
```

### 生产环境
```
VITE_API_BASE_URL=/api
```

## 主要页面

- 首页 (/)
- 工作台 (/dashboard)
- 发起请假 (/leave/apply)
- 我的请假 (/leave/list)
- 待我审批 (/leave/pending)
- 请假详情 (/leave/detail/:id)

## 设计规范

### 色彩方案
- 主色：#007AFF (苹果蓝)
- 成功：#34C759 (苹果绿)
- 警告：#FF9500 (苹果橙)
- 错误：#FF3B30 (苹果红)

### 圆角规范
- 大元素：12px
- 小元素：8px

# 数据仓库前端 (Vue 3 + TypeScript)

## 项目概述

这是 E-commerce 数据仓库项目的前端应用，提供了实时数据分析和仓库监控的可视化界面。

**核心技术栈**:

- Vue 3 (Composition API)
- TypeScript
- Vite (构建工具)
- ECharts (数据可视化)
- Ant Design Vue (UI 组件库)
- Axios (HTTP 客户端)

## 项目结构

```
frontend/
├── src/
│   ├── App.vue                           # 主应用组件
│   ├── main.ts                           # 入口文件
│   ├── style.css                         # 全局样式
│   ├── api/
│   │   └── analytics.ts                  # API 服务层
│   ├── router/
│   │   └── index.ts                      # 路由配置
│   ├── views/
│   │   ├── Dashboard.vue                 # 仪表盘
│   │   ├── SalesAnalytics.vue            # 销售分析
│   │   ├── ProductInsights.vue           # 产品洞察
│   │   └── SyncMonitor.vue               # 同步监控
│   └── stores/                           # Pinia 状态管理 (待扩展)
├── index.html
├── package.json
├── vite.config.ts
├── tsconfig.json
├── Dockerfile
├── nginx.conf
└── README.md
```

## 功能模块

### 1. Dashboard (仪表盘)

**展示内容**:

- 📈 总销售额统计
- 📦 总订单数量
- ⭐ 平均商品评分
- 🔄 ETL 同步状态

**交互功能**:

- 销售趋势折线图 (最近7天)
- 分类销售占比饼图
- 发送测试订单按钮 (用于测试 ETL 流程)

### 2. Sales Analytics (销售分析)

**展示内容**:

- 按分类的销售数据
- 销售柱状图 + 订单折线图
- 详细数据表格

**交互功能**:

- 日期范围选择
- 数据加载和刷新

### 3. Product Insights (产品洞察)

**展示内容**:

- 热门商品排名
- 商品评分星级显示
- 商品销售数据

**交互功能**:

- 可配置返回商品数量
- 动态加载数据

### 4. Sync Monitor (同步监控)

**展示内容**:

- 总同步数据量
- 成功率统计
- 最后同步时间
- 同步事件日志 (实时更新)

**交互功能**:

- 手动刷新同步状态
- 自动刷新 (每30秒)
- 按事件类型、数据源筛选

## API 接口

所有 API 调用通过 `src/api/analytics.ts` 进行:

```typescript
// 获取按分类的销售统计
analyticsApi.getSalesByCategory(startDate, endDate);

// 获取热门商品
analyticsApi.getTopRatedProducts(limit);

// 获取同步统计
analyticsApi.getSyncStatistics();

// 发送测试订单
analyticsApi.sendTestOrder(orderData);

// 健康检查
analyticsApi.health();
```

**API 基础 URL**: `http://localhost:8080` (开发环境) 或 `VITE_API_URL` (生产环境)

## 配置说明

### vite.config.ts

```typescript
server: {
  proxy: {
    '/api': 'http://localhost:8080'  // API 代理
  }
}
```

### tsconfig.json

- Target: ES2020
- Module: ESNext
- Strict mode 启用
- Path alias: `@` → `./src`

### package.json 脚本

```bash
npm run dev        # 开发服务器 (localhost:5173)
npm run build      # 构建生产版本
npm run preview    # 预览构建结果
npm run lint       # 执行 ESLint 检查
npm run type-check # TypeScript 类型检查
```

## 开发指南

### 环境变量

在 `.env` 文件中配置:

```env
VITE_API_URL=http://localhost:8080
```

### 开发服务器

```bash
npm install
npm run dev
```

访问 `http://localhost:5173`

### 构建生产版本

```bash
npm run build
```

输出文件在 `dist/` 目录

### Docker 方式构建

```bash
docker build -f frontend/Dockerfile -t warehouse-frontend:latest ./frontend
```

## 页面详解

### Dashboard 交互流程

1. **初始化**
   - 加载销售趋势数据
   - 初始化 ECharts 图表
   - 获取 ETL 同步状态

2. **发送测试订单**
   - 点击 "Send Test Order" 按钮
   - 生成随机订单数据
   - 通过 API 发送到后端
   - Kafka 接收并触发 ETL 流程
   - 实时更新仓库数据

3. **数据刷新**
   - 点击 "Refresh Data" 按钮
   - 重新获取同步统计
   - 更新界面显示

### Sales Analytics 数据流

```
Date Range Selection → Load Data → API Request → Chart Update → Table Display
```

### Sync Monitor 自动更新

```
Component Mount → 初始获取数据
                ↓
           定时器每30秒
                ↓
         refreshSyncStatus()
                ↓
           更新表格和统计
```

## 性能优化

### 1. 路由懒加载

```typescript
const Dashboard = () => import("../views/Dashboard.vue");
const SalesAnalytics = () => import("../views/SalesAnalytics.vue");
```

### 2. 图表优化

- 使用 ECharts 的 Canvas 渲染
- 响应式窗口大小变化
- 按需加载图表数据

### 3. 生产构建优化

- 代码分割 (Route + Vendor)
- Tree-shaking 移除未使用代码
- Gzip 压缩

### 4. Nginx 缓存策略

```nginx
# 平静资源缓存 1 年
location ~* \.(js|css|png|jpg|)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}
```

## 扩展计划

- [ ] 实现 Pinia 状态管理 (全局数据共享)
- [ ] 添加用户认证和授权
- [ ] WebSocket 实时数据推送
- [ ] 数据导出功能 (CSV, Excel)
- [ ] 自定义报表生成器
- [ ] 深色模式支持
- [ ] 多语言支持 (i18n)
- [ ] 离线缓存 (Service Worker)

## 故障排查

### 无法连接到后端 API

```
错误: "Failed to fetch from /api/analytics"
解决:
1. 检查后端服务是否运行 (docker-compose logs backend)
2. 确认 API_URL 配置正确
3. 检查 CORS 设置
```

### 图表不显示

```
原因: DOM 元素未正确初始化
解决:
1. 确保 ref 元素有明确的宽高
2. 检查浏览器控制台错误
3. 尝试刷新页面
```

### 数据不更新

```
原因: API 响应格式不匹配
解决:
1. 检查 API 返回数据结构
2. 确认未启用浏览器缓存
3. 检查 Network 标签查看实际响应
```

## 监控命令

```bash
# 检查前端容器日志
docker logs warehouse-frontend

# 查看 nginx 访问日志
docker exec warehouse-frontend tail -f /var/log/nginx/access.log

# 验证前端健康状态
curl http://localhost:5173/health

# 测试 API 代理
curl http://localhost:5173/api/analytics/health
```

## 版本信息

- Vue 3.3.4
- TypeScript 5.2.2
- Vite 4.4.11
- ECharts 5.4.3
- Ant Design Vue 4.2.3

## 更新日志

### v1.0.0 (2026-03-30)

- ✅ 完整的 Vue3 Composition API 实现
- ✅ TypeScript 全覆盖
- ✅ 4 个主要页面 (Dashboard, Sales, Products, Sync)
- ✅ ECharts 数据可视化
- ✅ Ant Design Vue UI 组件
- ✅ API 集成和错误处理
- ✅ 响应式设计
- ✅ Docker 支持
- ✅ Nginx 反向代理配置

---

**作者**: University of Windsor eCommerce Team  
**维护**: Frontend & UX Team  
**最后更新**: 2026-03-30

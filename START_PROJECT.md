# 项目启动指南

## 📚 项目结构

```
ecommerce_data_warehouse/
├── backend/                          # Java Spring Boot 后端
│   ├── pom.xml                       # Maven 配置
│   ├── src/main/java/com/example/   # Java 源代码
│   ├── src/main/resources/          # 配置文件
│   └── sql/                          # 数据库 SQL 脚本
│
├── frontend/                         # Vue3 + Vite 前端
│   ├── package.json                  # NPM 依赖
│   ├── vite.config.js               # Vite 配置
│   ├── index.html                   # HTML 入口
│   └── src/                         # Vue 源代码
│
├── DATABASE_DESIGN.md               # 数据库设计文档
├── JAVA_ARCHITECTURE.md             # Java 后端架构文档
├── FRONTEND_DESIGN.md               # 前端设计文档
├── QUICK_START.md                   # 快速开始指南
└── PROJECT_SUMMARY.md               # 项目总结
```

---

## 🚀 快速启动（5步）

### Step 1️⃣: 初始化MySQL数据库

**Linux/Mac:**

```bash
mysql -u root -p < backend/sql/source_db.sql
mysql -u root -p < backend/sql/warehouse_db.sql
mysql -u root -p < backend/sql/sample_data.sql
```

**Windows:**

```bash
mysql -u root -p < backend\sql\source_db.sql
mysql -u root -p < backend\sql\warehouse_db.sql
mysql -u root -p < backend\sql\sample_data.sql
```

### Step 2️⃣: 启动Java后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动

**Swagger API 文档**: http://localhost:8080/swagger-ui.html

### Step 3️⃣: 安装前端依赖

```bash
cd frontend
npm install
```

### Step 4️⃣: 启动前端开发服务器

```bash
npm run dev
```

前端将在 `http://localhost:5173` 启动

### Step 5️⃣: 打开浏览器查看仪表板

访问 **http://localhost:5173**

---

## 📋 API 端点列表

### 商品分析 API

| 方法 | 端点                         | 描述                       |
| ---- | ---------------------------- | -------------------------- |
| GET  | `/api/products/top-sales`    | 获取热销商品（按销量）     |
| GET  | `/api/products/top-reviews`  | 获取热销商品（按评论评分） |
| GET  | `/api/products/combined-top` | 获取综合热销商品           |

### 销售分析 API

| 方法 | 端点                     | 描述                 |
| ---- | ------------------------ | -------------------- |
| GET  | `/api/sales/by-season`   | 获取季节销售分析     |
| GET  | `/api/sales/by-category` | 获取按类别的销售分析 |

### 综合分析 API

| 方法 | 端点                         | 描述             |
| ---- | ---------------------------- | ---------------- |
| GET  | `/api/analytics/aov`         | 获取平均订单价值 |
| GET  | `/api/analytics/return-rate` | 获取退货率       |
| GET  | `/api/analytics/daily-kpi`   | 获取日KPI        |

---

## 🔧 配置说明

### 数据库连接参数

**文件**: `backend/src/main/resources/application.yml`

```yaml
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/ecommerce_source
      username: root
      password: root

    warehouse:
      url: jdbc:mysql://localhost:3306/ecommerce_warehouse
      username: root
      password: root
```

### 前端 API 代理

**文件**: `frontend/vite.config.js`

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
  }
}
```

---

## 🐛 常见问题排查

### Q1: MySQL 连接失败

**错误**: `java.sql.SQLException: Unable to load authentication plugin`

**解决方案**:

```bash
# 确保 MySQL 已启动
mysql -u root -p -e "SELECT 1"

# 检查数据库是否已创建
mysql -u root -p -e "SHOW DATABASES"
```

### Q2: 前端无法连接后端 API

**错误**: `CORS error`

**解决方案**:

- 确保后端运行在 `http://localhost:8080`
- 检查 Vite 代理配置
- 在浏览器控制台查看 Network 标签

### Q3: NPM 依赖安装失败

**解决方案**:

```bash
# 清除缓存后重新安装
npm cache clean --force
npm install
```

---

## 📊 测试 API

### 使用 Swagger UI

1. 启动后端: `mvn spring-boot:run`
2. 打开: http://localhost:8080/swagger-ui.html
3. 点击 API 端点进行测试

### 使用 curl

```bash
# 获取热销商品（按销量）
curl http://localhost:8080/api/products/top-sales

# 获取热销商品（按评分）
curl http://localhost:8080/api/products/top-reviews

# 获取综合热销商品
curl http://localhost:8080/api/products/combined-top
```

### 使用 Postman

1. 导入 API 集：File → New → Import
2. 输入 URL: `http://localhost:8080/v2/api-docs`
3. 导入后即可使用

---

## 📦 打包部署

### 构建后端 JAR

```bash
cd backend
mvn clean package
java -jar target/ecommerce-warehouse-demo-1.0.0.jar
```

### 构建前端

```bash
cd frontend
npm run build
# 生成的文件在 dist/ 目录
```

---

## 📝 开发检查清单

- [ ] MySQL 数据库已初始化
- [ ] 后端可以成功连接两个数据库
- [ ] 后端在 8080 端口正常运行
- [ ] Swagger 文档可以访问
- [ ] 前端依赖已安装
- [ ] 前端在 5173 端口正常运行
- [ ] 前端能正确加载 5 个图表
- [ ] 前端能正确调用后端 API

---

## 🌐 生产环境部署

### Docker 部署

**docker-compose.yml** 已配置。运行以下命令：

```bash
docker-compose up -d
```

### 手动部署

1. 部署 MySQL 数据库
2. 构建并运行 Java JAR
3. 构建并部署前端静态文件到 Nginx/Apache

---

## 📞 获取帮助

- 查看详细文档：[DOCUMENTATION_INDEX.md](DOCUMENTATION_INDEX.md)
- 查看快速开始指南：[QUICK_START.md](QUICK_START.md)
- 查看项目总结：[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)

---

**祝你使用愉快！** 🎉

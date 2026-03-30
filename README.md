# 🚀 电商数据仓库 - 多渠道数据融合

> 完整的多渠道电商数据仓库系统 - Docker一键启动，融合App和Web两个渠道数据+分析仓库+Spring Boot后端+Vue前端

[![Docker](https://img.shields.io/badge/Deploy-Docker%20Compose-2496ED?logo=docker)]()
[![Language](https://img.shields.io/badge/Language-Java%2011%2B-orange)]()
[![Database](https://img.shields.io/badge/Database-MySQL%208.0-blue)]()
[![Frontend](https://img.shields.io/badge/Frontend-Vue.js%203-green)]()

---

## 🎯 项目概览

这是一个**完整的多渠道数据融合系统**，支持两个完全不同的业务数据源：

- ✅ **多渠道架构** - ecommerce_source_app (App渠道) + ecommerce_source_web (Web渠道) + ecommerce_warehouse (分析库)
- ✅ **异构数据融合** - App系统使用order_id(INT, yyyy-MM-dd)，Web系统使用order_no(VARCHAR, MM/dd/yyyy)，自动转换和统一
- ✅ **Docker一键部署** - 一键启动所有服务（MySQL、Java后端、Vue前端）
- ✅ **数据分析** - 2大核心分析场景：商品销量分析 + 评价排行分析
- ✅ **完整代码** - 从SQL到ETL到API到UI，生产就绪

### ⚡ 快速启动

```bash
# 一键启动所有服务
docker-compose up -d

# 访问应用
前端仪表板: http://localhost:5173
API文档:   http://localhost:8080/swagger-ui.html
MySQL:     localhost:3306 (root/root)
```

### 核心功能

| #   | 功能         | 说明                                                   |
| --- | ------------ | ------------------------------------------------------ |
| 1️⃣  | 商品销量分析 | 按商品分类和时间维度统计销量和销售额（支持热力图展示） |
| 2️⃣  | Top商品排行  | 按平均评分和评论数统计Top5商品排行榜                   |
| 🔄  | 异构数据融合 | 自动处理App/Web数据格式差异（字段名、日期格式）        |
| 📊  | 可视化仪表板 | 交互式图表展示多维度分析结果                           |

---

## 📁 项目结构

```
ecommerce_data_warehouse/
├── 🐳 docker-compose.yml        # Docker编排配置
├── 📖 DOCKER_QUICKSTART.md      # Docker启动指南⭐
├── 📖 PROJECT_OVERVIEW.md       # 项目详细说明⭐
├── 📖 DATABASE_DESIGN.md        # 数据库设计⭐
│
├── backend/                     # Java后端 (Spring Boot)
│   ├── Dockerfile
│   ├── pom.xml
│   ├── src/main/
│   │   ├── java/com/warehouse/
│   │   │   ├── Application.java
│   │   │   ├── config/DataSourceConfig.java      # 多数据源配置
│   │   │   ├── entity/              # 实体类 (5个)
│   │   │   ├── dto/                 # DTO类 (4个)
│   │   │   ├── mapper/              # Mapper (3个)
│   │   │   ├── service/             # 服务 (1个)
│   │   │   └── controller/          # 控制器 (1个)
│   │   └── resources/
│   │       └── application.yml      # 多数据源配置
│   └── sql/
       ├── source_app.sql             # App数据源DDL
       ├── source_web.sql             # Web数据源DDL
       ├── warehouse_db.sql           # 分析库DDL
       └── sample_data.sql            # 示例数据
│
├── frontend/                    # Vue前端
│   ├── Dockerfile
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   ├── src/
│   │   ├── main.js
│   │   ├── router/
│   │   ├── api/client.js
│   │   ├── App.vue
│   │   └── components/         # 5个图表组件
│   └── dist/
│
├── .git/
├── .gitignore
└── docs/                       # 其他文档
```

├── .git/
├── .gitignore
└── docs/ # 其他文档

````

---

## 🛠️ 技术栈

### 后端 (Backend)
- **框架**: Spring Boot 2.7.0
- **ORM**: MyBatis Plus 3.5.1
- **多数据源**: DataSource路由 (3个库)
- **编译**: Maven 3.8.1
- **API文档**: Swagger 3.0

### 前端 (Frontend)
- **框架**: Vue.js 3.3
- **构建**: Vite
- **UI库**: Element Plus
- **图表**: ECharts

### 基础设施
- **容器**: Docker & Docker Compose
- **数据库**: MySQL 8.0
- **编排**: Docker Compose

---

## 🚀 快速开始 (5分钟)

```bash
# 1. 启动所有服务
docker-compose up -d

# 2. 查看状态
docker-compose ps

# 3. 访问应用
# 前端: http://localhost:5173
# API: http://localhost:8080/swagger-ui.html
# MySQL: localhost:3306 (root/root)
````

**✅ 第一次启动会自动**:

- 创建3个MySQL数据库 (ecommerce_source_app, ecommerce_source_web, ecommerce_warehouse)
- 执行SQL初始化脚本
- 插入App/Web两个渠道的示例数据
- 启动Spring Boot后端
- 启动Vue前端

---

## 📊 系统架构

```
┌──────────────────────────────────────────────┐
│         Docker Compose Network               │
├──────────────────────────────────────────────┤
│                                              │
│  ┌──────────────┐  ┌──────────┐  ┌────────┐│
│  │    MySQL     │  │ backend  │  │ Vue UI ││
│  │   3306       │  │  8080    │  │ 5173   ││
│  │              │  │          │  │        ││
│  │  App源 ┐     │  │ Spring   │◄─┤ Charts ││
│  │  Web源 ├──────┤ Boot      │  │        ││
│  │ Warehouse┘   │  │   ETL    │  │ Tables ││
│  │              │  │ Swagger  │  │        ││
│  └──────────────┘  └──────────┘  └────────┘│
│                                              │
└──────────────────────────────────────────────┘
```

---

## 📚 文档导航

| 文档                                         | 推荐人群         |
| -------------------------------------------- | ---------------- |
| [DOCKER_QUICKSTART.md](DOCKER_QUICKSTART.md) | 所有人⭐         |
| [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md)   | 开发者           |
| [DATABASE_DESIGN.md](DATABASE_DESIGN.md)     | DBA/数据库工程师 |
| [JAVA_ARCHITECTURE.md](JAVA_ARCHITECTURE.md) | Java开发         |
| [FRONTEND_DESIGN.md](FRONTEND_DESIGN.md)     | 前端开发         |

---

## 🎓 学习路径

### 路径1: 快速体验 (30分钟)

1. 启动Docker: `docker-compose up -d`
2. 访问前端: http://localhost:5173
3. 查看图表和数据

### 路径2: 开发学习 (4小时)

1. 阅读 [DOCKER_QUICKSTART.md](DOCKER_QUICKSTART.md) (20分钟)
2. 阅读 [PROJECT_OVERVIEW.md](PROJECT_OVERVIEW.md) (30分钟)
3. 学习 [DATABASE_DESIGN.md](DATABASE_DESIGN.md) (1小时)
4. 学习 [JAVA_ARCHITECTURE.md](JAVA_ARCHITECTURE.md) (1小时)
5. 学习 [FRONTEND_DESIGN.md](FRONTEND_DESIGN.md) (1小时)

### 路径3: 深度定制 (2-3天)

1. 修改数据库模式
2. 添加新的API端点
3. 创建新的前端图表
4. 优化性能和部署

---

## ❓ 常见问题

### Q: 如何查看MySQL中的数据?

```bash
docker-compose exec mysql mysql -u root -proot
mysql> USE ecommerce_source_app;    # 查看App渠道数据
mysql> SELECT * FROM orders;
mysql> USE ecommerce_source_web;    # 查看Web渠道数据
mysql> SELECT * FROM orders;
```

### Q: 如何查看后端日志?

```bash
docker-compose logs backend -f
```

### Q: 如何重新初始化数据库?

```bash
docker-compose down -v
docker-compose up -d
```

### Q: 如何修改代码并重新部署?

```bash
# 修改代码后...
docker-compose restart backend
docker-compose restart frontend
```

更多常见问题请见: [DOCKER_QUICKSTART.md#常见问题](DOCKER_QUICKSTART.md#常见问题)

---

## 📈 项目统计

│ └── sql/
│ ├── source_db.sql # 业务库建表脚本
│ ├── warehouse_db.sql # 分析库建表脚本
│ └── sample_data.sql # 样本数据
│
├── 📁 frontend/ # Vue前端
│ ├── package.json # npm配置
│ ├── vite.config.js # Vite构建配置
│ ├── src/
│ │ ├── api/ # API调用
│ │ ├── pages/ # 页面组件
│ │ ├── components/ # 重用组件
│ │ │ ├── HotProductChart.vue # 热销商品图表
│ │ │ ├── SeasonSalesChart.vue # 季节销售图表
│ │ │ ├── AOVChart.vue # AOV趋势图表
│ │ │ ├── ReturnRateChart.vue # 退货率图表
│ │ │ └── KPICard.vue # KPI卡片
│ │ ├── App.vue
│ │ └── main.js
│ └── public/
│
├── 📁 docs/ # 文档目录
│ ├── API文档.md
│ ├── 部署说明.md
│ └── 开发指南.md
│
└── 🐳 docker-compose.yml # Docker一键启动

````

---

## 🚀 快速开始 (5分钟)

### 前置要求

- Java 11+
- Node.js 16+
- MySQL 8.0+

### Step 1️⃣: 创建数据库

```bash
mysql -u root -p
CREATE DATABASE ecommerce_source CHARACTER SET utf8mb4;
CREATE DATABASE ecommerce_warehouse CHARACTER SET utf8mb4;
````

### Step 2️⃣: 执行建表脚本

```bash
mysql -u root -p ecommerce_source < backend/sql/source_db.sql
mysql -u root -p ecommerce_warehouse < backend/sql/warehouse_db.sql
mysql -u root -p ecommerce_source < backend/sql/sample_data.sql
```

### Step 3️⃣: 启动Java后端

```bash
cd backend
mvn spring-boot:run
```

✅ 后端启动: http://localhost:8080  
📖 API文档: http://localhost:8080/swagger-ui.html

### Step 4️⃣: 启动Vue前端

```bash
cd frontend
npm install
npm run dev
```

✅ 前端启动: http://localhost:5173

### Step 5️⃣: 打开浏览器查看仪表板

访问 **http://localhost:5173** 查看所有分析结果！

---

## 📊 API 端点

### 商品分析

```
GET /api/products/top-sales          # 热销商品（按销量TOP10）
GET /api/products/top-reviews        # 热销商品（按评分TOP10）
GET /api/products/combined-top       # 热销商品（综合评分TOP10）
```

### 销售分析

```
GET /api/sales/by-season             # 季节销售分析
GET /api/sales/by-category           # 品类销售统计
```

### 分析指标

```
GET /api/analytics/aov               # 平均订单价值
GET /api/analytics/return-rate       # 商品退货率
GET /api/analytics/daily-kpi         # 每日KPI统计
```

### 示例请求

```bash
# 获取热销商品
curl http://localhost:8080/api/products/top-sales?limit=10

# 获取季节销售
curl http://localhost:8080/api/sales/by-season?year=2024

# 获取AOV分析
curl http://localhost:8080/api/analytics/aov?dimension=day
```

---

## 🗄️ 数据库架构

### 业务库 (ecommerce_source)

```
6张表：
├── users             # 用户信息
├── products          # 商品信息
├── orders            # 订单信息
├── order_items       # 订单明细
├── product_reviews   # 商品评论
└── returns           # 退货信息
```

### 分析库 (ecommerce_warehouse)

```
5张表：
├── fact_sales            # 销售事实表
├── dim_product_analysis  # 商品分析维度
├── fact_sales_by_season  # 季节销售事实
├── fact_returns          # 退货事实表
└── kpi_daily             # 日KPI汇总
```

---

## 📈 前端可视化

### 仪表板包含：

| 组件               | 类型       | 说明                        |
| ------------------ | ---------- | --------------------------- |
| **KPI卡片**        | 数值展示   | 销售额、订单数、AOV、退货率 |
| **热销商品(销量)** | 柱状图     | TOP 10商品销量              |
| **热销商品(评分)** | 柱状图     | TOP 10商品评分              |
| **季节销售**       | 堆积柱状图 | 类别×季节销量对比           |
| **AOV趋势**        | 折线图     | 30天AOV变化趋势             |
| **退货率**         | 饼图       | 类别退货率分布              |

---

## ☕ 技术栈

### 后端

```yaml
Framework: Spring Boot 2.7.0
ORM: MyBatis Plus 3.5.1
Database: MySQL 8.0
DataSource: Dynamic DataSource (多库支持)
API文档: Swagger 2
Serialization: Fastjson
```

### 前端

```yaml
Framework: Vue.js 3
Build Tool: Vite 4
UI Framework: Element Plus
Charts: ECharts 5
HTTP Client: Axios
State: Pinia
```

---

## 📚 文档导航

按顺序阅读：

1. **[DEMO_PROJECT_SUMMARY.md](DEMO_PROJECT_SUMMARY.md)** ⭐ 从这个开始
   - 项目总体概览
   - 5大功能说明
   - 完成清单

2. **[DEMO_QUICK_START.md](DEMO_QUICK_START.md)** 🚀 快速开始
   - 5步快速启动
   - API测试方法
   - 常见问题排查

3. **[DEMO_REQUIREMENTS.md](DEMO_REQUIREMENTS.md)** 📋 详细需求
   - 完整需求描述
   - 功能模块说明
   - API设计详解

4. **[DEMO_DATABASE_DESIGN.md](DEMO_DATABASE_DESIGN.md)** 🗄️ 数据库设计
   - 完整SQL脚本
   - 11张表详细设计
   - 关键查询示例

5. **[DEMO_JAVA_ARCHITECTURE.md](DEMO_JAVA_ARCHITECTURE.md)** ☕ 后端架构
   - 项目结构
   - pom.xml配置
   - 核心代码示例

6. **[DEMO_FRONTEND_DESIGN.md](DEMO_FRONTEND_DESIGN.md)** 🎨 前端设计
   - Vue组件详解
   - ECharts配置
   - 部署配置

---

## 🔧 容器化部署 (可选)

### Docker Compose 一键启动

```bash
docker-compose up -d
```

这会启动：

- MySQL 数据库
- Java 后端服务
- Vue 前端服务

### 访问地址

- 前端: http://localhost
- 后端: http://localhost:8080
- API文档: http://localhost:8080/swagger-ui.html

---

## ✅ 项目完成度

### 文档

- [x] 需求规格书
- [x] 数据库设计
- [x] Java架构设计
- [x] 前端设计
- [x] 快速开始指南
- [x] 项目总结

### 功能

- [x] 两个MySQL数据库
- [x] 11张数据库表
- [x] 7个REST API端点
- [x] 5个分析功能
- [x] 5个ECharts图表
- [x] Spring Boot后端
- [x] Vue.js前端
- [x] 完整SQL脚本

### 代码示例

- [x] 数据库建表脚本
- [x] 样本数据脚本
- [x] Spring Boot配置
- [x] MyBatis Mapper
- [x] Service服务
- [x] Controller控制器
- [x] Vue组件
- [x] API调用

---

## 📊 项目指标

| 指标       | 值      |
| ---------- | ------- |
| 代码行数   | ~2000+  |
| 文档字数   | ~20000+ |
| SQL查询    | 20+     |
| Java类数   | 15+     |
| Vue组件数  | 8+      |
| 数据库表数 | 11      |
| API端点数  | 7       |
| 图表类型数 | 5       |

---

## 🎓 学习路径

### 初级开发者

1. 阅读DEMO_PROJECT_SUMMARY.md了解整体
2. 按照DEMO_QUICK_START.md快速启动
3. 查看前端Dashboard了解数据展示

### 中级开发者

1. 学习DEMO_DATABASE_DESIGN.md数据库设计
2. 研究DEMO_JAVA_ARCHITECTURE.md后端代码
3. 修改数据库查询，添加新的分析维度

### 高级开发者

1. 优化SQL查询性能
2. 集成缓存(Redis)
3. 添加权限管理和认证
4. 部署到云平台(AWS/Azure/阿里云)

---

## 🐛 常见问题

### Q: 数据库连接失败？

**A:** 检查MySQL是否运行，确认用户名密码，修改 `application.yml`

### Q: 前端页面空白？

**A:** 打开浏览器F12查看console错误，确保后端服务运行

### Q: API返回404？

**A:** 检查Spring Boot是否启动成功，查看端口是否为8080

### Q: 数据为空？

**A:** 确认执行了sample_data.sql脚本插入样本数据

更多问题见 [DEMO_QUICK_START.md](DEMO_QUICK_START.md#常见问题排查)

---

## 🚢 部署建议

### 开发环境

```bash
# 本地开发
后端: mvn spring-boot:run
前端: npm run dev
```

### 测试环境

```bash
# Docker容器化
docker-compose up -d
```

### 生产环境

```bash
# 编译打包
后端: mvn clean package
前端: npm run build

# 部署到服务器/云平台
后端: java -jar xxx.jar
前端: 上传dist到Nginx
```

---

## 📝 许可证

MIT License - 可自由使用和修改

---

## 👨‍💻 贡献者

欢迎提Issue和PR！

---

## 💬 反馈和建议

如有问题，请查看文档或创建Issue。

---

## 🎯 下一步行动

```
┌─────────────────────────────────────────┐
│  1️⃣ 阅读 DEMO_PROJECT_SUMMARY.md       │
│  2️⃣ 按 DEMO_QUICK_START.md 启动项目    │
│  3️⃣ 查看 http://localhost:5173          │
│  4️⃣ 开发定制功能                       │
│  5️⃣ 部署到生产环境                     │
└─────────────────────────────────────────┘
```

🚀 **现在就开始吧！** [👉 DEMO_PROJECT_SUMMARY.md](DEMO_PROJECT_SUMMARY.md)

---

**版本**: 1.0.0  
**更新时间**: 2026-03-30  
**状态**: ✅ 完成可部署

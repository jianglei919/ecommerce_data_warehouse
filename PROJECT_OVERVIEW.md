# 🏗️ 电商数据仓库项目完整指南

## 📌 项目概述

**项目名称**: 电商数据仓库(ecommerce_data_warehouse)  
**项目类型**: 多源数据汇聚 + 数据仓库分析  
**部署方式**: Docker 一键启动  
**数据架构**: 多源库 → ETL → 数据仓库

---

## 🎯 核心功能

### 多源数据汇聚

- **分店1** (`ecommerce_source_1`): 北京/上海/广州店铺业务数据
- **分店2** (`ecommerce_source_2`): 深圳/杭州/成都店铺业务数据
- **集团仓库** (`ecommerce_warehouse`): 汇聚分析数据库

### 业务数据模型

#### 业务表 (业务源库)

| 表名            | 描述     | 记录数        |
| --------------- | -------- | ------------- |
| users           | 用户信息 | 3条/库×2 = 6  |
| products        | 商品信息 | 4条/库×2 = 8  |
| orders          | 订单     | 4条/库×2 = 8  |
| order_items     | 订单项目 | 5条/库×2 = 10 |
| product_reviews | 商品评价 | 4条/库×2 = 8  |
| returns         | 退货     | 1条/库×2 = 2  |

#### 分析表 (数据仓库库)

| 表名                 | 描述           | 数据来源            |
| -------------------- | -------------- | ------------------- |
| fact_sales           | 销售事实表     | source_1 + source_2 |
| dim_product_analysis | 产品分析维度表 | source_1 + source_2 |
| fact_sales_by_season | 季节销售分析   | source_1 + source_2 |
| fact_returns         | 退货分析       | source_1 + source_2 |
| kpi_daily            | 日KPI统计      | source_1 + source_2 |

---

## 🚀 快速启动

### 一键启动所有服务

```bash
docker-compose up -d
```

### 访问应用

| 服务       | URL                                   | 用途                    |
| ---------- | ------------------------------------- | ----------------------- |
| 前端仪表板 | http://localhost:5173                 | Vue3 数据可视化         |
| 后端 API   | http://localhost:8080                 | Spring Boot RESTful API |
| API文档    | http://localhost:8080/swagger-ui.html | Swagger API 文档        |
| MySQL      | localhost:3306                        | 数据库连接              |

---

## 📁 项目结构

```
ecommerce_data_warehouse/
├── docker-compose.yml              # Docker 容器编排
├── DOCKER_QUICKSTART.md            # Docker 快速启动指南
├── PROJECT_OVERVIEW.md             # 本文件
│
├── backend/                        # Java 后端项目
│   ├── Dockerfile                  # 后端容器镜像
│   ├── pom.xml                     # Maven 依赖配置
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/warehouse/
│   │   │   │   ├── Application.java                    # 应用启动类
│   │   │   │   ├── config/DataSourceConfig.java        # 多数据源配置
│   │   │   │   ├── entity/                             # 数据实体 (5个)
│   │   │   │   ├── dto/                                # 数据传输对象 (4个)
│   │   │   │   ├── mapper/                             # MyBatis 映射器 (3个)
│   │   │   │   ├── service/                            # 业务服务 (2个)
│   │   │   │   └── controller/ProductController.java   # API 控制器
│   │   │   └── resources/
│   │   │       └── application.yml                     # 应用配置
│   │   └── test/
│   │       └── java/...                                # 单元测试
│   │
│   ├── sql/
│   │   ├── source_db.sql           # 两个源库 DDL (创建 source_1 和 source_2)
│   │   ├── warehouse_db.sql        # 仓库库 DDL (创建 warehouse)
│   │   └── sample_data.sql         # 样本数据初始化 (双源库配置)
│   └── target/                     # 编译输出目录
│
├── frontend/                       # Vue 前端项目
│   ├── Dockerfile                  # 前端容器镜像
│   ├── package.json                # NPM 依赖配置
│   ├── vite.config.js              # Vite 配置
│   ├── index.html                  # HTML 入口
│   ├── src/
│   │   ├── main.js                 # Vue 应用入口
│   │   ├── router/index.js          # Vue Router 配置
│   │   ├── api/client.js            # API 请求客户端
│   │   ├── App.vue                  # 根组件
│   │   └── components/
│   │       ├── Dashboard.vue        # 仪表板
│   │       ├── HotProductsChart.vue # 热销产品图表
│   │       ├── SalesChart.vue       # 销售趋势图表
│   │       ├── CategoryChart.vue    # 分类销售图表
│   │       └── ReviewsChart.vue     # 评价分布图表
│   ├── dist/                        # 构建输出目录
│   └── node_modules/               # NPM 依赖目录
│
├── .git/                           # Git 版本控制
├── .gitignore                      # Git 忽略列表
└── docs/                           # 文档(可选)
    ├── DATABASE_DESIGN.md
    ├── JAVA_ARCHITECTURE.md
    └── API_DOCUMENTATION.md
```

---

## 🛠️ 技术栈

### 后端 (Backend)

- **框架**: Spring Boot 2.7.0
- **ORM**: MyBatis Plus 3.5.1
- **数据库**: MySQL 8.0
- **文档**: Swagger 3.0
- **编译**: Maven 3.8.1
- **运行时**: Java 11 (OpenJDK)

### 前端 (Frontend)

- **框架**: Vue 3.3
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **图表库**: ECharts
- **运行时**: Node.js 18

### 基础设施 (Infrastructure)

- **容器化**: Docker & Docker Compose
- **数据库**: MySQL 8.0
- **网络**: Bridge Network (ecommerce-network)
- **编排**: Docker Compose

---

## 💾 数据库设计

### 数据流向

```
┌──────────────────────────────────────┐
│    业务系统 (两套独立运营系统)         │
├──────────────────────────────────────┤
│  ecommerce_source_1    ecommerce_source_2  │
│   (北京/上海/广州)      (深圳/杭州/成都)   │
│   · users              · users           │
│   · products           · products        │
│   · orders             · orders          │
│   · order_items        · order_items     │
│   · product_reviews    · product_reviews │
│   · returns            · returns         │
└──────────────────────────────────────┘
          │                   │
          └───────┬───────────┘
                  │ ETL / 数据汇聚
                  ▼
        ┌──────────────────────┐
        │ ecommerce_warehouse  │
        ├──────────────────────┤
        │ fact_sales (销售事实)│
        │ dim_product_analysis │
        │ fact_sales_by_season │
        │ fact_returns         │
        │ kpi_daily (KPI日报)  │
        └──────────────────────┘
```

### 关键设计特点

1. **多源标记**: 所有数据表都包含 `source_flag` 字段标记来源(source_1/source_2)
2. **模式一致性**: 两个源库采用完全相同的表结构，便于数据合并
3. **聚合能力**: 仓库库使用 UNION 查询汇聚两个源库的数据
4. **独立性**: 两个源库可独立运营，互不影响

---

## 🔌 API 端点

### 热销分析

```bash
# 获取热销商品
GET /api/products/top-sales

# 获取高好评商品
GET /api/products/top-reviews

# 综合排名(销售×好评)
GET /api/products/combined-top
```

### 销售分析

```bash
# 按季节分析
GET /api/sales/by-season

# 按分类分析
GET /api/sales/by-category
```

### 运营指标

```bash
# 平均订单金额
GET /api/analytics/aov

# 退货率
GET /api/analytics/return-rate

# 日KPI数据
GET /api/analytics/daily-kpi
```

---

## 📊 前端功能

### 仪表板组件

| 组件                 | 功能描述               |
| -------------------- | ---------------------- |
| Dashboard.vue        | 主仪表板 - 集合5张图表 |
| HotProductsChart.vue | 热销商品排行 (柱状图)  |
| SalesChart.vue       | 销售趋势 (折线图)      |
| CategoryChart.vue    | 分类销售对比 (饼图)    |
| ReviewsChart.vue     | 评价分布 (直方图)      |

---

## ⚙️ 配置说明

### Docker 环境变量

```yaml
# MySQL
MYSQL_ROOT_PASSWORD: root
MYSQL_DATABASE: ecommerce_source_1

# Java 应用
SPRING_DATASOURCE_PRIMARY_URL: jdbc:mysql://mysql:3306/ecommerce_source_1
SPRING_DATASOURCE_WAREHOUSE_URL: jdbc:mysql://mysql:3306/ecommerce_source_2
SPRING_DATASOURCE_WAREHOUSE2_URL: jdbc:mysql://mysql:3306/ecommerce_warehouse

# 前端
VITE_API_BASE_URL: http://backend:8080
```

### 健康检查

所有服务都配置了健康检查，确保服务间的正确依赖关系：

- **MySQL**: TCP 连接检查 (3306)
- **Backend**: HTTP 检查 (GET /swagger-ui.html)
- **Frontend**: HTTP 检查 (GET /)

---

## 📈 构建和部署

### 本地构建 (可选)

```bash
# 构建后端镜像
docker-compose build backend

# 构建前端镜像
docker-compose build frontend

# 构建所有镜像
docker-compose build --no-cache
```

### 启动顺序

Docker Compose 会自动按依赖关系启动:

1. **MySQL** - 先启动数据库
2. **Backend** - 数据库就绪后启动
3. **Frontend** - 后端健康后启动

---

## 🐛 故障排除

### 常见问题

#### 1. 数据库连接失败

```bash
# 检查 MySQL 容器日志
docker-compose logs mysql

# 验证 MySQL 是否完全启动
docker-compose ps mysql

# 强制重启 MySQL
docker-compose restart mysql
```

#### 2. 后端无法启动

```bash
# 查看后端日志
docker-compose logs backend

# 验证数据库初始化脚本是否执行
docker-compose exec mysql mysql -u root -proot -e "SHOW DATABASES;"
```

#### 3. 前端无法连接后端

```bash
# 验证后端是否健康
curl http://localhost:8080/swagger-ui.html

# 检查网络连接
docker-compose exec frontend curl http://backend:8080/swagger-ui.html

# 查看前端日志
docker-compose logs frontend
```

---

## 📚 文档导航

| 文档                                         | 内容                      |
| -------------------------------------------- | ------------------------- |
| [DOCKER_QUICKSTART.md](DOCKER_QUICKSTART.md) | Docker 快速启动和常用命令 |
| DATABASE_DESIGN.md                           | 数据库架构和设计细节      |
| JAVA_ARCHITECTURE.md                         | 后端架构和代码组织        |
| API_DOCUMENTATION.md                         | API 详细文档              |

---

## 🎓 学习目标

通过本项目，您将学到:

1. **多源数据汇聚**: 如何设计支持多数据源的数据仓库
2. **Docker 容器化**: 完整的 Docker & Docker Compose 实践
3. **微服务架构**: Spring Boot 多数据源配置和路由
4. **前后端分离**: Vue3 前端与 RESTful 后端的交互
5. **ETL 流程**: 数据抽取、转换、加载的完整过程
6. **数据分析**: 汇聚数据进行分析和报表

---

## 📞 支持信息

- **项目仓库**: git@github.com:jianglei919/ecommerce_data_warehouse.git
- **最后更新**: 2024-02-15
- **版本**: 1.0.0 (Docker 多源库版本)

---

## ✨ 快速命令速查

```bash
# 启动所有服务
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看实时日志
docker-compose logs -f

# 停止服务
docker-compose stop

# 完全清理(删除数据)
docker-compose down -v

# 进入 MySQL shell
docker-compose exec mysql mysql -u root -proot

# 查看容器内的文件
docker-compose exec backend ls -la /app

# 重启某个服务
docker-compose restart backend
```

---

**祝你使用愉快！** 🎉

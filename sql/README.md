# SQL 初始化脚本

本目录包含三个数据库的初始化脚本，用于快速启动项目演示。

## 📋 文件说明

### 1. `01-app-schema.sql` - App业务系统 📱

- **数据库名**: `ecommerce_source_app`
- **特点**:
  - `orders` 表主键: `order_id` (INT 类型)
  - `order_date`: yyyy-MM-dd 格式
  - 包含 5 个用户、10 个产品、10 个订单及示例数据
- **表结构**:
  - users (用户表)
  - products (产品表)
  - orders (订单表)
  - order_items (订单项目表)
  - product_reviews (产品评论表)

### 2. `02-web-schema.sql` - Web业务系统 🌐

- **数据库名**: `ecommerce_source_web`
- **特点**:
  - `orders` 表主键: `order_no` (VARCHAR 类型)
  - `order_items` 表使用 `order_no` 字段 ⭐ 关键区别
  - `order_date`: 逻辑上为 MM/dd/yyyy 格式
  - 包含 5 个用户、10 个产品、10 个订单及示例数据
- **表结构**:
  - users (用户表)
  - products (产品表)
  - orders (订单表 - order_no VARCHAR)
  - order_items (订单项目表 - 使用order_no)
  - product_reviews (产品评论表)

### 3. `03-warehouse-schema.sql` - 数据仓库 📊

- **数据库名**: `ecommerce_warehouse`
- **目的**: 统一存储ETL处理结果，支持多维分析
- **表结构**:
  - `fact_sales_by_category_time`: 销量事实表 (按类别和时间)
  - `fact_top_rated_products`: 评分事实表 (按商品和时间)
  - `sync_log`: 同步日志表 (监控ETL过程)
- **额外功能**:
  - 2 个视图 (v_sales_ranking, v_product_ratings)
  - 3 个存储过程 (数据聚合和查询)

## 🚀 快速开始

### 方式1: Docker Compose 自动初始化 (推荐)

```bash
docker-compose up -d
# Docker会自动执行这些脚本初始化数据库
```

### 方式2: 手动执行脚本

```bash
# 连接到App数据库
mysql -h 127.0.0.1 -u root -p ecommerce_source_app < 01-app-schema.sql

# 连接到Web数据库
mysql -h 127.0.0.1 -u root -p ecommerce_source_web < 02-web-schema.sql

# 连接到仓库数据库
mysql -h 127.0.0.1 -u root -p ecommerce_warehouse < 03-warehouse-schema.sql
```

### 方式3: MySQL 客户端

```bash
# 登录到MySQL
mysql -h 127.0.0.1 -u root -p

# 创建数据库
CREATE DATABASE ecommerce_source_app;
CREATE DATABASE ecommerce_source_web;
CREATE DATABASE ecommerce_warehouse;

# 执行脚本
SOURCE /path/to/01-app-schema.sql;
SOURCE /path/to/02-web-schema.sql;
SOURCE /path/to/03-warehouse-schema.sql;
```

## 🔄 ETL 数据流

```
App业务系统 (order_id INT)
    ↓ [ETL转换]
Web业务系统 (order_no VARCHAR)
    ↓ [数据统一]
数据仓库 (fact_sales_by_category_time, fact_top_rated_products)
    ↓ [多维分析]
Dashboard展示
```

## 📊 初始数据统计

| 数据库    | 用户 | 产品 | 订单      | 评论 | 订单项 |
| --------- | ---- | ---- | --------- | ---- | ------ |
| App       | 5    | 10   | 10        | 10   | 22     |
| Web       | 5    | 10   | 10        | 10   | 25     |
| Warehouse | -    | -    | 20 (聚合) | -    | -      |

## 🔍 验证脚本执行

执行完脚本后，可以运行以下查询验证：

```sql
-- 检查App数据库
USE ecommerce_source_app;
SELECT COUNT(*) FROM orders;  -- 应该是 10
SELECT SUM(total_amount) FROM orders;  -- 应该是 ~8859.89

-- 检查Web数据库
USE ecommerce_source_web;
SELECT COUNT(*) FROM orders;  -- 应该是 10
SELECT COUNT(*) FROM order_items WHERE order_no = 'WEB-2024-001';  -- 应该是 3

-- 检查仓库数据库
USE ecommerce_warehouse;
SELECT COUNT(*) FROM fact_sales_by_category_time;  -- 应该是 ~15
SELECT COUNT(*) FROM fact_top_rated_products;  -- 应该是 ~9
SELECT * FROM sync_log;  -- 初始为空，在ETL运行后会有数据
```

## 🛠️ 常见问题

### Q: 为什么要创建异构的 order_id 和 order_no？

A: 这是真实的数据集成场景。App和Web是不同的业务系统，恰好使用了不同的主键设计。ETL需要处理这种异构性。

### Q: order_items 中的 order_no 关键吗？

A: 非常关键！这是Web系统的独特设计。ETL过程必须正确处理这个字段的映射，这是本项目的一个重要业务规则。

### Q: 可以修改示例数据吗？

A: 完全可以！根据你的演示需求修改 INSERT 语句中的数据即可。

### Q: warehouse 数据怎么来的？

A: 初始数据是为了演示而预填充的。在实际运行时，这些表会由 Spring Boot 中的 ETL 消费者通过 Kafka 事件持续更新。

## 📝 后续步骤

1. ✅ **Phase 1 完成**: 数据库初始化脚本已生成
2. ⏳ **Phase 2**: 后端 Spring Boot + Kafka 实现
3. ⏳ **Phase 3**: 前端 Vue3 Dashboard
4. ⏳ **Phase 4**: Docker Compose 一键启动

---

**提示**: 这些脚本包含了生产级别的设计 (索引、约束、视图、存储过程)，同时保持了演示用途的简洁性。

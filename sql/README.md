# SQL 初始化脚本

本目录包含数据仓库的核心初始化脚本，提供多源系统的原始数据和 Star Schema 结构。
**最后更新**: 从运行容器提取真实数据 (2024-01-15 到 2024-02-10 的 20 条订单)

## 📋 文件说明

### 1. `01-app-schema.sql` - App业务系统 📱

- **数据库名**: `ecommerce_source_app`
- **数据时间范围**: 2024-01-15 到 2024-02-10
- **特点**:
  - `orders` 表主键: `order_id` (INT AUTO_INCREMENT 类型)
  - `order_date`: yyyy-MM-dd 格式
  - 包含 **10 个用户**、**10 个产品**、**10 个订单** 的真实数据
  - 所有数据通过 mysqldump 从运行容器提取
- **表结构**:
  - `users` - 用户表 (10 条记录)
  - `products` - 产品表 (10 条记录)
  - `orders` - 订单表 (10 条订单记录)
  - `order_items` - 订单项目表 (关联订单和产品)
  - `product_reviews` - 产品评论表

### 2. `02-web-schema.sql` - Web业务系统 🌐

- **数据库名**: `ecommerce_source_web`
- **数据时间范围**: 2024-01-15 到 2024-02-10
- **特点**:
  - `orders` 表主键: `order_no` (VARCHAR 类型)
  - `order_items` 表使用 `order_no` 字段 ⭐ 关键区别
  - `order_date`: yyyy-MM-dd 格式
  - 包含 **10 个用户**、**10 个产品**、**10 个订单** 的真实数据
  - 所有数据通过 mysqldump 从运行容器提取
- **表结构**:
  - `users` - 用户表 (10 条记录)
  - `products` - 产品表 (10 条记录)
  - `orders` - 订单表 (order_no VARCHAR 类型，10 条记录)
  - `order_items` - 订单项目表 (使用 order_no, 关联订单和产品)
  - `product_reviews` - 产品评论表

### 3. `03-warehouse-schema.sql` - 数据仓库 📊

- **数据库名**: `ecommerce_warehouse`
- **目的**: 数据库存储统一处理后的 Star Schema，支持 OLAP 分析
- **架构**: Star Schema with Dimension & Fact Tables
- **数据汇总**:
  - 合并两个源系统 (App + Web) 的 20 条订单
  - 订单日期范围: 2024-01-15 到 2024-02-10
  - 统一数据源追踪 (source 字段: 'APP' 或 'WEB')
- **表结构**:
  - `dim_products` - 产品维度表 (20 条: 10 APP + 10 WEB)
  - `dim_orders` - 订单维度表 (20 条)
  - `dim_order_items` - 订单项目维度表
  - `fact_sales_by_product_time` - 销售事实表 (按产品和时间聚合)
  - `fact_top_rated_products` - 商品评分排行事实表
  - `sync_log` - ETL 同步日志表 (记录事件处理状态)

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
数据仓库 (dim_products, dim_orders, dim_order_items, fact_sales_by_product_time, fact_top_rated_products)
    ↓ [多维分析]
Dashboard展示
```

## 📊 初始数据统计

| 指标     | App                     | Web   | Warehouse | 合计 |
| -------- | ----------------------- | ----- | --------- | ---- |
| 用户数   | 10                      | 10    | -         | 20   |
| 产品数   | 10                      | 10    | 20 (合并) | -    |
| 订单数   | 10                      | 10    | 20 (合并) | -    |
| 订单项数 | ~30                     | ~30   | -         | ~60  |
| 日期范围 | 2024-01-15 - 2024-02-10 | 同左  | 同左      | -    |
| 总销售额 | ~$10k                   | ~$10k | ~$20.5k   | -    |

## 🔍 验证脚本执行

执行完脚本后，可以运行以下查询验证：

```sql
-- 检查App数据库
USE ecommerce_source_app;
SELECT COUNT(*) as order_count FROM orders;  -- 应该是 10
SELECT SUM(total_amount) as total_sales FROM orders;  -- 验证总金额

-- 检查Web数据库
USE ecommerce_source_web;
SELECT COUNT(*) as order_count FROM orders;  -- 应该是 10
SELECT COUNT(DISTINCT order_no) as unique_orders FROM order_items;  -- 验证订单项

-- 检查仓库数据库
USE ecommerce_warehouse;
SELECT COUNT(*) as dim_orders FROM dim_orders;  -- 应该是 20
SELECT COUNT(*) as dim_products FROM dim_products;  -- 应该是 20
SELECT COUNT(*) as fact_sales FROM fact_sales_by_product_time;  -- 聚合的事实表数据
SELECT COUNT(*) as top_rated_products FROM fact_top_rated_products;  -- 商品评分排行事实表
SELECT COUNT(*) as sync_logs FROM sync_log;  -- ETL 同步日志记录数
SELECT * FROM fact_sales_by_product_time LIMIT 5;  -- 查看前5条数据
```

## 🛠️ 常见问题

### Q: 为什么 App 使用 order_id，而 Web 使用 order_no？

A: 这反映真实的多源系统集成场景。App 使用 INT 自增主键，Web 使用手动控制的 VARCHAR 编码 (如 "WEB-2024-001")。ETL 必须处理这种异构性.

### Q: dim_orders 和 dim_order_items 中的数据如何同步？

A: 这三个 SQL 文件是从运行中的容器通过 mysqldump 导出的。当 docker-compose up 运行时，这些文件会自动重建数据库和初始化数据。

### Q: 可以修改示例数据吗？

A: 完全可以！修改 INSERT 语句中的数据即可。建议保持订单日期在 2024-01-15 到 2024-02-10 范围内以匹配现有数据分布。

### Q：为什么 fact_sales_by_product_time 是由两个源数据库的订单组成的？

A: 这是数据仓库的核心功能——统一多个源系统的数据进行分析。Warehouse 合并了 App 和 Web 的订单数据，并标记了 source 字段 ('APP' 或 'WEB')。

## 📜 文件维护说明

- **自动生成**: 这些 SQL 文件是从运行的数据库容器通过 `mysqldump` 导出生成的
- **保存位置**: `sql/` 目录 (此目录)
- **版本控制**: 提交到 git，作为项目的初始化基础
- **更新方法**: 修改数据库内容后，重新运行 `mysqldump` 导出即可更新这些文件

## 🔗 相关文件

- `docker-compose.yml` - 定义了三个数据库容器的启动配置
- `backend/` - Java 应用程序代码，定义了数据库映射关系
- `frontend/` - Vue.js 前端应用

### Q: warehouse 数据怎么来的？

A: 初始数据是为了演示而预填充的。在实际运行时，这些表会由 Spring Boot 中的 ETL 消费者通过 Kafka 事件持续更新。

## 📝 后续步骤

1. ✅ **Phase 1 完成**: 数据库初始化脚本已生成
2. ⏳ **Phase 2**: 后端 Spring Boot + Kafka 实现
3. ⏳ **Phase 3**: 前端 Vue3 Dashboard
4. ⏳ **Phase 4**: Docker Compose 一键启动

---

**提示**: 这些脚本包含了生产级别的设计 (索引、约束、视图、存储过程)，同时保持了演示用途的简洁性。

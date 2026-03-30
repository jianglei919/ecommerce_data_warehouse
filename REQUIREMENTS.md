# 电商数据仓库 - 业务需求文档

## 项目目标

构建一个数据仓库系统，从两个不同来源的业务数据库（App和Web）进行数据清理、ETL处理，最后展示**销量分析**和**评论排行**数据。

---

## 数据源架构

### 数据库1：App业务系统 (ecommerce_source_app)

**表结构：**

| 表名                | 字段                                                | 说明       |
| ------------------- | --------------------------------------------------- | ---------- |
| **users**           | user_id, name, email, city, register_date           | 用户表     |
| **products**        | product_id, name, category, price, brand            | 商品表     |
| **orders**          | order_id, user_id, order_date, total_amount, status | 订单表     |
| **order_items**     | item_id, order_id, product_id, quantity, unit_price | 订单明细表 |
| **product_reviews** | review_id, product_id, user_id, rating, review_date | 商品评论表 |

**数据格式特征：**

- orders.**order_id**：数字类型（INT）
- orders.**order_date**：日期格式 `yyyy-MM-dd`

### 数据库2：Web业务系统 (ecommerce_source_web)

**表结构：** 与App相同，但订单表字段名不同

| 表名                | 字段                                                | 说明                     |
| ------------------- | --------------------------------------------------- | ------------------------ |
| **users**           | user_id, name, email, city, register_date           | 用户表                   |
| **products**        | product_id, name, category, price, brand            | 商品表                   |
| **orders**          | order_no, user_id, order_date, total_amount, status | 订单表（注：字段名不同） |
| **order_items**     | item_id, order_id, product_id, quantity, unit_price | 订单明细表               |
| **product_reviews** | review_id, product_id, user_id, rating, review_date | 商品评论表               |

**数据格式特征：**

- orders.**order_no**：字符+数字混合（VARCHAR，如 "WEB-001"）
- orders.**order_date**：日期格式 `MM/dd/yyyy`

**两个源库的关键差异：**

| 维度              | App (source_app) | Web (source_web)  |
| ----------------- | ---------------- | ----------------- |
| Order ID 字段名   | order_id         | order_no          |
| Order ID 数据格式 | 12345 (INT)      | WEB-001 (VARCHAR) |
| Order 日期格式    | 2024-03-01       | 03/01/2024        |

### 数据库3：分析数据仓库 (ecommerce_warehouse)

用于存储清理、转换后的统计数据。

**核心表：**

| 表名                            | 用途                           |
| ------------------------------- | ------------------------------ |
| **fact_sales_by_category_time** | 按商品种类和时间维度的销量统计 |
| **fact_top_rated_products**     | 按评价统计的Top商品            |

---

## 统计需求清单

### 需求1：按商品种类和时间维度分析销量

**维度：**

- 商品种类（category）
- 时间维度：年、月、日

**指标：**

- 销量（数量）
- 销售额

**输出展示：**

- 热力图（X轴：时间，Y轴：分类，值：销量）
- 柱状图（按分类或时间段对比）

**示例查询结果：**

```
Category: Electronics, Time: 2024-03, Quantity: 150, Amount: 45000
Category: Clothing,    Time: 2024-03, Quantity: 200, Amount: 15000
Category: Books,       Time: 2024-03, Quantity: 80,  Amount: 3200
```

---

### 需求2：按评论统计Top5商品

**维度：**

- 商品种类（category）
- 时间维度：年、月、日

**指标：**

- 平均评分（avg_rating）
- 评论数（review_count）

**输出展示：**

- 排行榜（显示Top 5商品及其评分）

**示例查询结果：**

```
Product: iPhone 14,       Category: Electronics, Avg Rating: 4.8, Reviews: 150
Product: MacBook Pro,     Category: Electronics, Avg Rating: 4.7, Reviews: 120
Product: Samsung Galaxy,  Category: Electronics, Avg Rating: 4.6, Reviews: 100
...
```

---

## 数据处理流程

```
┌──────────────────────┐         ┌──────────────────────┐
│ ecommerce_source_app │         │ ecommerce_source_web │
│  (App 数据源)        │         │  (Web 数据源)        │
└──────────┬───────────┘         └──────────┬───────────┘
           │                               │
           │  Data Clean-up & ETL          │
           │  - 字段名统一                  │
           │  - 日期格式转换                │
           │  - 数据验证和去重              │
           │                               │
           └──────────┬────────────────────┘
                      │
                      ▼
          ┌──────────────────────┐
          │ ecommerce_warehouse  │
          │  (分析数据仓库)       │
          └──────────┬───────────┘
                      │
         ┌────────────┴────────────┐
         ▼                         ▼
    ┌─────────────────┐    ┌──────────────────┐
    │ Sales Analysis  │    │ Top Rated Review │
    │ by Category     │    │ Product Analysis │
    └─────────────────┘    └──────────────────┘
         │                         │
         └────────────┬────────────┘
                      ▼
              UI 仪表板展示
```

---

## 技术要求

- **后端**：Spring Boot + MyBatis，支持多数据源查询和数据转换
- **前端**：Vue 3，使用图表库展示热力图、柱状图、排行榜
- **数据库**：MySQL 8.0
- **部署**：Docker Compose 一键启动

---

## 工作流程

1. ✅ **需求确认**（当前阶段）- 确认数据模型和统计需求
2. ⏳ **数据库DDL编写** - 创建两个源库和仓库库的表
3. ⏳ **样本数据插入** - 为两个源库插入测试数据（展示数据变化时用）
4. ⏳ **ETL逻辑开发** - 数据清理、转换和加载到仓库
5. ⏳ **API编写** - 后端查询接口
6. ⏳ **UI开发** - 前端仪表板
7. ⏳ **测试和部署** - Docker部署验证

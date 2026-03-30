# 电商数据仓库 - Java Demo 项目需求文档

## 项目概述

构建一个简单的电商数据仓库演示系统，集成数据分析、统计和可视化功能。

---

## 需求清单

### 1. 数据库设计（两个数据库）

#### 数据库1：原始业务数据库 (ecommerce_source)

存储原始的业务数据：

| 表名                | 字段                                                 | 说明       |
| ------------------- | ---------------------------------------------------- | ---------- |
| **users**           | user_id, name, email, city, register_date            | 用户表     |
| **products**        | product_id, name, category, price, brand             | 商品表     |
| **orders**          | order_id, user_id, order_date, total_amount, status  | 订单表     |
| **order_items**     | item_id, order_id, product_id, quantity, unit_price  | 订单明细表 |
| **product_reviews** | review_id, product_id, user_id, rating, review_date  | 商品评论表 |
| **returns**         | return_id, order_id, product_id, return_date, reason | 退货表     |

#### 数据库2：分析数据库 (ecommerce_warehouse)

存储分析结果和统计数据：

| 表名                     | 字段                                                                             | 说明             |
| ------------------------ | -------------------------------------------------------------------------------- | ---------------- |
| **fact_sales**           | sales_id, product_id, category, sales_qty, sales_amount, sale_date, season       | 销售事实表       |
| **dim_product_analysis** | product_id, name, avg_rating, total_reviews, total_sales_qty, total_sales_amount | 商品分析维度表   |
| **fact_sales_by_season** | product_id, category, season, total_qty, total_amount, year                      | 按季节销售事实表 |
| **fact_returns**         | return_id, product_id, order_id, return_date, return_qty, return_rate            | 退货事实表       |
| **kpi_daily**            | kpi_date, avg_order_value, total_orders, total_sales, return_rate                | 日KPI表          |

---

### 2. 分析需求

#### 2.1 热销商品分析

```
维度1：销量排行 TOP 10
维度2：评论评分 TOP 10
组合展示：综合评分（销量权重60% + 评论评分权重40%）
```

**查询示例**：

```sql
-- 按销量排行
SELECT product_id, name, SUM(quantity) as total_qty
FROM order_items oi
JOIN products p ON oi.product_id = p.product_id
GROUP BY product_id, name
ORDER BY total_qty DESC LIMIT 10;

-- 按评分排行
SELECT p.product_id, p.name, AVG(pr.rating) as avg_rating, COUNT(pr.review_id) as review_count
FROM products p
LEFT JOIN product_reviews pr ON p.product_id = pr.product_id
GROUP BY p.product_id, p.name
HAVING COUNT(pr.review_id) >= 5
ORDER BY avg_rating DESC LIMIT 10;
```

#### 2.2 按商品种类和季节分析销量

```
维度：
- 商品种类（category）
- 季节（Spring/Summer/Fall/Winter）

指标：
- 销量（数量）
- 销售额
- 环比增长率

展示：热力图或柱状图
```

#### 2.3 平均订单价值（AOV）

```
AOV = 总销售额 / 订单数
支持时间维度：日/周/月/季度
支持按用户等级、地区、商品类别分组
```

#### 2.4 商品退货率

```
计算方式：
- 商品级别：某商品退货数 / 该商品销售数
- 类别级别：某类别退货数 / 该类别销售数
- 整体退货率：总退货数 / 总订单数

展示：饼图 + 详细列表
```

#### 2.5 其他辅助分析

```
- 用户购买频率
- 订单金额分布
- 品牌销售对比
```

---

## 3. 系统开发方案

### 3.1 技术栈

```
后端：Java + Spring Boot
ORM：MyBatis-Plus
数据库：MySQL × 2
缓存：Redis（可选）
前端：Vue.js 3 + Echarts
API：RESTful
```

### 3.2 项目结构

```
ecommerce-warehouse-demo/
├── backend/
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/example/
│   │   │   ├── config/              # 数据库配置
│   │   │   ├── controller/          # API控制器
│   │   │   ├── service/             # 业务逻辑
│   │   │   ├── mapper/              # MyBatis mapper
│   │   │   ├── entity/              # 数据实体
│   │   │   ├── dto/                 # 数据传输对象
│   │   │   └── Application.java     # 启动类
│   │   └── resources/
│   │       ├── application.yml
│   │       └── mapper/              # SQL XML文件
│   └── sqls/                        # 建表脚本
│
├── frontend/
│   ├── package.json
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── api/                     # API调用
│   │   ├── App.vue
│   │   └── main.js
│   └── public/
│
└── docs/
    ├── 需求文档.md
    ├── 数据库设计.md
    ├── API文档.md
    └── 部署说明.md
```

### 3.3 核心功能模块

```
1. 数据导入模块 (Data Import Service)
   - 从业务库读取原始数据
   - 进行ETL转换
   - 加载到分析库

2. 分析计算模块 (Analysis Service)
   - 热销商品计算
   - 季节销量分析
   - AOV计算
   - 退货率计算

3. 数据查询API (Query API)
   - /api/products/top-sales       # TOP热销
   - /api/products/top-reviews     # TOP评分
   - /api/sales/by-season          # 季节分析
   - /api/analytics/aov            # 平均订单价值
   - /api/analytics/return-rate    # 退货率

4. 可视化展示层 (Frontend)
   - 仪表板页面
   - 多种图表展示
```

---

## 4. 具体功能说明

### 4.1 热销商品分析

**API设计**：

```
GET /api/products/hot-analysis?limit=10&type=sales|reviews|combined

返回格式：
{
  "sales_ranking": [
    {
      "rank": 1,
      "product_id": 101,
      "product_name": "iPhone 14 Pro",
      "category": "Electronics",
      "total_qty": 1500,
      "total_amount": 2250000,
      "avg_rating": 4.8
    },
    ...
  ],
  "review_ranking": [
    {
      "rank": 1,
      "product_id": 102,
      "product_name": "南孚电池",
      "avg_rating": 4.9,
      "review_count": 2500,
      "total_qty": 800
    },
    ...
  ],
  "combined_ranking": [ ... ]  // 综合排分
}
```

### 4.2 季节销量分析

**API设计**：

```
GET /api/sales/by-season?year=2024&category=all

返回格式：
{
  "data": [
    {
      "category": "Electronics",
      "spring": 500000,
      "summer": 620000,
      "fall": 580000,
      "winter": 720000
    },
    ...
  ]
}
```

### 4.3 平均订单价值

**API设计**：

```
GET /api/analytics/aov?dimension=day|week|month|quarter

返回格式：
{
  "daily_aov": [
    { "date": "2024-01-01", "aov": 2500, "order_count": 1200 },
    ...
  ],
  "monthly_aov": [
    { "month": "2024-01", "aov": 2450, "order_count": 35000 },
    ...
  ]
}
```

### 4.4 退货率分析

**API设计**：

```
GET /api/analytics/return-rate?type=product|category|overall

返回格式：
{
  "overall_return_rate": 0.05,
  "by_category": [
    {
      "category": "Electronics",
      "return_rate": 0.08,
      "return_count": 1200,
      "total_sales": 15000
    },
    ...
  ]
}
```

---

## 5. 数据库初始化

### 2个数据库实例配置

**database.yml**:

```yaml
# 业务库
database:
  primary:
    name: ecommerce_source
    host: localhost
    port: 3306
    username: root
    password: password

# 数据仓库库
database:
  warehouse:
    name: ecommerce_warehouse
    host: localhost
    port: 3306
    username: root
    password: password
```

### 建表脚本位置

- `sqls/source_db.sql` - 原始业务数据库建表脚本
- `sqls/warehouse_db.sql` - 分析数据库建表脚本
- `sqls/init_sample_data.sql` - 测试数据样本

---

## 6. 前端可视化需求

### 6.1 仪表板布局

```
┌─────────────────────────────────────────────────────┐
│           电商数据仓库分析系统                      │
├─────────────────────────────────────────────────────┤
│  [热销商品分析]  [季节销售]  [平均订单价值]  [退货率] │
├─────────────────────────────────────────────────────┤
│                                                       │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐ │
│  │  热销TOP 10 │  │  评分TOP 10 │  │  综合排分   │ │
│  │  (柱状图)   │  │  (柱状图)   │  │  (表格)     │ │
│  └─────────────┘  └─────────────┘  └─────────────┘ │
│                                                       │
│  ┌──────────────────────────────────────────────┐   │
│  │     季节销售热力图 (选择类别过滤)             │   │
│  │     Spring  Summer  Fall  Winter              │   │
│  │  └──────────────────────────────────────────┘   │
│                                                       │
│  ┌──────────────┐  ┌──────────────┐               │
│  │   平均订单   │  │   退货率     │               │
│  │   价值曲线   │  │   饼图       │               │
│  └──────────────┘  └──────────────┘               │
│                                                       │
└─────────────────────────────────────────────────────┘
```

### 6.2 图表类型

- 柱状图：热销商品排行、季节对比
- 热力图：季节销售分析
- 折线图：AOV时间序列
- 饼图：退货率分布
- 表格：详细数据查看

---

## 7. 开发优先级

### Phase 1（核心功能）

- [x] 数据库设计和创建
- [x] 数据导入脚本
- [ ] 热销商品查询API
- [ ] 基础前端展示

### Phase 2（完善功能）

- [ ] 季节分析API
- [ ] AOV计算API
- [ ] 退货率分析API
- [ ] 高级图表展示

### Phase 3（优化）

- [ ] 性能优化
- [ ] 缓存集成
- [ ] 定时任务（ETL自动化）
- [ ] 权限控制

---

## 8. 技术指标

| 指标         | 目标值     |
| ------------ | ---------- |
| API响应时间  | < 1秒      |
| 前端加载时间 | < 3秒      |
| 支持数据量   | 100W+ 订单 |
| 支持并发用户 | 50+        |

---

## 9. 交付成果

- ✅ 完整源代码（Java后端 + Vue前端）
- ✅ 数据库建表脚本
- ✅ 测试数据集
- ✅ API文档（Swagger）
- ✅ 部署运行说明
- ✅ 可视化仪表板

---

## 下一步

1. 确认需求无误
2. 开始数据库设计详细化
3. 设计Java后端服务结构
4. 创建Spring Boot项目框架

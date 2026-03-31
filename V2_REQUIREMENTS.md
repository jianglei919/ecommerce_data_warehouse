# V2 版本需求设计文档

## 需求概述

1. **统一订单表**：在数据仓库中新增 `unified_orders` 和 `unified_order_items` 表，整合两个业务数据源（App和Web）的订单数据
2. **基础表**：现有Fact表基于新的统一订单表构建
3. **展示界面**：创建前端界面展示整合后的订单数据

## 数据结构设计

### 源系统差异

- **App数据库**：order_id为INT，order_no为空
- **Web数据库**：order_no为VARCHAR，order_id为空

### 统一订单表结构

```
unified_orders
├── unified_order_id (PK): UUID 或 INT
├── source: 'APP' 或 'WEB'
├── app_order_id: 来自App系统的订单ID（可为NULL）
├── web_order_no: 来自Web系统的订单号（可为NULL）
├── user_id: INT
├── order_date: DATE
├── total_amount: DECIMAL
├── status: VARCHAR
└── 时间戳
```

## 实现路线图

1. **Phase 1**: 创建unified_orders和unified_order_items表
2. **Phase 2**: 创建ETL聚合逻辑
3. **Phase 3**: 创建后端API接口
4. **Phase 4**: 创建前端展示页面

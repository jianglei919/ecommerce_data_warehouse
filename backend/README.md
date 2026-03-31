# 数据仓库后端 (Spring Boot 3.0)

## 项目概述

这是 E-commerce 数据仓库项目的后端服务，实现了完整的 ETL (提取、转换、加载) 流程，支持实时数据同步。

**核心技术栈**:

- Spring Boot 3.0
- Apache Kafka 3.x (消息队列)
- MyBatis-Plus (ORM)
- MySQL 8.0 (多数据源)
- Redis (缓存)
- Docker Compose (容器化)

## 项目结构

```
backend/
├── src/
│   ├── main/
│   │   ├── java/com/uwindsor/warehouse/
│   │   │   ├── WarehouseApplication.java        # 主启动类
│   │   │   ├── config/
│   │   │   │   ├── KafkaConfig.java             # Kafka 配置
│   │   │   │   └── DataSourceConfig.java        # 多数据源配置
│   │   │   ├── event/
│   │   │   │   └── OrderEvent.java              # 订单事件模型
│   │   │   ├── kafka/
│   │   │   │   └── KafkaEventConsumer.java      # Kafka 消费者
│   │   │   ├── service/
│   │   │   │   └── ETLService.java              # ETL 处理核心逻辑
│   │   │   ├── domain/                          # 数据模型 (待扩展)
│   │   │   ├── mapper/                          # MyBatis 映射
│   │   │   └── repository/                      # 数据访问层 (待扩展)
│   │   └── resources/
│   │       └── application.yml                  # 应用配置
│   └── test/
├── pom.xml
└── Dockerfile
```

## 核心功能

### 1. 事件驱动架构

**Kafka Topics**:

- `order-events` (3 partitions) - 订单事件流
  - 事件类型: ORDER_CREATED, ORDER_UPDATED, ORDER_DELETED
  - 数据源: APP, WEB
- `sales-events` (2 partitions) - 销售聚合事件
- `sync-events` (1 partition) - 同步监控事件

### 2. ETL 流程

**数据流向**:

```
App DB / Web DB  →  Kafka  →  ETL Consumer  →  Transform  →  Warehouse DB
   (源系统)        (事件总线)   (KafkaEventConsumer)  (ETLService)
```

**转换规则**:

- App: `order_id (INT)` → Warehouse: 统一到事实表
- Web: `order_no (VARCHAR)` → 字段映射 + 类型转换
- 日期格式: `yyyy-MM-dd` 统一处理
- 金额: `Double` 精度处理

### 3. 数据仓库层

**事实表**:

- `fact_sales_by_category_time` - 按分类、日期聚合的销售数据
- `fact_top_rated_products` - 按商品、日期聚合的评分数据

**监控表**:

- `sync_log` - ETL 事件审计日志

**分析视图**:

- `v_sales_ranking` - 销售额排名 (带 RANK 窗口函数)
- `v_product_ratings` - 商品评分排名

### 4. REST API 接口

#### 分析接口 `/api/analytics`

**销售统计**

```
GET /api/analytics/sales/by-category?startDate=2024-01-01&endDate=2024-01-31
```

返回按分类聚合的销售数据

**热门商品**

```
GET /api/analytics/products/top-rated?limit=10
```

返回评分最高的 N 个商品

**同步统计**

```
GET /api/analytics/sync/statistics
```

返回 ETL 同步进度和统计

**测试订单**

```
POST /api/analytics/test/send-order
Content-Type: application/json

{
  "eventType": "ORDER_CREATED",
  "source": "APP",
  "userId": 1,
  "orderDate": "2024-01-15",
  "totalAmount": 299.99,
  "itemCount": 2
}
```

**健康检查**

```
GET /api/analytics/health
```

## 配置说明

### application.yml

**数据源配置** (多数据库支持):

```yaml
spring:
  datasource:
    app: # 应用源系统 DB
      url: jdbc:mysql://app-db:3306/ecommerce_source_app
    web: # Web 源系统 DB
      url: jdbc:mysql://web-db:3306/ecommerce_source_web
    warehouse: # 仓库 DB (主数据源)
      url: jdbc:mysql://warehouse-db:3306/ecommerce_warehouse
```

**Kafka 配置**:

```yaml
spring:
  kafka:
    bootstrap-servers: kafka:29092
    producer:
      key-serializer: StringSerializer
      value-serializer: JsonSerializer
      acks: all # 所有副本确认
      retries: 3 # 重试 3 次
    consumer:
      group-id: warehouse-etl-group
      auto-offset-reset: earliest
      enable-auto-commit: false # 手动提交
```

**ETL 配置**:

```yaml
app:
  etl:
    batch-size: 100 # 批处理大小
    batch-timeout-ms: 5000 # 批处理超时
    retry-count: 3 # 失败重试次数
    retry-delay-ms: 1000 # 重试延迟
```

## 构建和运行

### Maven 构建

```bash
# 在 backend 目录下
mvn clean package

# 运行
java -jar target/warehouse-backend-1.0.0.jar
```

### Docker 构建

```bash
# 在项目根目录
docker build -f backend/Dockerfile -t warehouse-backend:latest ./backend

# 或通过 docker-compose
docker-compose up -d
```

## 本地开发

### 前置要求

- JDK 17+
- Maven 3.9+
- MySQL 8.0
- Apache Kafka 3.x
- Redis 7.x

### 快速启动 (Docker Compose)

```bash
# 在项目根目录
docker-compose up -d

# 查看日志
docker-compose logs -f backend

# 停止服务
docker-compose down
```

### 测试 ETL 流程

```bash
# 1. 发送测试订单
curl -X POST http://localhost:8080/api/analytics/test/send-order \
  -H "Content-Type: application/json" \
  -d '{
    "eventType": "ORDER_CREATED",
    "source": "APP",
    "userId": 1,
    "orderDate": "2024-01-15",
    "totalAmount": 299.99,
    "itemCount": 2
  }'

# 2. 查看同步统计
curl http://localhost:8080/api/analytics/sync/statistics

# 3. 查看销售数据
curl "http://localhost:8080/api/analytics/sales/by-category"
```

## 架构特点

### 1. 高可用性

- Kafka 分区并行消费 (可配置并发度)
- HikariCP 连接池 (最大 20 连接)
- 手动确认 (Manual Batch Acknowledgment)
- 自动重试机制

### 2. 数据一致性

- 事务支持 (数据库级)
- 幂等操作 (事实表 UPSERT)
- 同步日志审计
- 错误恢复机制

### 3. 性能优化

- 批量处理 (500 条/批)
- 消息压缩 (Snappy)
- 索引优化 (覆盖索引)
- Redis 缓存层

### 4. 可观测性

- Spring Actuator 健康检查
- Kafka 消费者监控
- 同步日志审计
- 错误跟踪

## 扩展计划

- [ ] 实现动态数据源配置
- [ ] 添加异常处理和 DLQ (死信队列)
- [ ] 支持实时指标推送 (Prometheus)
- [ ] 数据质量校验规则
- [ ] 增量同步优化
- [ ] 多区域数据同步

## 故障排查

### 消费者无法连接到 Kafka

```
错误: "Could not connect to Kafka"
解决: 检查 bootstrap-servers 配置和网络连接
docker-compose logs kafka
```

### 数据仓库连接失败

```
错误: "Access denied for user 'root'"
解决: 确保数据库已初始化，检查凭证
docker-compose logs warehouse-db
```

### ETL 处理缓慢

```
优化方案:
1. 增加 Kafka 分区数
2. 提高消费者并发度 (KafkaListenerContainerFactory.setConcurrency)
3. 调整批处理大小 (app.etl.batch-size)
4. 检查数据库索引
```

## 监控命令

```bash
# 查看 Kafka Topic
docker exec warehouse-kafka kafka-topics --bootstrap-server localhost:9092 --list

# 查看消费者状态
docker exec warehouse-kafka kafka-consumer-groups --bootstrap-server localhost:9092 \
  --group warehouse-etl-group --describe

# 查看数据库同步日志
docker exec warehouse-db mysql -u root -proot ecommerce_warehouse \
  -e "SELECT * FROM sync_log ORDER BY sync_time DESC LIMIT 10;"

# 查看共享卷容量
du -sh ./backend
```

## 更新日志

### v1.0.0 (2026-03-30)

- ✅ 完整的 Kafka 消费者实现
- ✅ 多数据源配置支持
- ✅ ETL 核心逻辑完成
- ✅ REST API 框架
- ✅ Docker 支持

---

**作者**: University of Windsor eCommerce Team  
**维护**: Infrastructure & Data Engineering  
**最后更新**: 2026-03-30

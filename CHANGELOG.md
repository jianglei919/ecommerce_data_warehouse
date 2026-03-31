# 变更日志 (Changelog)

所有对本项目的重要变更都将被记录在此文件中。

## 格式说明

遵循 [Keep a Changelog](https://keepachangelog.com/en/1.0.0/) 的风格。

## 版本历史

### [Unreleased]

### [V2] - 2026-03-30

#### 新增 (Added)

- **数据库层**
  - 新建 `unified_orders` 表：统一订单主表，包含来自App和Web两个系统的订单数据
  - 新建 `unified_order_items` 表：统一订单明细表，包含订单关联的商品信息
  - 复合唯一约束 `(source, app_order_id, web_order_no)` 用于确保跨源订单的唯一性
  - 示例数据：16条统一订单 + 27条订单明细项

- **后端服务**
  - 新建 `UnifiedOrder.java` 领域模型类
  - 新建 `UnifiedOrderItem.java` 领域模型类
  - 新建 `UnifiedOrderMapper.java` MyBatis-Plus数据访问层
  - 新建 `UnifiedOrderItemMapper.java` MyBatis-Plus数据访问层
  - 新建 `UnifiedOrdersController.java` REST API控制器，提供6个端点：
    - `GET /api/unified-orders` - 获取订单列表（支持分页、过滤）
    - `GET /api/unified-orders/{id}` - 获取订单详情及关联项
    - `GET /api/unified-orders/overview` - 仪表板概览统计
    - `GET /api/unified-orders/stats/by-source` - 按来源统计
    - `GET /api/unified-orders/stats/product-sales` - 商品销售分析
    - `GET /api/unified-orders/by-source/{source}` - 按来源查询

- **前端页面**
  - 新建 `UnifiedOrders.vue` Vue3单文件组件，包含：
    - 概览统计卡片（总订单数、APP订单数、WEB订单数）
    - 数据源和状态过滤器
    - 分页订单列表表格
    - 订单详情弹窗
    - 源数据统计展示
  - 添加路由：`/orders` 指向 UnifiedOrders 组件
  - 更新导航菜单：添加"统一订单"链接

- **部署配置**
  - V2迁移SQL脚本：`sql/04-v2-migration.sql`
  - V2需求设计文档：`V2_REQUIREMENTS.md`

#### 变更 (Changed)

- **依赖更新**
  - 更新 Lombok 版本至 1.18.30（修复Java 25兼容性问题）

- **配置项**
  - 添加 `@MapperScan("com.uwindsor.warehouse.mapper")` 注解用于MyBatis mapper扫描

- **数据库设计**
  - 扩展 `ecommerce_warehouse` 数据仓库库，新增统一订单相关表
  - 添加表索引以提升查询性能

#### 修复 (Fixed)

- 修复 Lombok 与 Java 25 的编译兼容性问题
- 优化 MyBatis mapper 扫描配置

#### 文档更新 (Documentation)

- 更新 `REQUIREMENTS.md` 中文需求文档，添加V2阶段完整设计说明
- 更新 `REQUIREMENTS_EN.md` 英文需求文档，添加V2阶段完整设计说明
- 创建 `CHANGELOG.md` 用于记录项目变更历史

#### 测试验证

- ✅ 所有6个API端点功能测试通过
- ✅ 前端UI组件功能测试通过
- ✅ Docker容器正常启动和运行

#### 技术栈

**后端**

- Spring Boot 3.0.13
- MyBatis-Plus 3.5.4.1
- Lombok 1.18.30
- JdbcTemplate for flexible data access

**前端**

- Vue 3
- TypeScript
- Ant Design Vue
- Axios

**部署**

- Docker Compose
- MySQL 8.0
- 9个容器服务

---

### [Phase 4] - 2026-03-29

#### 新增 (Added)

- 完成Docker部署
- 9个微服务容器成功启动
- 后端API测试通过
- 前端应用部署完成

#### 修复 (Fixed)

- 修复Spring Boot 3.0兼容性问题
- 修复Redis连接问题
- 修复Kafka配置问题
- 修复前端构建问题

---

## 版本对比

### V2 vs Phase 4

| 功能         | Phase 4 | V2  |
| ------------ | ------- | --- |
| 订单管理     | ❌      | ✅  |
| 统一数据视图 | ❌      | ✅  |
| 订单分析     | ❌      | ✅  |
| 管理仪表板   | ❌      | ✅  |
| 销量分析     | ✅      | ✅  |
| 评论排行     | ✅      | ✅  |

---

## 未来规划 (Roadmap)

### Phase V3 - 计划中

- [ ] Fact表基于统一订单表重构
- [ ] ETL管道优化
- [ ] 报表导出功能
- [ ] 数据验证规则完善
- [ ] 性能监控面板

### Phase V4 - 计划中

- [ ] 实时数据流处理
- [ ] 预测分析模块
- [ ] 高级可视化
- [ ] 多语言支持

---

## 贡献指南

请参考 [CONTRIBUTING.md](./CONTRIBUTING.md) 了解如何贡献代码。

## 许可证

通过提交拉取请求或问题，您同意遵守项目许可证条款。

---

**最后更新**: 2026-03-30
**当前版本**: V2
**维护者**: UWindsor eCommerce Team

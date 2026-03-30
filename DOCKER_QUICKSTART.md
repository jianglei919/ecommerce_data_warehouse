# 🐳 Docker 一键启动指南

## 快速开始（一命令启动）

```bash
docker-compose up -d
```

---

## 📋 系统架构（多源库汇聚）

```
┌─────────────────────────────────────────────────────────┐
│                    Docker Network                       │
│                  ecommerce-network                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌──────────────┐    ┌──────────────┐                  │
│  │ MySQL (3306) │    │   Backend    │    ┌──────────┐  │
│  │              │    │   (8080)     │    │ Frontend │  │
│  │ ┌──────────┐ │    │              │    │ (5173)   │  │
│  │ │source_1  │◄┼────┤ Java API     │◄───┤ Vue 3    │  │
│  │ │source_2  │ │    │              │    │          │  │
│  │ │warehouse │ │    │ Elasticsearch   │ Vite     │  │
│  │ │  (ETL)   │ │    │              │    │          │  │
│  │ └──────────┘ │    └──────────────┘    └──────────┘  │
│  │              │                                       │
│  └──────────────┘                                       │
│                                                         │
└─────────────────────────────────────────────────────────┘

数据流向：
source_1 ──┐
           ├──► 数据仓库 warehouse ──► 分析查询
source_2 ──┘
```

---

## 🚀 启动步骤

### Step 1: 启动所有服务

```bash
# 在项目根目录执行
docker-compose up -d
```

**输出示例**:

```
Creating ecommerce_mysql    ... done
Creating ecommerce_backend  ... done
Creating ecommerce_frontend ... done
```

### Step 2: 检查服务状态

```bash
# 查看所有运行中的容器
docker-compose ps

# 预期输出
NAME                    STATUS              PORTS
ecommerce_mysql         Up 2 minutes        3306/tcp, 0.0.0.0:3306->3306/tcp
ecommerce_backend       Up 1 minute         0.0.0.0:8080->8080/tcp
ecommerce_frontend      Up 30 seconds       0.0.0.0:5173->5173/tcp
```

### Step 3: 等待服务就绪

```bash
# 监控日志（Ctrl+C 停止）
docker-compose logs -f backend mysql frontend

# 等待这些日志信息出现：
# mysql      | [Server] Ready for connections
# backend    | Started Application in X seconds
# frontend   | > serve -s dist -l 5173
```

---

## 🌐 访问服务

### 📊 前端仪表板

- **URL**: http://localhost:5173
- **功能**: 查看 5 大分析图表
- **浏览器**: Chrome/Firefox/Safari

### 📖 API 文档 (Swagger)

- **URL**: http://localhost:8080/swagger-ui.html
- **功能**: 查看和测试所有 API
- **登录**: 直接访问，无需登录

### 🔧 MySQL 数据库

- **主机**: localhost
- **端口**: 3306
- **用户名**: root
- **密码**: root
- **数据库**:
  - `ecommerce_source_1` (业务源库1)
  - `ecommerce_source_2` (业务源库2)
  - `ecommerce_warehouse` (数据仓库)

**使用 MySQL Workbench/DBeaver 连接**:

```
Host: 127.0.0.1
Port: 3306
Username: root
Password: root
```

---

## 📊 数据库说明

### 业务源库1 (ecommerce_source_1)

- **包含数据**: 北京/上海/广州的订单数据
- **用户数**: 3 个
- **商品数**: 4 个（手机、电脑、配件等）
- **订单数**: 4 笔
- **用途**: 分店1的业务系统

### 业务源库2 (ecommerce_source_2)

- **包含数据**: 深圳/杭州/成都的订单数据
- **用户数**: 3 个
- **商品数**: 4 个（平板、电脑、配件、可穿戴等）
- **订单数**: 4 笔
- **用途**: 分店2的业务系统

### 数据仓库 (ecommerce_warehouse)

- **核心表**: fact_sales, dim_product_analysis, fact_sales_by_season 等
- **数据来源**: 从 source_1 和 source_2 汇聚
- **用途**: 集团级数据分析、KPI 统计、报表生成
- **特点**: 包含 source_flag 标记，可区分数据来源

---

## 🔗 API 端点列表

### 热销商品分析

```bash
GET http://localhost:8080/api/products/top-sales
GET http://localhost:8080/api/products/top-reviews
GET http://localhost:8080/api/products/combined-top
```

### 销售分析

```bash
GET http://localhost:8080/api/sales/by-season
GET http://localhost:8080/api/sales/by-category
```

### 分析指标

```bash
GET http://localhost:8080/api/analytics/aov
GET http://localhost:8080/api/analytics/return-rate
GET http://localhost:8080/api/analytics/daily-kpi
```

---

## 🐛 常见问题

### Q1: 启动后前端无法连接后端

**症状**: 前端显示 CORS 错误或网络错误

**解决方案**:

```bash
# 1. 检查后端是否成功启动
docker-compose logs backend | grep "Started Application"

# 2. 确认后端容器是否正常运行
docker-compose ps backend

# 3. 检查网络连接
docker-compose exec frontend curl http://backend:8080/swagger-ui.html

# 4. 如果仍然失败，重启后端
docker-compose restart backend
```

### Q2: 数据库连接失败

**症状**: Backend 日志显示 "Connection refused"

**解决方案**:

```bash
# 1. 检查 MySQL 是否完全启动
docker-compose logs mysql | grep "Ready for connections"

# 2. 重启 MySQL 容器
docker-compose restart mysql

# 3. 等待 2-3 分钟再尝试连接

# 4. 查看完整日志
docker-compose logs mysql
```

### Q3: 端口被占用

**症状**: 错误信息：`bind: address already in use`

**解决方案**:

```bash
# 查看谁占用了端口
lsof -i :8080    # 查看 8080 端口
lsof -i :5173    # 查看 5173 端口
lsof -i :3306    # 查看 3306 端口

# 方案1: 停止占用端口的进程
kill -9 <PID>

# 方案2: 修改 docker-compose.yml 中的端口映射
# 例如: "8080:8080" 改为 "8888:8080"
```

### Q4: 修改代码后需要重新构建

**症状**: 修改代码后变更没有生效

**解决方案**:

```bash
# 重新构建镜像并启动
docker-compose up -d --build

# 或者单独重建某个服务
docker-compose build --no-cache backend
docker-compose up -d backend
```

---

## 📝 常用命令

### 启动/停止/重启

```bash
# 启动所有服务
docker-compose up -d

# 停止所有服务（保留数据）
docker-compose stop

# 停止并删除容器（保留数据）
docker-compose down

# 完全删除（包括数据）
docker-compose down -v

# 重启服务
docker-compose restart backend
docker-compose restart mysql
docker-compose restart frontend
```

### 查看日志

```bash
# 查看所有日志
docker-compose logs

# 实时监控日志（Ctrl+C 停止）
docker-compose logs -f

# 查看特定服务日志
docker-compose logs backend
docker-compose logs mysql
docker-compose logs frontend

# 查看最后 100 行日志
docker-compose logs --tail 100
```

### 进入容器

```bash
# 进入 MySQL 容器
docker-compose exec mysql bash

# 进入 MySQL 命令行
docker-compose exec mysql mysql -u root -p

# 进入后端容器
docker-compose exec backend bash

# 进入前端容器
docker-compose exec frontend sh
```

### 查看容器信息

```bash
# 查看所有容器状态
docker-compose ps

# 查看容器详细信息
docker-compose ps -a

# 查看容器资源使用情况
docker stats
```

---

## 🧹 清理和重置

### 清理所有数据

```bash
# 删除容器、网络和卷（包括数据）
docker-compose down -v

# 确认数据已删除
docker-compose ps
docker volume ls
```

### 重新初始化数据库

```bash
# 1. 停止服务
docker-compose down

# 2. 删除 MySQL 卷
docker volume rm ecommerce_data_warehouse_mysql_data

# 3. 重新启动
docker-compose up -d
```

---

## 📊 验证部署成功

### 检查清单

- [ ] MySQL 容器正常运行: `docker-compose ps mysql`
- [ ] 后端容器正常运行: `docker-compose ps backend`
- [ ] 前端容器正常运行: `docker-compose ps frontend`
- [ ] MySQL 能连接: 在 localhost:3306 连接成功
- [ ] 后端 Swagger 可访问: http://localhost:8080/swagger-ui.html
- [ ] 前端仪表板可访问: http://localhost:5173
- [ ] 前端能加载 5 个图表
- [ ] API 数据返回正确

### 查看业务数据

```bash
# 进入 MySQL
docker-compose exec mysql mysql -u root -proot

# 在 MySQL shell 中执行
USE ecommerce_source_1;
SELECT COUNT(*) FROM orders;  -- 应显示 4 条订单

USE ecommerce_source_2;
SELECT COUNT(*) FROM orders;  -- 应显示 4 条订单

USE ecommerce_warehouse;
SELECT COUNT(*) FROM fact_sales;  -- 应显示汇聚的销售数据
```

---

## 📈 性能优化建议

### 内存配置

如果容器运行缓慢，增加分配内存：

```bash
# 编辑 docker-compose.yml
services:
  mysql:
    environment:
      # 增加 MySQL 内存
      MYSQL_ROOT_PASSWORD: root
    deploy:
      resources:
        limits:
          memory: 1G  # 改为 2G 或更高

  backend:
    deploy:
      resources:
        limits:
          memory: 1G  # 改为 2G 或更高
```

### 数据库查询优化

```sql
-- 为常用查询字段添加索引
USE ecommerce_warehouse;

CREATE INDEX idx_product_id ON fact_sales(product_id);
CREATE INDEX idx_sale_date ON fact_sales(sale_date);
CREATE INDEX idx_category ON fact_sales(category);
```

---

## 🔄 备份和恢复

### 备份数据库

```bash
# 备份 MySQL 容器中的所有数据库
docker-compose exec mysql mysqldump -u root -proot --all-databases > backup.sql

# 备份特定数据库
docker-compose exec mysql mysqldump -u root -proot ecommerce_warehouse > warehouse_backup.sql
```

### 恢复数据库

```bash
# 恢复所有数据库
docker-compose exec -T mysql mysql -u root -proot < backup.sql

# 恢复特定数据库
docker-compose exec -T mysql mysql -u root -proot ecommerce_warehouse < warehouse_backup.sql
```

---

## 📞 技术支持

- 查看详细文档: [DOCUMENTATION_INDEX.md](../DOCUMENTATION_INDEX.md)
- 查看项目架构: [JAVA_ARCHITECTURE.md](../JAVA_ARCHITECTURE.md)
- 查看数据库设计: [DATABASE_DESIGN.md](../DATABASE_DESIGN.md)

---

**祝你使用愉快！** 🎉

如有问题，请查看日志或联系技术支持。

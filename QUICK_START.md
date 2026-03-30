# Java Demo 项目 - 快速开始指南

## 项目概览

这是一个**电商数据仓库Java演示项目**，包含：

- ✅ 两个MySQL数据库（业务库 + 分析库）
- ✅ 6张业务表 + 5张分析表
- ✅ 完整的Java后端API（Spring Boot + MyBatis Plus）
- ✅ Vue.js前端可视化仪表板
- ✅ 5大核心分析功能

---

## 核心功能

### 1. 热销商品分析

- **按销量排行** - TOP 10 商品（按销量排序）
- **按评分排行** - TOP 10 商品（按评分排序，需要≥5条评论）
- **综合排分** - 结合销量(60%)和评分(40%)的综合排名

### 2. 季节销售分析

- 按商品**类别**统计
- 按**季节**维度（春/夏/秋/冬）
- 展示**销量和销售额**对比
- 支持**年度切换**

### 3. 平均订单价值(AOV)分析

- 支持**日/周/月**维度查看
- 显示**最高/最低/平均** AOV
- 趋势变化**曲线图**展示
- 数据**实时更新**

### 4. 商品退货率分析

- 商品级别退货率
- 类别级别统计
- 整体退货率**饼图**展示
- 详细数据**表格**查看

### 5. KPI日报统计

- 总销售额
- 订单数量
- 退货率
- 新增用户数
- 日度环比对比

---

## 快速开始（5步）

### 第1步：创建数据库

```bash
# 登录MySQL
mysql -u root -p

# 创建两个数据库
CREATE DATABASE ecommerce_source CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE ecommerce_warehouse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 第2步：初始化表结构

```bash
# 执行建表脚本
mysql -u root -p ecommerce_source < sql/source_db.sql
mysql -u root -p ecommerce_warehouse < sql/warehouse_db.sql

# 插入测试数据
mysql -u root -p ecommerce_source < sql/sample_data.sql
```

### 第3步：启动Java后端

```bash
# 切换到Java项目目录
cd ecommerce-warehouse-java

# 配置数据库连接 (修改 application.yml)
vi src/main/resources/application.yml

# 编译和运行
mvn clean package
java -jar target/ecommerce-warehouse-demo-1.0.0.jar

# 或者直接运行
mvn spring-boot:run
```

**后端URL**: `http://localhost:8080`
**Swagger API文档**: `http://localhost:8080/swagger-ui.html`

### 第4步：启动前端

```bash
# 切换到Vue项目目录
cd ecommerce-warehouse-frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

**前端URL**: `http://localhost:5173`

### 第5步：访问系统

打开浏览器访问：**http://localhost:5173**

看到仪表板页面，包含：

- 4个KPI卡片
- 2个热销商品图表
- 1个季节销售热力图
- 1个AOV趋势图
- 1个退货率饼图

---

## 项目结构全景

```
ecommerce-warehouse-demo/
│
├── 📄 README.md                          # 项目总览(开始这里)
├── 📄 DEMO_REQUIREMENTS.md               # 需求文档
├── 📄 DEMO_DATABASE_DESIGN.md            # 数据库设计
├── 📄 DEMO_JAVA_ARCHITECTURE.md          # Java架构
├── 📄 DEMO_FRONTEND_DESIGN.md            # 前端设计
├── 📄 DEMO_QUICK_START.md                # 快速开始(本文件)
│
├── 📁 backend/                           # Java后端
│   ├── pom.xml
│   ├── src/
│   │   ├── main/java/com/example/
│   │   │   ├── config/                  # 多数据源配置
│   │   │   ├── controller/              # API控制器
│   │   │   ├── service/                 # 业务逻辑
│   │   │   ├── mapper/                  # 数据访问
│   │   │   ├── entity/                  # 数据实体
│   │   │   ├── dto/                     # 数据对象
│   │   │   └── Application.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── mapper/                  # SQL XML
│   └── sql/
│       ├── source_db.sql                # 建表脚本
│       ├── warehouse_db.sql
│       └── sample_data.sql
│
├── 📁 frontend/                          # Vue前端
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   ├── src/
│   │   ├── api/                         # API调用
│   │   ├── pages/                       # 页面组件
│   │   ├── components/                  # 通用组件
│   │   ├── App.vue
│   │   └── main.js
│   └── public/
│
└── 📁 docs/                              # 文档
    ├── 部署说明.md
    ├── API文档.md
    └── 开发指南.md
```

---

## 数据库连接参数

### 业务数据库 (ecommerce_source)

```
Host: localhost
Port: 3306
Database: ecommerce_source
Username: root
Password: 123456

表结构：
- users (用户)
- products (商品)
- orders (订单)
- order_items (订单明细)
- product_reviews (评论)
- returns (退货)
```

### 分析数据库 (ecommerce_warehouse)

```
Host: localhost
Port: 3306
Database: ecommerce_warehouse
Username: root
Password: 123456

表结构：
- fact_sales (销售事实)
- dim_product_analysis (商品分析维度)
- fact_sales_by_season (季节销售)
- fact_returns (退货事实)
- kpi_daily (日KPI)
```

---

## API 端点列表

### 商品分析API

| 端点                         | 方法 | 说明              | 示例      |
| ---------------------------- | ---- | ----------------- | --------- |
| `/api/products/top-sales`    | GET  | 热销商品-按销量   | ?limit=10 |
| `/api/products/top-reviews`  | GET  | 热销商品-按评分   | ?limit=10 |
| `/api/products/combined-top` | GET  | 热销商品-综合评分 | ?limit=10 |

### 销售分析API

| 端点                     | 方法 | 说明         | 示例                    |
| ------------------------ | ---- | ------------ | ----------------------- |
| `/api/sales/by-season`   | GET  | 季节销售分析 | ?year=2024&category=all |
| `/api/sales/by-category` | GET  | 品类销售统计 | ?year=2024              |

### 分析API

| 端点                         | 方法 | 说明         | 示例             |
| ---------------------------- | ---- | ------------ | ---------------- |
| `/api/analytics/aov`         | GET  | 平均订单价值 | ?dimension=day   |
| `/api/analytics/return-rate` | GET  | 退货率分析   | ?type=product    |
| `/api/analytics/daily-kpi`   | GET  | 日KPI统计    | ?date=2024-01-01 |

---

## 测试API

### 使用Swagger UI

1. 启动后端服务
2. 打开浏览器：`http://localhost:8080/swagger-ui.html`
3. 在Swagger界面中测试各个API

### 使用CURL

```bash
# 获取热销商品TOP 10（按销量）
curl -X GET "http://localhost:8080/api/products/top-sales?limit=10"

# 获取季节销售数据
curl -X GET "http://localhost:8080/api/sales/by-season?year=2024&category=all"

# 获取AOV数据
curl -X GET "http://localhost:8080/api/analytics/aov?dimension=day"
```

### 使用Postman

1. 新建Collection: "E-Commerce Demo"
2. 添加GET请求到 `{{base_url}}/api/products/top-sales`
3. 设置参数 `limit=10`
4. 点击Send

---

## 常见问题排查

### Q1: 数据库连接失败？

**错误**: `Connection refused` 或 `Access denied`

**解决**:

1. 检查MySQL是否运行：`mysql -u root -p`
2. 检查用户名密码是否正确
3. 修改 `application.yml` 中的数据库配置

### Q2: 后端启动报错？

**错误**: `Table doesn't exist` 或 `Unknown database`

**解决**:

1. 确认建表脚本已执行
2. 检查数据库名称是否匹配
3. 查看日志获取详细错误信息

### Q3: 前端页面空白？

**错误**: 图表不显示 或 数据为空

**解决**:

1. 打开浏览器开发者工具(F12)
2. 检查Network标签中API请求是否成功
3. 检查Console中是否有错误信息
4. 确保后端服务正常运行

### Q4: 跨域问题？

**错误**: `Access to XMLHttpRequest blocked by CORS`

**解决**: 在后端 `Application.java` 中添加CORS配置：

```java
@Bean
public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/api/**")
                    .allowedOrigins("http://localhost:5173")
                    .allowedMethods("GET", "POST", "PUT", "DELETE");
        }
    };
}
```

---

## 性能优化建议

### 后端优化

1. **数据库索引**

   ```sql
   CREATE INDEX idx_user_id ON orders(user_id);
   CREATE INDEX idx_order_date ON orders(order_date);
   CREATE INDEX idx_product_id ON order_items(product_id);
   ```

2. **缓存配置** (可选)

   ```java
   @Cacheable("products")
   public List<HotProductDTO> getTopProducts() { ... }
   ```

3. **分页查询**
   ```java
   Page<HotProductDTO> page = productService.page(pageNo, pageSize);
   ```

### 前端优化

1. **图表懒加载**

   ```javascript
   const lazyChart = defineAsyncComponent(
     () => import("./HotProductChart.vue"),
   );
   ```

2. **数据缓存**

   ```javascript
   const cache = new Map();
   cache.set("products", data);
   ```

3. **请求去重**
   ```javascript
   if (lastRequest !== currentRequest) {
     fetchData();
   }
   ```

---

## 部署到生产环境

### Docker 部署

#### 后端Docker化

```dockerfile
FROM java:11
COPY target/ecommerce-warehouse-demo-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
EXPOSE 8080
```

```bash
# 构建镜像
docker build -t ecommerce-backend:1.0 .

# 运行容器
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql-server:3306/ecommerce_source \
  ecommerce-backend:1.0
```

#### 前端Docker化

```dockerfile
FROM node:16 as build
WORKDIR /app
COPY . .
RUN npm install && npm run build

FROM nginx
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
```

```bash
# 构建镜像
docker build -t ecommerce-frontend:1.0 .

# 运行容器
docker run -p 80:80 ecommerce-frontend:1.0
```

### Docker Compose 一键启动

```yaml
version: "3"

services:
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: 123456
    volumes:
      - ./sql:/docker-entrypoint-initdb.d

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    depends_on:
      - mysql

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

启动命令：

```bash
docker-compose up -d
```

---

## 开发日记和检查清单

### 功能开发进度

- [x] 数据库设计
- [x] Java后端框架
- [x] API端点实现
- [x] 前端仪表板UI
- [x] 热销商品分析
- [x] 季节销售分析
- [x] AOV计算展示
- [x] 退货率分析
- [ ] 缓存优化（可选）
- [ ] 权限管理（可选）
- [ ] 导出功能（可选）

### 测试检查清单

- [ ] 数据库连接测试
- [ ] API端点测试 (Postman)
- [ ] 前端页面加载测试
- [ ] 图表展示测试
- [ ] 响应时间测试 (<1秒)
- [ ] 并发用户测试 (50+)

### 部署检查清单

- [ ] 代码审查
- [ ] 安全配置检查
- [ ] 性能基准测试
- [ ] 灾难恢复演练
- [ ] 运维文档准备
- [ ] 监控告警配置

---

## 下一步扩展方向

### Phase 2 - 高级功能

- [ ] 用户分析模块
- [ ] 库存预测
- [ ] 推荐系统
- [ ] 实时数据更新

### Phase 3 - 企业级功能

- [ ] 权限管理系统
- [ ] 数据导出(Excel/PDF)
- [ ] 定时任务调度
- [ ] 数据报警机制
- [ ] 用户行为追踪

### Phase 4 - 高级分析

- [ ] 机器学习模型集成
- [ ] 预测性分析
- [ ] 异常检测
- [ ] 智能推荐

---

## 技术支持

- 📖 查看详细文档：`DEMO_REQUIREMENTS.md`
- 🗄️ 数据库设计：`DEMO_DATABASE_DESIGN.md`
- ☕ Java代码：`DEMO_JAVA_ARCHITECTURE.md`
- 🎨 前端设计：`DEMO_FRONTEND_DESIGN.md`

---

## 许可证

MIT License - 可自由使用和修改

---

**准备好了吗？现在就开始第1步：创建数据库！** 🚀

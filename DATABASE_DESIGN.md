# Java Demo 项目 - 数据库设计

## 数据库1：业务原始数据库 (ecommerce_source)

### 1. 用户表 (users)

```sql
CREATE TABLE users (
    user_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(20),
    city VARCHAR(50),
    register_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. 商品表 (products)

```sql
CREATE TABLE products (
    product_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    cost DECIMAL(10,2),
    brand VARCHAR(50),
    stock_qty INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_category (category),
    INDEX idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. 订单表 (orders)

```sql
CREATE TABLE orders (
    order_id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    order_date DATE NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) DEFAULT 'completed',  -- completed, cancelled, returned
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_order_date (order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. 订单明细表 (order_items)

```sql
CREATE TABLE order_items (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    line_total DECIMAL(12,2) NOT NULL,  -- quantity * unit_price
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. 商品评论表 (product_reviews)

```sql
CREATE TABLE product_reviews (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    user_id INT,
    rating INT CHECK (rating >= 1 AND rating <= 5),  -- 1-5星
    comment TEXT,
    review_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    FOREIGN KEY (user_id) REFERENCES users(user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_rating (rating)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6. 退货表 (returns)

```sql
CREATE TABLE returns (
    return_id INT PRIMARY KEY AUTO_INCREMENT,
    order_id INT NOT NULL,
    product_id INT NOT NULL,
    return_date DATE,
    return_qty INT DEFAULT 1,
    reason VARCHAR(100),
    status VARCHAR(20) DEFAULT 'pending',  -- pending, approved, rejected
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES orders(order_id),
    FOREIGN KEY (product_id) REFERENCES products(product_id),
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 数据库2：分析数据仓库 (ecommerce_warehouse)

### 1. 销售事实表 (fact_sales)

```sql
CREATE TABLE fact_sales (
    sales_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    product_name VARCHAR(200),
    category VARCHAR(50),
    price DECIMAL(10,2),
    quantity INT,
    sales_amount DECIMAL(12,2),
    sale_date DATE,
    season VARCHAR(10),  -- Spring, Summer, Fall, Winter
    year INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_category (category),
    INDEX idx_sale_date (sale_date),
    INDEX idx_season (season)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. 商品分析维度表 (dim_product_analysis)

```sql
CREATE TABLE dim_product_analysis (
    analysis_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL UNIQUE,
    name VARCHAR(200),
    category VARCHAR(50),
    brand VARCHAR(50),
    avg_rating DECIMAL(3,2),  -- 平均评分
    total_reviews INT,  -- 总评论数
    total_sales_qty INT,  -- 总销量
    total_sales_amount DECIMAL(15,2),  -- 总销售额
    return_rate DECIMAL(5,4),  -- 退货率
    rank_by_sales INT,  -- 销量排名
    rank_by_rating INT,  -- 评分排名
    combined_score DECIMAL(5,2),  -- 综合评分
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. 季节销售事实表 (fact_sales_by_season)

```sql
CREATE TABLE fact_sales_by_season (
    season_sales_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    category VARCHAR(50),
    season VARCHAR(10),  -- Spring, Summer, Fall, Winter
    year INT,
    total_qty INT,
    total_amount DECIMAL(15,2),
    order_count INT,
    avg_order_value DECIMAL(12,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_category (category),
    INDEX idx_season (season)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. 退货事实表 (fact_returns)

```sql
CREATE TABLE fact_returns (
    return_fact_id INT PRIMARY KEY AUTO_INCREMENT,
    product_id INT NOT NULL,
    product_name VARCHAR(200),
    category VARCHAR(50),
    return_qty INT,
    return_date DATE,
    reason VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_product_id (product_id),
    INDEX idx_return_date (return_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. 日KPI表 (kpi_daily)

```sql
CREATE TABLE kpi_daily (
    kpi_id INT PRIMARY KEY AUTO_INCREMENT,
    kpi_date DATE NOT NULL UNIQUE,
    total_orders INT,
    total_sales DECIMAL(15,2),
    avg_order_value DECIMAL(12,2),
    return_rate DECIMAL(5,4),
    total_users INT,
    new_users INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_kpi_date (kpi_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 关键查询

### 1. 热销商品 TOP 10（按销量）

```sql
SELECT
    p.product_id,
    p.name,
    p.category,
    SUM(oi.quantity) as total_qty,
    SUM(oi.line_total) as total_amount,
    COUNT(DISTINCT o.order_id) as order_count,
    ROUND(SUM(oi.line_total) / SUM(oi.quantity), 2) as avg_price
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.status = 'completed'
GROUP BY p.product_id, p.name, p.category
ORDER BY total_qty DESC
LIMIT 10;
```

### 2. 商品评分 TOP 10

```sql
SELECT
    p.product_id,
    p.name,
    p.category,
    ROUND(AVG(pr.rating), 2) as avg_rating,
    COUNT(pr.review_id) as review_count,
    SUM(CASE WHEN pr.rating >= 4 THEN 1 ELSE 0 END) as positive_count
FROM products p
LEFT JOIN product_reviews pr ON p.product_id = pr.product_id
GROUP BY p.product_id, p.name, p.category
HAVING COUNT(pr.review_id) >= 5
ORDER BY avg_rating DESC, review_count DESC
LIMIT 10;
```

### 3. 按类别和季节分析销量

```sql
SELECT
    p.category,
    CASE
        WHEN MONTH(o.order_date) IN (3,4,5) THEN 'Spring'
        WHEN MONTH(o.order_date) IN (6,7,8) THEN 'Summer'
        WHEN MONTH(o.order_date) IN (9,10,11) THEN 'Fall'
        ELSE 'Winter'
    END as season,
    YEAR(o.order_date) as year,
    SUM(oi.quantity) as total_qty,
    SUM(oi.line_total) as total_amount
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.status = 'completed'
GROUP BY p.category, season, year
ORDER BY year DESC, category;
```

### 4. 平均订单价值（AOV）

```sql
SELECT
    DATE(o.order_date) as order_date,
    YEAR(o.order_date) as year,
    MONTH(o.order_date) as month,
    WEEK(o.order_date) as week,
    COUNT(o.order_id) as total_orders,
    SUM(o.total_amount) as total_sales,
    ROUND(SUM(o.total_amount) / COUNT(o.order_id), 2) as aov
FROM orders o
WHERE o.status = 'completed'
GROUP BY year, month, DATE(o.order_date)
ORDER BY order_date DESC;
```

### 5. 商品退货率

```sql
SELECT
    p.product_id,
    p.name,
    p.category,
    (SELECT COUNT(*) FROM returns r WHERE r.product_id = p.product_id) as return_count,
    (SELECT SUM(oi.quantity) FROM order_items oi
     JOIN orders o ON oi.order_id = o.order_id
     WHERE oi.product_id = p.product_id AND o.status = 'completed') as total_sold,
    ROUND(
        (SELECT COUNT(*) FROM returns r WHERE r.product_id = p.product_id) * 100 /
        (SELECT SUM(oi.quantity) FROM order_items oi
         JOIN orders o ON oi.order_id = o.order_id
         WHERE oi.product_id = p.product_id AND o.status = 'completed'),
        2
    ) as return_rate_percent
FROM products p
HAVING total_sold > 0
ORDER BY return_rate_percent DESC
LIMIT 20;
```

### 6. 按季节的类别销量对比

```sql
SELECT
    p.category,
    CASE
        WHEN MONTH(o.order_date) IN (3,4,5) THEN 'Spring'
        WHEN MONTH(o.order_date) IN (6,7,8) THEN 'Summer'
        WHEN MONTH(o.order_date) IN (9,10,11) THEN 'Fall'
        ELSE 'Winter'
    END as season,
    SUM(oi.quantity) as qty,
    ROUND(SUM(oi.line_total), 2) as amount
FROM order_items oi
JOIN orders o ON oi.order_id = o.order_id
JOIN products p ON oi.product_id = p.product_id
WHERE o.status = 'completed'
GROUP BY p.category, season
ORDER BY category,
    CASE season WHEN 'Spring' THEN 1 WHEN 'Summer' THEN 2 WHEN 'Fall' THEN 3 ELSE 4 END;
```

---

## 初始化脚本

### 样本数据插入

```sql
-- 插入用户样本数据
INSERT INTO users (name, email, city, register_date) VALUES
('张三', 'zhangsan@example.com', '北京', '2023-01-15'),
('李四', 'lisi@example.com', '上海', '2023-02-20'),
('王五', 'wangwu@example.com', '深圳', '2023-03-10');

-- 插入商品样本数据
INSERT INTO products (name, category, price, cost, brand) VALUES
('iPhone 14 Pro', 'Electronics', 7999.00, 5000.00, 'Apple'),
('华为 Mate 50', 'Electronics', 4999.00, 3000.00, 'Huawei'),
('南孚电池 4节', 'Accessories', 19.99, 8.00, '南孚'),
('苹果笔记本', 'Electronics', 12999.00, 8000.00, 'Apple'),
('小米手环6', 'Wearables', 249.00, 100.00, 'Xiaomi');

-- 插入订单样本数据
-- 注意：需要根据实际user_id和product_id调整
```

---

## 数据库连接配置（Spring Boot）

### application.yml

```yaml
spring:
  datasource:
    # 主库（业务数据库）
    primary:
      url: jdbc:mysql://localhost:3306/ecommerce_source?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver

    # 从库（分析库）
    warehouse:
      url: jdbc:mysql://localhost:3306/ecommerce_warehouse?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=Asia/Shanghai
      username: root
      password: 123456
      driver-class-name: com.mysql.cj.jdbc.Driver

mybatis-plus:
  mapper-locations: classpath:mapper/*.xml
  type-aliases-package: com.example.entity
  configuration:
    map-underscore-to-camel-case: true
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

-- =====================================================
-- ecommerce_warehouse - 数据仓库数据库
-- 根据 Ecommerce Data Warehouse Design Report.pdf
-- =====================================================
-- 目的:
--   - 统一存储App和Web的ETL处理结果
--   - 支持OLAP多维度分析
--   - Star Schema: 维度表 (dim_products) + 事实表 (fact_sales_by_product_time)
-- =====================================================

-- 统一订单表: 聚合来自App和Web两个业务源的订单数据
CREATE TABLE unified_orders (
    unified_order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '统一订单ID',
    source VARCHAR(10) NOT NULL COMMENT 'APP 或 WEB',
    app_order_id INT COMMENT 'App系统订单ID',
    web_order_no VARCHAR(50) COMMENT 'Web系统订单号',
    user_id INT NOT NULL COMMENT '用户ID',
    order_date DATE NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_source_order (source, app_order_id, web_order_no),
    KEY idx_source (source),
    KEY idx_order_date (order_date),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_source_date (source, order_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一订单表 (App+Web)';

-- 统一订单项详情表
CREATE TABLE unified_order_items (
    unified_item_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '统一订单项ID',
    unified_order_id INT NOT NULL,
    product_id INT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) COMMENT '商品类别',
    quantity INT NOT NULL DEFAULT 1,
    unit_price DECIMAL(10, 2) NOT NULL,
    subtotal DECIMAL(15, 2) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (unified_order_id) REFERENCES unified_orders (unified_order_id) ON DELETE CASCADE,
    KEY idx_unified_order_id (unified_order_id),
    KEY idx_product_id (product_id),
    KEY idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='统一订单项详情表';

-- =====================================================
-- Star Schema 维度表和事实表
-- =====================================================

-- 商品维度表 (Surrogate Key: product_key)
CREATE TABLE dim_products (
    product_key INT PRIMARY KEY AUTO_INCREMENT COMMENT '维度代理键 (Business Key: product_id)',
    product_id INT NOT NULL COMMENT '业务键 - 商品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category VARCHAR(50) NOT NULL COMMENT '商品类别',
    brand VARCHAR(50) COMMENT '品牌',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_product_id (product_id),
    KEY idx_category (category),
    KEY idx_brand (brand)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品维度表 (Surrogate Key)';

-- 销售事实表 (按商品+时间维度的聚合)
-- OLAP查询通过这个表与维度表JOIN获得多维分析结果
CREATE TABLE fact_sales_by_product_time (
    fact_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '事实表ID',
    product_key INT NOT NULL COMMENT 'FK: dim_products.product_key',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    day INT NOT NULL COMMENT '日期',
    total_quantity INT NOT NULL COMMENT '销售数量',
    total_sales_amount DECIMAL(15, 2) NOT NULL COMMENT '销售金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_key) REFERENCES dim_products (product_key),
    UNIQUE KEY uk_product_time (product_key, year, month, day),
    KEY idx_product_key (product_key),
    KEY idx_year_month_day (year, month, day),
    KEY idx_date_range (year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售事实表 (按商品+时间维度)';

-- =====================================================
-- 样本数据初始化
-- =====================================================

-- 插入维度表数据 (20种商品)
INSERT INTO dim_products (product_id, product_name, category, brand) VALUES
(1, 'iPhone 15 Pro', 'Electronics', 'Apple'),
(2, 'MacBook Pro M3', 'Computers', 'Apple'),
(3, 'Samsung Galaxy S24', 'Electronics', 'Samsung'),
(4, 'iPad Air', 'Electronics', 'Apple'),
(5, 'Dell XPS 15', 'Computers', 'Dell'),
(6, 'Wireless Mouse', 'Accessories', 'Logitech'),
(7, 'USB-C Cable', 'Accessories', 'Generic'),
(8, 'AirPods Pro Max', 'Electronics', 'Apple'),
(9, 'Apple Watch Ultra', 'Electronics', 'Apple'),
(10, 'Phone Charger', 'Accessories', 'Generic'),
(11, 'Screen Protector', 'Accessories', 'Generic'),
(12, 'Pixel 8 Pro', 'Electronics', 'Google'),
(13, 'Sony WH-1000XM5', 'Electronics', 'Sony'),
(14, 'Anker Power Bank', 'Electronics', 'Anker'),
(15, 'Laptop Stand', 'Accessories', 'Generic'),
(16, 'Desk Lamp', 'Accessories', 'Generic'),
(17, 'Samsung Galaxy Buds', 'Electronics', 'Samsung'),
(18, 'Case for Phone', 'Accessories', 'Generic'),
(19, 'Office Chair', 'Furniture', 'IKEA'),
(20, 'Monitor', 'Electronics', 'LG');

-- 插入聚合订单数据
INSERT INTO unified_orders (source, app_order_id, user_id, order_date, total_amount, status) VALUES
('APP', 1001, 100, '2024-01-15', 2899.97, 'completed'),
('APP', 1002, 101, '2024-01-16', 599.99, 'completed'),
('APP', 1003, 102, '2024-01-17', 4099.93, 'completed'),
('APP', 1004, 103, '2024-01-18', 1599.98, 'completed'),
('APP', 1005, 104, '2024-01-19', 1599.98, 'completed'),
('WEB', NULL, 105, '2024-02-01', 2799.97, 'completed'),
('WEB', NULL, 106, '2024-02-02', 3299.96, 'completed'),
('WEB', NULL, 107, '2024-02-03', 1649.96, 'completed'),
('WEB', NULL, 108, '2024-02-04', 2799.94, 'completed'),
('WEB', NULL, 109, '2024-02-05', 3699.93, 'completed'),
('WEB', NULL, 110, '2024-02-06', 1199.97, 'completed'),
('WEB', NULL, 111, '2024-02-07', 1349.94, 'completed'),
('WEB', NULL, 112, '2024-02-08', 1299.95, 'completed'),
('WEB', NULL, 113, '2024-02-09', 2249.97, 'completed'),
('WEB', NULL, 114, '2024-02-10', 3799.92, 'completed');

-- 获取最后插入的订单ID范围，供下面使用
SET @last_order_id = LAST_INSERT_ID();

-- 插入订单项数据 (25条订单项, 来自上面插入的15个订单)
INSERT INTO unified_order_items (unified_order_id, product_id, product_name, category, quantity, unit_price, subtotal) 
SELECT 
    unified_order_id,
    product_id,
    product_name,
    category,
    quantity,
    unit_price,
    subtotal
FROM (
    SELECT @last_order_id - 14 AS unified_order_id, 1 AS product_id, 'iPhone 15 Pro' AS product_name, 'Electronics' AS category, 1 AS quantity, 999.99 AS unit_price, 999.99 AS subtotal
    UNION ALL SELECT @last_order_id - 14, 8, 'AirPods Pro Max', 'Electronics', 1, 599.99, 599.99
    UNION ALL SELECT @last_order_id - 13, 2, 'MacBook Pro M3', 'Computers', 1, 1999.99, 1299.99
    UNION ALL SELECT @last_order_id - 12, 1, 'iPhone 15 Pro', 'Electronics', 1, 999.99, 999.99
    UNION ALL SELECT @last_order_id - 12, 4, 'iPad Air', 'Electronics', 1, 599.99, 599.99
    UNION ALL SELECT @last_order_id - 11, 1, 'iPhone 15 Pro', 'Electronics', 1, 999.99, 999.99
    UNION ALL SELECT @last_order_id - 10, 2, 'MacBook Pro M3', 'Computers', 1, 1999.99, 1299.98
    UNION ALL SELECT @last_order_id - 9, 3, 'Samsung Galaxy S24', 'Electronics', 1, 899.99, 749.97
    UNION ALL SELECT @last_order_id - 8, 4, 'iPad Air', 'Electronics', 2, 599.99, 1199.98
    UNION ALL SELECT @last_order_id - 7, 5, 'Dell XPS 15', 'Computers', 1, 1799.99, 1799.99
    UNION ALL SELECT @last_order_id - 7, 6, 'Wireless Mouse', 'Accessories', 1, 79.99, 79.99
    UNION ALL SELECT @last_order_id - 7, 7, 'USB-C Cable', 'Accessories', 2, 19.99, 39.99
    UNION ALL SELECT @last_order_id - 6, 8, 'AirPods Pro Max', 'Electronics', 1, 599.99, 599.99
    UNION ALL SELECT @last_order_id - 5, 9, 'Apple Watch Ultra', 'Electronics', 1, 799.99, 799.99
    UNION ALL SELECT @last_order_id - 5, 10, 'Phone Charger', 'Accessories', 1, 49.99, 49.99
    UNION ALL SELECT @last_order_id - 5, 11, 'Screen Protector', 'Accessories', 5, 5.99, 29.95
    UNION ALL SELECT @last_order_id - 4, 12, 'Pixel 8 Pro', 'Electronics', 1, 899.99, 899.97
    UNION ALL SELECT @last_order_id - 3, 13, 'Sony WH-1000XM5', 'Electronics', 1, 399.99, 399.99
    UNION ALL SELECT @last_order_id - 3, 14, 'Anker Power Bank', 'Electronics', 1, 49.99, 49.99
    UNION ALL SELECT @last_order_id - 3, 15, 'Laptop Stand', 'Accessories', 1, 49.99, 49.99
    UNION ALL SELECT @last_order_id - 3, 16, 'Desk Lamp', 'Accessories', 1, 99.99, 99.99
    UNION ALL SELECT @last_order_id - 2, 17, 'Samsung Galaxy Buds', 'Electronics', 1, 199.99, 199.99
    UNION ALL SELECT @last_order_id - 2, 18, 'Case for Phone', 'Accessories', 3, 29.99, 89.97
    UNION ALL SELECT @last_order_id - 2, 19, 'Office Chair', 'Furniture', 1, 299.99, 299.99
    UNION ALL SELECT @last_order_id - 2, 20, 'Monitor', 'Electronics', 1, 399.99, 399.99
) temp;

-- 自动生成事实表数据 (从聚合订单项 + 维度表)
INSERT INTO fact_sales_by_product_time (product_key, year, month, day, total_quantity, total_sales_amount) 
SELECT 
    dp.product_key, 
    YEAR(uo.order_date) as year, 
    MONTH(uo.order_date) as month, 
    DAY(uo.order_date) as day,
    SUM(uoi.quantity) as total_quantity, 
    SUM(uoi.subtotal) as total_sales_amount
FROM unified_order_items uoi
JOIN unified_orders uo ON uoi.unified_order_id = uo.unified_order_id
JOIN dim_products dp ON uoi.product_id = dp.product_id
GROUP BY dp.product_key, YEAR(uo.order_date), MONTH(uo.order_date), DAY(uo.order_date)
ON DUPLICATE KEY UPDATE 
    total_quantity = VALUES(total_quantity),
    total_sales_amount = VALUES(total_sales_amount);

-- =====================================================
-- OLAP 查询示例 (按照PDF Section 5)
-- =====================================================

-- 查询1: Rollup - 按类别和时间聚合销售 (按月汇总)
-- SELECT DISTINCT d.category, f.year, f.month,
--        SUM(f.total_quantity) AS monthly_qty,
--        SUM(f.total_sales_amount) AS monthly_sales
-- FROM fact_sales_by_product_time f
-- JOIN dim_products d ON f.product_key = d.product_key
-- GROUP BY d.category, f.year, f.month
-- ORDER BY f.year, f.month, d.category;

-- 查询2: Drilldown - 按商品详细分析某类别的日度销售
-- SELECT d.product_id, d.product_name, d.category, f.year, f.month, f.day,
--        f.total_quantity, f.total_sales_amount
-- FROM fact_sales_by_product_time f
-- JOIN dim_products d ON f.product_key = d.product_key
-- WHERE d.category = 'Electronics' AND f.year = 2024 AND f.month = 1
-- ORDER BY f.year, f.month, f.day, d.product_name;

-- 查询3: Slice - 单个维度的切片分析 (按类别筛选时间趋势)
-- SELECT f.year, f.month, f.day, f.total_sales_amount
-- FROM fact_sales_by_product_time f
-- JOIN dim_products d ON f.product_key = d.product_key
-- WHERE d.category = 'Electronics'
-- ORDER BY f.year, f.month, f.day;

-- 查询4: Dice - 多维度的多值筛选分析
-- SELECT d.category, f.year, f.month,
--        SUM(f.total_quantity) AS qty,
--        SUM(f.total_sales_amount) AS sales
-- FROM fact_sales_by_product_time f
-- JOIN dim_products d ON f.product_key = d.product_key
-- WHERE f.year = 2024 AND f.month IN (1, 2)
--   AND d.category IN ('Electronics', 'Computers', 'Accessories')
-- GROUP BY d.category, f.year, f.month
-- ORDER BY f.month, d.category;

-- 查询5: Pivot - 交叉维度分析 (以行和列展现多维度数据)
-- SELECT d.category,
--        SUM(CASE WHEN f.month = 1 THEN f.total_sales_amount ELSE 0 END) AS Jan_Sales,
--        SUM(CASE WHEN f.month = 2 THEN f.total_sales_amount ELSE 0 END) AS Feb_Sales,
--        SUM(f.total_sales_amount) AS Total_Sales
-- FROM fact_sales_by_product_time f
-- JOIN dim_products d ON f.product_key = d.product_key
-- WHERE f.year = 2024
-- GROUP BY d.category
-- ORDER BY Total_Sales DESC;

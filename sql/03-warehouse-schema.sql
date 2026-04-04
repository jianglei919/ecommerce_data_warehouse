-- =====================================================
-- ecommerce_warehouse - 数据仓库数据库
-- 根据 Ecommerce Data Warehouse Design Report.pdf
-- =====================================================
-- 目的:
--   - 统一存储App和Web的ETL处理结果
--   - 支持OLAP多维度分析
--   - Star Schema: 维度表 (dim_products) + 事实表 (fact_sales_by_product_time)
-- =====================================================
-- 订单维度表: 聚合来自App和Web两个业务源的订单数据
CREATE TABLE
    dim_orders (
        order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
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
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单维度表 (App+Web)';

-- 订单项维度表
CREATE TABLE
    dim_order_items (
        item_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
        order_id INT NOT NULL,
        product_id INT NOT NULL COMMENT '商品ID',
        product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
        category VARCHAR(50) COMMENT '商品类别',
        quantity INT NOT NULL DEFAULT 1,
        unit_price DECIMAL(10, 2) NOT NULL,
        subtotal DECIMAL(15, 2) NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (order_id) REFERENCES dim_orders (order_id) ON DELETE CASCADE,
        KEY idx_order_id (order_id),
        KEY idx_product_id (product_id),
        KEY idx_category (category)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '订单项维度表';

-- =====================================================
-- Star Schema 维度表和事实表
-- =====================================================
-- 商品维度表 (Surrogate Key: product_key)
CREATE TABLE
    dim_products (
        product_key INT PRIMARY KEY AUTO_INCREMENT COMMENT '维度代理键 (Business Key: source+product_id)',
        source VARCHAR(10) NOT NULL COMMENT 'APP 或 WEB 业务源',
        product_id INT NOT NULL COMMENT '业务键 - 商品ID',
        product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
        category VARCHAR(50) NOT NULL COMMENT '商品类别',
        brand VARCHAR(50) COMMENT '品牌',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_source_product (source, product_id),
        KEY idx_source (source),
        KEY idx_category (category),
        KEY idx_brand (brand)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '商品维度表 (Surrogate Key: source+product_id)';

-- 销售事实表 (按商品+时间维度的聚合)
-- OLAP查询通过这个表与维度表JOIN获得多维分析结果
CREATE TABLE
    fact_sales_by_product_time (
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
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '销售事实表 (按商品+时间维度)';

-- =====================================================
-- 样本数据初始化
-- =====================================================
-- 插入维度表数据 (20种商品: 10个App + 10个Web)
INSERT INTO
    dim_products (source, product_id, product_name, category, brand)
VALUES
    -- App系统产品 (source=APP, product_id=1-10)
    ('APP', 1, 'iPhone 15 Pro', 'Electronics', 'Apple'),
    (
        'APP',
        2,
        'MacBook Pro M3',
        'Electronics',
        'Apple'
    ),
    ('APP', 3, 'AirPods Pro', 'Audio', 'Apple'),
    (
        'APP',
        4,
        'Google Pixel 8',
        'Electronics',
        'Google'
    ),
    (
        'APP',
        5,
        'Google Pixel Watch',
        'Wearables',
        'Google'
    ),
    ('APP', 6, 'USB-C Cable', 'Accessories', 'Generic'),
    (
        'APP',
        7,
        'Wireless Charger',
        'Accessories',
        'Generic'
    ),
    ('APP', 8, 'Phone Case', 'Accessories', 'Generic'),
    (
        'APP',
        9,
        'Screen Protector',
        'Accessories',
        'Generic'
    ),
    (
        'APP',
        10,
        'Laptop Stand',
        'Accessories',
        'Generic'
    ),
    -- Web系统产品 (source=WEB, product_id=1-10)
    (
        'WEB',
        1,
        'Samsung Galaxy S24',
        'Electronics',
        'Samsung'
    ),
    (
        'WEB',
        2,
        'Samsung Galaxy Tab S9',
        'Electronics',
        'Samsung'
    ),
    (
        'WEB',
        3,
        'Samsung Galaxy Buds Pro',
        'Audio',
        'Samsung'
    ),
    (
        'WEB',
        4,
        'Google Pixel 8',
        'Electronics',
        'Google'
    ),
    (
        'WEB',
        5,
        'Google Pixel Watch',
        'Wearables',
        'Google'
    ),
    ('WEB', 6, 'USB Charger', 'Accessories', 'Generic'),
    ('WEB', 7, 'Case Cover', 'Accessories', 'Generic'),
    ('WEB', 8, 'Screen Film', 'Accessories', 'Generic'),
    ('WEB', 9, 'Phone Mount', 'Accessories', 'Generic'),
    (
        'WEB',
        10,
        'Tempered Glass',
        'Accessories',
        'Generic'
    );

-- 插入聚合订单数据
INSERT INTO
    dim_orders (
        order_id,
        source,
        app_order_id,
        web_order_no,
        user_id,
        order_date,
        total_amount,
        status
    )
VALUES
    (
        1,
        'APP',
        1001,
        NULL,
        100,
        '2024-01-15',
        2899.97,
        'completed'
    ),
    (
        2,
        'APP',
        1002,
        NULL,
        101,
        '2024-01-16',
        599.99,
        'completed'
    ),
    (
        3,
        'APP',
        1003,
        NULL,
        102,
        '2024-01-17',
        4099.93,
        'completed'
    ),
    (
        4,
        'APP',
        1004,
        NULL,
        103,
        '2024-01-18',
        1599.98,
        'completed'
    ),
    (
        5,
        'APP',
        1005,
        NULL,
        104,
        '2024-01-19',
        1599.98,
        'completed'
    ),
    (
        16,
        'APP',
        1006,
        NULL,
        105,
        '2024-01-20',
        1569.95,
        'completed'
    ),
    (
        17,
        'APP',
        1007,
        NULL,
        102,
        '2024-01-21',
        249.99,
        'completed'
    ),
    (
        18,
        'APP',
        1008,
        NULL,
        103,
        '2024-01-22',
        809.96,
        'completed'
    ),
    (
        19,
        'APP',
        1009,
        NULL,
        100,
        '2024-01-23',
        99.97,
        'completed'
    ),
    (
        20,
        'APP',
        1010,
        NULL,
        104,
        '2024-01-24',
        649.98,
        'completed'
    ),
    (
        6,
        'WEB',
        NULL,
        'WEB-2024-001',
        105,
        '2024-02-01',
        2799.97,
        'completed'
    ),
    (
        7,
        'WEB',
        NULL,
        'WEB-2024-002',
        106,
        '2024-02-02',
        3299.96,
        'completed'
    ),
    (
        8,
        'WEB',
        NULL,
        'WEB-2024-003',
        107,
        '2024-02-03',
        1649.96,
        'completed'
    ),
    (
        9,
        'WEB',
        NULL,
        'WEB-2024-004',
        108,
        '2024-02-04',
        2799.94,
        'completed'
    ),
    (
        10,
        'WEB',
        NULL,
        'WEB-2024-005',
        109,
        '2024-02-05',
        3699.93,
        'completed'
    ),
    (
        11,
        'WEB',
        NULL,
        'WEB-2024-006',
        110,
        '2024-02-06',
        1199.97,
        'completed'
    ),
    (
        12,
        'WEB',
        NULL,
        'WEB-2024-007',
        111,
        '2024-02-07',
        1349.94,
        'completed'
    ),
    (
        13,
        'WEB',
        NULL,
        'WEB-2024-008',
        112,
        '2024-02-08',
        1299.95,
        'completed'
    ),
    (
        14,
        'WEB',
        NULL,
        'WEB-2024-009',
        113,
        '2024-02-09',
        2249.97,
        'completed'
    ),
    (
        15,
        'WEB',
        NULL,
        'WEB-2024-010',
        114,
        '2024-02-10',
        3799.92,
        'completed'
    );

-- 插入订单项数据 (25条订单项, 来自上面插入的15个订单)
INSERT INTO
    dim_order_items (
        order_id,
        product_id,
        product_name,
        category,
        quantity,
        unit_price,
        subtotal
    )
VALUES
    (
        1,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    (
        1,
        8,
        'AirPods Pro Max',
        'Electronics',
        1,
        599.99,
        599.99
    ),
    (
        2,
        2,
        'MacBook Pro M3',
        'Computers',
        1,
        1999.99,
        1299.99
    ),
    (
        3,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    (
        3,
        4,
        'iPad Air',
        'Electronics',
        1,
        599.99,
        599.99
    ),
    (
        4,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    (
        5,
        2,
        'MacBook Pro M3',
        'Computers',
        1,
        1999.99,
        1299.98
    ),
    (
        6,
        3,
        'Samsung Galaxy S24',
        'Electronics',
        1,
        899.99,
        749.97
    ),
    (
        7,
        4,
        'iPad Air',
        'Electronics',
        2,
        599.99,
        1199.98
    ),
    (
        8,
        5,
        'Dell XPS 15',
        'Computers',
        1,
        1799.99,
        1799.99
    ),
    (
        8,
        6,
        'Wireless Mouse',
        'Accessories',
        1,
        79.99,
        79.99
    ),
    (
        8,
        7,
        'USB-C Cable',
        'Accessories',
        2,
        19.99,
        39.99
    ),
    (
        9,
        8,
        'AirPods Pro Max',
        'Electronics',
        1,
        599.99,
        599.99
    ),
    (
        10,
        9,
        'Apple Watch Ultra',
        'Electronics',
        1,
        799.99,
        799.99
    ),
    (
        10,
        10,
        'Phone Charger',
        'Accessories',
        1,
        49.99,
        49.99
    ),
    (
        10,
        11,
        'Screen Protector',
        'Accessories',
        5,
        5.99,
        29.95
    ),
    (
        11,
        12,
        'Pixel 8 Pro',
        'Electronics',
        1,
        899.99,
        899.97
    ),
    (
        12,
        13,
        'Sony WH-1000XM5',
        'Electronics',
        1,
        399.99,
        399.99
    ),
    (
        12,
        14,
        'Anker Power Bank',
        'Electronics',
        1,
        49.99,
        49.99
    ),
    (
        12,
        15,
        'Laptop Stand',
        'Accessories',
        1,
        49.99,
        49.99
    ),
    (
        12,
        16,
        'Desk Lamp',
        'Accessories',
        1,
        99.99,
        99.99
    ),
    (
        13,
        17,
        'Samsung Galaxy Buds',
        'Electronics',
        1,
        199.99,
        199.99
    ),
    (
        13,
        18,
        'Case for Phone',
        'Accessories',
        3,
        29.99,
        89.97
    ),
    (
        13,
        19,
        'Office Chair',
        'Furniture',
        1,
        299.99,
        299.99
    ),
    (
        13,
        20,
        'Monitor',
        'Electronics',
        1,
        399.99,
        399.99
    ),
    (
        16,
        3,
        'Samsung Galaxy S24',
        'Electronics',
        1,
        899.99,
        899.99
    ),
    (
        16,
        6,
        'Wireless Mouse',
        'Accessories',
        1,
        79.99,
        79.99
    ),
    (
        16,
        7,
        'USB-C Cable',
        'Accessories',
        2,
        19.99,
        39.99
    ),
    (
        17,
        10,
        'Phone Charger',
        'Accessories',
        1,
        49.99,
        49.99
    ),
    (
        17,
        11,
        'Screen Protector',
        'Accessories',
        4,
        5.99,
        23.99
    ),
    (
        18,
        5,
        'Dell XPS 15',
        'Computers',
        1,
        1799.99,
        1799.99
    ),
    (
        18,
        9,
        'Apple Watch Ultra',
        'Electronics',
        1,
        799.99,
        799.99
    ),
    (
        19,
        12,
        'Pixel 8 Pro',
        'Electronics',
        1,
        899.99,
        899.99
    ),
    (
        20,
        2,
        'MacBook Pro M3',
        'Computers',
        1,
        1999.99,
        1299.99
    ),
    (
        20,
        14,
        'Anker Power Bank',
        'Electronics',
        1,
        49.99,
        49.99
    );

-- 自动生成事实表数据 (从聚合订单项 + 维度表)
INSERT INTO
    fact_sales_by_product_time (
        product_key,
        year,
        month,
        day,
        total_quantity,
        total_sales_amount
    )
SELECT
    dp.product_key,
    YEAR (uo.order_date) as year,
    MONTH (uo.order_date) as month,
    DAY (uo.order_date) as day,
    SUM(uoi.quantity) as total_quantity,
    SUM(uoi.subtotal) as total_sales_amount
FROM
    dim_order_items uoi
    JOIN dim_orders uo ON uoi.order_id = uo.order_id
    JOIN dim_products dp ON uoi.product_id = dp.product_id
GROUP BY
    dp.product_key,
    YEAR (uo.order_date),
    MONTH (uo.order_date),
    DAY (uo.order_date) ON DUPLICATE KEY
UPDATE total_quantity =
VALUES
    (total_quantity),
    total_sales_amount =
VALUES
    (total_sales_amount);

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
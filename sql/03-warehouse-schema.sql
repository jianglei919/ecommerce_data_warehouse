-- =====================================================
-- ecommerce_warehouse - 数据仓库数据库
-- =====================================================
-- 目的:
--   - 统一存储App和Web的ETL处理结果
--   - 支持多维度分析
--   - 跟踪所有数据同步日志
-- =====================================================
-- 销量分析事实表 (按类别和时间维度)
CREATE TABLE
    fact_sales_by_category_time (
        id INT PRIMARY KEY AUTO_INCREMENT,
        category VARCHAR(50) NOT NULL,
        year INT NOT NULL,
        month INT NOT NULL,
        day INT NOT NULL,
        total_quantity INT NOT NULL DEFAULT 0,
        total_sales_amount DECIMAL(15, 2) NOT NULL DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_category_time (category, year, month, day),
        KEY idx_category (category),
        KEY idx_year_month (year, month),
        KEY idx_year_month_day (year, month, day)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '按类别和时间的销量汇总';

-- 商品评分事实表 (按商品和时间维度)
CREATE TABLE
    fact_top_rated_products (
        id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL,
        product_name VARCHAR(200) NOT NULL,
        category VARCHAR(50),
        year INT NOT NULL,
        month INT NOT NULL,
        day INT NOT NULL,
        avg_rating DECIMAL(3, 2),
        review_count INT NOT NULL DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_product_time (product_id, year, month, day),
        KEY idx_category (category),
        KEY idx_avg_rating (avg_rating DESC),
        KEY idx_year_month (year, month),
        KEY idx_product_id (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '按商品和时间的评分汇总';

-- 同步日志表 (用于监控和调试)
CREATE TABLE
    sync_log (
        id INT PRIMARY KEY AUTO_INCREMENT,
        event_type VARCHAR(50) COMMENT 'ORDER_CREATED, ORDER_UPDATED, etc.',
        source VARCHAR(20) COMMENT 'APP or WEB',
        order_id VARCHAR(50),
        status VARCHAR(30) COMMENT 'SUCCESS, VALIDATION_FAILED, ERROR',
        error_message TEXT,
        sync_time DATETIME,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        KEY idx_source_time (source, sync_time),
        KEY idx_status (status),
        KEY idx_event_type (event_type),
        KEY idx_sync_time (sync_time)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = 'ETL同步日志' ROW_FORMAT = COMPRESSED;

-- =====================================================
-- 示例数据 (初始化仓库)
-- =====================================================
-- 销量数据汇总 (来自App + Web的合并)
INSERT INTO
    fact_sales_by_category_time (
        category,
        year,
        month,
        day,
        total_quantity,
        total_sales_amount
    )
VALUES
    -- 2024-01-15
    ('Electronics', 2024, 1, 15, 2, 1899.98),
    ('Accessories', 2024, 1, 15, 10, 319.90),
    -- 2024-01-16
    ('Electronics', 2024, 1, 16, 1, 599.99),
    ('Audio', 2024, 1, 16, 1, 229.99),
    -- 2024-01-17
    ('Electronics', 2024, 1, 17, 3, 3899.94),
    ('Accessories', 2024, 1, 17, 18, 159.90),
    -- 2024-01-18
    ('Electronics', 2024, 1, 18, 1, 799.99),
    ('Accessories', 2024, 1, 18, 7, 90.93),
    -- 2024-01-19
    ('Electronics', 2024, 1, 19, 2, 1699.97),
    ('Audio', 2024, 1, 19, 1, 229.99),
    ('Wearables', 2024, 1, 19, 1, 299.99),
    -- 2024-01-20
    ('Electronics', 2024, 1, 20, 3, 3699.95),
    ('Audio', 2024, 1, 20, 1, 229.99),
    ('Accessories', 2024, 1, 20, 7, 179.90),
    -- 2024-01-21
    ('Audio', 2024, 1, 21, 2, 579.98),
    -- 2024-01-22
    ('Accessories', 2024, 1, 22, 100, 999.90),
    -- 2024-01-23
    ('Accessories', 2024, 1, 23, 10, 149.90),
    -- 2024-01-24
    ('Electronics', 2024, 1, 24, 2, 1399.97),
    ('Accessories', 2024, 1, 24, 10, 199.90);

-- 商品评分汇总 (来自App + Web的合并)
INSERT INTO
    fact_top_rated_products (
        product_id,
        product_name,
        category,
        year,
        month,
        day,
        avg_rating,
        review_count
    )
VALUES
    (
        1,
        'iPhone 15 Pro / Samsung Galaxy S24',
        'Electronics',
        2024,
        1,
        16,
        5.00,
        2
    ),
    (
        4,
        'iPad Air / Samsung Galaxy Tab S9',
        'Electronics',
        2024,
        1,
        18,
        4.50,
        2
    ),
    (
        2,
        'MacBook Pro M3 / Samsung Galaxy Tab S9',
        'Electronics',
        2024,
        1,
        19,
        5.00,
        2
    ),
    (
        3,
        'AirPods Pro / Samsung Galaxy Buds Pro',
        'Audio',
        2024,
        1,
        20,
        4.50,
        2
    ),
    (
        5,
        'Apple Watch / Google Pixel Watch',
        'Wearables',
        2024,
        1,
        21,
        4.50,
        2
    ),
    (
        4,
        'iPad Air / Google Pixel 8',
        'Electronics',
        2024,
        1,
        22,
        4.50,
        2
    ),
    (
        7,
        'Wireless Charger / Case Cover',
        'Accessories',
        2024,
        1,
        23,
        4.50,
        2
    ),
    (3, 'AirPods Pro', 'Audio', 2024, 1, 24, 5.00, 1),
    (
        1,
        'iPhone 15 Pro',
        'Electronics',
        2024,
        1,
        25,
        4.50,
        2
    );

-- =====================================================
-- 索引优化和统计
-- =====================================================
-- 为热查询添加覆盖索引
ALTER TABLE fact_sales_by_category_time ADD INDEX idx_category_year_month_sales (category, year, month, total_sales_amount);

ALTER TABLE fact_top_rated_products ADD INDEX idx_category_product_rating (category, product_id, avg_rating DESC);

-- =====================================================
-- 视图: 用于简化查询
-- =====================================================
-- 销量排行视图
CREATE VIEW
    v_sales_ranking AS
SELECT
    category,
    year,
    month,
    day,
    total_quantity,
    total_sales_amount,
    RANK() OVER (
        PARTITION BY
            year,
            month
        ORDER BY
            total_sales_amount DESC
    ) AS sales_rank
FROM
    fact_sales_by_category_time;

-- 评分排行视图
CREATE VIEW
    v_product_ratings AS
SELECT
    category,
    product_name,
    year,
    month,
    day,
    avg_rating,
    review_count,
    RANK() OVER (
        PARTITION BY
            year,
            month
        ORDER BY
            avg_rating DESC
    ) AS rating_rank
FROM
    fact_top_rated_products
WHERE
    avg_rating IS NOT NULL;

-- =====================================================
-- 存储过程: 用于聚合计算
-- =====================================================
-- 清空并重新计算销量汇总 (用于测试)
DELIMITER / / CREATE PROCEDURE sp_refresh_sales_facts () BEGIN
TRUNCATE TABLE fact_sales_by_category_time;

-- 实际应用中会从源数据库读取并聚合
INSERT INTO
    fact_sales_by_category_time (
        category,
        year,
        month,
        day,
        total_quantity,
        total_sales_amount
    )
SELECT
    'Electronics' AS category,
    2024 AS year,
    1 AS month,
    15 AS day,
    5 AS total_quantity,
    2499.97 AS total_sales_amount;

END / / DELIMITER;

-- 获取指定日期范围的销量统计
DELIMITER / / CREATE PROCEDURE sp_get_sales_by_date_range (IN p_start_date DATE, IN p_end_date DATE) BEGIN
SELECT
    category,
    SUM(total_quantity) AS total_qty,
    SUM(total_sales_amount) AS total_amount,
    DATE (CONCAT_WS ('-', year, month, day)) AS date_d
FROM
    fact_sales_by_category_time
WHERE
    DATE (CONCAT_WS ('-', year, month, day)) BETWEEN p_start_date AND p_end_date
GROUP BY
    category,
    DATE (CONCAT_WS ('-', year, month, day))
ORDER BY
    date_d DESC,
    total_amount DESC;

END / / DELIMITER;

-- 获取Top N评高商品
DELIMITER / / CREATE PROCEDURE sp_get_top_rated_products (IN p_limit INT) BEGIN
SELECT
    product_id,
    product_name,
    category,
    AVG(avg_rating) AS avg_rating,
    SUM(review_count) AS total_reviews
FROM
    fact_top_rated_products
WHERE
    avg_rating IS NOT NULL
GROUP BY
    product_id,
    product_name,
    category
ORDER BY
    avg_rating DESC,
    total_reviews DESC
LIMIT
    p_limit;

END / / DELIMITER;

-- =====================================================
-- 备注
-- =====================================================
-- 这个数据仓库设计遵循星形模式 (Star Schema):
-- - 中心: 两个事实表 (sales, ratings)
-- - 维度: category, year, month, day, product_id 等
-- - 特点: 快速查询, 易于聚合, 支持多维分析
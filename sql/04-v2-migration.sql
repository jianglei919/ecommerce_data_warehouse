-- V2版本迁移脚本
-- 创建统一订单表和相关数据
USE ecommerce_warehouse;

-- 检查表是否已存在，如果存在则删除（开发测试用）
DROP TABLE IF EXISTS unified_order_items;

DROP TABLE IF EXISTS unified_orders;

-- 统一订单表 (V2新增): 聚合来自App和Web两个业务源的订单数据
CREATE TABLE
    unified_orders (
        unified_order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '统一订单ID',
        source VARCHAR(10) NOT NULL COMMENT 'APP 或 WEB',
        app_order_id INT COMMENT 'App系统订单ID',
        web_order_no VARCHAR(50) COMMENT 'Web系统订单号',
        user_id INT NOT NULL COMMENT '用户ID (在各源系统中的ID)',
        order_date DATE NOT NULL,
        total_amount DECIMAL(15, 2) NOT NULL,
        status VARCHAR(20) DEFAULT 'pending' COMMENT 'pending, completed, cancelled, etc.',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_source_order (source, app_order_id, web_order_no),
        KEY idx_source (source),
        KEY idx_order_date (order_date),
        KEY idx_user_id (user_id),
        KEY idx_status (status),
        KEY idx_source_date (source, order_date)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '统一订单表 (App+Web)';

-- 统一订单项详情表
CREATE TABLE
    unified_order_items (
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
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '统一订单项详情表';

-- 示例数据 (V2): App + Web 聚合
INSERT INTO
    unified_orders (
        source,
        app_order_id,
        web_order_no,
        user_id,
        order_date,
        total_amount,
        status
    )
VALUES
    -- App源订单 (1001-1005)
    (
        'APP',
        1001,
        NULL,
        1,
        '2024-01-15',
        999.99,
        'completed'
    ),
    (
        'APP',
        1002,
        NULL,
        2,
        '2024-01-16',
        599.99,
        'completed'
    ),
    (
        'APP',
        1003,
        NULL,
        3,
        '2024-01-17',
        1899.97,
        'completed'
    ),
    (
        'APP',
        1004,
        NULL,
        4,
        '2024-01-18',
        799.99,
        'completed'
    ),
    (
        'APP',
        1005,
        NULL,
        5,
        '2024-01-19',
        1699.97,
        'completed'
    ),
    (
        'APP',
        1006,
        NULL,
        1,
        '2024-01-20',
        2699.95,
        'completed'
    ),
    -- Web源订单 (WEB-2024-001到 010)
    (
        'WEB',
        NULL,
        'WEB-2024-001',
        1,
        '2024-01-15',
        899.99,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-002',
        2,
        '2024-01-16',
        229.99,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-003',
        3,
        '2024-01-17',
        1999.97,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-004',
        4,
        '2024-01-18',
        319.90,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-005',
        5,
        '2024-01-19',
        1209.88,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-006',
        1,
        '2024-01-20',
        999.88,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-007',
        2,
        '2024-01-21',
        1199.97,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-008',
        3,
        '2024-01-22',
        579.98,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-009',
        4,
        '2024-01-23',
        1149.90,
        'completed'
    ),
    (
        'WEB',
        NULL,
        'WEB-2024-010',
        5,
        '2024-01-24',
        999.88,
        'completed'
    );

-- 统一订单项 (APP订单项汇总)
INSERT INTO
    unified_order_items (
        unified_order_id,
        product_id,
        product_name,
        category,
        quantity,
        unit_price,
        subtotal
    )
VALUES
    -- 订单1 (APP 1001)
    (
        1,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    -- 订单2 (APP 1002)
    (
        2,
        2,
        'MacBook Pro M3',
        'Electronics',
        1,
        599.99,
        599.99
    ),
    -- 订单3 (APP 1003)
    (
        3,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    (3, 3, 'AirPods Pro', 'Audio', 1, 249.99, 249.99),
    -- 订单4 (APP 1004)
    (
        4,
        2,
        'MacBook Pro M3',
        'Electronics',
        1,
        799.99,
        799.99
    ),
    -- 订单5 (APP 1005)
    (
        5,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    (
        5,
        4,
        'iPad Air',
        'Electronics',
        1,
        599.99,
        599.99
    ),
    -- 订单6 (APP 1006)
    (
        6,
        2,
        'MacBook Pro M3',
        'Electronics',
        1,
        1999.99,
        1999.99
    ),
    (6, 3, 'AirPods Pro', 'Audio', 1, 699.99, 699.99),
    -- 订单7 (WEB-2024-001)
    (
        7,
        5,
        'Samsung Galaxy S24',
        'Electronics',
        1,
        899.99,
        899.99
    ),
    -- 订单8 (WEB-2024-002)
    (
        8,
        6,
        'Samsung Galaxy Buds Pro',
        'Audio',
        1,
        229.99,
        229.99
    ),
    -- 订单9 (WEB-2024-003)
    (
        9,
        7,
        'Samsung Galaxy Tab S9',
        'Electronics',
        1,
        1199.99,
        1199.99
    ),
    (
        9,
        8,
        'Wireless Charger',
        'Accessories',
        1,
        99.99,
        99.99
    ),
    (
        9,
        9,
        'Screen Protector',
        'Accessories',
        5,
        19.99,
        99.99
    ),
    -- 订单10 (WEB-2024-004)
    (
        10,
        10,
        'USB-C Cable Set',
        'Accessories',
        1,
        319.90,
        319.90
    ),
    -- 订单11 (WEB-2024-005)
    (
        11,
        5,
        'Samsung Galaxy S24',
        'Electronics',
        1,
        899.99,
        899.99
    ),
    (
        11,
        6,
        'Samsung Galaxy Buds Pro',
        'Audio',
        1,
        229.99,
        229.99
    ),
    (
        11,
        10,
        'USB-C Cable Set',
        'Accessories',
        3,
        59.97,
        59.97
    ),
    -- 订单12 (WEB-2024-006)
    (
        12,
        9,
        'Screen Protector',
        'Accessories',
        10,
        99.99,
        99.99
    ),
    (
        12,
        8,
        'Wireless Charger',
        'Accessories',
        9,
        99.99,
        99.99
    ),
    -- 订单13 (WEB-2024-007)
    (
        13,
        7,
        'Samsung Galaxy Tab S9',
        'Electronics',
        1,
        1199.99,
        1199.99
    ),
    -- 订单14 (WEB-2024-008)
    (
        14,
        6,
        'Samsung Galaxy Buds Pro',
        'Audio',
        2,
        229.99,
        459.98
    ),
    (
        14,
        10,
        'USB-C Cable Set',
        'Accessories',
        1,
        119.99,
        119.99
    ),
    -- 订单15 (WEB-2024-009)
    (
        15,
        5,
        'Samsung Galaxy S24',
        'Electronics',
        1,
        899.99,
        899.99
    ),
    (
        15,
        9,
        'Screen Protector',
        'Accessories',
        10,
        149.90,
        249.90
    ),
    -- 订单16 (WEB-2024-010)
    (
        16,
        10,
        'USB-C Cable Set',
        'Accessories',
        5,
        199.99,
        199.99
    ),
    (
        16,
        8,
        'Wireless Charger',
        'Accessories',
        8,
        99.99,
        99.99
    );

-- 验证表创建
SELECT
    'unified_orders' as table_name,
    COUNT(*) as row_count
FROM
    unified_orders
UNION ALL
SELECT
    'unified_order_items' as table_name,
    COUNT(*) as row_count
FROM
    unified_order_items;

-- V2数据统计
SELECT
    source,
    COUNT(*) as order_count,
    SUM(total_amount) as total_sales,
    AVG(total_amount) as avg_order_value
FROM
    unified_orders
GROUP BY
    source;
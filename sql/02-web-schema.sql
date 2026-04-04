-- =====================================================
-- ecommerce_source_web - Web业务系统数据库 (简化版)
-- =====================================================
-- 根据 Ecommerce Data Warehouse Design Report.pdf 重新设计
-- 仅保留统一订单表，用于聚合 App 和 Web 数据
-- =====================================================
-- 统一订单表 (Web源)
CREATE TABLE
    unified_orders (
        unified_order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '统一订单ID',
        source VARCHAR(10) NOT NULL COMMENT 'WEB',
        web_order_no VARCHAR(50) NOT NULL COMMENT 'Web系统订单号',
        user_id INT NOT NULL COMMENT '用户ID',
        order_date DATE NOT NULL COMMENT '订单日期',
        total_amount DECIMAL(15, 2) NOT NULL COMMENT '总金额',
        status VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        UNIQUE KEY uk_web_order (web_order_no),
        KEY idx_order_date (order_date),
        KEY idx_user_id (user_id),
        KEY idx_status (status)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '统一订单表 (Web)';

-- 统一订单项详情表 (Web源)
CREATE TABLE
    unified_order_items (
        unified_item_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
        unified_order_id INT NOT NULL COMMENT '订单ID',
        product_id INT NOT NULL COMMENT '商品ID',
        product_name VARCHAR(200) NOT NULL COMMENT '商品名称',
        category VARCHAR(50) COMMENT '商品类别',
        quantity INT NOT NULL DEFAULT 1 COMMENT '购买数量',
        unit_price DECIMAL(10, 2) NOT NULL COMMENT '单价',
        subtotal DECIMAL(15, 2) NOT NULL COMMENT '小计',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (unified_order_id) REFERENCES unified_orders (unified_order_id) ON DELETE CASCADE,
        KEY idx_order_id (unified_order_id),
        KEY idx_product_id (product_id),
        KEY idx_category (category)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '统一订单项详情表 (Web)';

-- =====================================================
-- 示例数据
-- =====================================================
-- Web 统一订单 (10条)
INSERT INTO
    unified_orders (
        source,
        web_order_no,
        user_id,
        order_date,
        total_amount,
        status
    )
VALUES
    (
        'WEB',
        'WEB-2024-001',
        1,
        '2024-01-15',
        899.99,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-002',
        2,
        '2024-01-16',
        1299.98,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-003',
        3,
        '2024-01-17',
        749.97,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-004',
        4,
        '2024-01-18',
        1999.96,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-005',
        5,
        '2024-01-19',
        2099.97,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-006',
        1,
        '2024-02-15',
        599.99,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-007',
        2,
        '2024-02-16',
        1199.98,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-008',
        3,
        '2024-02-17',
        899.97,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-009',
        4,
        '2024-02-18',
        1599.98,
        'completed'
    ),
    (
        'WEB',
        'WEB-2024-010',
        5,
        '2024-02-19',
        999.99,
        'completed'
    );

-- Web 订单项 (25条示例数据)
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
    -- Order 1
    (
        1,
        1,
        'iPhone 15 Pro',
        'Electronics',
        1,
        999.99,
        999.99
    ),
    -- Order 2
    (
        2,
        2,
        'MacBook Pro M3',
        'Computers',
        1,
        1999.99,
        1299.98
    ),
    -- Order 3
    (
        3,
        3,
        'Samsung Galaxy S24',
        'Electronics',
        1,
        899.99,
        749.97
    ),
    -- Order 4
    (
        4,
        4,
        'iPad Air',
        'Electronics',
        2,
        599.99,
        1999.96
    ),
    -- Order 5
    (
        5,
        5,
        'Dell XPS 15',
        'Computers',
        1,
        1799.99,
        1799.99
    ),
    (
        5,
        6,
        'Wireless Mouse',
        'Accessories',
        1,
        79.99,
        79.99
    ),
    (
        5,
        7,
        'USB-C Cable',
        'Accessories',
        2,
        19.99,
        39.99
    ),
    -- Order 6
    (
        6,
        8,
        'AirPods Pro Max',
        'Electronics',
        1,
        599.99,
        599.99
    ),
    -- Order 7
    (
        7,
        9,
        'Apple Watch Ultra',
        'Electronics',
        1,
        799.99,
        799.99
    ),
    (
        7,
        10,
        'Phone Charger',
        'Accessories',
        1,
        49.99,
        49.99
    ),
    (
        7,
        11,
        'Screen Protector',
        'Accessories',
        5,
        5.99,
        29.95
    ),
    -- Order 8
    (
        8,
        12,
        'Pixel 8 Pro',
        'Electronics',
        1,
        899.99,
        899.97
    ),
    -- Order 9
    (
        9,
        13,
        'Sony WH-1000XM5',
        'Electronics',
        1,
        399.99,
        399.99
    ),
    (
        9,
        14,
        'Anker Power Bank',
        'Electronics',
        1,
        49.99,
        49.99
    ),
    (
        9,
        15,
        'Laptop Stand',
        'Accessories',
        1,
        49.99,
        49.99
    ),
    (
        9,
        16,
        'Desk Lamp',
        'Accessories',
        1,
        99.99,
        99.99
    ),
    -- Order 10
    (
        10,
        17,
        'Samsung Galaxy Buds',
        'Electronics',
        1,
        199.99,
        199.99
    ),
    (
        10,
        18,
        'Case for Phone',
        'Accessories',
        3,
        29.99,
        89.97
    ),
    (
        10,
        19,
        'Office Chair',
        'Furniture',
        1,
        299.99,
        299.99
    ),
    (
        10,
        20,
        'Monitor',
        'Electronics',
        1,
        399.99,
        399.99
    );
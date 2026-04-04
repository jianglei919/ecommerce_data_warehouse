-- =====================================================
-- ecommerce_source_web - Web业务系统数据库
-- =====================================================
-- 数据特点:
--   - order_no: VARCHAR 类型 (主键)
--   - order_date: MM/dd/yyyy 格式 (逻辑上)
--   - order_items 使用 order_no 字段 (而非 order_id)
-- =====================================================
-- 用户表
CREATE TABLE
    users (
        user_id INT PRIMARY KEY AUTO_INCREMENT,
        username VARCHAR(100) UNIQUE NOT NULL,
        email VARCHAR(100) UNIQUE NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        KEY idx_username (username)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 产品表
CREATE TABLE
    products (
        product_id INT PRIMARY KEY AUTO_INCREMENT,
        product_name VARCHAR(200) NOT NULL,
        category VARCHAR(50) NOT NULL,
        price DECIMAL(10, 2) NOT NULL,
        stock_quantity INT DEFAULT 0,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        KEY idx_category (category),
        KEY idx_product_name (product_name)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单表 (Web: order_no VARCHAR)
CREATE TABLE
    orders (
        order_no VARCHAR(50) PRIMARY KEY COMMENT '订单号 (Web)',
        user_id INT NOT NULL,
        order_date DATE NOT NULL COMMENT 'Web格式: MM/dd/yyyy (逻辑)',
        total_amount DECIMAL(15, 2) NOT NULL,
        status VARCHAR(20) DEFAULT 'pending',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (user_id),
        KEY idx_order_date (order_date),
        KEY idx_user_id (user_id),
        KEY idx_status (status)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单项目表 (使用 order_no 字段)
CREATE TABLE
    order_items (
        order_item_id INT PRIMARY KEY AUTO_INCREMENT,
        order_no VARCHAR(50) NOT NULL COMMENT '订单号 (而非 order_id)',
        product_id INT NOT NULL,
        quantity INT NOT NULL DEFAULT 1,
        unit_price DECIMAL(10, 2) NOT NULL,
        subtotal DECIMAL(15, 2) NOT NULL,
        FOREIGN KEY (order_no) REFERENCES orders (order_no) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        KEY idx_order_no (order_no),
        KEY idx_product_id (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 产品评论表
CREATE TABLE
    product_reviews (
        review_id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL,
        user_id INT NOT NULL,
        rating INT NOT NULL CHECK (
            rating >= 1
            AND rating <= 5
        ),
        review_text TEXT,
        review_date DATE NOT NULL,
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        FOREIGN KEY (user_id) REFERENCES users (user_id),
        KEY idx_product_id (product_id),
        KEY idx_user_id (user_id),
        KEY idx_review_date (review_date)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- =====================================================
-- 示例数据
-- =====================================================
-- 用户
INSERT INTO
    users (username, email)
VALUES
    ('user_web_001', 'user1@web.com'),
    ('user_web_002', 'user2@web.com'),
    ('user_web_003', 'user3@web.com'),
    ('user_web_004', 'user4@web.com'),
    ('user_web_005', 'user5@web.com');

-- 产品
INSERT INTO
    products (product_name, category, price, stock_quantity)
VALUES
    ('Samsung Galaxy S24', 'Electronics', 899.99, 55),
    (
        'Samsung Galaxy Tab S9',
        'Electronics',
        649.99,
        40
    ),
    ('Samsung Galaxy Buds Pro', 'Audio', 229.99, 90),
    ('Google Pixel 8', 'Electronics', 799.99, 50),
    ('Google Pixel Watch', 'Wearables', 299.99, 75),
    ('USB Charger', 'Accessories', 29.99, 400),
    ('Case Cover', 'Accessories', 19.99, 250),
    ('Screen Film', 'Accessories', 7.99, 350),
    ('Phone Mount', 'Accessories', 24.99, 120),
    ('Tempered Glass', 'Accessories', 12.99, 280);

-- 订单 (Web: VARCHAR order_no, 日期逻辑上是MM/dd/yyyy)
INSERT INTO
    orders (
        order_no,
        user_id,
        order_date,
        total_amount,
        status
    )
VALUES
    (
        'WEB-2024-001',
        1,
        '2024-01-15',
        1129.98,
        'completed'
    ),
    (
        'WEB-2024-002',
        2,
        '2024-01-16',
        649.99,
        'completed'
    ),
    (
        'WEB-2024-003',
        3,
        '2024-01-17',
        2099.96,
        'completed'
    ),
    (
        'WEB-2024-004',
        1,
        '2024-01-18',
        299.97,
        'completed'
    ),
    (
        'WEB-2024-005',
        4,
        '2024-01-19',
        529.97,
        'completed'
    ),
    (
        'WEB-2024-006',
        5,
        '2024-01-20',
        1579.94,
        'completed'
    ),
    (
        'WEB-2024-007',
        2,
        '2024-01-21',
        229.99,
        'completed'
    ),
    (
        'WEB-2024-008',
        3,
        '2024-01-22',
        889.96,
        'completed'
    ),
    (
        'WEB-2024-009',
        1,
        '2024-01-23',
        79.96,
        'completed'
    ),
    (
        'WEB-2024-010',
        4,
        '2024-01-24',
        749.97,
        'completed'
    );

-- 订单项目 (使用 order_no 字段名)
INSERT INTO
    order_items (
        order_no,
        product_id,
        quantity,
        unit_price,
        subtotal
    )
VALUES
    ('WEB-2024-001', 1, 1, 899.99, 899.99),
    ('WEB-2024-001', 6, 1, 29.99, 29.99),
    ('WEB-2024-001', 7, 4, 19.99, 79.96),
    ('WEB-2024-002', 2, 1, 649.99, 649.99),
    ('WEB-2024-003', 1, 2, 899.99, 1799.98),
    ('WEB-2024-003', 6, 1, 29.99, 29.99),
    ('WEB-2024-003', 8, 9, 7.99, 71.91),
    ('WEB-2024-003', 9, 1, 24.99, 24.99),
    ('WEB-2024-004', 3, 1, 229.99, 229.99),
    ('WEB-2024-004', 10, 7, 12.99, 90.93),
    ('WEB-2024-005', 4, 1, 799.99, 799.99),
    ('WEB-2024-005', 7, 1, 19.99, 19.99),
    ('WEB-2024-005', 5, 1, 299.99, 299.99),
    ('WEB-2024-006', 2, 2, 649.99, 1299.98),
    ('WEB-2024-006', 3, 1, 229.99, 229.99),
    ('WEB-2024-006', 6, 5, 29.99, 149.95),
    ('WEB-2024-007', 3, 1, 229.99, 229.99),
    ('WEB-2024-008', 8, 50, 7.99, 399.50),
    ('WEB-2024-008', 10, 49, 12.99, 490.51),
    ('WEB-2024-009', 7, 2, 19.99, 39.98),
    ('WEB-2024-009', 9, 2, 24.99, 49.98),
    ('WEB-2024-010', 2, 1, 649.99, 649.99),
    ('WEB-2024-010', 6, 1, 29.99, 29.99),
    ('WEB-2024-010', 10, 8, 12.99, 103.92),
    ('WEB-2024-010', 7, 1, 19.99, 19.99);

-- 评论
INSERT INTO
    product_reviews (
        product_id,
        user_id,
        rating,
        review_text,
        review_date
    )
VALUES
    (
        1,
        1,
        5,
        'Great Android phone, amazing display',
        '2024-01-16'
    ),
    (
        1,
        2,
        5,
        'Best Samsung phone so far',
        '2024-01-17'
    ),
    (
        2,
        3,
        4,
        'Excellent tablet for productivity',
        '2024-01-18'
    ),
    (4, 4, 5, 'Pixel 8 is fantastic', '2024-01-19'),
    (3, 5, 4, 'Good wireless earbuds', '2024-01-20'),
    (5, 1, 5, 'Love the Pixel Watch', '2024-01-21'),
    (
        2,
        2,
        5,
        'Samsung Galaxy Tab is perfect',
        '2024-01-22'
    ),
    (4, 3, 5, 'Excellent performance', '2024-01-23'),
    (3, 4, 4, 'Great sound quality', '2024-01-24'),
    (1, 5, 4, 'Very recommended phone', '2024-01-25');
-- =====================================================
-- ecommerce_source_app - App业务系统数据库
-- =====================================================
-- 数据特点:
--   - order_id: INT 类型
--   - order_date: yyyy-MM-dd 格式
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

-- 订单表 (App: order_id INT)
CREATE TABLE
    orders (
        order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID (App)',
        user_id INT NOT NULL,
        order_date DATE NOT NULL COMMENT 'App格式: yyyy-MM-dd',
        total_amount DECIMAL(15, 2) NOT NULL,
        status VARCHAR(20) DEFAULT 'pending',
        created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
        updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (user_id),
        KEY idx_order_date (order_date),
        KEY idx_user_id (user_id),
        KEY idx_status (status)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 订单项目表
CREATE TABLE
    order_items (
        order_item_id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL DEFAULT 1,
        unit_price DECIMAL(10, 2) NOT NULL,
        subtotal DECIMAL(15, 2) NOT NULL,
        FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        KEY idx_order_id (order_id),
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
    ('user_app_001', 'user1@app.com'),
    ('user_app_002', 'user2@app.com'),
    ('user_app_003', 'user3@app.com'),
    ('user_app_004', 'user4@app.com'),
    ('user_app_005', 'user5@app.com');

-- 产品
INSERT INTO
    products (product_name, category, price, stock_quantity)
VALUES
    ('iPhone 15 Pro', 'Electronics', 999.99, 50),
    ('MacBook Pro M3', 'Electronics', 1999.99, 30),
    ('AirPods Pro', 'Audio', 249.99, 100),
    ('iPad Air', 'Electronics', 599.99, 45),
    ('Apple Watch', 'Wearables', 399.99, 60),
    ('USB-C Cable', 'Accessories', 19.99, 500),
    ('Wireless Charger', 'Accessories', 79.99, 80),
    ('Phone Case', 'Accessories', 29.99, 200),
    ('Screen Protector', 'Accessories', 9.99, 300),
    ('Laptop Stand', 'Accessories', 49.99, 70);

-- 订单 (App: INT order_id, yyyy-MM-dd date)
INSERT INTO
    orders (
        order_id,
        user_id,
        order_date,
        total_amount,
        status
    )
VALUES
    (1001, 1, '2024-01-15', 1249.98, 'completed'),
    (1002, 2, '2024-01-16', 599.99, 'completed'),
    (1003, 3, '2024-01-17', 2299.97, 'completed'),
    (1004, 1, '2024-01-18', 329.98, 'completed'),
    (1005, 4, '2024-01-19', 449.97, 'completed'),
    (1006, 5, '2024-01-20', 1569.95, 'completed'),
    (1007, 2, '2024-01-21', 249.99, 'completed'),
    (1008, 3, '2024-01-22', 809.96, 'completed'),
    (1009, 1, '2024-01-23', 99.97, 'completed'),
    (1010, 4, '2024-01-24', 649.98, 'completed');

-- 订单项目
INSERT INTO
    order_items (
        order_id,
        product_id,
        quantity,
        unit_price,
        subtotal
    )
VALUES
    (1001, 1, 1, 999.99, 999.99),
    (1001, 6, 1, 19.99, 19.99),
    (1001, 8, 5, 29.99, 149.95),
    (1002, 4, 1, 599.99, 599.99),
    (1003, 2, 1, 1999.99, 1999.99),
    (1003, 6, 1, 19.99, 19.99),
    (1003, 7, 4, 79.99, 319.96),
    (1004, 3, 1, 249.99, 249.99),
    (1004, 9, 8, 9.99, 79.99),
    (1005, 5, 1, 399.99, 399.99),
    (1005, 8, 1, 29.99, 49.98),
    (1006, 2, 1, 1999.99, 1999.99),
    (1006, 1, 1, 999.99, 999.99),
    (1006, 10, 1, 49.99, 49.99),
    (1007, 3, 1, 249.99, 249.99),
    (1008, 7, 5, 79.99, 399.95),
    (1008, 9, 41, 9.99, 409.59),
    (1009, 8, 3, 29.99, 89.97),
    (1009, 6, 1, 9.99, 9.99),
    (1010, 4, 1, 599.99, 599.99),
    (1010, 6, 1, 19.99, 19.99),
    (1010, 10, 3, 49.99, 149.97);

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
        'Excellent phone, great camera!',
        '2024-01-16'
    ),
    (1, 2, 5, 'Best iPhone I ever had', '2024-01-17'),
    (4, 3, 4, 'Good tablet, very fast', '2024-01-18'),
    (
        2,
        4,
        5,
        'Powerful laptop, highly recommended',
        '2024-01-19'
    ),
    (3, 5, 4, 'Great sound quality', '2024-01-20'),
    (2, 1, 5, 'Worth every penny', '2024-01-21'),
    (
        4,
        2,
        5,
        'Perfect for work and entertainment',
        '2024-01-22'
    ),
    (
        5,
        3,
        4,
        'Good watch, battery lasts long',
        '2024-01-23'
    ),
    (3, 4, 5, 'Best earbuds ever', '2024-01-24'),
    (
        7,
        5,
        4,
        'Fast charging, very convenient',
        '2024-01-25'
    );
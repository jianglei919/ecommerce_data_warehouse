-- 创建业务原始数据库 ecommerce_source_1 (分店1)
CREATE DATABASE IF NOT EXISTS ecommerce_source_1 CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ecommerce_source_1;

-- 用户表
CREATE TABLE
    users (
        user_id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        email VARCHAR(100) UNIQUE,
        phone VARCHAR(20),
        city VARCHAR(50),
        register_date DATE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE
    products (
        product_id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(200) NOT NULL,
        description TEXT,
        category VARCHAR(50) NOT NULL,
        price DECIMAL(10, 2) NOT NULL,
        cost DECIMAL(10, 2),
        brand VARCHAR(50),
        stock_qty INT DEFAULT 0,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_category (category),
        INDEX idx_brand (brand)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE
    orders (
        order_id INT PRIMARY KEY AUTO_INCREMENT,
        user_id INT NOT NULL,
        order_date DATE NOT NULL,
        total_amount DECIMAL(12, 2) NOT NULL,
        status VARCHAR(20) DEFAULT 'completed',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (user_id),
        INDEX idx_user_id (user_id),
        INDEX idx_order_date (order_date)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 订单明细表
CREATE TABLE
    order_items (
        item_id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(10, 2) NOT NULL,
        line_total DECIMAL(12, 2) NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (order_id) REFERENCES orders (order_id) ON DELETE CASCADE,
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        INDEX idx_order_id (order_id),
        INDEX idx_product_id (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 商品评论表
CREATE TABLE
    product_reviews (
        review_id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL,
        user_id INT,
        rating INT CHECK (
            rating >= 1
            AND rating <= 5
        ),
        comment TEXT,
        review_date DATE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        FOREIGN KEY (user_id) REFERENCES users (user_id),
        INDEX idx_product_id (product_id),
        INDEX idx_rating (rating)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 退货表
CREATE TABLE
    returns (
        return_id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        return_date DATE,
        return_qty INT DEFAULT 1,
        reason VARCHAR(100),
        status VARCHAR(20) DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        INDEX idx_order_id (order_id),
        INDEX idx_product_id (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ============================================================================
-- 创建业务原始数据库 ecommerce_source_2 (分店2) - 与 source_1 完全相同的架构
-- ============================================================================
CREATE DATABASE IF NOT EXISTS ecommerce_source_2 CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ecommerce_source_2;

-- 用户表
CREATE TABLE
    users (
        user_id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(100) NOT NULL,
        email VARCHAR(100) UNIQUE,
        phone VARCHAR(20),
        city VARCHAR(50),
        register_date DATE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 商品表
CREATE TABLE
    products (
        product_id INT PRIMARY KEY AUTO_INCREMENT,
        name VARCHAR(200) NOT NULL,
        description TEXT,
        category VARCHAR(50) NOT NULL,
        price DECIMAL(10, 2) NOT NULL,
        cost DECIMAL(10, 2),
        brand VARCHAR(50),
        stock_qty INT DEFAULT 0,
        is_active BOOLEAN DEFAULT TRUE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        INDEX idx_category (category),
        INDEX idx_brand (brand)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 订单表
CREATE TABLE
    orders (
        order_id INT PRIMARY KEY AUTO_INCREMENT,
        user_id INT NOT NULL,
        order_date DATE NOT NULL,
        total_amount DECIMAL(12, 2) NOT NULL,
        status VARCHAR(20) DEFAULT 'completed',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        FOREIGN KEY (user_id) REFERENCES users (user_id),
        INDEX idx_user_id (user_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 订单详情表
CREATE TABLE
    order_items (
        order_item_id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        quantity INT NOT NULL,
        unit_price DECIMAL(10, 2) NOT NULL,
        discount_rate DECIMAL(5, 2) DEFAULT 0,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        INDEX idx_order_id (order_id),
        INDEX idx_product_id (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 商品评价表
CREATE TABLE
    product_reviews (
        review_id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL,
        order_item_id INT NOT NULL,
        rating INT NOT NULL,
        comment TEXT,
        review_date DATE,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        FOREIGN KEY (order_item_id) REFERENCES order_items (order_item_id),
        INDEX idx_product_id (product_id),
        INDEX idx_rating (rating)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 退货表
CREATE TABLE
    returns (
        return_id INT PRIMARY KEY AUTO_INCREMENT,
        order_id INT NOT NULL,
        product_id INT NOT NULL,
        return_date DATE,
        return_qty INT DEFAULT 1,
        reason VARCHAR(100),
        status VARCHAR(20) DEFAULT 'pending',
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        FOREIGN KEY (order_id) REFERENCES orders (order_id),
        FOREIGN KEY (product_id) REFERENCES products (product_id),
        INDEX idx_order_id (order_id),
        INDEX idx_product_id (product_id)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
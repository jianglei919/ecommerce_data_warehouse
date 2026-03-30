-- 创建分析数据仓库 ecommerce_warehouse
CREATE DATABASE IF NOT EXISTS ecommerce_warehouse CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE ecommerce_warehouse;

-- 销售事实表
CREATE TABLE
    fact_sales (
        sales_id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL,
        product_name VARCHAR(200),
        category VARCHAR(50),
        price DECIMAL(10, 2),
        quantity INT,
        sales_amount DECIMAL(12, 2),
        sale_date DATE,
        season VARCHAR(10),
        year INT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_product_id (product_id),
        INDEX idx_category (category),
        INDEX idx_sale_date (sale_date),
        INDEX idx_season (season)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 商品分析维度表
CREATE TABLE
    dim_product_analysis (
        analysis_id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL UNIQUE,
        name VARCHAR(200),
        category VARCHAR(50),
        brand VARCHAR(50),
        avg_rating DECIMAL(3, 2),
        total_reviews INT,
        total_sales_qty INT,
        total_sales_amount DECIMAL(15, 2),
        return_rate DECIMAL(5, 4),
        rank_by_sales INT,
        rank_by_rating INT,
        combined_score DECIMAL(5, 2),
        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 季节销售事实表
CREATE TABLE
    fact_sales_by_season (
        season_sales_id INT PRIMARY KEY AUTO_INCREMENT,
        product_id INT NOT NULL,
        category VARCHAR(50),
        season VARCHAR(10),
        year INT,
        total_qty INT,
        total_amount DECIMAL(15, 2),
        order_count INT,
        avg_order_value DECIMAL(12, 2),
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_product_id (product_id),
        INDEX idx_category (category),
        INDEX idx_season (season)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 退货事实表
CREATE TABLE
    fact_returns (
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
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 日KPI表
CREATE TABLE
    kpi_daily (
        kpi_id INT PRIMARY KEY AUTO_INCREMENT,
        kpi_date DATE NOT NULL UNIQUE,
        total_orders INT,
        total_sales DECIMAL(15, 2),
        avg_order_value DECIMAL(12, 2),
        return_rate DECIMAL(5, 4),
        total_users INT,
        new_users INT,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
        INDEX idx_kpi_date (kpi_date)
    ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
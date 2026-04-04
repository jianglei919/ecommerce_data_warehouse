#!/bin/bash

# 测试脚本：向 App 和 Web 业务系统添加新的订单和产品
# 并观察数据是否实时同步到 ecommerce_warehouse 库

set -e

echo "=========================================="
echo "数据同步测试脚本"
echo "=========================================="
echo ""

# 数据库凭证
APP_DB_HOST="127.0.0.1"
APP_DB_PORT="3306"
APP_DB_USER="root"
APP_DB_PASS="root"
APP_DB_NAME="ecommerce_source_app"

WEB_DB_HOST="127.0.0.1"
WEB_DB_PORT="3307"
WEB_DB_USER="root"
WEB_DB_PASS="root"
WEB_DB_NAME="ecommerce_source_web"

WAREHOUSE_DB_HOST="127.0.0.1"
WAREHOUSE_DB_PORT="3308"
WAREHOUSE_DB_USER="root"
WAREHOUSE_DB_PASS="root"
WAREHOUSE_DB_NAME="ecommerce_warehouse"

echo "✅ Step 1: 检查当前 App 库的订单和数据"
echo "==============================================="
mysql -h $APP_DB_HOST -P $APP_DB_PORT -u $APP_DB_USER -p$APP_DB_PASS $APP_DB_NAME << EOF
SELECT "=== 当前 App 订单统计 ===" as '';
SELECT COUNT(*) as total_orders FROM orders;
SELECT COUNT(*) as total_order_items FROM order_items;
SELECT COUNT(*) as total_products FROM products;
EOF

echo ""
echo "✅ Step 2: 在 App 系统创建新订单和产品"
echo "==============================================="

# 在 App 库中创建新产品
mysql -h $APP_DB_HOST -P $APP_DB_PORT -u $APP_DB_USER -p$APP_DB_PASS $APP_DB_NAME << EOF
-- 插入新产品
INSERT INTO products (product_name, category, price, stock_quantity) VALUES 
('高端安卓手机', '电子产品', 2999.99, 50),
('无线充电器', '配件', 199.99, 100);

SELECT "新产品已创建:" as '';
SELECT product_id, product_name, price FROM products WHERE product_name IN ('高端安卓手机', '无线充电器');

-- 为新用户 (ID=11) 创建新订单
INSERT INTO orders (user_id, order_date, total_amount, status) VALUES 
(11, '2024-04-04', 3199.98, 'pending');

SELECT "新订单已创建:" as '';
SELECT @new_order_id := order_id, user_id, order_date, total_amount, status 
FROM orders WHERE user_id = 11 ORDER BY order_id DESC LIMIT 1;

-- 为新订单添加订单项
INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES 
((SELECT MAX(order_id) FROM orders), (SELECT product_id FROM products WHERE product_name = '高端安卓手机' LIMIT 1), 1, 2999.99),
((SELECT MAX(order_id) FROM orders), (SELECT product_id FROM products WHERE product_name = '无线充电器' LIMIT 1), 1, 199.99);

SELECT "订单项已添加:" as '';
SELECT o.order_id, p.product_name, oi.quantity, oi.unit_price 
FROM order_items oi 
JOIN orders o ON oi.order_id = o.order_id 
JOIN products p ON oi.product_id = p.product_id 
WHERE o.user_id = 11;
EOF

echo ""
echo "✅ Step 3: 检查当前 Web 库的订单和数据"
echo "==============================================="
mysql -h $WEB_DB_HOST -P $WEB_DB_PORT -u $WEB_DB_USER -p$WEB_DB_PASS $WEB_DB_NAME << EOF
SELECT "=== 当前 Web 订单统计 ===" as '';
SELECT COUNT(*) as total_orders FROM orders;
SELECT COUNT(*) as total_order_items FROM order_items;
SELECT COUNT(*) as total_products FROM products;
EOF

echo ""
echo "✅ Step 4: 在 Web 系统创建新订单和产品"
echo "==============================================="

mysql -h $WEB_DB_HOST -P $WEB_DB_PORT -u $WEB_DB_USER -p$WEB_DB_PASS $WEB_DB_NAME << EOF
-- 插入新产品
INSERT INTO products (product_name, category, price, stock_quantity) VALUES 
('云存储服务', '网络服务', 99.99, 1000),
('网站建设服务', '网络服务', 5000.00, 20);

SELECT "新产品已创建:" as '';
SELECT product_id, product_name, price FROM products WHERE product_name IN ('云存储服务', '网站建设服务');

-- 生成新的 Web 订单号
SET @new_order_no = CONCAT('WEB-', DATE_FORMAT(NOW(), '%Y%m%d'), '-', LPAD(FLOOR(RAND()*100000), 5, '0'));

-- 为新用户 (ID=11) 创建新订单
INSERT INTO orders (order_no, user_id, order_date, total_amount, status) VALUES 
(@new_order_no, 11, '2024-04-04', 5099.99, 'pending');

SELECT "新Web订单已创建:" as '';
SELECT order_no, user_id, order_date, total_amount, status 
FROM orders WHERE order_no = @new_order_no;

-- 为新订单添加订单项 (Web 使用 order_no)
INSERT INTO order_items (order_no, product_id, quantity, unit_price) VALUES 
((SELECT order_no FROM orders WHERE user_id = 11 ORDER BY order_id DESC LIMIT 1), (SELECT product_id FROM products WHERE product_name = '云存储服务' LIMIT 1), 5, 99.99),
((SELECT order_no FROM orders WHERE user_id = 11 ORDER BY order_id DESC LIMIT 1), (SELECT product_id FROM products WHERE product_name = '网站建设服务' LIMIT 1), 1, 5000.00);

SELECT "订单项已添加:" as '';
SELECT o.order_no, p.product_name, oi.quantity, oi.unit_price 
FROM order_items oi 
JOIN orders o ON oi.order_no = o.order_no 
JOIN products p ON oi.product_id = p.product_id 
WHERE o.user_id = 11;
EOF

echo ""
echo "✅ Step 5: 检查数据是否已同步到 Warehouse 库"
echo "==============================================="
sleep 5 # 等待 ETL 处理

mysql -h $WAREHOUSE_DB_HOST -P $WAREHOUSE_DB_PORT -u $WAREHOUSE_DB_USER -p$WAREHOUSE_DB_PASS $WAREHOUSE_DB_NAME << EOF
SELECT "=== Warehouse 中的订单统计 ===" as '';
SELECT COUNT(*) as total_dim_orders FROM dim_orders;
SELECT COUNT(*) as total_dim_products FROM dim_products;

SELECT "最新的订单数据 (Source: APP or WEB):" as '';
SELECT order_id, source, user_id, order_date, total_amount, status 
FROM dim_orders 
WHERE order_date >= '2024-04-04' AND user_id = 11
ORDER BY order_id DESC
LIMIT 5;

SELECT "最新的产品数据:" as '';
SELECT product_id, product_name, category, price, source 
FROM dim_products 
WHERE product_name IN ('高端安卓手机', '无线充电器', '云存储服务', '网站建设服务')
ORDER BY product_id DESC;

SELECT "事实表 (按产品和时间聚合):" as '';
SELECT sale_date, product_id, source, SUM(quantity) as total_qty, SUM(total_price) as total_sales
FROM fact_sales_by_product_time 
WHERE sale_date = '2024-04-04'
GROUP BY sale_date, product_id, source
ORDER BY total_sales DESC
LIMIT 10;

SELECT "同步日志 (最近的100条):" as '';
SELECT id, source, order_id, sync_status, message, sync_timestamp 
FROM sync_log 
WHERE source IN ('APP', 'WEB')
ORDER BY sync_timestamp DESC 
LIMIT 10;
EOF

echo ""
echo "=========================================="
echo "✅ 测试完成！"
echo "=========================================="
echo ""
echo "总结:"
echo "- 在 App 系统添加了 2 个新产品和 1 个新订单（包含 2 个订单项）"
echo "- 在 Web 系统添加了 2 个新产品和 1 个新订单（包含 2 个订单项）"
echo "- 事实表中应该有相应的聚合数据"
echo ""
echo "检查点："
echo "1. dim_orders 中应该有新增的订单记录"
echo "2. dim_products 中应该有新增的产品记录"
echo "3. fact_sales_by_product_time 中应该有2024-04-04的销售数据"
echo "4. sync_log 中应该有相应的同步日志"
echo ""

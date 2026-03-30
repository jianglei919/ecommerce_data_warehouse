-- 样本数据初始化脚本
-- 向 ecommerce_source_1 和 ecommerce_source_2 插入示例业务数据
-- 向 ecommerce_warehouse 插入汇聚的分析数据
-- ============================================================================
-- 第1部分: 向 ecommerce_source_1 插入数据 (分店1)
-- ============================================================================
USE ecommerce_source_1;

-- 插入用户数据
INSERT INTO
    users (name, email, phone, city, register_date)
VALUES
    (
        '张三',
        'zhangsan@example.com',
        '13800000001',
        '北京',
        '2023-01-15'
    ),
    (
        '李四',
        'lisi@example.com',
        '13800000002',
        '上海',
        '2023-02-20'
    ),
    (
        '王五',
        'wangwu@example.com',
        '13800000003',
        '广州',
        '2023-03-10'
    );

-- 插入商品数据 source_1
INSERT INTO
    products (
        name,
        description,
        category,
        price,
        cost,
        brand,
        stock_qty,
        is_active
    )
VALUES
    (
        'iPhone 14 Pro',
        '苹果旗舰手机',
        '手机',
        8999.00,
        5000.00,
        'Apple',
        50,
        TRUE
    ),
    (
        'MacBook Pro 16',
        '专业级笔记本电脑',
        '电脑',
        19999.00,
        12000.00,
        'Apple',
        30,
        TRUE
    ),
    (
        'AirPods Pro',
        '降噪蓝牙耳机',
        '配件',
        1999.00,
        800.00,
        'Apple',
        100,
        TRUE
    ),
    (
        'iPad Air',
        '平板电脑',
        '平板',
        7499.00,
        4000.00,
        'Apple',
        45,
        TRUE
    );

-- 插入订单数据 source_1
INSERT INTO
    orders (user_id, order_date, total_amount, status)
VALUES
    (1, '2024-01-10', 10998.00, 'completed'),
    (2, '2024-01-15', 21998.00, 'completed'),
    (3, '2024-01-20', 9498.00, 'completed'),
    (1, '2024-02-05', 7499.00, 'completed');

-- ============================================================================
-- 第2部分: 向 ecommerce_source_2 插入数据 (分店2)
-- ============================================================================
USE ecommerce_source_2;

-- 插入用户数据 source_2
INSERT INTO
    users (name, email, phone, city, register_date)
VALUES
    (
        '赵六',
        'zhaoliu@example.com',
        '13800000004',
        '深圳',
        '2023-04-05'
    ),
    (
        '孙七',
        'sunqi@example.com',
        '13800000005',
        '杭州',
        '2023-05-12'
    ),
    (
        '周八',
        'zhouba@example.com',
        '13800000006',
        '成都',
        '2023-06-18'
    );

-- 插入商品数据 source_2
INSERT INTO
    products (
        name,
        description,
        category,
        price,
        cost,
        brand,
        stock_qty,
        is_active
    )
VALUES
    (
        'Samsung Galaxy S24',
        '安卓旗舰手机',
        '手机',
        7999.00,
        4500.00,
        'Samsung',
        40,
        TRUE
    ),
    (
        'Dell XPS 13',
        '轻薄笔记本电脑',
        '电脑',
        12999.00,
        8000.00,
        'Dell',
        25,
        TRUE
    ),
    (
        'Sony WH-1000XM5',
        '降噪耳机',
        '配件',
        2499.00,
        1200.00,
        'Sony',
        80,
        TRUE
    ),
    (
        'Samsung Galaxy Tab S9',
        '安卓平板',
        '平板',
        5999.00,
        3000.00,
        'Samsung',
        50,
        TRUE
    );

-- 数据初始化完成
-- 注意: 详细的订单、订单项目、评价、退货数据由后续的聚合查询自动生成
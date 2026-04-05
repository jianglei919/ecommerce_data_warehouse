package com.uwindsor.warehouse.service;

import com.uwindsor.warehouse.dto.OrderCreateRequest;
import com.uwindsor.warehouse.event.OrderEvent;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单服务
 * 处理订单创建、用户/产品验证
 */
@Slf4j
@Service
public class OrderService {

    private HikariDataSource appDataSource;
    private HikariDataSource webDataSource;

    public OrderService() {
        // 初始化两个数据源
        initAppDataSource();
        initWebDataSource();
    }

    private void initAppDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(
                    "jdbc:mysql://app-db:3306/ecommerce_source_app?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            config.setUsername("root");
            config.setPassword("root");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setAutoCommit(true);

            this.appDataSource = new HikariDataSource(config);
            log.info("✓ AppDataSource initialized successfully (app-db)");
        } catch (Exception e) {
            log.error("✗ Failed to initialize AppDataSource: {}", e.getMessage());
        }
    }

    private void initWebDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(
                    "jdbc:mysql://web-db:3306/ecommerce_source_web?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=30000&enableKeepAlive=true&maxReconnects=3");
            config.setUsername("root");
            config.setPassword("root");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            config.setMaximumPoolSize(20);
            config.setMinimumIdle(5);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setAutoCommit(true);
            config.setLeakDetectionThreshold(15000);

            this.webDataSource = new HikariDataSource(config);
            log.info("✓ WebDataSource initialized successfully (web-db)");
        } catch (Exception e) {
            log.error("✗ Failed to initialize WebDataSource on first try: {}", e.getMessage());
            log.info("⏳ Retrying WebDataSource initialization in 5 seconds...");

            try {
                Thread.sleep(5000);
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(
                        "jdbc:mysql://web-db:3306/ecommerce_source_web?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=30000&enableKeepAlive=true&maxReconnects=3");
                config.setUsername("root");
                config.setPassword("root");
                config.setDriverClassName("com.mysql.cj.jdbc.Driver");
                config.setMaximumPoolSize(20);
                config.setMinimumIdle(5);
                config.setConnectionTimeout(30000);
                config.setIdleTimeout(600000);
                config.setMaxLifetime(1800000);
                config.setAutoCommit(true);
                config.setLeakDetectionThreshold(15000);

                this.webDataSource = new HikariDataSource(config);
                log.info("✓ WebDataSource initialized successfully on retry (web-db)");
            } catch (Exception retryEx) {
                log.error("✗ Failed to initialize WebDataSource on retry: {}", retryEx.getMessage());
                log.warn("⚠ WebDataSource will remain null - WEB products may not load");
                this.webDataSource = null;
            }
        }
    }

    /**
     * 根据来源获取对应的数据源 (public版本供其他service使用)
     */
    public HikariDataSource getDataSourceBySourcePublic(String source) {
        if ("WEB".equalsIgnoreCase(source)) {
            return webDataSource;
        }
        return appDataSource; // 默认返回APP数据源
    }

    /**
     * 根据来源获取对应的数据源 (private版本)
     */
    private HikariDataSource getDataSourceBySource(String source) {
        if ("WEB".equalsIgnoreCase(source)) {
            return webDataSource;
        }
        return appDataSource; // 默认返回APP数据源
    }

    /**
     * 根据来源获取所有产品列表 (APP or WEB)
     */
    public List<Map<String, Object>> getAllProducts(String source) {
        List<Map<String, Object>> products = new ArrayList<>();
        try {
            HikariDataSource dataSource = getDataSourceBySource(source);
            if (dataSource == null) {
                log.warn("⚠ DataSource is null for source: {}", source);
                return products;
            }

            String sql = "SELECT product_id, product_name, category, price, stock_quantity FROM products";
            Connection conn = dataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                Map<String, Object> product = new HashMap<>();
                product.put("product_id", rs.getLong("product_id"));
                product.put("product_name", rs.getString("product_name"));
                product.put("category", rs.getString("category"));
                product.put("price", rs.getDouble("price"));
                product.put("stock_quantity", rs.getInt("stock_quantity"));
                product.put("source", source);
                products.add(product);
            }

            rs.close();
            stmt.close();
            conn.close();

            log.info("✓ Fetched {} products from {} source", products.size(), source);
        } catch (Exception e) {
            log.error("✗ Error fetching products from {} source: {}", source, e.getMessage());
            e.printStackTrace();
        }
        return products;
    }

    /**
     * 获取或创建用户
     * 如果userId存在则返回该用户ID
     * 如果不存在则创建一个默认用户并返回ID
     */
    public Long getOrCreateUser(Long userId) {
        try {
            if (appDataSource == null) {
                initAppDataSource();
            }

            Connection conn = appDataSource.getConnection();

            // 如果提供了userId，先检查是否存在
            if (userId != null && userId > 0) {
                String checkSql = "SELECT user_id FROM users WHERE user_id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                checkStmt.setLong(1, userId);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    log.info("✓ User {} found in database", userId);
                    rs.close();
                    checkStmt.close();
                    conn.close();
                    return userId;
                }
                rs.close();
                checkStmt.close();
            }

            // 如果userId不存在，创建默认用户
            String username = "user_" + System.currentTimeMillis();
            String email = username + "@warehouse.local";

            String insertSql = "INSERT INTO users (username, email, created_at) VALUES (?, ?, NOW())";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            insertStmt.setString(1, username);
            insertStmt.setString(2, email);
            insertStmt.executeUpdate();

            ResultSet generatedKeys = insertStmt.getGeneratedKeys();
            Long newUserId = null;
            if (generatedKeys.next()) {
                newUserId = generatedKeys.getLong(1);
                log.info("✓ Created new user with ID: {}", newUserId);
            }

            generatedKeys.close();
            insertStmt.close();
            conn.close();

            return newUserId;

        } catch (Exception e) {
            log.error("✗ Error getting/creating user: {}", e.getMessage());
            // 返回默认用户ID
            return 1L;
        }
    }

    /**
     * 获取产品信息 (支持APP/WEB来源)
     */
    public Map<String, Object> getProductInfo(Long productId, String source) {
        try {
            HikariDataSource dataSource = getDataSourceBySource(source);
            if (dataSource == null) {
                log.warn("⚠ DataSource is null for source: {}", source);
                return null;
            }

            String sql = "SELECT product_id, product_name, category, price, stock_quantity FROM products WHERE product_id = ?";
            Connection conn = dataSource.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setLong(1, productId);
            ResultSet rs = stmt.executeQuery();

            Map<String, Object> product = null;
            if (rs.next()) {
                product = new HashMap<>();
                product.put("product_id", rs.getLong("product_id"));
                product.put("product_name", rs.getString("product_name"));
                product.put("category", rs.getString("category"));
                product.put("price", rs.getDouble("price"));
                product.put("stock_quantity", rs.getInt("stock_quantity"));
                product.put("source", source);
            }

            rs.close();
            stmt.close();
            conn.close();

            return product;

        } catch (Exception e) {
            log.error("✗ Error fetching product {} from {} source: {}", productId, source, e.getMessage());
            return null;
        }
    }

    /**
     * 将OrderCreateRequest转换为OrderEvent
     */
    public OrderEvent createOrderEvent(OrderCreateRequest request) {
        try {
            // 验证或创建用户
            Long userId = getOrCreateUser(request.getUserId());

            // 获取产品信息（如果指定了productId）
            String productName = request.getProductName();
            Long productId = request.getProductId();
            String source = request.getSource() != null ? request.getSource() : "APP";

            if (productId != null && productId > 0) {
                Map<String, Object> productInfo = getProductInfo(productId, source);
                if (productInfo != null) {
                    productName = (String) productInfo.get("product_name");
                }
            }

            // 统一订单ID格式: ORD-时间戳-随机数
            String normalizedOrderId = normalizeOrderId(request.getOrderId());

            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(normalizedOrderId)
                    .userId(userId)
                    .productId(productId)
                    .productName(productName)
                    .totalAmount(request.getTotalAmount() != null ? request.getTotalAmount() : 0.0)
                    .eventType(request.getStatus().equals("CANCELLED") ? "ORDER_CANCELLED" : "ORDER_CREATED")
                    .source(source)
                    .orderDate(LocalDate.now().toString())
                    .orderStatus(request.getStatus())
                    .itemCount(request.getQuantity() != null ? request.getQuantity() : 1)
                    .eventTimestamp(System.currentTimeMillis())
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .version(1)
                    .payload(request.getRemarks())
                    .build();

            log.info("✓ Created OrderEvent: eventId={}, orderId={}, userId={}, productId={}, source={}",
                    event.getEventId(), event.getOrderId(), event.getUserId(), event.getProductId(), source);

            return event;

        } catch (Exception e) {
            log.error("✗ Error creating OrderEvent: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * 统一订单ID格式
     * 将 ORD-xxx, ORDER-xxx, APP-xxx 等统一转换为 ORD-时间戳-xxxx 格式
     */
    private String normalizeOrderId(String originalOrderId) {
        if (originalOrderId == null || originalOrderId.trim().isEmpty()) {
            return "ORD-" + System.currentTimeMillis() + "-" + new Random().nextInt(10000);
        }

        // 如果已经是标准格式，直接返回
        if (originalOrderId.startsWith("ORD-")) {
            return originalOrderId;
        }

        // 提取原始ID中的关键部分
        String id = originalOrderId
                .replaceAll("^(ORD|ORDER|APP|WEB)-?", "")
                .replaceAll("[^a-zA-Z0-9-]", "");

        return "ORD-" + System.currentTimeMillis() + "-" + id;
    }
}

package com.uwindsor.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uwindsor.warehouse.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.UUID;

/**
 * 数据导入服务
 * 处理产品和订单的导入、更新、以及Kafka消息发送
 */
@Slf4j
@Service
public class DataImportService {

    @Autowired
    private OrderService orderService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 导入产品数据
     * 
     * @param db       业务库 (APP|WEB)
     * @param dataList 产品数据列表
     * @return 导入的记录数
     */
    public int importProducts(String db, List<?> dataList) throws Exception {
        HikariDataSource dataSource = orderService.getDataSourceBySourcePublic(db);
        if (dataSource == null) {
            throw new RuntimeException("无效的业务库: " + db);
        }

        int successCount = 0;
        List<Map<String, Object>> importedProducts = new ArrayList<>();

        for (Object item : dataList) {
            try {
                Map<String, Object> product = objectMapper.convertValue(item, Map.class);

                boolean isUpdate = product.containsKey("product_id") && product.get("product_id") != null;

                if (isUpdate) {
                    // 更新产品
                    successCount += updateProduct(dataSource, product);
                    log.info("✓ 更新产品 ID: {}", product.get("product_id"));
                } else {
                    // 新增产品
                    Long generatedId = insertProduct(dataSource, product);
                    if (generatedId > 0) {
                        successCount++;
                        product.put("product_id", generatedId);
                        log.info("✓ 新增产品 ID: {}, 名称: {}", generatedId, product.get("product_name"));
                    }
                }

                importedProducts.add(product);

            } catch (Exception e) {
                log.error("✗ 处理产品失败: {}", e.getMessage());
            }
        }

        // 发送Kafka消息通知warehouse更新
        if (successCount > 0) {
            sendProductSyncEvent(db, importedProducts, successCount);
        }

        return successCount;
    }

    /**
     * 导入订单数据
     * 
     * @param db       业务库 (APP|WEB)
     * @param dataList 订单数据列表
     * @return 导入的记录数
     */
    public int importOrders(String db, List<?> dataList) throws Exception {
        HikariDataSource dataSource = orderService.getDataSourceBySourcePublic(db);
        if (dataSource == null) {
            throw new RuntimeException("无效的业务库: " + db);
        }

        int successCount = 0;
        List<OrderEvent> orderEvents = new ArrayList<>();

        for (Object item : dataList) {
            try {
                Map<String, Object> orderData = objectMapper.convertValue(item, Map.class);

                boolean isUpdate = orderData.containsKey("order_id") && orderData.get("order_id") != null;

                log.info("📦 处理订单: isUpdate={}, 数据={}", isUpdate, orderData);

                if (isUpdate) {
                    // 更新订单
                    int result = updateOrder(dataSource, orderData);
                    successCount += result;
                    log.info("✓ 更新订单: {}, result={}", orderData.get("order_id"), result);
                } else {
                    // 新增订单
                    Long generatedOrderId = insertOrder(dataSource, orderData);
                    if (generatedOrderId > 0) {
                        successCount++;
                        orderData.put("order_id", generatedOrderId);
                        log.info("✓ 新增订单 ID: {}", generatedOrderId);
                    }
                }

                // 构建OrderEvent
                OrderEvent event = buildOrderEvent(orderData, db);
                orderEvents.add(event);

            } catch (Exception e) {
                log.error("✗ 处理订单失败: {}", e.getMessage(), e);
            }
        }

        // 发送Kafka消息通知warehouse更新
        if (successCount > 0) {
            for (OrderEvent event : orderEvents) {
                kafkaTemplate.send("order-events", event.getEventId(), event);
            }
            log.info("✓ 发送{}个订单事件到Kafka", orderEvents.size());
        }

        return successCount;
    }

    /**
     * 插入新产品，返回生成的product_id
     */
    private Long insertProduct(HikariDataSource dataSource, Map<String, Object> product) throws SQLException {
        String sql = "INSERT INTO products (product_name, category, price, stock_quantity) VALUES (?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, (String) product.get("product_name"));
            stmt.setString(2, (String) product.getOrDefault("category", ""));
            stmt.setDouble(3, ((Number) product.getOrDefault("price", 0)).doubleValue());
            stmt.setInt(4, ((Number) product.getOrDefault("stock_quantity", 0)).intValue());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                return -1L;
            }

            // 获取生成的product_id
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }
            }
            return -1L;
        }
    }

    /**
     * 更新现有产品 - 只更新提供的字段，避免覆盖null值
     */
    private int updateProduct(HikariDataSource dataSource, Map<String, Object> product) throws SQLException {
        String productId = String.valueOf(product.get("product_id"));

        // 构建动态SQL - 只包含提供的字段
        StringBuilder sql = new StringBuilder("UPDATE products SET ");
        List<Object> params = new ArrayList<>();

        if (product.containsKey("product_name") && product.get("product_name") != null) {
            sql.append("product_name=?, ");
            params.add(product.get("product_name"));
        }

        if (product.containsKey("category") && product.get("category") != null) {
            sql.append("category=?, ");
            params.add(product.get("category"));
        }

        if (product.containsKey("brand") && product.get("brand") != null) {
            sql.append("brand=?, ");
            params.add(product.get("brand"));
        }

        if (product.containsKey("price") && product.get("price") != null) {
            sql.append("price=?, ");
            params.add(((Number) product.get("price")).doubleValue());
        }

        if (product.containsKey("stock_quantity") && product.get("stock_quantity") != null) {
            sql.append("stock_quantity=?, ");
            params.add(((Number) product.get("stock_quantity")).intValue());
        }

        // 移除最后的逗号空格
        if (params.isEmpty()) {
            log.warn("⚠️ 没有字段需要更新，product_id={}", productId);
            return 0;
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE product_id=?");
        params.add(Long.parseLong(productId));

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // 绑定参数
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int result = stmt.executeUpdate();
            if (result > 0) {
                log.info("✓ 更新产品 product_id={}, 更新字段={}", productId, params.size() - 1);
            }
            return result;
        }
    }

    /**
     * 插入新订单，返回生成的订单ID
     */
    private Long insertOrder(HikariDataSource dataSource, Map<String, Object> order) throws SQLException {
        // 验证必需字段
        if (!order.containsKey("product_id") || order.get("product_id") == null) {
            log.warn("⚠️ 订单缺少product_id字段，无法创建order_items");
            throw new IllegalArgumentException("订单必须包含product_id字段");
        }

        String sql = "INSERT INTO orders (user_id, order_date, total_amount, status) VALUES (?, CURDATE(), ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            Number userId = (Number) order.getOrDefault("user_id", 1);
            stmt.setLong(1, userId.longValue());

            Number totalAmount = (Number) order.getOrDefault("total_amount", 0);
            stmt.setDouble(2, totalAmount.doubleValue());

            stmt.setString(3, (String) order.getOrDefault("status", "pending"));

            int result = stmt.executeUpdate();

            // 获取生成的订单ID
            if (result > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        long orderId = generatedKeys.getLong(1);
                        // 创建order_items记录
                        insertOrderItem(dataSource, orderId, order);
                        return orderId;
                    }
                }
            }

            return -1L;
        }
    }

    /**
     * 更新现有订单 - 只更新提供的字段，避免覆盖null值
     */
    private int updateOrder(HikariDataSource dataSource, Map<String, Object> order) throws SQLException {
        Object orderIdObj = order.get("order_id");
        long orderId = orderIdObj instanceof Number
                ? ((Number) orderIdObj).longValue()
                : Long.parseLong(orderIdObj.toString());

        // 构建动态SQL - 只包含提供的字段
        StringBuilder sql = new StringBuilder("UPDATE orders SET ");
        List<Object> params = new ArrayList<>();

        if (order.containsKey("total_amount") && order.get("total_amount") != null) {
            sql.append("total_amount=?, ");
            params.add(((Number) order.get("total_amount")).doubleValue());
        }

        if (order.containsKey("status") && order.get("status") != null) {
            sql.append("status=?, ");
            params.add(order.get("status"));
        }

        // 移除最后的逗号空格
        if (params.isEmpty()) {
            log.warn("⚠️ 没有字段需要更新，order_id={}", orderId);
            return 0;
        }
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE order_id=?");
        params.add(orderId);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            // 绑定参数
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            int result = stmt.executeUpdate();

            if (result > 0) {
                log.info("✓ 更新订单 order_id={}, 更新字段={}", orderId, params.size() - 1);

                // 如果有product_id，更新或插入order_items
                if (order.containsKey("product_id") && order.get("product_id") != null) {
                    // 先尝试更新，如果没有记录则插入
                    int updateCount = updateOrderItem(dataSource, orderId, order);
                    if (updateCount == 0) {
                        insertOrderItem(dataSource, orderId, order);
                    }
                }
            }
            return result;
        }
    }

    /**
     * 插入订单项
     */
    private void insertOrderItem(HikariDataSource dataSource, long orderId, Map<String, Object> order) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price, subtotal) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            Number productId = (Number) order.get("product_id");
            Number quantityNum = (Number) order.getOrDefault("quantity", 1);
            Number totalAmount = (Number) order.getOrDefault("total_amount", 0);

            int quantity = quantityNum.intValue();
            double unitPrice = totalAmount.doubleValue() / quantity;
            double subtotal = totalAmount.doubleValue();

            stmt.setLong(1, orderId);
            stmt.setLong(2, productId.longValue());
            stmt.setInt(3, quantity);
            stmt.setDouble(4, unitPrice);
            stmt.setDouble(5, subtotal);

            stmt.executeUpdate();
            log.info("✓ 插入订单项: orderId={}, productId={}", orderId, productId);

        } catch (SQLException e) {
            log.error("✗ 插入订单项失败: {}", e.getMessage());
        }
    }

    /**
     * 更新订单项
     */
    private int updateOrderItem(HikariDataSource dataSource, long orderId, Map<String, Object> order) {
        String sql = "UPDATE order_items SET quantity=?, unit_price=?, subtotal=? WHERE order_id=? AND product_id=?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            Number productId = (Number) order.get("product_id");
            Number quantityNum = (Number) order.getOrDefault("quantity", 1);
            Number totalAmount = (Number) order.getOrDefault("total_amount", 0);

            int quantity = quantityNum.intValue();
            double unitPrice = totalAmount.doubleValue() / quantity;
            double subtotal = totalAmount.doubleValue();

            stmt.setInt(1, quantity);
            stmt.setDouble(2, unitPrice);
            stmt.setDouble(3, subtotal);
            stmt.setLong(4, orderId);
            stmt.setLong(5, productId.longValue());

            int updateCount = stmt.executeUpdate();
            if (updateCount > 0) {
                log.info("✓ 更新订单项: orderId={}, productId={}, quantity={}", orderId, productId, quantity);
            }
            return updateCount;
        } catch (SQLException e) {
            log.error("✗ 更新订单项失败 orderId {}：{}", orderId, e.getMessage());
            return 0;
        }
    }

    /**
     * 构建OrderEvent用于Kafka发送
     */
    private OrderEvent buildOrderEvent(Map<String, Object> orderData, String source) {
        Object totalAmountObj = orderData.get("total_amount");
        double totalAmount = 0;
        if (totalAmountObj instanceof Number) {
            totalAmount = ((Number) totalAmountObj).doubleValue();
        }

        // 处理order_id可能是Long或String的情况
        String orderId = null;
        Object orderIdObj = orderData.get("order_id");
        if (orderIdObj instanceof Number) {
            orderId = ((Number) orderIdObj).toString();
        } else if (orderIdObj != null) {
            orderId = orderIdObj.toString();
        }

        return OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .orderId(orderId)
                .userId(orderData.get("user_id") != null ? ((Number) orderData.get("user_id")).longValue() : null)
                .productId(
                        orderData.get("product_id") != null ? ((Number) orderData.get("product_id")).longValue() : null)
                .totalAmount(totalAmount)
                .eventType("ORDER_CREATED")
                .source(source)
                .orderDate(LocalDate.now().toString())
                .orderStatus((String) orderData.getOrDefault("status", "PENDING"))
                .itemCount(orderData.get("quantity") != null ? ((Number) orderData.get("quantity")).intValue() : 1)
                .eventTimestamp(System.currentTimeMillis())
                .createdAt(LocalDateTime.now())
                .retryCount(0)
                .version(1)
                .build();
    }

    /**
     * 发送产品同步事件到Kafka
     */
    private void sendProductSyncEvent(String db, List<Map<String, Object>> products, int count) {
        try {
            Map<String, Object> syncEvent = new HashMap<>();
            syncEvent.put("eventId", UUID.randomUUID().toString());
            syncEvent.put("eventType", "PRODUCT_SYNC");
            syncEvent.put("source", db);
            syncEvent.put("productCount", count);
            syncEvent.put("timestamp", System.currentTimeMillis());
            syncEvent.put("data", products);

            kafkaTemplate.send("product-events", (String) syncEvent.get("eventId"), syncEvent);
            log.info("✓ 发送产品同步事件到Kafka");
        } catch (Exception e) {
            log.error("✗ 发送产品同步事件失败: {}", e.getMessage());
        }
    }

    /**
     * 手动同步订单到仓库（从业务库读取已存在的订单，发送Kafka事件）
     * 用于处理直接插入数据库的订单，确保它们被同步到warehouse库
     */
    public int synchronizeOrders(String db, List<?> orderIds) throws Exception {
        HikariDataSource dataSource = orderService.getDataSourceBySourcePublic(db);
        if (dataSource == null) {
            throw new RuntimeException("无效的业务库: " + db);
        }

        int syncCount = 0;
        List<OrderEvent> orderEvents = new ArrayList<>();

        for (Object orderId : orderIds) {
            try {
                // 从业务库查询完整的订单数据
                Map<String, Object> orderData = fetchOrderFromDatabase(dataSource, orderId);
                if (orderData == null) {
                    log.warn("订单 {} 在{}库中不存在", orderId, db);
                    continue;
                }

                // 构建OrderEvent并添加到列表
                OrderEvent event = buildOrderEvent(orderData, db);
                orderEvents.add(event);
                syncCount++;

                log.info("✓ 同步订单 {}: {}", orderId, orderData.get("total_amount"));

            } catch (Exception e) {
                log.error("✗ 同步订单 {} 失败: {}", orderId, e.getMessage());
            }
        }

        // 批量发送Kafka消息
        if (!orderEvents.isEmpty()) {
            for (OrderEvent event : orderEvents) {
                kafkaTemplate.send("order-events", event.getEventId(), event);
            }
            log.info("✓ 发送{}个订单事件到Kafka用于同步", orderEvents.size());
        }

        return syncCount;
    }

    /**
     * 从业务库查询订单数据
     */
    private Map<String, Object> fetchOrderFromDatabase(HikariDataSource dataSource, Object orderId)
            throws SQLException {
        String sql = "SELECT order_id, user_id, order_date, total_amount, status FROM orders WHERE order_id = ?";

        try (Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            // 处理orderId可能是String或Number的情况
            if (orderId instanceof Number) {
                stmt.setLong(1, ((Number) orderId).longValue());
            } else {
                try {
                    stmt.setLong(1, Long.parseLong(orderId.toString()));
                } catch (NumberFormatException e) {
                    stmt.setString(1, orderId.toString());
                }
            }

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> order = new HashMap<>();
                    order.put("order_id", rs.getLong("order_id"));
                    order.put("user_id", rs.getLong("user_id"));
                    order.put("order_date", rs.getDate("order_date"));
                    order.put("total_amount", rs.getDouble("total_amount"));
                    order.put("status", rs.getString("status"));
                    return order;
                }
            }
        }

        return null;
    }
}

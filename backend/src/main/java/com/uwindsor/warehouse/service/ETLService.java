package com.uwindsor.warehouse.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uwindsor.warehouse.event.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.*;

/**
 * ETL 服务 - 提取、转换、加载
 * 负责数据仓库的增量更新和实时同步
 */
@Slf4j
@Service
public class ETLService {

    @Autowired
    @org.springframework.beans.factory.annotation.Qualifier("warehouseDataSource")
    private javax.sql.DataSource warehouseDataSource;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 批量处理订单事件
     * 流程: 获取完整订单数据 -> 字段映射转换 -> 写入数据仓库
     */
    @Transactional
    public void processBatch(List<OrderEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }

        log.info("Starting ETL batch processing for {} events", events.size());

        try (Connection conn = warehouseDataSource.getConnection()) {
            conn.setAutoCommit(false);

            for (OrderEvent event : events) {
                try {
                    processEvent(conn, event);
                } catch (Exception e) {
                    log.error("Error processing event {}: {}", event.getEventId(), e.getMessage());
                    recordSyncLog(conn, event, "FAILED", e.getMessage());
                }
            }

            conn.commit();
            log.info("Batch processing completed successfully");

        } catch (Exception e) {
            log.error("Critical error during batch processing: {}", e.getMessage(), e);
            throw new RuntimeException("ETL batch processing failed", e);
        }
    }

    /**
     * 处理单个事件
     * 1. 解析事件数据
     * 2. 从源数据库取完整数据
     * 3. 字段转换
     * 4. 写入维表
     * 5. 聚合写入事实表
     */
    private void processEvent(Connection warehouseConn, OrderEvent event) throws Exception {
        log.debug("Processing event: {}", event.getEventId());

        // 1. 获取完整订单数据
        Map<String, Object> fullOrderData = fetchFullOrderData(event);
        if (fullOrderData == null) {
            log.warn("Order data not found for event: {}", event.getEventId());
            recordSyncLog(warehouseConn, event, "NOT_FOUND", "Order data not found");
            return;
        }

        // 2. 写入维表（dim_orders）
        updateDimOrders(warehouseConn, fullOrderData, event.getSource());

        // 3. 记录同步日志
        recordSyncLog(warehouseConn, event, "SUCCESS", null);

        log.debug("Event processed successfully: {}", event.getEventId());
    }

    /**
     * 从源数据库获取完整订单数据
     */
    private Map<String, Object> fetchFullOrderData(OrderEvent event) {
        // 这是一个模拟实现，实际需要从 App/Web 数据库查询
        // 在完整实现中应该通过 Repository 或 Mapper 从相应数据源查询
        log.debug("Fetching order data for: source={}, orderId={}", event.getSource(), event.getOrderId());

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("order_id", event.getOrderId());
        orderData.put("user_id", event.getUserId());
        orderData.put("order_date", event.getOrderDate());
        orderData.put("total_amount", event.getTotalAmount());
        orderData.put("item_count", event.getItemCount());
        orderData.put("source", event.getSource());

        // TODO: 实现实际的数据库查询
        return orderData;
    }

    /**
     * 更新维表 dim_orders
     * 插入或更新订单维档
     */
    private void updateDimOrders(Connection conn, Map<String, Object> orderData, String source) throws Exception {
        String source_param = (String) orderData.get("source");
        if (source_param == null) {
            source_param = source;
        }

        String orderId = String.valueOf(orderData.get("order_id"));
        Object userIdObj = orderData.get("user_id");
        Integer userId = null;
        if (userIdObj != null) {
            if (userIdObj instanceof Number) {
                userId = ((Number) userIdObj).intValue();
            } else {
                userId = Integer.parseInt(userIdObj.toString());
            }
        }
        Object orderDateObj = orderData.get("order_date");
        Double totalAmount = null;
        Object totalAmountObj = orderData.get("total_amount");
        if (totalAmountObj != null) {
            if (totalAmountObj instanceof Number) {
                totalAmount = ((Number) totalAmountObj).doubleValue();
            } else {
                totalAmount = Double.parseDouble(totalAmountObj.toString());
            }
        }

        String insertOrUpdateSql = """
                INSERT INTO dim_orders
                (source, app_order_id, web_order_no, user_id, order_date, total_amount, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, 'pending', NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                user_id = VALUES(user_id),
                order_date = VALUES(order_date),
                total_amount = VALUES(total_amount),
                updated_at = NOW()
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertOrUpdateSql)) {
            stmt.setString(1, source_param); // source

            // 设置 app_order_id 或 web_order_no
            if ("APP".equals(source_param)) {
                stmt.setInt(2, Integer.parseInt(orderId)); // app_order_id
                stmt.setNull(3, java.sql.Types.VARCHAR); // web_order_no
            } else {
                stmt.setNull(2, java.sql.Types.INTEGER); // app_order_id
                stmt.setString(3, orderId); // web_order_no
            }

            stmt.setInt(4, userId != null ? userId : 0); // user_id
            stmt.setObject(5, orderDateObj); // order_date
            stmt.setDouble(6, totalAmount != null ? totalAmount : 0.0); // total_amount

            stmt.executeUpdate();
            log.debug("dim_orders updated for order: {} (source={})", orderId, source_param);
        }
    }

    /**
     * 记录同步日志
     * 用于审计和监控 ETL 过程
     */
    private void recordSyncLog(Connection conn, OrderEvent event, String status, String errorMessage) throws Exception {
        if (event == null || event.getEventId() == null) {
            log.warn("Cannot record sync log: event or event_id is null");
            return;
        }

        String logSql = """
                INSERT INTO sync_log
                (event_id, event_type, source, order_id, status, error_message, sync_time)
                VALUES (?, ?, ?, ?, ?, ?, NOW())
                """;

        try (PreparedStatement stmt = conn.prepareStatement(logSql)) {
            stmt.setString(1, event.getEventId()); // event_id
            stmt.setString(2, event.getEventType()); // event_type
            stmt.setString(3, event.getSource()); // source
            stmt.setString(4, event.getOrderId()); // order_id
            stmt.setString(5, status); // status
            stmt.setString(6, errorMessage); // error_message

            stmt.executeUpdate();
            log.debug("Sync log recorded: event={}, status={}", event.getEventId(), status);
        }
    }

    /**
     * 记录错误信息
     */
    public void logErrors(List<OrderEvent> events, Exception e) {
        log.error("Logging errors for {} events", events.size());
        // 实现错误记录逻辑
        for (OrderEvent event : events) {
            try {
                if (warehouseDataSource != null) {
                    try (Connection conn = warehouseDataSource.getConnection()) {
                        recordSyncLog(conn, event, "ERROR", e.getMessage());
                    }
                }
            } catch (Exception ex) {
                log.error("Failed to log error for event {}: {}", event.getEventId(), ex.getMessage());
            }
        }
    }

    /**
     * 批量处理产品事件 - 独立方法
     * 处理产品同步：产品数据 -> dim_products 表
     */
    @Transactional
    public void processBatchProducts(List<Map<String, Object>> productEvents) {
        if (productEvents == null || productEvents.isEmpty()) {
            return;
        }

        log.info("Starting product ETL batch processing for {} events", productEvents.size());

        try (Connection conn = warehouseDataSource.getConnection()) {
            conn.setAutoCommit(false);

            for (Map<String, Object> productEvent : productEvents) {
                try {
                    processProductEvent(conn, productEvent);
                } catch (Exception e) {
                    log.error("Error processing product event: {}", e.getMessage());
                }
            }

            conn.commit();
            log.info("Product batch processing completed successfully");

        } catch (Exception e) {
            log.error("Critical error during product batch processing: {}", e.getMessage(), e);
            throw new RuntimeException("Product ETL batch processing failed", e);
        }
    }

    /**
     * 处理单个产品事件
     */
    private void processProductEvent(Connection conn, Map<String, Object> productEvent) throws Exception {
        log.debug("Processing product event");

        // 获取产品列表
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> productList = (List<Map<String, Object>>) productEvent.get("data");
        if (productList == null || productList.isEmpty()) {
            log.warn("No product data in event");
            return;
        }

        // 处理每个产品
        for (Map<String, Object> product : productList) {
            try {
                updateDimProducts(conn, product);
            } catch (Exception e) {
                log.error("Error updating product {}: {}", product.get("product_id"), e.getMessage());
            }
        }
    }

    /**
     * 更新维表 dim_products
     * 插入或更新产品维档
     */
    private void updateDimProducts(Connection conn, Map<String, Object> product) throws Exception {
        Object productIdObj = product.get("product_id");
        String productName = (String) product.get("product_name");
        String category = (String) product.getOrDefault("category", "");
        String source = (String) product.getOrDefault("source", "APP");

        if (productIdObj == null || productName == null) {
            log.warn("Product missing id or name: {}", product);
            return;
        }

        Integer productId = null;
        if (productIdObj instanceof Number) {
            productId = ((Number) productIdObj).intValue();
        } else {
            productId = Integer.parseInt(productIdObj.toString());
        }

        String insertOrUpdateSql = """
                INSERT INTO dim_products
                (source, product_id, product_name, category, created_at, updated_at)
                VALUES (?, ?, ?, ?, NOW(), NOW())
                ON DUPLICATE KEY UPDATE
                product_name = VALUES(product_name),
                category = VALUES(category),
                updated_at = NOW()
                """;

        try (PreparedStatement stmt = conn.prepareStatement(insertOrUpdateSql)) {
            stmt.setString(1, source);
            stmt.setInt(2, productId);
            stmt.setString(3, productName);
            stmt.setString(4, category);

            stmt.executeUpdate();
            log.info("✓ dim_products updated/inserted: product_id={}, name={}", productId, productName);
        }
    }

    /**
     * 获取同步统计信息
     */
    public Map<String, Object> getSyncStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            String countSql = "SELECT COUNT(*) as total_count, " +
                    "SUM(CASE WHEN status = 'SUCCESS' THEN 1 ELSE 0 END) as success_count, " +
                    "MAX(sync_time) as last_sync_time " +
                    "FROM sync_log";

            try (Connection conn = warehouseDataSource.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(countSql);
                    ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) {
                    long totalCount = rs.getLong("total_count");
                    long successCount = rs.getLong("success_count");
                    Object lastSyncObj = rs.getObject("last_sync_time");

                    stats.put("total_synced", totalCount);

                    // 计算成功率（百分比，保留一位小数）
                    double successRate = totalCount > 0 ? Math.round((double) successCount / totalCount * 1000.0) / 10.0
                            : 0.0;
                    stats.put("success_rate", successRate);

                    // 最后同步时间
                    stats.put("last_sync_time", lastSyncObj != null ? lastSyncObj.toString() : "Never");

                    log.info("Sync statistics: total={}, success={}, rate={}%",
                            totalCount, successCount, successRate);
                }
            }
        } catch (Exception e) {
            log.error("Error fetching sync statistics: {}", e.getMessage());
            stats.put("total_synced", 0);
            stats.put("success_rate", 0.0);
            stats.put("last_sync_time", "Error");
        }

        return stats;
    }

    /**
     * 获取同步日志列表 - 用于前端显示
     */
    public List<Map<String, Object>> getSyncLogs(int limit) {
        List<Map<String, Object>> logs = new ArrayList<>();

        try {
            String sql = "SELECT sync_log_id, event_id, event_type, source, order_id, status, error_message, sync_time "
                    +
                    "FROM sync_log " +
                    "ORDER BY sync_time DESC " +
                    "LIMIT ?";

            try (Connection conn = warehouseDataSource.getConnection();
                    PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, limit);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> log = new HashMap<>();
                        log.put("syncLogId", rs.getLong("sync_log_id"));
                        log.put("eventId", rs.getString("event_id"));
                        log.put("eventType", rs.getString("event_type"));
                        log.put("source", rs.getString("source"));
                        log.put("orderId", rs.getString("order_id"));
                        log.put("status", rs.getString("status"));
                        log.put("errorMessage", rs.getString("error_message"));
                        Object syncTimeObj = rs.getObject("sync_time");
                        log.put("timestamp", syncTimeObj != null ? syncTimeObj.toString() : null);
                        logs.add(log);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching sync logs: {}", e.getMessage());
        }

        return logs;
    }

}

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

    @Autowired(required = false)
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
     * 4. 聚合写入事实表
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

        // 2. 转换数据并写入事实表
        updateSalesFactTable(warehouseConn, fullOrderData, event.getSource());

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
     * 更新销售事实表
     * 根据订单信息更新或插入 fact_sales_by_category_time
     */
    private void updateSalesFactTable(Connection conn, Map<String, Object> orderData, String source) throws Exception {
        String updateSql = """
                INSERT INTO fact_sales_by_category_time
                (category, year, month, day, quantity_sold, sales_amount, avg_price, order_count, last_updated)
                VALUES (?, ?, ?, ?, ?, ?, ?, 1, NOW())
                ON DUPLICATE KEY UPDATE
                quantity_sold = quantity_sold + VALUES(quantity_sold),
                sales_amount = sales_amount + VALUES(sales_amount),
                avg_price = (sales_amount + VALUES(sales_amount)) / (quantity_sold + VALUES(quantity_sold)),
                order_count = order_count + 1,
                last_updated = NOW()
                """;

        try (PreparedStatement stmt = conn.prepareStatement(updateSql)) {
            // 示例：所有订单都归为 "Electronics" 分类
            stmt.setString(1, "Electronics"); // category
            stmt.setInt(2, 2024); // year
            stmt.setInt(3, 1); // month
            stmt.setInt(4, 15); // day
            stmt.setInt(5, (Integer) orderData.get("item_count")); // quantity_sold
            stmt.setDouble(6, (Double) orderData.get("total_amount")); // sales_amount
            stmt.setDouble(7, ((Double) orderData.get("total_amount")) / (Integer) orderData.get("item_count")); // avg_price

            stmt.executeUpdate();
            log.debug("Sales fact table updated for order: {}", orderData.get("order_id"));
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
     * 获取同步统计信息
     */
    public Map<String, Object> getSyncStatistics() {
        Map<String, Object> stats = new HashMap<>();
        // TODO: 从 sync_log 表查询统计信息
        stats.put("total_synced", 0);
        stats.put("last_sync_time", LocalDateTime.now());
        return stats;
    }

}

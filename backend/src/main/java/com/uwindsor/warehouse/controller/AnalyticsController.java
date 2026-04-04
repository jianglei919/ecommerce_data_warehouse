package com.uwindsor.warehouse.controller;

import com.uwindsor.warehouse.event.OrderEvent;
import com.uwindsor.warehouse.service.ETLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 分析接口控制器
 * 提供数据仓库查询和统计功能
 */
@Slf4j
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AnalyticsController {

    @Autowired
    private ETLService etlService;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired(required = false)
    private javax.sql.DataSource warehouseDataSource;

    /**
     * 获取销售统计 - 按分类
     */
    @GetMapping("/sales/by-category")
    public ResponseEntity<?> getSalesByCategory(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            String sql = "SELECT uoi.category, " +
                    "SUM(uoi.quantity) as total_quantity, " +
                    "SUM(uoi.subtotal) as total_sales_amount, " +
                    "COUNT(DISTINCT uo.unified_order_id) as order_count " +
                    "FROM unified_order_items uoi " +
                    "JOIN unified_orders uo ON uoi.unified_order_id = uo.unified_order_id " +
                    "WHERE 1=1";

            if (startDate != null && !startDate.isEmpty()) {
                sql += " AND uo.order_date >= '" + startDate + "'";
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql += " AND uo.order_date <= '" + endDate + "'";
            }

            sql += " GROUP BY uoi.category ORDER BY total_sales_amount DESC";

            Connection conn = warehouseDataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<Map<String, Object>> salesList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("category", rs.getString("category"));
                row.put("total_quantity", rs.getInt("total_quantity"));
                row.put("total_sales_amount", rs.getDouble("total_sales_amount"));
                row.put("order_count", rs.getInt("order_count"));
                salesList.add(row);
            }

            rs.close();
            stmt.close();
            conn.close();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", salesList);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched {} categories", salesList.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching sales by category: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取热门商品排名
     */
    @GetMapping("/products/top-rated")
    public ResponseEntity<?> getTopRatedProducts(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            String sql = "SELECT dp.product_id, dp.product_name, dp.category, dp.brand, " +
                    "COUNT(DISTINCT uoi.unified_order_id) as sales_count, " +
                    "SUM(uoi.quantity) as total_quantity, " +
                    "SUM(uoi.subtotal) as total_sales_amount, " +
                    "ROUND(SUM(uoi.subtotal) / SUM(uoi.quantity), 2) as avg_price " +
                    "FROM dim_products dp " +
                    "LEFT JOIN unified_order_items uoi ON dp.product_id = uoi.product_id " +
                    "GROUP BY dp.product_id, dp.product_name, dp.category, dp.brand " +
                    "ORDER BY total_sales_amount DESC, total_quantity DESC " +
                    "LIMIT " + limit;

            Connection conn = warehouseDataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<Map<String, Object>> productsList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("productId", rs.getString("product_id"));
                row.put("name", rs.getString("product_name"));
                row.put("category", rs.getString("category"));
                row.put("brand", rs.getString("brand"));
                row.put("salesCount", rs.getInt("sales_count"));
                row.put("totalQuantity", rs.getInt("total_quantity"));
                row.put("totalSalesAmount", rs.getDouble("total_sales_amount"));
                row.put("avgPrice", rs.getDouble("avg_price"));
                row.put("rating", 4.0 + (Math.random() * 1.0)); // 4.0-5.0 rating
                row.put("status", "Active");
                productsList.add(row);
            }

            rs.close();
            stmt.close();
            conn.close();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", productsList);
            result.put("limit", limit);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched {} top products", productsList.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching top rated products: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 发送测试订单事件到 Kafka
     * 用于演示和测试 ETL 流程
     */
    @PostMapping("/test/send-order")
    public ResponseEntity<?> sendTestOrder(@RequestBody OrderEvent orderEvent) {
        try {
            orderEvent.setEventId(UUID.randomUUID().toString());
            orderEvent.setEventTimestamp(System.currentTimeMillis());
            orderEvent.setCreatedAt(LocalDateTime.now());
            orderEvent.setRetryCount(0);

            kafkaTemplate.send("order-events", orderEvent.getEventId(), orderEvent)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send test order: {}", ex.getMessage());
                        } else {
                            log.info("Test order sent successfully: {}", orderEvent.getEventId());
                        }
                    });

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "event_id", orderEvent.getEventId(),
                    "message", "Test order sent to Kafka"));

        } catch (Exception e) {
            log.error("Error sending test order: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取 ETL 同步统计
     */
    @GetMapping("/sync/statistics")
    public ResponseEntity<?> getSyncStatistics() {
        try {
            Map<String, Object> stats = etlService.getSyncStatistics();
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "data", stats,
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            log.error("Error fetching sync statistics: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 健康检查端点
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "warehouse-backend",
                "timestamp", LocalDateTime.now()));
    }

}

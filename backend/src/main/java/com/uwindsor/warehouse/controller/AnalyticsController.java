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
                    "COUNT(DISTINCT uoi.order_id) as order_count " +
                    "FROM dim_order_items uoi " +
                    "INNER JOIN dim_orders uo ON uoi.order_id = uo.order_id " +
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
     * 获取总统计数据（总销售额、总订单数等）
     */
    @GetMapping("/sales/summary")
    public ResponseEntity<?> getSalesSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            String sql = "SELECT " +
                    "COUNT(DISTINCT uo.order_id) as total_orders, " +
                    "COALESCE(SUM(uoi.quantity), 0) as total_quantity, " +
                    "COALESCE(SUM(uoi.subtotal), 0) as total_sales_amount " +
                    "FROM dim_orders uo " +
                    "LEFT JOIN dim_order_items uoi ON uo.order_id = uoi.order_id " +
                    "WHERE 1=1";

            if (startDate != null && !startDate.isEmpty()) {
                sql += " AND uo.order_date >= '" + startDate + "'";
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql += " AND uo.order_date <= '" + endDate + "'";
            }

            Connection conn = warehouseDataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            Map<String, Object> result = new HashMap<>();
            if (rs.next()) {
                result.put("total_orders", rs.getInt("total_orders"));
                result.put("total_quantity", rs.getInt("total_quantity"));
                result.put("total_sales_amount", rs.getDouble("total_sales_amount"));
            }

            rs.close();
            stmt.close();
            conn.close();

            result.put("status", "success");
            result.put("timestamp", LocalDateTime.now());

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching sales summary: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取热门商品排名 - 支持按源系统筛选 (APP/WEB/所有)
     * 
     * @param source 源系统 (可选: APP, WEB, 或留空表示所有)
     * @param limit  返回记录数
     */
    @GetMapping("/products/top-rated")
    public ResponseEntity<?> getTopRatedProducts(
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "10") int limit) {

        try {
            // 构建WHERE条件
            String whereClause = "";
            if (source != null && !source.isEmpty() && !source.equals("ALL")) {
                whereClause = " AND uo.source = '" + source + "'";
            }

            // 通过dim_orders关联获取source，确保product_id + source的正确匹配
            String sql = "SELECT dp.source, dp.product_id, dp.product_name, dp.category, dp.brand, " +
                    "COUNT(DISTINCT uoi.order_id) as sales_count, " +
                    "SUM(uoi.quantity) as total_quantity, " +
                    "SUM(uoi.subtotal) as total_sales_amount, " +
                    "ROUND(SUM(uoi.subtotal) / SUM(uoi.quantity), 2) as avg_price " +
                    "FROM dim_products dp " +
                    "LEFT JOIN dim_order_items uoi ON uoi.product_id = dp.product_id " +
                    "LEFT JOIN dim_orders uo ON uoi.order_id = uo.order_id " +
                    "WHERE 1=1 " +
                    "AND dp.source = IFNULL(uo.source, dp.source) " +
                    whereClause +
                    " GROUP BY dp.product_key, dp.source, dp.product_id, dp.product_name, dp.category, dp.brand " +
                    "ORDER BY total_sales_amount DESC, total_quantity DESC " +
                    "LIMIT " + limit;

            Connection conn = warehouseDataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<Map<String, Object>> productsList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("source", rs.getString("source"));
                row.put("productId", rs.getInt("product_id"));
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
            result.put("source", source != null ? source : "ALL");
            result.put("limit", limit);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched {} top products (source: {})", productsList.size(), source != null ? source : "ALL");
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

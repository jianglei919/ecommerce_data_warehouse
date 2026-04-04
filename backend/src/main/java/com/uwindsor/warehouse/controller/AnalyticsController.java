package com.uwindsor.warehouse.controller;

import com.uwindsor.warehouse.event.OrderEvent;
import com.uwindsor.warehouse.service.ETLService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
     * 获取销售趋势 - 按日期分组 (用于Sales Trend图表)
     */
    @GetMapping("/sales/by-date")
    public ResponseEntity<?> getSalesByDate(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            String sql = "SELECT " +
                    "uo.order_date, " +
                    "SUM(uoi.quantity) as total_quantity, " +
                    "SUM(uoi.subtotal) as total_sales_amount, " +
                    "COUNT(DISTINCT uo.order_id) as order_count " +
                    "FROM dim_orders uo " +
                    "LEFT JOIN dim_order_items uoi ON uo.order_id = uoi.order_id " +
                    "WHERE 1=1";

            if (startDate != null && !startDate.isEmpty()) {
                sql += " AND uo.order_date >= '" + startDate + "'";
            }
            if (endDate != null && !endDate.isEmpty()) {
                sql += " AND uo.order_date <= '" + endDate + "'";
            }

            sql += " GROUP BY uo.order_date ORDER BY uo.order_date ASC";

            Connection conn = warehouseDataSource.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            List<Map<String, Object>> salesList = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("order_date", rs.getDate("order_date"));
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

            log.info("Fetched sales data for {} dates", salesList.size());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching sales by date: {}", e.getMessage());
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
     * 直接测试 - 通过 @RequestParam 而不是 @RequestBody 来避免 JSON 反序列化问题
     */
    @PostMapping("/test/simple-order")
    public ResponseEntity<?> sendSimpleOrder(
            @RequestParam String orderId,
            @RequestParam(required = false) Double totalAmount,
            @RequestParam(required = false) String eventType) {
        try {
            OrderEvent event = OrderEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .orderId(orderId)
                    .totalAmount(totalAmount != null ? totalAmount : 0.0)
                    .eventType(eventType != null ? eventType : "ORDER_CREATED")
                    .source("APP")
                    .orderDate(LocalDateTime.now().toString())
                    .orderStatus("PENDING")
                    .itemCount(1)
                    .eventTimestamp(System.currentTimeMillis())
                    .createdAt(LocalDateTime.now())
                    .retryCount(0)
                    .build();

            log.info("[PARAM] Sending order: id={}, orderId={}, amount={}, type={}",
                    event.getEventId(), event.getOrderId(), event.getTotalAmount(), event.getEventType());

            kafkaTemplate.send("order-events", event.getEventId(), event);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "event_id", event.getEventId(),
                    "message", "Simple order sent"));
        } catch (Exception e) {
            log.error("[PARAM] Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 测试 Jackson 反序列化
     */
    @PostMapping("/test/deserialize-test")
    public ResponseEntity<?> testDeserialize(@RequestBody String jsonString) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            log.info(">>> Testing Jackson deserialization");
            log.info(">>> Raw JSON: {}", jsonString);

            OrderEvent event = mapper.readValue(jsonString, OrderEvent.class);
            log.info(">>> After deserialization: orderId={}, eventType={}, source={}",
                    event.getOrderId(), event.getEventType(), event.getSource());

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "orderId", event.getOrderId(),
                    "eventType", event.getEventType(),
                    "source", event.getSource()));
        } catch (Exception e) {
            log.error(">>> Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 发送测试订单事件到 Kafka
     * 用于演示和测试 ETL 流程
     */
    @PostMapping("/test/send-order")
    public ResponseEntity<?> sendTestOrder(@RequestBody OrderEvent orderEvent) {
        try {
            // 立即记录接收到的原始对象
            log.info(">>> RECEIVED ORDER EVENT (after @RequestBody  deserialization):");
            log.info("    eventId={}, eventType={}, source={}",
                    orderEvent.getEventId(), orderEvent.getEventType(), orderEvent.getSource());
            log.info("    orderId={}, userId={}, totalAmount={}",
                    orderEvent.getOrderId(), orderEvent.getUserId(), orderEvent.getTotalAmount());
            log.info("    orderDate={}, orderStatus={}, itemCount={}",
                    orderEvent.getOrderDate(), orderEvent.getOrderStatus(), orderEvent.getItemCount());

            // 确保所有必须的字段都已初始化
            if (orderEvent.getEventId() == null) {
                orderEvent.setEventId(UUID.randomUUID().toString());
            }
            if (orderEvent.getEventType() == null) {
                orderEvent.setEventType("ORDER_CREATED");
            }
            if (orderEvent.getSource() == null) {
                orderEvent.setSource("APP");
            }
            if (orderEvent.getOrderDate() == null) {
                orderEvent.setOrderDate(LocalDateTime.now().toString());
            }
            if (orderEvent.getOrderStatus() == null) {
                orderEvent.setOrderStatus("PENDING");
            }
            if (orderEvent.getUserId() == null) {
                orderEvent.setUserId(System.currentTimeMillis() % 1000); // 使用时间戳作为默认用户ID
            }

            orderEvent.setEventTimestamp(System.currentTimeMillis());
            orderEvent.setCreatedAt(LocalDateTime.now());
            orderEvent.setRetryCount(0);

            log.info("Sending test order event: id={}, source={}, orderId={}, amount={}",
                    orderEvent.getEventId(), orderEvent.getSource(),
                    orderEvent.getOrderId(), orderEvent.getTotalAmount());

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
            log.error("Error sending test order: {}", e.getMessage(), e);
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

package com.uwindsor.warehouse.controller;

import com.uwindsor.warehouse.domain.UnifiedOrder;
import com.uwindsor.warehouse.domain.UnifiedOrderItem;
import com.uwindsor.warehouse.mapper.UnifiedOrderMapper;
import com.uwindsor.warehouse.mapper.UnifiedOrderItemMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 统一订单接口控制器 (V2)
 * 提供聚合后的App和Web订单数据查询
 */
@Slf4j
@RestController
@RequestMapping("/api/unified-orders")
@CrossOrigin(origins = "*", maxAge = 3600)
public class UnifiedOrdersController {

    @Autowired(required = false)
    private UnifiedOrderMapper unifiedOrderMapper;

    @Autowired(required = false)
    private UnifiedOrderItemMapper unifiedOrderItemMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<UnifiedOrder> UNIFIED_ORDER_ROW_MAPPER = (rs, rowNum) -> {
        UnifiedOrder order = new UnifiedOrder();
        order.setUnifiedOrderId(rs.getInt("unified_order_id"));
        order.setSource(rs.getString("source"));
        order.setAppOrderId((Integer) rs.getObject("app_order_id"));
        order.setWebOrderNo(rs.getString("web_order_no"));
        order.setUserId(rs.getInt("user_id"));
        order.setOrderDate(rs.getDate("order_date").toLocalDate());
        order.setTotalAmount(rs.getBigDecimal("total_amount"));
        order.setStatus(rs.getString("status"));
        return order;
    };

    /**
     * 获取所有统一订单列表
     */
    @GetMapping("")
    public ResponseEntity<?> getAllOrders(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        try {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");

            // 构建查询SQL
            StringBuilder sql = new StringBuilder("SELECT * FROM unified_orders WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (source != null && !source.isEmpty()) {
                sql.append(" AND source = ?");
                params.add(source.toUpperCase());
            }
            if (status != null && !status.isEmpty()) {
                sql.append(" AND status = ?");
                params.add(status.toLowerCase());
            }

            sql.append(" ORDER BY order_date DESC, unified_order_id DESC");

            // 获取总数
            String countSql = "SELECT COUNT(*) FROM unified_orders WHERE 1=1";
            List<Object> countParams = new ArrayList<>();
            if (source != null && !source.isEmpty()) {
                countSql += " AND source = ?";
                countParams.add(source.toUpperCase());
            }
            if (status != null && !status.isEmpty()) {
                countSql += " AND status = ?";
                countParams.add(status.toLowerCase());
            }

            Integer total = jdbcTemplate.queryForObject(countSql, Integer.class, countParams.toArray());

            // 分页
            int offset = (page - 1) * pageSize;
            sql.append(" LIMIT ?, ?");
            params.add(offset);
            params.add(pageSize);

            List<UnifiedOrder> orders = jdbcTemplate.query(sql.toString(), UNIFIED_ORDER_ROW_MAPPER, params.toArray());

            result.put("data", orders);
            result.put("pagination", Map.of(
                    "page", page,
                    "pageSize", pageSize,
                    "total", total != null ? total : 0,
                    "totalPages", total != null ? (int) Math.ceil((double) total / pageSize) : 0));
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched {} unified orders (page {}/{})",
                    orders.size(), page, total != null ? (int) Math.ceil((double) total / pageSize) : 0);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching unified orders: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取订单详情及项目信息
     */
    @GetMapping("/{unifiedOrderId}")
    public ResponseEntity<?> getOrderDetail(@PathVariable Integer unifiedOrderId) {

        try {
            // 查询订单主表
            String orderSql = "SELECT * FROM unified_orders WHERE unified_order_id = ?";
            List<UnifiedOrder> orders = jdbcTemplate.query(orderSql, UNIFIED_ORDER_ROW_MAPPER, unifiedOrderId);

            if (orders.isEmpty()) {
                return ResponseEntity.status(404).body(Map.of(
                        "status", "error",
                        "message", "Order not found"));
            }

            UnifiedOrder order = orders.get(0);

            // 查询订单项
            String itemsSql = """
                    SELECT
                        unified_item_id, unified_order_id, product_id,
                        product_name, category, quantity, unit_price, subtotal
                    FROM unified_order_items
                    WHERE unified_order_id = ?
                    """;

            List<Map<String, Object>> itemMaps = jdbcTemplate.queryForList(itemsSql, unifiedOrderId);
            List<UnifiedOrderItem> items = itemMaps.stream().map(map -> {
                UnifiedOrderItem item = new UnifiedOrderItem();
                item.setUnifiedItemId(((Number) map.get("unified_item_id")).intValue());
                item.setUnifiedOrderId(((Number) map.get("unified_order_id")).intValue());
                item.setProductId(((Number) map.get("product_id")).intValue());
                item.setProductName((String) map.get("product_name"));
                item.setCategory((String) map.get("category"));
                item.setQuantity(((Number) map.get("quantity")).intValue());
                item.setUnitPrice(
                        map.get("unit_price") != null ? new java.math.BigDecimal(map.get("unit_price").toString())
                                : null);
                item.setSubtotal(
                        map.get("subtotal") != null ? new java.math.BigDecimal(map.get("subtotal").toString()) : null);
                return item;
            }).toList();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("order", order);
            result.put("items", items);
            result.put("itemCount", items.size());
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched order detail: {}", unifiedOrderId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching order detail: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取订单统计信息 (按来源统计)
     */
    @GetMapping("/stats/by-source")
    public ResponseEntity<?> getOrderStatsBySource() {

        try {
            List<Map<String, Object>> stats = unifiedOrderMapper.selectOrderStatsBySource();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", stats);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched order statistics by source");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching order stats: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取商品销售统计
     */
    @GetMapping("/stats/product-sales")
    public ResponseEntity<?> getProductSalesStats() {

        try {
            List<Map<String, Object>> stats = unifiedOrderItemMapper.selectProductSalesStats();

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", stats);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched product sales statistics");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching product sales stats: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 按来源类型获取订单列表
     */
    @GetMapping("/by-source/{source}")
    public ResponseEntity<?> getOrdersBySource(
            @PathVariable String source,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize) {

        try {
            List<UnifiedOrder> orders = unifiedOrderMapper.selectList(null);
            orders = orders.stream()
                    .filter(o -> o.getSource().equalsIgnoreCase(source))
                    .toList();

            int total = orders.size();
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, total);
            List<UnifiedOrder> pageData = orders.subList(startIndex, endIndex);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("source", source);
            result.put("data", pageData);
            result.put("pagination", Map.of(
                    "page", page,
                    "pageSize", pageSize,
                    "total", total,
                    "totalPages", (int) Math.ceil((double) total / pageSize)));
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched {} orders from source: {}", pageData.size(), source);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching orders by source: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * 获取概览统计信息
     */
    @GetMapping("/overview")
    public ResponseEntity<?> getOverview() {

        try {
            String totalSql = "SELECT COUNT(*) FROM unified_orders";
            String appSql = "SELECT COUNT(*) FROM unified_orders WHERE source = 'APP'";
            String webSql = "SELECT COUNT(*) FROM unified_orders WHERE source = 'WEB'";

            Integer totalOrders = jdbcTemplate.queryForObject(totalSql, Integer.class);
            Integer appOrders = jdbcTemplate.queryForObject(appSql, Integer.class);
            Integer webOrders = jdbcTemplate.queryForObject(webSql, Integer.class);

            String stats_sql = """
                    SELECT
                        source,
                        COUNT(*) as order_count,
                        SUM(total_amount) as total_sales,
                        AVG(total_amount) as avg_order_value
                    FROM unified_orders
                    GROUP BY source
                    ORDER BY source
                    """;

            List<Map<String, Object>> sourceStats = jdbcTemplate.queryForList(stats_sql);

            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("totalOrders", totalOrders != null ? totalOrders : 0);
            result.put("appOrders", appOrders != null ? appOrders : 0);
            result.put("webOrders", webOrders != null ? webOrders : 0);
            result.put("sourceStats", sourceStats);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetched overview statistics");
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("Error fetching overview: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }
}

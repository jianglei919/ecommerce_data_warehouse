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
        order.setUnifiedOrderId(rs.getInt("order_id"));
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
            StringBuilder sql = new StringBuilder("SELECT * FROM dim_orders WHERE 1=1");
            List<Object> params = new ArrayList<>();

            if (source != null && !source.isEmpty()) {
                sql.append(" AND source = ?");
                params.add(source.toUpperCase());
            }
            if (status != null && !status.isEmpty()) {
                sql.append(" AND status = ?");
                params.add(status.toLowerCase());
            }

            sql.append(" ORDER BY order_date DESC, order_id DESC");

            // 获取总数
            String countSql = "SELECT COUNT(*) FROM dim_orders WHERE 1=1";
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
            String orderSql = "SELECT * FROM dim_orders WHERE order_id = ?";
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
                        item_id, order_id, product_id,
                        product_name, category, quantity, unit_price, subtotal
                    FROM dim_order_items
                    WHERE order_id = ?
                    """;

            List<Map<String, Object>> itemMaps = jdbcTemplate.queryForList(itemsSql, unifiedOrderId);
            List<UnifiedOrderItem> items = itemMaps.stream().map(map -> {
                UnifiedOrderItem item = new UnifiedOrderItem();
                item.setUnifiedItemId(((Number) map.get("item_id")).intValue());
                item.setUnifiedOrderId(((Number) map.get("order_id")).intValue());
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
            String totalSql = "SELECT COUNT(*) FROM dim_orders";
            String appSql = "SELECT COUNT(*) FROM dim_orders WHERE source = 'APP'";
            String webSql = "SELECT COUNT(*) FROM dim_orders WHERE source = 'WEB'";

            Integer totalOrders = jdbcTemplate.queryForObject(totalSql, Integer.class);
            Integer appOrders = jdbcTemplate.queryForObject(appSql, Integer.class);
            Integer webOrders = jdbcTemplate.queryForObject(webSql, Integer.class);

            String stats_sql = """
                    SELECT
                        source,
                        COUNT(*) as order_count,
                        SUM(total_amount) as total_sales,
                        AVG(total_amount) as avg_order_value
                    FROM dim_orders
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

    // =====================================================
    // OLAP 分析端点 (根据PDF Section 5设计)
    // =====================================================

    /**
     * OLAP操作1: Rollup - 按类别和时间聚合销售 (按月汇总)
     */
    @GetMapping("/analytics/rollup")
    public ResponseEntity<?> getOlapRollup(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer year) {

        try {
            String sql = """
                    SELECT d.source, d.category, f.year, f.month,
                           SUM(f.total_quantity) AS monthly_qty,
                           SUM(f.total_sales_amount) AS monthly_sales
                    FROM fact_sales_by_product_time f
                    JOIN dim_products d ON f.product_key = d.product_key
                    WHERE 1=1
                    """;

            List<Object> params = new ArrayList<>();

            if (source != null && !source.isEmpty()) {
                sql += " AND d.source = ?";
                params.add(source);
            }
            if (category != null && !category.isEmpty()) {
                sql += " AND d.category = ?";
                params.add(category);
            }
            if (year != null) {
                sql += " AND f.year = ?";
                params.add(year);
            }

            sql += " GROUP BY d.source, d.category, f.year, f.month ORDER BY d.source, f.year, f.month, d.category";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());

            log.info("Fetched OLAP Rollup data: {} rows", result.size());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "operation", "Rollup",
                    "data", result,
                    "count", result.size()));

        } catch (Exception e) {
            log.error("Error fetching OLAP Rollup: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * OLAP操作2: Drilldown - 按商品详细分析某类别的日度销售
     */
    @GetMapping("/analytics/drilldown")
    public ResponseEntity<?> getOlapDrilldown(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {

        try {
            String sql = """
                    SELECT d.source, d.product_id, d.product_name, d.category, f.year, f.month, f.day,
                           f.total_quantity, f.total_sales_amount
                    FROM fact_sales_by_product_time f
                    JOIN dim_products d ON f.product_key = d.product_key
                    WHERE 1=1
                    """;

            List<Object> params = new ArrayList<>();

            if (source != null && !source.isEmpty()) {
                sql += " AND d.source = ?";
                params.add(source);
            }
            if (category != null && !category.isEmpty()) {
                sql += " AND d.category = ?";
                params.add(category);
            }
            if (year != null) {
                sql += " AND f.year = ?";
                params.add(year);
            }
            if (month != null) {
                sql += " AND f.month = ?";
                params.add(month);
            }

            sql += " ORDER BY d.source, f.year, f.month, f.day, d.product_name";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());

            log.info("Fetched OLAP Drilldown data: {} rows", result.size());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "operation", "Drilldown",
                    "data", result,
                    "count", result.size()));

        } catch (Exception e) {
            log.error("Error fetching OLAP Drilldown: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * OLAP操作3: Slice - 单个维度的切片分析 (按类别筛选时间趋势)
     */
    @GetMapping("/analytics/slice")
    public ResponseEntity<?> getOlapSlice(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer year) {

        try {
            String sql = """
                    SELECT d.source, f.year, f.month, f.day, f.total_quantity, f.total_sales_amount
                    FROM fact_sales_by_product_time f
                    JOIN dim_products d ON f.product_key = d.product_key
                    WHERE 1=1
                    """;

            List<Object> params = new ArrayList<>();

            if (source != null && !source.isEmpty()) {
                sql += " AND d.source = ?";
                params.add(source);
            }
            if (category != null && !category.isEmpty()) {
                sql += " AND d.category = ?";
                params.add(category);
            }
            if (year != null) {
                sql += " AND f.year = ?";
                params.add(year);
            }

            sql += " ORDER BY d.source, f.year, f.month, f.day";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());

            log.info("Fetched OLAP Slice data: {} rows", result.size());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "operation", "Slice",
                    "data", result,
                    "count", result.size()));

        } catch (Exception e) {
            log.error("Error fetching OLAP Slice: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * OLAP操作4: Dice - 多维度的多值筛选分析
     */
    @GetMapping("/analytics/dice")
    public ResponseEntity<?> getOlapDice(
            @RequestParam(required = false) String categories,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String months) {

        try {
            String sql = """
                    SELECT d.source, d.category, f.year, f.month,
                           SUM(f.total_quantity) AS qty,
                           SUM(f.total_sales_amount) AS sales
                    FROM fact_sales_by_product_time f
                    JOIN dim_products d ON f.product_key = d.product_key
                    WHERE 1=1
                    """;

            List<Object> params = new ArrayList<>();

            if (source != null && !source.isEmpty()) {
                sql += " AND d.source = ?";
                params.add(source);
            }

            // Parse categories (comma-separated)
            if (categories != null && !categories.isEmpty()) {
                String[] categoryArray = categories.split(",");
                List<String> categoryList = new ArrayList<>();
                for (String cat : categoryArray) {
                    categoryList.add(cat.trim());
                }
                String placeholders = String.join(",", Collections.nCopies(categoryList.size(), "?"));
                sql += " AND d.category IN (" + placeholders + ")";
                params.addAll(categoryList);
            }

            if (year != null) {
                sql += " AND f.year = ?";
                params.add(year);
            }

            // Parse months (comma-separated)
            if (months != null && !months.isEmpty()) {
                String[] monthArray = months.split(",");
                List<Integer> monthList = new ArrayList<>();
                for (String m : monthArray) {
                    monthList.add(Integer.parseInt(m.trim()));
                }
                String placeholders = String.join(",", Collections.nCopies(monthList.size(), "?"));
                sql += " AND f.month IN (" + placeholders + ")";
                params.addAll(monthList);
            }

            sql += " GROUP BY d.source, d.category, f.year, f.month ORDER BY d.source, f.month, d.category";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());

            log.info("Fetched OLAP Dice data: {} rows", result.size());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "operation", "Dice",
                    "data", result,
                    "count", result.size()));

        } catch (Exception e) {
            log.error("Error fetching OLAP Dice: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    /**
     * OLAP操作5: Pivot - 交叉维度分析 (以行和列展现多维度数据)
     */
    @GetMapping("/analytics/pivot")
    public ResponseEntity<?> getOlapPivot(
            @RequestParam(required = false) Integer year) {

        try {
            String sql = """
                    SELECT d.category,
                           SUM(CASE WHEN f.month = 1 THEN f.total_sales_amount ELSE 0 END) AS Jan_Sales,
                           SUM(CASE WHEN f.month = 2 THEN f.total_sales_amount ELSE 0 END) AS Feb_Sales,
                           SUM(CASE WHEN f.month = 3 THEN f.total_sales_amount ELSE 0 END) AS Mar_Sales,
                           SUM(CASE WHEN f.month = 4 THEN f.total_sales_amount ELSE 0 END) AS Apr_Sales,
                           SUM(CASE WHEN f.month = 5 THEN f.total_sales_amount ELSE 0 END) AS May_Sales,
                           SUM(CASE WHEN f.month = 6 THEN f.total_sales_amount ELSE 0 END) AS Jun_Sales,
                           SUM(CASE WHEN f.month = 7 THEN f.total_sales_amount ELSE 0 END) AS Jul_Sales,
                           SUM(CASE WHEN f.month = 8 THEN f.total_sales_amount ELSE 0 END) AS Aug_Sales,
                           SUM(CASE WHEN f.month = 9 THEN f.total_sales_amount ELSE 0 END) AS Sep_Sales,
                           SUM(CASE WHEN f.month = 10 THEN f.total_sales_amount ELSE 0 END) AS Oct_Sales,
                           SUM(CASE WHEN f.month = 11 THEN f.total_sales_amount ELSE 0 END) AS Nov_Sales,
                           SUM(CASE WHEN f.month = 12 THEN f.total_sales_amount ELSE 0 END) AS Dec_Sales,
                           SUM(f.total_sales_amount) AS Total_Sales
                    FROM fact_sales_by_product_time f
                    JOIN dim_products d ON f.product_key = d.product_key
                    WHERE 1=1
                    """;

            List<Object> params = new ArrayList<>();

            if (year != null) {
                sql += " AND f.year = ?";
                params.add(year);
            } else {
                sql += " AND f.year = YEAR(CURDATE())";
            }

            sql += " GROUP BY d.category ORDER BY Total_Sales DESC";

            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, params.toArray());

            log.info("Fetched OLAP Pivot data: {} rows", result.size());
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "operation", "Pivot",
                    "data", result,
                    "count", result.size()));

        } catch (Exception e) {
            log.error("Error fetching OLAP Pivot: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }
}

package com.uwindsor.warehouse.controller;

import com.uwindsor.warehouse.event.OrderEvent;
import com.uwindsor.warehouse.service.ETLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

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
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", Collections.emptyList());
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetching sales by category: startDate={}, endDate={}", startDate, endDate);
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
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("data", Collections.emptyList());
            result.put("limit", limit);
            result.put("timestamp", LocalDateTime.now());

            log.info("Fetching top {} rated products", limit);
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

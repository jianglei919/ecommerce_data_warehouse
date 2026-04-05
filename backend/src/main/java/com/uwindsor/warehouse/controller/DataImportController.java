package com.uwindsor.warehouse.controller;

import com.uwindsor.warehouse.service.DataImportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 数据导入接口控制器
 * 处理产品和订单的导入操作
 */
@Slf4j
@RestController
@RequestMapping("/api/data/import")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DataImportController {

    @Autowired
    private DataImportService dataImportService;

    /**
     * 导入产品数据
     */
    @PostMapping("/products")
    public ResponseEntity<?> importProducts(@RequestBody Map<String, Object> request) {
        try {
            String db = (String) request.get("db");
            List<?> dataList = (List<?>) request.get("data");

            if (db == null || db.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "业务库参数不能为空 (APP|WEB)"));
            }

            if (dataList == null || dataList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "产品数据不能为空"));
            }

            log.info("📦 导入{}个产品到{}库", dataList.size(), db);

            int count = dataImportService.importProducts(db, dataList);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", String.format("成功导入%d个产品到%s库，已发送Kafka消息", count, db),
                    "count", count,
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            log.error("产品导入失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "产品导入失败: " + e.getMessage()));
        }
    }

    /**
     * 导入订单数据
     */
    @PostMapping("/orders")
    public ResponseEntity<?> importOrders(@RequestBody Map<String, Object> request) {
        try {
            String db = (String) request.get("db");
            List<?> dataList = (List<?>) request.get("data");

            if (db == null || db.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "业务库参数不能为空 (APP|WEB)"));
            }

            if (dataList == null || dataList.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "订单数据不能为空"));
            }

            log.info("📦 导入{}个订单到{}库", dataList.size(), db);

            int count = dataImportService.importOrders(db, dataList);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", String.format("成功导入%d个订单到%s库，已发送Kafka消息", count, db),
                    "count", count,
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            log.error("订单导入失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "订单导入失败: " + e.getMessage()));
        }
    }

    /**
     * 手动同步订单到仓库（用于处理直接插入数据库的订单）
     */
    @PostMapping("/orders/sync")
    public ResponseEntity<?> syncOrders(@RequestBody Map<String, Object> request) {
        try {
            String db = (String) request.get("db");
            List<?> orderIds = (List<?>) request.get("order_ids");

            if (db == null || db.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "业务库参数不能为空 (APP|WEB)"));
            }

            if (orderIds == null || orderIds.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "status", "error",
                        "message", "订单ID列表不能为空"));
            }

            log.info("📦 同步{}个订单到仓库库, db={}", orderIds.size(), db);

            int count = dataImportService.synchronizeOrders(db, orderIds);

            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", String.format("成功同步%d个订单到仓库库，已发送Kafka消息", count),
                    "count", count,
                    "timestamp", LocalDateTime.now()));

        } catch (Exception e) {
            log.error("订单同步失败: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "status", "error",
                    "message", "订单同步失败: " + e.getMessage()));
        }
    }
}

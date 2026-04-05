package com.uwindsor.warehouse.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uwindsor.warehouse.event.OrderEvent;
import com.uwindsor.warehouse.service.ETLService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Kafka 消费者 - ETL 事件处理
 * 监听所有订单事件，进行数据转换和仓库写入
 */
@Slf4j
@Component
public class KafkaEventConsumer {

    @Autowired
    private ETLService etlService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 监听订单事件 - 批量处理模式
     * 触发 ETL 流程：提取数据 -> 转换 -> 加载到仓库
     */
    @KafkaListener(topics = "${app.kafka.topics.order-events}", groupId = "warehouse-etl-group", containerFactory = "kafkaListenerContainerFactory")
    public void handleOrderEvents(
            @Payload List<OrderEvent> events,
            Acknowledgment acknowledgment) {

        if (events == null || events.isEmpty()) {
            log.debug("Empty event batch received");
            return;
        }

        long startTime = System.currentTimeMillis();
        log.info("Processing batch of {} events", events.size());

        try {
            // 批量 ETL 处理
            etlService.processBatch(events);

            // 手动提交偏移量
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("Batch of {} events committed successfully (took {} ms)",
                        events.size(), System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            log.error("Error processing event batch: {}", e.getMessage(), e);
            // 错误事件会被发送到 DLQ 或者记录到 sync_log 表
            etlService.logErrors(events, e);
        }
    }

    /**
     * 监听产品事件 - 处理产品同步
     */
    @KafkaListener(topics = "${app.kafka.topics.product-events}", groupId = "warehouse-product-sync-group", containerFactory = "productListenerContainerFactory")
    public void handleProductEvents(
            @Payload List<Map<String, Object>> events,
            Acknowledgment acknowledgment) {

        if (events == null || events.isEmpty()) {
            log.debug("Empty product event batch received");
            return;
        }

        long startTime = System.currentTimeMillis();
        log.info("Processing batch of {} product events", events.size());

        try {
            // 处理产品同步
            etlService.processBatchProducts(events);

            // 手动提交偏移量
            if (acknowledgment != null) {
                acknowledgment.acknowledge();
                log.info("Batch of {} product events committed successfully (took {} ms)",
                        events.size(), System.currentTimeMillis() - startTime);
            }

        } catch (Exception e) {
            log.error("Error processing product event batch: {}", e.getMessage(), e);
        }
    }

    /**
     * 监听同步事件 - 用于监控和调试
     */
    @KafkaListener(topics = "${app.kafka.topics.sync-events}", groupId = "warehouse-sync-monitor", containerFactory = "kafkaListenerContainerFactory")
    public void handleSyncEvents(
            @Payload List<String> events,
            Acknowledgment acknowledgment) {

        if (events == null || events.isEmpty()) {
            return;
        }

        log.debug("Received {} sync events", events.size());

        try {
            // 处理同步监控事件
            for (String event : events) {
                log.debug("Sync event: {}", event);
            }

            if (acknowledgment != null) {
                acknowledgment.acknowledge();
            }

        } catch (Exception e) {
            log.error("Error processing sync events: {}", e.getMessage(), e);
        }
    }

}

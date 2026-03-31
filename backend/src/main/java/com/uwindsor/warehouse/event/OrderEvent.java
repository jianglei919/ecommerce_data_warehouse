package com.uwindsor.warehouse.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单事件模型 - 用于Kafka消息传输
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    @JsonProperty("event_id")
    private String eventId;

    /**
     * 事件类型: ORDER_CREATED, ORDER_UPDATED, ORDER_DELETED
     */
    @JsonProperty("event_type")
    private String eventType;

    /**
     * 数据来源: APP, WEB
     */
    @JsonProperty("source")
    private String source;

    /**
     * 订单ID (App) 或 订单号 (Web)
     */
    @JsonProperty("order_id")
    private String orderId;

    /**
     * 用户ID
     */
    @JsonProperty("user_id")
    private Long userId;

    /**
     * 订单日期
     */
    @JsonProperty("order_date")
    private String orderDate;

    /**
     * 订单总金额
     */
    @JsonProperty("total_amount")
    private Double totalAmount;

    /**
     * 订单状态
     */
    @JsonProperty("order_status")
    private String orderStatus;

    /**
     * 商品数量
     */
    @JsonProperty("item_count")
    private Integer itemCount;

    /**
     * 核心数据 (JSON格式的订单明细)
     */
    @JsonProperty("payload")
    private String payload;

    /**
     * 事件时间戳
     */
    @JsonProperty("event_timestamp")
    private Long eventTimestamp;

    /**
     * 事件创建时间
     */
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    /**
     * 版本号 (用于乐观锁)
     */
    @JsonProperty("version")
    private Integer version;

    /**
     * 重试次数
     */
    @JsonProperty("retry_count")
    private Integer retryCount;

}

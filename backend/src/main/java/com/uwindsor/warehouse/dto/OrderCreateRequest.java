package com.uwindsor.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 订单创建请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    /**
     * 订单ID - 支持 ORD-xxx, ORDER-xxx, APP-xxx 等格式
     */
    private String orderId;

    /**
     * 用户ID (可选，不存在时会创建默认用户)
     */
    private Long userId;

    /**
     * 产品ID (可选)
     */
    private Long productId;

    /**
     * 订单总金额
     */
    private Double totalAmount;

    /**
     * 数据来源: APP 或 WEB
     */
    private String source = "APP";

    /**
     * 订单状态: PENDING, SHIPPED, DELIVERED, CANCELLED
     */
    private String status = "PENDING";

    /**
     * 商品数量
     */
    private Integer quantity = 1;

    /**
     * 产品名称 (可选)
     */
    private String productName;

    /**
     * 订单备注
     */
    private String remarks;
}

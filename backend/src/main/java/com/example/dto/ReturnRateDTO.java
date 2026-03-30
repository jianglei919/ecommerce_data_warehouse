package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 退货率分析 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReturnRateDTO {

    private Integer productId;

    private String productName;

    private String category;

    private Long totalOrderQty; // 订单总数

    private Long returnQty; // 退货数

    private BigDecimal returnRate; // 退货率 = 退货数/订单总数 * 100

    private String reason; // 退货原因 (最常见)

    private Integer reasonCount; // 该原因的退货次数
}

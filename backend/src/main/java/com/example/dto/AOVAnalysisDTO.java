package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 平均订单价值 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AOVAnalysisDTO {

    private String date;

    private BigDecimal aov; // 平均订单价值

    private Long orderCount; // 订单数

    private BigDecimal totalSales; // 总销售额

    private BigDecimal maxOrderValue;

    private BigDecimal minOrderValue;
}

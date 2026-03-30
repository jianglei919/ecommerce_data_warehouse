package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 季节销售分析 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeasonSalesDTO {

    private String productName;

    private String category;

    private String season; // spring, summer, autumn, winter

    private Integer year;

    private Long totalQty;

    private BigDecimal totalAmount;

    private Long orderCount;

    private BigDecimal avgOrderValue;
}

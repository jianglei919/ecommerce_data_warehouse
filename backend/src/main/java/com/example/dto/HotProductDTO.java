package com.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 热销商品 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HotProductDTO {

    private Integer productId;

    private String productName;

    private String category;

    private Long salesQty; // 销量

    private BigDecimal salesAmount; // 销售金额

    private Double avgRating; // 平均评分

    private Long reviewCount; // 评论数

    private Double combinedScore; // 综合评分 = 销量权重 + 评分权重
}

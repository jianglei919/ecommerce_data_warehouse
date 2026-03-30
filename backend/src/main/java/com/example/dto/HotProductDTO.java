package com.example.dto;

import java.math.BigDecimal;

/**
 * 热销商品 DTO
 */
public class HotProductDTO {

    private Integer productId;

    private String productName;

    private String category;

    private Long salesQty; // 销量

    private BigDecimal salesAmount; // 销售金额

    private Double avgRating; // 平均评分

    private Long reviewCount; // 评论数

    private Double combinedScore; // 综合评分 = 销量权重 + 评分权重

    public HotProductDTO() {
    }

    public HotProductDTO(Integer productId, String productName, String category, Long salesQty,
            BigDecimal salesAmount, Double avgRating, Long reviewCount, Double combinedScore) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.salesQty = salesQty;
        this.salesAmount = salesAmount;
        this.avgRating = avgRating;
        this.reviewCount = reviewCount;
        this.combinedScore = combinedScore;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getSalesQty() {
        return salesQty;
    }

    public void setSalesQty(Long salesQty) {
        this.salesQty = salesQty;
    }

    public BigDecimal getSalesAmount() {
        return salesAmount;
    }

    public void setSalesAmount(BigDecimal salesAmount) {
        this.salesAmount = salesAmount;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Long getReviewCount() {
        return reviewCount;
    }

    public void setReviewCount(Long reviewCount) {
        this.reviewCount = reviewCount;
    }

    public Double getCombinedScore() {
        return combinedScore;
    }

    public void setCombinedScore(Double combinedScore) {
        this.combinedScore = combinedScore;
    }
}

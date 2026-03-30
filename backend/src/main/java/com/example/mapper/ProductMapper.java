package com.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.dto.HotProductDTO;
import com.example.entity.Product;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 商品 Mapper
 */
@Repository
public interface ProductMapper extends BaseMapper<Product> {

    /**
     * 查询热销商品（按销量）- 业务库
     */
    @Select("SELECT " +
            "p.product_id, p.name productName, p.category, " +
            "SUM(oi.quantity) salesQty, " +
            "SUM(oi.line_total) salesAmount, " +
            "AVG(pr.rating) avgRating, " +
            "COUNT(DISTINCT pr.review_id) reviewCount " +
            "FROM products p " +
            "LEFT JOIN order_items oi ON p.product_id = oi.product_id " +
            "LEFT JOIN product_reviews pr ON p.product_id = pr.product_id " +
            "GROUP BY p.product_id, p.name, p.category " +
            "ORDER BY salesQty DESC " +
            "LIMIT 10")
    List<HotProductDTO> getTopProductsBySales();

    /**
     * 查询热销商品（按评论和评分）
     */
    @Select("SELECT " +
            "p.product_id, p.name productName, p.category, " +
            "SUM(oi.quantity) salesQty, " +
            "SUM(oi.line_total) salesAmount, " +
            "AVG(pr.rating) avgRating, " +
            "COUNT(DISTINCT pr.review_id) reviewCount " +
            "FROM products p " +
            "LEFT JOIN order_items oi ON p.product_id = oi.product_id " +
            "LEFT JOIN product_reviews pr ON p.product_id = pr.product_id " +
            "GROUP BY p.product_id, p.name, p.category " +
            "HAVING COUNT(DISTINCT pr.review_id) >= 5 " +
            "ORDER BY avgRating DESC " +
            "LIMIT 10")
    List<HotProductDTO> getTopProductsByReviewsAndRating();
}

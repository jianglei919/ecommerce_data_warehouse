package com.example.service.impl;

import com.example.dto.HotProductDTO;
import com.example.mapper.ProductMapper;
import com.example.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分析 Service 实现
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<HotProductDTO> getTopProductsBySales() {
        return productMapper.getTopProductsBySales();
    }

    @Override
    public List<HotProductDTO> getTopProductsByReviewsAndRating() {
        return productMapper.getTopProductsByReviewsAndRating();
    }

    @Override
    public List<HotProductDTO> getCombinedTopProducts() {
        // 60% 销量权重 + 40% 评分权重
        return productMapper.getTopProductsBySales()
                .stream()
                .peek(p -> {
                    double salesScore = p.getSalesQty() != null ? p.getSalesQty().doubleValue() : 0;
                    double reviewScore = p.getAvgRating() != null ? p.getAvgRating() * 20 : 0; // 5分制转100分制
                    double combinedScore = salesScore * 0.6 + reviewScore * 0.4;
                    p.setCombinedScore(combinedScore);
                })
                .sorted((a, b) -> Double.compare(b.getCombinedScore(), a.getCombinedScore()))
                .limit(10)
                .collect(Collectors.toList());
    }
}

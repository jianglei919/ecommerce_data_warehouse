package com.example.service;

import com.example.dto.HotProductDTO;
import com.example.dto.SeasonSalesDTO;
import com.example.dto.AOVAnalysisDTO;
import com.example.dto.ReturnRateDTO;

import java.util.List;
import java.util.Map;

/**
 * 商品分析 Service 接口
 */
public interface ProductService {

    /**
     * 获取热销商品（按销量）
     */
    List<HotProductDTO> getTopProductsBySales();

    /**
     * 获取热销商品（按评论和评分）
     */
    List<HotProductDTO> getTopProductsByReviewsAndRating();

    /**
     * 获取综合热销商品
     */
    List<HotProductDTO> getCombinedTopProducts();
}

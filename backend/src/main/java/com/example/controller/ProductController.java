package com.example.controller;

import com.example.dto.HotProductDTO;
import com.example.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品分析 API Controller
 */
@RestController
@RequestMapping("/api/products")
@Api(tags = "商品分析")
public class ProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取热销商品（按销量）
     */
    @GetMapping("/top-sales")
    @ApiOperation("获取热销商品（按销量排序）")
    public Map<String, Object> getTopProductsBySales() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HotProductDTO> data = productService.getTopProductsBySales();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取热销商品（按评论和评分）
     */
    @GetMapping("/top-reviews")
    @ApiOperation("获取热销商品（按评论和评分排序）")
    public Map<String, Object> getTopProductsByReviewsAndRating() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HotProductDTO> data = productService.getTopProductsByReviewsAndRating();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            response.put("data", null);
        }
        return response;
    }

    /**
     * 获取综合热销商品
     */
    @GetMapping("/combined-top")
    @ApiOperation("获取综合热销商品（销量60% + 评分40%）")
    public Map<String, Object> getCombinedTopProducts() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<HotProductDTO> data = productService.getCombinedTopProducts();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);
        } catch (Exception e) {
            response.put("code", 500);
            response.put("message", "Error: " + e.getMessage());
            response.put("data", null);
        }
        return response;
    }
}

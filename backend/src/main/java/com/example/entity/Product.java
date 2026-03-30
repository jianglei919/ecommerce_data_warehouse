package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 商品实体类
 */
@Data
@NoArgsConstructor
@TableName("products")
public class Product {

    @TableId(type = IdType.AUTO)
    private Integer productId;

    private String name;

    private String description;

    private String category;

    private BigDecimal price;

    private BigDecimal cost;

    private String brand;

    private Integer stockQty;

    private Boolean isActive;

    private Date createTime;

    private Date updateTime;
}

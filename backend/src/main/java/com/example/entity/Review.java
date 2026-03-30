package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 商品评论实体类
 */
@Data
@NoArgsConstructor
@TableName("product_reviews")
public class Review {

    @TableId(type = IdType.AUTO)
    private Integer reviewId;

    private Integer productId;

    private Integer userId;

    private Integer rating; // 1-5

    private String comment;

    private Date reviewDate;

    private Date createTime;
}

package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单实体类
 */
@Data
@NoArgsConstructor
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Integer orderId;

    private Integer userId;

    private Date orderDate;

    private BigDecimal totalAmount;

    private String status; // pending, completed, cancelled

    private Date createTime;

    private Date updateTime;
}

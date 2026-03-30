package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 订单项目实体类
 */
@Data
@NoArgsConstructor
@TableName("order_items")
public class OrderItem {

    @TableId(type = IdType.AUTO)
    private Integer itemId;

    private Integer orderId;

    private Integer productId;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal lineTotal;
}

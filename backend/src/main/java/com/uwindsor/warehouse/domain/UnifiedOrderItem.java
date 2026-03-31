package com.uwindsor.warehouse.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 统一订单项详情表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("unified_order_items")
public class UnifiedOrderItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "unified_item_id", type = IdType.AUTO)
    private Integer unifiedItemId;

    /** 统一订单ID */
    private Integer unifiedOrderId;

    /** 商品ID */
    private Integer productId;

    /** 商品名称 */
    private String productName;

    /** 商品类别 */
    private String category;

    /** 购买数量 */
    private Integer quantity;

    /** 单价 */
    private BigDecimal unitPrice;

    /** 小计 */
    private BigDecimal subtotal;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

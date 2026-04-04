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
import java.time.LocalDate;

/**
 * 订单维度表 (V2新增)
 * 聚合来自App和Web两个业务数据源的订单数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("dim_orders")
public class UnifiedOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "order_id", type = IdType.AUTO)
    private Integer unifiedOrderId;

    /** 数据源: APP 或 WEB */
    private String source;

    /** App系统订单ID (可为NULL) */
    private Integer appOrderId;

    /** Web系统订单号 (可为NULL) */
    private String webOrderNo;

    /** 用户ID */
    private Integer userId;

    /** 订单日期 */
    private LocalDate orderDate;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 订单状态: pending, completed, cancelled */
    private String status;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;
}

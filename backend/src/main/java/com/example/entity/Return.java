package com.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 退货实体类
 */
@Data
@NoArgsConstructor
@TableName("returns")
public class Return {

    @TableId(type = IdType.AUTO)
    private Integer returnId;

    private Integer orderId;

    private Integer productId;

    private Date returnDate;

    private Integer returnQty;

    private String reason;

    private String status; // pending, approved, rejected, completed

    private Date createTime;
}
